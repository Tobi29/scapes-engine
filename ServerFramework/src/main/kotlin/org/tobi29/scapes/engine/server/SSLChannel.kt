/*
 * Copyright 2012-2017 Tobi29
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

import kotlinx.coroutines.experimental.yield
import org.tobi29.scapes.engine.utils.AtomicBoolean
import org.tobi29.scapes.engine.utils.AtomicInteger
import org.tobi29.scapes.engine.utils.assert
import org.tobi29.scapes.engine.utils.io.*
import org.tobi29.scapes.engine.utils.task.TaskExecutor
import java.nio.channels.ByteChannel
import java.security.cert.X509Certificate
import javax.net.ssl.*

class SSLChannel(address: RemoteAddress,
                 private val channelRead: ReadableByteChannel,
                 private val channelWrite: WritableByteChannel,
                 taskExecutor: TaskExecutor,
                 ssl: SSLHandle,
                 engine: SSLEngine
) : SSLLayer(address, taskExecutor, ssl, engine), ByteChannel {
    private val inRate = AtomicInteger()
    private val outRate = AtomicInteger()

    val outputRate get() = outRate.getAndSet(0)

    val inputRate get() = inRate.getAndSet(0)

    // TODO: @Throws(IOException::class)
    override fun read(buffer: ByteBuffer): Int {
        val pos = buffer.position()
        while (buffer.hasRemaining()) {
            var read = dataSSL()
            if (read == null) {
                if (!process()) {
                    break
                }
                read = dataSSL()
                if (read == null) {
                    readSSL()
                    read = dataSSL()
                    if (read == null) {
                        break
                    }
                }
            }
            val len = read.remaining().coerceAtMost(buffer.remaining())
            val limit = read.limit()
            read.limit(read.position() + len)
            buffer.put(read)
            read.limit(limit)
        }
        val len = buffer.position() - pos
        return if (len > 0 || isOpen) len else -1
    }

    // TODO: @Throws(IOException::class)
    override fun write(buffer: ByteBuffer): Int {
        val pos = buffer.position()
        while (buffer.hasRemaining() && process()) {
            writeSSL(buffer)
        }
        val len = buffer.position() - pos
        return if (len > 0 || isOpen) len else -1
    }

    // TODO: @Throws(IOException::class)
    override fun close() {
    }

    override fun fill(buffer: ByteBuffer): Boolean {
        val read = channelRead.read(buffer)
        if (read < 0) {
            return false
        }
        inRate.getAndAdd(read)
        return true
    }

    override fun flush(buffer: ByteBuffer): Boolean {
        val write = channelWrite.write(buffer)
        if (write < 0) {
            return false
        }
        outRate.getAndAdd(write)
        return true
    }
}

abstract class SSLLayer(private val address: RemoteAddress,
                        private val taskExecutor: TaskExecutor,
                        private val ssl: SSLHandle,
                        private val engine: SSLEngine) {
    private val taskCounter = AtomicInteger()
    private val verified = AtomicBoolean()
    private val readBuffer = ByteBufferStream(growth = { it + 16384 })
    private val readData = ByteBufferStream(
            growth = { it + 16384 }).apply { buffer().limit(0) }
    private val writeBuffer = ByteBufferStream(
            growth = { it + 16384 }).apply { buffer().limit(0) }
    private var verifyException: IOException? = null
    private var close = false
    private var state = State.HANDSHAKE

    fun isOpen() = state != State.CLOSED

    fun requestClose() {
        close = true
    }

    fun process(): Boolean {
        while (true) {
            // Glorious design on Java's part:
            // The SSLEngine locks whilst delegated task runs
            // Solution: No touchy
            if (taskCounter.get() > 0) {
                return false
            }
            if (writeBuffer.hasRemaining()) {
                if (!flush(writeBuffer.buffer())) {
                    engine.closeOutbound()
                    state = State.CLOSED
                    return false
                }
                if (writeBuffer.hasRemaining()) {
                    return false
                }
            }
            if (state == State.HANDSHAKE) {
                if (handshake()) {
                    state = State.VERIFY
                    taskExecutor.runTask({ verifySSL() }, "SSL-Verify")
                } else {
                    continue
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
            if (state == State.OPEN) {
                if (close) {
                    engine.closeOutbound()
                    state = State.CLOSING
                } else {
                    return true
                }
            }
            if (state == State.CLOSING) {
                if (handshake()) {
                    state = State.CLOSED
                } else {
                    continue
                }
            }
            if (state == State.CLOSED) {
                assert { !writeBuffer.hasRemaining() }
                return false
            }
            throw IllegalStateException("Process fallthrough")
        }
    }

    protected fun dataSSL(): ByteBuffer? {
        if (readData.hasRemaining()) {
            return readData.buffer()
        }
        return null
    }

    protected fun readSSL() {
        readData.buffer().clear()
        if (!fill(readBuffer.buffer())) {
            engine.closeInbound()
            state = State.CLOSED
            return
        }
        readBuffer.buffer().flip()
        loop@ do {
            val result = engine.unwrap(readBuffer.buffer(),
                    readData.buffer())
            when (result.status) {
                SSLEngineResult.Status.OK -> {
                    readBuffer.buffer().compact()
                    readData.buffer().flip()
                    return
                }
                SSLEngineResult.Status.BUFFER_OVERFLOW -> readData.grow()
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                    readBuffer.buffer().compact()
                    if (!readBuffer.hasRemaining()) {
                        readBuffer.grow()
                    }
                    break@loop
                }
                SSLEngineResult.Status.CLOSED -> {
                    engine.closeInbound()
                    state = State.CLOSING
                    readBuffer.buffer().compact()
                    break@loop
                }
                else -> throw IllegalStateException(
                        "Invalid SSL status: " + result.status)
            }
        } while (readBuffer.hasRemaining())
        readData.buffer().limit(0)
        return
    }

    protected fun writeSSL(buffer: ByteBuffer? = null) {
        if (writeBuffer.hasRemaining()) {
            return
        }
        while (true) {
            writeBuffer.buffer().clear()
            val result = if (buffer == null) {
                engine.wrap(emptyArray(), writeBuffer.buffer())
            } else {
                engine.wrap(buffer, writeBuffer.buffer())
            }
            when (result.status) {
                SSLEngineResult.Status.OK -> {
                    writeBuffer.buffer().flip()
                    return
                }
                SSLEngineResult.Status.BUFFER_OVERFLOW -> writeBuffer.grow()
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> throw SSLException(
                        "Buffer underflow occurred after a wrap. I don't think we should ever get here.")
                SSLEngineResult.Status.CLOSED -> {
                    engine.closeOutbound()
                    state = State.CLOSING
                    writeBuffer.buffer().flip()
                    return
                }
                else -> throw IllegalStateException(
                        "Invalid SSL status: " + result.status)
            }
        }
    }

    protected abstract fun fill(buffer: ByteBuffer): Boolean

    protected abstract fun flush(buffer: ByteBuffer): Boolean

    private fun handshake(): Boolean {
        try {
            when (engine.handshakeStatus) {
                SSLEngineResult.HandshakeStatus.FINISHED, SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING -> return true
                SSLEngineResult.HandshakeStatus.NEED_UNWRAP -> readSSL()
                SSLEngineResult.HandshakeStatus.NEED_WRAP -> writeSSL()
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
        } catch (e: SSLHandshakeException) {
            var cause: Throwable? = e
            val set = HashSet<Throwable?>()
            while (true) {
                if (cause == null || !set.add(cause)) {
                    break
                }
                if (cause is SavedCertificateException) {
                    throw SSLCertificateException(e, cause.certificates)
                }
                cause = cause.cause
            }
            throw e
        }
    }

    private fun verifySSL() {
        try {
            try {
                ssl.verifySession(address, engine.session, engine.useClientMode)
                verified.set(true)
            } catch (e: IOException) {
                verifyException = e
            }
        } catch (e: SSLPeerUnverifiedException) {
            verifyException = e
        }
    }

    protected enum class State {
        HANDSHAKE,
        VERIFY,
        OPEN,
        CLOSING,
        CLOSED
    }
}

class SSLCertificateException(
        cause: Exception,
        val certificates: Array<X509Certificate>
) : SSLHandshakeException(cause.message)

inline suspend fun SSLChannel.finishAsync() {
    (this as SSLLayer).finishAsync()
}

inline suspend fun SSLLayer.finishAsync() {
    requestClose()
    while (isOpen()) {
        process()
        yield()
    }
}
