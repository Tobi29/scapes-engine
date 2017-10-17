// GENERATED FILE, DO NOT EDIT DIRECTLY!!!
// Generation script can be found in `resources/codegen/GenArrays.kts`.
// Run `resources/codegen/codegen.sh` to update sources.

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils

/**
 * Read-only slice of an array, indexed in elements
 */
interface BooleanArraySliceRO : ArrayVarSlice<Boolean> {
    override fun slice(index: Int,
                       size: Int): BooleanArraySliceRO

    /**
     * Returns the element at the given index in the slice
     * @param index Index of the element
     * @return The value at the given index
     */
    operator fun get(index: Int): Boolean

    fun getBoolean(index: Int): Boolean = get(index)

    fun getBooleans(index: Int,
                    slice: BooleanArraySlice) {
        var j = index
        for (i in 0 until slice.size) {
            slice.set(i, get(j++))
        }
    }

    override fun iterator(): Iterator<Boolean> =
            object : SliceIterator<Boolean>(size) {
                override fun access(index: Int) = get(index)
            }
}

/**
 * Slice of an array, indexed in elements
 */
interface BooleanArraySlice : BooleanArraySliceRO {
    override fun slice(index: Int,
                       size: Int): BooleanArraySlice

    /**
     * Sets the element at the given index in the slice
     * @param index Index of the element
     * @param value The value to set to
     */
    operator fun set(index: Int,
                     value: Boolean)

    fun setBoolean(index: Int,
                   value: Boolean) = set(index, value)

    fun setBooleans(index: Int,
                    slice: BooleanArraySliceRO) =
            slice.getBooleans(0, slice(index, slice.size))
}

/**
 * Slice of a normal heap array
 */
interface HeapBooleanArraySlice : HeapArrayVarSlice<Boolean>, BooleanArraySlice {
    val array: BooleanArray
    override fun slice(index: Int,
                       size: Int): HeapBooleanArraySlice

    override fun get(index: Int): Boolean = array[index(index)]
    override fun set(index: Int,
                     value: Boolean) = array.set(index(index), value)

    override fun getBooleans(index: Int,
                             slice: BooleanArraySlice) {
        if (slice !is HeapBooleanArraySlice) return super.getBooleans(index,
                slice)

        if (index < 0 || index + slice.size > size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        copy(array, slice.array, slice.size, index + this.offset, slice.offset)
    }

    override fun setBooleans(index: Int,
                             slice: BooleanArraySliceRO) {
        if (slice !is HeapBooleanArraySlice) return super.setBooleans(index,
                slice)

        if (index < 0 || index + slice.size > size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        copy(slice.array, array, slice.size, slice.offset, index + this.offset)
    }
}

fun HeapBooleanArraySlice(
        array: BooleanArray,
        offset: Int,
        size: Int
): HeapBooleanArraySlice = HeapBooleanArraySliceImpl(array, offset, size)

private class HeapBooleanArraySliceImpl(
        override val array: BooleanArray,
        override val offset: Int,
        override val size: Int
) : HeapBooleanArraySlice {
    override fun slice(index: Int,
                       size: Int): HeapBooleanArraySlice =
            prepareSlice(index, size, array,
                    ::HeapBooleanArraySlice)
}

/**
 * Creates a slice from the given array, which holds the array itself as backing
 * storage
 * @param index Index to start the slice at
 * @param size Amount of elements in slice
 * @receiver The array to create a slice of
 * @return A slice from the given array
 */
inline fun BooleanArray.sliceOver(
        index: Int = 0,
        size: Int = this.size - index
): HeapBooleanArraySlice = HeapBooleanArraySlice(this, index, size)

/**
 * Class wrapping an array to provide nicer support for 2-dimensional data.
 *
 * The layout for the dimensions is as follows:
 * `index = y * width + x`
 */
class BooleanArray2
/**
 * Creates a new wrapper around the given array.
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param array Array for storing elements
 */
(
        /**
         * Width of the wrapper.
         */
        val width: Int,
        /**
         * Height of the wrapper.
         */
        val height: Int,
        private val array: BooleanArray) : Iterable<Boolean> {
    /**
     * Size of the array.
     */
    val size = width * height

    init {
        if (size != array.size) {
            throw IllegalArgumentException(
                    "Array has invalid size: ${array.size} (should be $size)")
        }
    }

    /**
     * Retrieve an element from the array.
     * @param x Index on the first axis
     * @param y Index on the second axis
     * @throws IndexOutOfBoundsException When accessing indices out of bounds
     * @return The element at the given position
     */
    operator fun get(x: Int,
                     y: Int): Boolean {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw IndexOutOfBoundsException("$x $y")
        }
        return array[y * width + x]
    }

    /**
     * Retrieve an element from the array or `null` if out of bounds.
     * @param x Index on the first axis
     * @param y Index on the second axis
     * @return The element at the given position or `null` if out of bounds
     */
    fun getOrNull(x: Int,
                  y: Int): Boolean? {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return null
        }
        return array[y * width + x]
    }

    /**
     * Stores an element in the array.
     * @param x Index on the first axis
     * @param y Index on the second axis
     * @param value The element to store
     */
    operator fun set(x: Int,
                     y: Int,
                     value: Boolean) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw IndexOutOfBoundsException("$x $y")
        }
        array[y * width + x] = value
    }

    /**
     * Creates an iterator for iterating over the elements of the array.
     */
    override operator fun iterator() = array.iterator()

    /**
     * Makes a shallow copy of the array and wrapper.
     * @return A new wrapper around a new array
     */
    fun copyOf() = BooleanArray2(width, height, array.copyOf())
}

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x and y coords of the element
 */
