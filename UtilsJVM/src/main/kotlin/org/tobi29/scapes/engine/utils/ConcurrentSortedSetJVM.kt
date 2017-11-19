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

import java.util.concurrent.ConcurrentSkipListSet

actual class ConcurrentSortedSet<T : Comparable<T>> : AbstractSet<T>(), MutableSet<T> {
    private val set = ConcurrentSkipListSet<T>()

    actual override val size get() = set.size

    actual override fun isEmpty() = set.isEmpty()

    actual override fun iterator() = set.iterator()

    actual override fun add(element: T) = set.add(element)

    actual override fun addAll(elements: Collection<T>) = set.addAll(elements)

    actual override fun clear() = set.clear()

    actual override fun remove(element: T) = set.remove(element)

    actual override fun removeAll(elements: Collection<T>) =
            set.removeAll(elements)

    actual override fun retainAll(elements: Collection<T>) =
            set.retainAll(elements)

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
