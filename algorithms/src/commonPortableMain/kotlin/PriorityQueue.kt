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

package org.tobi29.algorithms

import org.tobi29.stdex.AbstractQueue
import org.tobi29.stdex.ArrayDeque
import org.tobi29.stdex.InlineUtility
import org.tobi29.stdex.Queue

actual class PriorityQueue<E : Any>(
    private val comparator: Comparator<E>,
    private val data: ArrayList<E>
) : AbstractQueue<E>(), Queue<E> {
    actual constructor(
        comparator: Comparator<E>
    ) : this(comparator, ArrayList())

    override val size get() = data.size

    override fun iterator(): MutableIterator<E> = IteratorImpl(this)

    override fun offer(element: E): Boolean {
        data.add(element)
        bubbleUp(element = element)
        return true
    }

    override fun poll(): E? {
        if (isEmpty()) return null
        val element = data[0]
        siftDown()
        return element
    }

    override fun peek(): E? = data.firstOrNull()

    override fun remove(element: E): Boolean {
        for ((index, entry) in data.withIndex()) {
            if (entry == element) {
                removeAt(index)
                return true
            }
        }
        return false
    }

    override fun addAll(elements: Collection<E>): Boolean {
        var modified = false
        for (element in elements) {
            if (add(element)) modified = true
        }
        return modified
    }

    override fun removeAll(elements: Collection<E>): Boolean =
        removeAll { it in elements }

    override fun retainAll(elements: Collection<E>): Boolean =
        removeAll { it !in elements }

    private fun siftDown(
        index: Int = 0,
        element: E = data.removeAt(size - 1)
    ): Boolean {
        if (index >= size) return false

        data[index] = element

        var elementIndex = index

        val size = size
        val max = size shr 1


        while (elementIndex < max) {
            // Left child
            var childIndex = (elementIndex shl 1) + 1
            var child = data[childIndex]

            // Right child
            if (childIndex + 1 < size) {
                val childRight = data[childIndex + 1]
                if (comparator.compare(child, childRight) > 0) {
                    childIndex++
                    child = childRight
                }
            }

            // Compare with smaller child
            if (comparator.compare(element, child) <= 0) break

            // Swap
            data[elementIndex] = child
            elementIndex = childIndex
        }

        data[elementIndex] = element

        return true
    }

    private fun bubbleUp(
        index: Int = size - 1,
        element: E = data[index]
    ) {
        var elementIndex = index

        while (elementIndex > 0) {
            // Parent
            val parentIndex = elementIndex - 1 shr 1
            val parent = data[parentIndex]

            // Compare with parent
            if (comparator.compare(element, parent) >= 0) break

            // Swap
            data[elementIndex] = parent
            elementIndex = parentIndex
        }

        data[elementIndex] = element
    }

    private fun removeAt(index: Int): E? {
        val element = data[index]
        if (siftDown(index) && data[index] === element) {
            bubbleUp(index, element)
            if (data[index] !== element) {
                return element
            }
        }
        return null
    }

    private fun removeExact(element: E): E? {
        for ((index, entry) in data.withIndex()) {
            if (entry === element) return removeAt(index)
        }
        return null
    }

    private class IteratorImpl<E : Any>(
        private val queue: PriorityQueue<E>
    ) : MutableIterator<E> {
        private var index = 0
        private var previous = -1
        private var previousElement: E? = null
        private var removed = ArrayDeque<E>()

        override fun hasNext(): Boolean =
            index < queue.size || removed.isNotEmpty()

        override fun next(): E {
            if (index < queue.size) {
                previous = index
                return queue.data[index++]
            }
            previous = -1
            previousElement = removed.poll()
            previousElement?.let { return it }
            throw NoSuchElementException()
        }

        override fun remove() {
            if (previous >= 0) {
                val moved = queue.removeAt(previous)
                previous = -1
                if (moved == null) {
                    index--
                } else {
                    removed.add(moved)
                }
                return
            }
            previousElement?.let { element ->
                queue.removeExact(element)
                previousElement = null
                return
            }
            error("No element in iterator")
        }
    }
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun <E : Comparable<E>> PriorityQueue(): PriorityQueue<E> =
    PriorityQueue(naturalOrder())

