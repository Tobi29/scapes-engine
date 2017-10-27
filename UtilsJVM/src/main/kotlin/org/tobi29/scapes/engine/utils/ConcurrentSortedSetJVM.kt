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
