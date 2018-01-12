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

actual inline fun <T> Collection<T>.readOnly(): Collection<T> =
        this

actual inline fun <T> List<T>.readOnly(): List<T> =
        this

actual inline fun <T> Set<T>.readOnly(): Set<T> =
        this

actual inline fun <K, V> Map<K, V>.readOnly(): Map<K, V> =
        this

actual inline fun <T> Collection<T>.synchronized(): Collection<T> =
        this

actual inline fun <T> MutableCollection<T>.synchronized(): MutableCollection<T> =
        this

actual inline fun <T> List<T>.synchronized(): List<T> =
        this

actual inline fun <T> MutableList<T>.synchronized(): MutableList<T> =
        this

actual inline fun <T> Set<T>.synchronized(): Set<T> =
        this

actual inline fun <T> MutableSet<T>.synchronized(): MutableSet<T> =
        this

actual inline fun <K, V> Map<K, V>.synchronized(): Map<K, V> =
        this

actual inline fun <K, V> MutableMap<K, V>.synchronized(): MutableMap<K, V> =
        this

actual inline fun <reified E : Enum<E>, V> EnumMap(): MutableMap<E, V> = HashMap()

actual fun <K, V> MutableMap<K, V>.putAbsent(key: K,
                                             value: V): V? {
    this[key]?.let { return it }
    put(key, value)
    return null
}

actual inline fun <K, V> ConcurrentMap<K, V>.putAbsent(key: K,
                                                       value: V): V? {
    this[key]?.let { return it }
    put(key, value)
    return null
}

actual fun <K, V> MutableMap<K, V>.computeAlways(key: K,
                                                 block: (K, V?) -> V): V {
    val old = this[key]
    val new = block(key, old)
    this[key] = new
    return new
}

actual fun <K, V> ConcurrentMap<K, V>.computeAlways(key: K,
                                                    block: (K, V?) -> V): V {
    val old = this[key]
    val new = block(key, old)
    this[key] = new
    return new
}

@JsName("computeAlwaysNullable")
actual fun <K, V> MutableMap<K, V>.computeAlways(key: K,
                                                 block: (K, V?) -> V?): V? {
    val old = this[key]
    val new = block(key, old)
    if (new != null) {
        this[key] = new
    } else if (old != null || containsKey(key)) {
        remove(key)
    }
    return new
}

@JsName("computeAlwaysNullableConcurrent")
actual fun <K, V> ConcurrentMap<K, V>.computeAlways(key: K,
                                                    block: (K, V?) -> V?): V? {
    val old = this[key]
    val new = block(key, old)
    if (new != null) {
        this[key] = new
    } else if (old != null || containsKey(key)) {
        remove(key)
    }
    return new
}

actual inline fun <K, V> MutableMap<K, V>.computeAbsent(key: K,
                                                        block: (K) -> V): V {
    this[key]?.let { return it }
    val new = block(key)
    return putAbsent(key, new) ?: new
}

actual inline fun <K, V> ConcurrentMap<K, V>.computeAbsent(key: K,
                                                           block: (K) -> V): V {
    this[key]?.let { return it }
    val new = block(key)
    return putAbsent(key, new) ?: new
}

@JsName("computeAbsentNullable")
actual inline fun <K, V> MutableMap<K, V>.computeAbsent(key: K,
                                                        block: (K) -> V?): V? {
    this[key]?.let { return it }
    val new = block(key) ?: return null
    return putAbsent(key, new) ?: new
}

@JsName("computeAbsentNullableConcurrent")
actual inline fun <K, V> ConcurrentMap<K, V>.computeAbsent(key: K,
                                                           block: (K) -> V?): V? {
    this[key]?.let { return it }
    val new = block(key) ?: return null
    return putAbsent(key, new) ?: new
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual inline fun <K, V> MutableMap<K, V>.removeEqual(key: K,
                                                      value: V): Boolean =
        if (this[key] == value) {
            remove(key)
            true
        } else false
