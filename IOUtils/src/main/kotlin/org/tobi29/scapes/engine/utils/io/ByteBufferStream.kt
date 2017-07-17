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

import org.tobi29.scapes.engine.utils.assert

class ByteBufferStream(
        private val bufferProvider: ByteBufferProvider = DefaultByteBufferProvider,
        private val growth: (Int) -> Int = { it + 8192 },
        private var buffer: ByteBuffer = bufferProvider.allocate(growth(0))
) : RandomWritableByteStream, RandomReadableByteStream {
    constructor(buffer: ByteBuffer) : this(DefaultByteBufferProvider,
            buffer = buffer)

    fun buffer(): ByteBuffer {
        return buffer
    }

    override fun position(): Int {
        return buffer.position()
    }

    override fun position(pos: Int): ByteBufferStream {
        ensurePut(pos - buffer.position())
        buffer.position(pos)
        return this
    }

    override fun put(buffer: ByteBuffer,
                     len: Int): ByteBufferStream {
        ensurePut(len)
        val limit = buffer.limit()
        buffer.limit(buffer.position() + len)
        this.buffer.put(buffer)
        buffer.limit(limit)
        return this
    }

    override fun put(b: Byte): ByteBufferStream {
        ensurePut(1)
        buffer.put(b)
        return this
    }

    override fun putShort(value: Short): ByteBufferStream {
        ensurePut(2)
        buffer.putShort(value)
        return this
    }

    override fun putInt(value: Int): ByteBufferStream {
        ensurePut(4)
        buffer.putInt(value)
        return this
    }

    override fun putLong(value: Long): ByteBufferStream {
        ensurePut(8)
        buffer.putLong(value)
        return this
    }

    override fun putFloat(value: Float): ByteBufferStream {
        ensurePut(4)
        buffer.putFloat(value)
        return this
    }

    override fun putDouble(value: Double): ByteBufferStream {
        ensurePut(8)
        buffer.putDouble(value)
        return this
    }

    fun ensurePut(len: Int) {
        val used = buffer.position()
        var size = buffer.capacity()
        if (len <= size - used) {
            return
        }
        do {
            size = growth(size)
        } while (len > size - used)
        grow(size)
    }

    fun grow(size: Int = growth(buffer.capacity())) {
        if (size < buffer.capacity()) {
            throw IllegalArgumentException(
                    "Tried to shrink buffer with " + buffer.capacity() +
                            " bytes to " + size)
        }
        val newBuffer = bufferProvider.allocate(size)
        assert { newBuffer.capacity() == size }
        buffer.flip()
        newBuffer.put(buffer)
        buffer = newBuffer
    }

    override fun limit(): Int {
        return buffer.limit()
    }

    override fun limit(limit: Int): ReadableByteStream {
        buffer.limit(limit)
        return this
    }

    override fun remaining(): Int {
        return buffer.remaining()
    }

    override fun get(buffer: ByteBuffer,
                     len: Int): ReadableByteStream {
        ensureGet(len)
        val limit = this.buffer.limit()
        this.buffer.limit(this.buffer.position() + len)
        buffer.put(this.buffer)
        this.buffer.limit(limit)
        return this
    }

    override fun getSome(buffer: ByteBuffer,
                         len: Int): Boolean {
        if (!hasRemaining()) {
            return false
        }
        var len = len
        len = len.coerceAtMost(this.buffer.remaining())
        val limit = this.buffer.limit()
        this.buffer.limit(this.buffer.position() + len)
        buffer.put(this.buffer)
        this.buffer.limit(limit)
        return true
    }

    override fun get(): Byte {
        ensureGet(1)
        return buffer.get()
    }

    override fun getShort(): Short {
        ensureGet(2)
        return buffer.short
    }

    override fun getInt(): Int {
        ensureGet(4)
        return buffer.int
    }

    override fun getLong(): Long {
        ensureGet(8)
        return buffer.long
    }

    override fun getFloat(): Float {
        ensureGet(4)
        return buffer.float
    }

    override fun getDouble(): Double {
        ensureGet(8)
        return buffer.double
    }

    private fun ensureGet(len: Int) {
        if (buffer.remaining() < len) {
            throw IOException("End of stream")
        }
    }
}
