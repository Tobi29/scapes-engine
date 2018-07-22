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

import org.tobi29.arrays.HeapBytes
import org.tobi29.kzlib.*

actual class DeflateHandle(
    level: Int = -1,
    method: Int = Z_DEFLATED,
    windowBits: Int = DEF_WBITS,
    memLevel: Int = DEF_MEM_LEVEL,
    strategy: Int = Z_DEFAULT_STRATEGY,
    wrapperType: WrapperType = WrapperType.ZLIB
) : FilterHandle {
    private val deflater = Deflater(
        level, method, windowBits, memLevel, strategy, wrapperType
    )

    actual constructor(
        level: Int
    ) : this(
        level,
        Z_DEFLATED, DEF_WBITS, DEF_MEM_LEVEL, Z_DEFAULT_STRATEGY,
        WrapperType.ZLIB
    )

    override fun process(
        inputBuffer: HeapBytes,
        outputBuffer: HeapBytes,
        output: (HeapBytes) -> Unit
    ): Boolean {
        try {
            val stream = deflater.first
            stream.next_in = inputBuffer.array
            stream.next_in_i = inputBuffer.offset
            stream.avail_in = inputBuffer.size
            while (stream.avail_in > 0) {
                stream.next_out = outputBuffer.array
                stream.next_out_i = outputBuffer.offset
                stream.avail_out = outputBuffer.size
                val status = deflater.deflate(Z_NO_FLUSH)
                output(
                    outputBuffer.slice(
                        0, stream.next_out_i - outputBuffer.offset
                    )
                )
                if (status == Z_STREAM_END) return false
            }
            return true
        } catch (e: ZLibException) {
            throw DeflateException(e)
        }
    }

    override fun processFinish(
        outputBuffer: HeapBytes,
        output: (HeapBytes) -> Unit
    ) {
        try {
            val stream = deflater.first
            while (true) {
                stream.next_out = outputBuffer.array
                stream.next_out_i = outputBuffer.offset
                stream.avail_out = outputBuffer.size
                val status = deflater.deflate(Z_FINISH)
                output(
                    outputBuffer.slice(
                        0, stream.next_out_i - outputBuffer.offset
                    )
                )
                if (status == Z_STREAM_END) return
            }
        } catch (e: ZLibException) {
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

actual class InflateHandle(
    windowBits: Int = DEF_WBITS,
    wrapperType: WrapperType = WrapperType.ZLIB
) : FilterHandle {
    private val inflater = Inflater(
        windowBits, wrapperType
    )

    actual constructor(
    ) : this(
        DEF_WBITS,
        WrapperType.ZLIB
    )

    override fun process(
        inputBuffer: HeapBytes,
        outputBuffer: HeapBytes,
        output: (HeapBytes) -> Unit
    ): Boolean {
        try {
            val stream = inflater.first
            stream.next_in = inputBuffer.array
            stream.next_in_i = inputBuffer.offset
            stream.avail_in = inputBuffer.size
            while (stream.avail_in > 0) {
                stream.next_out = outputBuffer.array
                stream.next_out_i = outputBuffer.offset
                stream.avail_out = outputBuffer.size
                val status = inflater.inflate(Z_NO_FLUSH)
                output(
                    outputBuffer.slice(
                        0, stream.next_out_i - outputBuffer.offset
                    )
                )
                if (status == Z_STREAM_END) return false
            }
            return true
        } catch (e: ZLibException) {
            throw DeflateException(e)
        }
    }

    override fun processFinish(
        outputBuffer: HeapBytes,
        output: (HeapBytes) -> Unit
    ) {
        try {
            val stream = inflater.first
            while (true) {
                stream.next_out = outputBuffer.array
                stream.next_out_i = outputBuffer.offset
                stream.avail_out = outputBuffer.size
                val status = inflater.inflate(Z_FINISH)
                output(
                    outputBuffer.slice(
                        0, stream.next_out_i - outputBuffer.offset
                    )
                )
                if (status == Z_STREAM_END) return
            }
        } catch (e: ZLibException) {
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
