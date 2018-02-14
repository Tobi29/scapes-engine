#!/usr/bin/kotlinc -script
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

val isReference = args.isEmpty()
val type =
    if (args.isEmpty()) "T" else args[0]

fun rawGeneric(vararg elements: String) =
    if (elements.isEmpty()) ""
    else elements.joinToString(", ", "<", ">")

fun appendedRawGeneric(vararg elements: String) =
    if (elements.isEmpty()) ""
    else " ${rawGeneric(*elements)}"

fun generic(vararg others: String) =
    rawGeneric(
        *(if (args.isEmpty()) arrayOf(type) else emptyArray()),
        *others
    )

fun appendedGeneric(vararg others: String) =
    appendedRawGeneric(
        *(if (args.isEmpty()) arrayOf(type) else emptyArray()),
        *others
    )

fun genericOut(vararg others: String) =
    rawGeneric(
        *(if (args.isEmpty()) arrayOf("out $type") else emptyArray()),
        *others
    )

fun appendedGenericOut(vararg others: String) =
    appendedRawGeneric(
        *(if (args.isEmpty()) arrayOf("out $type") else emptyArray()),
        *others
    )

fun genericIn(vararg others: String) =
    rawGeneric(
        *(if (args.isEmpty()) arrayOf("in $type") else emptyArray()),
        *others
    )

fun appendedGenericIn(vararg others: String) =
    appendedRawGeneric(
        *(if (args.isEmpty()) arrayOf("in $type") else emptyArray()),
        *others
    )

fun genericReified(vararg others: String) =
    rawGeneric(
        *(if (args.isEmpty()) arrayOf("reified $type") else emptyArray()),
        *others
    )

fun appendedGenericReified(vararg others: String) =
    appendedRawGeneric(
        *(if (args.isEmpty()) arrayOf("reified $type") else emptyArray()),
        *others
    )

fun genericReifiedOut(vararg others: String) =
    rawGeneric(
        *(if (args.isEmpty()) arrayOf("reified out $type") else emptyArray()),
        *others
    )

fun appendedGenericReifiedOut(vararg others: String) =
    appendedRawGeneric(
        *(if (args.isEmpty()) arrayOf("reified out $type") else emptyArray()),
        *others
    )

fun genericReifiedIn(vararg others: String) =
    rawGeneric(
        *(if (args.isEmpty()) arrayOf("reified in $type") else emptyArray()),
        *others
    )

fun appendedGenericReifiedIn(vararg others: String) =
    appendedRawGeneric(
        *(if (args.isEmpty()) arrayOf("reified in $type") else emptyArray()),
        *others
    )

val generic = generic()
val genericOut = genericOut()
val genericIn = genericIn()
val genericReified = genericReified()
val genericReifiedOut = genericReifiedOut()
val genericReifiedIn = genericReifiedIn()

val genericFun = "fun${appendedGeneric()}"
val genericFunReified = "fun${appendedGenericReified()}"
val specialize: (String) -> String =
    if (args.isEmpty()) {
        { "$it<$type>" }
    } else {
        { "$type$it" }
    }
val specializeOut: (String) -> String =
    if (args.isEmpty()) {
        { "$it<out $type>" }
    } else specialize
val specializeIn: (String) -> String =
    if (args.isEmpty()) {
        { "$it<in $type>" }
    } else specialize
val specializeAny: (String) -> String =
    if (args.isEmpty()) {
        { "$it<*>" }
    } else specialize
val specializeName: (String) -> String =
    if (args.isEmpty()) {
        { it }
    } else specialize

