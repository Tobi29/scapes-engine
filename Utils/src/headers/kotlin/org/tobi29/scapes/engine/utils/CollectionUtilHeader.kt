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
expect fun <K, V> MutableMap<K, V>.computeAlways(key: K,
                                                 block: (K, V?) -> V): V

/**
 * Fetch the value for the [key] and remap it using [block]
 * @return The value returned from [block]
 */
expect fun <K, V> ConcurrentMap<K, V>.computeAlways(key: K,
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
expect inline fun <K, V> MutableMap<K, V>.computeAbsent(key: K,
                                                        block: (K) -> V): V

/**
 * Fetch the value for the [key], if it is not set, call [block] and add its
 * result
 * @return The value mapped to [key] at the end
 */
expect inline fun <K, V> ConcurrentMap<K, V>.computeAbsent(key: K,
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
