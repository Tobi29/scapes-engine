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

interface ReadableByteStream : Readable {
    fun available(): Int

    fun hasAvailable(): Boolean {
        return available() > 0
    }

    // TODO: @Throws(IOException::class)
    fun skip(len: Int) {
        get(ByteBuffer(len))
    }

    // TODO: @Throws(IOException::class)
    fun skip(len: Long) {
        while (len > Int.MAX_VALUE) {
            skip(Int.MAX_VALUE)
        }
        skip(len.toInt())
    }

    // TODO: @Throws(IOException::class)
    operator fun get(buffer: ByteBuffer): ReadableByteStream {
        return get(buffer, buffer.remaining())
    }

    // TODO: @Throws(IOException::class)
    operator fun get(buffer: ByteBuffer,
                     len: Int): ReadableByteStream

    // TODO: @Throws(IOException::class)
    fun getSome(buffer: ByteBuffer): Boolean {
        return getSome(buffer, buffer.remaining())
    }

    // TODO: @Throws(IOException::class)
    fun getSome(buffer: ByteBuffer,
                len: Int): Boolean

    // TODO: @Throws(IOException::class)
    operator fun get(dest: ByteArray,
                     off: Int = 0,
                     len: Int = dest.size): ReadableByteStream {
        return get(dest.asByteBuffer(off, len))
    }

    // TODO: @Throws(IOException::class)
    fun getSome(dest: ByteArray,
                off: Int = 0,
                len: Int = dest.size): Int {
        val buffer = dest.asByteBuffer(off, len)
        if (!getSome(buffer)) {
            return -1
        }
        return buffer.position() - off
    }

    // TODO: @Throws(IOException::class)
    fun getBoolean() = get() != 0.toByte()

    // TODO: @Throws(IOException::class)
    fun get(): Byte

    // TODO: @Throws(IOException::class)
    fun getUByte(): Short {
        var value = get().toShort()
        if (value < 0) {
            value = (value + 0x100).toShort()
        }
        return value
    }

    // TODO: @Throws(IOException::class)
    fun getShort(): Short

    // TODO: @Throws(IOException::class)
    fun getUShort(): Int {
        var value = getShort().toInt()
        if (value < 0) {
            value += 0x10000
        }
        return value
    }

    // TODO: @Throws(IOException::class)
    fun getInt(): Int

    // TODO: @Throws(IOException::class)
    fun getUInt(): Long {
        var value = getInt().toLong()
        if (value < 0) {
            value += 0x100000000L
        }
        return value
    }

    // TODO: @Throws(IOException::class)
    fun getLong(): Long

    // TODO: @Throws(IOException::class)
    fun getFloat(): Float

    // TODO: @Throws(IOException::class)
    fun getDouble(): Double

    // TODO: @Throws(IOException::class)
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

    // TODO: @Throws(IOException::class)
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

    // TODO: @Throws(IOException::class)
    fun getString(limit: Int = Int.MAX_VALUE): String {
        return getByteArray(limit).strUTF8()
    }

    // TODO: @Throws(IOException::class)
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
