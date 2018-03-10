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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.stdex

import java.util.*
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet

actual typealias Queue<E> = java.util.Queue<E>
actual typealias Deque<E> = java.util.Deque<E>
actual typealias ArrayDeque<E> = java.util.ArrayDeque<E>

actual typealias ConcurrentMap<K, V> = java.util.concurrent.ConcurrentMap<K, V>

actual class ConcurrentHashMap<K, V> private constructor(
    private val map: java.util.concurrent.ConcurrentHashMap<K, V>
) : ConcurrentMap<K, V> by map {
    actual constructor() : this(java.util.concurrent.ConcurrentHashMap<K, V>())

    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> =
        map.entries
    actual override val keys: MutableSet<K>
    actual override val values: MutableCollection<V>

    init {
        val map: ConcurrentMap<K, V> = map
        keys = map.keys
        values = map.values
    }

    actual override fun put(key: K, value: V): V? = map.put(key, value)

    actual override fun putAll(from: Map<out K, V>) = map.putAll(from)

    actual override fun replace(key: K, value: V): V? = map.replace(key, value)

    actual override fun replace(key: K, oldValue: V, newValue: V): Boolean =
        map.replace(key, oldValue, newValue)

    actual override fun putIfAbsent(key: K, value: V): V? =
        map.putIfAbsent(key, value)

    actual override fun remove(key: K): V? = map.remove(key)

    actual override fun remove(key: K, value: V): Boolean =
        map.remove(key, value)

    actual override fun clear() = map.clear()

    override fun equals(other: Any?) = map == other
    override fun hashCode() = map.hashCode()
    override fun toString() = map.toString()
}

actual class ConcurrentHashSet<E> : MutableSet<E> {
    actual override fun addAll(elements: Collection<E>): Boolean {
        var added = false
        for (element in elements) {
            added = add(element) || added
        }
        return added
    }

    actual override fun removeAll(elements: Collection<E>) =
        map.keys.removeAll(elements)

    actual override fun retainAll(elements: Collection<E>) =
        map.keys.retainAll(elements)

    actual override fun contains(element: E) = map.keys.contains(element)
    actual override fun containsAll(elements: Collection<E>) =
        map.keys.containsAll(elements)

    actual override fun isEmpty() = map.isEmpty()

    private val map = java.util.concurrent.ConcurrentHashMap<E, Unit>()

    actual override val size get() = map.size

    actual override fun add(element: E) = map.put(element, Unit) == null
    actual override fun remove(element: E) = map.remove(element) != null
    actual override fun clear() = map.clear()
    actual override fun iterator(): MutableIterator<E> = map.keys.iterator()

    override fun equals(other: Any?) = map.keys == other
    override fun hashCode() = map.keys.hashCode()
    override fun toString() = map.keys.toString()
}

actual class ConcurrentSortedMap<K : Comparable<K>, V> : AbstractMap<K, V>(),
    ConcurrentMap<K, V> {
    private val map: java.util.concurrent.ConcurrentMap<K, V> =
        ConcurrentSkipListMap<K, V>()

    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> =
        map.entries
    actual override val keys: MutableSet<K> = map.keys
    actual override val values: MutableCollection<V> = map.values

    actual override fun put(
        key: K,
        value: V
    ): V? = map.put(key, value)

    actual override fun putAll(from: Map<out K, V>) = map.putAll(from)

    actual override fun replace(
        key: K,
        value: V
    ): V? = map.replace(key, value)

    actual override fun replace(key: K, oldValue: V, newValue: V): Boolean =
        map.replace(key, oldValue, newValue)

    actual override fun putIfAbsent(key: K, value: V): V? =
        map.putIfAbsent(key, value)

    actual override fun remove(key: K): V? = map.remove(key)

    actual override fun remove(key: K, value: V): Boolean =
        map.remove(key, value)

    actual override fun clear() = map.clear()
}

