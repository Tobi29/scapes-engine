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

import org.tobi29.arrays.HeapBytes
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

actual class DeflateHandle actual constructor(
    level: Int
) : FilterHandle {
    private val deflater = Deflater(level)

    override fun process(
        inputBuffer: HeapBytes,
        outputBuffer: HeapBytes,
        output: (HeapBytes) -> Unit
    ): Boolean {
        try {
            deflater.setInput(
                inputBuffer.array,
                inputBuffer.offset,
                inputBuffer.size
            )
            while (!deflater.needsInput()) {
                val size = deflater.deflate(
                    outputBuffer.array,
                    outputBuffer.offset,
                    outputBuffer.size
                )
                if (size > 0) output(outputBuffer.slice(0, size))
                if (deflater.finished()) return false
            }
            return true
        } catch (e: DataFormatException) {
            throw DeflateException(e)
        }
    }

    override fun processFinish(
        outputBuffer: HeapBytes,
        output: (HeapBytes) -> Unit
    ) {
        try {
            deflater.finish()
            while (!deflater.finished()) {
                val size = deflater.deflate(
                    outputBuffer.array,
                    outputBuffer.offset,
                    outputBuffer.size
                )
                if (size > 0) output(outputBuffer.slice(0, size))
            }
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

    override fun process(
        inputBuffer: HeapBytes,
        outputBuffer: HeapBytes,
        output: (HeapBytes) -> Unit
    ): Boolean {
        try {
            inflater.setInput(
                inputBuffer.array,
                inputBuffer.offset,
                inputBuffer.size
            )
            while (!inflater.needsInput()) {
                val size = inflater.inflate(
                    outputBuffer.array,
                    outputBuffer.offset,
                    outputBuffer.size
                )
                if (size > 0) output(outputBuffer.slice(0, size))
                if (inflater.finished()) return false
            }
            return true
        } catch (e: DataFormatException) {
            throw DeflateException(e)
        }
    }

    override fun processFinish(
        outputBuffer: HeapBytes,
        output: (HeapBytes) -> Unit
    ) {
        try {
            while (!inflater.finished()) {
                val size = inflater.inflate(
                    outputBuffer.array,
                    outputBuffer.offset,
                    outputBuffer.size
                )
                if (size > 0) output(outputBuffer.slice(0, size))
            }
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
