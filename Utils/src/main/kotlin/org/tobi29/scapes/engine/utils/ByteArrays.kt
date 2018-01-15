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

// GENERATED FILE, DO NOT EDIT DIRECTLY!!!
// Generation script can be found in `resources/codegen/GenArrays.kts`.
// Run `resources/codegen/codegen.sh` to update sources.

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils

/**
 * 1-dimensional read-only array
 */
interface BytesRO : Vars {
    /**
     * Returns the element at the given index in the array
     * @param index Index of the element
     * @return The value at the given index
     */
    operator fun get(index: Int): Byte
}

/**
 * 1-dimensional read-write array
 */
interface Bytes : BytesRO {
    /**
     * Sets the element at the given index in the array
     * @param index Index of the element
     * @param value The value to set to
     */
    operator fun set(index: Int,
                     value: Byte)
}

/**
 * 2-dimensional read-only array
 */
interface BytesRO2 : Vars2 {
    /**
     * Returns the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @return The value at the given index
     */
    operator fun get(index1: Int,
                     index2: Int): Byte
}

/**
 * 2-dimensional read-write array
 */
interface Bytes2 : BytesRO2 {
    /**
     * Sets the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param value The value to set to
     */
    operator fun set(index1: Int,
                     index2: Int,
                     value: Byte)
}

/**
 * 3-dimensional read-only array
 */
interface BytesRO3 : Vars3 {
    /**
     * Returns the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param index3 Index on the third axis of the element
     * @return The value at the given index
     */
    operator fun get(index1: Int,
                     index2: Int,
                     index3: Int): Byte
}

/**
 * 3-dimensional read-write array
 */
interface Bytes3 : BytesRO3 {
    /**
     * Sets the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param index3 Index on the third axis of the element
     * @param value The value to set to
     */
    operator fun set(index1: Int,
                     index2: Int,
                     index3: Int,
                     value: Byte)
}

/**
 * Read-only slice of an array, indexed in elements
 */
interface ByteArraySliceRO : BytesRO,
        ArrayVarSlice<Byte> {
    override fun slice(index: Int): ByteArraySliceRO

    override fun slice(index: Int,
                       size: Int): ByteArraySliceRO


    fun getByte(index: Int): Byte = get(index)

    fun getBytes(index: Int,
                 slice: ByteArraySlice) {
        var j = index
        for (i in 0 until slice.size) {
            slice.set(i, get(j++))
        }
    }

    override fun iterator(): Iterator<Byte> =
            object : SliceIterator<Byte>(size) {
                override fun access(index: Int) = get(index)
            }
}

/**
 * Slice of an array, indexed in elements
 */
interface ByteArraySlice : Bytes,
        ByteArraySliceRO {
    override fun slice(index: Int): ByteArraySlice

    override fun slice(index: Int,
                       size: Int): ByteArraySlice


    fun setByte(index: Int,
                value: Byte) = set(index, value)

    fun setBytes(index: Int,
                 slice: ByteArraySliceRO) =
            slice.getBytes(0, slice(index, slice.size))
}

/**
 * Slice of a normal heap array
 */
