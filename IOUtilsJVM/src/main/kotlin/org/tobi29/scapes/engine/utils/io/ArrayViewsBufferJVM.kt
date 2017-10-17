package org.tobi29.scapes.engine.utils.io

import org.tobi29.scapes.engine.utils.HeapByteArraySlice
import java.nio.ByteBuffer

inline val ByteBuffer.viewE: ByteViewE
    get() = if (order() == BIG_ENDIAN) viewBE else viewLE

inline val ByteBuffer.viewSliceE: ByteViewE
    get() = viewE.slice(position(), remaining())

inline val ByteBuffer.viewBE: ByteViewBE
    get() = if (hasArray()) HeapViewByteBE(array(), arrayOffset(), capacity())
    else ByteBufferViewBE(duplicate().order(BIG_ENDIAN))

inline val ByteBuffer.viewSliceBE: ByteViewBE
    get() = viewBE.slice(position(), remaining())

inline val ByteBuffer.viewLE: ByteViewLE
    get() = if (hasArray()) HeapViewByteLE(array(), arrayOffset(), capacity())
    else ByteBufferViewLE(duplicate().order(LITTLE_ENDIAN))

inline val ByteBuffer.viewSliceLE: ByteViewLE
    get() = viewLE.slice(position(), remaining())

inline val ByteBuffer.viewBufferE: ByteBufferView
    get() = if (order() == BIG_ENDIAN) viewBufferBE else viewBufferLE

inline val ByteBuffer.viewBufferBE: ByteBufferViewBE
    get() = ByteBufferViewBE(duplicate().order(BIG_ENDIAN))

inline val ByteBuffer.viewBufferLE: ByteBufferViewLE
    get() = ByteBufferViewLE(duplicate().order(LITTLE_ENDIAN))

sealed class ByteBufferView(protected val buffer: ByteBuffer) : ByteViewE {
    override final val size = buffer.remaining()
    abstract val byteBuffer: ByteBuffer
}

class ByteBufferViewBE(
        buffer: ByteBuffer
) : ByteBufferView(buffer), ByteViewBE {
    override val byteBuffer: ByteBuffer
        get() = buffer.duplicate().order(
                BIG_ENDIAN)

    override fun slice(index: Int,
                       size: Int) =
            ByteBufferViewBE(
                    buffer.duplicate().apply {
                        position(index)
                        limit(position() + size)
                    }.slice().order(
                            BIG_ENDIAN))

    override fun get(index: Int) = buffer.get(index)
    override fun set(index: Int,
                     value: Byte) {
        buffer.put(index, value)
    }

    override fun getShort(index: Int) = buffer.getShort(index)
    override fun setShort(index: Int,
                          value: Short) {
        buffer.putShort(index, value)
    }

    override fun getChar(index: Int) = buffer.getChar(index)
    override fun setChar(index: Int,
                         value: Char) {
        buffer.putChar(index, value)
    }

    override fun getInt(index: Int) = buffer.getInt(index)
    override fun setInt(index: Int,
                        value: Int) {
        buffer.putInt(index, value)
    }

    override fun getFloat(index: Int) = buffer.getFloat(index)
    override fun setFloat(index: Int,
                          value: Float) {
        buffer.putFloat(index, value)
    }

    override fun getLong(index: Int) = buffer.getLong(index)
    override fun setLong(index: Int,
                         value: Long) {
        buffer.putLong(index, value)
    }

    override fun getDouble(index: Int) = buffer.getDouble(index)
    override fun setDouble(index: Int,
                           value: Double) {
        buffer.putDouble(index, value)
    }

    override fun setBytes(index: Int,
                          slice: ByteViewRO) {
        when (slice) {
            is HeapByteArraySlice -> {
                val position = buffer.position()
                buffer.position(index)
                buffer.put(slice.array, slice.offset, slice.size)
                buffer.position(position)
            }
            is ByteBufferView -> {
                val position = buffer.position()
                buffer.position(index)
                buffer.put(slice.byteBuffer)
                buffer.position(position)
            }
            else -> super<ByteBufferView>.setBytes(index, slice)
        }
    }
}

class ByteBufferViewLE(
        buffer: ByteBuffer
) : ByteBufferView(buffer), ByteViewLE {
    override val byteBuffer: ByteBuffer
        get() = buffer.duplicate().order(
                LITTLE_ENDIAN)

    override fun slice(index: Int,
                       size: Int) =
            ByteBufferViewLE(
                    buffer.duplicate().apply {
                        position(index)
                        limit(position() + size)
                    }.slice().order(
                            LITTLE_ENDIAN))

    override fun get(index: Int) = buffer.get(index)
    override fun set(index: Int,
                     value: Byte) {
        buffer.put(index, value)
    }

    override fun getShort(index: Int) = buffer.getShort(index)
    override fun setShort(index: Int,
                          value: Short) {
        buffer.putShort(index, value)
    }

    override fun getChar(index: Int) = buffer.getChar(index)
    override fun setChar(index: Int,
                         value: Char) {
        buffer.putChar(index, value)
    }

    override fun getInt(index: Int) = buffer.getInt(index)
    override fun setInt(index: Int,
                        value: Int) {
        buffer.putInt(index, value)
    }

    override fun getFloat(index: Int) = buffer.getFloat(index)
    override fun setFloat(index: Int,
                          value: Float) {
        buffer.putFloat(index, value)
    }

    override fun getLong(index: Int) = buffer.getLong(index)
    override fun setLong(index: Int,
                         value: Long) {
        buffer.putLong(index, value)
    }

    override fun getDouble(index: Int) = buffer.getDouble(index)
    override fun setDouble(index: Int,
                           value: Double) {
        buffer.putDouble(index, value)
    }

    override fun setBytes(index: Int,
                          byteView: ByteViewRO) {
        when (byteView) {
            is HeapByteArraySlice -> {
                val position = buffer.position()
                buffer.position(index)
                buffer.put(byteView.array, byteView.offset, byteView.size)
                buffer.position(position)
            }
            is ByteBufferView -> {
                val position = buffer.position()
                buffer.position(index)
                buffer.put(byteView.byteBuffer)
                buffer.position(position)
            }
            else -> super<ByteBufferView>.setBytes(index, byteView)
        }
    }
}
