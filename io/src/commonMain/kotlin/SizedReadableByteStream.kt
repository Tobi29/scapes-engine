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
import org.tobi29.stdex.JsName

/**
 * [ReadableByteStream] exposing how much data is left in the stream
 */
interface SizedReadableByteStream : ReadableByteStream {
    /**
     * Amount of bytes left in the stream
     */
    val remaining: Int

    /**
     * `true` if there are remaining bytes and reading at least a
     * single byte will guaranteed not throw because of the stream ending
     */
    val hasRemaining: Boolean get() = remaining > 0

    /**
     * Skips through the entire stream
     * @throws IOException When an IO error occurs
     */
    fun consume() {
        while (hasRemaining) {
            skip(remaining)
        }
    }

    override fun getSome(buffer: Bytes): Int =
        remaining.let {
            if (it <= 0) -1
            else buffer.size.coerceAtMost(it).also { size ->
                get(buffer.slice(0, size))
            }
        }

    // TODO: Remove after 0.0.14

    /**
     * Returns amount of bytes left in the stream, may not change arbitrarily
     * @return Amount of bytes left in the stream
     */
    @JsName("remainingFun")
    @Deprecated("Use property", ReplaceWith("remaining"))
    fun remaining(): Int = remaining

    /**
     * `true` if there are remaining bytes and reading at least a
     * single byte will guaranteed not throw because of the stream ending
     */
    @JsName("hasRemainingFun")
    @Deprecated("Use property", ReplaceWith("hasRemaining"))
    fun hasRemaining(): Boolean = hasRemaining
}
