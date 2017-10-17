package org.tobi29.scapes.engine.utils.io

import org.tobi29.scapes.engine.utils.HeapByteArraySlice

header sealed class HeapViewByte(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapByteArraySlice, HeapView {
    override abstract fun slice(index: Int,
                                size: Int): HeapViewByte
}

header class HeapViewByteBE(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapViewByte, ByteViewBE {
    override fun slice(index: Int,
                       size: Int): HeapViewByteBE
}

header class HeapViewByteLE(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapViewByte, ByteViewLE {
    override fun slice(index: Int,
                       size: Int): HeapViewByteLE
}
