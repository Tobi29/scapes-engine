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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils

import java.util.*
import java.util.concurrent.ConcurrentMap

/**
 * Returns an unmodifiable version of the given collection
 * @param T The type of elements
 * @receiver The collection
 * @returns A read only view of the collection
 */
inline fun <T> Collection<T>.readOnly(): Collection<T> = Collections.unmodifiableCollection(
        this)

/**
 * Returns an unmodifiable version of the given list
 * @param T The type of elements
 * @receiver The list
 * @returns A read only view of the list
 */
inline fun <T> List<T>.readOnly(): List<T> = Collections.unmodifiableList(this)

/**
 * Returns an unmodifiable version of the given set
 * @param T The type of elements
 * @receiver The set
 * @returns A read only view of the set
 */
inline fun <T> Set<T>.readOnly(): Set<T> = Collections.unmodifiableSet(this)

/**
 * Returns an unmodifiable version of the given map
 * @param K The type of keys
 * @param V The type of values
 * @receiver The map
 * @returns A read only view of the map
 */
inline fun <K, V> Map<K, V>.readOnly(): Map<K, V> = Collections.unmodifiableMap(
        this)

/**
 * Returns a synchronized version of the given collection
 * @param T The type of elements
 * @receiver The collection
 * @returns A synchronized view of the collection
 */
inline fun <T> Collection<T>.synchronized(): Collection<T> = Collections.synchronizedCollection(
        this)

/**
 * Returns a synchronized version of the given collection
 * @param T The type of elements
 * @receiver The collection
 * @returns A synchronized view of the collection
 */
@JvmName("synchronizedMut")
inline fun <T> MutableCollection<T>.synchronized(): MutableCollection<T> = Collections.synchronizedCollection(
        this)

/**
 * Returns a synchronized version of the given list
 * @param T The type of elements
 * @receiver The list
 * @returns A synchronized view of the list
 */
inline fun <T> List<T>.synchronized(): List<T> = Collections.synchronizedList(
        this)

/**
 * Returns a synchronized version of the given list
 * @param T The type of elements
 * @receiver The list
 * @returns A synchronized view of the list
 */
@JvmName("synchronizedMut")
inline fun <T> MutableList<T>.synchronized(): MutableList<T> = Collections.synchronizedList(
        this)

/**
 * Returns a synchronized version of the given set
 * @param T The type of elements
 * @receiver The set
 * @returns A synchronized view of the set
 */
inline fun <T> Set<T>.synchronized(): Set<T> = Collections.synchronizedSet(
        this)

/**
 * Returns a synchronized version of the given set
 * @param T The type of elements
 * @receiver The set
 * @returns A synchronized view of the set
 */
@JvmName("synchronizedMut")
inline fun <T> MutableSet<T>.synchronized(): MutableSet<T> = Collections.synchronizedSet(
        this)

/**
 * Returns a synchronized version of the given map
 * @param K The type of keys
 * @param V The type of values
 * @receiver The map
 * @returns A synchronized view of the map
 */
inline fun <K, V> Map<K, V>.synchronized(): Map<K, V> = Collections.synchronizedMap(
        this)

/**
 * Returns a synchronized version of the given map
 * @param K The type of keys
 * @param V The type of values
 * @receiver The map
 * @returns A synchronized view of the map
 */
@JvmName("synchronizedMut")
inline fun <K, V> MutableMap<K, V>.synchronized(): MutableMap<K, V> = Collections.synchronizedMap(
        this)

/**
 * Filters out elements of the wrong type and casts them
 * @param T The type to cast to
 * @receiver The sequence of elements to map
 * @returns A lazy sequence
 */
inline fun <reified T : Any> Sequence<*>.filterMap(): Sequence<T> {
    return mapNotNull { it as? T }
}

/**
 * Takes all elements of a sequence and puts them into an array
 * @param T The type of elements
 * @receiver The sequence of elements to collect
 * @returns A new array containing
 */
inline fun <reified T> Sequence<T>.toArray(): Array<T> {
    return toList().toTypedArray()
}

/**
 * Limits the amount of elements available in the sequence
 * @param T The type of elements
 * @receiver The sequence of elements to limit
 * @returns A lazy sequence
 */
inline fun <T> Sequence<T>.limit(amount: Int): Sequence<T> {
    return Sequence {
        val iterator = iterator()
        object : Iterator<T> {
            private var count = 0

            override fun hasNext() = iterator.hasNext() && count < amount

            override fun next(): T {
                if (!hasNext()) {
                    throw NoSuchElementException()
                }
                count++
                return iterator.next()
            }
        }
    }
}

/**
 * Accumulates value starting with the first element and applying [operation]
 * from left to right to current accumulator value and each element.
 * @param S Element type
 * @param T Element type
 * @param operation Function that takes the current accumulator value and the element itself and calculates the next accumulator value
 * @receiver The sequence of elements to reduce
 * @returns The resulting element or null if called on empty sequence
 */
inline fun <S, T : S> Sequence<T>.reduceOrNull(operation: (S, T) -> S): S? {
    val iterator = this.iterator()
    if (!iterator.hasNext()) {
        return null
    }
    var accumulator: S = iterator.next()
    while (iterator.hasNext()) {
        accumulator = operation(accumulator, iterator.next())
    }
    return accumulator
}

/**
 * Accumulates value starting with the first element and applying [operation]
 * from left to right to current accumulator value and each element with its
 * index in the original sequence.
 * @param S Element type
 * @param T Element type
 * @param operation Function that takes the index of an element, current accumulator value and the element itself and calculates the next accumulator value
 * @receiver The sequence of elements to reduce
 * @returns The resulting element or null if called on empty sequence
 */
