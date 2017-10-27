package org.tobi29.scapes.engine.utils

import java.util.concurrent.ConcurrentSkipListMap

actual class ConcurrentSortedMap<K : Comparable<K>, V> : AbstractMap<K, V>(), ConcurrentMap<K, V> {
    private val map = ConcurrentSkipListMap<K, V>()

    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> = map.entries
    actual override val keys: MutableSet<K> = map.keys
    actual override val values: MutableCollection<V> = map.values

    actual override fun put(key: K,
                          value: V): V? = map.put(key, value)

    actual override fun putAll(from: Map<out K, V>) = map.putAll(from)

    actual override fun remove(key: K): V? = map.remove(key)

    actual override fun clear() = map.clear()
}
