package org.tobi29.scapes.engine.utils.io

import org.tobi29.scapes.engine.utils.HeapByteArraySlice

actual sealed class HeapViewByte actual constructor(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapByteArraySlice(array, offset, size), HeapView {
    init {
        if (offset < 0 || size < 0 || offset + size > array.size shl 0)
            throw IndexOutOfBoundsException("Invalid offset or size")
    }

    actual override abstract fun slice(index: Int): HeapViewByte

    actual override abstract fun slice(index: Int,
                                     size: Int): HeapViewByte
}

actual class HeapViewByteBE actual constructor(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapViewByte(array, offset, size), ByteViewBE {
    actual override fun slice(index: Int) = slice(index, size - index)

    actual override fun slice(index: Int,
                            size: Int): HeapViewByteBE =
            prepareSlice(array, index, size, ::HeapViewByteBE)
}

actual class HeapViewByteLE actual constructor(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapViewByte(array, offset, size), ByteViewLE {
    actual override fun slice(index: Int) = slice(index, size - index)

    actual override fun slice(index: Int,
                            size: Int): HeapViewByteLE =
            prepareSlice(array, index, size, ::HeapViewByteLE)
}
