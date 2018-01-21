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

import org.tobi29.stdex.assert

typealias MemoryViewProvider<B> = (Int) -> B

val DefaultMemoryViewProvider: MemoryViewProvider<HeapViewByteBE> =
        { HeapViewByteBE(ByteArray(it), 0, it) }

interface MemoryStream : RandomWritableByteStream, RandomReadableByteStream {
    override fun position(pos: Int): MemoryStream

    fun flip()
    fun rewind()
    fun reset()
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

class MemoryViewReadableStream<out B : ByteViewERO>(
        private val mbuffer: B
) : RandomReadableByteStream {
    private var position: Int = 0
    private var limit: Int = mbuffer.size

    fun buffer(): B {
        return mbuffer
    }

    fun bufferSlice(): B {
        @Suppress("UNCHECKED_CAST")
        return mbuffer.slice(position, limit - position) as B
    }

    override fun position(): Int {
        return position
    }

    override fun position(pos: Int) = apply {
        if (pos < 0 || pos > limit)
            throw IllegalArgumentException("Invalid position")
        ensure(pos - position)
        position = pos
    }

    fun ensure(len: Int) = ensureTry(len) ?: throw EndOfStreamException()

    fun ensureTry(len: Int): Boolean? {
        val used = position
        val size = mbuffer.size
        if (len <= size - used) {
            return true
        }
        return null
    }

    override fun limit(): Int {
        return limit
    }

    override fun limit(limit: Int) = apply {
        this.limit = if (limit < -1) mbuffer.size else limit
    }

    override fun remaining(): Int {
        return limit - position
    }

    override fun getTry(): Int {
        ensureTry(1) ?: return -1
        return mbuffer.getByte(position).also { position++ }.toInt() and 0xFF
    }

    override fun get(): Byte {
        ensure(1)
        return mbuffer.getByte(position).also { position++ }
    }

    override fun getShort(): Short {
        ensure(2)
        return mbuffer.getShort(position).also { position += 2 }
    }

    override fun getInt(): Int {
        ensure(4)
        return mbuffer.getInt(position).also { position += 4 }
    }

    override fun getLong(): Long {
        ensure(8)
        return mbuffer.getLong(position).also { position += 8 }
    }

    override fun getFloat(): Float {
        ensure(4)
        return mbuffer.getFloat(position).also { position += 4 }
    }

    override fun getDouble(): Double {
        ensure(8)
        return mbuffer.getDouble(position).also { position += 8 }
    }

    override fun get(buffer: ByteView) = apply {
        ensure(buffer.size)
        mbuffer.getBytes(position, buffer)
        position += buffer.size
    }
}

class MemoryViewStream<out B : ByteViewE>(
        private val bufferProvider: MemoryViewProvider<B>?,
        private val growth: (Int) -> Int = { it + 8192 },
        private var mbuffer: B
) : MemoryStream {
    constructor(
            bufferProvider: MemoryViewProvider<B>,
            growth: (Int) -> Int = { (it shl 1).coerceAtLeast(8192) }
    ) : this(bufferProvider, growth, bufferProvider(growth(0)))

    constructor(buffer: B) : this(null, mbuffer = buffer) {
        limit(buffer.size)
    }

    private var position: Int = 0
    private var limit: Int = -1

    fun buffer(): B {
        return mbuffer
    }

    fun bufferSlice(): B {
        if (limit >= 0) ensure(limit - position)
        @Suppress("UNCHECKED_CAST")
        return mbuffer.slice(position,
                (if (limit < 0) mbuffer.size else limit) - position) as B
    }

    override fun flip() {
        limit = position
        position = 0
    }

    override fun rewind() {
        position = 0
    }

    fun compact() {
        val limit = limit
        if (limit < 0) throw IllegalStateException(
                "Cannot compact without limit")
        val end = limit.coerceAtMost(mbuffer.size)
        val remaining = end - position
        mbuffer.setBytes(0, mbuffer.slice(position, remaining))
        position = remaining
        if (limit > end) {
            ensure(limit - end)
            repeat(limit - end) { mbuffer.setByte(position++, 0) }
        }
        this.limit = -1
    }

    override fun reset() {
        position = 0
        limit = -1
    }

    override fun position(): Int {
        return position
    }

    override fun position(pos: Int) = apply {
        if (pos < 0 || (pos > limit && limit >= 0))
            throw IllegalArgumentException("Invalid position")
        ensure(pos - position)
        position = pos
    }

    override fun put(b: Byte) = apply {
        ensure(1)
        mbuffer.setByte(position, b)
        position++
    }

    override fun putShort(value: Short) = apply {
        ensure(2)
        mbuffer.setShort(position, value)
        position += 2
    }

    override fun putInt(value: Int) = apply {
        ensure(4)
        mbuffer.setInt(position, value)
        position += 4
    }

    override fun putLong(value: Long) = apply {
        ensure(8)
        mbuffer.setLong(position, value)
        position += 8
    }

    override fun putFloat(value: Float) = apply {
        ensure(4)
        mbuffer.setFloat(position, value)
        position += 4
    }

    override fun putDouble(value: Double) = apply {
        ensure(8)
        mbuffer.setDouble(position, value)
        position += 8
    }

    override fun put(buffer: ByteViewRO) = apply {
        ensure(buffer.size)
        mbuffer.setBytes(position, buffer)
        position += buffer.size
    }

    fun ensure(len: Int) = ensureTry(len) ?: throw EndOfStreamException()

    fun ensureTry(len: Int): Boolean? {
        val used = position
        var size = mbuffer.size
        if (len <= size - used) return true
        do {
            size = growth(size)
        } while (len > size - used)
        if (limit >= 0) size = size.coerceAtMost(limit)
        if (len > size - used) return null
        grow(size)
        return true
    }

    fun grow(size: Int = growth(mbuffer.size)) {
        val bufferProvider = bufferProvider
                ?: throw BufferOverflowException()
        if (size < mbuffer.size) {
            throw IllegalArgumentException(
                    "Tried to shrink buffer with " + mbuffer.size +
                            " bytes to " + size)
        }
        val newBuffer = bufferProvider(size)
        assert { newBuffer.size == size }
        newBuffer.setBytes(0, mbuffer)
        mbuffer = newBuffer
    }

    override fun limit(): Int {
        return limit
    }

    override fun limit(limit: Int) = apply {
        this.limit = if (limit < -1) -1 else limit
    }

    override fun remaining(): Int {
        return (if (limit < 0) Int.MAX_VALUE else limit) - position
    }

    override fun getTry(): Int {
        ensureTry(1) ?: return -1
        return mbuffer.getByte(position).also { position++ }.toInt() and 0xFF
    }

    override fun get(): Byte {
        ensure(1)
        return mbuffer.getByte(position).also { position++ }
    }

    override fun getShort(): Short {
        ensure(2)
        return mbuffer.getShort(position).also { position += 2 }
    }

    override fun getInt(): Int {
        ensure(4)
        return mbuffer.getInt(position).also { position += 4 }
    }

    override fun getLong(): Long {
        ensure(8)
        return mbuffer.getLong(position).also { position += 8 }
    }

    override fun getFloat(): Float {
        ensure(4)
        return mbuffer.getFloat(position).also { position += 4 }
    }

    override fun getDouble(): Double {
        ensure(8)
        return mbuffer.getDouble(position).also { position += 8 }
    }

    override fun get(buffer: ByteView) = apply {
        ensure(buffer.size)
        mbuffer.getBytes(position, buffer)
        position += buffer.size
    }
}
