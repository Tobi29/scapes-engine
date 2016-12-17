/*
 * Copyright 2012-2016 Tobi29
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

import org.tobi29.scapes.engine.utils.ByteBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

interface ReadableByteStream {
    fun available(): Int

    fun hasAvailable(): Boolean {
        return available() > 0
    }

    @Throws(IOException::class)
    fun skip(len: Int) {
        get(ByteBuffer(len))
    }

    @Throws(IOException::class)
    fun skip(len: Long) {
        while (len > Int.MAX_VALUE) {
            skip(Int.MAX_VALUE)
        }
        skip(len.toInt())
    }

    @Throws(IOException::class)
    operator fun get(buffer: ByteBuffer): ReadableByteStream {
        return get(buffer, buffer.remaining())
    }

    @Throws(IOException::class)
    operator fun get(buffer: ByteBuffer,
                     len: Int): ReadableByteStream

    @Throws(IOException::class)
    fun getSome(buffer: ByteBuffer): Boolean {
        return getSome(buffer, buffer.remaining())
    }

    @Throws(IOException::class)
    fun getSome(buffer: ByteBuffer,
                len: Int): Boolean

    @Throws(IOException::class)
    operator fun get(src: ByteArray,
                     off: Int = 0,
                     len: Int = src.size): ReadableByteStream {
        return get(ByteBuffer.wrap(src, off, len))
    }

    @Throws(IOException::class)
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
        assert(position <= len)
        return position
    }

    val boolean: Boolean
        @Throws(IOException::class)
        get() = get().toInt() != 0

    @Throws(IOException::class)
    fun get(): Byte

    val uByte: Short
        @Throws(IOException::class)
        get() {
            var value = get().toShort()
            if (value < 0) {
                value = (value + 0x100).toShort()
            }
            return value
        }

    val short: Short

    val uShort: Int
        @Throws(IOException::class)
        get() {
            var value = short.toInt()
            if (value < 0) {
                value += 0x10000
            }
            return value
        }

    val int: Int

    val uInt: Long
        @Throws(IOException::class)
        get() {
            var value = int.toLong()
            if (value < 0) {
                value += 0x100000000L
            }
            return value
        }

    val long: Long

    val float: Float

    val double: Double

    val byteArray: ByteArray
        @Throws(IOException::class)
        get() = getByteArray(Int.MAX_VALUE)

    @Throws(IOException::class)
    fun getByteArray(limit: Int): ByteArray {
        var len = uByte.toInt()
        if (len == 0xFF) {
            len = int
        }
        if (len < 0 || len > limit) {
            throw IOException(
                    "Array length outside of 0 to $limit: $limit")
        }
        val array = ByteArray(len)
        get(array, 0, array.size)
        return array
    }

    val byteArrayLong: ByteArray
        @Throws(IOException::class)
        get() = getByteArrayLong(Int.MAX_VALUE)

    @Throws(IOException::class)
    fun getByteArrayLong(limit: Int): ByteArray {
        var len = uShort
        if (len == 0xFFFF) {
            len = int
        }
        if (len < 0 || len > limit) {
            throw IOException(
                    "Array length outside of 0 to $limit: $limit")
        }
        val array = ByteArray(len)
        get(array, 0, array.size)
        return array
    }

    val string: String
        @Throws(IOException::class)
        get() = getString(Int.MAX_VALUE)

    @Throws(IOException::class)
    fun getString(limit: Int): String {
        return String(getByteArray(limit), StandardCharsets.UTF_8)
    }
}
