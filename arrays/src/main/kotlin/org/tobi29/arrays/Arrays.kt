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

// GENERATED FILE, DO NOT EDIT DIRECTLY!!!
// Generation script can be found in `resources/codegen/GenArrays.kts`.
// Run `resources/codegen/codegen.sh` to update sources.

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.arrays

import org.tobi29.stdex.copy

/**
 * 1-dimensional read-only array
 */
interface ElementsRO<out T> : VarsIterable<T> {
    /**
     * Returns the element at the given index in the array
     * @param index Index of the element
     * @return The value at the given index
     */
    operator fun get(index: Int): T

    override fun slice(index: Int): ElementsRO<T> =
        slice(index, size - index)

    override fun slice(index: Int, size: Int): ElementsRO<T> =
        prepareSlice(index, size, this, ::ElementsROSlice)


    fun getElements(index: Int, slice: Elements<in T>) {
        var j = index
        for (i in 0 until slice.size) {
            slice[i] = this[j++]
        }
    }

    override fun iterator(): Iterator<T> =
        object : SliceIterator<T>(size) {
            override fun access(index: Int) = get(index)
        }
}

/**
 * 1-dimensional read-write array
 */
interface Elements<T> : ElementsRO<T> {
    /**
     * Sets the element at the given index in the array
     * @param index Index of the element
     * @param value The value to set to
     */
    operator fun set(index: Int, value: T)

    override fun slice(index: Int): Elements<T> =
        slice(index, size - index)

    override fun slice(index: Int, size: Int): Elements<T> =
        prepareSlice(index, size, this, ::ElementsSlice)


    fun setElements(index: Int, slice: ElementsRO<out T>) =
        slice.getElements(0, slice(index, slice.size))
}

/**
 * 2-dimensional read-only array
 */
interface ElementsRO2<out T> : Vars2 {
    /**
     * Returns the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @return The value at the given index
     */
    operator fun get(index1: Int, index2: Int): T
}

/**
 * 2-dimensional read-write array
 */
interface Elements2<T> : ElementsRO2<T> {
    /**
     * Sets the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param value The value to set to
     */
    operator fun set(index1: Int, index2: Int, value: T)
}

/**
 * 3-dimensional read-only array
 */
interface ElementsRO3<out T> : Vars3 {
    /**
     * Returns the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param index3 Index on the third axis of the element
     * @return The value at the given index
     */
    operator fun get(index1: Int, index2: Int, index3: Int): T
}

/**
 * 3-dimensional read-write array
 */
interface Elements3<T> : ElementsRO3<T> {
    /**
     * Sets the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param index3 Index on the third axis of the element
     * @param value The value to set to
     */
    operator fun set(index1: Int, index2: Int, index3: Int, value: T)
}

internal open class ElementsROSlice<T>(
    open val array: ElementsRO<T>,
    final override val offset: Int,
    final override val size: Int
) : HeapArrayVarSlice<T>, ElementsRO<T> {
    final override fun get(index: Int): T =
        array[index(offset, size, index)]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ElementsRO<*>) return false
        for (i in 0 until size) {
            if (this[i] != other[i]) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var h = 1
        for (i in 0 until size) {
            h = h * 31 + (this[i]?.hashCode() ?: 0)
        }
        return h
    }
}

internal class ElementsSlice<T>(
    override val array: Elements<T>,
    offset: Int,
    size: Int
) : ElementsROSlice<T>(array, offset, size), Elements<T> {
    override fun set(index: Int, value: T) =
        array.set(index(offset, size, index), value)
}

/**
 * Slice of a normal heap array
 */