open class HeapByteArraySlice(
        val array: ByteArray,
        override final val offset: Int,
        override final val size: Int
) : HeapArrayVarSlice<Byte>,
        ByteArraySlice {
    override fun slice(index: Int): HeapByteArraySlice =
            slice(index, size - index)

    override fun slice(index: Int,
                       size: Int): HeapByteArraySlice =
            prepareSlice(index, size, array,
                    ::HeapByteArraySlice)

    override final fun get(index: Int): Byte = array[index(offset, size, index)]
    override final fun set(index: Int,
                           value: Byte) = array.set(index(offset, size, index),
            value)

    override final fun getBytes(index: Int,
                                slice: ByteArraySlice) {
        if (slice !is HeapByteArraySlice) return super.getBytes(index, slice)

        if (index < 0 || index + slice.size > size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        copy(array, slice.array, slice.size, index + this.offset, slice.offset)
    }

    override final fun setBytes(index: Int,
                                slice: ByteArraySliceRO) {
        if (slice !is HeapByteArraySlice) return super.setBytes(index, slice)

        if (index < 0 || index + slice.size > size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        copy(slice.array, array, slice.size, slice.offset, index + this.offset)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ByteArraySliceRO) return false
        for (i in 0 until size) {
            if (this[i] != other[i]) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var h = 1
        for (i in 0 until size) {
            h = h * 31 + this[i].hashCodePrimitive()
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
inline fun ByteArray.sliceOver(
        index: Int = 0,
        size: Int = this.size - index
): HeapByteArraySlice = HeapByteArraySlice(this, index, size)

/**
 * Class wrapping an array to provide nicer support for 2-dimensional data.
 *
 * The layout for the dimensions is as follows:
 * `index = y * width + x`
 */
class ByteArray2
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
        override val width: Int,
        /**
         * Height of the wrapper.
         */
        override val height: Int,
        private val array: ByteArray) : Bytes2,
        Iterable<Byte> {
    init {
        if (size != array.size) {
            throw IllegalArgumentException(
                    "Array has invalid size: ${array.size} (should be $size)")
        }
    }

    /**
     * Retrieve an element from the array or `null` if out of bounds.
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @return The element at the given position or `null` if out of bounds
     */
    fun getOrNull(index1: Int,
                  index2: Int): Byte? {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            return null
        }
        return array[index2 * width + index1]
    }

    override fun get(index1: Int,
                     index2: Int): Byte {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            throw IndexOutOfBoundsException("$index1 $index2")
        }
        return array[index2 * width + index1]
    }

    override fun set(index1: Int,
                     index2: Int,
                     value: Byte) {
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
    fun copyOf() = ByteArray2(width, height, array.copyOf())
}

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x and y coords of the element
 */
inline fun ByteArray2.indices(block: (Int, Int) -> Unit) {
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
inline fun ByteArray2(width: Int,
                      height: Int,
                      init: (Int, Int) -> Byte) =
        ByteArray2(width, height) { i ->
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
inline fun ByteArray2(width: Int,
                      height: Int,
                      init: (Int) -> Byte) =
        ByteArray2(width, height, ByteArray(width * height) { init(it) })

/**
 * Class wrapping an array to provide nicer support for 3-dimensional data.
 *
 * The layout for the dimensions is as follows:
 * `index = (z * height + y) * width + x`
 */
class ByteArray3
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
        override val width: Int,
        /**
         * Height of the wrapper.
         */
        override val height: Int,
        /**
         * Depth of the wrapper.
         */
        override val depth: Int,
        private val array: ByteArray) : Bytes3,
        Iterable<Byte> {
    init {
        if (size != array.size) {
            throw IllegalArgumentException(
                    "Array has invalid size: ${array.size} (should be $size)")
        }
    }

    /**
     * Retrieve an element from the array or `null` if out of bounds.
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param index3 Index on the third axis of the element
     * @return The element at the given position or `null` if out of bounds
     */
    fun getOrNull(index1: Int,
                  index2: Int,
                  index3: Int): Byte? {
        if (index1 < 0 || index2 < 0 || index3 < 0
                || index1 >= width || index2 >= height || index3 >= depth) {
            return null
        }
        return array[(index3 * height + index2) * width + index1]
    }

    override fun get(index1: Int,
                     index2: Int,
                     index3: Int): Byte {
        if (index1 < 0 || index2 < 0 || index3 < 0
                || index1 >= width || index2 >= height || index3 >= depth) {
            throw IndexOutOfBoundsException("$index1 $index2 $index3")
        }
        return array[(index3 * height + index2) * width + index1]
    }

    override fun set(index1: Int,
                     index2: Int,
                     index3: Int,
                     value: Byte) {
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
    fun copyOf() = ByteArray3(width, height, depth, array.copyOf())
}

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x, y and z coords of the element
 */
inline fun ByteArray3.indices(block: (Int, Int, Int) -> Unit) {
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
inline fun ByteArray3(width: Int,
                      height: Int,
                      depth: Int,
                      init: (Int, Int, Int) -> Byte) =
        ByteArray3(width, height, depth) { i ->
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
inline fun ByteArray3(width: Int,
                      height: Int,
                      depth: Int,
                      init: (Int) -> Byte) =
        ByteArray3(width, height, depth,
                ByteArray(width * height * depth) { init(it) })

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun ByteArray.fill(supplier: (Int) -> Byte) {
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
inline fun ByteArray2.fill(block: (Int, Int) -> Byte) = indices { x, y ->
    this[x, y] = block(x, y)
}

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array and stores its return value in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x, y and z coords of the element
 */
inline fun ByteArray3.fill(block: (Int, Int, Int) -> Byte) = indices { x, y, z ->
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
inline fun copy(src: ByteArray,
                dest: ByteArray,
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
inline fun ByteArray2(width: Int,
                      height: Int) =
        ByteArray2(width, height, ByteArray(width * height))

/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param depth Depth of the wrapper
 * @return Wrapper around a new array
 */
inline fun ByteArray3(width: Int,
                      height: Int,
                      depth: Int) =
        ByteArray3(width, height, depth, ByteArray(width * height * depth))
