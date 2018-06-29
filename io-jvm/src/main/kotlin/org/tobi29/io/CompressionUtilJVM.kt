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

package org.tobi29.io

import java.util.zip.Deflater
import java.util.zip.Inflater

// TODO: Remove after 0.0.14

actual class ZDeflater actual constructor(
    level: Int,
    private val buffer: Int
) : CompressionUtil.Filter {
    private val deflater = Deflater(level)
    private val output = ByteArray(buffer)
    private var input = MemoryViewStreamDefault()

    actual override fun input(buffer: ReadableByteStream): Boolean {
        input.limit(input.position() + this.buffer)
        val read = buffer.getSome(input.bufferSlice())
        if (read < 0) return false
        input.position(input.position() + read)
        input.buffer().slice(0, input.position()).let {
            deflater.setInput(it.array, it.offset, it.size)
        }
        return true
    }

    actual override fun output(buffer: WritableByteStream): Int {
        val length = deflater.deflate(output)
        buffer.put(output.view.slice(0, length))
        input.reset()
        return length
    }

    actual override fun finish() {
        deflater.finish()
    }

    actual override fun needsInput(): Boolean {
        return deflater.needsInput()
    }

    actual override fun finished(): Boolean {
        return deflater.finished()
    }

    actual override fun reset() {
        deflater.reset()
    }

    actual override fun close() {
        deflater.end()
    }
}

actual class ZInflater actual constructor(
    private val buffer: Int
) : CompressionUtil.Filter {
    private val inflater = Inflater()
    private val output = ByteArray(buffer)
    private var input = MemoryViewStreamDefault()

    actual override fun input(buffer: ReadableByteStream): Boolean {
        input.limit(input.position() + this.buffer)
        val read = buffer.getSome(input.bufferSlice())
        if (read < 0) return false
        input.position(input.position() + read)
        input.buffer().slice(0, input.position()).let {
            inflater.setInput(it.array, it.offset, it.size)
        }
        return true
    }

    actual override fun output(buffer: WritableByteStream): Int {
        val length = inflater.inflate(output)
        buffer.put(output.view.slice(0, length))
        input.reset()
        return length
    }

    actual override fun finish() {
    }

    actual override fun needsInput(): Boolean {
        return inflater.needsInput()
    }

    actual override fun finished(): Boolean {
        return inflater.finished()
    }

    actual override fun reset() {
        inflater.reset()
    }

    actual override fun close() {
        inflater.end()
    }
}
