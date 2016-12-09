/*
 * Copyright 2012-2016 Tobi29
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

inline fun <reified T : Any> Sequence<*>.filterMap(): Sequence<T> {
    return mapNotNull { it as? T }
}

inline fun <reified T> Sequence<T>.toArray(): Array<T> {
    return toList().toTypedArray()
}

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
