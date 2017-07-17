package org.tobi29.scapes.engine.utils

header class ConcurrentSortedMap<K : Comparable<K>, V> : AbstractMap<K, V>(), ConcurrentMap<K, V> {
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>

    override fun put(key: K,
                     value: V): V?

    override fun putAll(from: Map<out K, V>)

    override fun remove(key: K): V?

    override fun clear()
}
