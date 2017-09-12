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

import org.tobi29.scapes.engine.utils.strUTF8
import kotlin.experimental.and

/**
 * Interface for blocking read operations, with an extensive set of operations
 * to make implementing protocols easier
 */
// TODO: Implement most operations with defaults
interface ReadableByteStream : Readable {
    /**
     * Returns the amount of bytes available to be read from the stream without
     * blocking
     */
    fun available(): Int

    /**
     * Skip the given amount of bytes in the stream
     * @param len The amount of bytes to skip
     * @throws IOException When an IO error occurs
     */
    fun skip(len: Int) {
        get(ByteBuffer(len))
    }

    /**
     * Skip the given amount of bytes in the stream
     * @param len The amount of bytes to skip
     * @throws IOException When an IO error occurs
     */
    fun skip(len: Long) {
        var l = len
        while (l > Int.MAX_VALUE) {
            skip(Int.MAX_VALUE)
            l -= Int.MAX_VALUE
        }
        skip(l.toInt())
    }

    /**
     * Read by filling the given buffer completely
     * @param buffer Buffer to write to
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    operator fun get(buffer: ByteBuffer): ReadableByteStream {
        return get(buffer, buffer.remaining())
    }

    /**
     * Read by filling the given buffer by the given amount of bytes
     * @param buffer Buffer to write to
     * @param len The amount of bytes to read
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    operator fun get(buffer: ByteBuffer,
                     len: Int): ReadableByteStream

    /**
     * Read by filling the given buffer as much as possible
     * @param buffer Buffer to write to
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun getSome(buffer: ByteBuffer): Boolean {
        return getSome(buffer, buffer.remaining())
    }

    /**
     * Read by filling the given buffer as much as possible
     * @param buffer Buffer to write to
     * @param len The amount of bytes to read at most
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun getSome(buffer: ByteBuffer,
                len: Int): Boolean

    /**
     * Read by filling the given array by the given amount of bytes
     * @param dest Array to write to
     * @param off First index to write into the byte array
     * @param len The amount of bytes to read
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    operator fun get(dest: ByteArray,
                     off: Int = 0,
                     len: Int = dest.size - off): ReadableByteStream {
        return get(dest.asByteBuffer(off, len))
    }

    /**
     * Read by filling the given array at most by the given amount of bytes as
     * much as possible
     * @param dest Array to write to
     * @param off First index to write into the byte array
     * @param len The amount of bytes to read
     * @throws IOException When an IO error occurs
     * @return The current stream
     */
    fun getSome(dest: ByteArray,
                off: Int = 0,
                len: Int = dest.size - off): Int {
        val buffer = dest.asByteBuffer(off, len)
        if (!getSome(buffer)) return -1
        return buffer.position() - off
    }

    /**
     * Reads a boolean
     * @throws IOException When an IO error occurs
     * @return `true` if the byte in the stream is not `0`
     */
    fun getBoolean() = get() != 0.toByte()

    /**
     * Reads a byte
     * @throws IOException When an IO error occurs
     * @return The byte in the stream
     */
    fun get(): Byte

    /**
     * Reads an unsigned byte
     * @throws IOException When an IO error occurs
     * @return The byte in the stream
     */
    fun getUByte(): Short {
        var value = get().toShort()
        if (value < 0) value = (value + 0x100).toShort()
        return value
    }

    /**
     * Reads a big-endian short
     * @throws IOException When an IO error occurs
     * @return The short in the stream
     */
    fun getShort(): Short

    /**
     * Reads a big-endian unsigned short
     * @throws IOException When an IO error occurs
     * @return The short in the stream
     */
    fun getUShort(): Int {
        var value = getShort().toInt()
        if (value < 0) value += 0x10000
        return value
    }

    /**
     * Reads a big-endian integer
     * @throws IOException When an IO error occurs
     * @return The integer in the stream
     */
    fun getInt(): Int

    /**
     * Reads a big-endian unsigned integer
     * @throws IOException When an IO error occurs
     * @return The integer in the stream
     */
    fun getUInt(): Long {
        var value = getInt().toLong()
        if (value < 0) value += 0x100000000L
        return value
    }

    /**
     * Reads a big-endian long
     * @throws IOException When an IO error occurs
     * @return The long in the stream
     */
    fun getLong(): Long

    /**
     * Reads a float
     * @throws IOException When an IO error occurs
     * @return The float in the stream
     */
    fun getFloat(): Float

    /**
     * Reads a double
     * @throws IOException When an IO error occurs
     * @return The double in the stream
     */
    fun getDouble(): Double

