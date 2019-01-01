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

@file:JvmName("CollectionsJVMKt")

package org.tobi29.stdex

import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet

actual typealias Queue<E> = java.util.Queue<E>
actual typealias AbstractQueue<E> = java.util.AbstractQueue<E>
actual typealias Deque<E> = java.util.Deque<E>
actual typealias ArrayDeque<E> = java.util.ArrayDeque<E>

actual typealias ConcurrentMap<K, V> = java.util.concurrent.ConcurrentMap<K, V>

actual class ConcurrentHashMap<K, V> private constructor(
    private val map: java.util.concurrent.ConcurrentHashMap<K, V>
) : ConcurrentMap<K, V> by map {
    actual constructor() : this(java.util.concurrent.ConcurrentHashMap<K, V>())

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> =
        map.entries
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>

    init {
        val map: ConcurrentMap<K, V> = map
        keys = map.keys
        values = map.values
    }

    override fun put(key: K, value: V): V? = map.put(key, value)

    override fun putAll(from: Map<out K, V>) = map.putAll(from)

    override fun replace(key: K, value: V): V? = map.replace(key, value)

    override fun replace(key: K, oldValue: V, newValue: V): Boolean =
        map.replace(key, oldValue, newValue)

    override fun putIfAbsent(key: K, value: V): V? =
        map.putIfAbsent(key, value)

    override fun remove(key: K): V? = map.remove(key)

    override fun remove(key: K, value: V): Boolean =
        map.remove(key, value)

    override fun clear() = map.clear()

    override fun equals(other: Any?) = map == other
    override fun hashCode() = map.hashCode()
    override fun toString() = map.toString()
}

actual class ConcurrentHashSet<E> : MutableSet<E> {
    override fun addAll(elements: Collection<E>): Boolean {
        var added = false
        for (element in elements) {
            added = add(element) || added
        }
        return added
    }

    override fun removeAll(elements: Collection<E>) =
        map.keys.removeAll(elements)

    override fun retainAll(elements: Collection<E>) =
        map.keys.retainAll(elements)

    override fun contains(element: E) = map.keys.contains(element)
    override fun containsAll(elements: Collection<E>) =
        map.keys.containsAll(elements)

    override fun isEmpty() = map.isEmpty()

    // We need to shim the type for Android compatibility
    private val map: java.util.concurrent.ConcurrentMap<E, Unit> =
        ConcurrentHashMap()

    override val size get() = map.size

    override fun add(element: E) = map.put(element, Unit) == null
    override fun remove(element: E) = map.remove(element) != null
    override fun clear() = map.clear()
    override fun iterator(): MutableIterator<E> = map.keys.iterator()

    override fun equals(other: Any?) = map.keys == other
    override fun hashCode() = map.keys.hashCode()
    override fun toString() = map.keys.toString()
}

actual class ConcurrentSortedMap<K : Comparable<K>, V> :
    AbstractMutableMap<K, V>(),
    ConcurrentMap<K, V> {
    private val map: java.util.concurrent.ConcurrentMap<K, V> =
        ConcurrentSkipListMap<K, V>()

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> =
        map.entries
    override val keys: MutableSet<K> = map.keys
    override val values: MutableCollection<V> = map.values

    override fun put(
        key: K,
        value: V
    ): V? = map.put(key, value)

    override fun putAll(from: Map<out K, V>) = map.putAll(from)

    override fun replace(
        key: K,
        value: V
    ): V? = map.replace(key, value)

    override fun replace(key: K, oldValue: V, newValue: V): Boolean =
        map.replace(key, oldValue, newValue)

    override fun putIfAbsent(key: K, value: V): V? =
        map.putIfAbsent(key, value)

    override fun remove(key: K): V? = map.remove(key)

    override fun remove(key: K, value: V): Boolean =
        map.remove(key, value)

    override fun clear() = map.clear()
}

