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

import org.tobi29.stdex.ConcurrentSortedSet
import org.tobi29.stdex.atomic.AtomicLong

class ConcurrentOrderedCollection<T>(
    private val comparator: Comparator<T> = DummyComparator()
) : AbstractCollection<T>(), MutableCollection<T> {
    private val uidCounter = AtomicLong(Long.MIN_VALUE)
    private val set = ConcurrentSortedSet<Entry>()

    override val size get() = set.size

    override fun isEmpty() = set.isEmpty()

    override fun add(element: T) = set.add(Entry(element))

    override fun addAll(elements: Collection<T>) = elements.any { add(it) }

    override fun clear() = set.clear()

    override fun iterator() = set.iterator().let { iterator ->
        object : MutableIterator<T> {
            override fun remove() = iterator.remove()
            override fun hasNext() = iterator.hasNext()
            override fun next() = iterator.next().value
        }
    }

    override fun remove(element: T) =
        set.remove(Entry(element, 0))

    override fun removeAll(elements: Collection<T>) =
        elements.any { remove(it) }

    override fun retainAll(elements: Collection<T>): Boolean {
        val iterator = iterator()
        var modified = false
        while (iterator.hasNext()) {
            if (iterator.next() !in elements) {
                iterator.remove()
                modified = true
            }
        }
        return modified
    }

    override fun contains(element: T): Boolean =
        set.contains(Entry(element, 0L))

    override fun containsAll(elements: Collection<T>) =
        elements.all { contains(it) }

    override fun equals(other: Any?): Boolean {
        if (other !is ConcurrentOrderedCollection<*>) {
            return false
        }
        return other === this || size == other.size &&
                set.zip(other.set).all { (a, b) ->
                    a.value == b.value
                }
    }

    override fun hashCode() = set.sumBy { (it.value?.hashCode() ?: 0) }

    override fun toString() = joinToString(prefix = "[", postfix = "]")

    private inner class Entry(
        val value: T,
        private val uid: Long = uidCounter.incrementAndGet()
    ) : Comparable<Entry> {
        override fun compareTo(other: Entry) =
            comparator.compare(value, other.value).let {
                if (uid > other.uid) 1
                else if (uid < other.uid) -1
                else 0
            }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || other !is ConcurrentOrderedCollection<*>.Entry)
                return false
            return value == other.value
        }

        override fun hashCode(): Int = value?.hashCode() ?: 0
    }
}

private class DummyComparator<T> : Comparator<T> {
    override fun compare(a: T, b: T) = 0
}