open class HeapElements<T>(
    val array: Array<T>,
    final override val offset: Int,
    final override val size: Int
) : HeapArrayVarSlice<T>, Elements<T> {
    override fun slice(index: Int): HeapElements<T> =
        slice(index, size - index)

    override fun slice(index: Int, size: Int): HeapElements<T> =
        prepareSlice(index, size, array, ::HeapElements)

    final override fun get(index: Int): T =
        array[index(offset, size, index)]

    final override fun set(index: Int, value: T) =
        array.set(index(offset, size, index), value)

    final override fun getElements(
        index: Int,
        slice: Elements<in T>
    ) {
        if (slice !is HeapElements) return super.getElements(index, slice)

        if (index < 0 || index + slice.size > size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        copy(array, slice.array, slice.size, index + this.offset, slice.offset)
    }

    final override fun setElements(index: Int, slice: ElementsRO<out T>) {
        if (slice !is HeapElements) return super.setElements(index, slice)

        if (index < 0 || index + slice.size > size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        copy(slice.array, array, slice.size, slice.offset, index + this.offset)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ElementsRO<*>) return false
        for (i in 0 until size) {
            if (this[i] != other[i]) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var h = 1
        for (i in 0 until size) {
            h = h * 31 + (this[i]?.hashCode() ?: 0)
        }
        return h
    }
}

/**
 * Creates a slice from the given array, which holds the array itself as backing
 * storage
 * @param index Index to start the slice at
 * @param size Amount of elements in slice
 * @receiver The array to create a slice of
 * @return A slice from the given array
 */
inline fun <T> Array<T>.sliceOver(
    index: Int = 0,
    size: Int = this.size - index
): HeapElements<T> = HeapElements(this, index, size)

/**
 * Exposes the contents of the slice in an array and calls [block] with
 * the array and beginning and end of the slice data in it
 *
 * **Note:** The array may or may not be a copy of the slice, so modifying it
 * is under no circumstances supported
 *
 * **Note:** The lifecycle of the exposed array does *not* extend outside
 * of this call, so storing the array for later use is not supported
 *
 * **Note:** To improve performance the section containing the slice data
 * may be only a sub sequence of the array
 *
 * **Note:** This is a performance oriented function, so read those notes!
 * @param block Code to execute with the arrays contents
 * @receiver The slice to read
 * @return Return value of [block]
 */
inline fun <reified T, R> ElementsRO<T>.readAsArray(block: (Array<T>, Int, Int) -> R): R {
    val array: Array<T>
    val offset: Int
    when (this) {
        is HeapElements<T> -> {
            array = this.array
            offset = this.offset
        }
        else -> {
            array = toArray()
            offset = 0
        }
    }
    return block(array, offset, size)
}

/**
 * Exposes the contents of the slice in an array and calls [block] with
 * the array and beginning and end of the slice data in it
 *
 * **Note:** The array may or may not be a copy of the slice, so reading
 * or modifying the original slice during this call can lead to surprising
 * results
 *
 * **Note:** The lifecycle of the exposed array does *not* extend outside
 * of this call, so storing the array for later use is not supported
 *
 * **Note:** To improve performance the section containing the slice data
 * may be only a sub sequence of the array
 *
 * **Note:** This is a performance oriented function, so read those notes!
 * @param block Code to execute with the arrays contents
 * @receiver The slice to read and modify
 * @return Return value of [block]
 */
inline fun <reified T, R> Elements<T>.mutateAsArray(block: (Array<T>, Int, Int) -> R): R {
    val array: Array<T>
    val offset: Int
    val mapped = when (this) {
        is HeapElements<T> -> {
            array = this.array
            offset = this.offset
            true
        }
        else -> {
            array = toArray()
            offset = 0
            false
        }
    }
    return try {
        block(array, offset, size)
    } finally {
        if (!mapped) getElements(0, array.sliceOver())
    }
}

/**
 * Exposes the contents of the slice in an array
 *
 * **Note:** The array may or may not be a copy of the slice, so modifying it
 * is under no circumstances supported
 * @receiver The slice to read
 * @return Array containing the data of the slice
 */
inline fun <reified T> ElementsRO<T>.readAsArray(): Array<T> =
    if (this is HeapElements<T> && size == array.size && offset == 0) array
    else Array(size) { get(it) }

/**
 * Copies the contents of the slice into an array
 * @receiver The slice to copy
 * @return Array containing the data of the slice
 */
inline fun <reified T> ElementsRO<T>.toArray(): Array<T> =
    Array(size) { get(it) }

/**
 * Class wrapping an array to provide nicer support for 2-dimensional data.
 *
 * The layout for the dimensions is as follows:
 * `index = y * width + x`
 */
class Array2<T>(
    override val width: Int,
    override val height: Int,
    val array: Array<T>
) : Elements2<T>,
    Iterable<T> {
    init {
        if (size != array.size) {
            throw IllegalArgumentException(
                "Array has invalid size: ${array.size} (should be $size)"
            )
        }
    }

    /**
     * Retrieve an element from the array or `null` if out of bounds.
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @return The element at the given position or `null` if out of bounds
     */
    fun getOrNull(index1: Int, index2: Int): T? {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            return null
        }
        return array[index2 * width + index1]
    }

    override fun get(index1: Int, index2: Int): T {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            throw IndexOutOfBoundsException("$index1 $index2")
        }
        return array[index2 * width + index1]
    }

    override fun set(index1: Int, index2: Int, value: T) {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            throw IndexOutOfBoundsException("$index1 $index2")
        }
        array[index2 * width + index1] = value
    }

    override operator fun iterator() = array.iterator()

    /**
     * Makes a shallow copy of the array and wrapper.
     * @return A new wrapper around a new array
     */
    fun copyOf() = Array2(width, height, array.copyOf())
}

/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param init Returns values to be inserted by default
 * @return Wrapper around a new array
 */
inline fun <reified T> Array2(
    width: Int, height: Int,
    init: (Int, Int) -> T
) = Array2(width, height) { i ->
    val x = i % width
    val y = i / width
    init(x, y)
}

/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param init Returns values to be inserted by default
 * @return Wrapper around a new array
 */
inline fun <reified T> Array2(
    width: Int, height: Int,
    init: (Int) -> T
) = Array2(
    width, height,
    Array(width * height) { init(it) }
)

/**
 * Class wrapping an array to provide nicer support for 3-dimensional data.
 *
 * The layout for the dimensions is as follows:
 * `index = (z * height + y) * width + x`
 */
