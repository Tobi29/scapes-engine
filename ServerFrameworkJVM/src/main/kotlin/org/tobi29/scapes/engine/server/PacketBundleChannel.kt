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

import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.yield
import org.tobi29.scapes.engine.utils.ThreadLocal
import org.tobi29.scapes.engine.utils.assert
import org.tobi29.scapes.engine.utils.io.*
import java.lang.ref.WeakReference
import java.nio.channels.Selector

class PacketBundleChannel(private val channelRead: ReadableByteChannel,
                          private val channelWrite: WritableByteChannel) {
    private val dataStreamOut = ByteBufferStream(growth = { it + 102400 })
    private val byteBufferStreamOut = ByteBufferStream(growth = { it + 102400 })
    private val queue = Channel<ByteBuffer>(Channel.UNLIMITED)
    private val deflater: CompressionUtil.Filter
    private val inflater: CompressionUtil.Filter
    private var output: ByteBuffer? = null
    private var input = ByteBuffer(1024)
    private var selector: Selector? = null
    private var hasInput: Boolean = false
    private var hasBundle: Boolean = false

    init {
        deflater = ZDeflater(1)
        inflater = ZInflater()
        input.limit(BUNDLE_HEADER_SIZE)
    }

    constructor(channel: ByteChannel) : this(channel, channel)

    val outputStream: RandomWritableByteStream
        get() = dataStreamOut

    val inputStream: RandomReadableByteStream
        get() = byteBufferStreamOut

    fun bundleSize(): Int {
        return dataStreamOut.buffer().position()
    }

    val outputFlushed get() = queue.isEmpty && output == null

    // TODO: @Throws(IOException::class)
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
        if (!queue.offer(bundle)) throw IOException("Send buffer full")
        dataStreamOut.buffer().clear()
        selector?.wakeup()
    }

    // TODO: @Throws(IOException::class)
    fun process(): FetchResult {
        var timeout = 0
        while (timeout < 2) {
            if (hasBundle) {
                hasBundle = false
                return FetchResult.BUNDLE
            } else {
                timeout++
            }
            if (fetch() || write()) {
                return FetchResult.CLOSED
            }
            if (hasBundle) {
                timeout = 0
            }
        }
        return FetchResult.YIELD
    }

    fun write(): Boolean {
        while (true) {
            var writeOutput = output
            if (writeOutput == null) {
                writeOutput = queue.poll()
                if (writeOutput == null) {
                    return false
                }
                output = writeOutput
            }
            val write = channelWrite.write(writeOutput)
            if (write < 0) {
                return true
            }
            if (!writeOutput.hasRemaining()) {
                BUFFER_CACHE.get().add(WeakReference(writeOutput))
                output = null
                continue
            }
            return false
        }
    }

    // TODO: @Throws(IOException::class)
    fun close() {
        channelRead.close()
        channelWrite.close()
        deflater.close()
        inflater.close()
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
            bundle = ByteBuffer(capacity)
        }
        return bundle
    }

    private fun fetch(): Boolean {
        assert { !hasBundle }
        if (input.hasRemaining()) {
            val read = channelRead.read(input)
            if (read < 0) {
                return true
            }
        }
        if (input.hasRemaining()) {
            return false
        }
        input.flip()
        if (!hasInput) {
            if (input.remaining() != BUNDLE_HEADER_SIZE) {
                throw IOException(
                        "Invalid bundle header size: " + input.remaining())
            }
            val limit = input.getInt()
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
            return false
        }
        byteBufferStreamOut.buffer().clear()
        CompressionUtil.filter(ByteBufferStream(input), byteBufferStreamOut,
                inflater)
        byteBufferStreamOut.buffer().flip()
        hasInput = false
        input.clear().limit(BUNDLE_HEADER_SIZE)
        hasBundle = true
        return false
    }

    companion object {
        private val BUNDLE_HEADER_SIZE = 4
        private val BUNDLE_MAX_SIZE = 1 shl 10 shl 10 shl 6
        private val BUFFER_CACHE = ThreadLocal { ArrayList<WeakReference<ByteBuffer>>() }
    }

    enum class FetchResult {
        BUNDLE, YIELD, CLOSED
    }
}

// TODO: @Throws(IOException::class)
suspend fun PacketBundleChannel.receive(): Boolean {
    while (true) {
        when (process()) {
            PacketBundleChannel.FetchResult.CLOSED -> return true
            PacketBundleChannel.FetchResult.BUNDLE -> return false
            PacketBundleChannel.FetchResult.YIELD -> yield()
        }
    }
}

// TODO: @Throws(IOException::class)
suspend fun PacketBundleChannel.receiveOrThrow() {
    if (receive()) {
        throw IOException("Connection closed")
    }
}

// TODO: @Throws(IOException::class)
suspend fun PacketBundleChannel.flushAsync() {
    try {
        while (!outputFlushed) {
            if (write()) {
                break
            }
            yield()
        }
    } catch (e: Exception) {
    }
}

// TODO: @Throws(IOException::class)
suspend fun PacketBundleChannel.finishAsync() {
    while (!receive()) {
    }
}
