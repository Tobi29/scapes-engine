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

package org.tobi29.io

import org.tobi29.arrays.Bytes
import org.tobi29.stdex.Throws

// TODO: Remove after 0.0.14

@Deprecated("Use compression-deflate module")
object CompressionUtil {

    // FIXME @Throws(IOException::class)
    fun compress(
        input: ReadableByteStream,
        level: Int = -1
    ): Bytes {
        val stream = MemoryViewStreamDefault()
        compress(input, stream, level)
        stream.flip()
        return stream.bufferSlice()
    }

    // FIXME @Throws(IOException::class)
    fun compress(
        input: ReadableByteStream,
        output: WritableByteStream,
        level: Int = 1
    ) {
        ZDeflater(level, 8192).use { filter -> filter(input, output, filter) }
    }

    // FIXME @Throws(IOException::class)
    fun decompress(input: ReadableByteStream): Bytes {
        val stream = MemoryViewStreamDefault()
        decompress(input, stream)
        stream.flip()
        return stream.bufferSlice()
    }

    // FIXME @Throws(IOException::class)
    fun decompress(
        input: ReadableByteStream,
        output: WritableByteStream
    ) {
        ZInflater(8192).use { filter -> filter(input, output, filter) }
    }

    // FIXME @Throws(IOException::class)
    fun filter(
        input: ReadableByteStream,
        output: WritableByteStream,
        filter: Filter
    ) {
        while (filter.input(input)) {
            while (!filter.needsInput()) {
                val len = filter.output(output)
                if (len <= 0) {
                    break
                }
            }
        }
        filter.finish()
        while (!filter.finished()) {
            val len = filter.output(output)
            if (len <= 0) {
                break
            }
        }
        filter.reset()
    }

    interface Filter : AutoCloseable {
        // FIXME @Throws(IOException::class)
        fun input(buffer: ReadableByteStream): Boolean

        // FIXME @Throws(IOException::class)
        fun output(buffer: WritableByteStream): Int

        fun finish()

        fun needsInput(): Boolean

        fun finished(): Boolean

        fun reset()

        override fun close()
    }
}

expect class ZDeflater : CompressionUtil.Filter {
    constructor(level: Int, buffer: Int)

    override fun input(buffer: ReadableByteStream): Boolean
    override fun output(buffer: WritableByteStream): Int
    override fun finish()
    override fun needsInput(): Boolean
    override fun finished(): Boolean
    override fun reset()
    override fun close()
}

expect class ZInflater : CompressionUtil.Filter {
    constructor(buffer: Int)

    override fun input(buffer: ReadableByteStream): Boolean
    override fun output(buffer: WritableByteStream): Int
    override fun finish()
    override fun needsInput(): Boolean
    override fun finished(): Boolean
    override fun reset()
    override fun close()
}
