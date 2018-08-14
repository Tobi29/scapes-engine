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

import org.tobi29.stdex.JsName

/**
 * [SizedReadableByteStream] supporting arbitrary seeking
 */
interface RandomReadableByteStream : SizedReadableByteStream {
    var position: Int
    var limit: Int

    override val available: Int get() = remaining
    override val remaining get() = limit - position

    override fun skip(length: Int) {
        position += length
    }

    // TODO: Remove after 0.0.14

    /**
     * Returns current position of the stream
     * @return Current position of the stream
     */
    @JsName("positionFun")
    @Deprecated("Use property", ReplaceWith("position"))
    fun position(): Int = position

    /**
     * Set current position of the stream
     * @param pos New position
     * @throws IllegalArgumentException When an invalid position was given
     */
    @JsName("positionFunSet")
    @Deprecated("Use property")
    fun position(pos: Int) {
        position = pos
    }

    /**
     * Returns current limit of the stream
     * @return Current limit of the stream
     */
    @JsName("limitFun")
    @Deprecated("Use property", ReplaceWith("limit"))
    fun limit(): Int = limit

    /**
     * Set current limit of the stream
     * @param limit New position
     * @throws IllegalArgumentException When an invalid limit was given
     */
    @JsName("limitFunSet")
    @Deprecated("Use property")
    fun limit(limit: Int) {
        this.limit = limit
    }
}
