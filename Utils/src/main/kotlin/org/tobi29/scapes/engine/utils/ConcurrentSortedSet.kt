package org.tobi29.scapes.engine.utils

header class ConcurrentSortedSet<T : Comparable<T>> : AbstractSet<T>(), MutableSet<T> {
    override val size: Int
    override fun isEmpty(): Boolean
    override fun iterator(): MutableIterator<T>
    override fun add(element: T): Boolean
    override fun addAll(elements: Collection<T>): Boolean
    override fun clear()
    override fun remove(element: T): Boolean
    override fun removeAll(elements: Collection<T>): Boolean
    override fun retainAll(elements: Collection<T>): Boolean
    override fun contains(element: T): Boolean
    override fun containsAll(elements: Collection<T>): Boolean
}

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
            removeAll { it == element }

    override fun removeAll(elements: Collection<T>) =
            removeAll { it in elements }

    override fun retainAll(elements: Collection<T>) =
            removeAll { it !in elements }

    override fun contains(element: T) =
            set.any { it == element }

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

    private inner class Entry(val value: T) : Comparable<Entry> {
        private val uid = uidCounter.incrementAndGet()

        override fun compareTo(other: Entry) =
                comparator.compare(value, other.value).let {
                    if (uid > other.uid) 1
                    else if (uid < other.uid) -1
                    else 0
                }
    }

    private class DummyComparator<T> : Comparator<T> {
        override fun compare(a: T,
                             b: T) = 0
    }
}