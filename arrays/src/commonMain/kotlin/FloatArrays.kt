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

// GENERATED FILE, DO NOT EDIT DIRECTLY!!!
// Generation script can be found in `resources/codegen/GenArrays.kts`.
// Run `resources/codegen/codegen.sh` to update sources.

package org.tobi29.arrays

import org.tobi29.stdex.InlineUtility
import org.tobi29.stdex.copy
import org.tobi29.stdex.primitiveHashCode

/**
 * 1-dimensional read-only array
 */
interface FloatsRO : VarsIterable<Float> {
    /**
     * Returns the element at the given index in the array
     * @param index Index of the element
     * @return The value at the given index
     */
    operator fun get(index: Int): Float

    override fun slice(index: Int): FloatsRO =
        slice(index, size - index)

    override fun slice(index: Int, size: Int): FloatsRO =
        prepareSlice(index, size, this, ::FloatsROSlice)

    fun getFloat(index: Int): Float = get(index)

    fun getFloats(index: Int, slice: Floats) {
        var j = index
        for (i in 0 until slice.size) {
            slice[i] = this[j++]
        }
    }

    override fun iterator(): Iterator<Float> =
        object : SliceIterator<Float>(size) {
            override fun access(index: Int) = get(index)
        }
}

/**
 * 1-dimensional read-write array
 */
interface Floats : FloatsRO, VarsModifiableIterable<Float> {
    /**
     * Sets the element at the given index in the array
     * @param index Index of the element
     * @param value The value to set to
     */
    operator fun set(index: Int, value: Float)

    override fun slice(index: Int): Floats =
        slice(index, size - index)

    override fun slice(index: Int, size: Int): Floats =
        prepareSlice(index, size, this, ::FloatsSlice)

    fun setFloat(index: Int, value: Float) = set(index, value)

    fun setFloats(index: Int, slice: FloatsRO) =
        slice.getFloats(0, slice(index, slice.size))

    override fun iterator(): ModifiableIterator<Float> =
        object : SliceModifiableIterator<Float>(size) {
            override fun access(index: Int) =
                get(index)

            override fun accessSet(index: Int, value: Float) =
                set(index, value)
        }
}

/**
 * 2-dimensional read-only array
 */
interface FloatsRO2 : Vars2 {
    /**
     * Returns the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @return The value at the given index
     */
    operator fun get(index1: Int, index2: Int): Float
}

/**
 * 2-dimensional read-write array
 */
interface Floats2 : FloatsRO2 {
    /**
     * Sets the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param value The value to set to
     */
    operator fun set(index1: Int, index2: Int, value: Float)
}

/**
 * 3-dimensional read-only array
 */
interface FloatsRO3 : Vars3 {
    /**
     * Returns the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param index3 Index on the third axis of the element
     * @return The value at the given index
     */
    operator fun get(index1: Int, index2: Int, index3: Int): Float
}

/**
 * 3-dimensional read-write array
 */
interface Floats3 : FloatsRO3 {
    /**
     * Sets the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param index3 Index on the third axis of the element
     * @param value The value to set to
     */
    operator fun set(index1: Int, index2: Int, index3: Int, value: Float)
}

internal open class FloatsROSlice(
    open val array: FloatsRO,
    final override val offset: Int,
    final override val size: Int
) : HeapArrayVarSlice<Float>, FloatsRO {
    final override fun get(index: Int): Float =
        array[index(offset, size, index)]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FloatsRO || size != other.size) return false
        for (i in 0 until size) {
            if (this[i] != other[i]) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var h = 1
        for (i in 0 until size) {
            h = h * 31 + this[i].primitiveHashCode()
        }
        return h
    }
}

internal class FloatsSlice(
    override val array: Floats,
    offset: Int,
    size: Int
) : FloatsROSlice(array, offset, size), Floats {
    override fun set(index: Int, value: Float) =
        array.set(index(offset, size, index), value)
}

/**
 * Slice of a normal heap array
 */
