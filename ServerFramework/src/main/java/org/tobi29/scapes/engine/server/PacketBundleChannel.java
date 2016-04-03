/*
 * Copyright 2012-2015 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tobi29.scapes.engine.server;

import java8.util.Optional;
import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.ThreadLocalUtil;
import org.tobi29.scapes.engine.utils.io.ByteBufferStream;
import org.tobi29.scapes.engine.utils.io.CompressionUtil;
import org.tobi29.scapes.engine.utils.io.RandomReadableByteStream;
import org.tobi29.scapes.engine.utils.io.RandomWritableByteStream;
import org.tobi29.scapes.engine.utils.task.TaskExecutor;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PacketBundleChannel {
    private static final int BUNDLE_HEADER_SIZE = 4;
    private static final int BUNDLE_MAX_SIZE = 1 << 10 << 10 << 6;
    private static final ThreadLocal<List<WeakReference<ByteBuffer>>>
            BUFFER_CACHE = ThreadLocalUtil.of(ArrayList::new);
    private static final ByteBuffer EMPTY_BUFFER = BufferCreator.bytes(0);
    private final RemoteAddress address;
    private final SocketChannel channel;
    private final TaskExecutor taskExecutor;
    private final SSLHandle ssl;
    private final AtomicInteger taskCounter = new AtomicInteger();
    private final AtomicBoolean verified = new AtomicBoolean();
    private final ByteBufferStream dataStreamOut =
            new ByteBufferStream(BufferCreator::bytes,
                    length -> length + 102400), byteBufferStreamOut =
            new ByteBufferStream(BufferCreator::bytes,
                    length -> length + 102400);
    private final Queue<ByteBuffer> queue = new ConcurrentLinkedQueue<>();
    private final CompressionUtil.Filter deflater, inflater;
    private final AtomicInteger inRate = new AtomicInteger(), outRate =
            new AtomicInteger();
    private final SSLEngine engine;
    private final ByteBufferStream myNetData =
            new ByteBufferStream(BufferCreator::bytes,
                    length -> length + 16384);
    private final ByteBufferStream peerAppData =
            new ByteBufferStream(BufferCreator::bytes,
                    length -> length + 16384);
    private final ByteBufferStream peerNetData =
            new ByteBufferStream(BufferCreator::bytes,
                    length -> length + 16384);
    private ByteBuffer output, input = BufferCreator.bytes(1024);
    private Optional<ByteBuffer> spill = Optional.empty();
    private Optional<Selector> selector = Optional.empty();
    private Optional<IOException> verifyException = Optional.empty();
    private boolean hasInput, close;
    private State state = State.HANDSHAKE;

    public PacketBundleChannel(RemoteAddress address, SocketChannel channel,
            TaskExecutor taskExecutor, SSLHandle ssl, boolean client)
            throws IOException {
        this.address = address;
        this.channel = channel;
        this.taskExecutor = taskExecutor;
        this.ssl = ssl;
        deflater = new CompressionUtil.ZDeflater(1);
        inflater = new CompressionUtil.ZInflater();
        engine = ssl.newEngine(address);
        engine.setUseClientMode(client);
        myNetData.buffer().limit(0);
        input.limit(BUNDLE_HEADER_SIZE);
        engine.beginHandshake();
    }

    public RandomWritableByteStream getOutputStream() {
        return dataStreamOut;
    }

    public int bundleSize() {
        return dataStreamOut.buffer().position();
    }

    public void queueBundle() throws IOException {
        dataStreamOut.buffer().flip();
        byteBufferStreamOut.buffer().clear();
        CompressionUtil.filter(dataStreamOut, byteBufferStreamOut, deflater);
        byteBufferStreamOut.buffer().flip();
        int size = byteBufferStreamOut.buffer().remaining();
        if (size > BUNDLE_MAX_SIZE) {
            throw new IOException("Bundle size too large: " + size);
        }
        ByteBuffer bundle = buffer(BUNDLE_HEADER_SIZE + size);
        bundle.putInt(size);
        bundle.put(byteBufferStreamOut.buffer());
        bundle.flip();
        queue.add(bundle);
        dataStreamOut.buffer().clear();
        selector.ifPresent(Selector::wakeup);
    }

    public boolean process() throws IOException {
        while (true) {
            // Glorious design on Java's part:
            // The SSLEngine locks whilst delegated task runs
            // Solution: No touchy
            if (taskCounter.get() > 0) {
                return false;
            }
            if (flush()) {
                return false;
            }
            if (state == State.CLOSED) {
                return true;
            }
            if (state == State.HANDSHAKE) {
                if (handshake()) {
                    if (ssl.requiresVerification()) {
                        state = State.VERIFY;
                        taskExecutor.runTask(() -> {
                            try {
                                Certificate[] certificates = engine.getSession()
                                        .getPeerCertificates();
                                X509Certificate[] x509Certificates =
                                        Streams.of(certificates)
                                                .filter(certificate -> certificate instanceof X509Certificate)
                                                .map(certificate -> (X509Certificate) certificate)
                                                .toArray(
                                                        X509Certificate[]::new);
                                try {
                                    ssl.verifySession(address, engine,
                                            x509Certificates);
                                    verified.set(true);
                                } catch (IOException e) {
                                    if (x509Certificates.length == 0 ||
                                            !ssl.certificateFeedback(
                                                    x509Certificates)) {
                                        verifyException = Optional.of(e);
                                    } else {
                                        verified.set(true);
                                    }
                                }
                            } catch (SSLPeerUnverifiedException e) {
                                verifyException = Optional.of(e);
                            }
                        }, "SSL-Verify");
                    } else {
                        state = State.OPEN;
                    }
                } else {
                    return false;
                }
            }
            if (state == State.VERIFY) {
                if (verifyException.isPresent()) {
                    throw new IOException(verifyException.get());
                }
                if (verified.get()) {
                    state = State.OPEN;
                } else {
                    return false;
                }
            }
            if (state == State.CLOSING && handshake()) {
                state = State.CLOSED;
                continue;
            }
            if (output != null) {
                writeSSL(output);
                if (output.hasRemaining()) {
                    continue;
                }
                BUFFER_CACHE.get().add(new WeakReference<>(output));
            }
            output = queue.poll();
            if (output == null) {
                if (state == State.OPEN && close) {
                    engine.closeOutbound();
                    state = State.CLOSING;
                } else {
                    return false;
                }
            }
        }
    }

    public Optional<RandomReadableByteStream> fetch() throws IOException {
        if (state != State.OPEN) {
            return Optional.empty();
        }
        ByteBuffer buffer;
        if (spill.isPresent()) {
            buffer = spill.get();
            spill = Optional.empty();
        } else {
            Optional<ByteBuffer> fetch = readSSL();
            if (!fetch.isPresent()) {
                return Optional.empty();
            }
            buffer = fetch.get();
        }
        if (buffer.remaining() > input.remaining()) {
            int limit = buffer.limit();
            buffer.limit(buffer.position() + input.remaining());
            input.put(buffer);
            buffer.limit(limit);
            spill = Optional.of(buffer);
        } else {
            input.put(buffer);
        }
        if (input.hasRemaining()) {
            return Optional.empty();
        }
        input.flip();
        if (!hasInput) {
            if (input.remaining() != BUNDLE_HEADER_SIZE) {
                throw new IOException(
                        "Invalid bundle header size: " + input.remaining());
            }
            int limit = input.getInt();
            if (limit > BUNDLE_MAX_SIZE) {
                throw new IOException("Bundle size too large: " + limit);
            }
            if (input.capacity() < limit) {
                BUFFER_CACHE.get().add(new WeakReference<>(input));
                input = buffer(limit);
            } else {
                input.clear();
            }
            input.limit(limit);
            hasInput = true;
            return Optional.empty();
        }
        byteBufferStreamOut.buffer().clear();
        CompressionUtil.filter(new ByteBufferStream(input), byteBufferStreamOut,
                inflater);
        byteBufferStreamOut.buffer().flip();
        hasInput = false;
        input.clear().limit(BUNDLE_HEADER_SIZE);
        return Optional.of(byteBufferStreamOut);
    }

    public void register(Selector selector, int opt) throws IOException {
        channel.register(selector, opt);
        this.selector = Optional.of(selector);
    }

    public void close() throws IOException {
        channel.close();
        deflater.close();
        inflater.close();
    }

    public void requestClose() {
        close = true;
    }

    public int getOutputRate() {
        return outRate.getAndSet(0);
    }

    public int getInputRate() {
        return inRate.getAndSet(0);
    }

    public Optional<InetSocketAddress> getRemoteAddress() {
        SocketAddress address = channel.socket().getRemoteSocketAddress();
        if (address instanceof InetSocketAddress) {
            return Optional.of((InetSocketAddress) address);
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return channel.socket().getRemoteSocketAddress().toString();
    }

    private ByteBuffer buffer(int capacity) {
        List<WeakReference<ByteBuffer>> bufferCache = BUFFER_CACHE.get();
        ByteBuffer bundle = null;
        int i = 0;
        while (i < bufferCache.size()) {
            ByteBuffer cacheBuffer = bufferCache.get(i).get();
            if (cacheBuffer == null) {
                bufferCache.remove(i);
            } else if (cacheBuffer.capacity() >= capacity) {
                bufferCache.remove(i);
                bundle = cacheBuffer;
                bundle.clear().limit(capacity);
                break;
            } else {
                i++;
            }
        }
        if (bundle == null) {
            bundle = BufferCreator.bytes(capacity);
        }
        return bundle;
    }

    private Optional<ByteBuffer> readSSL() throws IOException {
        fill();
        do {
            peerAppData.buffer().clear();
            SSLEngineResult result =
                    engine.unwrap(peerNetData.buffer(), peerAppData.buffer());
            switch (result.getStatus()) {
                case OK:
                    peerAppData.buffer().flip();
                    peerNetData.buffer().compact();
                    return Optional.of(peerAppData.buffer());
                case BUFFER_OVERFLOW:
                    peerAppData.grow();
                    break;
                case BUFFER_UNDERFLOW:
                    peerNetData.buffer().compact();
                    if (!peerNetData.hasRemaining()) {
                        peerNetData.grow();
                    }
                    return Optional.empty();
                case CLOSED:
                    engine.closeOutbound();
                    state = State.CLOSING;
                    peerNetData.buffer().compact();
                    return Optional.empty();
                default:
                    throw new IllegalStateException(
                            "Invalid SSL status: " + result.getStatus());
            }
        } while (peerNetData.hasRemaining());
        peerNetData.buffer().compact();
        return Optional.empty();
    }

    private void writeSSL(ByteBuffer buffer) throws IOException {
        while (true) {
            myNetData.buffer().clear();
            SSLEngineResult result = engine.wrap(buffer, myNetData.buffer());
            switch (result.getStatus()) {
                case OK:
                    myNetData.buffer().flip();
                    return;
                case BUFFER_OVERFLOW:
                    myNetData.grow();
                    break;
                case BUFFER_UNDERFLOW:
                    throw new SSLException(
                            "Buffer underflow occurred after a wrap. I don't think we should ever get here.");
                case CLOSED:
                    engine.closeOutbound();
                    state = State.CLOSING;
                    myNetData.buffer().flip();
                    return;
                default:
                    throw new IllegalStateException(
                            "Invalid SSL status: " + result.getStatus());
            }
        }
    }

    private boolean handshake() throws IOException {
        switch (engine.getHandshakeStatus()) {
            case FINISHED:
            case NOT_HANDSHAKING:
                return true;
            case NEED_UNWRAP:
                readSSL();
                break;
            case NEED_WRAP:
                writeSSL(EMPTY_BUFFER);
                break;
            case NEED_TASK:
                Runnable task = engine.getDelegatedTask();
                if (task != null) {
                    taskCounter.incrementAndGet();
                    taskExecutor.runTask(() -> {
                        task.run();
                        taskCounter.decrementAndGet();
                    }, "SSLEngine-Task");
                }
                break;
            default:
                throw new IllegalStateException(
                        "Invalid SSL status: " + engine.getHandshakeStatus());
        }
        return false;
    }

    private void fill() throws IOException {
        int read = channel.read(peerNetData.buffer());
        peerNetData.buffer().flip();
        if (read < 0) {
            engine.closeInbound();
            state = State.CLOSING;
        }
        inRate.getAndAdd(read);
    }

    private boolean flush() throws IOException {
        if (!myNetData.hasRemaining()) {
            return false;
        }
        int write = channel.write(myNetData.buffer());
        if (write < 0) {
            engine.closeOutbound();
            state = State.CLOSING;
        }
        outRate.getAndAdd(write);
        return myNetData.hasRemaining();
    }

    private enum State {
        HANDSHAKE,
        VERIFY,
        OPEN,
        CLOSING,
        CLOSED
    }
}