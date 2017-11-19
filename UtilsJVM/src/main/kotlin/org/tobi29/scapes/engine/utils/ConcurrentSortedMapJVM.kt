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
