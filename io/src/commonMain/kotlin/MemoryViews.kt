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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.io

import org.tobi29.arrays.*
import org.tobi29.stdex.*

interface MemorySegmentE : Vars {
    val isBigEndian: Boolean
    val isLittleEndian: Boolean get() = !isBigEndian
}

interface MemorySegmentBE : MemorySegmentE {
    override val isBigEndian: Boolean get() = true
}

interface MemorySegmentLE : MemorySegmentE {
    override val isBigEndian: Boolean get() = false
}

interface ByteViewERO : BytesRO, ShortViewERO, MemorySegmentE {
    override fun slice(index: Int): ByteViewERO

    override fun slice(index: Int, size: Int): ByteViewERO

    override fun getShorts(index: Int, shortView: ShortView) {
        if (shortView is Bytes) return getBytes(index, shortView)
        super.getShorts(index, shortView)
    }
}

interface ByteViewE : ByteViewERO, Bytes, ShortViewE, MemorySegmentE {
    override fun slice(index: Int): ByteViewE

    override fun slice(index: Int, size: Int): ByteViewE

    override fun setShorts(index: Int, shortView: ShortView) {
        if (shortView is Bytes) return setBytes(index, shortView)
        super.setShorts(index, shortView)
    }
}

interface ByteViewBERO : ByteViewERO, ShortViewBERO, MemorySegmentBE {
    override fun slice(index: Int): ByteViewBERO

    override fun slice(index: Int, size: Int): ByteViewBERO

    override fun getShort(index: Int): Short =
        combineToShort(getByte(index + 0), getByte(index + 1))
}

interface ByteViewBE : ByteViewBERO, ByteViewE, ShortViewBE {
    override fun slice(index: Int): ByteViewBE

    override fun slice(index: Int, size: Int): ByteViewBE

    override fun setShort(index: Int, value: Short) =
        value.splitToBytes { b1, b0 ->
            setByte(index + 0, b1)
            setByte(index + 1, b0)
        }
}

interface ByteViewLERO : ByteViewERO, ShortViewLERO, MemorySegmentLE {
    override fun slice(index: Int): ByteViewLERO

    override fun slice(index: Int, size: Int): ByteViewLERO

    override fun getShort(index: Int): Short =
        combineToShort(getByte(index + 1), getByte(index + 0))
}

interface ByteViewLE : ByteViewLERO, ByteViewE, ShortViewLE {
    override fun slice(index: Int): ByteViewLE

    override fun slice(index: Int, size: Int): ByteViewLE

    override fun setShort(index: Int, value: Short) =
        value.splitToBytes { b1, b0 ->
            setByte(index + 0, b0)
            setByte(index + 1, b1)
        }
}

interface ShortViewRO : Vars {
    override fun slice(index: Int): ShortViewRO

    override fun slice(index: Int, size: Int): ShortViewRO

    fun getShort(index: Int): Short

    fun getShorts(
        index: Int,
        shortView: ShortView
    ) {
        var j = index
        for (i in 0 until shortView.size step 2) {
            shortView.setShort(i, getShort(j))
            j += 2
        }
    }

    fun getChar(index: Int): Char =
        getShort(index).toChar()

    fun getChars(index: Int, charView: CharView) =
        getShorts(index, charView)
}

interface ShortView : ShortViewRO {
    override fun slice(index: Int): ShortView

    override fun slice(index: Int, size: Int): ShortView

    fun setShort(index: Int, value: Short)

    fun setChar(index: Int, value: Char) =
        setShort(index, value.toShort())

    fun setShorts(index: Int, shortView: ShortView) =
        shortView.getShorts(0, slice(index, shortView.size))

    fun setChars(index: Int, charView: CharView) =
        setShorts(index, charView)
}

interface ShortViewERO : ShortViewRO, IntViewERO, MemorySegmentE {
    override fun slice(index: Int): ShortViewERO

    override fun slice(index: Int, size: Int): ShortViewERO

    override fun getInts(index: Int, intView: IntView) {
        if (intView is ShortView) return getShorts(index, intView)
        super.getInts(index, intView)
    }
}

interface ShortViewE : ShortViewERO, ShortView, IntViewE, MemorySegmentE {
    override fun slice(index: Int): ShortViewE

    override fun slice(index: Int, size: Int): ShortViewE

    override fun setInts(index: Int, intView: IntView) {
        if (intView is ShortView) return setShorts(index, intView)
        super.setInts(index, intView)
    }
}


interface ShortViewBERO : ShortViewERO, IntViewBERO, MemorySegmentBE {
    override fun slice(index: Int): ShortViewBERO

