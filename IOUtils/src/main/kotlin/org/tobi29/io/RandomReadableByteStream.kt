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

package org.tobi29.io

/**
 * [SizedReadableByteStream] supporting arbitrary seeking
 */
interface RandomReadableByteStream : SizedReadableByteStream {
    override fun available(): Int = remaining()

    override fun skip(length: Int) = position(position() + length)

    /**
     * Returns current position of the stream
     * @return Current position of the stream
     */
    fun position(): Int

    /**
     * Set current position of the stream
     * @param pos New position
     * @throws IllegalArgumentException When an invalid position was given
     * @return The current stream
     */
    fun position(pos: Int): ReadableByteStream

    /**
     * Returns current limit of the stream
     * @return Current limit of the stream
     */
    fun limit(): Int

    /**
     * Set current limit of the stream
     * @param limit New position
     * @throws IllegalArgumentException When an invalid limit was given
     * @return The current stream
     */
    fun limit(limit: Int): ReadableByteStream
}
