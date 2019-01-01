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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.io.compression.deflate

import org.tobi29.arrays.HeapBytes
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

actual class DeflateHandle actual constructor(
    level: Int
) : FilterHandle {
    private val deflater = Deflater(level)

    override fun input(inputBuffer: HeapBytes) {
        deflater.setInput(
            inputBuffer.array,
            inputBuffer.offset,
            inputBuffer.size
        )
    }

    override fun process(
        outputBuffer: HeapBytes
    ): Int {
        try {
            if (deflater.needsInput()) return FILTER_NEEDS_INPUT
            return deflater.deflate(
                outputBuffer.array,
                outputBuffer.offset,
                outputBuffer.size
            ).let { if (deflater.finished()) it + Int.MIN_VALUE else it }
        } catch (e: DataFormatException) {
            throw DeflateException(e)
        }
    }

    override fun processFinish(
        outputBuffer: HeapBytes
    ): Int {
        try {
            deflater.finish()
            return deflater.deflate(
                outputBuffer.array,
                outputBuffer.offset,
                outputBuffer.size
            ).let { if (deflater.finished()) it + Int.MIN_VALUE else it }
        } catch (e: DataFormatException) {
            throw DeflateException(e)
        }
    }

    override fun reset() {
        deflater.reset()
    }

    override fun close() {
        deflater.end()
    }
}

actual class InflateHandle actual constructor(
) : FilterHandle {
    private val inflater = Inflater()

    override fun input(inputBuffer: HeapBytes) {
        inflater.setInput(
            inputBuffer.array,
            inputBuffer.offset,
            inputBuffer.size
        )
    }

    override fun process(
        outputBuffer: HeapBytes
    ): Int {
        try {
            if (inflater.needsInput()) return FILTER_NEEDS_INPUT
            return inflater.inflate(
                outputBuffer.array,
                outputBuffer.offset,
                outputBuffer.size
            ).let { if (inflater.finished()) it + Int.MIN_VALUE else it }
        } catch (e: DataFormatException) {
            throw DeflateException(e)
        }
    }

    override fun processFinish(
        outputBuffer: HeapBytes
    ): Int {
        try {
            return inflater.inflate(
                outputBuffer.array,
                outputBuffer.offset,
                outputBuffer.size
            ).let { if (inflater.finished()) it + Int.MIN_VALUE else it }
        } catch (e: DataFormatException) {
            throw DeflateException(e)
        }
    }

    override fun reset() {
        inflater.reset()
    }

    override fun close() {
        inflater.end()
    }
}