print(
    """/*
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
${if (!isReference) "import org.tobi29.stdex.primitiveHashCode\n"
    else ""}
/**
 * 1-dimensional read-only array
 */
interface ${specializeOut(if (isReference) "ElementsRO" else "sRO")} : Vars {
    /**
     * Returns the element at the given index in the array
     * @param index Index of the element
     * @return The value at the given index
     */
    operator fun get(index: Int): $type
}

/**
 * 1-dimensional read-write array
 */
interface ${specialize(if (isReference) "Elements" else "s")} : ${specialize(if (isReference) "ElementsRO" else "sRO")} {
    /**
     * Sets the element at the given index in the array
     * @param index Index of the element
     * @param value The value to set to
     */
    operator fun set(index: Int, value: $type)
}

/**
 * 2-dimensional read-only array
 */
interface ${specializeOut(if (isReference) "ElementsRO2" else "sRO2")} : Vars2 {
    /**
     * Returns the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @return The value at the given index
     */
    operator fun get(index1: Int, index2: Int): $type
}

/**
 * 2-dimensional read-write array
 */
interface ${specialize(if (isReference) "Elements2" else "s2")} : ${specialize(
        if (isReference) "ElementsRO2" else "sRO2"
    )} {
    /**
     * Sets the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param value The value to set to
     */
    operator fun set(index1: Int, index2: Int, value: $type)
}

/**
 * 3-dimensional read-only array
 */
interface ${specializeOut(if (isReference) "ElementsRO3" else "sRO3")} : Vars3 {
    /**
     * Returns the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param index3 Index on the third axis of the element
     * @return The value at the given index
     */
    operator fun get(index1: Int, index2: Int, index3: Int): $type
}

/**
 * 3-dimensional read-write array
 */
interface ${specialize(if (isReference) "Elements3" else "s3")} : ${specialize(
        if (isReference) "ElementsRO3" else "sRO3"
    )} {
    /**
     * Sets the element at the given index in the array
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @param index3 Index on the third axis of the element
     * @param value The value to set to
     */
    operator fun set(index1: Int, index2: Int, index3: Int, value: $type)
}

/**
 * Read-only slice of an array, indexed in elements
 */
interface ${specialize("ArraySliceRO")} : ${specialize(if (isReference) "ElementsRO" else "sRO")},
    ArrayVarSlice<$type> {
    override fun slice(index: Int): ${specialize("ArraySliceRO")}

    override fun slice(index: Int, size: Int): ${specialize("ArraySliceRO")}
${if (!isReference) """    fun get$type(index: Int): $type = get(index)
""" else ""}
    fun get${if (isReference) "Element" else type}s(index: Int, slice: ${specializeIn(
        "ArraySlice"
    )}) {
        var j = index
        for (i in 0 until slice.size) {
            slice.set(i, get(j++))
        }
    }

    override fun iterator(): Iterator<$type> =
        object : SliceIterator<$type>(size) {
            override fun access(index: Int) = get(index)
        }
}

/**
 * Slice of an array, indexed in elements
 */
interface ${specialize("ArraySlice")} : ${specialize(if (isReference) "Elements" else "s")},
    ${specialize("ArraySliceRO")} {
    override fun slice(index: Int): ${specialize("ArraySlice")}

    override fun slice(index: Int, size: Int): ${specialize("ArraySlice")}
${if (!isReference) """    fun set$type(index: Int, value: $type) = set(index, value)
""" else ""}
    fun set${if (isReference) "Element" else type}s(index: Int, slice: ${specializeOut(
        "ArraySliceRO"
    )}) =
        slice.get${if (isReference) "Element" else type}s(0, slice(index, slice.size))
}

/**
 * Slice of a normal heap array
 */
open class Heap${specialize("ArraySlice")}(
    val array: ${specialize("Array")},
    final override val offset: Int,
    final override val size: Int
) : HeapArrayVarSlice<$type>, ${specialize("ArraySlice")} {
    override fun slice(index: Int): Heap${specialize("ArraySlice")} =
        slice(index, size - index)

    override fun slice(index: Int, size: Int): Heap${specialize("ArraySlice")} =
        prepareSlice(index, size, array, ::Heap${specializeName("ArraySlice")})

    final override fun get(index: Int): $type =
        array[index(offset, size, index)]

    final override fun set(index: Int, value: $type) =
        array.set(index(offset, size, index), value)

    final override fun get${if (isReference) "Element" else type}s(
        index: Int,
        slice: ${specializeIn("ArraySlice")}
    ) {
        if (slice !is Heap${specializeName(
        "ArraySlice"
    )}) return super.get${if (isReference) "Element" else type}s(index, slice)

        if (index < 0 || index + slice.size > size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        copy(array, slice.array, slice.size, index + this.offset, slice.offset)
    }

    final override fun set${if (isReference) "Element" else type}s(index: Int, slice: ${specializeOut(
        "ArraySliceRO"
    )}) {
        if (slice !is Heap${specializeName(
        "ArraySlice"
    )}) return super.set${if (isReference) "Element" else type}s(index, slice)

        if (index < 0 || index + slice.size > size)
            throw IndexOutOfBoundsException("Invalid index or view too long")

        copy(slice.array, array, slice.size, slice.offset, index + this.offset)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ${specializeAny("ArraySliceRO")}) return false
        for (i in 0 until size) {
            if (this[i] != other[i]) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var h = 1
        for (i in 0 until size) {
            h = h * 31 + ${if (isReference) "(this[i]?.hashCode() ?: 0)" else "this[i].primitiveHashCode()"}
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
inline $genericFun ${specialize("Array")}.sliceOver(
    index: Int = 0,
    size: Int = this.size - index
): Heap${specialize("ArraySlice")} = Heap${specializeName("ArraySlice")}(this, index, size)

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
inline fun${appendedGenericReified("R")} ${specialize("ArraySliceRO")}.readAs${specializeName(
        "Array"
    )}(block: (${specialize(
        "Array"
    )}, Int, Int) -> R): R {
    val array: ${specialize("Array")}
    val offset: Int
    when (this) {
        is Heap${specialize("ArraySlice")} -> {
            array = this.array
            offset = this.offset
        }
        else -> {
            array = to${specializeName("Array")}()
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
inline fun${appendedGenericReified("R")} ${specialize("ArraySlice")}.mutateAs${specializeName(
        "Array"
    )}(block: (${specialize(
        "Array"
    )}, Int, Int) -> R): R {
    val array: ${specialize("Array")}
    val offset: Int
    val mapped = when (this) {
        is Heap${specialize("ArraySlice")} -> {
            array = this.array
            offset = this.offset
            true
        }
        else -> {
            array = to${specializeName("Array")}()
            offset = 0
            false
        }
    }
    return try {
        block(array, offset, size)
    } finally {
        if (!mapped) get${specializeName(if (isReference) "Elements" else "s")}(0, array.sliceOver())
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
${if (isReference) "inline " else ""}$genericFunReified ${specialize("ArraySliceRO")
    }.readAs${specializeName("Array")}(): ${specialize("Array")} =${
    if (args.isEmpty()) """
    if (this is Heap${specialize("ArraySlice")} && size == array.size && offset == 0) array
    else ${specializeName("Array")}(size) { get${specializeName("")}(it) }"""
    else """ when (this) {
    is Heap${specialize("ArraySlice")} ->
        if (size == array.size && offset == 0) array else {
            ${specializeName("Array")}(size)
                .also { copy(array, it, size, offset) }
        }
    else -> ${specializeName("Array")}(size) { get${specializeName("")}(it) }
}"""}

/**
 * Copies the contents of the slice into an array
 * @receiver The slice to copy
 * @return Array containing the data of the slice
 */
${if (isReference) "inline " else ""}$genericFunReified ${specialize("ArraySliceRO")
    }.to${specializeName("Array")}(): ${specialize("Array")} =${
    if (args.isEmpty()) """
    ${specializeName("Array")}(size) { get${specializeName("")}(it) }"""
    else """ when (this) {
    is Heap${specialize("ArraySlice")} -> ${specializeName("Array")}(size)
        .also { copy(array, it, size, offset) }
    else -> ${specializeName("Array")}(size) { get${specializeName("")}(it) }
}"""}

/**
 * Class wrapping an array to provide nicer support for 2-dimensional data.
 *
 * The layout for the dimensions is as follows:
 * `index = y * width + x`
 */
class ${specialize("Array2")}(
    override val width: Int,
    override val height: Int,
    private val array: ${specialize("Array")}
) : ${specialize(if (isReference) "Elements2" else "s2")},
    Iterable<$type> {
    init {
        if (size != array.size) {
            throw IllegalArgumentException(
                "Array has invalid size: ${'$'}{array.size} (should be ${'$'}size)"
            )
        }
    }

    /**
     * Retrieve an element from the array or `null` if out of bounds.
     * @param index1 Index on the first axis of the element
     * @param index2 Index on the second axis of the element
     * @return The element at the given position or `null` if out of bounds
     */
    fun getOrNull(index1: Int, index2: Int): $type? {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            return null
        }
        return array[index2 * width + index1]
    }

    override fun get(index1: Int, index2: Int): $type {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            throw IndexOutOfBoundsException("${'$'}index1 ${'$'}index2")
        }
        return array[index2 * width + index1]
    }

    override fun set(index1: Int, index2: Int, value: $type) {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            throw IndexOutOfBoundsException("${'$'}index1 ${'$'}index2")
        }
        array[index2 * width + index1] = value
    }

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
inline $genericFunReified ${specializeName("Array2")}(
    width: Int, height: Int,
    init: (Int, Int) -> $type
) = ${specializeName("Array2")}(width, height) { i ->
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
inline $genericFunReified ${specializeName("Array2")}(
    width: Int, height: Int,
    init: (Int) -> $type
) = ${specializeName("Array2")}(
    width, height,
    ${specializeName("Array")}(width * height) { init(it) }
)

/**
 * Class wrapping an array to provide nicer support for 3-dimensional data.
 *
 * The layout for the dimensions is as follows:
 * `index = (z * height + y) * width + x`
 */
class ${specialize("Array3")}(
    override val width: Int,
    override val height: Int,
    override val depth: Int,
    private val array: ${specialize("Array")}
) : ${specialize(if (isReference) "Elements3" else "s3")},
    Iterable<$type> {
    init {
        if (size != array.size) {
            throw IllegalArgumentException(
                "Array has invalid size: ${'$'}{array.size} (should be ${'$'}size)"
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
    fun getOrNull(index1: Int, index2: Int, index3: Int): $type? {
        if (index1 < 0 || index2 < 0 || index3 < 0
            || index1 >= width || index2 >= height || index3 >= depth) {
            return null
        }
        return array[(index3 * height + index2) * width + index1]
    }

    override fun get(index1: Int, index2: Int, index3: Int): $type {
        if (index1 < 0 || index2 < 0 || index3 < 0
            || index1 >= width || index2 >= height || index3 >= depth) {
            throw IndexOutOfBoundsException("${'$'}index1 ${'$'}index2 ${'$'}index3")
        }
        return array[(index3 * height + index2) * width + index1]
    }

    override fun set(index1: Int, index2: Int, index3: Int, value: $type) {
        if (index1 < 0 || index2 < 0 || index3 < 0
            || index1 >= width || index2 >= height || index3 >= depth) {
            throw IndexOutOfBoundsException("${'$'}index1 ${'$'}index2")
        }
        array[(index3 * height + index2) * width + index1] = value
    }

    override operator fun iterator() = array.iterator()

    /**
     * Makes a shallow copy of the array and wrapper.
     * @return A new wrapper around a new array
     */
    fun copyOf() = ${specializeName(
        "Array3"
    )}(width, height, depth, array.copyOf())
}

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x, y and z coords of the element
 */
inline fun ${specializeAny("Array3")}.indices(block: (Int, Int, Int) -> Unit) {
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
inline $genericFunReified ${specializeName("Array3")}(
    width: Int, height: Int, depth: Int,
    init: (Int, Int, Int) -> $type
) = ${specializeName("Array3")}(width, height, depth) { i ->
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
inline $genericFunReified ${specializeName("Array3")}(
    width: Int, height: Int, depth: Int,
    init: (Int) -> $type
) = ${specializeName("Array3")}(
    width, height, depth,
    ${specializeName("Array")}(width * height * depth) { init(it) }
)

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
inline $genericFun ${specializeIn("Array2")}.fill(block: (Int, Int) -> $type) =
    indices { x, y -> this[x, y] = block(x, y) }

/**
 * Calls the given [block] with all indices of the given wrapper ordered by
 * their layout in the array and stores its return value in the array.
 * @receiver The wrapper to iterate through
 * @param block Called with x, y and z coords of the element
 */
inline $genericFun ${specializeIn("Array3")}.fill(block: (Int, Int, Int) -> $type) =
    indices { x, y, z -> this[x, y, z] = block(x, y, z) }
"""
)

