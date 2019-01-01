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

infix fun <T : Comparable<T>> ClosedRange<T>.shouldContain(element: T) =
    asserter.assertContains(this, element)

infix fun <T : Comparable<T>> ClosedRange<T>.shouldContainAll(elements: Iterable<T>) =
    asserter.assertContainsAll(this, elements)

infix fun <T : Comparable<T>> ClosedRange<T>.shouldNotContain(element: T) =
    asserter.assertNotContains(this, element)

infix fun <T : Comparable<T>> ClosedRange<T>.shouldNotContainAny(elements: Iterable<T>) =
    asserter.assertNotContainsAny(this, elements)

fun <T : Comparable<T>> Asserter.assertContains(
    range: ClosedRange<T>,
    element: T,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$range> did not contain <$element>."
        )
    }, range.contains(element))
}

fun <T : Comparable<T>> Asserter.assertContainsAll(
    range: ClosedRange<T>,
    elements: Iterable<T>,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$range> did not contain <$elements>."
        )
    }, elements.all { range.contains(it) })
}

fun <T : Comparable<T>> Asserter.assertNotContains(
    range: ClosedRange<T>,
    element: T,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$range> contained <$element>."
        )
    }, !range.contains(element))
}

fun <T : Comparable<T>> Asserter.assertNotContainsAny(
    range: ClosedRange<T>,
    elements: Iterable<T>,
    message: String? = null
) {
    assertTrue({
        messagePrefix(
            message, "<$range> contained <$elements>."
        )
    }, !elements.all { range.contains(it) })
}
