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

expect interface Queue<E> : MutableCollection<E> {
    fun offer(element: E): Boolean
    fun remove(): E
    fun poll(): E?
    fun element(): E
    fun peek(): E
}

expect interface Deque<E> : Queue<E> {
    fun addFirst(element: E)
    fun addLast(element: E)
    fun offerFirst(element: E): Boolean
    fun offerLast(element: E): Boolean
    fun removeFirst(): E
    fun removeLast(): E
    fun pollFirst(): E?
    fun pollLast(): E?
    fun getFirst(): E
    fun getLast(): E
    fun peekFirst(): E
    fun peekLast(): E
    fun removeFirstOccurrence(element: Any): Boolean
    fun removeLastOccurrence(element: Any): Boolean
    fun push(element: E)
    fun pop(): E
    fun descendingIterator(): MutableIterator<E>
}

expect class ArrayDeque<E : Any>() : Deque<E> {
    constructor(size: Int)

    override val size: Int
    override fun contains(element: E): Boolean
    override fun isEmpty(): Boolean
    override fun iterator(): MutableIterator<E>
    override fun remove(element: E): Boolean
    override fun clear()

    override fun offer(element: E): Boolean
    override fun remove(): E
    override fun poll(): E?
    override fun element(): E
    override fun peek(): E

    override fun containsAll(elements: Collection<E>): Boolean
    override fun addAll(elements: Collection<E>): Boolean
    override fun removeAll(elements: Collection<E>): Boolean
    override fun retainAll(elements: Collection<E>): Boolean
    override fun add(element: E): Boolean

    override fun addFirst(element: E)
    override fun addLast(element: E)
    override fun offerFirst(element: E): Boolean
    override fun offerLast(element: E): Boolean
    override fun removeFirst(): E
    override fun removeLast(): E
    override fun pollFirst(): E?
    override fun pollLast(): E?
    override fun getFirst(): E
    override fun getLast(): E
    override fun peekFirst(): E
    override fun peekLast(): E
    override fun removeFirstOccurrence(element: Any): Boolean
    override fun removeLastOccurrence(element: Any): Boolean
    override fun push(element: E)
    override fun pop(): E
    override fun descendingIterator(): MutableIterator<E>
}

expect interface ConcurrentMap<K, V> : MutableMap<K, V> {
    fun putIfAbsent(key: K,
                    value: V): V?

    fun remove(key: K,
               value: V): Boolean

    fun replace(key: K,
                oldValue: V,
                newValue: V): Boolean

    fun replace(key: K,
                value: V): V?
}

expect class ConcurrentHashMap<K, V>() : ConcurrentMap<K, V> {
    override val size: Int
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override fun isEmpty(): Boolean
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override fun get(key: K): V?
    override fun put(key: K,
                     value: V): V?

    override fun putAll(from: Map<out K, V>)
    override fun putIfAbsent(key: K,
                             value: V): V?

    override fun remove(key: K): V?

    override fun remove(key: K,
                        value: V): Boolean

    override fun replace(key: K,
                         value: V): V?

    override fun replace(key: K,
                         oldValue: V,
                         newValue: V): Boolean

    override fun clear()
}

expect class ConcurrentHashSet<E>() : MutableSet<E> {
    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: E): Boolean
    override fun containsAll(elements: Collection<E>): Boolean
    override fun iterator(): MutableIterator<E>
    override fun add(element: E): Boolean
    override fun addAll(elements: Collection<E>): Boolean
    override fun remove(element: E): Boolean
    override fun removeAll(elements: Collection<E>): Boolean
    override fun retainAll(elements: Collection<E>): Boolean
    override fun clear()
}

expect class ConcurrentSortedMap<K : Comparable<K>, V>() : ConcurrentMap<K, V> {
    override val size: Int
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override fun isEmpty(): Boolean
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override fun get(key: K): V?
    override fun put(key: K,
                     value: V): V?

    override fun putAll(from: Map<out K, V>)
    override fun putIfAbsent(key: K,
                             value: V): V?

    override fun remove(key: K): V?

    override fun remove(key: K,
                        value: V): Boolean

    override fun replace(key: K,
                         value: V): V?

    override fun replace(key: K,
                         oldValue: V,
                         newValue: V): Boolean

    override fun clear()
}

expect class ConcurrentSortedSet<T : Comparable<T>>() : MutableSet<T> {
    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: T): Boolean
    override fun containsAll(elements: Collection<T>): Boolean
    override fun iterator(): MutableIterator<T>
    override fun add(element: T): Boolean
    override fun addAll(elements: Collection<T>): Boolean
    override fun remove(element: T): Boolean
    override fun removeAll(elements: Collection<T>): Boolean
    override fun retainAll(elements: Collection<T>): Boolean
    override fun clear()
}

/**
 * Returns an unmodifiable version of the given collection
 * @param T The type of elements
 * @receiver The collection
 * @return A read only view of the collection
 */
expect inline fun <T> Collection<T>.readOnly(): Collection<T>

/**
 * Returns an unmodifiable version of the given list
 * @param T The type of elements
 * @receiver The list
 * @return A read only view of the list
 */
expect inline fun <T> List<T>.readOnly(): List<T>

/**
 * Returns an unmodifiable version of the given set
 * @param T The type of elements
 * @receiver The set
 * @return A read only view of the set
 */
