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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.yield
import org.tobi29.io.*
import org.tobi29.io.compression.deflate.DeflateHandle
import org.tobi29.io.compression.deflate.InflateHandle
import org.tobi29.io.compression.deflate.process
import org.tobi29.stdex.ThreadLocal
import org.tobi29.stdex.assert
import java.lang.ref.WeakReference

class PacketBundleChannel(
    private val channelRead: ReadableByteChannel,
    private val channelWrite: WritableByteChannel
) {
    private val dataStreamOut = MemoryViewStreamDefault(
        growth = { it + 102400 })
    private val byteBufferStreamOut = MemoryViewStreamDefault(
        growth = { it + 102400 })
    private val compressStreamOut = MemoryViewStreamDefault(
        growth = { it + 102400 })
    private val queue = Channel<HeapViewByteBE>(Channel.UNLIMITED)
    private val deflater = DeflateHandle(1)
    private val inflater = InflateHandle()
    private var output: HeapViewByteBE? = null
    private var input = MemoryViewStreamDefault().apply { limit = 4 }
    private var hasInput: Boolean = false
    private var hasBundle: Boolean = false

    constructor(channel: ByteChannel) : this(channel, channel)

    val outputStream: RandomWritableByteStream
        get() = dataStreamOut

    val inputStream: RandomReadableByteStream
        get() = byteBufferStreamOut

    fun bundleSize(): Int {
        return dataStreamOut.position
    }

    val outputFlushed get() = queue.isEmpty && output == null

    // FIXME @Throws(IOException::class)
    fun queueBundle() {
        dataStreamOut.flip()
        compressStreamOut.reset()
        deflater.process(dataStreamOut, compressStreamOut)
        deflater.reset()
        compressStreamOut.flip()
        val size = compressStreamOut.remaining
        if (size > BUNDLE_MAX_SIZE) {
            throw IOException("Bundle size too large: $size")
        }
        val bundle = buffer(BUNDLE_HEADER_SIZE + size)
        bundle.setInt(0, size)
        bundle.setBytes(BUNDLE_HEADER_SIZE, compressStreamOut.bufferSlice())
        if (!queue.offer(bundle)) throw IOException("Send buffer full")
        dataStreamOut.reset()
    }

    // FIXME @Throws(IOException::class)
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
            var write = output
            if (write == null) {
                write = queue.poll()
                if (write == null) {
                    return false
                }
                output = write
            }
            val wrote = channelWrite.write(write)
            if (wrote < 0) return true
            write = write.slice(wrote)
            if (write.size <= 0) {
                BUFFER_CACHE.get().add(WeakReference(write.array))
                output = null
                continue
            }
            output = write
            return false
        }
    }

    // FIXME @Throws(IOException::class)
    fun close() {
        channelRead.close()
        channelWrite.close()
        deflater.close()
        inflater.close()
    }

    private fun buffer(capacity: Int): HeapViewByteBE {
        val bufferCache = BUFFER_CACHE.get()
        var bundle: HeapViewByteBE? = null
        var i = 0
        while (i < bufferCache.size) {
            val cacheBuffer = bufferCache[i].get()
            if (cacheBuffer == null) {
                bufferCache.removeAt(i)
            } else if (cacheBuffer.size >= capacity) {
                bufferCache.removeAt(i)
                bundle = cacheBuffer.viewBE.slice(0, capacity)
                break
            } else {
                i++
            }
        }
        if (bundle == null) {
            bundle = ByteArray(capacity).viewBE
        }
        return bundle
    }

    private fun fetch(): Boolean {
        assert { !hasBundle }
        if (input.hasRemaining) {
            val read = channelRead.read(input)
            if (read < 0) return true
        }
        if (input.hasRemaining) return false
        input.flip()
        if (!hasInput) {
            if (input.remaining != BUNDLE_HEADER_SIZE) {
                throw IOException(
                    "Invalid bundle header size: " + input.remaining
                )
            }
            val limit = input.getInt()
            if (limit > BUNDLE_MAX_SIZE) {
                throw IOException("Bundle size too large: $limit")
            }
            input.reset()
            input.limit = limit
            hasInput = true
            return false
        }
        byteBufferStreamOut.reset()
        inflater.process(input, byteBufferStreamOut)
        inflater.reset()
        byteBufferStreamOut.flip()
        hasInput = false
        input.reset()
        input.limit = BUNDLE_HEADER_SIZE
        hasBundle = true
        return false
    }

    companion object {
        private val BUNDLE_HEADER_SIZE = 4
        private val BUNDLE_MAX_SIZE = 1 shl 10 shl 10 shl 6
        private val BUFFER_CACHE =
            ThreadLocal { ArrayList<WeakReference<ByteArray>>() }
    }

    enum class FetchResult {
        BUNDLE, YIELD, CLOSED
    }
}

// FIXME @Throws(IOException::class)
suspend fun PacketBundleChannel.receive(): Boolean {
    while (true) {
        when (process()) {
            PacketBundleChannel.FetchResult.CLOSED -> return true
            PacketBundleChannel.FetchResult.BUNDLE -> return false
            PacketBundleChannel.FetchResult.YIELD -> yield()
        }
    }
}

// FIXME @Throws(IOException::class)
suspend fun PacketBundleChannel.receiveOrThrow() {
    if (receive()) {
        throw IOException("Connection closed")
    }
}

// FIXME @Throws(IOException::class)
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

// FIXME @Throws(IOException::class)
suspend fun PacketBundleChannel.finishAsync() {
    while (!receive()) {
    }
}
