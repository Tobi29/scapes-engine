/*
 * Copyright 2012-2019 Tobi29
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
package org.tobi29.server

import kotlinx.coroutines.*
import org.tobi29.arrays.Bytes
import org.tobi29.arrays.BytesRO
import org.tobi29.io.*
import org.tobi29.stdex.Throws
import org.tobi29.stdex.assert
import org.tobi29.stdex.atomic.AtomicBoolean
import org.tobi29.stdex.atomic.AtomicInt
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*
import kotlin.coroutines.CoroutineContext

class SSLChannel(
    address: RemoteAddress,
    private val channelRead: ReadableByteChannel,
    private val channelWrite: WritableByteChannel,
    taskExecutor: CoroutineContext,
    ssl: SSLHandle,
    engine: SSLEngine
) : SSLLayer(address, taskExecutor, ssl, engine), ByteChannel {
    private val inRate = AtomicInt()
    private val outRate = AtomicInt()

    val outputRate get() = outRate.getAndSet(0)

    val inputRate get() = inRate.getAndSet(0)

    // FIXME @Throws(IOException::class)
    override fun read(buffer: Bytes): Int {
        var currentBuffer = buffer
        while (currentBuffer.size > 0) {
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
            val len = read.remaining.coerceAtMost(currentBuffer.size)
            read.get(currentBuffer.slice(0, len))
            currentBuffer = currentBuffer.slice(len)
        }
        val len = buffer.size - currentBuffer.size
        return if (len > 0 || isOpen()) len else -1
    }

    // FIXME @Throws(IOException::class)
    override fun write(buffer: BytesRO): Int {
        var currentBuffer = buffer
        while (currentBuffer.size > 0 && process()) {
            val wrote = writeSSL(currentBuffer)
            if (wrote < 0) {
                if (currentBuffer.size == buffer.size) return -1
                break
            }
            currentBuffer = currentBuffer.slice(wrote)
        }
        return buffer.size - currentBuffer.size
    }

    override fun fill(buffer: Bytes): Int {
        val read = channelRead.read(buffer)
        if (read < 0) return read
        inRate.getAndAdd(read)
        return read
    }

    override fun flush(buffer: Bytes): Int {
        val wrote = channelWrite.write(buffer)
        if (wrote < 0) return wrote
        outRate.getAndAdd(wrote)
        return wrote
    }
}

abstract class SSLLayer(
    private val address: RemoteAddress,
    taskExecutor: CoroutineContext,
    private val ssl: SSLHandle,
    private val engine: SSLEngine
) : AutoCloseable {
    private val taskCounter = AtomicInt()
    private val verified = AtomicBoolean()
    private val readBuffer = MemoryViewStreamDefault()
    private val readData = MemoryViewStreamDefault().apply { limit = 0 }
    private val writeBuffer = MemoryViewStreamDefault().apply { limit = 0 }
    private val job = Job()
    private val coroutineScope = CoroutineScope(job + taskExecutor)
    private var verifyException: IOException? = null
    private var close = false
    private var state = State.HANDSHAKE

    fun isOpen() = state != State.CLOSED

    fun requestClose() {
        close = true
    }

    // FIXME @Throws(IOException::class)
    override fun close() {
        job.cancel()
    }

    fun process(): Boolean {
        while (true) {
            // Glorious design on Java's part:
            // The SSLEngine locks whilst delegated task runs
            // Solution: No touchy
            if (taskCounter.get() > 0) {
                return false
            }
            if (writeBuffer.hasRemaining) {
                val wrote = flush(writeBuffer.bufferSlice())
                if (wrote < 0) {
                    engine.closeOutbound()
                    state = State.CLOSED
                    return false
                }
                writeBuffer.position += wrote
                if (writeBuffer.hasRemaining) {
                    return false
                }
            }
            if (state == State.HANDSHAKE) {
                if (handshake()) {
                    state = State.VERIFY
                    coroutineScope.launch(CoroutineName("SSL-Verify")) {
                        try {
                            verifySSL()
                        } finally {
                            taskCounter.decrementAndGet()
                        }
                    }
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
                assert { !writeBuffer.hasRemaining }
                return false
            }
            throw IllegalStateException("Process fallthrough")
        }
    }

    protected fun dataSSL(): RandomReadableByteStream? {
        if (readData.hasRemaining) {
            return readData
        }
        return null
    }

    protected fun readSSL() {
        readData.reset()
        readBuffer.limit = readBuffer.position + 8192
        val read = fill(readBuffer.bufferSlice())
        if (read < 0) {
            engine.closeInbound()
            state = State.CLOSED
            return
        }
        readBuffer.skip(read)
        readBuffer.flip()
        loop@ do {
            val bufferRead = readBuffer.bufferSlice().readAsByteBuffer()
            val result =
                readData.bufferSlice().mutateAsByteBuffer { bufferData ->
                    engine.unwrap(bufferRead, bufferData).also {
                        readBuffer.skip(bufferRead.position())
                        readData.skip(bufferData.position())
                    }
                }
            when (result.status) {
                SSLEngineResult.Status.OK -> {
                    readBuffer.compact()
                    readData.flip()
                    return
                }
                SSLEngineResult.Status.BUFFER_OVERFLOW -> readData.grow()
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                    readBuffer.compact()
                    if (!readBuffer.hasRemaining) {
                        readBuffer.grow()
                    }
                    break@loop
                }
                SSLEngineResult.Status.CLOSED -> {
                    engine.closeInbound()
                    state = State.CLOSING
                    readBuffer.compact()
                    break@loop
                }
                else -> throw IllegalStateException(
                    "Invalid SSL status: " + result.status
                )
            }
        } while (readBuffer.hasRemaining)
        readData.limit = 0
        return
    }

    protected fun writeSSL(buffer: BytesRO? = null): Int {
        if (writeBuffer.hasRemaining) {
            return 0
        }
        while (true) {
            writeBuffer.reset()
            var wrote = 0
            val result =
                writeBuffer.bufferSlice().mutateAsByteBuffer { bufferWrite ->
                    if (buffer == null) {
                        engine.wrap(emptyArray(), bufferWrite).also {
                            writeBuffer.skip(bufferWrite.position())
                        }
                    } else {
                        val byteBuffer = buffer.readAsByteBuffer()
                        engine.wrap(byteBuffer, bufferWrite).also {
                            writeBuffer.skip(bufferWrite.position())
                            wrote = byteBuffer.position()
                        }
                    }
                }
            when (result.status) {
                SSLEngineResult.Status.OK -> {
                    writeBuffer.flip()
                    return wrote
                }
                SSLEngineResult.Status.BUFFER_OVERFLOW -> writeBuffer.grow()
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> throw SSLException(
                    "Buffer underflow occurred after a wrap. I don't think we should ever get here."
                )
                SSLEngineResult.Status.CLOSED -> {
                    engine.closeOutbound()
                    state = State.CLOSING
                    writeBuffer.flip()
                    return -1
                }
                else -> throw IllegalStateException(
                    "Invalid SSL status: " + result.status
                )
            }
        }
    }

    protected abstract fun fill(buffer: Bytes): Int

    protected abstract fun flush(buffer: Bytes): Int

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
                        coroutineScope.launch(CoroutineName("SSLEngine-Task")) {
                            try {
                                task.run()
                            } finally {
                                taskCounter.decrementAndGet()
                            }
                        }
                    }
                }
                else -> throw IllegalStateException(
                    "Invalid SSL status: " + engine.handshakeStatus
                )
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

suspend inline fun SSLChannel.finishAsync() {
    (this as SSLLayer).finishAsync()
}

suspend inline fun SSLLayer.finishAsync() {
    requestClose()
    while (isOpen()) {
        process()
        yield()
    }
}
