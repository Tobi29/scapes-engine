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

package org.tobi29.stdex

actual class ConcurrentSortedMap<K : Comparable<K>, V> :
    AbstractMutableMap<K, V>(),
    ConcurrentMap<K, V> {
    private val map = HashMap<K, V>()
    private var list = emptyList<MutableMap.MutableEntry<K, V>>()

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> =
        object : AbstractMutableSet<MutableMap.MutableEntry<K, V>>(),
            MutableSet<MutableMap.MutableEntry<K, V>> {
            override val size get() = map.size

            override fun add(element: MutableMap.MutableEntry<K, V>): Boolean =
                throw UnsupportedOperationException(
                    "Add is not supported on entries"
                )

            override fun addAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean =
                throw UnsupportedOperationException(
                    "Add is not supported on entries"
                )

            override fun clear() = this@ConcurrentSortedMap.clear()

            override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> {
                val iterator = list.iterator()
                return object : MutableIterator<MutableMap.MutableEntry<K, V>> {
                    private var current: MutableMap.MutableEntry<K, V>? =
                        null

                    override fun hasNext() = iterator.hasNext()

                    override fun next() =
                        iterator.next().also { current = it }

                    override fun remove() {
                        (current
                                ?: throw IllegalStateException(
                                    "No element in iterator yet"
                                )
                                ).let { current ->
                            remove(current)
                        }
                    }
                }
            }

            override fun remove(element: MutableMap.MutableEntry<K, V>) =
                this@ConcurrentSortedMap.remove(element.key) != null
        }

    override fun put(
        key: K,
        value: V
    ) =
        map.put(key, value).also { previous ->
            if (previous == null) {
                val newList = ArrayList<MutableMap.MutableEntry<K, V>>(
                    list.size + 1
                )
                newList.addAll(list)
                newList.add(map.entries.first { it.key == key })
                newList.sortBy { it.key }
                list = newList
            } else {
                list.asSequence().filter { it.key == key }.forEach {
                    it.setValue(value)
                }
            }
        }

    override fun putAll(from: Map<out K, V>) {
        val newList = ArrayList<MutableMap.MutableEntry<K, V>>(
            list.size + from.size
        )
        newList.addAll(list)
        for ((key, value) in from) {
            val previous = map.put(key, value)
            if (previous == null) {
                newList.add(map.entries.first { it.key == key })
            } else {
                newList.asSequence().filter { it.key == key }.forEach {
                    it.setValue(value)
                }
            }
        }
        newList.sortBy { it.key }
        list = newList
    }

    override fun replace(
        key: K,
        value: V
    ): V? = if (map.containsKey(key)) put(key, value) else null

    override fun replace(
        key: K,
        oldValue: V,
        newValue: V
    ): Boolean =
        if (map[key] == oldValue) {
            put(key, newValue)
            true
        } else false

    override fun putIfAbsent(key: K, value: V): V? =
        putIfAbsent(key, value)

    override fun remove(key: K) =
        map.remove(key)?.also {
            list = list.filter { it.key != key }
        }

    override fun remove(key: K, value: V): Boolean =
        if (map[key] == value) {
            map.remove(key)
            true
        } else false

    override fun clear() {
        map.clear()
        list = emptyList()
    }

    override fun equals(other: Any?) = map == other

    override fun hashCode() = map.hashCode()
}
