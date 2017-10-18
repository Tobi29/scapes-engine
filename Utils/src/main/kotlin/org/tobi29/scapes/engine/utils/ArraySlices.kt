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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils

/**
 * Arbitrary array segment
 */
interface ArraySegment {
    /**
     * Amount of elements in the slice
     */
    val size: Int

    fun slice(index: Int): ArraySegment

    fun slice(index: Int,
              size: Int): ArraySegment
}

/**
 * Arbitrary array slice
 */
interface ArrayVarSlice<out T> : ArraySegment, Iterable<T>

/**
 * Arbitrary array slice based on some heap array with offset support
 */
interface HeapArrayVarSlice<out T> : ArrayVarSlice<T> {
    /**
     * First element in backing array used by the slice
     */
    val offset: Int
}

inline internal fun <T, R : HeapArrayVarSlice<*>> R.prepareSlice(
        index: Int,
        size: Int,
        array: T,
        supplier: (T, Int, Int) -> R
): R = prepareSlice(this.offset, this.size, index, size) { offset, size ->
    supplier(array, offset, size)
}

fun checkSliceBounds(srcLength: Int,
                     index: Int,
                     length: Int) {
    if (index < 0 || length < 0 || index + length > srcLength) {
        throw IndexOutOfBoundsException(
                "Invalid slice bounds: srcLength = $srcLength index = $index length = $length")
    }
}

inline fun <R> R.prepareSlice(
        srcOffset: Int,
        srcLength: Int,
        index: Int,
        size: Int,
        supplier: (Int, Int) -> R
): R {
    checkSliceBounds(srcLength, index, size)
    return if (index == 0 && srcLength == size) this
    else supplier(srcOffset + index, size)
}

inline fun index(offset: Int,
                 size: Int,
                 index: Int,
                 dataLength: Int = 1): Int {
    if (index < 0 || index + dataLength > size)
        throw IndexOutOfBoundsException("Invalid index")
    return index + offset
}

internal abstract class SliceIterator<out T>(size: Int) : Iterator<T> {
    private var index = 0
    private val end = index + size
    override fun hasNext() = index < end
    override fun next(): T {
        if (index >= end) throw NoSuchElementException(
                "No more elements in iterator")
        return access(index++)
    }

    protected abstract fun access(index: Int): T
}
