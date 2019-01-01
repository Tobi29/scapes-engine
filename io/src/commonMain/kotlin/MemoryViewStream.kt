/*
 * Copyright 2012-2019 Tobi29
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

import org.tobi29.arrays.Bytes
import org.tobi29.arrays.BytesRO
import org.tobi29.stdex.assert

typealias MemoryViewProvider<B> = (Int) -> B

val DefaultMemoryViewProvider: MemoryViewProvider<HeapViewByteBE> =
    { HeapViewByteBE(ByteArray(it), 0, it) }

interface MemoryStream : RandomWritableByteStream, RandomReadableByteStream {
    fun flip()
    fun rewind()
    fun reset()

    // TODO: Remove after 0.0.14

    override fun position(): Int =
        super<RandomReadableByteStream>.position()

    override fun position(pos: Int) =
        super<RandomReadableByteStream>.position(pos)
}

fun MemoryViewStreamDefault(
    buffer: HeapViewByteBE
): MemoryViewStream<HeapViewByteBE> =
    MemoryViewStreamDefault({ (it shl 1).coerceAtLeast(8192) }, buffer)

fun MemoryViewStreamDefault(
    growth: (Int) -> Int = { (it shl 1).coerceAtLeast(8192) },
    buffer: HeapViewByteBE = DefaultMemoryViewProvider(growth(0))
): MemoryViewStream<HeapViewByteBE> =
    MemoryViewStream(DefaultMemoryViewProvider, growth, buffer)

sealed class MemoryViewReadableStream<out B : ByteViewERO>(
    protected var _position: Int,
    protected var _limit: Int
) : RandomReadableByteStream {
    protected abstract val mbuffer: B

    fun buffer(): B = mbuffer

    abstract fun bufferSlice(): B

    fun ensure(len: Int) {
        if (!ensureTry(len)) throw EndOfStreamException()
    }

    abstract fun ensureTry(len: Int): Boolean

    override fun getTry(): Int {
        if (!ensureTry(1)) return -1
        return mbuffer.getByte(_position).also { _position++ }.toInt() and 0xFF
    }

    override fun get(): Byte {
        ensure(1)
        return mbuffer.getByte(_position).also { _position++ }
    }

    override fun getShort(): Short {
        ensure(2)
        return mbuffer.getShort(_position).also { _position += 2 }
    }

    override fun getInt(): Int {
        ensure(4)
        return mbuffer.getInt(_position).also { _position += 4 }
    }

    override fun getLong(): Long {
        ensure(8)
        return mbuffer.getLong(_position).also { _position += 8 }
    }

    override fun getFloat(): Float {
        ensure(4)
        return mbuffer.getFloat(_position).also { _position += 4 }
    }

    override fun getDouble(): Double {
        ensure(8)
        return mbuffer.getDouble(_position).also { _position += 8 }
    }

    override fun get(buffer: Bytes) {
        ensure(buffer.size)
        mbuffer.getBytes(_position, buffer)
        _position += buffer.size
    }
}

fun <B : ByteViewERO> MemoryViewReadableStream(
    mbuffer: B
): MemoryViewReadableStream<B> = MemoryViewReadableStreamImpl(mbuffer)

private class MemoryViewReadableStreamImpl<out B : ByteViewERO>(
    override val mbuffer: B
) : MemoryViewReadableStream<B>(0, mbuffer.size) {
    override var position: Int
        get() = _position
        set (value) {
            if (value < 0 || value > _limit)
                throw IllegalArgumentException("Invalid position")
            ensure(value - _position)
            _position = value
        }
    override var limit: Int
        get() = _limit
        set (value) {
            if (value > mbuffer.size)
                throw IllegalArgumentException("Invalid limit")
            _limit = if (value < -1) mbuffer.size else value
        }

    override fun ensureTry(len: Int): Boolean {
        val position = _position
        val limit = _limit
        if (limit >= 0 && len > limit - position) return false
        return true
    }

    override fun bufferSlice(): B {
        @Suppress("UNCHECKED_CAST")
        return mbuffer.slice(_position, _limit - _position) as B
    }
}

class MemoryViewStream<out B : ByteViewE>(
    private val bufferProvider: MemoryViewProvider<B>?,
    private val growth: (Int) -> Int = { it + 8192 },
    mbuffer: B
) : MemoryViewReadableStream<B>(0, -1), MemoryStream {
    private var _mbuffer = mbuffer
    override val mbuffer get() = _mbuffer

    override var position: Int
        get() = _position
        set (value) {
            @Suppress("ConvertTwoComparisonsToRangeCheck") // Very readable
            if (value < 0 || (value > _limit && _limit >= 0))
                throw IllegalArgumentException("Invalid position")
            ensure(value - _position)
            _position = value
        }
    override var limit: Int
        get() = if (_limit < 0) Int.MAX_VALUE else _limit
        set (value) {
            _limit = if (value < -1) -1 else value
        }

    constructor(
        bufferProvider: MemoryViewProvider<B>,
        growth: (Int) -> Int = { (it shl 1).coerceAtLeast(8192) }
    ) : this(bufferProvider, growth, bufferProvider(growth(0)))

    constructor(buffer: B) : this(null, mbuffer = buffer) {
        _limit = buffer.size
    }

    override fun bufferSlice(): B {
        if (_limit >= 0) ensure(_limit - _position)
        @Suppress("UNCHECKED_CAST")
        return mbuffer.slice(
            _position,
            (if (_limit < 0) mbuffer.size else _limit) - _position
        ) as B
    }

    override fun flip() {
        _limit = _position
        _position = 0
    }

    override fun rewind() {
        _position = 0
    }

    fun compact() {
        val limit = _limit
        if (limit < 0) throw IllegalStateException(
            "Cannot compact without limit"
        )
        val end = limit.coerceAtMost(mbuffer.size)
        val remaining = end - _position
        mbuffer.setBytes(0, mbuffer.slice(_position, remaining))
        _position = remaining
        if (limit > end) {
            ensure(limit - end)
            repeat(limit - end) { mbuffer.setByte(_position++, 0) }
        }
        _limit = -1
    }

    override fun reset() {
        _position = 0
        _limit = -1
    }

    override fun put(value: Byte) {
        ensure(1)
        mbuffer.setByte(_position, value)
        _position++
    }

    override fun putShort(value: Short) {
        ensure(2)
        mbuffer.setShort(_position, value)
        _position += 2
    }

    override fun putInt(value: Int) {
        ensure(4)
        mbuffer.setInt(_position, value)
        _position += 4
    }

    override fun putLong(value: Long) {
        ensure(8)
        mbuffer.setLong(_position, value)
        _position += 8
    }

    override fun putFloat(value: Float) {
        ensure(4)
        mbuffer.setFloat(_position, value)
        _position += 4
    }

    override fun putDouble(value: Double) {
        ensure(8)
        mbuffer.setDouble(_position, value)
        _position += 8
    }

    override fun put(buffer: BytesRO) {
        ensure(buffer.size)
        mbuffer.setBytes(_position, buffer)
        _position += buffer.size
    }

    override fun ensureTry(len: Int): Boolean {
        val position = _position
        val limit = _limit
        if (limit >= 0 && len > limit - position) return false
        var size = mbuffer.size
        if (len > size - position) {
            do {
                size = growth(size)
            } while (len > size - position)
            if (limit >= 0) size = size.coerceAtMost(limit)
            if (len > size - position) return false
            grow(size)
        }
        return true
    }

    fun grow(size: Int = growth(mbuffer.size)) {
        val bufferProvider = bufferProvider
                ?: throw BufferOverflowException()
        if (size < mbuffer.size) {
            throw IllegalArgumentException(
                "Tried to shrink buffer with " + mbuffer.size +
                        " bytes to " + size
            )
        }
        val newBuffer = bufferProvider(size)
        assert { newBuffer.size == size }
        newBuffer.setBytes(0, mbuffer)
        _mbuffer = newBuffer
    }
}
