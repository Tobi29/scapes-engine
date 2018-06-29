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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.io.compression.deflate

import org.tobi29.io.*
import java.util.zip.Deflater
import java.util.zip.Inflater

actual class DeflateHandle actual constructor(
    level: Int, internal val bufferSize: Int
) : AutoCloseable {
    internal val outputBuffer = ByteArray(bufferSize)
    internal val inputBuffer = MemoryViewStreamDefault()
    internal val deflater = Deflater(level)

    actual fun reset() {
        deflater.reset()
        inputBuffer.reset()
    }

    override fun close() {
        deflater.end()
    }
}

actual class InflateHandle actual constructor(
    internal val bufferSize: Int
) : AutoCloseable {
    internal val outputBuffer = ByteArray(bufferSize)
    internal val inputBuffer = MemoryViewStreamDefault()
    internal val inflater = Inflater()

    actual fun reset() {
        inflater.reset()
        inputBuffer.reset()
    }

    override fun close() {
        inflater.end()
    }
}

actual fun DeflateHandle.deflate(
    input: ReadableByteStream,
    output: WritableByteStream
) {
    try {
        filter(
            input, output,
            { bufferInput(it) },
            { bufferOutput(it) },
            { deflater.finish() },
            { deflater.needsInput() },
            { deflater.finished() }
        )
    } finally {
        deflater.end()
    }
}

actual fun InflateHandle.inflate(
    input: ReadableByteStream,
    output: WritableByteStream
) {
    try {
        filter(
            input, output,
            { bufferInput(it) },
            { bufferOutput(it) },
            {},
            { inflater.needsInput() },
            { inflater.finished() }
        )
    } finally {
        inflater.end()
    }
}

private fun DeflateHandle.bufferInput(
    buffer: ReadableByteStream
): Boolean = bufferInput(bufferSize, inputBuffer, buffer) {
    deflater.setInput(it.array, it.offset, it.size)
}

private fun InflateHandle.bufferInput(
    buffer: ReadableByteStream
): Boolean = bufferInput(bufferSize, inputBuffer, buffer) {
    inflater.setInput(it.array, it.offset, it.size)
}

private inline fun <B : ByteViewE> bufferInput(
    bufferSize: Int,
    inputBuffer: MemoryViewStream<B>,
    buffer: ReadableByteStream,
    setInput: (B) -> Unit
): Boolean {
    inputBuffer.limit(inputBuffer.position() + bufferSize)
    val read = buffer.getSome(inputBuffer.bufferSlice())
    if (read < 0) return false
    inputBuffer.position(inputBuffer.position() + read)
    @Suppress("UNCHECKED_CAST")
    setInput(inputBuffer.buffer().slice(0, inputBuffer.position()) as B)
    return true
}

private fun DeflateHandle.bufferOutput(
    buffer: WritableByteStream
): Int = bufferOutput(inputBuffer, outputBuffer, buffer) {
    deflater.deflate(it)
}

private fun InflateHandle.bufferOutput(
    buffer: WritableByteStream
): Int = bufferOutput(inputBuffer, outputBuffer, buffer) {
    inflater.inflate(it)
}

private inline fun bufferOutput(
    inputBuffer: MemoryViewStream<*>,
    outputBuffer: ByteArray,
    buffer: WritableByteStream,
    output: (ByteArray) -> Int
): Int {
    val length = output(outputBuffer)
    buffer.put(outputBuffer.view.slice(0, length))
    inputBuffer.reset()
    return length
}
