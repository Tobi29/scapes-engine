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

infix fun Array<Any?>?.shouldEqual(other: Array<Any?>?) =
    asserter.assertEquals(other, this)

infix fun ByteArray?.shouldEqual(other: ByteArray?) =
    asserter.assertEquals(other, this)

infix fun ShortArray?.shouldEqual(other: ShortArray?) =
    asserter.assertEquals(other, this)

infix fun IntArray?.shouldEqual(other: IntArray?) =
    asserter.assertEquals(other, this)

infix fun LongArray?.shouldEqual(other: LongArray?) =
    asserter.assertEquals(other, this)

infix fun FloatArray?.shouldEqual(other: FloatArray?) =
    asserter.assertEquals(other, this)

infix fun DoubleArray?.shouldEqual(other: DoubleArray?) =
    asserter.assertEquals(other, this)

infix fun BooleanArray?.shouldEqual(other: BooleanArray?) =
    asserter.assertEquals(other, this)

infix fun CharArray?.shouldEqual(other: CharArray?) =
    asserter.assertEquals(other, this)

fun Asserter.assertEquals(
    expected: Array<Any?>?,
    actual: Array<Any?>?,
    message: String? = null
) {
    assertTrue(
        {
            messagePrefix(
                message, "Expected <${expected?.contentDeepToString()
                }>, actual <${actual?.contentDeepToString()}>."
            )
        },
        (expected == null && actual == null)
                || (expected === actual)
                || (expected != null && actual != null
                && expected.contentDeepEquals(actual))
    )
}

fun Asserter.assertEquals(
    expected: ByteArray?,
    actual: ByteArray?,
    message: String? = null
) {
    assertTrue(
        {
            messagePrefix(
                message, "Expected <${expected?.contentToString()
                }>, actual <${actual?.contentToString()}>."
            )
        },
        (expected == null && actual == null)
                || (expected === actual)
                || (expected != null && actual != null
                && expected.contentEquals(actual))
    )
}

fun Asserter.assertEquals(
    expected: ShortArray?,
    actual: ShortArray?,
    message: String? = null
) {
    assertTrue(
        {
            messagePrefix(
                message, "Expected <${expected?.contentToString()
                }>, actual <${actual?.contentToString()}>."
            )
        },
        (expected == null && actual == null)
                || (expected === actual)
                || (expected != null && actual != null
                && expected.contentEquals(actual))
    )
}

fun Asserter.assertEquals(
    expected: IntArray?,
    actual: IntArray?,
    message: String? = null
) {
    assertTrue(
        {
            messagePrefix(
                message, "Expected <${expected?.contentToString()
                }>, actual <${actual?.contentToString()}>."
            )
        },
        (expected == null && actual == null)
                || (expected === actual)
                || (expected != null && actual != null
                && expected.contentEquals(actual))
    )
}

fun Asserter.assertEquals(
    expected: LongArray?,
    actual: LongArray?,
    message: String? = null
) {
    assertTrue(
        {
            messagePrefix(
                message, "Expected <${expected?.contentToString()
                }>, actual <${actual?.contentToString()}>."
            )
        },
        (expected == null && actual == null)
                || (expected === actual)
                || (expected != null && actual != null
                && expected.contentEquals(actual))
    )
}

fun Asserter.assertEquals(
    expected: FloatArray?,
    actual: FloatArray?,
    message: String? = null
) {
    assertTrue(
        {
            messagePrefix(
                message, "Expected <${expected?.contentToString()
                }>, actual <${actual?.contentToString()}>."
            )
        },
        (expected == null && actual == null)
                || (expected === actual)
                || (expected != null && actual != null
                && expected.contentEquals(actual))
    )
}

fun Asserter.assertEquals(
    expected: DoubleArray?,
    actual: DoubleArray?,
    message: String? = null
) {
    assertTrue(
        {
            messagePrefix(
                message, "Expected <${expected?.contentToString()
                }>, actual <${actual?.contentToString()}>."
            )
        },
        (expected == null && actual == null)
                || (expected === actual)
                || (expected != null && actual != null
                && expected.contentEquals(actual))
    )
}

fun Asserter.assertEquals(
    expected: BooleanArray?,
    actual: BooleanArray?,
    message: String? = null
) {
    assertTrue(
        {
            messagePrefix(
                message, "Expected <${expected?.contentToString()
                }>, actual <${actual?.contentToString()}>."
            )
        },
        (expected == null && actual == null)
                || (expected === actual)
                || (expected != null && actual != null
                && expected.contentEquals(actual))
    )
}

fun Asserter.assertEquals(
    expected: CharArray?,
    actual: CharArray?,
    message: String? = null
) {
    assertTrue(
        {
            messagePrefix(
                message, "Expected <${expected?.contentToString()
                }>, actual <${actual?.contentToString()}>."
            )
        },
        (expected == null && actual == null)
                || (expected === actual)
                || (expected != null && actual != null
                && expected.contentEquals(actual))
    )
}
