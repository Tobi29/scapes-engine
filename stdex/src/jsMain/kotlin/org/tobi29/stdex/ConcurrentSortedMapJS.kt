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
    private var list = emptyList<Entry<K, V>>()

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> =
        object : AbstractMutableSet<MutableMap.MutableEntry<K, V>>(),
            MutableSet<MutableMap.MutableEntry<K, V>> {
            override val size get() = list.size

            override fun add(element: MutableMap.MutableEntry<K, V>): Boolean =
                throw UnsupportedOperationException(
                    "Add is not supported on entries"
                )

            override fun addAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean =
                throw UnsupportedOperationException(
                    "Add is not supported on entries"
                )

            override fun clear() = this@ConcurrentSortedMap.clear()

            override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> =
                IteratorImpl(this@ConcurrentSortedMap, list.iterator())

            override fun remove(element: MutableMap.MutableEntry<K, V>) =
                this@ConcurrentSortedMap.remove(element.key) != null
        }

    private fun binarySearchKey(key: K) = list.binarySearchKey(key)

    private fun List<Entry<K, V>>.binarySearchKey(key: K) =
        binarySearchBy(key) { it.key }

    override fun get(key: K) =
        binarySearchKey(key).let { if (it < 0) null else list[it].value }

    override fun put(key: K, value: V): V? {
        var i = binarySearchKey(key)
        return if (i < 0) {
            i = -i - 1
            val newList = ArrayList<Entry<K, V>>(list.size + 1)
            for (j in 0 until i) {
                newList.add(list[j])
            }
            newList.add(Entry(key, value))
            for (j in i until list.size) {
                newList.add(list[j])
            }
            list = newList
            null
        } else {
            val entry = list[i]
            entry.value.also { entry.setValue(value) }
        }
    }

    override fun putAll(from: Map<out K, V>) {
        val newList = ArrayList<Entry<K, V>>(list.size + from.size)
        newList.addAll(list)
        for ((key, value) in from) {
            var i = newList.binarySearchKey(key)
            if (i < 0) {
                i = -i - 1
                newList.add(i, Entry(key, value))
            } else {
                newList[i].setValue(value)
            }
        }
        list = newList
    }

    override fun replace(key: K, value: V): V? =
        if (containsKey(key)) put(key, value) else null

    override fun replace(key: K, oldValue: V, newValue: V): Boolean =
        if (this[key] == oldValue) {
            put(key, newValue)
            true
        } else false

    override fun putIfAbsent(key: K, value: V): V? {
        this[key]?.let { return it }
        put(key, value)
        return null
    }

    override fun remove(key: K): V? {
        val i = binarySearchKey(key)
        return if (i < 0) {
            null
        } else {
            val newList = ArrayList<Entry<K, V>>(list.size - 1)
            for (j in 0 until i) {
                newList.add(list[j])
            }
            for (j in i + 1 until list.size) {
                newList.add(list[j])
            }
            list = newList
            list[i].value
        }
    }

    override fun remove(key: K, value: V): Boolean =
        if (this[key] == value) {
            remove(key)
            true
        } else false

    override fun clear() {
        list = emptyList()
    }

    private class Entry<K, V>(
        override val key: K, value: V
    ) : MutableMap.MutableEntry<K, V> {
        override var value: V = value
            private set

        override fun setValue(newValue: V): V = value.also { value = newValue }

        override fun toString() = "$key=$value"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Map.Entry<*, *>) return false
            return key == other.key && value == other.value
        }

        override fun hashCode() =
            (key?.hashCode() ?: 0) xor (value?.hashCode() ?: 0)
    }

    private class IteratorImpl<K : Comparable<K>, V>(
        val map: ConcurrentSortedMap<K, V>,
        val iterator: Iterator<MutableMap.MutableEntry<K, V>>
    ) : MutableIterator<MutableMap.MutableEntry<K, V>> {
        private var current: MutableMap.MutableEntry<K, V>? = null

        override fun hasNext() = iterator.hasNext()

        override fun next() = iterator.next().also { current = it }

        override fun remove() {
            map.remove(current?.key ?: error("No element in iterator yet"))
        }
    }
}
