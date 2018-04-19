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

import java.util.*
import kotlin.test.asserter

infix fun Array<Any?>?.shouldEqual(other: Array<Any?>?) = assertEquals(other,
        this)

infix fun ByteArray?.shouldEqual(other: ByteArray?) = assertEquals(other, this)

infix fun ShortArray?.shouldEqual(other: ShortArray?) = assertEquals(other,
        this)

infix fun IntArray?.shouldEqual(other: IntArray?) = assertEquals(other, this)

infix fun LongArray?.shouldEqual(other: LongArray?) = assertEquals(other, this)

infix fun FloatArray?.shouldEqual(other: FloatArray?) = assertEquals(other,
        this)

infix fun DoubleArray?.shouldEqual(other: DoubleArray?) = assertEquals(other,
        this)

infix fun BooleanArray?.shouldEqual(other: BooleanArray?) = assertEquals(other,
        this)

infix fun CharArray?.shouldEqual(other: CharArray?) = assertEquals(other, this)

fun assertEquals(expected: Array<Any?>?,
                 actual: Array<Any?>?,
                 message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "Expected <${Arrays.toString(
                expected)}>, actual <${Arrays.toString(actual)}>."
    }, Arrays.equals(expected, actual))
}

fun assertEquals(expected: ByteArray?,
                 actual: ByteArray?,
                 message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "Expected <${Arrays.toString(
                expected)}>, actual <${Arrays.toString(actual)}>."
    }, Arrays.equals(expected, actual))
}

fun assertEquals(expected: ShortArray?,
                 actual: ShortArray?,
                 message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "Expected <${Arrays.toString(
                expected)}>, actual <${Arrays.toString(actual)}>."
    }, Arrays.equals(expected, actual))
}

fun assertEquals(expected: IntArray?,
                 actual: IntArray?,
                 message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "Expected <${Arrays.toString(
                expected)}>, actual <${Arrays.toString(actual)}>."
    }, Arrays.equals(expected, actual))
}

fun assertEquals(expected: LongArray?,
                 actual: LongArray?,
                 message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "Expected <${Arrays.toString(
                expected)}>, actual <${Arrays.toString(actual)}>."
    }, Arrays.equals(expected, actual))
}

fun assertEquals(expected: FloatArray?,
                 actual: FloatArray?,
                 message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "Expected <${Arrays.toString(
                expected)}>, actual <${Arrays.toString(actual)}>."
    }, Arrays.equals(expected, actual))
}

fun assertEquals(expected: DoubleArray?,
                 actual: DoubleArray?,
                 message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "Expected <${Arrays.toString(
                expected)}>, actual <${Arrays.toString(actual)}>."
    }, Arrays.equals(expected, actual))
}

fun assertEquals(expected: BooleanArray?,
                 actual: BooleanArray?,
                 message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "Expected <${Arrays.toString(
                expected)}>, actual <${Arrays.toString(actual)}>."
    }, Arrays.equals(expected, actual))
}

fun assertEquals(expected: CharArray?,
                 actual: CharArray?,
                 message: String? = null) {
    asserter.assertTrue({
        (message?.let { "$it. " } ?: "") + "Expected <${Arrays.toString(
                expected)}>, actual <${Arrays.toString(actual)}>."
    }, Arrays.equals(expected, actual))
}