expect inline fun <T> Set<T>.readOnly(): Set<T>

/**
 * Returns an unmodifiable version of the given map
 * @param K The type of keys
 * @param V The type of values
 * @receiver The map
 * @return A read only view of the map
 */
expect inline fun <K, V> Map<K, V>.readOnly(): Map<K, V>

/**
 * Returns a synchronized version of the given collection
 * @param T The type of elements
 * @receiver The collection
 * @return A synchronized view of the collection
 */
expect inline fun <T> Collection<T>.synchronized(): Collection<T>

/**
 * Returns a synchronized version of the given collection
 * @param T The type of elements
 * @receiver The collection
 * @return A synchronized view of the collection
 */
expect inline fun <T> MutableCollection<T>.synchronized(): MutableCollection<T>

/**
 * Returns a synchronized version of the given list
 * @param T The type of elements
 * @receiver The list
 * @return A synchronized view of the list
 */
expect inline fun <T> List<T>.synchronized(): List<T>

/**
 * Returns a synchronized version of the given list
 * @param T The type of elements
 * @receiver The list
 * @return A synchronized view of the list
 */
expect inline fun <T> MutableList<T>.synchronized(): MutableList<T>

/**
 * Returns a synchronized version of the given set
 * @param T The type of elements
 * @receiver The set
 * @return A synchronized view of the set
 */
expect inline fun <T> Set<T>.synchronized(): Set<T>

/**
 * Returns a synchronized version of the given set
 * @param T The type of elements
 * @receiver The set
 * @return A synchronized view of the set
 */
expect inline fun <T> MutableSet<T>.synchronized(): MutableSet<T>

/**
 * Returns a synchronized version of the given map
 * @param K The type of keys
 * @param V The type of values
 * @receiver The map
 * @return A synchronized view of the map
 */
expect inline fun <K, V> Map<K, V>.synchronized(): Map<K, V>

/**
 * Returns a synchronized version of the given map
 * @param K The type of keys
 * @param V The type of values
 * @receiver The map
 * @return A synchronized view of the map
 */
expect inline fun <K, V> MutableMap<K, V>.synchronized(): MutableMap<K, V>

/**
 * Returns a map using the given enum as keys
 *
 * This can provide a performance advantage over normal maps
 * @param E The enum type
 * @param V The value type
 * @return A map using the given enum as keys
 */
expect inline fun <reified E : Enum<E>, V> EnumMap(): MutableMap<E, V>

/**
 * Adds the given [value] if [key] was not already in the map
 * @return The value that was already mapped or `null` if [value] got added
 */
expect fun <K, V> MutableMap<K, V>.putAbsent(key: K,
                                             value: V): V?

/**
 * Adds the given [value] if [key] was not already in the map
 * @return The value that was already mapped or `null` if [value] got added
 */
expect fun <K, V> ConcurrentMap<K, V>.putAbsent(key: K,
                                                value: V): V?

/**
 * Fetch the value for the [key] and remap it using [block]
 * @return The value returned from [block]
 */
expect fun <K, V : Any> MutableMap<K, V>.computeAlways(key: K,
                                                       block: (K, V?) -> V): V

/**
 * Fetch the value for the [key] and remap it using [block]
 * @return The value returned from [block]
 */
expect fun <K, V : Any> ConcurrentMap<K, V>.computeAlways(key: K,
                                                          block: (K, V?) -> V): V

/**
 * Fetch the value for the [key] and remap it using [block]
 *
 * Returning `null` in [block] will remove the value from the map
 * @return The value returned from [block]
 */
expect fun <K, V> MutableMap<K, V>.computeAlways(key: K,
                                                 block: (K, V?) -> V?): V?

/**
 * Fetch the value for the [key] and remap it using [block]
 *
 * Returning `null` in [block] will remove the value from the map
 * @return The value returned from [block]
 */
expect fun <K, V> ConcurrentMap<K, V>.computeAlways(key: K,
                                                    block: (K, V?) -> V?): V?

/**
 * Fetch the value for the [key], if it is not set, call [block] and add its
 * result
 * @return The value mapped to [key] at the end
 */
expect inline fun <K, V : Any> MutableMap<K, V>.computeAbsent(key: K,
                                                              block: (K) -> V): V

/**
 * Fetch the value for the [key], if it is not set, call [block] and add its
 * result
 * @return The value mapped to [key] at the end
 */
expect inline fun <K, V : Any> ConcurrentMap<K, V>.computeAbsent(key: K,
                                                                 block: (K) -> V): V

/**
 * Fetch the value for the [key], if it is not set, call [block] and add its
 * result
 *
 * Returning `null` in [block] will remove the value from the map
 * @return The value mapped to [key] at the end
 */
expect inline fun <K, V> MutableMap<K, V>.computeAbsent(key: K,
                                                        block: (K) -> V?): V?

/**
 * Fetch the value for the [key], if it is not set, call [block] and add its
 * result
 *
 * Returning `null` in [block] will remove the value from the map
 * @return The value mapped to [key] at the end
 */
expect inline fun <K, V> ConcurrentMap<K, V>.computeAbsent(key: K,
                                                           block: (K) -> V?): V?

expect fun <K, V> MutableMap<K, V>.removeEqual(key: K,
                                               value: V): Boolean
