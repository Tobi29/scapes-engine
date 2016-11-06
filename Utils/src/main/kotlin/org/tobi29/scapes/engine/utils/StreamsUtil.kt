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

import java8.util.Spliterator
import java8.util.stream.*
import java.util.*

/**
 * Converts given Collection into a [Stream]
 * @param T Element type
 * @return [Stream] providing the elements
 */
inline fun <T> Collection<T>.stream(): Stream<T> {
    return StreamSupport.stream(this)
}

/**
 * Converts given [Pool] into a [Stream]
 * @param T Element type
 * @return [Stream] providing the elements
 */
inline fun <T> Pool<T>.stream(): Stream<T> {
    return spliterator8().stream()
}

/**
 * Returns an empty [Stream]
 * @param T Element type
 * @return An empty [Stream]
 */
inline fun <T> stream(): Stream<T> {
    return RefStreams.empty<T>()
}

/**
 * Returns a [Stream] providing just the given [item] if present
 * @param item The only element to be in the [Stream] if present
 * @param T Element type
 * @return A [Stream] with up to one element
 */
inline fun <T> stream(item: T?): Stream<T> {
    if (item == null) {
        return stream()
    }
    return RefStreams.of(item)
}

/**
 * Converts given [array] into a [Stream]
 * @param array The array
 * @param T Element type
 * @return [Stream] providing the elements
 */
inline fun <T> stream(vararg array: T): Stream<T> {
    return RefStreams.of(*array)
}

/**
 * Converts given [array] into an [IntStream]
 * @param array The array
 * @return [IntStream] providing the elements
 */
inline fun stream(array: IntArray): IntStream {
    return IntStreams.of(*array)
}

/**
 * Converts given [array] into a [LongStream]
 * @param array The array
 * @return [LongStream] providing the elements
 */
inline fun stream(array: LongArray): LongStream {
    return LongStreams.of(*array)
}

/**
 * Converts given [array] into a [DoubleStream]
 * @param array The array
 * @return [DoubleStream] providing the elements
 */
inline fun stream(array: DoubleArray): DoubleStream {
    return DoubleStreams.of(*array)
}

/**
 * Returns an [IntStream] containing all values between [min] and [max]
 * @param min Start of the stream, inclusive
 * @param max End of the stream, exclusive
 */
inline fun streamRange(min: Int,
                       max: Int): IntStream {
    return IntStreams.range(min, max)
}

/**
 * Returns a [LongStream] containing all values between [min] and [max]
 * @param min Start of the stream, inclusive
 * @param max End of the stream, exclusive
 */
inline fun streamRange(min: Long,
                       max: Long): LongStream {
    return LongStreams.range(min, max)
}

/**
 * Converts given [Spliterator] into a [Stream]
 * @param T Element type
 * @return [Stream] providing the elements
 */
inline fun <T> Spliterator<T>.stream(): Stream<T> {
    return StreamSupport.stream(this, false)
}

/**
 * Iterates through the given [Iterable], filters out null elements  and passes
 * the remaining elements to the [consumer]
 * @param consumer Consumer that the elements are passed to
 * @param T Element type
 */
inline fun <T> Iterable<T?>.forEachNonNull(consumer: (T) -> Unit) {
    for (entry in this) {
        entry?.let { consumer(entry) }
    }
}

/**
 * Iterates through the given array, filters out null elements and passes the
 * remaining elements to the [consumer]
 * @param consumer Consumer that the elements are passed to
 * @param T Element type
 */
inline fun <T> Array<T?>.forEachNonNull(consumer: (T) -> Unit) {
    for (entry in this) {
        entry?.let { consumer(entry) }
    }
}

/**
 * Iterates through the given [Iterable], filters out elements using
 * the [filter] and passes the remaining elements to the [consumer]
 * @param filter The predicate to filter elements with
 * @param consumer Consumer that the elements are passed to
 * @param T Element type
 */
inline fun <T> Iterable<T>.forEach(filter: (T) -> Boolean,
                                   consumer: (T) -> Unit) {
    for (entry in this) {
        if (filter(entry)) {
            consumer(entry)
        }
    }
}

/**
 * Iterates through the given array, filters out elements using the [filter] and
 * passes the remaining elements to the [consumer]
 * @param filter The predicate to filter elements with
 * @param consumer Consumer that the elements are passed to
 * @param T Element type
 */
