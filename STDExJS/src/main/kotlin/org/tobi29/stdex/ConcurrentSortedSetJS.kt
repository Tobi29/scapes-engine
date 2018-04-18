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

actual class ConcurrentSortedSet<T : Comparable<T>> private constructor(
    private val set: HashSet<T>
) : MutableSet<T>, Set<T> by set {
    actual constructor() : this(HashSet<T>())

    private var list = emptyList<T>()

    override fun iterator() = list.iterator().let { iterator ->
        object : MutableIterator<T> {
            private var current: T? = null

            override fun hasNext() = iterator.hasNext()

            override fun next() = iterator.next().also { current = it }

            override fun remove() {
                this@ConcurrentSortedSet.remove(
                    current
                            ?: throw IllegalStateException(
                                "No element in iterator yet"
                            )
                )
            }
        }
    }

    override fun add(element: T) =
        if (set.add(element)) {
            val newList = ArrayList<T>(list.size + 1)
            newList.addAll(list)
            newList.add(element)
            newList.sort()
            list = newList
            true
        } else false

    override fun addAll(elements: Collection<T>) =
        if (set.addAll(elements)) {
            val newList = ArrayList<T>(list.size + elements.size)
            newList.addAll(list)
            newList.addAll(elements)
            newList.sort()
            list = newList
            true
        } else false

    override fun clear() {
        set.clear()
        list = emptyList()
    }

    override fun remove(element: T) =
        if (set.remove(element)) {
            list -= element
            true
        } else false

    override fun removeAll(elements: Collection<T>) =
        if (set.removeAll(elements)) {
            list -= elements
            true
        } else false

    override fun retainAll(elements: Collection<T>) =
        if (set.retainAll(elements)) {
            list = elements.filter { set.contains(it) }
            true
        } else false
}
