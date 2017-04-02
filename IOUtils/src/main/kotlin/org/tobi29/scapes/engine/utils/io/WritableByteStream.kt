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

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

interface WritableByteStream {
    @Throws(IOException::class)
    fun put(buffer: ByteBuffer): WritableByteStream {
        return put(buffer, buffer.remaining())
    }

    @Throws(IOException::class)
    fun put(buffer: ByteBuffer,
            len: Int): WritableByteStream

    @Throws(IOException::class)
    fun put(src: ByteArray,
            off: Int = 0,
            len: Int = src.size): WritableByteStream {
        return put(ByteBuffer.wrap(src, off, len))
    }

    @Throws(IOException::class)
    fun putBoolean(value: Boolean): WritableByteStream {
        return put(if (value) 1.toByte() else 0)
    }

    @Throws(IOException::class)
    fun put(b: Byte): WritableByteStream

    @Throws(IOException::class)
    fun putShort(value: Short): WritableByteStream

    @Throws(IOException::class)
    fun putInt(value: Int): WritableByteStream

    @Throws(IOException::class)
    fun putLong(value: Long): WritableByteStream

    @Throws(IOException::class)
    fun putFloat(value: Float): WritableByteStream

    @Throws(IOException::class)
    fun putDouble(value: Double): WritableByteStream

    @Throws(IOException::class)
    fun putByteArray(value: ByteArray): WritableByteStream {
        if (value.size < 0xFF) {
            put(value.size.toByte())
        } else {
            put(0xFF.toByte())
            putInt(value.size)
        }
        return put(value)
    }

    @Throws(IOException::class)
    fun putByteArrayLong(value: ByteArray): WritableByteStream {
        if (value.size < 0xFFFF) {
            putShort(value.size.toShort())
        } else {
            putShort(0xFFFF.toShort())
            putInt(value.size)
        }
        return put(value)
    }

    @Throws(IOException::class)
    fun putString(value: String): WritableByteStream {
        return putByteArray(value.toByteArray(StandardCharsets.UTF_8))
    }
}
