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

import org.tobi29.scapes.engine.utils.IOException
import org.tobi29.scapes.engine.utils.assert

interface ReadableByteStream {
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
    operator fun get(src: ByteArray,
                     off: Int = 0,
                     len: Int = src.size): ReadableByteStream {
        return get(ByteBuffer.wrap(src, off, len))
    }

    // TODO: @Throws(IOException::class)
    fun getSome(src: ByteArray,
                off: Int,
                len: Int): Int {
        val buffer = ByteBuffer.wrap(src, off, len)
        val available = getSome(buffer)
        var position = buffer.position()
        position -= off
        if (position == 0 && !available) {
            return -1
        }
        assert { position <= len }
        return position
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
        return String(getByteArray(limit))
    }
}
