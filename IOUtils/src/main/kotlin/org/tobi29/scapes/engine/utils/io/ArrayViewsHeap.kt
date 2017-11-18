package org.tobi29.scapes.engine.utils.io

import org.tobi29.scapes.engine.utils.HeapByteArraySlice
import org.tobi29.scapes.engine.utils.index
import org.tobi29.scapes.engine.utils.prepareSlice

val ByteViewRO.viewBE: ByteViewBERO
    get() = when (this) {
        is ByteViewBERO -> this
        is HeapByteArraySlice -> HeapViewByteBE(array, offset, size)
        else -> object : ByteViewBERO, ByteViewRO by this {
            override fun slice(index: Int) = slice(index, size - index)

            override fun slice(index: Int,
                               size: Int): ByteViewBERO =
                    this@viewBE.slice(index, size).let {
                        if (it === this@viewBE) this
                        else it.viewBE
                    }
        }
    }

val ByteView.viewBE: ByteViewBE
    get() = when (this) {
        is ByteViewBE -> this
        is HeapByteArraySlice -> HeapViewByteBE(array, offset, size)
        else -> object : ByteViewBE, ByteView by this {
            override fun slice(index: Int) = slice(index, size - index)

            override fun slice(index: Int,
                               size: Int): ByteViewBE =
                    this@viewBE.slice(index, size).let {
                        if (it === this@viewBE) this
                        else it.viewBE
                    }
        }
    }

val ByteViewRO.viewLE: ByteViewLERO
    get() = when (this) {
        is ByteViewLERO -> this
        is HeapByteArraySlice -> HeapViewByteLE(array, offset, size)
        else -> object : ByteViewLERO, ByteViewRO by this {
            override fun slice(index: Int) = slice(index, size - index)

            override fun slice(index: Int,
                               size: Int): ByteViewLERO =
                    this@viewLE.slice(index, size).let {
                        if (it === this@viewLE) this
                        else it.viewLE
                    }
        }
    }

val ByteView.viewLE: ByteViewLE
    get() = when (this) {
        is ByteViewLE -> this
        is HeapByteArraySlice -> HeapViewByteLE(array, offset, size)
        else -> object : ByteViewLE, ByteView by this {
            override fun slice(index: Int) = slice(index, size - index)

            override fun slice(index: Int,
                               size: Int): ByteViewLE =
                    this@viewLE.slice(index, size).let {
                        if (it === this@viewLE) this
                        else it.viewLE
                    }
        }
    }

interface HeapView : ByteViewE {
    val offset: Int
}

inline fun ByteViewERO.equivalentFor(byteArray: ByteArray): HeapViewByte =
        if (isBigEndian) byteArray.viewBE
        else byteArray.viewLE

inline fun HeapViewByte.equivalentFor(byteArray: ByteArray): HeapViewByte =
        when (this) {
            is HeapViewByteBE -> equivalentFor(byteArray)
            is HeapViewByteLE -> equivalentFor(byteArray)
            else -> throw IllegalArgumentException(
                    "Memory view has no endianness")
        }

inline fun HeapViewByteBE.equivalentFor(byteArray: ByteArray): HeapViewByteBE =
        byteArray.viewBE

inline val ByteArray.viewBE: HeapViewByteBE
    get() = HeapViewByteBE(this, 0, size)

inline fun HeapViewByteLE.equivalentFor(byteArray: ByteArray): HeapViewByteLE =
        byteArray.viewLE

inline val ByteArray.viewLE: HeapViewByteLE
    get() = HeapViewByteLE(this, 0, size)

inline fun <A, R : HeapView> R.prepareSlice(
        array: A,
        index: Int,
        size: Int,
        supplier: (A, Int, Int) -> R
): R = prepareSlice(index, size) { offset, size ->
    supplier(array, offset, size)
}

inline fun <R : HeapView> R.prepareSlice(
        index: Int,
        size: Int,
        supplier: (Int, Int) -> R
): R = prepareSlice(this.offset, this.size, index, size, supplier)

inline fun HeapView.index(index: Int,
                          size: Int = 1): Int =
        index(offset, this.size, index, size)

inline fun <R> ByteViewRO.readAsByteArray(block: (ByteArray, Int, Int) -> R): R {
    val array: ByteArray
    val offset: Int
    when (this) {
        is HeapByteArraySlice -> {
            array = this.array
            offset = this.offset
        }
        else -> {
            array = asByteArray()
            offset = 0
        }
    }
    return block(array, offset, size)
}