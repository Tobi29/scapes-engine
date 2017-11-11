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

actual class ConcurrentSortedMap<K : Comparable<K>, V> : AbstractMap<K, V>(), ConcurrentMap<K, V> {
    private val map = HashMap<K, V>()
    private var list = emptyList<MutableMap.MutableEntry<K, V>>()

    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> =
            object : AbstractSet<MutableMap.MutableEntry<K, V>>(), MutableSet<MutableMap.MutableEntry<K, V>> {
                override val size get() = map.size

                override fun add(element: MutableMap.MutableEntry<K, V>): Boolean =
                        throw UnsupportedOperationException(
                                "Add is not supported on entries")

                override fun addAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean =
                        throw UnsupportedOperationException(
                                "Add is not supported on entries")

                override fun clear() = this@ConcurrentSortedMap.clear()

                override fun iterator() =
                        list.iterator().let { iterator ->
                            object : MutableIterator<MutableMap.MutableEntry<K, V>> {
                                private var current: MutableMap.MutableEntry<K, V>? = null

                                override fun hasNext() = iterator.hasNext()

                                override fun next() = iterator.next().also { current = it }

                                override fun remove() {
                                    (current
                                            ?: throw IllegalStateException(
                                            "No element in iterator yet")
                                            ).let { current ->
                                        remove(current)
                                    }
                                }
                            }
                        }

                override fun remove(element: MutableMap.MutableEntry<K, V>) =
                        this@ConcurrentSortedMap.remove(element.key) != null

                override fun removeAll(elements: Collection<MutableMap.MutableEntry<K, V>>) =
                        iterator().let { iterator ->
                            var removed = false
                            while (iterator.hasNext()) {
                                if (iterator.next() in elements) {
                                    iterator.remove()
                                    removed = true
                                }
                            }
                            removed
                        }

                override fun retainAll(elements: Collection<MutableMap.MutableEntry<K, V>>) =
                        iterator().let { iterator ->
                            var removed = false
                            while (iterator.hasNext()) {
                                if (iterator.next() !in elements) {
                                    iterator.remove()
                                    removed = true
                                }
                            }
                            removed
                        }

                override fun equals(other: Any?) = map.entries == other

                override fun hashCode() = map.entries.hashCode()
            }

    actual override val keys: MutableSet<K> =
            object : AbstractSet<K>(), MutableSet<K> {
                override val size get() = map.size

                override fun add(element: K): Boolean =
                        throw UnsupportedOperationException(
                                "Add is not supported on entries")

                override fun addAll(elements: Collection<K>): Boolean =
                        throw UnsupportedOperationException(
                                "Add is not supported on entries")

                override fun clear() = this@ConcurrentSortedMap.clear()

                override fun iterator() =
                        entries.iterator().let { iterator ->
                            object : MutableIterator<K> {
                                override fun hasNext() = iterator.hasNext()

                                override fun next() = iterator.next().key

                                override fun remove() = iterator.remove()
                            }
                        }

                override fun remove(element: K) =
                        this@ConcurrentSortedMap.remove(element) != null

                override fun removeAll(elements: Collection<K>) =
                        iterator().let { iterator ->
                            var removed = false
                            while (iterator.hasNext()) {
                                if (iterator.next() in elements) {
                                    iterator.remove()
                                    removed = true
                                }
                            }
                            removed
                        }

                override fun retainAll(elements: Collection<K>) =
                        iterator().let { iterator ->
                            var removed = false
                            while (iterator.hasNext()) {
                                if (iterator.next() !in elements) {
                                    iterator.remove()
                                    removed = true
                                }
                            }
                            removed
                        }

                override fun equals(other: Any?) = map.keys == other

                override fun hashCode() = map.keys.hashCode()
            }

    actual override val values: MutableCollection<V> =
            object : AbstractCollection<V>(), MutableCollection<V> {
                override val size get() = map.size

                override fun add(element: V): Boolean =
                        throw UnsupportedOperationException(
                                "Add is not supported on entries")

                override fun addAll(elements: Collection<V>): Boolean =
                        throw UnsupportedOperationException(
                                "Add is not supported on entries")

                override fun clear() = this@ConcurrentSortedMap.clear()

                override fun iterator() =
                        entries.iterator().let { iterator ->
                            object : MutableIterator<V> {
                                override fun hasNext() = iterator.hasNext()

                                override fun next() = iterator.next().value

                                override fun remove() = iterator.remove()
                            }
                        }

                override fun remove(element: V) =
                        iterator().let { iterator ->
                            while (iterator.hasNext()) {
                                if (iterator.next() == element) {
                                    iterator.remove()
                                    return@let true
                                }
                            }
                            false
                        }

                override fun removeAll(elements: Collection<V>) =
                        iterator().let { iterator ->
                            var removed = false
                            while (iterator.hasNext()) {
                                if (iterator.next() in elements) {
                                    iterator.remove()
                                    removed = true
                                }
                            }
                            removed
                        }

                override fun retainAll(elements: Collection<V>) =
                        iterator().let { iterator ->
                            var removed = false
                            while (iterator.hasNext()) {
                                if (iterator.next() !in elements) {
                                    iterator.remove()
                                    removed = true
                                }
                            }
                            removed
                        }

                override fun equals(other: Any?) = map.values == other

                override fun hashCode() = map.values.hashCode()
            }

    actual override fun put(key: K,
                            value: V) =
            map.put(key, value).also { previous ->
                if (previous == null) {
                    val newList = ArrayList<MutableMap.MutableEntry<K, V>>(
                            list.size + 1)
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

    actual override fun putAll(from: Map<out K, V>) {
        val newList = ArrayList<MutableMap.MutableEntry<K, V>>(
                list.size + from.size)
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

    actual override fun remove(key: K) =
            map.remove(key)?.also {
                list = list.filter { it.key != key }
            }

    actual override fun clear() {
        map.clear()
        list = emptyList()
    }

    override fun equals(other: Any?) = map == other

    override fun hashCode() = map.hashCode()
}
