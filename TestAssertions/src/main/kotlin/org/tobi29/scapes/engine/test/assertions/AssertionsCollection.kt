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

package org.tobi29.scapes.engine.test.assertions

import kotlin.test.asserter

infix fun <E> Collection<E>.shouldContain(element: E) = assertContains(this,
        element)

infix fun <E> Collection<E>.shouldContain(elements: Collection<E>) = assertContains(
        this, elements)

infix fun <E> Collection<E>.shouldNotContain(element: E) = assertNotContains(
        this,
        element)

infix fun <E> Collection<E>.shouldNotContain(elements: Collection<E>) = assertNotContains(
        this, elements)

fun <E> assertContains(collection: Collection<E>,
                       element: E,
                       message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "<$collection> did not contain <$element>."
    }, collection.contains(element))
}

fun <E> assertContains(collection: Collection<E>,
                       elements: Collection<E>,
                       message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "<$collection> did not contain <$elements>."
    }, collection.containsAll(elements))
}

fun <E> assertNotContains(collection: Collection<E>,
                          element: E,
                          message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "<$collection> contained <$element>."
    }, !collection.contains(element))
}

fun <E> assertNotContains(collection: Collection<E>,
                          elements: Collection<E>,
                          message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "<$collection> contained <$elements>."
    }, !collection.containsAll(elements))
}
