package org.tobi29.scapes.engine.utils

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
