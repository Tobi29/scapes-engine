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

import kotlin.math.abs
import kotlin.test.Asserter
import kotlin.test.asserter

infix fun Any?.shouldEqual(other: Any?) =
    asserter.assertEquals(null, other, this)

infix fun Any?.shouldNotEqual(other: Any?) =
    asserter.assertNotEquals(null, other, this)

infix fun Any?.shouldBe(other: Any?) =
    asserter.assertSame(other, this)

infix fun Any?.shouldNotBe(other: Any?) =
    asserter.assertNotSame(other, this)

fun Float.shouldEqual(other: Float, margin: Float = 0.0001f) =
    asserter.assertEquals(
        this, other,
        margin
    )

fun Float.shouldNotEqual(other: Float, margin: Float = 0.0001f) =
    asserter.assertNotEquals(
        this, other,
        margin
    )

fun Double.shouldEqual(other: Double, margin: Double = 0.0001) =
    asserter.assertEquals(
        this, other,
        margin
    )

fun Double.shouldNotEqual(other: Double, margin: Double = 0.0001) =
    asserter.assertNotEquals(
        this,
        other, margin
    )

inline fun <reified E : Throwable> shouldThrow(noinline block: () -> Unit) {
    shouldThrow<E>(null, block)
}

inline fun <reified E : Throwable> shouldThrow(
    message: String?,
    noinline block: () -> Unit
) {
    try {
        block()
        asserter.fail(
            message ?: "Expected to throw ${E::class.simpleName}, but did not."
        )
    } catch (e: Throwable) {
        if (e !is E) {
            throw e
        }
    }
}

fun Asserter.assertSame(
    expected: Any?,
    actual: Any?,
    message: String? = null
) {
    assertTrue({
        (message?.let { "$it. " }
                ?: "") + "Expected <$expected>, actual <$actual>."
    }, actual == expected)
}

fun Asserter.assertNotSame(
    expected: Any?,
    actual: Any?,
    message: String? = null
) {
    assertTrue({
        (message?.let { "$it. " } ?: "") + "Illegal value: <$actual>."
    }, actual != expected)
}

fun Asserter.assertEquals(
    expected: Float,
    actual: Float,
    margin: Float = 0.0001f,
    message: String? = null
) {
    assertTrue({
        (message?.let { "$it. " }
                ?: "") + "Expected <$expected>, actual <$actual> with a margin of error of <$margin>."
    }, actual.isNaN() && expected.isNaN() || abs(actual - expected) <= margin)
}

fun Asserter.assertEquals(
    expected: Double,
    actual: Double,
    margin: Double = 0.0001,
    message: String? = null
) {
    assertTrue({
        (message?.let { "$it. " }
                ?: "") + "Expected <$expected>, actual <$actual> with a margin of error of <$margin>."
    }, actual.isNaN() && expected.isNaN() || abs(actual - expected) <= margin)
}

fun Asserter.assertNotEquals(
    expected: Float,
    actual: Float,
    margin: Float = 0.0001f,
    message: String? = null
) {
    assertTrue({
        (message?.let { "$it. " }
                ?: "") + "Illegal value: <$actual> with a margin of error of <$margin>."
    }, actual.isNaN() && expected.isNaN() || abs(actual - expected) > margin)
}

fun Asserter.assertNotEquals(
    expected: Double,
    actual: Double,
    margin: Double = 0.0001,
    message: String? = null
) {
    assertTrue({
        (message?.let { "$it. " }
                ?: "") + "Illegal value: <$actual> with a margin of error of <$margin>."
    }, actual.isNaN() && expected.isNaN() || abs(actual - expected) > margin)
}