inline fun <S, T : S> Sequence<T>.reduceIndexedOrNull(operation: (Int, S, T) -> S): S? {
    val iterator = this.iterator()
    if (!iterator.hasNext()) {
        return null
    }
    var index = 1
    var accumulator: S = iterator.next()
    while (iterator.hasNext()) {
        accumulator = operation(index++, accumulator, iterator.next())
    }
    return accumulator
}

inline fun <T> Iterator(crossinline block: () -> T?): Iterator<T> {
    return object : Iterator<T> {
        private var next: T? = null
        private var init = false

        // We have this in a function to only inline once, but still have a
        // non-virtual call
        private fun iterate() {
            next = block()
        }

        // Used to avoid having to call invoke() before actually evaluating
        // Makes this properly lazy
        private fun touch() {
            if (!init) {
                init = true
                iterate()
            }
        }

        override fun hasNext(): Boolean {
            touch()
            return next != null
        }

        override fun next(): T {
            touch()
            val element = next ?: throw NoSuchElementException(
                    "No more elements in iterator")
            iterate()
            return element
        }
    }
}

/**
 * Adds the given [value] if [key] was not already in the map
 * @return The value that was already mapped or `null` if [value] got added
 */
fun <K, V> MutableMap<K, V>.putAbsent(key: K,
                                      value: V): V? {
    if (this is ConcurrentMap) {
        return this.putAbsent(key, value)
    } else {
        this[key]?.let { return it }
        put(key, value)
        return null
    }
}

/**
 * Adds the given [value] if [key] was not already in the map
 * @return The value that was already mapped or `null` if [value] got added
 */
inline fun <K, V> ConcurrentMap<K, V>.putAbsent(key: K,
                                                value: V) = putIfAbsent(key,
        value)

/**
 * Fetch the value for the [key] and remap it using [block]
 * @return The value returned from [block]
 */
fun <K, V> MutableMap<K, V>.computeAlways(key: K,
                                          block: (K, V?) -> V): V {
    if (this is ConcurrentMap) {
        return this.computeAlways(key, block)
    } else {
        val old = this[key]
        val new = block(key, old)
        this[key] = new
        return new
    }
}

/**
 * Fetch the value for the [key] and remap it using [block]
 * @return The value returned from [block]
 */
fun <K, V> ConcurrentMap<K, V>.computeAlways(key: K,
                                             block: (K, V?) -> V): V {
    while (true) {
        var old = this[key]
        while (true) {
            val new = block(key, old)
            if (old == null) {
                old = putAbsent(key, new)
                if (old == null) {
                    return new
                } else {
                    continue
                }
            }
            if (replace(key, old, new)) {
                return new
            } else {
                break
            }
        }
    }
}

/**
 * Fetch the value for the [key] and remap it using [block]
 *
 * Returning `null` in [block] will remove the value from the map
 * @return The value returned from [block]
 */
@JvmName("computeAlwaysNullable")
fun <K, V> MutableMap<K, V>.computeAlways(key: K,
                                          block: (K, V?) -> V?): V? {
    if (this is ConcurrentMap) {
        return this.computeAlways(key, block)
    } else {
        val old = this[key]
        val new = block(key, old)
        if (new != null) {
            this[key] = new
        } else if (old != null || containsKey(key)) {
            remove(key)
        }
        return new
    }
}

/**
 * Fetch the value for the [key] and remap it using [block]
 *
 * Returning `null` in [block] will remove the value from the map
 * @return The value returned from [block]
 */
@JvmName("computeAlwaysNullable")
fun <K, V> ConcurrentMap<K, V>.computeAlways(key: K,
                                             block: (K, V?) -> V?): V? {
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
                if (old == null) {
                    return new
                } else {
                    continue
                }
            }
            if (replace(key, old, new)) {
                return new
            } else {
                break
            }
        }
    }
}

/**
 * Fetch the value for the [key], if it is not set, call [block] and add its
 * result
 * @return The value mapped to [key] at the end
 */
inline fun <K, V> MutableMap<K, V>.computeAbsent(key: K,
                                                 block: (K) -> V): V {
    if (this is ConcurrentMap) {
        // Should we try to eliminate the second inline of block?
        return this.computeAbsent(key, block)
    } else {
        this[key]?.let { return it }
        val new = block(key)
        return putAbsent(key, new) ?: new
    }
}

/**
 * Fetch the value for the [key], if it is not set, call [block] and add its
 * result
 * @return The value mapped to [key] at the end
 */
inline fun <K, V> ConcurrentMap<K, V>.computeAbsent(key: K,
                                                    block: (K) -> V): V {
    this[key]?.let { return it }
    val new = block(key)
    return putAbsent(key, new) ?: new
}

/**
 * Fetch the value for the [key], if it is not set, call [block] and add its
 * result
 *
 * Returning `null` in [block] will remove the value from the map
 * @return The value mapped to [key] at the end
 */
@JvmName("computeAbsentNullable")
inline fun <K, V> MutableMap<K, V>.computeAbsent(key: K,
                                                 block: (K) -> V?): V? {
    if (this is ConcurrentMap) {
        // Should we try to eliminate the second inline of block?
        return this.computeAbsent(key, block)
    } else {
        this[key]?.let { return it }
        val new = block(key) ?: return null
        return putAbsent(key, new) ?: new
    }
}

/**
 * Fetch the value for the [key], if it is not set, call [block] and add its
 * result
 *
 * Returning `null` in [block] will remove the value from the map
 * @return The value mapped to [key] at the end
 */
@JvmName("computeAbsentNullable")
inline fun <K, V> ConcurrentMap<K, V>.computeAbsent(key: K,
                                                    block: (K) -> V?): V? {
    this[key]?.let { return it }
    val new = block(key) ?: return null
    return putAbsent(key, new) ?: new
}
