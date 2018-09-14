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

package org.tobi29.utils

import org.tobi29.stdex.copy

/**
 * Using [.push] you can retrieve objects from this pool, modify them
 * and then later iterate through them
 *
 * As this class is meant for optimization of allocation and deallocation of
 * objects, any data in the pool is kept until the instance is deleted by the GC
 * or you removed it using [.remove]
 *
 * **Note:** This class is not thread-safe at all and should only be used by
 * one thread at a time
 * @param E of elements stored
 */
class Pool<E>(private val supplier: () -> E) : AbstractMutableList<E>() {
    /**
     * Returns the current size of this [Pool]
     */
    override var size = 0
        private set
    /**
     * Amount of allocated elements in this [Pool]
     */
    var filled = 0
        private set
    private var array = emptyArray<Any?>()

    /**
     * Resets the pool so it can be reused
     *
     * **Note:** The stored objects are **not** cleared! To remove references
     * to elements use [clear]
     * @see clear
     */
    fun reset() {
        size = 0
    }

    /**
     * Returns the next object from the pool or creates a new ones if none was
     * available
     * @return A possibly reused object
     */
    fun push(): E =
        if (filled <= size) {
            supplier().also {
                ensure(1)
                array[size] = it
                size++
                filled++
            }
        } else {
            @Suppress("UNCHECKED_CAST")
            array[size++] as E
        }

    /**
     * Discards the last element in the pool and returns the second to last one
     * @return Last object in pool or null if it is now empty
     * @throws NoSuchElementException When the pool is empty before invoking this method
     */
    fun pop(): E? {
        if (size == 0) {
            throw NoSuchElementException("Pool is empty")
        }
        size--
        if (size == 0) {
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return array[size - 1] as E
    }

    /**
     * Discards the specified amount of elements in the pool
     * @param elements Amount of elements to discard
     */
    fun pop(elements: Int) {
        if (size < elements)
            throw IndexOutOfBoundsException("Not enough elements in pool")
        size -= elements
    }

    /**
     * Appends the element to the pool to be reused by [push]
     * **Note**: This element may no longer be used outside
     * **Node**: This element will only be used by the pool once [push] needed it
     */
    fun give(element: E) {
        ensure(1)
        array[filled++] = element
    }

    /**
     * Allocates enough space in the internal buffer for the specified amount
     * of elements
     *
     * **Note:** This purely avoids allocating the array later, especially
     * for large pools, the objects however are allocated on demand still
     * @param capacity Amount of entry to be able to hold
     */
    fun ensureCapacity(capacity: Int): Boolean {
        var current = array.size
        if (capacity > current) {
            current = current.coerceAtLeast(8)
            do {
                // TODO: Make growth strategy configurable?
                current *= 2
            } while (capacity > current)
            resize(current)
            return true
        }
        return false
    }

    override fun get(index: Int): E {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("Index: $index Size: $size")
        }
        return getUnsafe(index)
    }

    /**
     * Returns an element in the pool with no error checking at all,
     * likely tripping into unsafe cast issues when misused
     *
     * **Note**: This can be used to safely retrieve unused elements in the pool
     * as long as [index] is less than [filled]
     * @param index The index of the element to retrieve
     * @see get
     */
    @Suppress("UNCHECKED_CAST")
    fun getUnsafe(index: Int): E = array[index] as E

    override fun removeAt(index: Int): E {
        if (index > size)
            throw IndexOutOfBoundsException("Index: $index Size: $size")
        @Suppress("UNCHECKED_CAST")
        val element = array[index] as E
        copy(array, array, filled - index - 1, index + 1, index)
        size--
        filled--
        return element
    }

    override fun iterator(): MutableIterator<E> {
        return object : MutableIterator<E> {
            private var i = 0
            private var j = -1

            override fun hasNext() = i < size

            override fun next(): E {
                if (i >= size) {
                    throw NoSuchElementException(
                        "Reached limit: $i of $size"
                    )
                }
                @Suppress("UNCHECKED_CAST")
                val element = array[i] as E
                i++
                j = j.coerceAtLeast(i - 1)
                return element
            }

            override fun remove() {
                if (j < 0) throw IllegalStateException(
                    "Cannot remove element before calling next"
                )
                if (j < i) removeAt(j++)
            }
        }
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        val count = elements.size
        if (index - 1 + elements.size > size)
            throw IndexOutOfBoundsException(
                "Index: $index Size: $size Elements: $count"
            )
        ensure(count)
        copy(array, array, filled - index, index, index + count)
        var i = index
        for (element in elements) {
            array[i++] = element
        }
        size += count
        filled += count
        return true
    }

    override fun add(index: Int, element: E) {
        if (index > size)
            throw IndexOutOfBoundsException("Index: $index Size: $size")
        ensure(1)
        copy(array, array, filled - index, index, index + 1)
        array[index] = element
        size++
        filled++
    }

    override fun clear() {
        array = emptyArray()
        size = 0
        filled = 0
    }

    override fun set(index: Int, element: E): E {
        if (index > size)
            throw IndexOutOfBoundsException("Index: $index Size: $size")
        @Suppress("UNCHECKED_CAST")
        return (array[index] as E).also { array[index] = element }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun ensure(elements: Int = 1) =
        ensureCapacity(size + elements)

    private fun resize(capacity: Int) {
        if (capacity < size) throw IllegalArgumentException(
            "Capacity is smaller than size: $capacity < $size"
        )
        val newArray = arrayOfNulls<Any>(capacity)
        copy(array, newArray, filled)
        array = newArray
    }
}

/**
 * Performs the given [action] on each object referenced by the pool
 */
inline fun <E> Pool<E>.forAllObjects(action: (E) -> Unit) {
    for (i in 0 until filled) {
        @Suppress("UNCHECKED_CAST")
        action(getUnsafe(i))
    }
}