open class HeapFloats(
    val array: FloatArray,
    final override val offset: Int,
    final override val size: Int
) : HeapArrayVarSlice<Float>, Floats {
    override fun slice(index: Int): HeapFloats =
        slice(index, size - index)

    override fun slice(index: Int, size: Int): HeapFloats =
        prepareSlice(index, size, array, ::HeapFloats)

    final override fun get(index: Int): Float =
        array[index(offset, size, index)]

    final override fun set(index: Int, value: Float) =
        array.set(index(offset, size, index), value)

    final override fun getFloats(
        index: Int,
        slice: Floats
    ) {
        if (slice !is HeapFloats) return super.getFloats(index, slice)

        if (index < 0 || index + slice.size > size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        copy(array, slice.array, slice.size, index + this.offset, slice.offset)
    }

    final override fun setFloats(index: Int, slice: FloatsRO) {
        if (slice !is HeapFloats) return super.setFloats(index, slice)

        if (index < 0 || index + slice.size > size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        copy(slice.array, array, slice.size, slice.offset, index + this.offset)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FloatsRO || size != other.size) return false
        for (i in 0 until size) {
            if (this[i] != other[i]) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var h = 1
        for (i in 0 until size) {
            h = h * 31 + this[i].primitiveHashCode()
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
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun FloatArray.sliceOver(
    index: Int = 0,
    size: Int = this.size - index
): HeapFloats = HeapFloats(this, index, size)

/**
 * Exposes the contents of the slice in an array and calls [block] with
 * the array and beginning and end of the slice data in it
 *
 * **Note:** The array may or may not be a copy of the slice, so modifying it
 * is under no circumstances supported
 *
 * **Note:** The array *may* be used after returning out of the function
 *
 * **Note:** To improve performance the section containing the slice data
 * may be only a sub sequence of the array
 *
 * **Note:** This is a performance oriented function, so read those notes!
 * @param block Code to execute with the arrays contents
 * @receiver The slice to read
 * @return Return value of [block]
 */
inline fun <R> FloatsRO.readAsFloatArray(block: (FloatArray, Int, Int) -> R): R {
    val array: FloatArray
    val offset: Int
    when (this) {
        is HeapFloats -> {
            array = this.array
            offset = this.offset
        }
        else -> {
            array = toFloatArray()
            offset = 0
        }
    }
    return block(array, offset, size)
}

/**
 * Exposes the contents of the slice in an array and calls [block] with
 * the array and beginning and end of the slice data in it
 *
 * The last parameter is a callback to dispose the array (which will write
 * back data if needed)
 *
 * **Note:** The array may or may not be a copy of the slice, so reading
 * or modifying the original slice during this call can lead to surprising
 * results
 *
 * **Note:** The array *may* be used after returning out of the function,
 * but not after calling the close callback
 *
 * **Note:** To improve performance the section containing the slice data
 * may be only a sub sequence of the array
 *
 * **Note:** This is a performance oriented function, so read those notes!
 * @param block Code to execute with the arrays contents
 * @receiver The slice to read and modify
 * @return Return value of [block]
 */
inline fun <R> Floats.mutateAsFloatArray(block: (FloatArray, Int, Int, () -> Unit) -> R): R {
    val array: FloatArray
    val offset: Int
    val mapped = when (this) {
        is HeapFloats -> {
            array = this.array
            offset = this.offset
            true
        }
        else -> {
            array = toFloatArray()
            offset = 0
            false
        }
    }
    return block(array, offset, size) {
        if (!mapped) setFloats(0, array.sliceOver())
    }
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
inline fun <R> Floats.mutateAsFloatArray(block: (FloatArray, Int, Int) -> R): R =
    mutateAsFloatArray { array, offset, size, close ->
        try {
            block(array, offset, size)
        } finally {
            close()
        }
    }

/**
 * Exposes the contents of the slice in an array
 *
 * **Note:** The array may or may not be a copy of the slice, so modifying it
 * is under no circumstances supported
 *
 * **Note:** Consider using the function taking a lambda to avoid copies on
 * array slices
 * @receiver The slice to read
 * @return Array containing the data of the slice
 */
fun FloatsRO.readAsFloatArray(): FloatArray = when (this) {
    is HeapFloats ->
        if (size == array.size && offset == 0) array else {
            FloatArray(size)
                .also { copy(array, it, size, offset) }
        }
    else -> FloatArray(size) { getFloat(it) }
}

/**
 * Copies the contents of the slice into an array
 * @receiver The slice to copy
 * @return Array containing the data of the slice
 */
fun FloatsRO.toFloatArray(): FloatArray = when (this) {
    is HeapFloats -> FloatArray(size)
        .also { copy(array, it, size, offset) }
    else -> FloatArray(size) { getFloat(it) }
}

/**
 * Class wrapping an array to provide nicer support for 2-dimensional data.
 *
 * The layout for the dimensions is as follows:
 * `index = y * width + x`
 */
class FloatArray2(
    override val width: Int,
    override val height: Int,
    val array: FloatArray
) : Floats2,
    Iterable<Float> {
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
    fun getOrNull(index1: Int, index2: Int): Float? {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            return null
        }
        return array[index2 * width + index1]
    }

    override fun get(index1: Int, index2: Int): Float {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            throw IndexOutOfBoundsException("$index1 $index2")
        }
        return array[index2 * width + index1]
    }

    override fun set(index1: Int, index2: Int, value: Float) {
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
    fun copyOf() = FloatArray2(width, height, array.copyOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FloatsRO2
            || width != other.width || height != other.height) return false
        indices { x, y ->
            if (this[x, y] != other[x, y]) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var h = 1
        indices { x, y ->
            h = h * 31 + this[x, y].primitiveHashCode()
        }
        return h
    }
}

/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param init Returns values to be inserted by default
 * @return Wrapper around a new array
 */
inline fun FloatArray2(
    width: Int, height: Int,
    init: (Int, Int) -> Float
) = FloatArray2(width, height) { i ->
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
inline fun FloatArray2(
    width: Int, height: Int,
    init: (Int) -> Float
) = FloatArray2(
    width, height,
    FloatArray(width * height) { init(it) }
)

/**
 * Class wrapping an array to provide nicer support for 3-dimensional data.
 *
 * The layout for the dimensions is as follows:
 * `index = (z * height + y) * width + x`
 */
class FloatArray3(
    override val width: Int,
    override val height: Int,
    override val depth: Int,
    val array: FloatArray
) : Floats3,
    Iterable<Float> {
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
    fun getOrNull(index1: Int, index2: Int, index3: Int): Float? {
        if (index1 < 0 || index2 < 0 || index3 < 0
            || index1 >= width || index2 >= height || index3 >= depth) {
            return null
        }
        return array[(index3 * height + index2) * width + index1]
    }

    override fun get(index1: Int, index2: Int, index3: Int): Float {
        if (index1 < 0 || index2 < 0 || index3 < 0
            || index1 >= width || index2 >= height || index3 >= depth) {
            throw IndexOutOfBoundsException("$index1 $index2 $index3")
        }
        return array[(index3 * height + index2) * width + index1]
    }

    override fun set(index1: Int, index2: Int, index3: Int, value: Float) {
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
    fun copyOf() = FloatArray3(width, height, depth, array.copyOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FloatsRO3
            || width != other.width || height != other.height) return false
        indices { x, y, z ->
            if (this[x, y, z] != other[x, y, z]) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var h = 1
        indices { x, y, z ->
            h = h * 31 + this[x, y, z].primitiveHashCode()
        }
        return h
    }
}

/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param depth Depth of the wrapper
 * @param init Returns values to be inserted by default
 * @return Wrapper around a new array
 */
inline fun FloatArray3(
    width: Int, height: Int, depth: Int,
    init: (Int, Int, Int) -> Float
) = FloatArray3(width, height, depth) { i ->
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
inline fun FloatArray3(
    width: Int, height: Int, depth: Int,
    init: (Int) -> Float
) = FloatArray3(
    width, height, depth,
    FloatArray(width * height * depth) { init(it) }
)

/**
 * Calls the given [block] with all indices of the given wrapper.
 * @receiver The wrapper to iterate through
 * @param block Called with x index of the element
 */
inline fun FloatArray.indices(block: (Int) -> Unit) {
    for (x in indices) {
        block(x)
    }
}

/**
 * Fills the given array with values
 */
inline fun FloatArray.fill(supplier: (Int) -> Float) =
    indices { x -> this[x] = supplier(x) }

/**
 * Fills the given array with values
 */
inline fun Floats.fill(supplier: (Int) -> Float) =
    indices { x -> this[x] = supplier(x) }

/**
 * Fills the given array with values
 */
inline fun FloatArray2.fill(supplier: (Int, Int) -> Float) =
    indices { x, y -> this[x, y] = supplier(x, y) }

/**
 * Fills the given array with values
 */
inline fun FloatArray3.fill(supplier: (Int, Int, Int) -> Float) =
    indices { x, y, z -> this[x, y, z] = supplier(x, y, z) }

/**
 * Updates the value at the given index
 *
 * **Note:** This operation is not atomic
 */
inline fun FloatArray.change(
    index1: Int,
    update: (Float) -> Float
) {
    this[index1] = update(this[index1])
}

/**
 * Updates the value at the given index
 *
 * **Note:** This operation is not atomic
 */
inline fun Floats.change(
    index1: Int,
    update: (Float) -> Float
) {
    this[index1] = update(this[index1])
}

/**
 * Updates the value at the given indices
 *
 * **Note:** This operation is not atomic
 */
inline fun FloatArray2.change(
    index1: Int, index2: Int,
    update: (Float) -> Float
) {
    this[index1, index2] = update(this[index1, index2])
}

/**
 * Updates the value at the given indices
 *
 * **Note:** This operation is not atomic
 */
inline fun FloatArray3.change(
    index1: Int, index2: Int, index3: Int,
    update: (Float) -> Float
) {
    this[index1, index2, index3] = update(this[index1, index2, index3])
}

inline fun FloatArray2.shift(
    x: Int,
    y: Int,
    dispose: (Float, Int, Int) -> Unit,
    supplier: (Int, Int) -> Float
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
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @return Wrapper around a new array
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun FloatArray2(width: Int, height: Int) =
    FloatArray2(width, height, FloatArray(width * height))

/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param depth Depth of the wrapper
 * @return Wrapper around a new array
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun FloatArray3(width: Int, height: Int, depth: Int) =
    FloatArray3(width, height, depth, FloatArray(width * height * depth))
