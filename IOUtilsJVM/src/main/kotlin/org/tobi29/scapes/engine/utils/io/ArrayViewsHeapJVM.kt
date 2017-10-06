package org.tobi29.scapes.engine.utils.io

import org.tobi29.scapes.engine.utils.bits
import org.tobi29.scapes.engine.utils.bitsToDouble
import org.tobi29.scapes.engine.utils.bitsToFloat
import org.tobi29.scapes.engine.utils.copy

impl sealed class HeapViewByte impl constructor(
        impl override final val byteArray: ByteArray,
        impl override final val offset: Int,
        impl override final val size: Int
) : HeapView, ArrayByteView {
    init {
        if (offset < 0 || size < 0 || offset + size > byteArray.size shl 0)
            throw IndexOutOfBoundsException("Invalid offset or size")
    }
}

impl sealed class HeapViewShort impl constructor(
        impl val shortArray: ShortArray,
        impl override final val offset: Int,
        impl override final val size: Int
) : HeapView {
    init {
        if (offset < 0 || size < 0 || offset + size > shortArray.size shl 1)
            throw IndexOutOfBoundsException("Invalid offset or size")
    }

    override fun getShorts(index: Int,
                           shortView: ShortView) {
        if (shortView !is HeapViewShort
                || ((this.offset + index) and 1) != (shortView.offset and 1))
            return super.getShorts(index, shortView)

        if (index < 0 || index + shortView.size > this.size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        val padFront = (2 - (this.offset + index)) and 1
        val padBack = (this.offset + index + shortView.size) and 1
        var j = index
        for (i in 0 until padFront) {
            shortView.setByte(j++, getByte(i))
        }
        copy(shortArray, shortView.shortArray,
                (shortView.size - padFront - padBack) shr 1,
                (index + this.offset + padFront) shr 1,
                (shortView.offset + padFront) shr 1)
        j = index + shortView.size - padBack
        for (i in shortView.size - padBack until shortView.size) {
            shortView.setByte(j++, getByte(i))
        }
    }
}

impl sealed class HeapViewChar impl constructor(
        impl val charArray: CharArray,
        impl override final val offset: Int,
        impl override final val size: Int
) : HeapView {
    init {
        if (offset < 0 || size < 0 || offset + size > charArray.size shl 1)
            throw IndexOutOfBoundsException("Invalid offset or size")
    }

    override fun getChars(index: Int,
                          charView: CharView) {
        if (charView !is HeapViewChar
                || ((this.offset + index) and 1) != (charView.offset and 1))
            return super.getChars(index, charView)

        if (index < 0 || index + charView.size > this.size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        val padFront = (2 - (this.offset + index)) and 1
        val padBack = (this.offset + index + charView.size) and 1
        var j = index
        for (i in 0 until padFront) {
            charView.setByte(j++, getByte(i))
        }
        copy(charArray, charView.charArray,
                (charView.size - padFront - padBack) shr 1,
                (index + this.offset + padFront) shr 1,
                (charView.offset + padFront) shr 1)
        j = index + charView.size - padBack
        for (i in charView.size - padBack until charView.size) {
            charView.setByte(j++, getByte(i))
        }
    }
}

impl sealed class HeapViewInt impl constructor(
        impl val intArray: IntArray,
        impl override final val offset: Int,
        impl override final val size: Int
) : HeapView {
    init {
        if (offset < 0 || size < 0 || offset + size > intArray.size shl 2)
            throw IndexOutOfBoundsException("Invalid offset or size")
    }

    override fun getInts(index: Int,
                         intView: IntView) {
        if (intView !is HeapViewInt
                || ((this.offset + index) and 3) != (intView.offset and 3))
            return super.getInts(index, intView)

        if (index < 0 || index + intView.size > this.size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        val padFront = (4 - (this.offset + index)) and 3
        val padBack = (this.offset + index + intView.size) and 3
        var j = index
        for (i in 0 until padFront) {
            intView.setByte(j++, getByte(i))
        }
        copy(intArray, intView.intArray,
                (intView.size - padFront - padBack) shr 2,
                (index + this.offset + padFront) shr 2,
                (intView.offset + padFront) shr 2)
        j = index + intView.size - padBack
        for (i in intView.size - padBack until intView.size) {
            intView.setByte(j++, getByte(i))
        }
    }
}

impl sealed class HeapViewFloat impl constructor(
        impl val floatArray: FloatArray,
        impl override final val offset: Int,
        impl override final val size: Int
) : HeapView {
    init {
        if (offset < 0 || size < 0 || offset + size > floatArray.size shl 2)
            throw IndexOutOfBoundsException("Invalid offset or size")
    }

    override fun getFloats(index: Int,
                           floatView: FloatView) {
        if (floatView !is HeapViewFloat
                || ((this.offset + index) and 3) != (floatView.offset and 3))
            return super.getFloats(index, floatView)

        if (index < 0 || index + floatView.size > this.size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        val padFront = (4 - (this.offset + index)) and 3
        val padBack = (this.offset + index + floatView.size) and 3
        var j = index
        for (i in 0 until padFront) {
            floatView.setByte(j++, getByte(i))
        }
        copy(floatArray, floatView.floatArray,
                (floatView.size - padFront - padBack) shr 2,
                (index + this.offset + padFront) shr 2,
                (floatView.offset + padFront) shr 2)
        j = index + floatView.size - padBack
        for (i in floatView.size - padBack until floatView.size) {
            floatView.setByte(j++, getByte(i))
        }
    }
}

impl sealed class HeapViewLong impl constructor(
        impl val longArray: LongArray,
        impl override final val offset: Int,
        impl override final val size: Int
) : HeapView {
    init {
        if (offset < 0 || size < 0 || offset + size > longArray.size shl 3)
            throw IndexOutOfBoundsException("Invalid offset or size")
    }

    override fun getLongs(index: Int,
                          longView: LongView) {
        if (longView !is HeapViewLong
                || ((this.offset + index) and 7) != (longView.offset and 7))
            return super.getLongs(index, longView)

        if (index < 0 || index + longView.size > this.size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        val padFront = (48 - (this.offset + index)) and 7
        val padBack = (this.offset + index + longView.size) and 7
        var j = index
        for (i in 0 until padFront) {
            longView.setByte(j++, getByte(i))
        }
        copy(longArray, longView.longArray,
                (longView.size - padFront - padBack) shr 3,
                (index + this.offset + padFront) shr 3,
                (longView.offset + padFront) shr 3)
        j = index + longView.size - padBack
        for (i in longView.size - padBack until longView.size) {
            longView.setByte(j++, getByte(i))
        }
    }
}

impl sealed class HeapViewDouble impl constructor(
        impl val doubleArray: DoubleArray,
        impl override final val offset: Int,
        impl override final val size: Int
) : HeapView {
    init {
        if (offset < 0 || size < 0 || offset + size > doubleArray.size shl 3)
            throw IndexOutOfBoundsException("Invalid offset or size")
    }

    override fun getDoubles(index: Int,
                            doubleView: DoubleView) {
        if (doubleView !is HeapViewDouble
                || ((this.offset + index) and 7) != (doubleView.offset and 7))
            return super.getDoubles(index, doubleView)

        if (index < 0 || index + doubleView.size > this.size)
            throw IndexOutOfBoundsException("Invalid index or view too double")

        val padFront = (48 - (this.offset + index)) and 7
        val padBack = (this.offset + index + doubleView.size) and 7
        var j = index
        for (i in 0 until padFront) {
            doubleView.setByte(j++, getByte(i))
        }
        copy(doubleArray, doubleView.doubleArray,
                (doubleView.size - padFront - padBack) shr 3,
                (index + this.offset + padFront) shr 3,
                (doubleView.offset + padFront) shr 3)
        j = index + doubleView.size - padBack
        for (i in doubleView.size - padBack until doubleView.size) {
            doubleView.setByte(j++, getByte(i))
        }
    }
}

impl class HeapViewByteBE impl constructor(
        byteArray: ByteArray,
        offset: Int,
        size: Int
) : HeapViewByte(byteArray, offset, size), ByteViewBE {
    impl override fun slice(index: Int,
                            size: Int): HeapViewByteBE =
            prepareSlice(byteArray, index, size, ::HeapViewByteBE)

    impl override fun getByte(index: Int): Byte =
            byteArray[(this as HeapView).index(index)]

    impl override fun setByte(index: Int,
                              value: Byte) =
            byteArray.set((this as HeapView).index(index), value)

    impl override fun setBytes(index: Int,
                               byteView: ByteViewRO) =
            super<HeapViewByte>.setBytes(index, byteView)
}

impl class HeapViewByteLE impl constructor(
        byteArray: ByteArray,
        offset: Int,
        size: Int
) : HeapViewByte(byteArray, offset, size), ByteViewLE {
    impl override fun slice(index: Int,
                            size: Int): HeapViewByteLE =
            prepareSlice(byteArray, index, size, ::HeapViewByteLE)

    impl override fun getByte(index: Int): Byte =
            byteArray[(this as HeapView).index(index)]

    impl override fun setByte(index: Int,
                              value: Byte) =
            byteArray.set((this as HeapView).index(index), value)

    impl override fun setBytes(index: Int,
                               byteView: ByteViewRO) =
            super<HeapViewByte>.setBytes(index, byteView)
}

impl class HeapViewShortBE impl constructor(
        shortArray: ShortArray,
        offset: Int,
        size: Int
) : HeapViewShort(shortArray, offset, size), ByteViewBE {
    impl override fun slice(index: Int,
                            size: Int): HeapViewShortBE =
            prepareSlice(shortArray, index, size, ::HeapViewShortBE)

    impl override fun getByte(index: Int): Byte {
        val offsetIndex = index(index)
        val arrayValue = shortArray[offsetIndex shr 1].toInt()
        return when (offsetIndex and 1) {
            0 -> arrayValue ushr 8
            1 -> arrayValue ushr 0
            else -> throw IllegalStateException("Maths broke")
        }.toByte()
    }

    impl override fun setByte(index: Int,
                              value: Byte) {
        val offsetIndex = index(index)
        val arrayIndex = offsetIndex shr 1
        val arrayValue = shortArray[arrayIndex].toInt()
        val setValue = value.toInt() and 0xFF
        shortArray[arrayIndex] = when (offsetIndex and 1) {
            0 -> (setValue shl 8) or (arrayValue and 0x00FF)
            1 -> (setValue shl 0) or (arrayValue and 0xFF00)
            else -> throw IllegalStateException("Maths broke")
        }.toShort()
    }

    override fun getShort(index: Int): Short =
            index(index, 2).let { offsetIndex ->
                if (offsetIndex and 1 == 0) shortArray[offsetIndex shr 1]
                else super.getShort(index)
            }

    override fun setShort(index: Int,
                          value: Short) =
            index(index, 2).let { offsetIndex ->
                if (offsetIndex and 1 == 0) shortArray.set(offsetIndex shr 1,
                        value)
                else super.setShort(index, value)
            }
}

impl class HeapViewShortLE impl constructor(
        shortArray: ShortArray,
        offset: Int,
        size: Int
) : HeapViewShort(shortArray, offset, size), ByteViewLE {
    impl override fun slice(index: Int,
                            size: Int): HeapViewShortLE =
            prepareSlice(shortArray, index, size, ::HeapViewShortLE)

    impl override fun getByte(index: Int): Byte {
        val offsetIndex = index(index)
        val arrayValue = shortArray[offsetIndex shr 1].toInt()
        return when (offsetIndex and 1) {
            0 -> arrayValue ushr 0
            1 -> arrayValue ushr 8
            else -> throw IllegalStateException("Maths broke")
        }.toByte()
    }

    impl override fun setByte(index: Int,
                              value: Byte) {
        val offsetIndex = index(index)
        val arrayIndex = offsetIndex shr 1
        val arrayValue = shortArray[arrayIndex].toInt()
        val setValue = value.toInt() and 0xFF
        shortArray[arrayIndex] = when (offsetIndex and 1) {
            0 -> (setValue shl 0) or (arrayValue and 0xFF00)
            1 -> (setValue shl 8) or (arrayValue and 0x00FF)
            else -> throw IllegalStateException("Maths broke")
        }.toShort()
    }

    override fun getShort(index: Int): Short =
            index(index, 2).let { offsetIndex ->
                if (offsetIndex and 1 == 0) shortArray[offsetIndex shr 1]
                else super.getShort(index)
            }

    override fun setShort(index: Int,
                          value: Short) =
            index(index, 2).let { offsetIndex ->
                if (offsetIndex and 1 == 0) shortArray.set(offsetIndex shr 1,
                        value)
                else super.setShort(index, value)
            }
}

impl class HeapViewCharBE impl constructor(
        charArray: CharArray,
        offset: Int,
        size: Int
) : HeapViewChar(charArray, offset, size), ByteViewBE {
    impl override fun slice(index: Int,
                            size: Int): HeapViewCharBE =
            prepareSlice(charArray, index, size, ::HeapViewCharBE)

    impl override fun getByte(index: Int): Byte {
        val offsetIndex = index(index)
        val arrayValue = charArray[offsetIndex shr 1].toInt()
        return when (offsetIndex and 1) {
            0 -> arrayValue ushr 8
            1 -> arrayValue ushr 0
            else -> throw IllegalStateException("Maths broke")
        }.toByte()
    }

    impl override fun setByte(index: Int,
                              value: Byte) {
        val offsetIndex = index(index)
        val arrayIndex = offsetIndex shr 1
        val arrayValue = charArray[arrayIndex].toInt()
        val setValue = value.toInt() and 0xFF
        charArray[arrayIndex] = when (offsetIndex and 1) {
            0 -> (setValue shl 8) or (arrayValue and 0x00FF)
            1 -> (setValue shl 0) or (arrayValue and 0xFF00)
            else -> throw IllegalStateException("Maths broke")
        }.toChar()
    }

    override fun getShort(index: Int): Short = getChar(index).toShort()
    override fun setShort(index: Int,
                          value: Short) = setChar(index, value.toChar())

    override fun getChar(index: Int): Char =
            index(index, 2).let { offsetIndex ->
                if (offsetIndex and 1 == 0) charArray[offsetIndex shr 1]
                else super.getShort(index).toChar()
            }

    override fun setChar(index: Int,
                         value: Char) =
            index(index, 2).let { offsetIndex ->
                if (offsetIndex and 1 == 0) charArray.set(offsetIndex shr 1,
                        value)
                else super.setShort(index, value.toShort())
            }
}

impl class HeapViewCharLE impl constructor(
        charArray: CharArray,
        offset: Int,
        size: Int
) : HeapViewChar(charArray, offset, size), ByteViewLE {
    impl override fun slice(index: Int,
                            size: Int): HeapViewCharLE =
            prepareSlice(charArray, index, size, ::HeapViewCharLE)

    impl override fun getByte(index: Int): Byte {
        val offsetIndex = index(index)
        val arrayValue = charArray[offsetIndex shr 1].toInt()
        return when (offsetIndex and 1) {
            0 -> arrayValue ushr 0
            1 -> arrayValue ushr 8
            else -> throw IllegalStateException("Maths broke")
        }.toByte()
    }

    impl override fun setByte(index: Int,
                              value: Byte) {
        val offsetIndex = index(index)
        val arrayIndex = offsetIndex shr 1
        val arrayValue = charArray[arrayIndex].toInt()
        val setValue = value.toInt() and 0xFF
        charArray[arrayIndex] = when (offsetIndex and 1) {
            0 -> (setValue shl 0) or (arrayValue and 0xFF00)
            1 -> (setValue shl 8) or (arrayValue and 0x00FF)
            else -> throw IllegalStateException("Maths broke")
        }.toChar()
    }

    override fun getShort(index: Int): Short = getChar(index).toShort()
    override fun setShort(index: Int,
                          value: Short) = setChar(index, value.toChar())

    override fun getChar(index: Int): Char =
            index(index, 2).let { offsetIndex ->
                if (offsetIndex and 1 == 0) charArray[offsetIndex shr 1]
                else super.getShort(index).toChar()
            }

    override fun setChar(index: Int,
                         value: Char) =
            index(index, 2).let { offsetIndex ->
                if (offsetIndex and 1 == 0) charArray.set(offsetIndex shr 1,
                        value)
                else super.setShort(index, value.toShort())
            }
}

impl class HeapViewIntBE impl constructor(
        intArray: IntArray,
        offset: Int,
        size: Int
) : HeapViewInt(intArray, offset, size), ByteViewBE {
    impl override fun slice(index: Int,
                            size: Int): HeapViewIntBE =
            prepareSlice(intArray, index, size, ::HeapViewIntBE)

    impl override fun getByte(index: Int): Byte {
        val offsetIndex = index(index)
        val arrayValue = intArray[offsetIndex shr 2]
        return when (offsetIndex and 3) {
            0 -> arrayValue ushr 24
            1 -> arrayValue ushr 16
            2 -> arrayValue ushr 8
            3 -> arrayValue ushr 0
            else -> throw IllegalStateException("Maths broke")
        }.toByte()
    }

    impl override fun setByte(index: Int,
                              value: Byte) {
        val offsetIndex = index(index)
        val arrayIndex = offsetIndex shr 2
        val arrayValue = intArray[arrayIndex]
        val setValue = value.toInt() and 0xFF
        intArray[arrayIndex] = when (offsetIndex and 3) {
            0 -> (setValue shl 24) or (arrayValue and 0x00FFFFFF.toInt())
            1 -> (setValue shl 16) or (arrayValue and 0xFF00FFFF.toInt())
            2 -> (setValue shl 8) or (arrayValue and 0xFFFF00FF.toInt())
            3 -> (setValue shl 0) or (arrayValue and 0xFFFFFF00.toInt())
            else -> throw IllegalStateException("Maths broke")
        }
    }

    override fun getInt(index: Int): Int =
            index(index, 4).let { offsetIndex ->
                if (offsetIndex and 3 == 0) intArray[offsetIndex shr 2]
                else super.getInt(index)
            }

    override fun setInt(index: Int,
                        value: Int) =
            index(index, 4).let { offsetIndex ->
                if (offsetIndex and 3 == 0) intArray.set(offsetIndex shr 2,
                        value)
                else super.setInt(index, value)
            }
}

impl class HeapViewIntLE impl constructor(
        intArray: IntArray,
        offset: Int,
        size: Int
) : HeapViewInt(intArray, offset, size), ByteViewLE {
    impl override fun slice(index: Int,
                            size: Int): HeapViewIntLE =
            prepareSlice(intArray, index, size, ::HeapViewIntLE)

    impl override fun getByte(index: Int): Byte {
        val offsetIndex = index(index)
        val arrayValue = intArray[offsetIndex shr 2]
        return when (offsetIndex and 3) {
            0 -> arrayValue ushr 0
            1 -> arrayValue ushr 8
            2 -> arrayValue ushr 16
            3 -> arrayValue ushr 24
            else -> throw IllegalStateException("Maths broke")
        }.toByte()
    }

    impl override fun setByte(index: Int,
                              value: Byte) {
        val offsetIndex = index(index)
        val arrayIndex = offsetIndex shr 2
        val arrayValue = intArray[arrayIndex]
        val setValue = value.toInt() and 0xFF
        intArray[arrayIndex] = when (offsetIndex and 3) {
            0 -> (setValue shl 0) or (arrayValue and 0xFFFFFF00.toInt())
            1 -> (setValue shl 8) or (arrayValue and 0xFFFF00FF.toInt())
            2 -> (setValue shl 16) or (arrayValue and 0xFF00FFFF.toInt())
            3 -> (setValue shl 24) or (arrayValue and 0x00FFFFFF.toInt())
            else -> throw IllegalStateException("Maths broke")
        }
    }

    override fun getInt(index: Int): Int =
            index(index, 4).let { offsetIndex ->
                if (offsetIndex and 3 == 0) intArray[offsetIndex shr 2]
                else super.getInt(index)
            }

    override fun setInt(index: Int,
                        value: Int) =
            index(index, 4).let { offsetIndex ->
                if (offsetIndex and 3 == 0) intArray.set(offsetIndex shr 2,
                        value)
                else super.setInt(index, value)
            }
}

impl class HeapViewFloatBE impl constructor(
        floatArray: FloatArray,
        offset: Int,
        size: Int
) : HeapViewFloat(floatArray, offset, size), ByteViewBE {
    impl override fun slice(index: Int,
                            size: Int): HeapViewFloatBE =
            prepareSlice(floatArray, index, size, ::HeapViewFloatBE)

    impl override fun getByte(index: Int): Byte {
        val offsetIndex = index(index)
        val arrayValue = floatArray[offsetIndex shr 2].bits()
        return when (offsetIndex and 3) {
            0 -> arrayValue ushr 24
            1 -> arrayValue ushr 16
            2 -> arrayValue ushr 8
            3 -> arrayValue ushr 0
            else -> throw IllegalStateException("Maths broke")
        }.toByte()
    }

    impl override fun setByte(index: Int,
                              value: Byte) {
        val offsetIndex = index(index)
        val arrayIndex = offsetIndex shr 2
        val arrayValue = floatArray[arrayIndex].bits()
        val setValue = value.toInt() and 0xFF
        floatArray[arrayIndex] = when (offsetIndex and 3) {
            0 -> (setValue shl 24) or (arrayValue and 0x00FFFFFF.toInt())
            1 -> (setValue shl 16) or (arrayValue and 0xFF00FFFF.toInt())
            2 -> (setValue shl 8) or (arrayValue and 0xFFFF00FF.toInt())
            3 -> (setValue shl 0) or (arrayValue and 0xFFFFFF00.toInt())
            else -> throw IllegalStateException("Maths broke")
        }.bitsToFloat()
    }

    override fun getInt(index: Int): Int = getFloat(index).bits()
    override fun setInt(index: Int,
                        value: Int) = setFloat(index, value.bitsToFloat())

    override fun getFloat(index: Int): Float =
            index(index, 4).let { offsetIndex ->
                if (offsetIndex and 3 == 0) floatArray[offsetIndex shr 2]
                else super.getInt(index).bitsToFloat()
            }

    override fun setFloat(index: Int,
                          value: Float) =
            index(index, 4).let { offsetIndex ->
                if (offsetIndex and 3 == 0) floatArray.set(offsetIndex shr 2,
                        value)
                else super.setInt(index, value.bits())
            }
}

impl class HeapViewFloatLE impl constructor(
        floatArray: FloatArray,
        offset: Int,
        size: Int
) : HeapViewFloat(floatArray, offset, size), ByteViewLE {
    impl override fun slice(index: Int,
                            size: Int): HeapViewFloatLE =
            prepareSlice(floatArray, index, size, ::HeapViewFloatLE)

    impl override fun getByte(index: Int): Byte {
        val offsetIndex = index(index)
        val arrayValue = floatArray[offsetIndex shr 2].bits()
        return when (offsetIndex and 3) {
            0 -> arrayValue ushr 0
            1 -> arrayValue ushr 8
            2 -> arrayValue ushr 16
            3 -> arrayValue ushr 24
            else -> throw IllegalStateException("Maths broke")
        }.toByte()
    }

    impl override fun setByte(index: Int,
                              value: Byte) {
        val offsetIndex = index(index)
        val arrayIndex = offsetIndex shr 2
        val arrayValue = floatArray[arrayIndex].bits()
        val setValue = value.toInt() and 0xFF
        floatArray[arrayIndex] = when (offsetIndex and 3) {
            0 -> (setValue shl 0) or (arrayValue and 0xFFFFFF00.toInt())
            1 -> (setValue shl 8) or (arrayValue and 0xFFFF00FF.toInt())
            2 -> (setValue shl 16) or (arrayValue and 0xFF00FFFF.toInt())
            3 -> (setValue shl 24) or (arrayValue and 0x00FFFFFF.toInt())
            else -> throw IllegalStateException("Maths broke")
        }.bitsToFloat()
    }

    override fun getInt(index: Int): Int = getFloat(index).bits()
    override fun setInt(index: Int,
                        value: Int) = setFloat(index, value.bitsToFloat())

    override fun getFloat(index: Int): Float =
            index(index, 4).let { offsetIndex ->
                if (offsetIndex and 3 == 0) floatArray[offsetIndex shr 2]
                else super.getInt(index).bitsToFloat()
            }

    override fun setFloat(index: Int,
                          value: Float) =
            index(index, 4).let { offsetIndex ->
                if (offsetIndex and 3 == 0) floatArray.set(offsetIndex shr 2,
                        value)
                else super.setInt(index, value.bits())
            }
}

impl class HeapViewLongBE impl constructor(
        longArray: LongArray,
        offset: Int,
        size: Int
) : HeapViewLong(longArray, offset, size), ByteViewBE {
    impl override fun slice(index: Int,
                            size: Int): HeapViewLongBE =
            prepareSlice(longArray, index, size, ::HeapViewLongBE)

    impl override fun getByte(index: Int): Byte {
        val offsetIndex = index(index)
        val arrayValue = longArray[offsetIndex shr 3]
        return when (offsetIndex and 7) {
            0 -> arrayValue ushr 56
            1 -> arrayValue ushr 48
            2 -> arrayValue ushr 40
            3 -> arrayValue ushr 32
            4 -> arrayValue ushr 24
            5 -> arrayValue ushr 16
            6 -> arrayValue ushr 8
            7 -> arrayValue ushr 0
            else -> throw IllegalStateException("Maths broke")
        }.toByte()
    }

    impl override fun setByte(index: Int,
                              value: Byte) {
        val offsetIndex = index(index)
        val arrayIndex = offsetIndex shr 3
        val arrayValue = longArray[arrayIndex]
        val setValue = value.toLong() and 0xFF
        longArray[arrayIndex] = when (offsetIndex and 7) {
            0 -> (setValue shl 56) or (arrayValue and 0x00FFFFFFFFFFFFFF)
            1 -> (setValue shl 48) or (arrayValue and -0xFF000000000001)
            2 -> (setValue shl 40) or (arrayValue and -0x00FF0000000001)
            3 -> (setValue shl 32) or (arrayValue and -0x0000FF00000001)
            4 -> (setValue shl 24) or (arrayValue and -0x000000FF000001)
            5 -> (setValue shl 16) or (arrayValue and -0x00000000FF0001)
            6 -> (setValue shl 8) or (arrayValue and -0x0000000000FF01)
            7 -> (setValue shl 0) or (arrayValue and -0x000000000100)
            else -> throw IllegalStateException("Maths broke")
        }.toLong()
    }

    override fun getLong(index: Int): Long =
            index(index, 8).let { offsetIndex ->
                if (offsetIndex and 7 == 0) longArray[offsetIndex shr 3]
                else super.getLong(index)
            }

    override fun setLong(index: Int,
                         value: Long) =
            index(index, 8).let { offsetIndex ->
                if (offsetIndex and 7 == 0) longArray.set(offsetIndex shr 3,
                        value)
                else super.setLong(index, value)
            }
}

impl class HeapViewLongLE impl constructor(
        longArray: LongArray,
        offset: Int,
        size: Int
) : HeapViewLong(longArray, offset, size), ByteViewLE {
    impl override fun slice(index: Int,
                            size: Int): HeapViewLongLE =
            prepareSlice(longArray, index, size, ::HeapViewLongLE)

    impl override fun getByte(index: Int): Byte {
        val offsetIndex = index(index)
        val arrayValue = longArray[offsetIndex shr 3]
        return when (offsetIndex and 7) {
            0 -> arrayValue ushr 0
            1 -> arrayValue ushr 8
            2 -> arrayValue ushr 16
            3 -> arrayValue ushr 24
            4 -> arrayValue ushr 32
            5 -> arrayValue ushr 40
            6 -> arrayValue ushr 48
            7 -> arrayValue ushr 56
            else -> throw IllegalStateException("Maths broke")
        }.toByte()
    }

    impl override fun setByte(index: Int,
                              value: Byte) {
        val offsetIndex = index(index)
        val arrayIndex = offsetIndex shr 3
        val arrayValue = longArray[arrayIndex]
        val setValue = value.toLong() and 0xFF
        longArray[arrayIndex] = when (offsetIndex and 7) {
            0 -> (setValue shl 0) or (arrayValue and -0x000000000100)
            1 -> (setValue shl 8) or (arrayValue and -0x0000000000FF01)
            2 -> (setValue shl 16) or (arrayValue and -0x00000000FF0001)
            3 -> (setValue shl 24) or (arrayValue and -0x000000FF000001)
            4 -> (setValue shl 32) or (arrayValue and -0x0000FF00000001)
            5 -> (setValue shl 40) or (arrayValue and -0x00FF0000000001)
            6 -> (setValue shl 48) or (arrayValue and -0xFF000000000001)
            7 -> (setValue shl 56) or (arrayValue and 0x00FFFFFFFFFFFFFF)
            else -> throw IllegalStateException("Maths broke")
        }.toLong()
    }

    override fun getLong(index: Int): Long =
            index(index, 8).let { offsetIndex ->
                if (offsetIndex and 7 == 0) longArray[offsetIndex shr 3]
                else super.getLong(index)
            }

    override fun setLong(index: Int,
                         value: Long) =
            index(index, 8).let { offsetIndex ->
                if (offsetIndex and 7 == 0) longArray.set(offsetIndex shr 3,
                        value)
                else super.setLong(index, value)
            }
}

impl class HeapViewDoubleBE impl constructor(
        doubleArray: DoubleArray,
        offset: Int,
        size: Int
) : HeapViewDouble(doubleArray, offset, size), ByteViewBE {
    impl override fun slice(index: Int,
                            size: Int): HeapViewDoubleBE =
            prepareSlice(doubleArray, index, size, ::HeapViewDoubleBE)

    impl override fun getByte(index: Int): Byte {
        val offsetIndex = index(index)
        val arrayValue = doubleArray[offsetIndex shr 3].bits()
        return when (offsetIndex and 7) {
            0 -> arrayValue ushr 56
            1 -> arrayValue ushr 48
            2 -> arrayValue ushr 40
            3 -> arrayValue ushr 32
            4 -> arrayValue ushr 24
            5 -> arrayValue ushr 16
            6 -> arrayValue ushr 8
            7 -> arrayValue ushr 0
            else -> throw IllegalStateException("Maths broke")
        }.toByte()
    }

    impl override fun setByte(index: Int,
                              value: Byte) {
        val offsetIndex = index(index)
        val arrayIndex = offsetIndex shr 3
        val arrayValue = doubleArray[arrayIndex].bits()
        val setValue = value.toLong() and 0xFF
        doubleArray[arrayIndex] = when (offsetIndex and 7) {
            0 -> (setValue shl 56) or (arrayValue and 0x00FFFFFFFFFFFFFF)
            1 -> (setValue shl 48) or (arrayValue and -0xFF000000000001)
            2 -> (setValue shl 40) or (arrayValue and -0x00FF0000000001)
            3 -> (setValue shl 32) or (arrayValue and -0x0000FF00000001)
            4 -> (setValue shl 24) or (arrayValue and -0x000000FF000001)
            5 -> (setValue shl 16) or (arrayValue and -0x00000000FF0001)
            6 -> (setValue shl 8) or (arrayValue and -0x0000000000FF01)
            7 -> (setValue shl 0) or (arrayValue and -0x000000000100)
            else -> throw IllegalStateException("Maths broke")
        }.bitsToDouble()
    }

    override fun getLong(index: Int): Long = getDouble(index).bits()
    override fun setLong(index: Int,
                         value: Long) = setDouble(index, value.bitsToDouble())

    override fun getDouble(index: Int): Double =
            index(index, 8).let { offsetIndex ->
                if (offsetIndex and 7 == 0) doubleArray[offsetIndex shr 3]
                else super.getLong(index).bitsToDouble()
            }

    override fun setDouble(index: Int,
                           value: Double) =
            index(index, 8).let { offsetIndex ->
                if (offsetIndex and 7 == 0) doubleArray.set(offsetIndex shr 3,
                        value)
                else super.setLong(index, value.bits())
            }
}

impl class HeapViewDoubleLE impl constructor(
        doubleArray: DoubleArray,
        offset: Int,
        size: Int
) : HeapViewDouble(doubleArray, offset, size), ByteViewLE {
    impl override fun slice(index: Int,
                            size: Int): HeapViewDoubleLE =
            prepareSlice(doubleArray, index, size, ::HeapViewDoubleLE)

    impl override fun getByte(index: Int): Byte {
        val offsetIndex = index(index)
        val arrayValue = doubleArray[offsetIndex shr 3].bits()
        return when (offsetIndex and 7) {
            0 -> arrayValue ushr 0
            1 -> arrayValue ushr 8
            2 -> arrayValue ushr 16
            3 -> arrayValue ushr 24
            4 -> arrayValue ushr 32
            5 -> arrayValue ushr 40
            6 -> arrayValue ushr 48
            7 -> arrayValue ushr 56
            else -> throw IllegalStateException("Maths broke")
        }.toByte()
    }

    impl override fun setByte(index: Int,
                              value: Byte) {
        val offsetIndex = index(index)
        val arrayIndex = offsetIndex shr 3
        val arrayValue = doubleArray[arrayIndex].bits()
        val setValue = value.toLong() and 0xFF
        doubleArray[arrayIndex] = when (offsetIndex and 7) {
            0 -> (setValue shl 0) or (arrayValue and -0x000000000100)
            1 -> (setValue shl 8) or (arrayValue and -0x0000000000FF01)
            2 -> (setValue shl 16) or (arrayValue and -0x00000000FF0001)
            3 -> (setValue shl 24) or (arrayValue and -0x000000FF000001)
            4 -> (setValue shl 32) or (arrayValue and -0x0000FF00000001)
            5 -> (setValue shl 40) or (arrayValue and -0x00FF0000000001)
            6 -> (setValue shl 48) or (arrayValue and -0xFF000000000001)
            7 -> (setValue shl 56) or (arrayValue and 0x00FFFFFFFFFFFFFF)
            else -> throw IllegalStateException("Maths broke")
        }.bitsToDouble()
    }

    override fun getLong(index: Int): Long = getDouble(index).bits()
    override fun setLong(index: Int,
                         value: Long) = setDouble(index, value.bitsToDouble())

    override fun getDouble(index: Int): Double =
            index(index, 8).let { offsetIndex ->
                if (offsetIndex and 7 == 0) doubleArray[offsetIndex shr 3]
                else super.getLong(index).bitsToDouble()
            }

    override fun setDouble(index: Int,
                           value: Double) =
            index(index, 8).let { offsetIndex ->
                if (offsetIndex and 7 == 0) doubleArray.set(offsetIndex shr 3,
                        value)
                else super.setLong(index, value.bits())
            }
}
