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

import org.tobi29.arrays.Bytes
import org.tobi29.arrays.HeapBytes
import org.tobi29.arrays.sliceOver
import org.tobi29.io.*

class DeflateException : IOException {
    internal constructor(message: String) : super(message)
    internal constructor(cause: Exception) : super(cause)
}

interface FilterHandle : AutoCloseable {
    fun input(
        inputBuffer: HeapBytes
    )

    fun process(
        outputBuffer: HeapBytes
    ): Int

    fun processFinish(
        outputBuffer: HeapBytes
    ): Int

    fun reset()
}

const val FILTER_NEEDS_INPUT = 0
inline fun filteredBytes(size: Int): Int =
    if (size >= 0) size else size + Int.MIN_VALUE

expect class DeflateHandle(
    level: Int = -1
) : FilterHandle

expect class InflateHandle(
) : FilterHandle

fun deflate(
    input: ReadableByteStream,
    level: Int = -1,
    inputBuffer: HeapBytes = ByteArray(16384).sliceOver(),
    outputBuffer: HeapBytes = ByteArray(16384).sliceOver()
): Bytes {
    val stream = MemoryViewStreamDefault()
    deflate(input, stream, level, inputBuffer, outputBuffer)
    stream.flip()
    return stream.bufferSlice()
}

fun deflate(
    input: ReadableByteStream,
    output: WritableByteStream,
    level: Int = -1,
    inputBuffer: HeapBytes = ByteArray(16384).sliceOver(),
    outputBuffer: HeapBytes = ByteArray(16384).sliceOver()
) = DeflateHandle(level).use {
    it.process(input, output, inputBuffer, outputBuffer)
}

fun inflate(
    input: ReadableByteStream,
    inputBuffer: HeapBytes = ByteArray(16384).sliceOver(),
    outputBuffer: HeapBytes = ByteArray(16384).sliceOver()
): Bytes {
    val stream = MemoryViewStreamDefault()
    inflate(input, stream, inputBuffer, outputBuffer)
    stream.flip()
    return stream.bufferSlice()
}

fun inflate(
    input: ReadableByteStream,
    output: WritableByteStream,
    inputBuffer: HeapBytes = ByteArray(16384).sliceOver(),
    outputBuffer: HeapBytes = ByteArray(16384).sliceOver()
) = InflateHandle().use {
    it.process(input, output, inputBuffer, outputBuffer)
}

fun FilterHandle.process(
    input: ReadableByteStream,
    output: WritableByteStream,
    inputBuffer: HeapBytes = ByteArray(16384).sliceOver(),
    outputBuffer: HeapBytes = ByteArray(16384).sliceOver()
) {
    val callback: (HeapBytes) -> Unit = { output.put(it) }
    while (true) {
        val read = input.getSome(inputBuffer)
        if (read < 0) break
        if (!process(inputBuffer.slice(0, read), outputBuffer, callback)) break
    }
    processFinish(outputBuffer, callback)
}

inline fun FilterHandle.process(
    inputBuffer: HeapBytes,
    outputBuffer: HeapBytes,
    output: (HeapBytes) -> Unit
): Boolean {
    input(inputBuffer)
    while (true) {
        val size = process(outputBuffer)
        if (size == FILTER_NEEDS_INPUT) return true
        val bytes = filteredBytes(size)
        if (bytes != 0) output(outputBuffer.slice(0, bytes))
        if (size < 0) return false
    }
}

inline fun FilterHandle.processFinish(
    outputBuffer: HeapBytes,
    output: (HeapBytes) -> Unit
) {
    while (true) {
        val size = processFinish(outputBuffer)
        val bytes = filteredBytes(size)
        if (bytes != 0) output(outputBuffer.slice(0, bytes))
        if (size < 0) return
    }
}
