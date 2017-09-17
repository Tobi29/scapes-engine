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

impl inline fun <T> Collection<T>.readOnly(): Collection<T> =
        this

impl inline fun <T> List<T>.readOnly(): List<T> =
        this

impl inline fun <T> Set<T>.readOnly(): Set<T> =
        this

impl inline fun <K, V> Map<K, V>.readOnly(): Map<K, V> =
        this

impl inline fun <T> Collection<T>.synchronized(): Collection<T> =
        this

impl inline fun <T> MutableCollection<T>.synchronized(): MutableCollection<T> =
        this

impl inline fun <T> List<T>.synchronized(): List<T> =
        this

impl inline fun <T> MutableList<T>.synchronized(): MutableList<T> =
        this

impl inline fun <T> Set<T>.synchronized(): Set<T> =
        this

impl inline fun <T> MutableSet<T>.synchronized(): MutableSet<T> =
        this

impl inline fun <K, V> Map<K, V>.synchronized(): Map<K, V> =
        this

impl inline fun <K, V> MutableMap<K, V>.synchronized(): MutableMap<K, V> =
        this

impl inline fun <reified E : Enum<E>, V> EnumMap(): MutableMap<E, V> = HashMap()

impl fun <K, V> MutableMap<K, V>.putAbsent(key: K,
                                           value: V): V? {
    if (this is ConcurrentMap) {
        return this.putAbsent(key, value)
    } else {
        this[key]?.let { return it }
        put(key, value)
        return null
    }
}

impl inline fun <K, V> ConcurrentMap<K, V>.putAbsent(key: K,
                                                     value: V): V? {
    this[key]?.let { return it }
    put(key, value)
    return null
}

impl fun <K, V> MutableMap<K, V>.computeAlways(key: K,
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

impl fun <K, V> ConcurrentMap<K, V>.computeAlways(key: K,
                                                  block: (K, V?) -> V): V {
    val old = this[key]
    val new = block(key, old)
    this[key] = new
    return new
}

@JsName("computeAlwaysNullable")
impl fun <K, V> MutableMap<K, V>.computeAlways(key: K,
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

@JsName("computeAlwaysNullableConcurrent")
impl fun <K, V> ConcurrentMap<K, V>.computeAlways(key: K,
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

impl inline fun <K, V> MutableMap<K, V>.computeAbsent(key: K,
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

impl inline fun <K, V> ConcurrentMap<K, V>.computeAbsent(key: K,
                                                         block: (K) -> V): V {
    this[key]?.let { return it }
    val new = block(key)
    return putAbsent(key, new) ?: new
}

@JsName("computeAbsentNullable")
impl inline fun <K, V> MutableMap<K, V>.computeAbsent(key: K,
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

@JsName("computeAbsentNullableConcurrent")
impl inline fun <K, V> ConcurrentMap<K, V>.computeAbsent(key: K,
                                                         block: (K) -> V?): V? {
    this[key]?.let { return it }
    val new = block(key) ?: return null
    return putAbsent(key, new) ?: new
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
impl inline fun <K, V> MutableMap<K, V>.removeEqual(key: K,
                                                    value: V): Boolean =
        if (this[key] == value) {
            remove(key)
            true
        } else false