if (isReference) {
    print(
        """
/**
 * Creates a new array and makes it accessible using a wrapper initialized with
 * nulls
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @return Wrapper around a new array
 */
inline $genericFunReified array2OfNulls(
    width: Int, height: Int
) = ${specializeName("Array2")}(
    width, height,
    arrayOfNulls$generic(width * height)
)

/**
 * Creates a new array and makes it accessible using a wrapper initialized with
 * nulls
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param depth Depth of the wrapper
 * @return Wrapper around a new array
 */
inline $genericFunReified array3OfNulls(
    width: Int, height: Int, depth: Int
) = ${specializeName("Array3")}(
    width, height, depth,
    arrayOfNulls$generic(width * height)
)
"""
    )
} else {
    print(
        """
/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @return Wrapper around a new array
 */
inline $genericFunReified ${specializeName("Array2")}(width: Int, height: Int) =
    ${specializeName("Array2")}(width, height, ${specialize("Array")}(width * height))

/**
 * Creates a new array and makes it accessible using a wrapper
 * @param width Width of the wrapper
 * @param height Height of the wrapper
 * @param depth Depth of the wrapper
 * @return Wrapper around a new array
 */
inline $genericFunReified ${specializeName("Array3")}(width: Int, height: Int, depth: Int) =
    ${specializeName("Array3")}(width, height, depth, ${specialize("Array")}(width * height * depth))
"""
    )
}
