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

actual class ConcurrentSortedSet<T : Comparable<T>> : AbstractSet<T>(), MutableSet<T> {
    private val set = HashSet<T>()
    private var list = emptyList<T>()

    actual override val size get() = set.size

    actual override fun isEmpty() = set.isEmpty()

    actual override fun iterator() = list.iterator().let { iterator ->
        object : MutableIterator<T> {
            private var current: T? = null

            override fun hasNext() = iterator.hasNext()

            override fun next() = iterator.next().also { current = it }

            override fun remove() {
                this@ConcurrentSortedSet.remove(current
                        ?: throw IllegalStateException(
                        "No element in iterator yet"))
            }
        }
    }

    actual override fun add(element: T) =
            if (set.add(element)) {
                val newList = ArrayList<T>(list.size + 1)
                newList.addAll(list)
                newList.add(element)
                newList.sort()
                list = newList
                true
            } else false

    actual override fun addAll(elements: Collection<T>) =
            if (set.addAll(elements)) {
                val newList = ArrayList<T>(list.size + elements.size)
                newList.addAll(list)
                newList.addAll(elements)
                newList.sort()
                list = newList
                true
            } else false

    actual override fun clear() {
        set.clear()
        list = emptyList()
    }

    actual override fun remove(element: T) =
            if (set.remove(element)) {
                list -= element
                true
            } else false

    actual override fun removeAll(elements: Collection<T>) =
            if (set.removeAll(elements)) {
                list -= elements
                true
            } else false

    actual override fun retainAll(elements: Collection<T>) =
            if (set.retainAll(elements)) {
                list = elements.filter { set.contains(it) }
                true
            } else false

    actual override fun contains(element: T) = set.contains(element)

    actual override fun containsAll(elements: Collection<T>) =
            set.containsAll(elements)

    override fun equals(other: Any?): Boolean {
        if (other !is ConcurrentSortedSet<*>) {
            return false
        }
        return set == other.set
    }

    override fun hashCode() = set.hashCode()

    override fun toString() = set.toString()
}