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

expect class DeflateHandle(
    level: Int = -1,
    outputBufferSize: Int = 8192,
    inputBufferSize: Int = 8192
) : AutoCloseable {
    fun reset()
}

expect class InflateHandle(
    outputBufferSize: Int = 8192,
    inputBufferSize: Int = 8192
) : AutoCloseable {
    fun reset()
}

fun deflate(
    input: ReadableByteStream,
    level: Int = -1
): ByteView {
    val stream = MemoryViewStreamDefault()
    deflate(input, stream, level)
    stream.flip()
    return stream.bufferSlice()
}

fun deflate(
    input: ReadableByteStream,
    output: WritableByteStream,
    level: Int = -1
) = DeflateHandle(level).use { it.deflate(input, output) }

expect fun DeflateHandle.deflate(
    input: ReadableByteStream,
    output: WritableByteStream
)

fun inflate(
    input: ReadableByteStream
): ByteView {
    val stream = MemoryViewStreamDefault()
    inflate(input, stream)
    stream.flip()
    return stream.bufferSlice()
}

fun inflate(
    input: ReadableByteStream,
    output: WritableByteStream
) = InflateHandle().use { it.inflate(input, output) }

expect fun InflateHandle.inflate(
    input: ReadableByteStream,
    output: WritableByteStream
)
