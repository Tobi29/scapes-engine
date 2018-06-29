/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.io.compression.deflate

import org.tobi29.io.*
import org.tobi29.kzlib.*

class DeflateException : IOException {
    internal constructor(message: String) : super(message)
    internal constructor(cause: Exception) : super(cause)
}

actual class DeflateHandle(
    level: Int = -1, internal val bufferSize: Int = 8192,
    gzip: Boolean = false
) : AutoCloseable {
    internal val outputBuffer = ByteArray(bufferSize)
    internal val inputBuffer = MemoryViewStreamDefault()
    internal val deflater = try {
        Deflater(level, wrapperType = if (gzip) W_GZIP else W_ZLIB)
    } catch (e: GZIPException) {
        throw DeflateException(e)
    }

    actual constructor(level: Int, bufferSize: Int) : this(
        level, bufferSize, false
    )

    actual fun reset() {
        deflater.first.reset()
        inputBuffer.reset()
    }

    override fun close() {
        deflater.first.end()
    }
}

actual class InflateHandle(
    internal val bufferSize: Int = 8192,
    any: Boolean = false
) : AutoCloseable {
    internal val outputBuffer = ByteArray(bufferSize)
    internal val inputBuffer = MemoryViewStreamDefault()
    internal val inflater = try {
        Inflater(wrapperType = if (any) W_ANY else W_ZLIB)
    } catch (e: GZIPException) {
        throw DeflateException(e)
    }

    actual constructor(bufferSize: Int) : this(bufferSize, false)

    actual fun reset() {
        inflater.first.reset()
        inputBuffer.reset()
    }

    override fun close() {
        inflater.first.end()
    }
}

actual fun DeflateHandle.deflate(
    input: ReadableByteStream,
    output: WritableByteStream
) {
    try {
        var finishing = false
        while (true) {
            if (!finishing && !bufferInput(input)) finishing = true
            while (finishing || deflater.second.avail_in > 0) {
                val status =
                    bufferOutput(
                        if (finishing) Z_FINISH else Z_NO_FLUSH, output
                    )
                if (status == Z_STREAM_END) return
                if (status != Z_OK) throw DeflateException(
                    "ZLib error: $status, ${deflater.second.msg}"
                )
            }
        }
    } catch (e: GZIPException) {
        throw DeflateException(e)
    }
}

actual fun InflateHandle.inflate(
    input: ReadableByteStream,
    output: WritableByteStream
) {
    try {
        var finishing = false
        while (true) {
            if (!finishing && !bufferInput(input)) finishing = true
            while (finishing || inflater.second.avail_in > 0) {
                val status =
                    bufferOutput(
                        if (finishing) Z_FINISH else Z_NO_FLUSH, output
                    )
                if (status == Z_STREAM_END) return
                if (status != Z_OK) throw DeflateException(
                    "ZLib error: $status, ${inflater.second.msg}"
                )
            }
        }
    } catch (e: GZIPException) {
        throw DeflateException(e)
    }
}

fun DeflateHandle.bufferInput(
    buffer: ReadableByteStream
): Boolean = deflater.second.bufferInput(bufferSize, inputBuffer, buffer)

fun InflateHandle.bufferInput(
    buffer: ReadableByteStream
): Boolean = inflater.second.bufferInput(bufferSize, inputBuffer, buffer)

private fun <B : ByteViewE> ZStream.bufferInput(
    bufferSize: Int,
    inputBuffer: MemoryViewStream<B>,
    buffer: ReadableByteStream
): Boolean {
    inputBuffer.limit(inputBuffer.position() + bufferSize)
    val read = buffer.getSome(inputBuffer.bufferSlice())
    if (read < 0) return false
    inputBuffer.position(inputBuffer.position() + read)
    @Suppress("UNCHECKED_CAST")
    setInput(inputBuffer.buffer().readAsByteArray(), 0, inputBuffer.position())
    return true
}

fun DeflateHandle.bufferOutput(
    flush: Int,
    buffer: WritableByteStream
): Int = deflater.second.bufferOutput(inputBuffer, outputBuffer, buffer) {
    deflater.first.deflate(flush)
}

fun InflateHandle.bufferOutput(
    flush: Int,
    buffer: WritableByteStream
): Int = inflater.second.bufferOutput(inputBuffer, outputBuffer, buffer) {
    inflater.first.inflate(flush)
}

private inline fun ZStream.bufferOutput(
    inputBuffer: MemoryViewStream<*>,
    outputBuffer: ByteArray,
    buffer: WritableByteStream,
    output: () -> Int
): Int {
    setOutput(outputBuffer)
    val status = output()
    buffer.put(outputBuffer.view.slice(0, next_out_index))
    inputBuffer.reset()
    return status
}
