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

package org.tobi29.scapes.engine.utils

// TODO: Is it viable to make a compact but efficient implementation for this on js?
impl class ArrayDeque<E : Any> private constructor(
        private var array: ArrayList<E>
) : Deque<E>, MutableCollection<E> by array {
    impl constructor() : this(ArrayList<E>())

    impl constructor(size: Int) : this()

    impl override fun addFirst(element: E) = array.add(0, element)

    impl override fun pollFirst(): E? =
            if (isEmpty()) null else array.removeAt(0)

    impl override fun pollLast(): E? =
            if (isEmpty()) null else array.removeAt(array.lastIndex)

    impl override fun getFirst(): E = array[0]

    impl override fun getLast(): E = array[size - 1]

    impl override fun removeFirstOccurrence(element: E): Boolean {
        for (i in 0 until size) {
            if (element == array[i]) {
                array.removeAt(i)
                return true
            }
        }
        return false
    }

    impl override fun removeLastOccurrence(element: E): Boolean {
        for (i in size - 1 downTo 0) {
            if (element == array[i]) {
                array.removeAt(i)
                return true
            }
        }
        return false
    }

    impl override fun descendingIterator(): MutableIterator<E> =
            array.listIterator(array.lastIndex).descendingMutableIterator()

    // Boilerplate following

    impl override fun offerFirst(element: E): Boolean {
        addFirst(element)
        return true
    }

    impl override fun offerLast(element: E): Boolean {
        addLast(element)
        return true
    }

    impl override fun removeFirst(): E {
        return pollFirst() ?: throw NoSuchElementException()
    }

    impl override fun removeLast(): E {
        return pollLast() ?: throw NoSuchElementException()
    }

    impl override fun peekFirst() = getFirst()

    impl override fun peekLast() = getLast()

    impl override fun addLast(element: E) {
        add(element)
    }

    impl override fun offer(element: E) = offerLast(element)

    impl override fun remove() = removeFirst()

    impl override fun poll() = pollFirst()

    impl override fun element() = getFirst()

    impl override fun peek() = getFirst()

    impl override fun push(element: E) {
        addFirst(element)
    }

    impl override fun pop() = removeFirst()
}
