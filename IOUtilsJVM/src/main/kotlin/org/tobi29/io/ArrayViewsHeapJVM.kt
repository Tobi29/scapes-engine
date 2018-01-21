/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.io

import org.tobi29.arrays.HeapByteArraySlice

actual sealed class HeapViewByte actual constructor(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapByteArraySlice(array, offset, size),
        HeapView {
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
) : HeapViewByte(array, offset, size),
        ByteViewBE {
    actual override fun slice(index: Int) = slice(index, size - index)

    actual override fun slice(index: Int,
                              size: Int): HeapViewByteBE =
            prepareSlice(array, index, size, ::HeapViewByteBE)
}

actual class HeapViewByteLE actual constructor(
        array: ByteArray,
        offset: Int,
        size: Int
) : HeapViewByte(array, offset, size),
        ByteViewLE {
    actual override fun slice(index: Int) = slice(index, size - index)

    actual override fun slice(index: Int,
                              size: Int): HeapViewByteLE =
            prepareSlice(array, index, size, ::HeapViewByteLE)
}