    /**
     * Reads a byte array by fetching the length from the stream
     *
     * Format for length is either a single unsigned byte or 0xFF followed
     * by a big-endian int
     *
     * **Note:** It is highly recommended to set a limit to avoid attackers to
     * crash the program by setting the length to [Int.MAX_VALUE]
     * @param limit Maximum length to read before throwing an [IOException]
     * @throws IOException When an IO error occurs or limit was broken
     * @return The byte array in the stream
     */
    fun getByteArray(limit: Int = Int.MAX_VALUE): ByteArray {
        var len = getUByte().toInt()
        if (len == 0xFF) {
            len = getInt()
        }
        if (len < 0 || len > limit) {
            throw IOException(
                    "Array length outside of 0 to $limit: $limit")
        }
        val array = ByteArray(len)
        get(array, 0, array.size)
        return array
    }

    /**
     * Reads a byte array by fetching the length from the stream
     *
     * Format for length is either a big-endian unsigned short or 0xFFFF
     * followed by a big-endian int
     *
     * **Note:** It is highly recommended to set a limit to avoid attackers to
     * crash the program by setting the length to [Int.MAX_VALUE]
     * @param limit Maximum length to read before throwing an [IOException]
     * @throws IOException When an IO error occurs or limit was broken
     * @return The byte array in the stream
     */
    fun getByteArrayLong(limit: Int = Int.MAX_VALUE): ByteArray {
        var len = getUShort()
        if (len == 0xFFFF) {
            len = getInt()
        }
        if (len < 0 || len > limit) {
            throw IOException(
                    "Array length outside of 0 to $limit: $limit")
        }
        val array = ByteArray(len)
        get(array, 0, array.size)
        return array
    }

    /**
     * Reads a UTF-8 string by fetching the length from the stream
     *
     * The format is consistent to first reading the data using [getByteArray]
     * and then decoding the array from UTF-8
     *
     * **Note:** It is highly recommended to set a limit to avoid attackers to
     * crash the program by setting the length to [Int.MAX_VALUE]
     * @param limit Maximum length to read before throwing an [IOException], in UTF-8 bytes, *not* characters
     * @throws IOException When an IO error occurs or limit was broken
     * @return The string in the stream
     */
    fun getString(limit: Int = Int.MAX_VALUE): String {
        return getByteArray(limit).strUTF8()
    }

    /**
     * Reads a single character from the stream encoded in UTF-8
     * @throws IOException When an IO error occurs or an invalid UTF-8 sequence appeared
     * @return The character in the stream
     */
    override fun read(): Char {
        val initial = get()
        if (initial and 0b10000000.toByte() == 0b00000000.toByte()) {
            return initial.toChar()
        } else if (initial and 0b11100000.toByte() == 0b11000000.toByte()) {
            val extra1 = get()
            if (extra1 and 0b11000000.toByte() != 0b10000000.toByte()) {
                throw IOException("Invalid UTF-8 byte: $extra1")
            }
            val c = ((initial.toInt() and 0b00011111) shl 6) or
                    ((extra1.toInt() and 0b00111111) shl 0)
            return c.toChar()
        } else if (initial and 0b11110000.toByte() == 0b11100000.toByte()) {
            val extra1 = get()
            val extra2 = get()
            if (extra1 and 0b11000000.toByte() != 0b10000000.toByte()) {
                throw IOException("Invalid UTF-8 byte: $extra1")
            } else if (extra2 and 0b11000000.toByte() != 0b10000000.toByte()) {
                throw IOException("Invalid UTF-8 byte: $extra2")
            }
            val c = ((initial.toInt() and 0b00011111) shl 12) or
                    ((extra1.toInt() and 0b00111111) shl 6) or
                    ((extra2.toInt() and 0b00111111) shl 0)
            return c.toChar()
        } else if (initial and 0b11111000.toByte() == 0b11110000.toByte()) {
            val extra1 = get()
            val extra2 = get()
            val extra3 = get()
            if (extra1 and 0b11000000.toByte() != 0b10000000.toByte()) {
                throw IOException("Invalid UTF-8 byte: $extra1")
            } else if (extra2 and 0b11000000.toByte() != 0b10000000.toByte()) {
                throw IOException("Invalid UTF-8 byte: $extra2")
            } else if (extra3 and 0b11000000.toByte() != 0b10000000.toByte()) {
                throw IOException("Invalid UTF-8 byte: $extra3")
            }
            val c = ((initial.toInt() and 0b00011111) shl 18) or
                    ((extra1.toInt() and 0b00111111) shl 12) or
                    ((extra2.toInt() and 0b00111111) shl 6) or
                    ((extra2.toInt() and 0b00111111) shl 0)
            return c.toChar()
        } else {
            throw IOException("Invalid UTF-8 byte: $initial")
        }
    }
}
