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

impl class ConcurrentHashMap<K, V> : HashMap<K, V>(), ConcurrentMap<K, V> {
    impl open fun replace(key: K,
                          oldValue: V,
                          newValue: V) =
            if (this[key] == oldValue) {
                this[key] = newValue
                true
            } else {
                false
            }
}

impl fun String.toUUID(): UUID? {
    val split = split('-')
    if (split.size != 5) return null
    val value = "${
    split[0].prefixToLength('0', 8)}${
    split[1].prefixToLength('0', 4)}${
    split[2].prefixToLength('0', 4)}${
    split[3].prefixToLength('0', 4)}${
    split[4].prefixToLength('0', 12)}".toUInt128OrNull(16) ?: return null
    return java.util.UUID(value)
}
