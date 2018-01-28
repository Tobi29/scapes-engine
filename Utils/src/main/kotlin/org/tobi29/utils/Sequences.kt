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

package org.tobi29.utils

/**
 * Takes all elements of a sequence and puts them into an array
 * @param T The type of elements
 * @receiver The sequence of elements to collect
 * @return A new array containing
 */
inline fun <reified T> Sequence<T>.toArray(): Array<T> =
    toList().toTypedArray()

/**
 * Constructs an infinite sequence starting with all the elements in the given
 * one and filling the rest with `null`
 * @receiver The sequence to start with
 * @return An infinite sequence
 */
fun <T> Sequence<T>.andNull() = Sequence { iterator().andNull() }

/**
 * Accumulates value starting with the first element and applying [operation]
 * from left to right to current accumulator value and each element.
 * @param S Element type
 * @param T Element type
 * @param operation Function that takes the current accumulator value and the element itself and calculates the next accumulator value
 * @receiver The sequence of elements to reduce
 * @return The resulting element or null if called on empty sequence
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
 * @return The resulting element or null if called on empty sequence
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

/**
 * Find the first element of correct type and casts it
 * @param T The type to cast to
 * @receiver The elements to search in
 * @return The first casted element or `null`
 */
inline fun <reified T : Any> Sequence<*>.findMap(): T? {
    return find { it is T } as? T
}

/**
 * Construct a sequence returning all combinations of the elements from
 * the given [Sequence] and [other].
 *
 * For each element in [other] every element in the [Sequence] will be returned
 * until the next element from [other] is taken.
 */
operator fun <T, U> Sequence<T>.times(
    other: Sequence<U>
): Sequence<Pair<T, U>> = Sequence {
    asIterable().times(other.iterator())
}
