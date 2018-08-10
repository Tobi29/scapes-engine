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

package org.tobi29.stdex

actual class ConcurrentSortedSet<T : Comparable<T>> : AbstractMutableSet<T>(),
    MutableSet<T> {
    private var list = emptyList<T>()

    override val size get() = list.size

    override fun iterator(): MutableIterator<T> =
        IteratorImpl(this, list.iterator())

    private fun binarySearchKey(element: T) = list.binarySearchKey(element)

    private fun List<T>.binarySearchKey(element: T) = binarySearch(element)

    override fun contains(element: T) = binarySearchKey(element) < 0

    override fun add(element: T): Boolean {
        var i = binarySearchKey(element)
        return if (i < 0) {
            i = -i - 1
            val newList = ArrayList<T>(list.size + 1)
            for (j in 0 until i) {
                newList.add(list[j])
            }
            newList.add(element)
            for (j in i until list.size) {
                newList.add(list[j])
            }
            list = newList
            true
        } else false
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val newList = ArrayList<T>(list.size + elements.size)
        newList.addAll(list)
        var modified = false
        for (element in elements) {
            var i = newList.binarySearchKey(element)
            if (i < 0) {
                i = -i - 1
                newList.add(i, element)
                modified = true
            }
        }
        list = newList
        return modified
    }

    override fun clear() {
        list = emptyList()
    }

    override fun remove(element: T): Boolean {
        val i = binarySearchKey(element)
        return if (i < 0) false
        else {
            val newList = ArrayList<T>(list.size - 1)
            for (j in 0 until i) {
                newList.add(list[j])
            }
            for (j in i until list.size) {
                newList.add(list[j])
            }
            list = newList
            true
        }
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val newList = ArrayList<T>(list.size - elements.size)
        newList.addAll(list)
        for (element in elements) {
            val i = newList.binarySearchKey(element)
            if (i >= 0) {
                newList.removeAt(i)
            }
        }
        return if (list.size == newList.size) false
        else {
            list = newList
            true
        }
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val newList = ArrayList<T>(elements.size)
        for (element in elements) {
            val i = list.binarySearchKey(element)
            if (i >= 0) {
                var j = newList.binarySearchKey(element)
                if (j < 0) {
                    j = -j - 1
                    newList.add(j, list[i])
                }
            }
        }
        return if (list.size == newList.size) false
        else {
            list = newList
            true
        }
    }

    private class IteratorImpl<T : Comparable<T>>(
        val set: ConcurrentSortedSet<T>,
        val iterator: Iterator<T>
    ) : MutableIterator<T> {
        private var current: T? = null

        override fun hasNext() = iterator.hasNext()

        override fun next() = iterator.next().also { current = it }

        override fun remove() {
            set.remove(current ?: error("No element in iterator yet"))
        }
    }
}
