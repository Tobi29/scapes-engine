/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.utils.io

/**
 * [ReadableByteStream] exposing how much data is left in the stream
 */
interface SizedReadableByteStream : ReadableByteStream {
    /**
     * Returns amount of bytes left in the stream, may not change arbitrarily
     * @return Amount of bytes left in the stream
     */
    fun remaining(): Int

    /**
     * Returns `true` if there are remaining bytes and reading at least a
     * single byte will guaranteed not throw because of the stream ending
     * @return `true` if there are remaining bytes
     */
    fun hasRemaining(): Boolean = remaining() > 0

    /**
     * Skips through the entire stream
     * @throws IOException When an IO error occurs
     */
    fun consume() {
        while (hasRemaining()) {
            skip(remaining())
        }
    }

    override fun getSome(buffer: ByteView): Int =
            remaining().let {
                if (it <= 0) -1
                else buffer.size.coerceAtMost(it).also { size ->
                    get(buffer.slice(size = size))
                }
            }
}