actual class ConcurrentSortedSet<T : Comparable<T>> : AbstractMutableSet<T>(),
    MutableSet<T> {
    private val set = ConcurrentSkipListSet<T>()

    override val size get() = set.size

    override fun isEmpty() = set.isEmpty()

    override fun iterator() = set.iterator()

    override fun add(element: T) = set.add(element)

    override fun addAll(elements: Collection<T>) = set.addAll(elements)

    override fun clear() = set.clear()

    override fun remove(element: T) = set.remove(element)

    override fun removeAll(elements: Collection<T>) =
        set.removeAll(elements)

    override fun retainAll(elements: Collection<T>) =
        set.retainAll(elements)

    override fun contains(element: T) = set.contains(element)

    override fun containsAll(elements: Collection<T>) =
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

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun <T> Collection<T>.readOnly(): Collection<T> =
    java.util.Collections.unmodifiableCollection(this)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun <T> List<T>.readOnly(): List<T> =
    java.util.Collections.unmodifiableList(this)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun <T> Set<T>.readOnly(): Set<T> =
    java.util.Collections.unmodifiableSet(this)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun <K, V> Map<K, V>.readOnly(): Map<K, V> =
    java.util.Collections.unmodifiableMap(this)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun <T> Collection<T>.synchronized(): Collection<T> =
    java.util.Collections.synchronizedCollection(this)

@JvmName("synchronizedMut")
@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun <T> MutableCollection<T>.synchronized(): MutableCollection<T> =
    java.util.Collections.synchronizedCollection(this)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun <T> List<T>.synchronized(): List<T> =
    java.util.Collections.synchronizedList(this)

@JvmName("synchronizedMut")
@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun <T> MutableList<T>.synchronized(): MutableList<T> =
    java.util.Collections.synchronizedList(this)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun <T> Set<T>.synchronized(): Set<T> =
    java.util.Collections.synchronizedSet(this)

@JvmName("synchronizedMut")
@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun <T> MutableSet<T>.synchronized(): MutableSet<T> =
    java.util.Collections.synchronizedSet(this)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun <K, V> Map<K, V>.synchronized(): Map<K, V> =
    java.util.Collections.synchronizedMap(this)

@JvmName("synchronizedMut")
@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun <K, V> MutableMap<K, V>.synchronized(): MutableMap<K, V> =
    java.util.Collections.synchronizedMap(this)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun <reified E : Enum<E>, V> EnumMap(): MutableMap<E, V> =
    java.util.EnumMap<E, V>(E::class.java)

actual inline fun <K, V : Any> MutableMap<K, V>.computeAlways(
    key: K, block: (K, V?) -> V
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
    key: K, block: (K, V?) -> V
): V {
    while (true) {
        var old = this[key]
        while (true) {
            val new = block(key, old)
            if (old == null) {
                old = putIfAbsent(key, new)
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
    key: K, block: (K, V?) -> V?
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
    key: K, block: (K, V?) -> V?
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
                old = putIfAbsent(key, new)
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
    key: K, block: (K) -> V
): V {
    return if (this is ConcurrentMap) {
        // Should we try to eliminate the second inline of block?
        this.computeAbsent(key, block)
    } else {
        this[key]?.let { return it }
        val new = block(key)
        putIfAbsent(key, new) ?: new
    }
}

actual inline fun <K, V : Any> ConcurrentMap<K, V>.computeAbsent(
    key: K, block: (K) -> V
): V {
    this[key]?.let { return it }
    val new = block(key)
    return putIfAbsent(key, new) ?: new
}

@JvmName("computeAbsentNullable")
actual inline fun <K, V> MutableMap<K, V>.computeAbsent(
    key: K, block: (K) -> V?
): V? {
    return if (this is ConcurrentMap) {
        // Should we try to eliminate the second inline of block?
        this.computeAbsent(key, block)
    } else {
        this[key]?.let { return it }
        val new = block(key) ?: return null
        putIfAbsent(key, new) ?: new
    }
}

@JvmName("computeAbsentNullable")
actual inline fun <K, V> ConcurrentMap<K, V>.computeAbsent(
    key: K, block: (K) -> V?
): V? {
    this[key]?.let { return it }
    val new = block(key) ?: return null
    return putIfAbsent(key, new) ?: new
}

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun <K, V> MutableMap<K, V>.removeEqual(
    key: K, value: V
): Boolean = remove(key, value)
