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

import org.tobi29.scapes.engine.utils.bytesUTF8

/**
 * Interface for blocking write operations, with an extensive set of operations
 * to make implementing protocols easier
 */
// TODO: Implement most operations with defaults
interface WritableByteStream : Appendable {
    /**
     * Write by processing the given buffer completely
     * @param buffer Buffer to read from
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun put(buffer: ByteBuffer): WritableByteStream =
            put(buffer, buffer.remaining())

    /**
     * Write by processing the given buffer by the given amount of bytes
     * @param buffer Buffer to read from
     * @param len The amount of bytes to read
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun put(buffer: ByteBuffer,
            len: Int): WritableByteStream

    /**
     * Write by processing the given array by the given amount of bytes
     * @param dest Array to read from
     * @param off First index to read from the byte array
     * @param len The amount of bytes to read
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun put(src: ByteArray,
            off: Int = 0,
            len: Int = src.size - off): WritableByteStream =
            put(src.asByteBuffer(off, len))

    /**
     * Writes `1` into the stream if [value] is `true` or `0` otherwise
     * @param value The value to write
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun putBoolean(value: Boolean): WritableByteStream =
            put(if (value) 1.toByte() else 0)

    /**
     * Writes the byte into the stream
     * @param value The value to write
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun put(b: Byte): WritableByteStream

    /**
     * Writes the big-endian short into the stream
     * @param value The value to write
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun putShort(value: Short): WritableByteStream

    /**
     * Writes the big-endian integer into the stream
     * @param value The value to write
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun putInt(value: Int): WritableByteStream

    /**
     * Writes the big-endian long into the stream
     * @param value The value to write
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun putLong(value: Long): WritableByteStream

    /**
     * Writes the big-endian float into the stream
     * @param value The value to write
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun putFloat(value: Float): WritableByteStream

    /**
     * Writes the big-endian double into the stream
     * @param value The value to write
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun putDouble(value: Double): WritableByteStream

    /**
     * Writes the byte array to the stream, prefixed by the length
     *
     * Format for length is either a single unsigned byte or 0xFF followed
     * by a big-endian int
     * @param value The value to write
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun putByteArray(value: ByteArray): WritableByteStream {
        if (value.size < 0xFF) {
            put(value.size.toByte())
        } else {
            put(0xFF.toByte())
            putInt(value.size)
        }
        return put(value)
    }

    /**
     * Writes the byte array to the stream, prefixed by the length
     *
     * Format for length is either a big-endian unsigned short or 0xFFFF
     * followed by a big-endian inth
     * @param value The value to write
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun putByteArrayLong(value: ByteArray): WritableByteStream {
        if (value.size < 0xFFFF) {
            putShort(value.size.toShort())
        } else {
            putShort(0xFFFF.toShort())
            putInt(value.size)
        }
        return put(value)
    }

    /**
     * Writes the string to the stream, encoded in UTF-8 and prefixes by length
     *
     * The format is consistent to first encoding the string in UTF-8 and then
     * writing the array using [putByteArray]
     * @param value The value to write
     * @throws IOException When an IO error occurs or limit was broken
     * @return The current stream
     */
    fun putString(value: String): WritableByteStream =
            putByteArray(value.bytesUTF8())

    override fun append(c: Char): Appendable {
        put(c.toString().bytesUTF8())
        return this
    }

    override fun append(csq: CharSequence?): Appendable =
            append(csq, 0, csq?.length ?: 4) // "null" is of length 4

    override fun append(csq: CharSequence?,
                        start: Int,
                        end: Int): Appendable {
        val str = csq?.toString() ?: "null"
        put(str.bytesUTF8())
        return this
    }
}
