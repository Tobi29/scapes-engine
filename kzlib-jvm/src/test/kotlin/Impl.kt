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

package org.tobi29.kzlib

import org.tobi29.arrays.sliceOver
import org.tobi29.io.*

class DeflateHandle(
    level: Int = -1,
    outputBufferSize: Int = 16384,
    inputBufferSize: Int = 16384,
    method: Int = Z_DEFLATED,
    windowBits: Int = DEF_WBITS,
    memLevel: Int = DEF_MEM_LEVEL,
    strategy: Int = Z_DEFAULT_STRATEGY,
    wrapperType: WrapperType = WrapperType.ZLIB
) : AutoCloseable {
    internal val outputBuffer = ByteArray(outputBufferSize)
    internal val inputBuffer = ByteArray(inputBufferSize)
    internal val deflater = Deflater(
        level, method, windowBits, memLevel, strategy, wrapperType
    )

    constructor(
        level: Int,
        outputBufferSize: Int,
        inputBufferSize: Int
    ) : this(
        level, outputBufferSize, inputBufferSize,
        Z_DEFLATED, DEF_WBITS, DEF_MEM_LEVEL, Z_DEFAULT_STRATEGY,
        WrapperType.ZLIB
    )

    fun reset() {
        deflater.reset()
    }

    override fun close() {
        deflater.end()
    }
}

class InflateHandle(
    outputBufferSize: Int = 16384,
    inputBufferSize: Int = 16384,
    windowBits: Int = DEF_WBITS,
    wrapperType: WrapperType = WrapperType.ZLIB
) : AutoCloseable {
    internal val outputBuffer = ByteArray(outputBufferSize)
    internal val inputBuffer = ByteArray(inputBufferSize)
    internal val inflater = Inflater(
        windowBits, wrapperType
    )

    constructor(
        outputBufferSize: Int,
        inputBufferSize: Int
    ) : this(
        outputBufferSize, inputBufferSize,
        DEF_WBITS,
        WrapperType.ZLIB
    )

    fun reset() {
        inflater.reset()
    }

    override fun close() {
        inflater.end()
    }
}

fun DeflateHandle.deflate(
    input: ReadableByteStream,
    output: WritableByteStream
) {
    var finishing = false
    while (true) {
        if (!finishing && !bufferInput(input)) finishing = true
        while (finishing || deflater.first.avail_in > 0) {
            val status =
                bufferOutput(if (finishing) Z_FINISH else Z_NO_FLUSH, output)
            if (status == Z_STREAM_END) return
        }
    }
}

fun InflateHandle.inflate(
    input: ReadableByteStream,
    output: WritableByteStream
) {
    var finishing = false
    while (true) {
        if (!finishing && !bufferInput(input)) finishing = true
        while (finishing || inflater.first.avail_in > 0) {
            val status =
                bufferOutput(if (finishing) Z_FINISH else Z_NO_FLUSH, output)
            if (status == Z_STREAM_END) return
        }
    }
}

fun DeflateHandle.bufferInput(
    buffer: ReadableByteStream
): Boolean = deflater.first.bufferInput(inputBuffer, buffer)

fun InflateHandle.bufferInput(
    buffer: ReadableByteStream
): Boolean = inflater.first.bufferInput(inputBuffer, buffer)

private fun z_stream.bufferInput(
    inputBuffer: ByteArray,
    buffer: ReadableByteStream
): Boolean {
    val read = buffer.getSome(inputBuffer.sliceOver())
    if (read < 0) return false
    next_in = inputBuffer
    next_in_i = 0
    avail_in = read
    return true
}

fun DeflateHandle.bufferOutput(
    flush: Int,
    buffer: WritableByteStream
): Int = deflater.first.bufferOutput(outputBuffer, buffer) {
    deflater.deflate(flush)
}

fun InflateHandle.bufferOutput(
    flush: Int,
    buffer: WritableByteStream
): Int = inflater.first.bufferOutput(outputBuffer, buffer) {
    inflater.inflate(flush)
}

private inline fun z_stream.bufferOutput(
    outputBuffer: ByteArray,
    buffer: WritableByteStream,
    output: () -> Int
): Int {
    next_out = outputBuffer
    next_out_i = 0
    avail_out = outputBuffer.size
    val status = output()
    buffer.put(outputBuffer.view.slice(0, next_out_i))
    return status
}

// Common

fun deflate(
    input: ReadableByteStream,
    level: Int = -1
): ByteView = DeflateHandle(level).use { it.deflate(input) }

fun deflate(
    input: ReadableByteStream,
    output: WritableByteStream,
    level: Int = -1
) = DeflateHandle(level).use { it.deflate(input, output) }

fun DeflateHandle.deflate(
    input: ReadableByteStream,
    level: Int = -1
): ByteView {
    val stream = MemoryViewStreamDefault()
    deflate(input, stream)
    stream.flip()
    return stream.bufferSlice()
}

fun inflate(
    input: ReadableByteStream
): ByteView = InflateHandle().use { it.inflate(input) }

fun inflate(
    input: ReadableByteStream,
    output: WritableByteStream
) = InflateHandle().use { it.inflate(input, output) }

fun InflateHandle.inflate(
    input: ReadableByteStream
): ByteView {
    val stream = MemoryViewStreamDefault()
    inflate(input, stream)
    stream.flip()
    return stream.bufferSlice()
}