    override fun slice(index: Int, size: Int): ShortViewBERO

    override fun getInt(index: Int): Int =
        combineToInt(getShort(index + 0), getShort(index + 2))
}

interface ShortViewBE : ShortViewBERO, ShortViewE, IntViewBE {
    override fun slice(index: Int): ShortViewBE

    override fun slice(index: Int, size: Int): ShortViewBE

    override fun getInt(index: Int): Int =
        combineToInt(getShort(index + 0), getShort(index + 2))

    override fun setInt(index: Int, value: Int) =
        value.splitToShorts { s1, s0 ->
            setShort(index + 0, s1)
            setShort(index + 2, s0)
        }
}

interface ShortViewLERO : ShortViewERO, IntViewLERO, MemorySegmentLE {
    override fun slice(index: Int): ShortViewLERO

    override fun slice(index: Int, size: Int): ShortViewLERO

    override fun getInt(index: Int): Int =
        combineToInt(getShort(index + 2), getShort(index + 0))
}

interface ShortViewLE : ShortViewLERO, ShortViewE, IntViewLE {
    override fun slice(index: Int): ShortViewLE

    override fun slice(index: Int, size: Int): ShortViewLE

    override fun getInt(index: Int): Int =
        combineToInt(getShort(index + 2), getShort(index + 0))

    override fun setInt(index: Int, value: Int) =
        value.splitToShorts { s1, s0 ->
            setShort(index + 0, s0)
            setShort(index + 2, s1)
        }
}

typealias CharViewRO = ShortViewRO
typealias CharView = ShortView
typealias CharViewERO = ShortViewERO
typealias CharViewE = ShortViewE
typealias CharViewBERO = ShortViewBERO
typealias CharViewBE = ShortViewBE
typealias CharViewLERO = ShortViewLERO
typealias CharViewLE = ShortViewLE

interface IntViewRO : Vars {
    override fun slice(index: Int): IntViewRO

    override fun slice(index: Int, size: Int): IntViewRO

    fun getInt(index: Int): Int

    fun getInts(index: Int, intView: IntView) {
        var j = index
        for (i in 0 until intView.size step 4) {
            intView.setInt(i, getInt(j))
            j += 4
        }
    }

    fun getFloat(index: Int): Float =
        Float.fromBits(getInt(index))

    fun getFloats(index: Int, floatView: FloatView) =
        getInts(index, floatView)
}

interface IntView : IntViewRO {
    override fun slice(index: Int): IntView

    override fun slice(index: Int, size: Int): IntView

    fun setInt(index: Int, value: Int)

    fun setInts(index: Int, intView: IntView) =
        intView.getInts(0, slice(index, intView.size))

    fun setFloat(index: Int, value: Float) =
        setInt(index, value.toRawBits())

    fun setFloats(index: Int, floatView: FloatView) =
        setInts(index, floatView)
}

interface IntViewERO : IntViewRO, LongViewERO, MemorySegmentE {
    override fun slice(index: Int): IntViewERO

    override fun slice(index: Int, size: Int): IntViewERO

    override fun getLongs(index: Int, longView: LongView) {
        if (longView is IntView) return getInts(index, longView)
        super.getLongs(index, longView)
    }
}

interface IntViewE : IntViewERO, IntView, LongViewE {
    override fun slice(index: Int): IntViewE

    override fun slice(index: Int, size: Int): IntViewE

    override fun setLongs(index: Int, longView: LongView) {
        if (longView is IntView) return setInts(index, longView)
        super.setLongs(index, longView)
    }
}

interface IntViewBERO : IntViewERO, LongViewBERO, MemorySegmentBE {
    override fun slice(index: Int): IntViewBERO

    override fun slice(index: Int, size: Int): IntViewBERO

    override fun getLong(index: Int): Long = combineToLong(
        getInt(index + 0), getInt(index + 4)
    )
}

interface IntViewBE : IntViewBERO, IntViewE, LongViewBE {
    override fun slice(index: Int): IntViewBE

    override fun slice(index: Int, size: Int): IntViewBE

    override fun setLong(index: Int, value: Long) =
        value.splitToInts { i1, i0 ->
            setInt(index + 0, i1)
            setInt(index + 4, i0)
        }
}

interface IntViewLERO : IntViewERO, LongViewLERO, MemorySegmentLE {
    override fun slice(index: Int): IntViewLERO

    override fun slice(index: Int, size: Int): IntViewLERO

    override fun getLong(index: Int): Long = combineToLong(
        getInt(index + 4), getInt(index + 0)
    )
}

interface IntViewLE : IntViewLERO, IntViewE, LongViewLE {
    override fun slice(index: Int): IntViewLE

