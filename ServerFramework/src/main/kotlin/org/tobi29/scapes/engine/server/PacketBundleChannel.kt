/*
 * Copyright 2012-2016 Tobi29
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
package org.tobi29.scapes.engine.server

import org.tobi29.scapes.engine.utils.*
import org.tobi29.scapes.engine.utils.io.ByteBufferStream
import org.tobi29.scapes.engine.utils.io.CompressionUtil
import org.tobi29.scapes.engine.utils.io.RandomReadableByteStream
import org.tobi29.scapes.engine.utils.io.RandomWritableByteStream
import org.tobi29.scapes.engine.utils.task.Joiner
import org.tobi29.scapes.engine.utils.task.TaskExecutor
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult
import javax.net.ssl.SSLException
import javax.net.ssl.SSLPeerUnverifiedException

class PacketBundleChannel(private val address: RemoteAddress, private val channel: SocketChannel,
                          private val taskExecutor: TaskExecutor, private val ssl: SSLHandle, client: Boolean) {
    private val taskCounter = AtomicInteger()
    private val verified = AtomicBoolean()
    private val dataStreamOut = ByteBufferStream({ BufferCreator.bytes(it) },
            { it + 102400 })
    private val byteBufferStreamOut = ByteBufferStream(
            { BufferCreator.bytes(it) }, { it + 102400 })
    private val queue = ConcurrentLinkedQueue<ByteBuffer>()
    private val deflater: CompressionUtil.Filter
    private val inflater: CompressionUtil.Filter
    private val inRate = AtomicInteger()
    private val outRate = AtomicInteger()
    private val engine: SSLEngine
    private val myNetData = ByteBufferStream({ BufferCreator.bytes(it) },
            { it + 16384 })
    private val peerAppData = ByteBufferStream({ BufferCreator.bytes(it) },
            { it + 16384 })
    private val peerNetData = ByteBufferStream({ BufferCreator.bytes(it) },
            { it + 16384 })
    private var output: ByteBuffer? = null
    private var input = BufferCreator.bytes(1024)
    private var spill: ByteBuffer? = null
    private var selector: Selector? = null
    private var verifyException: IOException? = null
    private var hasInput: Boolean = false
    private var hasBundle: Boolean = false
    private var close: Boolean = false
    private var state = State.HANDSHAKE

    init {
        deflater = CompressionUtil.ZDeflater(1)
        inflater = CompressionUtil.ZInflater()
        engine = ssl.newEngine(address)
        engine.useClientMode = client
        myNetData.buffer().limit(0)
        input.limit(BUNDLE_HEADER_SIZE)
        engine.beginHandshake()
    }

    val outputStream: RandomWritableByteStream
        get() = dataStreamOut

    fun bundleSize(): Int {
        return dataStreamOut.buffer().position()
    }

    fun queueBundle() {
        dataStreamOut.buffer().flip()
        byteBufferStreamOut.buffer().clear()
        CompressionUtil.filter(dataStreamOut, byteBufferStreamOut, deflater)
        byteBufferStreamOut.buffer().flip()
        val size = byteBufferStreamOut.buffer().remaining()
        if (size > BUNDLE_MAX_SIZE) {
            throw IOException("Bundle size too large: " + size)
        }
        val bundle = buffer(BUNDLE_HEADER_SIZE + size)
        bundle.putInt(size)
        bundle.put(byteBufferStreamOut.buffer())
        bundle.flip()
        queue.add(bundle)
        dataStreamOut.buffer().clear()
        selector?.wakeup()
    }

    fun process(consumer: (RandomReadableByteStream) -> Boolean,
                max: Int = Int.MAX_VALUE): Boolean {
        var timeout = if (state == State.OPEN) 0 else -2
        var received = 0
        while (timeout < 2 && received < max) {
            if (hasBundle) {
                if (!consumer(byteBufferStreamOut)) {
                    return false
                }
                received++
                hasBundle = false
            } else {
                timeout++
            }
            fetch()
            if (process()) {
                return true
            }
            if (hasBundle) {
                timeout = 0
            }
        }
        return false
    }

    private fun fetch() {
        if (state != State.OPEN) {
            return
        }
        assert(!hasBundle)
        val buffer = spill ?: run { readSSL() ?: return }
        spill = null
        if (buffer.remaining() > input.remaining()) {
            val limit = buffer.limit()
            buffer.limit(buffer.position() + input.remaining())
            input.put(buffer)
            buffer.limit(limit)
            spill = buffer
        } else {
            input.put(buffer)
        }
        if (input.hasRemaining()) {
            return
        }
        input.flip()
        if (!hasInput) {
            if (input.remaining() != BUNDLE_HEADER_SIZE) {
                throw IOException(
                        "Invalid bundle header size: " + input.remaining())
            }
            val limit = input.int
            if (limit > BUNDLE_MAX_SIZE) {
                throw IOException("Bundle size too large: " + limit)
            }
            if (input.capacity() < limit) {
                BUFFER_CACHE.get().add(WeakReference(input))
                input = buffer(limit)
            } else {
                input.clear()
            }
            input.limit(limit)
            hasInput = true
            return
        }
        byteBufferStreamOut.buffer().clear()
        CompressionUtil.filter(ByteBufferStream(input), byteBufferStreamOut,
                inflater)
        byteBufferStreamOut.buffer().flip()
        hasInput = false
        input.clear().limit(BUNDLE_HEADER_SIZE)
        hasBundle = true
    }

    private fun process(): Boolean {
        while (true) {
            // Glorious design on Java's part:
            // The SSLEngine locks whilst delegated task runs
            // Solution: No touchy
            if (taskCounter.get() > 0) {
                return false
            }
            if (flush()) {
                return false
            }
            if (state == State.CLOSED) {
                return true
            }
            if (state == State.HANDSHAKE) {
                if (handshake()) {
                    if (ssl.requiresVerification()) {
                        state = State.VERIFY
                        taskExecutor.runTask({ verifySSL() }, "SSL-Verify")
                    } else {
                        state = State.OPEN
                    }
                } else {
                    return false
                }
            }
            if (state == State.VERIFY) {
                verifyException?.let { throw IOException(it) }
                if (verified.get()) {
                    state = State.OPEN
                } else {
                    return false
                }
            }
            if (state == State.CLOSING && handshake()) {
                state = State.CLOSED
                continue
            }
            val writeOutput = output
            if (writeOutput != null) {
                writeSSL(writeOutput)
                if (writeOutput.hasRemaining()) {
                    continue
                }
                BUFFER_CACHE.get().add(WeakReference(writeOutput))
            }
            output = queue.poll()
            if (output == null) {
                if (state == State.OPEN && close) {
                    engine.closeOutbound()
                    state = State.CLOSING
                } else {
                    return false
                }
            }
        }
    }

    fun register(selector: Selector,
                 opt: Int) {
        channel.register(selector, opt)
        this.selector = selector
    }

    fun register(joiner: Joiner.SelectorJoinable,
                 opt: Int) {
        register(joiner.selector, opt)
    }

    fun close() {
        channel.close()
        deflater.close()
        inflater.close()
    }

    fun requestClose() {
        close = true
    }

    val outputRate: Int
        get() = outRate.getAndSet(0)

    val inputRate: Int
        get() = inRate.getAndSet(0)

    val remoteAddress: InetSocketAddress?
        get() {
            val address = channel.socket().remoteSocketAddress
            if (address is InetSocketAddress) {
                return address
            }
            return null
        }

    override fun toString(): String {
        return channel.socket().remoteSocketAddress.toString()
    }

    private fun buffer(capacity: Int): ByteBuffer {
        val bufferCache = BUFFER_CACHE.get()
        var bundle: ByteBuffer? = null
        var i = 0
        while (i < bufferCache.size) {
            val cacheBuffer = bufferCache[i].get()
            if (cacheBuffer == null) {
                bufferCache.removeAt(i)
            } else if (cacheBuffer.capacity() >= capacity) {
                bufferCache.removeAt(i)
                bundle = cacheBuffer
                bundle.clear().limit(capacity)
                break
            } else {
                i++
            }
        }
        if (bundle == null) {
            bundle = BufferCreator.bytes(capacity)
        }
        return bundle
    }

    private fun readSSL(): ByteBuffer? {
        fill()
        do {
            peerAppData.buffer().clear()
            val result = engine.unwrap(peerNetData.buffer(),
                    peerAppData.buffer())
            when (result.status) {
                SSLEngineResult.Status.OK -> {
                    peerAppData.buffer().flip()
                    peerNetData.buffer().compact()
                    return peerAppData.buffer()
                }
                SSLEngineResult.Status.BUFFER_OVERFLOW -> peerAppData.grow()
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                    peerNetData.buffer().compact()
                    if (!peerNetData.hasRemaining()) {
                        peerNetData.grow()
                    }
                    return null
                }
                SSLEngineResult.Status.CLOSED -> {
                    engine.closeOutbound()
                    state = State.CLOSING
                    peerNetData.buffer().compact()
                    return null
                }
                else -> throw IllegalStateException(
                        "Invalid SSL status: " + result.status)
            }
        } while (peerNetData.hasRemaining())
        peerNetData.buffer().compact()
        return null
    }

    private fun writeSSL(buffer: ByteBuffer) {
        while (true) {
            myNetData.buffer().clear()
            val result = engine.wrap(buffer, myNetData.buffer())
            when (result.status) {
                SSLEngineResult.Status.OK -> {
                    myNetData.buffer().flip()
                    return
                }
                SSLEngineResult.Status.BUFFER_OVERFLOW -> myNetData.grow()
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> throw SSLException(
                        "Buffer underflow occurred after a wrap. I don't think we should ever get here.")
                SSLEngineResult.Status.CLOSED -> {
                    engine.closeOutbound()
                    state = State.CLOSING
                    myNetData.buffer().flip()
                    return
                }
                else -> throw IllegalStateException(
                        "Invalid SSL status: " + result.status)
            }
        }
    }

    private fun handshake(): Boolean {
        when (engine.handshakeStatus) {
            SSLEngineResult.HandshakeStatus.FINISHED, SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING -> return true
            SSLEngineResult.HandshakeStatus.NEED_UNWRAP -> readSSL()
            SSLEngineResult.HandshakeStatus.NEED_WRAP -> writeSSL(EMPTY_BUFFER)
            SSLEngineResult.HandshakeStatus.NEED_TASK -> {
                val task = engine.delegatedTask
                if (task != null) {
                    taskCounter.incrementAndGet()
                    taskExecutor.runTask({
                        task.run()
                        taskCounter.decrementAndGet()
                    }, "SSLEngine-Task")
                }
            }
            else -> throw IllegalStateException(
                    "Invalid SSL status: " + engine.handshakeStatus)
        }
        return false
    }

    private fun verifySSL() {
        try {
            val certificates = engine.session.peerCertificates
            val x509Certificates = stream(
                    *certificates).filterMap<X509Certificate>().toTypedArray()
            try {
                ssl.verifySession(address, engine, x509Certificates)
                verified.set(true)
            } catch (e: IOException) {
                if (x509Certificates.size == 0 || !ssl.certificateFeedback(
                        x509Certificates)) {
                    verifyException = e
                } else {
                    verified.set(true)
                }
            }
        } catch (e: SSLPeerUnverifiedException) {
            verifyException = e
        }
    }

    private fun fill() {
        val read = channel.read(peerNetData.buffer())
        peerNetData.buffer().flip()
        if (read < 0) {
            engine.closeInbound()
            state = State.CLOSING
        }
        inRate.getAndAdd(read)
    }

    private fun flush(): Boolean {
        if (!myNetData.hasRemaining()) {
            return false
        }
        val write = channel.write(myNetData.buffer())
        if (write < 0) {
            engine.closeOutbound()
            state = State.CLOSING
        }
        outRate.getAndAdd(write)
        return myNetData.hasRemaining()
    }

    private enum class State {
        HANDSHAKE,
        VERIFY,
        OPEN,
        CLOSING,
        CLOSED
    }

    companion object {
        private val BUNDLE_HEADER_SIZE = 4
        private val BUNDLE_MAX_SIZE = 1 shl 10 shl 10 shl 6
        private val BUFFER_CACHE = ThreadLocal { ArrayList<WeakReference<ByteBuffer>>() }
        private val EMPTY_BUFFER = BufferCreator.bytes(0)
    }
}

inline fun PacketBundleChannel.processVoid(): Boolean {
    return process({ true })
}

inline fun <S> PacketBundleChannel.process(crossinline state: () -> S?,
                                           crossinline consumer: (S, RandomReadableByteStream) -> Unit,
                                           max: Int = Int.MAX_VALUE): Boolean {
    return process({ bundle ->
        val currentState = state()
        if (currentState == null) {
            false
        } else {
            consumer(currentState, bundle)
            true
        }
    }, max)
}

inline fun PacketBundleChannel.process2(crossinline state: () -> ChannelState?,
                                        max: Int = Int.MAX_VALUE): Boolean {
    val output = outputStream
    if (state()?.producer?.invoke(output) ?: false) {
        queueBundle()
    }
    return process({ bundle ->
        val currentState = state()
        if (currentState == null) {
            false
        } else {
            if (currentState.consumer(bundle, output)) {
                queueBundle()
            }
            if (currentState.producer(output)) {
                queueBundle()
            }
            true
        }
    }, max)
}

class ChannelState(val consumer: (RandomReadableByteStream, RandomWritableByteStream) -> Boolean = { i, o -> false },
                   val producer: (RandomWritableByteStream) -> Boolean = { false })
