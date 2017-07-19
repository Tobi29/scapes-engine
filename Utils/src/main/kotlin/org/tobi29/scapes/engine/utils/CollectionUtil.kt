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
 * Constructs an infinite sequence starting with all the elements in the given
 * one and filling the rest with `null`
 * @receiver The sequence to start with
 * @returns An infinite sequence
 */
fun <T> Sequence<T>.andNull() = Sequence {
    val iterator = iterator()
    object : Iterator<T?> {
        override fun hasNext() = true

        override fun next(): T? {
            return if (iterator.hasNext()) iterator.next() else null
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
 * Returns an unmodifiable version of the given collection
 * @param T The type of elements
 * @receiver The collection
 * @returns A read only view of the collection
 */
header inline fun <T> Collection<T>.readOnly(): Collection<T>

/**
 * Returns an unmodifiable version of the given list
 * @param T The type of elements
 * @receiver The list
 * @returns A read only view of the list
 */
header inline fun <T> List<T>.readOnly(): List<T>

/**
 * Returns an unmodifiable version of the given set
 * @param T The type of elements
 * @receiver The set
 * @returns A read only view of the set
 */
header inline fun <T> Set<T>.readOnly(): Set<T>

/**
 * Returns an unmodifiable version of the given map
 * @param K The type of keys
 * @param V The type of values
 * @receiver The map
 * @returns A read only view of the map
 */
header inline fun <K, V> Map<K, V>.readOnly(): Map<K, V>

/**
 * Returns a synchronized version of the given collection
 * @param T The type of elements
 * @receiver The collection
 * @returns A synchronized view of the collection
 */
header inline fun <T> Collection<T>.synchronized(): Collection<T>

/**
 * Returns a synchronized version of the given collection
 * @param T The type of elements
 * @receiver The collection
 * @returns A synchronized view of the collection
 */
header inline fun <T> MutableCollection<T>.synchronized(): MutableCollection<T>

/**
 * Returns a synchronized version of the given list
 * @param T The type of elements
 * @receiver The list
 * @returns A synchronized view of the list
 */
header inline fun <T> List<T>.synchronized(): List<T>

/**
 * Returns a synchronized version of the given list
 * @param T The type of elements
 * @receiver The list
 * @returns A synchronized view of the list
 */
header inline fun <T> MutableList<T>.synchronized(): MutableList<T>

/**
 * Returns a synchronized version of the given set
 * @param T The type of elements
 * @receiver The set
 * @returns A synchronized view of the set
 */
header inline fun <T> Set<T>.synchronized(): Set<T>

/**
 * Returns a synchronized version of the given set
 * @param T The type of elements
 * @receiver The set
 * @returns A synchronized view of the set
 */
header inline fun <T> MutableSet<T>.synchronized(): MutableSet<T>

/**
 * Returns a synchronized version of the given map
 * @param K The type of keys
 * @param V The type of values
 * @receiver The map
 * @returns A synchronized view of the map
 */
header inline fun <K, V> Map<K, V>.synchronized(): Map<K, V>

/**
 * Returns a synchronized version of the given map
 * @param K The type of keys
 * @param V The type of values
 * @receiver The map
 * @returns A synchronized view of the map
 */
header inline fun <K, V> MutableMap<K, V>.synchronized(): MutableMap<K, V>

/**
 * Returns a map using the given enum as keys
 *
 * This can provide a performance advantage over normal maps
 * @param E The enum type
 * @param V The value type
 * @returns A map using the given enum as keys
 */
header inline fun <reified E : Enum<E>, V> EnumMap(): MutableMap<E, V>

/**
 * Adds the given [value] if [key] was not already in the map
 * @return The value that was already mapped or `null` if [value] got added
 */
header fun <K, V> MutableMap<K, V>.putAbsent(key: K,
                                             value: V): V?

/**
 * Adds the given [value] if [key] was not already in the map
 * @return The value that was already mapped or `null` if [value] got added
 */
header fun <K, V> ConcurrentMap<K, V>.putAbsent(key: K,
                                                value: V): V?

/**
 * Fetch the value for the [key] and remap it using [block]
 * @return The value returned from [block]
 */
header fun <K, V> MutableMap<K, V>.computeAlways(key: K,
                                                 block: (K, V?) -> V): V

/**
 * Fetch the value for the [key] and remap it using [block]
 * @return The value returned from [block]
 */
header fun <K, V> ConcurrentMap<K, V>.computeAlways(key: K,
                                                    block: (K, V?) -> V): V

/**
 * Fetch the value for the [key] and remap it using [block]
 *
 * Returning `null` in [block] will remove the value from the map
 * @return The value returned from [block]
 */
header fun <K, V> MutableMap<K, V>.computeAlways(key: K,
                                                 block: (K, V?) -> V?): V?

/**
 * Fetch the value for the [key] and remap it using [block]
 *
 * Returning `null` in [block] will remove the value from the map
 * @return The value returned from [block]
 */
header fun <K, V> ConcurrentMap<K, V>.computeAlways(key: K,
                                                    block: (K, V?) -> V?): V?

/**
 * Fetch the value for the [key], if it is not set, call [block] and add its
 * result
 * @return The value mapped to [key] at the end
 */
header inline fun <K, V> MutableMap<K, V>.computeAbsent(key: K,
                                                        block: (K) -> V): V

/**
 * Fetch the value for the [key], if it is not set, call [block] and add its
 * result
 * @return The value mapped to [key] at the end
 */
header inline fun <K, V> ConcurrentMap<K, V>.computeAbsent(key: K,
                                                           block: (K) -> V): V

/**
 * Fetch the value for the [key], if it is not set, call [block] and add its
 * result
 *
 * Returning `null` in [block] will remove the value from the map
 * @return The value mapped to [key] at the end
 */
header inline fun <K, V> MutableMap<K, V>.computeAbsent(key: K,
                                                        block: (K) -> V?): V?

/**
 * Fetch the value for the [key], if it is not set, call [block] and add its
 * result
 *
 * Returning `null` in [block] will remove the value from the map
 * @return The value mapped to [key] at the end
 */
header inline fun <K, V> ConcurrentMap<K, V>.computeAbsent(key: K,
                                                           block: (K) -> V?): V?

header fun <K, V> MutableMap<K, V>.removeEqual(key: K,
                                               value: V): Boolean

fun <T : Comparable<T>> comparator(): Comparator<T> =
        object : Comparator<T> {
            override fun compare(a: T,
                                 b: T) = a.compareTo(b)
        }
