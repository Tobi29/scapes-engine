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
 * Find the first element of correct type and casts it
 * @param T The type to cast to
 * @receiver The elements to search in
 * @return The first casted element or `null`
 */
inline fun <reified T : Any> Iterable<*>.findMap(): T? {
    return find { it is T } as? T
}

/**
 * Construct an iterator returning all combinations of the elements from
 * the given [Iterable] and [other].
 *
 * For each element in [other] every element in the [Iterable] will be returned
 * until the next element from [other] is taken.
 */
operator fun <T, U> Iterable<T>.times(
        other: Iterator<U>
): Iterator<Pair<T, U>> = object : Iterator<Pair<T, U>> {
    private var left: Iterator<T>? = null
    private var right: U? = null

    private fun ensureIterator(): Iterator<T>? {
        var iterator = left
        while (iterator == null || !iterator.hasNext()) {
            if (!other.hasNext()) return null
            iterator = this@times.iterator()
            left = iterator
            right = other.next()
        }
        return iterator
    }

    override fun hasNext() = ensureIterator()?.hasNext() == true

    override fun next(): Pair<T, U> {
        val iterator = ensureIterator()
                ?: throw NoSuchElementException("Iterator has no more elements")
        return iterator.next() to right!!
    }
}

/**
 * Construct an iterator returning all combinations of the elements from
 * the given [Iterable] and [other].
 *
 * For each element in [other] every element in the [Iterable] will be returned
 * until the next element from [other] is taken.
 */
operator fun <T, U> Iterable<T>.times(
        other: Sequence<U>
): Sequence<Pair<T, U>> = Sequence {
    asIterable().times(other.iterator())
}

/**
 * Construct an iterator returning all combinations of the elements from
 * the given [Iterable] and [other].
 *
 * For each element in [other] every element in the [Iterable] will be returned
 * until the next element from [other] is taken.
 */
operator fun <T, U> Iterable<T>.times(
        other: Iterable<U>
): Iterable<Pair<T, U>> = object : Iterable<Pair<T, U>> {
    override fun iterator() = this@times.times(other.iterator())
}

/**
 * Returns a copy of the given map by replacing the given key with the
 * result of [transform]
 *
 * **Note:** [transform] will be called with `null` if the key did not exist
 * and will still be added
 *
 * @param key The key to replace
 * @param transform Called with the previous key or `null` if not mapped
 * @return A copy of the original map, with the replaced value
 * @see replace
 */
inline fun <K, V> Map<K, V>.substitute(
        key: K,
        transform: (V?) -> V
): Map<K, V> {
    val map = HashMap<K, V>()
    map.putAll(this)
    map[key] = transform(map[key])
    return map
}

/**
 * Returns a copy of the given map by replacing the given key with the
 * result of [transform]
 *
 * **Note:** If the key did not have a mapping previously a copy of the original
 * map is returned
 *
 * @param key The key to replace
 * @param transform Called with the previous key
 * @return A copy of the original map, possibly with the replaced value
 * @see substitute
 */
inline fun <K, V : Any> Map<K, V>.replace(
        key: K,
        transform: (V) -> V
): Map<K, V> {
    val map = HashMap<K, V>()
    map.putAll(this)
    map[key]?.let { map[key] = transform(it) }
    return map
}

/**
 * Generates a lazy iterator containing all permutations of the given elements
 * in lists of the same size as [size]
 *
 * @param size The size of each list
 * @receiver The elements to use
 */
inline fun <reified T> Iterable<T>.permutations(size: Int): Iterator<List<T>> =
        toList().toTypedArray().permutations(size)

/**
 * Generates a lazy iterator containing all permutations of the given elements
 * in lists of the same size as the given array
 *
 * @receiver The elements for each index in the resulting lists to use
 */
inline fun <reified T> Iterable<Iterable<T>>.permutations(): Iterator<List<T>> =
        map { it.toList().toTypedArray() }.toTypedArray().permutations()
