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

package org.tobi29.utils

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.tobi29.assertions.shouldEqual
import org.tobi29.stdex.splitToBytes

object NumbersTests : Spek({
    describe("splitting a short into bytes") {
        val cases = listOf(
                0x0000.toShort() to SplitShort(
                        0x00, 0x00),
                0xFFFF.toShort() to SplitShort(
                        0xFF, 0xFF),
                0x00FF.toShort() to SplitShort(
                        0x00, 0xFF),
                0xFF00.toShort() to SplitShort(
                        0xFF, 0x00),
                0x1234.toShort() to SplitShort(
                        0x12, 0x34))

        for ((input, expected) in cases) {
            given("$input") {
                val actual = input.splitToBytes { o1, o0 ->
                    SplitShort(o1, o0)
                }
                it("should return \"$expected\"") {
                    actual shouldEqual expected
                }
            }
        }
    }
    describe("splitting an integer into bytes") {
        val cases = listOf(
                0x00000000 to SplitInt(0x00,
                        0x00, 0x00, 0x00),
                0xFFFFFFFF.toInt() to SplitInt(
                        0xFF, 0xFF, 0xFF, 0xFF),
                0x00FF00FF to SplitInt(0x00,
                        0xFF, 0x00, 0xFF),
                0xFF00FF00.toInt() to SplitInt(
                        0xFF, 0x00, 0xFF, 0x00),
                0x12345678 to SplitInt(0x12,
                        0x34, 0x56, 0x78))

        for ((input, expected) in cases) {
            given("$input") {
                val actual = input.splitToBytes { o3, o2, o1, o0 ->
                    SplitInt(o3, o2, o1, o0)
                }
                it("should return \"$expected\"") {
                    actual shouldEqual expected
                }
            }
        }
    }
    describe("splitting an integer into bytes") {
        val cases = listOf(
                0x0000000000000000.toLong() to SplitLong(
                        0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00),
                -1L to SplitLong(0xFF, 0xFF,
                        0xFF, 0xFF,
                        0xFF, 0xFF, 0xFF, 0xFF),
                0x00FF00FF00FF00FF to SplitLong(
                        0x00, 0xFF, 0x00, 0xFF,
                        0x00, 0xFF, 0x00, 0xFF),
                0x00FF00FF00FF0100 to SplitLong(
                        0xFF, 0x00, 0xFF, 0x00,
                        0xFF, 0x00, 0xFF, 0x00),
                0x123456789ABCDEF0 to SplitLong(
                        0x12, 0x34, 0x56, 0x78,
                        0x9A, 0xBC, 0xDE, 0xF0))

        for ((input, expected) in cases) {
            given("$input") {
                val actual = input.splitToBytes { o7, o6, o5, o4, o3, o2, o1, o0 ->
                    SplitLong(o7, o6, o5, o4, o3,
                            o2, o1, o0)
                }
                it("should return \"$expected\"") {
                    actual shouldEqual expected
                }
            }
        }
    }
})

private data class SplitShort(val b1: Byte,
                              val b0: Byte) {
    constructor(b1: Int,
                b0: Int) : this(b1.toByte(), b0.toByte())

    override fun toString(): String = "${b1.toString(16)} ${b0.toString(16)}"
}

private data class SplitInt(val b3: Byte,
                            val b2: Byte,
                            val b1: Byte,
                            val b0: Byte) {
    constructor(b3: Int,
                b2: Int,
                b1: Int,
                b0: Int) : this(b3.toByte(), b2.toByte(), b1.toByte(),
            b0.toByte())

    override fun toString(): String = "${b3.toString(16)} ${b2.toString(
            16)} ${b1.toString(16)} ${b0.toString(16)}"
}

private data class SplitLong(val b7: Byte,
                             val b6: Byte,
                             val b5: Byte,
                             val b4: Byte,
                             val b3: Byte,
                             val b2: Byte,
                             val b1: Byte,
                             val b0: Byte) {
    constructor(b7: Int,
                b6: Int,
                b5: Int,
                b4: Int,
                b3: Int,
                b2: Int,
                b1: Int,
                b0: Int) : this(b7.toByte(), b6.toByte(), b5.toByte(),
            b4.toByte(), b3.toByte(), b2.toByte(), b1.toByte(), b0.toByte())

    override fun toString(): String = "${b7.toString(16)} ${b6.toString(
            16)} ${b5.toString(16)} ${b4.toString(16)} ${b3.toString(
            16)} ${b2.toString(16)} ${b1.toString(16)} ${b0.toString(16)}"
}
