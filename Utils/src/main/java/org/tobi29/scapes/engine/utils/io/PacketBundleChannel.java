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

package org.tobi29.scapes.engine.utils.io;

import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.StringLongHash;
import org.tobi29.scapes.engine.utils.UnsupportedJVMException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PacketBundleChannel {
    private static final IvParameterSpec IV;
    private static final ChecksumUtil.Algorithm CHECKSUM_ALGORITHM =
            ChecksumUtil.Algorithm.SHA256;
    private static final int CHECKSUM_LENGTH = CHECKSUM_ALGORITHM.bytes();
    private static final int BUNDLE_HEADER_SIZE = 4;
    private static final int BUNDLE_MAX_SIZE = 1 << 10 << 10 << 6;
    private static final ThreadLocal<List<WeakReference<ByteBuffer>>>
            BUFFER_CACHE = ThreadLocal.withInitial(ArrayList::new);

    static {
        Random random = new Random(
                StringLongHash.hash("Totally secure initialization vector :P"));
        byte[] array = new byte[16];
        random.nextBytes(array);
        IV = new IvParameterSpec(array);
    }

    private final SocketChannel channel;
    private final ByteBufferStream dataStreamOut = new ByteBufferStream(
            length -> BufferCreator.bytes(length + 102400)),
            byteBufferStreamOut = new ByteBufferStream(
                    length -> BufferCreator.bytes(length + 102400));
    private final ByteBuffer header = BufferCreator.bytes(BUNDLE_HEADER_SIZE);
    private final byte[] checksum = new byte[CHECKSUM_LENGTH];
    private final Queue<ByteBuffer> queue = new ConcurrentLinkedQueue<>();
    private final CompressionUtil.Filter deflater, inflater;
    private final MessageDigest digest = CHECKSUM_ALGORITHM.digest();
    private final Cipher encryptCipher, decryptCipher;
    private final AtomicInteger inRate = new AtomicInteger(), outRate =
            new AtomicInteger();
    private final List<WeakReference<ByteBuffer>> bufferCache;
    private Optional<Selector> selector = Optional.empty();
    private boolean encrypt, hasInput;
    private ByteBuffer output, input = BufferCreator.bytes(1024);

    public PacketBundleChannel(SocketChannel channel) {
        this(channel, null, null);
    }

    public PacketBundleChannel(SocketChannel channel, byte[] encryptKey,
            byte[] decryptKey) {
        this.channel = channel;
        deflater = new CompressionUtil.ZDeflater(1);
        inflater = new CompressionUtil.ZInflater();
        try {
            encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new UnsupportedJVMException(e);
        }
        bufferCache = BUFFER_CACHE.get();
        setKey(encryptKey, decryptKey);
    }

    public WritableByteStream getOutputStream() {
        return dataStreamOut;
    }

    public int bundleSize() {
        return dataStreamOut.buffer().position();
    }

    public void queueBundle() throws IOException {
        dataStreamOut.buffer().flip();
        if (!dataStreamOut.buffer().hasRemaining()) {
            dataStreamOut.buffer().clear();
            return;
        }
        byteBufferStreamOut.buffer().clear();
        byteBufferStreamOut.position(CHECKSUM_LENGTH);
        CompressionUtil.filter(dataStreamOut, byteBufferStreamOut, deflater);
        byteBufferStreamOut.buffer().flip().position(CHECKSUM_LENGTH);
        digest.update(byteBufferStreamOut.buffer());
        byteBufferStreamOut.buffer().flip();
        byteBufferStreamOut.put(digest.digest());
        byteBufferStreamOut.buffer().rewind();
        ByteBuffer bundle;
        if (encrypt) {
            bundle = buffer(BUNDLE_HEADER_SIZE + encryptCipher
                    .getOutputSize(byteBufferStreamOut.buffer().remaining()));
        } else {
            bundle = buffer(BUNDLE_HEADER_SIZE +
                    byteBufferStreamOut.buffer().remaining());
        }
        bundle.position(BUNDLE_HEADER_SIZE);
        int size;
        if (encrypt) {
            try {
                size = encryptCipher
                        .doFinal(byteBufferStreamOut.buffer(), bundle);
            } catch (IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {
                throw new IOException(e);
            }
        } else {
            size = byteBufferStreamOut.buffer().remaining();
            bundle.put(byteBufferStreamOut.buffer());
        }
        if (size > BUNDLE_MAX_SIZE) {
            throw new IOException(
                    "Unable to send too large bundle of size: " + size);
        }
        bundle.flip();
        bundle.putInt(size);
        bundle.rewind();
        queue.add(bundle);
        dataStreamOut.buffer().clear();
        digest.reset();
        selector.ifPresent(Selector::wakeup);
    }

    public boolean process() throws IOException {
        if (output == null) {
            output = queue.poll();
        }
        if (output != null) {
            int write = channel.write(output);
            if (write == -1) {
                throw new IOException("Connection closed");
            }
            outRate.getAndAdd(write);
            if (!output.hasRemaining()) {
                bufferCache.add(new WeakReference<>(output));
                output = null;
            }
            return true;
        }
        return false;
    }

    public Optional<ReadableByteStream> fetch() throws IOException {
        if (!hasInput) {
            int read = channel.read(header);
            if (read == -1) {
                throw new IOException("Connection closed");
            }
            inRate.getAndAdd(read);
            if (!header.hasRemaining()) {
                header.flip();
                int limit = header.getInt();
                if (limit < 0 || limit > BUNDLE_MAX_SIZE) {
                    throw new IOException("Invalid bundle length: " + limit);
                }
                if (limit > input.capacity()) {
                    input = BufferCreator.bytes(limit);
                } else {
                    input.clear().limit(limit);
                }
                hasInput = true;
                header.clear();
            }
        }
        if (hasInput) {
            int read = channel.read(input);
            if (read == -1) {
                throw new IOException("Connection closed");
            }
            inRate.getAndAdd(read);
            if (!input.hasRemaining()) {
                input.flip();
                byteBufferStreamOut.buffer().clear();
                ByteBufferStream data;
                if (encrypt) {
                    dataStreamOut.buffer().clear();
                    dataStreamOut.ensurePut(
                            decryptCipher.getOutputSize(input.remaining()));
                    try {
                        decryptCipher.doFinal(input, dataStreamOut.buffer());
                    } catch (IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {
                        throw new IOException(e);
                    }
                    dataStreamOut.buffer().flip();
                    data = dataStreamOut;
                } else {
                    data = new ByteBufferStream(input);
                }
                data.get(checksum);
                digest.update(data.buffer());
                if (!Arrays.equals(checksum, digest.digest())) {
                    throw new IOException("Integrity check failed");
                }
                data.buffer().flip().position(CHECKSUM_LENGTH);
                CompressionUtil.filter(data, byteBufferStreamOut, inflater);
                byteBufferStreamOut.buffer().flip();
                dataStreamOut.buffer().clear();
                hasInput = false;
                return Optional.of(byteBufferStreamOut);
            }
        }
        return Optional.empty();
    }

    public void setKey(byte[] encryptKey, byte[] decryptKey) {
        if (encryptKey == null || decryptKey == null) {
            encrypt = false;
        } else {
            SecretKeySpec encryptKeySpec = new SecretKeySpec(encryptKey, "AES");
            SecretKeySpec decryptKeySpec = new SecretKeySpec(decryptKey, "AES");
            try {
                encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKeySpec, IV);
                decryptCipher.init(Cipher.DECRYPT_MODE, decryptKeySpec, IV);
            } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
                throw new UnsupportedJVMException(e);
            }
            encrypt = true;
        }
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

    public int getOutputRate() {
        return outRate.getAndSet(0);
    }

    public int getInputRate() {
        return inRate.getAndSet(0);
    }

    public Optional<InetSocketAddress> getRemoteAddress() {
        try {
            SocketAddress address = channel.getRemoteAddress();
            if (address instanceof InetSocketAddress) {
                return Optional.of((InetSocketAddress) address);
            }
        } catch (IOException e) {
        }
        return Optional.empty();
    }

    @SuppressWarnings("ObjectToString")
    @Override
    public String toString() {
        try {
            return channel.getRemoteAddress().toString();
        } catch (IOException e) {
        }
        return super.toString();
    }

    private ByteBuffer buffer(int capacity) {
        ByteBuffer bundle = null;
        int i = 0;
        while (i < bufferCache.size()) {
            ByteBuffer cacheBuffer = bufferCache.get(i).get();
            if (cacheBuffer == null) {
                bufferCache.remove(i);
            } else if (cacheBuffer.capacity() >= capacity) {
                bufferCache.remove(i);
                bundle = cacheBuffer;
                bundle.clear();
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
}