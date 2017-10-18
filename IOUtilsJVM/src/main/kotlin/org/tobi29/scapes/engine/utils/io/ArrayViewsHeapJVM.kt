package org.tobi29.scapes.engine.utils.io

import org.tobi29.scapes.engine.utils.HeapByteArraySlice

impl sealed class HeapViewByte impl constructor(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapByteArraySlice(array, offset, size), HeapView {
    init {
        if (offset < 0 || size < 0 || offset + size > array.size shl 0)
            throw IndexOutOfBoundsException("Invalid offset or size")
    }

    impl override abstract fun slice(index: Int): HeapViewByte

    impl override abstract fun slice(index: Int,
                                     size: Int): HeapViewByte
}

impl class HeapViewByteBE impl constructor(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapViewByte(array, offset, size), ByteViewBE {
    impl override fun slice(index: Int) = slice(index, size - index)

    impl override fun slice(index: Int,
                            size: Int): HeapViewByteBE =
            prepareSlice(array, index, size, ::HeapViewByteBE)
}

impl class HeapViewByteLE impl constructor(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapViewByte(array, offset, size), ByteViewLE {
    impl override fun slice(index: Int) = slice(index, size - index)

    impl override fun slice(index: Int,
                            size: Int): HeapViewByteLE =
            prepareSlice(array, index, size, ::HeapViewByteLE)
}
