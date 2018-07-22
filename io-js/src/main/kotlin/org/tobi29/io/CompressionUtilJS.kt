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

import org.tobi29.arrays.sliceOver
import org.tobi29.kzlib.*

// TODO: Remove after 0.0.14

actual class ZDeflater actual constructor(
    private val level: Int,
    buffer: Int
) : CompressionUtil.Filter {
    private val inputBuffer = ByteArray(buffer).sliceOver()
    private val outputBuffer = ByteArray(buffer).sliceOver()
    private var finishing = false
    private var hasFinished = false
    private val deflater = Deflater(level)

    actual override fun input(buffer: ReadableByteStream): Boolean {
        if (deflater.first.avail_in <= 0) {
            val read = buffer.getSome(inputBuffer)
            if (read < 0) return false
            deflater.first.next_in = inputBuffer.array
            deflater.first.next_in_i = inputBuffer.offset
            deflater.first.avail_in = read
        }
        return true
    }

    actual override fun output(buffer: WritableByteStream): Int {
        deflater.first.next_out = outputBuffer.array
        deflater.first.next_out_i = outputBuffer.offset
        deflater.first.avail_out = outputBuffer.size
        val status = deflater.deflate(if (finishing) Z_FINISH else Z_NO_FLUSH)
        if (status == Z_STREAM_END) hasFinished = true
        return deflater.first.next_out_i - outputBuffer.offset
    }

    actual override fun finish() {
        finishing = true
    }

    actual override fun reset() {
        try {
            deflater.reset()
        } catch (e: ZLibException) {
            throw IOException(e)
        }
    }

    actual override fun needsInput(): Boolean = deflater.first.avail_in <= 0

    actual override fun finished(): Boolean = hasFinished

    actual override fun close() {
        try {
            deflater.end()
        } catch (e: ZLibException) {
            throw IOException(e)
        }
    }
}

actual class ZInflater actual constructor(
    buffer: Int
) : CompressionUtil.Filter {
    private val inputBuffer = ByteArray(buffer).sliceOver()
    private val outputBuffer = ByteArray(buffer).sliceOver()
    private var finishing = false
    private var hasFinished = false
    private val inflater = Inflater()

    actual override fun input(buffer: ReadableByteStream): Boolean {
        if (inflater.first.avail_in <= 0) {
            val read = buffer.getSome(inputBuffer)
            if (read < 0) return false
            inflater.first.next_in = inputBuffer.array
            inflater.first.next_in_i = inputBuffer.offset
            inflater.first.avail_in = read
        }
        return true
    }

    actual override fun output(buffer: WritableByteStream): Int {
        inflater.first.next_out = outputBuffer.array
        inflater.first.next_out_i = outputBuffer.offset
        inflater.first.avail_out = outputBuffer.size
        val status = inflater.inflate(if (finishing) Z_FINISH else Z_NO_FLUSH)
        if (status == Z_STREAM_END) hasFinished = true
        return inflater.first.next_out_i - outputBuffer.offset
    }

    actual override fun finish() {
        finishing = true
    }

    actual override fun reset() {
        try {
            inflater.reset()
        } catch (e: ZLibException) {
            throw IOException(e)
        }
    }

    actual override fun needsInput(): Boolean = inflater.first.avail_in <= 0

    actual override fun finished(): Boolean = hasFinished

    actual override fun close() {
        try {
            inflater.end()
        } catch (e: ZLibException) {
            throw IOException(e)
        }
    }
}