actual class ConcurrentSortedSet<T : Comparable<T>> : AbstractSet<T>(),
    MutableSet<T> {
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

actual inline fun <T> Collection<T>.readOnly(): Collection<T> =
    Collections.unmodifiableCollection(this)

actual inline fun <T> List<T>.readOnly(): List<T> =
    Collections.unmodifiableList(this)

actual inline fun <T> Set<T>.readOnly(): Set<T> =
    Collections.unmodifiableSet(this)

actual inline fun <K, V> Map<K, V>.readOnly(): Map<K, V> =
    Collections.unmodifiableMap(this)

actual inline fun <T> Collection<T>.synchronized(): Collection<T> =
    Collections.synchronizedCollection(this)

@JvmName("synchronizedMut")
actual inline fun <T> MutableCollection<T>.synchronized(): MutableCollection<T> =
    Collections.synchronizedCollection(this)

actual inline fun <T> List<T>.synchronized(): List<T> =
    Collections.synchronizedList(this)

@JvmName("synchronizedMut")
actual inline fun <T> MutableList<T>.synchronized(): MutableList<T> =
    Collections.synchronizedList(this)

actual inline fun <T> Set<T>.synchronized(): Set<T> =
    Collections.synchronizedSet(this)

@JvmName("synchronizedMut")
actual inline fun <T> MutableSet<T>.synchronized(): MutableSet<T> =
    Collections.synchronizedSet(this)

actual inline fun <K, V> Map<K, V>.synchronized(): Map<K, V> =
    Collections.synchronizedMap(this)

@JvmName("synchronizedMut")
actual inline fun <K, V> MutableMap<K, V>.synchronized(): MutableMap<K, V> =
    Collections.synchronizedMap(this)

actual inline fun <reified E : Enum<E>, V> EnumMap(): MutableMap<E, V> =
    java.util.EnumMap<E, V>(E::class.java)

actual fun <K, V> MutableMap<K, V>.putAbsent(key: K, value: V): V? {
    return if (this is ConcurrentMap) {
        this.putAbsent(key, value)
    } else {
        this[key]?.let { return it }
        put(key, value)
        null
    }
}

actual inline fun <K, V> ConcurrentMap<K, V>.putAbsent(key: K, value: V): V? =
    putIfAbsent(key, value)

actual inline fun <K, V : Any> MutableMap<K, V>.computeAlways(
    key: K,
    block: (K, V?) -> V
): V {
    return if (this is ConcurrentMap) {
        this.computeAlways(key, block)
    } else {
        val old = this[key]
        val new = block(key, old)
        this[key] = new
        new
    }
}

actual inline fun <K, V : Any> ConcurrentMap<K, V>.computeAlways(
    key: K,
    block: (K, V?) -> V
): V {
    while (true) {
        var old = this[key]
        while (true) {
            val new = block(key, old)
            if (old == null) {
                old = putAbsent(key, new)
                return if (old == null) {
                    new
                } else {
                    continue
                }
            }
            return if (replace(key, old, new)) {
                new
            } else {
                break
            }
        }
    }
}

@JvmName("computeAlwaysNullable")
actual inline fun <K, V> MutableMap<K, V>.computeAlways(
    key: K,
    block: (K, V?) -> V?
): V? {
    return if (this is ConcurrentMap) {
        this.computeAlways(key, block)
    } else {
        val old = this[key]
        val new = block(key, old)
        if (new != null) {
            this[key] = new
        } else if (old != null || containsKey(key)) {
            remove(key)
        }
        new
    }
}

@JvmName("computeAlwaysNullable")
actual inline fun <K, V> ConcurrentMap<K, V>.computeAlways(
    key: K,
    block: (K, V?) -> V?
): V? {
    while (true) {
        var old = this[key]
        while (true) {
            val new = block(key, old)
            if (new == null) {
                if (old == null || remove(key, old)) {
                    return new
                }
                break
            }
            if (old == null) {
                old = putAbsent(key, new)
                return if (old == null) {
                    new
                } else {
                    continue
                }
            }
            return if (replace(key, old, new)) {
                new
            } else {
                break
            }
        }
    }
}

actual inline fun <K, V : Any> MutableMap<K, V>.computeAbsent(
    key: K,
    block: (K) -> V
): V {
    return if (this is ConcurrentMap) {
        // Should we try to eliminate the second inline of block?
        this.computeAbsent(key, block)
    } else {
        this[key]?.let { return it }
        val new = block(key)
        putAbsent(key, new) ?: new
    }
}

actual inline fun <K, V : Any> ConcurrentMap<K, V>.computeAbsent(
    key: K,
    block: (K) -> V
): V {
    this[key]?.let { return it }
    val new = block(key)
    return putAbsent(key, new) ?: new
}

@JvmName("computeAbsentNullable")
actual inline fun <K, V> MutableMap<K, V>.computeAbsent(
    key: K,
    block: (K) -> V?
): V? {
    return if (this is ConcurrentMap) {
        // Should we try to eliminate the second inline of block?
        this.computeAbsent(key, block)
    } else {
        this[key]?.let { return it }
        val new = block(key) ?: return null
        putAbsent(key, new) ?: new
    }
}

@JvmName("computeAbsentNullable")
actual inline fun <K, V> ConcurrentMap<K, V>.computeAbsent(
    key: K,
    block: (K) -> V?
): V? {
    this[key]?.let { return it }
    val new = block(key) ?: return null
    return putAbsent(key, new) ?: new
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual inline fun <K, V> MutableMap<K, V>.removeEqual(
    key: K,
    value: V
): Boolean = remove(key, value)
