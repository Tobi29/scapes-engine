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

import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.asserter

infix fun Any?.shouldEqual(other: Any?) = assertEquals(other, this)

infix fun Any?.shouldBe(other: Any?) = assertSame(other, this)

fun Float.shouldEqual(other: Float,
                      margin: Float = 0.0001f) = assertEquals(this, other,
        margin)

fun Double.shouldEqual(other: Double,
                       margin: Double = 0.0001) = assertEquals(this, other,
        margin)

inline fun <reified E : Throwable> shouldThrow(noinline block: () -> Unit) {
    shouldThrow<E>(null, block)
}

inline fun <reified E : Throwable> shouldThrow(message: String?,
                                               noinline block: () -> Unit) {
    shouldThrowAndMsg(message, block, E::class)
}

fun <E : Throwable> shouldThrowAndMsg(message: String?,
                                      block: () -> Unit,
                                      clazz: KClass<E>) {
    try {
        block()
        asserter.fail(
                message ?: "Expected to throw ${clazz.simpleName}, but did not.")
    } catch (e: E) {
    }
}

fun assertSame(expected: Any?,
               actual: Any?,
               message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "Expected <$expected>, actual <$actual>."
    }, actual == expected)
}

fun assertEquals(expected: Float,
                 actual: Float,
                 margin: Float = 0.0001f,
                 message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "Expected <$expected>, actual <$actual> with a margin of error of <$margin>."
    }, actual.isNaN() && expected.isNaN() || actual - expected <= margin)
}

fun assertEquals(expected: Double,
                 actual: Double,
                 margin: Double = 0.0001,
                 message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "Expected <$expected>, actual <$actual> with a margin of error of <$margin>."
    }, actual.isNaN() && expected.isNaN() || actual - expected <= margin)
}
