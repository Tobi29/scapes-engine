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

package org.tobi29.arrays

import org.tobi29.stdex.InlineUtility
import org.tobi29.stdex.copy
import org.tobi29.stdex.primitiveHashCode

/**
 * 1-dimensional read-only array
 */
interface LongsRO : VarsIterable<Long> {
    /**
     * Returns the element at the given index in the array
     * @param index Index of the element
     * @return The value at the given index
     */
    operator fun get(index: Int): Long

    override fun slice(index: Int): LongsRO =
        slice(index, size - index)

    override fun slice(index: Int, size: Int): LongsRO =
        prepareSlice(index, size, this, ::LongsROSlice)

    fun getLong(index: Int): Long = get(index)

    fun getLongs(index: Int, slice: Longs) {
        var j = index
        for (i in 0 until slice.size) {
            slice[i] = this[j++]
        }
    }

    override fun iterator(): Iterator<Long> =
        object : SliceIterator<Long>(size) {
            override fun access(index: Int) = get(index)
        }
}

/**
 * 1-dimensional read-write array
 */
interface Longs : LongsRO, VarsModifiableIterable<Long> {
    /**
     * Sets the element at the given index in the array
     * @param index Index of the element
     * @param value The value to set to
     */
    operator fun set(index: Int, value: Long)

    override fun slice(index: Int): Longs =
        slice(index, size - index)

    override fun slice(index: Int, size: Int): Longs =
        prepareSlice(index, size, this, ::LongsSlice)

    fun setLong(index: Int, value: Long) = set(index, value)

    fun setLongs(index: Int, slice: LongsRO) =
        slice.getLongs(0, slice(index, slice.size))

    override fun iterator(): ModifiableIterator<Long> =
        object : SliceModifiableIterator<Long>(size) {
            override fun access(index: Int) =
                get(index)

            override fun accessSet(index: Int, value: Long) =
                set(index, value)
        }
}

/**
 * 2-dimensional read-only array
 */
interface LongsRO2 : Vars2 {
    /**
     * Returns the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @return The value at the given index
     */
    operator fun get(index1: Int, index2: Int): Long
}

/**
 * 2-dimensional read-write array
 */
interface Longs2 : LongsRO2 {
    /**
     * Sets the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param value The value to set to
     */
    operator fun set(index1: Int, index2: Int, value: Long)
}

/**
 * 3-dimensional read-only array
 */
interface LongsRO3 : Vars3 {
    /**
     * Returns the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param index3 Index on the third axis of the element
     * @return The value at the given index
     */
    operator fun get(index1: Int, index2: Int, index3: Int): Long
}

/**
 * 3-dimensional read-write array
 */
interface Longs3 : LongsRO3 {
    /**
     * Sets the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param index3 Index on the third axis of the element
     * @param value The value to set to
     */
    operator fun set(index1: Int, index2: Int, index3: Int, value: Long)
}

internal open class LongsROSlice(
    open val array: LongsRO,
    final override val offset: Int,
    final override val size: Int
) : HeapArrayVarSlice<Long>, LongsRO {
    final override fun get(index: Int): Long =
        array[index(offset, size, index)]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LongsRO || size != other.size) return false
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

internal class LongsSlice(
    override val array: Longs,
    offset: Int,
    size: Int
) : LongsROSlice(array, offset, size), Longs {
    override fun set(index: Int, value: Long) =
        array.set(index(offset, size, index), value)
}

/**
 * Slice of a normal heap array
 */