inline fun <T> Array<T>.forEach(filter: (T) -> Boolean,
                                consumer: (T) -> Unit) {
    for (entry in this) {
        if (filter(entry)) {
            consumer(entry)
        }
    }
}

/**
 * Iterates through the given [Iterable] and inserts them into an [ArrayList]
 * @param T Element type
 * @return An [ArrayList] containing the elements of the collection
 */
inline fun <T> Iterable<T>.collect(): ArrayList<T> {
    val list = ArrayList<T>()
    for (entry in this) {
        list.add(entry)
    }
    return list
}

/**
 * Inserts the item into an [ArrayList]
 * @param item The only element to be in the [ArrayList]
 * @param T Element type
 * @return An [ArrayList] containing the element
 */
inline fun <T> collect(item: T): ArrayList<T> {
    val list = ArrayList<T>(1)
    list.add(item)
    return list
}

/**
 * Inserts the array into an [ArrayList]
 * @param array The array to construct a for-each loop from
 * @param T Element type
 * @return An [ArrayList] containing the elements of the array
 */
inline fun <T> collect(vararg array: T): ArrayList<T> {
    val list = ArrayList<T>(array.size)
    Collections.addAll(list, *array)
    return list
}

/**
 * Iterates through the given [Iterable], filters out elements using the
 * [filter] and inserts them into an [ArrayList]
 * @param filter The predicate to filter elements with
 * @param T Element type
 * @return An [ArrayList] containing the filtered elements of the collection
 */
inline fun <T> Iterable<T>.collect(filter: (T) -> Boolean): ArrayList<T> {
    val list = ArrayList<T>()
    for (entry in this) {
        if (filter(entry)) {
            list.add(entry)
        }
    }
    return list
}

/**
 * Inserts the [item] into an [ArrayList] if the filter succeeds
 * @param item The only element to be in the [ArrayList]
 * @param filter The predicate to filter the element element with
 * @param T Element type
 * @return An [ArrayList] maybe containing the element
 */
inline fun <T> collect(item: T,
                       filter: (T) -> Boolean): ArrayList<T> {
    if (!filter(item)) {
        return ArrayList(0)
    }
    val list = ArrayList<T>(1)
    list.add(item)
    return list
}

/**
 * Iterates through the [array], filters out elements using the [filter]
 * and inserts them into an [ArrayList]
 * @param array The array
 * @param filter The predicate to filter elements with
 * @param T Element type
 * @return An [ArrayList] containing the filtered elements of the array
 */
inline fun <T> collect(vararg array: T,
                       filter: (T) -> Boolean): ArrayList<T> {
    val list = ArrayList<T>(array.size)
    for (entry in array) {
        if (filter(entry)) {
            list.add(entry)
        }
    }
    return list
}

/**
 * Iterates through the given [Array], filters out elements using the [filter],
 * maps them using the [map] and inserts them into an [ArrayList]
 * @receiver The array
 * @param filter Filters the elements
 * @param map Maps the filtered elements before adding them to the list
 * @param T Element type
 * @param R Return element type
 * @return An [ArrayList] containing the filtered elements of the array
 */
inline fun <T, R> Array<T>.collect(filter: (T) -> Boolean,
                                   map: (T) -> R): ArrayList<R> {
    val list = ArrayList<R>(size)
    for (entry in this) {
        if (filter(entry)) {
            list.add(map(entry))
        }
    }
    return list
}

/**
 * Filters the stream of nulls and maps them to non-nullable types
 * @receiver The stream to filter
 * @return The filtered stream
 */
@Suppress("UNCHECKED_CAST")
inline fun <T> Stream<T?>.notNull(): Stream<T> {
    return filter { it != null } as Stream<T>
}

/**
 * Calls [Stream.toArray] without requiring extra code to allocate the array
 * @receiver The stream to collect
 * @return An array containing the elements of the stream
 */
inline fun <reified T> Stream<out T>.toTypedArray(): Array<T> {
    return toArray { arrayOfNulls<T>(it) }
}

/**
 * Calls [Stream.filter] and [Stream.map] to cast the elements of a stream to
 * [T]
 * @receiver The stream to cast
 * @return The casted stream
 */
inline fun <reified T> Stream<*>.filterMap(): Stream<T> {
    return filter { it is T }.map { it as T }
}
