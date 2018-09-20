/*
 * Copyright 2012-2018 Tobi29
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

infix fun <E> Collection<E>.shouldContain(element: E) =
    asserter.assertContains(this, element)

infix fun <E> Collection<E>.shouldContainAll(elements: Iterable<E>) =
    asserter.assertContainsAll(this, elements)

infix fun <E> Collection<E>.shouldContainAll(elements: Collection<E>) =
    asserter.assertContainsAll(this, elements)

infix fun <E> Collection<E>.shouldNotContain(element: E) =
    asserter.assertNotContains(this, element)

infix fun <E> Collection<E>.shouldNotContainAll(elements: Iterable<E>) =
    asserter.assertNotContainsAll(this, elements)

fun <E> Asserter.assertContains(
    collection: Collection<E>,
    element: E,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$collection> did not contain <$element>."
        )
    }, collection.contains(element))
}

fun <E> Asserter.assertContainsAll(
    collection: Collection<E>,
    elements: Iterable<E>,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$collection> did not contain <$elements>."
        )
    }, elements.all { collection.contains(it) })
}

fun <E> Asserter.assertContainsAll(
    collection: Collection<E>,
    elements: Collection<E>,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$collection> did not contain <$elements>."
        )
    }, collection.containsAll(elements))
}

fun <E> Asserter.assertNotContains(
    collection: Collection<E>,
    element: E,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$collection> contained <$element>."
        )
    }, !collection.contains(element))
}

fun <E> Asserter.assertNotContainsAll(
    collection: Collection<E>,
    elements: Iterable<E>,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$collection> contained <$elements>."
        )
    }, elements.all { !collection.contains(it) })
}