open class HeapLongs(
    val array: LongArray,
    final override val offset: Int,
    final override val size: Int
) : HeapArrayVarSlice<Long>, Longs {
    override fun slice(index: Int): HeapLongs =
        slice(index, size - index)

    override fun slice(index: Int, size: Int): HeapLongs =
        prepareSlice(index, size, array, ::HeapLongs)

    final override fun get(index: Int): Long =
        array[index(offset, size, index)]

    final override fun set(index: Int, value: Long) =
        array.set(index(offset, size, index), value)

    final override fun getLongs(
        index: Int,
        slice: Longs
    ) {
        if (slice !is HeapLongs) return super.getLongs(index, slice)

        if (index < 0 || index + slice.size > size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        copy(array, slice.array, slice.size, index + this.offset, slice.offset)
    }

    final override fun setLongs(index: Int, slice: LongsRO) {
        if (slice !is HeapLongs) return super.setLongs(index, slice)

        if (index < 0 || index + slice.size > size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        copy(slice.array, array, slice.size, slice.offset, index + this.offset)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LongsRO || size != other.size) return false
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
inline fun LongArray.sliceOver(
    index: Int = 0,
    size: Int = this.size - index
): HeapLongs = HeapLongs(this, index, size)

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
inline fun <R> LongsRO.readAsLongArray(block: (LongArray, Int, Int) -> R): R {
    val array: LongArray
    val offset: Int
    when (this) {
        is HeapLongs -> {
            array = this.array
            offset = this.offset
        }
        else -> {
            array = toLongArray()
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
inline fun <R> Longs.mutateAsLongArray(block: (LongArray, Int, Int) -> R): R {
    val array: LongArray
    val offset: Int
    val mapped = when (this) {
        is HeapLongs -> {
            array = this.array
            offset = this.offset
            true
        }
        else -> {
            array = toLongArray()
            offset = 0
            false
        }
    }
    return try {
        block(array, offset, size)
    } finally {
        if (!mapped) setLongs(0, array.sliceOver())
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
fun LongsRO.readAsLongArray(): LongArray = when (this) {
    is HeapLongs ->
        if (size == array.size && offset == 0) array else {
            LongArray(size)
                .also { copy(array, it, size, offset) }
        }
    else -> LongArray(size) { getLong(it) }
}

/**
 * Copies the contents of the slice into an array
 * @receiver The slice to copy
 * @return Array containing the data of the slice
 */
fun LongsRO.toLongArray(): LongArray = when (this) {
    is HeapLongs -> LongArray(size)
        .also { copy(array, it, size, offset) }
    else -> LongArray(size) { getLong(it) }
}

/**
 * Class wrapping an array to provide nicer support for 2-dimensional data.
 *
 * The layout for the dimensions is as follows:
 * `index = y * width + x`
 */
class LongArray2(
    override val width: Int,
    override val height: Int,
    val array: LongArray
) : Longs2,
    Iterable<Long> {
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
    fun getOrNull(index1: Int, index2: Int): Long? {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            return null
        }
        return array[index2 * width + index1]
    }

    override fun get(index1: Int, index2: Int): Long {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            throw IndexOutOfBoundsException("$index1 $index2")
        }
        return array[index2 * width + index1]
    }

    override fun set(index1: Int, index2: Int, value: Long) {
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
    fun copyOf() = LongArray2(width, height, array.copyOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LongsRO2
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
inline fun LongArray2(
    width: Int, height: Int,
    init: (Int, Int) -> Long
) = LongArray2(width, height) { i ->
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
inline fun LongArray2(
    width: Int, height: Int,
    init: (Int) -> Long
) = LongArray2(
    width, height,
    LongArray(width * height) { init(it) }
)

/**
 * Class wrapping an array to provide nicer support for 3-dimensional data.
 *
 * The layout for the dimensions is as follows:
 * `index = (z * height + y) * width + x`
 */
class LongArray3(
    override val width: Int,
    override val height: Int,
    override val depth: Int,
    val array: LongArray
) : Longs3,
    Iterable<Long> {
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
    fun getOrNull(index1: Int, index2: Int, index3: Int): Long? {
        if (index1 < 0 || index2 < 0 || index3 < 0
            || index1 >= width || index2 >= height || index3 >= depth) {
            return null
        }
        return array[(index3 * height + index2) * width + index1]
    }

    override fun get(index1: Int, index2: Int, index3: Int): Long {
        if (index1 < 0 || index2 < 0 || index3 < 0
            || index1 >= width || index2 >= height || index3 >= depth) {
            throw IndexOutOfBoundsException("$index1 $index2 $index3")
        }
        return array[(index3 * height + index2) * width + index1]
    }

    override fun set(index1: Int, index2: Int, index3: Int, value: Long) {
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
    fun copyOf() = LongArray3(width, height, depth, array.copyOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LongsRO3
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
inline fun LongArray3(
    width: Int, height: Int, depth: Int,
    init: (Int, Int, Int) -> Long
) = LongArray3(width, height, depth) { i ->
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
inline fun LongArray3(
    width: Int, height: Int, depth: Int,
    init: (Int) -> Long
) = LongArray3(
    width, height, depth,
    LongArray(width * height * depth) { init(it) }
)

/**
 * Calls the given [block] with all indices of the given wrapper.
 * @receiver The wrapper to iterate through
 * @param block Called with x index of the element
 */
inline fun LongArray.indices(block: (Int) -> Unit) {
    for (x in indices) {
        block(x)
    }
}

/**
 * Fills the given array with values
 */
inline fun LongArray.fill(supplier: (Int) -> Long) =
    indices { x -> this[x] = supplier(x) }

/**
 * Fills the given array with values
 */
inline fun Longs.fill(supplier: (Int) -> Long) =
    indices { x -> this[x] = supplier(x) }

/**
 * Fills the given array with values
 */
inline fun LongArray2.fill(supplier: (Int, Int) -> Long) =
    indices { x, y -> this[x, y] = supplier(x, y) }

/**
 * Fills the given array with values
 */
inline fun LongArray3.fill(supplier: (Int, Int, Int) -> Long) =
    indices { x, y, z -> this[x, y, z] = supplier(x, y, z) }

/**
 * Updates the value at the given index
 *
 * **Note:** This operation is not atomic
 */
inline fun LongArray.change(
    index1: Int,
    update: (Long) -> Long
) {
    this[index1] = update(this[index1])
}

/**
 * Updates the value at the given index
 *
 * **Note:** This operation is not atomic
 */
inline fun Longs.change(
    index1: Int,
    update: (Long) -> Long
) {
    this[index1] = update(this[index1])
}

/**
 * Updates the value at the given indices
 *
 * **Note:** This operation is not atomic
 */
inline fun LongArray2.change(
    index1: Int, index2: Int,
    update: (Long) -> Long
) {
    this[index1, index2] = update(this[index1, index2])
}

/**
 * Updates the value at the given indices
 *
 * **Note:** This operation is not atomic
 */
inline fun LongArray3.change(
    index1: Int, index2: Int, index3: Int,
    update: (Long) -> Long
) {
    this[index1, index2, index3] = update(this[index1, index2, index3])
}

inline fun LongArray2.shift(
    x: Int,
    y: Int,
    dispose: (Long, Int, Int) -> Unit,
    supplier: (Int, Int) -> Long
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
inline fun LongArray2(width: Int, height: Int) =
    LongArray2(width, height, LongArray(width * height))

/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param depth Depth of the wrapper
 * @return Wrapper around a new array
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun LongArray3(width: Int, height: Int, depth: Int) =
    LongArray3(width, height, depth, LongArray(width * height * depth))
