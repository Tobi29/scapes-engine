package org.tobi29.scapes.engine.utils.io

import org.tobi29.scapes.engine.utils.assert

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
        ensurePut(pos - position)
        position = pos
    }

    fun ensurePut(len: Int) {
        val used = position
        val size = mbuffer.size
        if (len <= size - used) {
            return
        }
        throw IOException("End of stream")
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

    override fun get(): Byte {
        ensurePut(1)
        return mbuffer.getByte(position).also { position++ }
    }

    override fun getShort(): Short {
        ensurePut(2)
        return mbuffer.getShort(position).also { position += 2 }
    }

    override fun getInt(): Int {
        ensurePut(4)
        return mbuffer.getInt(position).also { position += 4 }
    }

    override fun getLong(): Long {
        ensurePut(8)
        return mbuffer.getLong(position).also { position += 8 }
    }

    override fun getFloat(): Float {
        ensurePut(4)
        return mbuffer.getFloat(position).also { position += 4 }
    }

    override fun getDouble(): Double {
        ensurePut(8)
        return mbuffer.getDouble(position).also { position += 8 }
    }

    override fun get(buffer: ByteView) = apply {
        ensurePut(buffer.size)
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
        if (limit >= 0) ensurePut(limit - position)
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
            ensurePut(limit - end)
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
        ensurePut(pos - position)
        position = pos
    }

    override fun put(b: Byte) = apply {
        ensurePut(1)
        mbuffer.setByte(position, b)
        position++
    }

    override fun putShort(value: Short) = apply {
        ensurePut(2)
        mbuffer.setShort(position, value)
        position += 2
    }

    override fun putInt(value: Int) = apply {
        ensurePut(4)
        mbuffer.setInt(position, value)
        position += 4
    }

    override fun putLong(value: Long) = apply {
        ensurePut(8)
        mbuffer.setLong(position, value)
        position += 8
    }

    override fun putFloat(value: Float) = apply {
        ensurePut(4)
        mbuffer.setFloat(position, value)
        position += 4
    }

    override fun putDouble(value: Double) = apply {
        ensurePut(8)
        mbuffer.setDouble(position, value)
        position += 8
    }

    override fun put(buffer: ByteViewRO) = apply {
        ensurePut(buffer.size)
        mbuffer.setBytes(position, buffer)
        position += buffer.size
    }

    fun ensurePut(len: Int) {
        val used = position
        var size = mbuffer.size
        if (len <= size - used) {
            return
        }
        do {
            size = growth(size)
        } while (len > size - used)
        if (limit >= 0) size = size.coerceAtMost(limit)
        if (len > size - used) throw IOException("End of stream")
        grow(size)
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

    override fun get(): Byte {
        ensurePut(1)
        return mbuffer.getByte(position).also { position++ }
    }

    override fun getShort(): Short {
        ensurePut(2)
        return mbuffer.getShort(position).also { position += 2 }
    }

    override fun getInt(): Int {
        ensurePut(4)
        return mbuffer.getInt(position).also { position += 4 }
    }

    override fun getLong(): Long {
        ensurePut(8)
        return mbuffer.getLong(position).also { position += 8 }
    }

    override fun getFloat(): Float {
        ensurePut(4)
        return mbuffer.getFloat(position).also { position += 4 }
    }

    override fun getDouble(): Double {
        ensurePut(8)
        return mbuffer.getDouble(position).also { position += 8 }
    }

    override fun get(buffer: ByteView) = apply {
        ensurePut(buffer.size)
        mbuffer.getBytes(position, buffer)
        position += buffer.size
    }
}
