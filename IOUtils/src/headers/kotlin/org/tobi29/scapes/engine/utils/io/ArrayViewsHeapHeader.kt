package org.tobi29.scapes.engine.utils.io

import org.tobi29.scapes.engine.utils.HeapByteArraySlice

expect sealed class HeapViewByte(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapByteArraySlice, HeapView {
    override abstract fun slice(index: Int): HeapViewByte

    override abstract fun slice(index: Int,
                                size: Int): HeapViewByte
}

expect class HeapViewByteBE(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapViewByte, ByteViewBE {
    override fun slice(index: Int): HeapViewByteBE

    override fun slice(index: Int,
                       size: Int): HeapViewByteBE
}

expect class HeapViewByteLE(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapViewByte, ByteViewLE {
    override fun slice(index: Int): HeapViewByteLE

    override fun slice(index: Int,
                       size: Int): HeapViewByteLE
}
