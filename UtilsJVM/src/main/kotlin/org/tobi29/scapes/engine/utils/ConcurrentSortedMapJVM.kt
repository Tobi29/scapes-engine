package org.tobi29.scapes.engine.utils

import java.util.concurrent.ConcurrentSkipListMap

impl class ConcurrentSortedMap<K : Comparable<K>, V> : AbstractMap<K, V>(), ConcurrentMap<K, V> {
    private val map = ConcurrentSkipListMap<K, V>()

    impl override val entries: MutableSet<MutableMap.MutableEntry<K, V>> = map.entries
    impl override val keys: MutableSet<K> = map.keys
    impl override val values: MutableCollection<V> = map.values

    impl override fun put(key: K,
                          value: V): V? = map.put(key, value)

    impl override fun putAll(from: Map<out K, V>) = map.putAll(from)

    impl override fun remove(key: K): V? = map.remove(key)

    impl override fun clear() = map.clear()
}
