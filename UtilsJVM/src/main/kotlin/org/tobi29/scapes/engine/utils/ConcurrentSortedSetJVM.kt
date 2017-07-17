package org.tobi29.scapes.engine.utils

import java.util.concurrent.ConcurrentSkipListSet

impl class ConcurrentSortedSet<T : Comparable<T>> : AbstractSet<T>(), MutableSet<T> {
    private val set = ConcurrentSkipListSet<T>()

    impl override val size get() = set.size

    impl override fun isEmpty() = set.isEmpty()

    impl override fun iterator() = set.iterator()

    impl override fun add(element: T) = set.add(element)

    impl override fun addAll(elements: Collection<T>) = set.addAll(elements)

    impl override fun clear() = set.clear()

    impl override fun remove(element: T) = set.remove(element)

    impl override fun removeAll(elements: Collection<T>) =
            set.removeAll(elements)

    impl override fun retainAll(elements: Collection<T>) =
            set.retainAll(elements)

    impl override fun contains(element: T) = set.contains(element)

    impl override fun containsAll(elements: Collection<T>) =
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
