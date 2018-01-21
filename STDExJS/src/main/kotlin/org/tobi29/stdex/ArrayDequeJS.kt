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

package org.tobi29.stdex

// TODO: Is it viable to make a compact but efficient implementation for this on js?
actual class ArrayDeque<E : Any> private constructor(
        private var array: ArrayList<E>
) : Deque<E>,
        MutableCollection<E> by array {
    actual constructor() : this(ArrayList<E>())

    actual constructor(size: Int) : this()

    actual override fun addFirst(element: E) = array.add(0, element)

    actual override fun pollFirst(): E? =
            if (isEmpty()) null else array.removeAt(0)

    actual override fun pollLast(): E? =
            if (isEmpty()) null else array.removeAt(array.lastIndex)

    actual override fun getFirst(): E = array[0]

    actual override fun getLast(): E = array[size - 1]

    actual override fun removeFirstOccurrence(element: Any): Boolean {
        for (i in 0 until size) {
            if (element == array[i]) {
                array.removeAt(i)
                return true
            }
        }
        return false
    }

    actual override fun removeLastOccurrence(element: Any): Boolean {
        for (i in size - 1 downTo 0) {
            if (element == array[i]) {
                array.removeAt(i)
                return true
            }
        }
        return false
    }

    actual override fun descendingIterator(): MutableIterator<E> =
            array.listIterator(array.lastIndex).let { iterator ->
                object : MutableIterator<E> {
                    override fun hasNext(): Boolean = iterator.hasPrevious()

                    override fun next(): E = iterator.previous()

                    override fun remove() = iterator.remove()
                }
            }

    // Boilerplate following

    actual override fun offerFirst(element: E): Boolean {
        addFirst(element)
        return true
    }

    actual override fun offerLast(element: E): Boolean {
        addLast(element)
        return true
    }

    actual override fun removeFirst(): E {
        return pollFirst() ?: throw NoSuchElementException()
    }

    actual override fun removeLast(): E {
        return pollLast() ?: throw NoSuchElementException()
    }

    actual override fun peekFirst() = getFirst()

    actual override fun peekLast() = getLast()

    actual override fun addLast(element: E) {
        add(element)
    }

    actual override fun offer(element: E) = offerLast(element)

    actual override fun remove() = removeFirst()

    actual override fun poll() = pollFirst()

    actual override fun element() = getFirst()

    actual override fun peek() = getFirst()

    actual override fun push(element: E) {
        addFirst(element)
    }

    actual override fun pop() = removeFirst()
}
