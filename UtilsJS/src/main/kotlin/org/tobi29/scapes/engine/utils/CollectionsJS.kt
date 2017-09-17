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

impl class ConcurrentHashSet<E> : MutableSet<E> {
    impl override fun addAll(elements: Collection<E>): Boolean {
        var added = false
        for (element in elements) {
            added = add(element) || added
        }
        return added
    }

    impl override fun removeAll(elements: Collection<E>) =
            map.keys.removeAll(elements)

    impl override fun retainAll(elements: Collection<E>) =
            map.keys.retainAll(elements)

    impl override fun contains(element: E) = map.keys.contains(element)
    impl override fun containsAll(elements: Collection<E>) =
            map.keys.containsAll(elements)

    impl override fun isEmpty() = map.isEmpty()

    private val map = ConcurrentHashMap<E, Unit>()

    impl override val size get() = map.size

    impl override fun add(element: E) = map.put(element, Unit) == null
    impl override fun remove(element: E) = map.remove(element) != null
    impl override fun clear() = map.clear()
    impl override fun iterator(): MutableIterator<E> = map.keys.iterator()

    override fun equals(other: Any?) = map.keys == other
    override fun hashCode() = map.keys.hashCode()
    override fun toString() = map.keys.toString()
}
