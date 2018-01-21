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

package org.tobi29.stdex

actual interface Queue<E> : MutableCollection<E> {
    actual fun offer(element: E): Boolean
    actual fun remove(): E
    actual fun poll(): E?
    actual fun element(): E
    actual fun peek(): E
}

actual interface Deque<E> : Queue<E> {
    actual fun addFirst(element: E)
    actual fun addLast(element: E)
    actual fun offerFirst(element: E): Boolean
    actual fun offerLast(element: E): Boolean
    actual fun removeFirst(): E
    actual fun removeLast(): E
    actual fun pollFirst(): E?
    actual fun pollLast(): E?
    actual fun getFirst(): E
    actual fun getLast(): E
    actual fun peekFirst(): E
    actual fun peekLast(): E
    actual fun removeFirstOccurrence(element: Any): Boolean
    actual fun removeLastOccurrence(element: Any): Boolean
    actual fun push(element: E)
    actual fun pop(): E
    actual fun descendingIterator(): MutableIterator<E>
}

actual interface ConcurrentMap<K, V> : MutableMap<K, V> {
    actual fun replace(key: K,
                       value: V): V?

    actual fun replace(key: K,
                       oldValue: V,
                       newValue: V): Boolean

    actual fun putIfAbsent(key: K,
                           value: V): V?

    actual fun remove(key: K,
                      value: V): Boolean
}

actual class ConcurrentHashMap<K, V> : HashMap<K, V>(),
        ConcurrentMap<K, V> {
    actual override fun replace(key: K,
                                value: V): V? =
            if (containsKey(key)) put(key, value) else null

    actual override fun replace(key: K,
                                oldValue: V,
                                newValue: V): Boolean =
            if (this[key] == oldValue) {
                put(key, newValue)
                true
            } else false

    actual override fun putIfAbsent(key: K,
                                    value: V): V? =
            putIfAbsent(key, value)

    actual override fun remove(key: K,
                               value: V): Boolean =
            if (this[key] == value) {
                remove(key)
                true
            } else false
}

actual class ConcurrentHashSet<E> : MutableSet<E> {
    private val map = ConcurrentHashMap<E, Unit>()

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

    actual override val size get() = map.size

    actual override fun add(element: E) = map.put(element, Unit) == null
    actual override fun remove(element: E) = map.remove(element) != null
    actual override fun clear() = map.clear()
    actual override fun iterator(): MutableIterator<E> = map.keys.iterator()

    override fun equals(other: Any?) = map.keys == other
    override fun hashCode() = map.keys.hashCode()
    override fun toString() = map.keys.toString()
}

actual inline fun <T> Collection<T>.readOnly(): Collection<T> =
        this

actual inline fun <T> List<T>.readOnly(): List<T> =
        this

actual inline fun <T> Set<T>.readOnly(): Set<T> =
        this

actual inline fun <K, V> Map<K, V>.readOnly(): Map<K, V> =
        this

actual inline fun <T> Collection<T>.synchronized(): Collection<T> =
        this

actual inline fun <T> MutableCollection<T>.synchronized(): MutableCollection<T> =
        this

actual inline fun <T> List<T>.synchronized(): List<T> =
        this

actual inline fun <T> MutableList<T>.synchronized(): MutableList<T> =
        this

actual inline fun <T> Set<T>.synchronized(): Set<T> =
        this

actual inline fun <T> MutableSet<T>.synchronized(): MutableSet<T> =
        this

actual inline fun <K, V> Map<K, V>.synchronized(): Map<K, V> =
        this

actual inline fun <K, V> MutableMap<K, V>.synchronized(): MutableMap<K, V> =
        this

actual inline fun <reified E : Enum<E>, V> EnumMap(): MutableMap<E, V> = HashMap()

actual fun <K, V> MutableMap<K, V>.putAbsent(key: K,
                                             value: V): V? {
    this[key]?.let { return it }
    put(key, value)
    return null
}

actual inline fun <K, V> ConcurrentMap<K, V>.putAbsent(key: K,
                                                       value: V): V? {
    this[key]?.let { return it }
    put(key, value)
    return null
}

actual fun <K, V : Any> MutableMap<K, V>.computeAlways(key: K,
                                                       block: (K, V?) -> V): V {
    val old = this[key]
    val new = block(key, old)
    this[key] = new
    return new
}

actual fun <K, V : Any> ConcurrentMap<K, V>.computeAlways(key: K,
                                                          block: (K, V?) -> V): V {
    val old = this[key]
    val new = block(key, old)
    this[key] = new
    return new
}

@JsName("computeAlwaysNullable")
actual fun <K, V> MutableMap<K, V>.computeAlways(key: K,
                                                 block: (K, V?) -> V?): V? {
    val old = this[key]
    val new = block(key, old)
    if (new != null) {
        this[key] = new
    } else if (old != null || containsKey(key)) {
        remove(key)
    }
    return new
}

@JsName("computeAlwaysNullableConcurrent")
actual fun <K, V> ConcurrentMap<K, V>.computeAlways(key: K,
                                                    block: (K, V?) -> V?): V? {
    val old = this[key]
    val new = block(key, old)
    if (new != null) {
        this[key] = new
    } else if (old != null || containsKey(key)) {
        remove(key)
    }
    return new
}

actual inline fun <K, V : Any> MutableMap<K, V>.computeAbsent(key: K,
                                                              block: (K) -> V): V {
    this[key]?.let { return it }
    val new = block(key)
    return putAbsent(key, new) ?: new
}

actual inline fun <K, V : Any> ConcurrentMap<K, V>.computeAbsent(key: K,
                                                                 block: (K) -> V): V {
    this[key]?.let { return it }
    val new = block(key)
    return putAbsent(key, new) ?: new
}

@JsName("computeAbsentNullable")
actual inline fun <K, V> MutableMap<K, V>.computeAbsent(key: K,
                                                        block: (K) -> V?): V? {
    this[key]?.let { return it }
    val new = block(key) ?: return null
    return putAbsent(key, new) ?: new
}

@JsName("computeAbsentNullableConcurrent")
actual inline fun <K, V> ConcurrentMap<K, V>.computeAbsent(key: K,
                                                           block: (K) -> V?): V? {
    this[key]?.let { return it }
    val new = block(key) ?: return null
    return putAbsent(key, new) ?: new
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual inline fun <K, V> MutableMap<K, V>.removeEqual(key: K,
                                                      value: V): Boolean =
        if (this[key] == value) {
            remove(key)
            true
        } else false