class Array3<T>(
    override val width: Int,
    override val height: Int,
    override val depth: Int,
    val array: Array<T>
) : Elements3<T>,
    Iterable<T> {
    init {
        if (size != array.size) {
            throw IllegalArgumentException(
                "Array has invalid size: ${array.size} (should be $size)"
            )
        }
    }

    /**
     * Retrieve an element from the array or `null` if out of bounds.
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param index3 Index on the third axis of the element
     * @return The element at the given position or `null` if out of bounds
     */
    fun getOrNull(index1: Int, index2: Int, index3: Int): T? {
        if (index1 < 0 || index2 < 0 || index3 < 0
            || index1 >= width || index2 >= height || index3 >= depth) {
            return null
        }
        return array[(index3 * height + index2) * width + index1]
    }

    override fun get(index1: Int, index2: Int, index3: Int): T {
        if (index1 < 0 || index2 < 0 || index3 < 0
            || index1 >= width || index2 >= height || index3 >= depth) {
            throw IndexOutOfBoundsException("$index1 $index2 $index3")
        }
        return array[(index3 * height + index2) * width + index1]
    }

    override fun set(index1: Int, index2: Int, index3: Int, value: T) {
        if (index1 < 0 || index2 < 0 || index3 < 0
            || index1 >= width || index2 >= height || index3 >= depth) {
            throw IndexOutOfBoundsException("$index1 $index2")
        }
        array[(index3 * height + index2) * width + index1] = value
    }

    override operator fun iterator() = array.iterator()

    /**
     * Makes a shallow copy of the array and wrapper.
     * @return A new wrapper around a new array
     */
    fun copyOf() = Array3(width, height, depth, array.copyOf())
}

/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param depth Depth of the wrapper
 * @param init Returns values to be inserted by default
 * @return Wrapper around a new array
 */
inline fun <reified T> Array3(
    width: Int, height: Int, depth: Int,
    init: (Int, Int, Int) -> T
) = Array3(width, height, depth) { i ->
    val x = i % width
    val j = i / width
    val y = j % height
    val z = j / height
    init(x, y, z)
}

/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param depth Depth of the wrapper
 * @param init Returns values to be inserted by default
 * @return Wrapper around a new array
 */
inline fun <reified T> Array3(
    width: Int, height: Int, depth: Int,
    init: (Int) -> T
) = Array3(
    width, height, depth,
    Array(width * height * depth) { init(it) }
)

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun <T> Array<in T>.fill(supplier: (Int) -> T) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array and stores its return value in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x and y coords of the element
 */
inline fun <T> Array2<in T>.fill(block: (Int, Int) -> T) =
    indices { x, y -> this[x, y] = block(x, y) }

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array and stores its return value in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x, y and z coords of the element
 */
inline fun <T> Array3<in T>.fill(block: (Int, Int, Int) -> T) =
    indices { x, y, z -> this[x, y, z] = block(x, y, z) }

inline fun <T> Array2<T>.shift(
    x: Int,
    y: Int,
    dispose: (T, Int, Int) -> Unit,
    supplier: (Int, Int) -> T
) {
    if (x == 0 && y == 0) return
    val dx = x.coerceIn(-width, width)
    val dy = y.coerceIn(-height, height)
    run {
        val start = if (dx > 0) width - dx else 0
        val end = if (dx > 0) width else -dx
        for (yy in 0 until height) {
            for (xx in start until end) {
                dispose(this[xx, yy], xx, yy)
            }
        }
    }
    run {
        val start = if (dy > 0) height - dy else 0
        val end = if (dy > 0) height else -dy
        for (yy in start until end) {
            for (xx in 0 until width) {
                dispose(this[xx, yy], xx, yy)
            }
        }
    }
    (dy * width + dx).let { d ->
        if (d in 1 until array.size) {
            copy(array, array, array.size - d, 0, d)
        } else if (-d in 1 until array.size) {
            copy(array, array, array.size + d, -d, 0)
        }
    }
    run {
        val start = if (dx > 0) 0 else width + dx
        val end = if (dx > 0) dx else width
        for (yy in 0 until height) {
            for (xx in start until end) {
                this[xx, yy] = supplier(xx, yy)
            }
        }
    }
    run {
        val start = if (dy > 0) 0 else height + dy
        val end = if (dy > 0) dy else height
        for (yy in start until end) {
            for (xx in 0 until width) {
                this[xx, yy] = supplier(xx, yy)
            }
        }
    }
}

/**
 * Creates a new array and makes it accessible using a wrapper initialized with
 * nulls
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @return Wrapper around a new array
 */
inline fun <reified T> array2OfNulls(
    width: Int, height: Int
) = Array2(
    width, height,
    arrayOfNulls<T>(width * height)
)

/**
 * Creates a new array and makes it accessible using a wrapper initialized with
 * nulls
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param depth Depth of the wrapper
 * @return Wrapper around a new array
 */
inline fun <reified T> array3OfNulls(
    width: Int, height: Int, depth: Int
) = Array3(
    width, height, depth,
    arrayOfNulls<T>(width * height)
)
