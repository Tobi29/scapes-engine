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

class LimitedBufferStream(private val stream: ReadableByteStream,
                          private var remaining: Int) : SizedReadableByteStream {

    override fun available(): Int {
        return stream.available().coerceAtMost(remaining)
    }

    override fun skip(length: Int) = apply {
        check(length)
        stream.skip(length)
    }

    override fun get(buffer: ByteView) = apply {
        check(buffer.size)
        stream.get(buffer)
    }

    override fun getSome(buffer: ByteView): Int {
        if (remaining <= 0) return -1
        return buffer.size.coerceAtMost(remaining).let {
            stream.getSome(buffer.slice(0, it)).also {
                if (it < 0) throw EndOfStreamException()
                remaining -= it
            }
        }
    }

    override fun getTry(): Int {
        if (remaining < 1) return -1
        remaining--
        return stream.get().toInt() and 0xFF
    }

    override fun get(): Byte {
        check(1)
        return stream.get()
    }

    override fun getShort(): Short {
        check(2)
        return stream.getShort()
    }

    override fun getInt(): Int {
        check(4)
        return stream.getInt()
    }

    override fun getLong(): Long {
        check(8)
        return stream.getLong()
    }

    override fun getFloat(): Float {
        check(4)
        return stream.getFloat()
    }

    override fun getDouble(): Double {
        check(8)
        return stream.getDouble()
    }

    private fun check(len: Int) {
        if (remaining < len) {
            throw EndOfStreamException()
        }
        remaining -= len
    }

    override fun remaining(): Int {
        return remaining
    }

    override fun hasRemaining(): Boolean {
        return remaining > 0
    }
}