    override fun slice(index: Int, size: Int): IntViewLE

    override fun setLong(index: Int, value: Long) =
        value.splitToInts { i1, i0 ->
            setInt(index + 0, i0)
            setInt(index + 4, i1)
        }
}

typealias FloatViewRO = IntViewRO
typealias FloatView = IntView
typealias FloatViewERO = IntViewERO
typealias FloatViewE = IntViewE
typealias FloatViewBERO = IntViewBERO
typealias FloatViewBE = IntViewBE
typealias FloatViewLERO = IntViewLERO
typealias FloatViewLE = IntViewLE

interface LongViewRO : Vars {
    override fun slice(index: Int): LongViewRO

    override fun slice(index: Int, size: Int): LongViewRO

    fun getLong(index: Int): Long

    fun getLongs(index: Int, longView: LongView) {
        var j = index
        for (i in 0 until longView.size step 8) {
            longView.setLong(i, getLong(j))
            j += 8
        }
    }

    fun getDouble(index: Int): Double =
        Double.fromBits(getLong(index))

    fun getDoubles(index: Int, doubleView: DoubleView) =
        getLongs(index, doubleView)
}

interface LongView : LongViewRO, Vars {
    override fun slice(index: Int): LongView

    override fun slice(index: Int, size: Int): LongView

    fun setLong(index: Int, value: Long)

    fun setLongs(index: Int, longView: LongView) =
        longView.getLongs(0, slice(index, longView.size))

    fun setDouble(index: Int, value: Double) =
        setLong(index, value.toRawBits())

    fun setDoubles(index: Int, doubleView: DoubleView) =
        setLongs(index, doubleView)
}

interface LongViewERO : LongViewRO, MemorySegmentE {
    override fun slice(index: Int): LongViewERO

    override fun slice(index: Int, size: Int): LongViewERO
}

interface LongViewE : LongViewERO, LongView, MemorySegmentE {
    override fun slice(index: Int): LongViewE

    override fun slice(index: Int, size: Int): LongViewE
}

interface LongViewBERO : LongViewERO,
    MemorySegmentBE {
    override fun slice(index: Int): LongViewBERO

    override fun slice(index: Int, size: Int): LongViewBERO
}

interface LongViewBE : LongViewBERO, LongViewE {
    override fun slice(index: Int): LongViewBE

    override fun slice(index: Int, size: Int): LongViewBE
}

interface LongViewLERO : LongViewERO, MemorySegmentLE {
    override fun slice(index: Int): LongViewLERO

    override fun slice(index: Int, size: Int): LongViewLERO
}

interface LongViewLE : LongViewLERO, LongViewE {
    override fun slice(index: Int): LongViewLE

    override fun slice(index: Int, size: Int): LongViewLE
}

typealias DoubleViewRO = LongViewRO
typealias DoubleView = LongView
typealias DoubleViewERO = LongViewERO
typealias DoubleViewE = LongViewE
typealias DoubleViewBERO = LongViewBERO
typealias DoubleViewBE = LongViewBE
typealias DoubleViewLERO = LongViewLERO
typealias DoubleViewLE = LongViewLE

fun BytesRO.readAsByteArray(): ByteArray = when (this) {
    is HeapBytes ->
        if (size == array.size && offset == 0) array else {
            ByteArray(size).also { copy(array, it, size, offset) }
        }
    else -> ByteArray(size) { getByte(it) }
}

fun BytesRO.asByteArray(): ByteArray = when (this) {
    is HeapBytes -> ByteArray(size).also {
        copy(array, it, size, offset)
    }
    else -> ByteArray(size) { getByte(it) }
}

inline fun HeapBytes.index(
    index: Int,
    dataLength: Int = 1
): Int = index(offset, size, index, dataLength)

expect val BytesRO.ro: BytesRO
expect val ByteViewERO.ro: ByteViewERO
expect val ByteViewBERO.ro: ByteViewBERO
expect val ByteViewLERO.ro: ByteViewLERO

expect val ShortViewRO.ro: ShortViewRO
expect val ShortViewERO.ro: ShortViewERO
expect val ShortViewBERO.ro: ShortViewBERO
expect val ShortViewLERO.ro: ShortViewLERO

expect val IntViewRO.ro: IntViewRO
expect val IntViewERO.ro: IntViewERO
expect val IntViewBERO.ro: IntViewBERO
expect val IntViewLERO.ro: IntViewLERO

expect val LongViewRO.ro: LongViewRO
expect val LongViewERO.ro: LongViewERO
expect val LongViewBERO.ro: LongViewBERO
expect val LongViewLERO.ro: LongViewLERO
