#!/usr/bin/kotlinc -script
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

val isReference: Boolean
val type: String
val generic: String
val genericOut: String
val genericIn: String
val genericReified: String
val genericReifiedOut: String
val genericReifiedIn: String
val genericFun: String
val genericFunReified: String
val specialize: (String) -> String
val specializeOut: (String) -> String
val specializeIn: (String) -> String
val specializeAny: (String) -> String
val specializeName: (String) -> String
if (args.isEmpty()) {
    isReference = true
    type = "T"
    generic = "<$type>"
    genericOut = "<out $type>"
    genericIn = "<in $type>"
    genericReified = "<reified $type>"
    genericReifiedOut = "<reified out $type>"
    genericReifiedIn = "<reified in $type>"
    genericFun = "fun $generic"
    genericFunReified = "fun $genericReified"
    specialize = { "$it<$type>" }
    specializeOut = { "$it<out $type>" }
    specializeIn = { "$it<in $type>" }
    specializeAny = { "$it<*>" }
    specializeName = { it }
} else {
    isReference = false
    type = args[0]
    generic = ""
    genericOut = generic
    genericIn = generic
    genericReified = generic
    genericReifiedOut = generic
    genericReifiedIn = generic
    genericFun = "fun"
    genericFunReified = genericFun
    specialize = { "$type$it" }
    specializeOut = specialize
    specializeIn = specialize
    specializeAny = specialize
    specializeName = specialize
}

print("""@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils

/**
 * Class wrapping an array to provide nicer support for 2-dimensional data.
 *
 * The layout for the dimensions is as follows:
 * `index = y * width + x`
 */
class ${specialize("Array2")}
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
        private val array: ${specialize("Array")}) : Iterable<$type> {
    /**
     * Size of the array.
     */
    val size = width * height

    init {
        if (size != array.size) {
            throw IllegalArgumentException(
                    "Array has invalid size: ${'$'}{array.size} (should be ${'$'}size)")
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
                     y: Int): $type {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw IndexOutOfBoundsException("${'$'}x ${'$'}y")
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
                  y: Int): $type? {
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
                     value: $type) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw IndexOutOfBoundsException("${'$'}x ${'$'}y")
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
    fun copyOf() = ${specializeName("Array2")}(width, height, array.copyOf())
}

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x and y coords of the element
 */
inline fun ${specializeAny("Array2")}.indices(block: (Int, Int) -> Unit) {
    for (y in 0..height - 1) {
        for (x in 0..width - 1) {
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
inline $genericFunReified ${specializeName("Array2")}(width: Int,
                                                      height: Int,
                                                      init: (Int, Int) -> $type) =
        ${specializeName("Array2")}(width, height) { i ->
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
inline $genericFunReified ${specializeName("Array2")}(width: Int,
                                                      height: Int,
                                                      init: (Int) -> $type) =
        ${specializeName(
        "Array2")}(width, height, ${specializeName(
        "Array")}(width * height) { init(it) })

/**
 * Class wrapping an array to provide nicer support for 3-dimensional data.
 *
 * The layout for the dimensions is as follows:
 * `index = (z * height + y) * width + x`
 */
class ${specialize("Array3")}
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
        private val array: ${specialize("Array")}) : Iterable<$type> {
    /**
     * Size of the array.
     */
    val size = width * height * depth

    init {
        if (size != array.size) {
            throw IllegalArgumentException(
                    "Array has invalid size: ${'$'}{array.size} (should be ${'$'}size)")
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
                     z: Int): $type {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth) {
            throw IndexOutOfBoundsException("${'$'}x ${'$'}y ${'$'}z")
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
                  z: Int): $type? {
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
                     value: $type) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth) {
            throw IndexOutOfBoundsException("${'$'}x ${'$'}y ${'$'}z")
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
    fun copyOf() = ${specializeName(
        "Array3")}(width, height, depth, array.copyOf())
}

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x, y and z coords of the element
 */
inline fun ${specializeAny("Array3")}.indices(block: (Int, Int, Int) -> Unit) {
    for (z in 0..depth - 1) {
        for (y in 0..height - 1) {
            for (x in 0..width - 1) {
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
inline $genericFunReified ${specializeName("Array3")}(width: Int,
                                                      height: Int,
                                                      depth: Int,
                                                      init: (Int, Int, Int) -> $type) =
        ${specializeName("Array3")}(width, height, depth) { i ->
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
inline $genericFunReified ${specializeName("Array3")}(width: Int,
                                                      height: Int,
                                                      depth: Int,
                                                      init: (Int) -> $type) =
        ${specializeName(
        "Array3")}(width, height, depth, ${specializeName(
        "Array")}(width * height * depth) { init(it) })

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline $genericFun ${specializeIn("Array")}.fill(supplier: (Int) -> $type) {
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
inline $genericFun ${specializeIn(
        "Array2")}.fill(block: (Int, Int) -> $type) = indices { x, y ->
    this[x, y] = block(x, y)
}

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array and stores its return value in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x, y and z coords of the element
 */
inline $genericFun ${specializeIn(
        "Array3")}.fill(block: (Int, Int, Int) -> $type) = indices { x, y, z ->
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
inline $genericFun copy(src: ${specializeOut("Array")},
                        dest: ${specializeIn("Array")},
                        length: Int = src.size.coerceAtMost(dest.size),
                        offsetSrc: Int = 0,
                        offsetDest: Int = 0) =
        copyArray(src, dest, length, offsetSrc, offsetDest)

/**
 * Copy data from the [src] array to [dest]
 * @param src The array to copy from
 * @param dest The array to copy to
 * @param length The amount of elements to copy
 * @param offsetSrc Offset in the source array
 * @param offsetDest Offset in the destination array
 */
header $genericFun copyArray(src: ${specializeOut("Array")},
                             dest: ${specializeIn("Array")},
                             length: Int,
                             offsetSrc: Int,
                             offsetDest: Int)
""")

if (isReference) {
    print("""
/**
 * Creates a new array and makes it accessible using a wrapper initialized with
 * nulls
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @return Wrapper around a new array
 */
inline $genericFunReified array2OfNulls(width: Int,
                                        height: Int) =
        ${specializeName(
            "Array2")}(width, height, arrayOfNulls$generic(width * height))

/**
 * Creates a new array and makes it accessible using a wrapper initialized with
 * nulls
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param depth Depth of the wrapper
 * @return Wrapper around a new array
 */
inline $genericFunReified array3OfNulls(width: Int,
                                        height: Int,
                                        depth: Int) =
        ${specializeName(
            "Array3")}(width, height, depth, arrayOfNulls$generic(width * height))
""")
} else {
    print("""
/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @return Wrapper around a new array
 */
inline $genericFunReified ${specializeName("Array2")}(width: Int,
                                                      height: Int) =
        ${specializeName("Array2")}(width, height, ${specialize(
            "Array")}(width * height))

/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param depth Depth of the wrapper
 * @return Wrapper around a new array
 */
inline $genericFunReified ${specializeName("Array3")}(width: Int,
                                                      height: Int,
                                                      depth: Int) =
        ${specializeName(
            "Array3")}(width, height, depth, ${specialize(
            "Array")}(width * height * depth))
""")
}
