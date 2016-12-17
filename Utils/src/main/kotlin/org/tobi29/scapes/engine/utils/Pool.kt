/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.utils

import java.util.*

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
class Pool<E>
/**
 * Creates a new instance using the given [supplier]
 * @param supplier Called to create new objects in case the pool ran out of reusable ones
 */
(private val supplier: () -> E) : Collection<E> {
    /**
     * Returns the current size of this [Pool]
     */
    override var size = 0
        private set
    private val list = ArrayList<E>()

    /**
     * Resets the pool so it can be reused
     *
     * **Note:** The stored objects are **not** cleared!
     */
    fun reset() {
        size = 0
    }

    /**
     * Returns the next object from the pool or creates a new ones if none was
     * available
     * @return A possibly reused object
     */
    fun push(): E {
        val value: E
        if (list.size <= size) {
            value = supplier()
            list.add(value)
            size++
        } else {
            value = list[size++]
        }
        return value
    }

    /**
     * Returns the object at the given index
     * @param i Index to look at (Has to be in range `0` to `size - 1`
     * @return Object at given index
     * @throws IndexOutOfBoundsException When index is equal or greater than size or less than 0
     */
    operator fun get(i: Int): E {
        if (i < 0 || i >= size) {
            throw IndexOutOfBoundsException(
                    "Index: $i Size: $size")
        }
        return list[i]
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
        return list[size - 1]
    }

    /**
     * Removes the given object out of the pool
     *
     * **Note:** The object it removed even after calling reset
     * @param element Object to remove
     * @return When `true` the object is no longer referenced by the pool,
     * * otherwise it never was to begin with
     */
    fun remove(element: E): Boolean {
        if (list.remove(element)) {
            size--
            return true
        }
        return false
    }

    /**
     * Remove the element at the given index [i]
     * @param i The index of the element to remove
     * @return The element previously at that index
     * @throws IndexOutOfBoundsException If `i < 0` or `i >= size`
     */
    fun removeAt(i: Int): E {
        if (i < 0 || i >= size) {
            throw IndexOutOfBoundsException(
                    "Index: $i Size: $size")
        }
        val element = list.removeAt(i)
        size--
        return element
    }

    /**
     * Appends the element to the pool to be reused by [push]
     * **Note**: This element may no longer be used outside
     * **Node**: This element will only be used by the pool once [push] needed it
     */
    fun give(element: E) {
        list.add(element)
    }

    /**
     * Returns an Iterator to iterate through all objects previously
     * retrieved by [.push]
     * @return An Iterator to iterate through the pool's data
     */
    override fun iterator(): MutableIterator<E> {
        return object : MutableIterator<E> {
            private var i: Int = 0

            override fun hasNext(): Boolean {
                return i < size
            }

            override fun next(): E {
                if (i >= size) {
                    throw NoSuchElementException(
                            "Reached limit: $i of $size")
                }
                return list[i++]
            }

            override fun remove() {
                throw UnsupportedOperationException(
                        "Cannot remove object from pool")
            }
        }
    }

    /**
     * Returns whether or not the given object can be found in the pool
     *
     * **Note:** Object outside the range are not checked
     * @param element The object to search or `null`
     * @return `true` if the object was found
     */
    override operator fun contains(element: E): Boolean {
        if (element == null) {
            for (i in 0..size - 1) {
                if (list[i] == null) {
                    return true
                }
            }
        } else {
            for (i in 0..size - 1) {
                if (element == list[i]) {
                    return true
                }
            }
        }
        return false
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return elements.all { contains(it) }
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }
}
