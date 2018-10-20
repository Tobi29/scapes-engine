/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.arrays

/**
 * Arbitrary 1-dimensional array
 */
interface Vars {
    /**
     * Amount of elements in the array
     */
    val size: Int

    fun slice(index: Int): Vars

    fun slice(index: Int, size: Int): Vars
}

/**
 * Arbitrary 1-dimensional array
 */
interface VarsIterable<out T> : Vars {
    operator fun iterator(): Iterator<T>
}

/**
 * Arbitrary 1-dimensional array
 */
interface VarsModifiableIterable<T> : VarsIterable<T> {
    override fun iterator(): ModifiableIterator<T>
}

fun <T> VarsIterable<T>.asIterable(): Iterable<T> = object : Iterable<T> {
    override fun iterator(): Iterator<T> = this@asIterable.iterator()
}

fun <T> VarsIterable<T>.asSequence(): Sequence<T> = object : Sequence<T> {
    override fun iterator(): Iterator<T> = this@asSequence.iterator()
}

/**
 * Iterator which allows modifying elements
 */
interface ModifiableIterator<T> : Iterator<T> {
    /**
     * Changed the element at the index of the last [next] call
     */
    fun set(value: T)
}

/**
 * Arbitrary 2-dimensional array
 */
interface Vars2 {
    /**
     * Amount of indices on the first axis
     */
    val width: Int

    /**
     * Amount of indices on the second axis
     */
    val height: Int

    val size: Int get() = width * height
}

/**
 * Arbitrary 3-dimensional array
 */
interface Vars3 {
    /**
     * Amount of indices on the first axis
     */
    val width: Int

    /**
     * Amount of indices on the second axis
     */
    val height: Int

    /**
     * Amount of indices on the third axis
     */
    val depth: Int

    val size: Int get() = width * height * depth
}

/**
 * Arbitrary array slice based on some heap array with offset support
 */
interface HeapArrayVarSlice<out T> : VarsIterable<T> {
    /**
     * First element in backing array used by the slice
     */
    val offset: Int
}

internal inline fun <T, R : HeapArrayVarSlice<*>> R.prepareSlice(
    index: Int,
    size: Int,
    array: T,
    supplier: (T, Int, Int) -> R
): R = prepareSlice(this.offset, this.size, index, size) { offset, size ->
    supplier(array, offset, size)
}

internal inline fun <T, R : Vars> R.prepareSlice(
    index: Int,
    size: Int,
    array: T,
    supplier: (T, Int, Int) -> R
): R = prepareSlice(0, this.size, index, size) { offset, size ->
    supplier(array, offset, size)
}

fun checkSliceBounds(
    srcLength: Int,
    index: Int,
    length: Int
) {
    if (index < 0 || length < 0 || index + length > srcLength) {
        throw IndexOutOfBoundsException(
            "Invalid slice bounds: srcLength = $srcLength index = $index length = $length"
        )
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

inline fun index(
    offset: Int,
    size: Int,
    index: Int,
    dataLength: Int = 1
): Int {
    if (index < 0 || index + dataLength > size)
        throw IndexOutOfBoundsException("Invalid index")
    return index + offset
}

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x index of the element
 */
inline fun Vars.indices(block: (Int) -> Unit) {
    for (x in 0 until size) {
        block(x)
    }
}

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x and y coords of the element
 */
inline fun Vars2.indices(block: (Int, Int) -> Unit) {
    for (y in 0 until height) {
        for (x in 0 until width) {
            block(x, y)
        }
    }
}

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x, y and z coords of the element
 */
inline fun Vars3.indices(block: (Int, Int, Int) -> Unit) {
    for (z in 0 until depth) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                block(x, y, z)
            }
        }
    }
}

internal abstract class SliceIterator<out T>(
    private val size: Int
) : Iterator<T> {
    protected var index = -1
        private set

    override fun hasNext() = index + 1 < size
    override fun next(): T {
        if (!hasNext()) throw NoSuchElementException(
            "No more elements in iterator"
        )
        index++
        return access(index)
    }

    protected abstract fun access(index: Int): T
}

internal abstract class SliceModifiableIterator<T>(
    size: Int
) : SliceIterator<T>(size), ModifiableIterator<T> {
    override fun set(value: T) {
        if (index < 0) throw NoSuchElementException(
            "Need to call next at least once before calling set"
        )
        accessSet(index, value)
    }

    protected abstract fun accessSet(index: Int, value: T)
}
