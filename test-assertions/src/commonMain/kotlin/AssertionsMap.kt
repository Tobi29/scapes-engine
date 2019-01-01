/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.assertions

import kotlin.test.Asserter
import kotlin.test.asserter

infix fun <K, V> Map<K, V>.shouldContainKey(key: K) =
    asserter.assertContainsKey(this, key)

infix fun <K, V> Map<K, V>.shouldContainKeyAll(keys: Iterable<K>) =
    asserter.assertContainsKeyAll(this, keys)

infix fun <K, V> Map<K, V>.shouldNotContainKey(key: K) =
    asserter.assertNotContainsKey(this, key)

infix fun <K, V> Map<K, V>.shouldNotContainKeyAny(keys: Iterable<K>) =
    asserter.assertNotContainsKeyAny(this, keys)

infix fun <K, V> Map<K, V>.shouldContainValue(value: V) =
    asserter.assertContainsValue(this, value)

infix fun <K, V> Map<K, V>.shouldContainValueAll(values: Iterable<V>) =
    asserter.assertContainsValueAll(this, values)

infix fun <K, V> Map<K, V>.shouldNotContainValue(value: V) =
    asserter.assertNotContainsValue(this, value)

infix fun <K, V> Map<K, V>.shouldNotContainValueAny(values: Iterable<V>) =
    asserter.assertNotContainsValueAny(this, values)

fun <K, V> Asserter.assertContainsKey(
    map: Map<K, V>,
    key: K,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$map> did not contain key <$key>."
        )
    }, map.containsKey(key))
}

fun <K, V> Asserter.assertContainsKeyAll(
    map: Map<K, V>,
    keys: Iterable<K>,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$map> did not contain keys <$keys>."
        )
    }, keys.all { map.containsKey(it) })
}

fun <K, V> Asserter.assertNotContainsKey(
    map: Map<K, V>,
    key: K,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$map> contained key <$key>."
        )
    }, !map.containsKey(key))
}

fun <K, V> Asserter.assertNotContainsKeyAny(
    map: Map<K, V>,
    keys: Iterable<K>,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$map> contained keys <$keys>."
        )
    }, keys.all { !map.containsKey(it) })
}

fun <K, V> Asserter.assertContainsValue(
    map: Map<K, V>,
    value: V,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$map> did not contain value <$value>."
        )
    }, map.containsValue(value))
}

fun <K, V> Asserter.assertContainsValueAll(
    map: Map<K, V>,
    values: Iterable<V>,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$map> did not contain values <$values>."
        )
    }, values.all { map.containsValue(it) })
}

fun <K, V> Asserter.assertNotContainsValue(
    map: Map<K, V>,
    value: V,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$map> contained value <$value>."
        )
    }, !map.containsValue(value))
}

fun <K, V> Asserter.assertNotContainsValueAny(
    map: Map<K, V>,
    values: Iterable<V>,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$map> contained values <$values>."
        )
    }, values.all { !map.containsValue(it) })
}
