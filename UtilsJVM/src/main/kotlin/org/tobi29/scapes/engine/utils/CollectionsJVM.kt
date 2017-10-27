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

    private val map = ConcurrentHashMap<E, Unit>()

    actual override val size get() = map.size

    actual override fun add(element: E) = map.put(element, Unit) == null
    actual override fun remove(element: E) = map.remove(element) != null
    actual override fun clear() = map.clear()
    actual override fun iterator(): MutableIterator<E> = map.keys.iterator()

    override fun equals(other: Any?) = map.keys == other
    override fun hashCode() = map.keys.hashCode()
    override fun toString() = map.keys.toString()
}

// TODO: Use type alias
actual class ArrayDeque<E> actual constructor(size: Int) : Deque<E> {
    actual constructor() : this(16)

    private val deque = java.util.ArrayDeque<E>(size)

    actual override val size get() = deque.size

    actual override fun contains(element: E) = deque.contains(element)
    actual override fun containsAll(elements: Collection<E>) = deque.containsAll(
            elements)

    actual override fun isEmpty() = deque.isEmpty()
    actual override fun add(element: E) = deque.add(element)
    actual override fun addAll(elements: Collection<E>) = deque.addAll(elements)
    actual override fun clear() = deque.clear()
    actual override fun iterator() = deque.iterator()
    actual override fun remove(element: E) = deque.remove(element)
    actual override fun removeAll(elements: Collection<E>) = deque.removeAll(
            elements)

    actual override fun retainAll(elements: Collection<E>) = deque.retainAll(
            elements)

    actual override fun offer(element: E) = deque.offer(element)
    actual override fun remove(): E = deque.remove()
    actual override fun poll(): E? = deque.poll()
    actual override fun element(): E = deque.element()
    actual override fun peek(): E = deque.peek()

    actual override fun addFirst(element: E) = deque.addFirst(element)
    actual override fun addLast(element: E) = deque.addLast(element)
    actual override fun offerFirst(element: E) = deque.offerFirst(element)
    actual override fun offerLast(element: E) = deque.offerLast(element)
    actual override fun removeFirst(): E = deque.removeFirst()
    actual override fun removeLast(): E = deque.removeLast()
    actual override fun pollFirst(): E? = deque.pollFirst()
    actual override fun pollLast(): E? = deque.pollLast()
    actual override fun getFirst(): E = deque.first
    actual override fun getLast(): E = deque.last
    actual override fun peekFirst(): E = deque.peekFirst()
    actual override fun peekLast(): E = deque.peekLast()
    actual override fun removeFirstOccurrence(element: E) =
            deque.removeFirstOccurrence(element)

    actual override fun removeLastOccurrence(element: E) =
            deque.removeLastOccurrence(element)

    actual override fun push(element: E) = deque.push(element)
    actual override fun pop(): E = deque.pop()
    actual override fun descendingIterator(): MutableIterator<E> =
            deque.descendingIterator()

    override fun equals(other: Any?) = deque == other
    override fun hashCode() = deque.hashCode()
    override fun toString() = deque.toString()
}

// TODO: Use type alias
actual class ConcurrentHashMap<K, V> : ConcurrentMap<K, V>, java.util.concurrent.ConcurrentMap<K, V> {
    private val map = java.util.concurrent.ConcurrentHashMap<K, V>()

    actual override val size: Int get() = map.size
    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = map.entries
    actual override val keys: MutableSet<K> get() = map.keys
    actual override val values: MutableCollection<V> get() = map.values

    override fun replace(key: K,
                         value: V): V? = map.replace(key, value)

    actual override fun replace(key: K,
                              oldValue: V,
                              newValue: V): Boolean =
            map.replace(key, oldValue, newValue)

    actual override fun containsValue(value: V) = map.containsValue(value)

    actual override fun remove(key: K): V? = map.remove(key)

    override fun remove(key: K,
                        value: V) = map.remove(key, value)

    actual override fun get(key: K): V? = map[key]

    override fun putIfAbsent(key: K,
                             value: V): V? = map.putIfAbsent(key, value)

    actual override fun containsKey(key: K) = map.containsKey(key)

    actual override fun isEmpty() = map.isEmpty()

    actual override fun clear() = map.clear()

    actual override fun put(key: K,
                          value: V): V? = map.put(key, value)

    actual override fun putAll(from: Map<out K, V>) = map.putAll(from)

    override fun equals(other: Any?) = map == other
    override fun hashCode() = map.hashCode()
    override fun toString() = map.toString()
}
