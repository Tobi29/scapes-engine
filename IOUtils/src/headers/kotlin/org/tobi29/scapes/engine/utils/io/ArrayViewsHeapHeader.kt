package org.tobi29.scapes.engine.utils.io

import org.tobi29.scapes.engine.utils.HeapByteArraySlice

header sealed class HeapViewByte(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapView, HeapByteArraySlice {
    override final val array: ByteArray
    override final val size: Int
    override final val offset: Int

    override abstract fun slice(index: Int,
                                size: Int): HeapViewByte
}

header sealed class HeapViewShort(
        shortArray: ShortArray,
        offset: Int,
        size: Int
) : HeapView {
    val shortArray: ShortArray
    override final val offset: Int
    override final val size: Int
}

header sealed class HeapViewChar(
        charArray: CharArray,
        offset: Int,
        size: Int
) : HeapView {
    val charArray: CharArray
    override final val offset: Int
    override final val size: Int
}

header sealed class HeapViewInt(
        intArray: IntArray,
        offset: Int,
        size: Int
) : HeapView {
    val intArray: IntArray
    override final val offset: Int
    override final val size: Int
}

header sealed class HeapViewFloat(
        floatArray: FloatArray,
        offset: Int,
        size: Int
) : HeapView {
    val floatArray: FloatArray
    override final val offset: Int
    override final val size: Int
}

header sealed class HeapViewLong(
        longArray: LongArray,
        offset: Int,
        size: Int
) : HeapView {
    val longArray: LongArray
    override final val offset: Int
    override final val size: Int
}

header sealed class HeapViewDouble(
        doubleArray: DoubleArray,
        offset: Int,
        size: Int
) : HeapView {
    val doubleArray: DoubleArray
    override final val offset: Int
    override final val size: Int
}

header class HeapViewByteBE(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapViewByte, ByteViewBE {
    override fun slice(index: Int,
                       size: Int): HeapViewByteBE

    override fun get(index: Int): Byte
    override fun set(index: Int,
                     value: Byte)

    override fun setBytes(index: Int,
                          slice: ByteViewRO)
}

header class HeapViewByteLE(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapViewByte, ByteViewLE {
    override fun slice(index: Int,
                       size: Int): HeapViewByteLE

    override fun get(index: Int): Byte
    override fun set(index: Int,
                     value: Byte)

    override fun setBytes(index: Int,
                          slice: ByteViewRO)
}

header class HeapViewShortBE(
        shortArray: ShortArray,
        offset: Int,
        size: Int
) : HeapViewShort, ByteViewBE {
    override fun slice(index: Int,
                       size: Int): HeapViewShortBE

    override fun get(index: Int): Byte

    override fun set(index: Int,
                     value: Byte)
}

header class HeapViewShortLE(
        shortArray: ShortArray,
        offset: Int,
        size: Int
) : HeapViewShort, ByteViewLE {
    override fun slice(index: Int,
                       size: Int): HeapViewShortLE

    override fun get(index: Int): Byte

    override fun set(index: Int,
                     value: Byte)
}

header class HeapViewCharBE(
        charArray: CharArray,
        offset: Int,
        size: Int
) : HeapViewChar, ByteViewBE {
    override fun slice(index: Int,
                       size: Int): HeapViewCharBE

    override fun get(index: Int): Byte

    override fun set(index: Int,
                     value: Byte)
}

header class HeapViewCharLE(
        charArray: CharArray,
        offset: Int,
        size: Int
) : HeapViewChar, ByteViewLE {
    override fun slice(index: Int,
                       size: Int): HeapViewCharLE

    override fun get(index: Int): Byte

    override fun set(index: Int,
                     value: Byte)
}

header class HeapViewIntBE(
        intArray: IntArray,
        offset: Int,
        size: Int
) : HeapViewInt, ByteViewBE {
    override fun slice(index: Int,
                       size: Int): HeapViewIntBE

    override fun get(index: Int): Byte

    override fun set(index: Int,
                     value: Byte)
}

header class HeapViewIntLE(
        intArray: IntArray,
        offset: Int,
        size: Int
) : HeapViewInt, ByteViewLE {
    override fun slice(index: Int,
                       size: Int): HeapViewIntLE

    override fun get(index: Int): Byte

    override fun set(index: Int,
                     value: Byte)
}

header class HeapViewFloatBE(
        floatArray: FloatArray,
        offset: Int,
        size: Int
) : HeapViewFloat, ByteViewBE {
    override fun slice(index: Int,
                       size: Int): HeapViewFloatBE

    override fun get(index: Int): Byte

    override fun set(index: Int,
                     value: Byte)
}

header class HeapViewFloatLE(
        floatArray: FloatArray,
        offset: Int,
        size: Int
) : HeapViewFloat, ByteViewLE {
    override fun slice(index: Int,
                       size: Int): HeapViewFloatLE

    override fun get(index: Int): Byte

    override fun set(index: Int,
                     value: Byte)
}

header class HeapViewLongBE(
        longArray: LongArray,
        offset: Int,
        size: Int
) : HeapViewLong, ByteViewBE {
    override fun slice(index: Int,
                       size: Int): HeapViewLongBE

    override fun get(index: Int): Byte

    override fun set(index: Int,
                     value: Byte)
}

header class HeapViewLongLE(
        longArray: LongArray,
        offset: Int,
        size: Int
) : HeapViewLong, ByteViewLE {
    override fun slice(index: Int,
                       size: Int): HeapViewLongLE

    override fun get(index: Int): Byte

    override fun set(index: Int,
                     value: Byte)
}

header class HeapViewDoubleBE(
        doubleArray: DoubleArray,
        offset: Int,
        size: Int
) : HeapViewDouble, ByteViewBE {
    override fun slice(index: Int,
                       size: Int): HeapViewDoubleBE

    override fun get(index: Int): Byte

    override fun set(index: Int,
                     value: Byte)
}

header class HeapViewDoubleLE(
        doubleArray: DoubleArray,
        offset: Int,
        size: Int
) : HeapViewDouble, ByteViewLE {
    override fun slice(index: Int,
                       size: Int): HeapViewDoubleLE

    override fun get(index: Int): Byte

    override fun set(index: Int,
                     value: Byte)
}
