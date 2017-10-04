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

impl class ConcurrentHashSet<E> : MutableSet<E> {
    impl override fun addAll(elements: Collection<E>): Boolean {
        var added = false
        for (element in elements) {
            added = add(element) || added
        }
        return added
    }

    impl override fun removeAll(elements: Collection<E>) =
            map.keys.removeAll(elements)

    impl override fun retainAll(elements: Collection<E>) =
            map.keys.retainAll(elements)

    impl override fun contains(element: E) = map.keys.contains(element)
    impl override fun containsAll(elements: Collection<E>) =
            map.keys.containsAll(elements)

    impl override fun isEmpty() = map.isEmpty()

    private val map = ConcurrentHashMap<E, Unit>()

    impl override val size get() = map.size

    impl override fun add(element: E) = map.put(element, Unit) == null
    impl override fun remove(element: E) = map.remove(element) != null
    impl override fun clear() = map.clear()
    impl override fun iterator(): MutableIterator<E> = map.keys.iterator()

    override fun equals(other: Any?) = map.keys == other
    override fun hashCode() = map.keys.hashCode()
    override fun toString() = map.keys.toString()
}

// TODO: Use type alias
impl class ArrayDeque<E> impl constructor(size: Int) : Deque<E> {
    impl constructor() : this(16)

    private val deque = java.util.ArrayDeque<E>(size)

    impl override val size get() = deque.size

    impl override fun contains(element: E) = deque.contains(element)
    impl override fun containsAll(elements: Collection<E>) = deque.containsAll(
            elements)

    impl override fun isEmpty() = deque.isEmpty()
    impl override fun add(element: E) = deque.add(element)
    impl override fun addAll(elements: Collection<E>) = deque.addAll(elements)
    impl override fun clear() = deque.clear()
    impl override fun iterator() = deque.iterator()
    impl override fun remove(element: E) = deque.remove(element)
    impl override fun removeAll(elements: Collection<E>) = deque.removeAll(
            elements)

    impl override fun retainAll(elements: Collection<E>) = deque.retainAll(
            elements)

    impl override fun offer(element: E) = deque.offer(element)
    impl override fun remove(): E = deque.remove()
    impl override fun poll(): E? = deque.poll()
    impl override fun element(): E = deque.element()
    impl override fun peek(): E = deque.peek()

    impl override fun addFirst(element: E) = deque.addFirst(element)
    impl override fun addLast(element: E) = deque.addLast(element)
    impl override fun offerFirst(element: E) = deque.offerFirst(element)
    impl override fun offerLast(element: E) = deque.offerLast(element)
    impl override fun removeFirst(): E = deque.removeFirst()
    impl override fun removeLast(): E = deque.removeLast()
    impl override fun pollFirst(): E? = deque.pollFirst()
    impl override fun pollLast(): E? = deque.pollLast()
    impl override fun getFirst(): E = deque.first
    impl override fun getLast(): E = deque.last
    impl override fun peekFirst(): E = deque.peekFirst()
    impl override fun peekLast(): E = deque.peekLast()
    impl override fun removeFirstOccurrence(element: E) =
            deque.removeFirstOccurrence(element)

    impl override fun removeLastOccurrence(element: E) =
            deque.removeLastOccurrence(element)

    impl override fun push(element: E) = deque.push(element)
    impl override fun pop(): E = deque.pop()
    impl override fun descendingIterator(): MutableIterator<E> =
            deque.descendingIterator()

    override fun equals(other: Any?) = deque == other
    override fun hashCode() = deque.hashCode()
    override fun toString() = deque.toString()
}

// TODO: Use type alias
impl class ConcurrentHashMap<K, V> : ConcurrentMap<K, V>, java.util.concurrent.ConcurrentMap<K, V> {
    private val map = java.util.concurrent.ConcurrentHashMap<K, V>()

    impl override val size: Int get() = map.size
    impl override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = map.entries
    impl override val keys: MutableSet<K> get() = map.keys
    impl override val values: MutableCollection<V> get() = map.values

    override fun replace(key: K,
                         value: V): V? = map.replace(key, value)

    impl override fun replace(key: K,
                              oldValue: V,
                              newValue: V): Boolean =
            map.replace(key, oldValue, newValue)

    impl override fun containsValue(value: V) = map.containsValue(value)

    impl override fun remove(key: K): V? = map.remove(key)

    override fun remove(key: K,
                        value: V) = map.remove(key, value)

    impl override fun get(key: K): V? = map[key]

    override fun putIfAbsent(key: K,
                             value: V): V? = map.putIfAbsent(key, value)

    impl override fun containsKey(key: K) = map.containsKey(key)

    impl override fun isEmpty() = map.isEmpty()

    impl override fun clear() = map.clear()

    impl override fun put(key: K,
                          value: V): V? = map.put(key, value)

    impl override fun putAll(from: Map<out K, V>) = map.putAll(from)

    override fun equals(other: Any?) = map == other
    override fun hashCode() = map.hashCode()
    override fun toString() = map.toString()
}