inline fun BooleanArray2.indices(block: (Int, Int) -> Unit) {
    for (y in 0 until height) {
        for (x in 0 until width) {
            block(x, y)
        }
    }
}

/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param init Returns values to be inserted by default
 * @return Wrapper around a new array
 */
inline fun BooleanArray2(width: Int,
                         height: Int,
                         init: (Int, Int) -> Boolean) =
        BooleanArray2(width, height) { i ->
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
inline fun BooleanArray2(width: Int,
                         height: Int,
                         init: (Int) -> Boolean) =
        BooleanArray2(width, height, BooleanArray(width * height) { init(it) })

/**
 * Class wrapping an array to provide nicer support for 3-dimensional data.
 *
 * The layout for the dimensions is as follows:
 * `index = (z * height + y) * width + x`
 */
class BooleanArray3
/**
 * Creates a new wrapper around the given array.
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param depth Depth of the wrapper
 * @param array Array for storing elements
 */
(
        /**
         * Width of the wrapper.
         */
        val width: Int,
        /**
         * Height of the wrapper.
         */
        val height: Int,
        /**
         * Depth of the wrapper.
         */
        val depth: Int,
        private val array: BooleanArray) : Iterable<Boolean> {
    /**
     * Size of the array.
     */
    val size = width * height * depth

    init {
        if (size != array.size) {
            throw IllegalArgumentException(
                    "Array has invalid size: ${array.size} (should be $size)")
        }
    }

    /**
     * Retrieve an element from the array.
     * @param x Index on the first axis
     * @param y Index on the second axis
     * @param z Index on the third axis
     * @throws IndexOutOfBoundsException When accessing indices out of bounds
     * @return The element at the given position
     */
    operator fun get(x: Int,
                     y: Int,
                     z: Int): Boolean {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth) {
            throw IndexOutOfBoundsException("$x $y $z")
        }
        return array[(z * height + y) * width + x]
    }

    /**
     * Retrieve an element from the array or `null` if out of bounds.
     * @param x Index on the first axis
     * @param y Index on the second axis
     * @param z Index on the third axis
     * @return The element at the given position or `null` if out of bounds
     */
    fun getOrNull(x: Int,
                  y: Int,
                  z: Int): Boolean? {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth) {
            return null
        }
        return array[(z * height + y) * width + x]
    }

    /**
     * Stores an element in the array.
     * @param x Index on the first axis
     * @param y Index on the second axis
     * @param z Index on the third axis
     * @param value The element to store
     */
    operator fun set(x: Int,
                     y: Int,
                     z: Int,
                     value: Boolean) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth) {
            throw IndexOutOfBoundsException("$x $y $z")
        }
        array[(z * height + y) * width + x] = value
    }

    /**
     * Creates an iterator for iterating over the elements of the array.
     */
    override operator fun iterator() = array.iterator()

    /**
     * Makes a shallow copy of the array and wrapper.
     * @return A new wrapper around a new array
     */
    fun copyOf() = BooleanArray3(width, height, depth, array.copyOf())
}

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x, y and z coords of the element
 */
inline fun BooleanArray3.indices(block: (Int, Int, Int) -> Unit) {
    for (z in 0 until depth) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                block(x, y, z)
            }
        }
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
inline fun BooleanArray3(width: Int,
                         height: Int,
                         depth: Int,
                         init: (Int, Int, Int) -> Boolean) =
        BooleanArray3(width, height, depth) { i ->
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
inline fun BooleanArray3(width: Int,
                         height: Int,
                         depth: Int,
                         init: (Int) -> Boolean) =
        BooleanArray3(width, height, depth,
                BooleanArray(width * height * depth) { init(it) })

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun BooleanArray.fill(supplier: (Int) -> Boolean) {
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
inline fun BooleanArray2.fill(block: (Int, Int) -> Boolean) = indices { x, y ->
    this[x, y] = block(x, y)
}

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array and stores its return value in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x, y and z coords of the element
 */
inline fun BooleanArray3.fill(block: (Int, Int, Int) -> Boolean) = indices { x, y, z ->
    this[x, y, z] = block(x, y, z)
}

/**
 * Copy data from the [src] array to [dest]
 * @param src The array to copy from
 * @param dest The array to copy to
 * @param length The amount of elements to copy
 * @param offsetSrc Offset in the source array
 * @param offsetDest Offset in the destination array
 */
inline fun copy(src: BooleanArray,
                dest: BooleanArray,
                length: Int = src.size.coerceAtMost(dest.size),
                offsetSrc: Int = 0,
                offsetDest: Int = 0) =
        copyArray(src, dest, length, offsetSrc, offsetDest)

/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @return Wrapper around a new array
 */
inline fun BooleanArray2(width: Int,
                         height: Int) =
        BooleanArray2(width, height, BooleanArray(width * height))

/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param depth Depth of the wrapper
 * @return Wrapper around a new array
 */
inline fun BooleanArray3(width: Int,
                         height: Int,
                         depth: Int) =
        BooleanArray3(width, height, depth,
                BooleanArray(width * height * depth))
