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

package com.j256.simplemagik.endian

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.tobi29.arrays.sliceOver
import org.tobi29.assertions.shouldEqual

object BigEndianConverterTests : Spek({
    data class SplitLong(
        val b7: Byte, val b6: Byte, val b5: Byte, val b4: Byte,
        val b3: Byte, val b2: Byte, val b1: Byte, val b0: Byte
    ) {
        constructor(
            b7: Int, b6: Int, b5: Int, b4: Int,
            b3: Int, b2: Int, b1: Int, b0: Int
        ) : this(
            b7.toByte(), b6.toByte(), b5.toByte(), b4.toByte(),
            b3.toByte(), b2.toByte(), b1.toByte(), b0.toByte()
        )

        constructor(array: ByteArray) : this(
            array[0], array[1], array[2], array[3],
            array[4], array[5], array[6], array[7]
        )

        val array: ByteArray
            get() = byteArrayOf(
                b7, b6, b5, b4, b3, b2, b1, b0
            )

        override fun toString(): String =
            "${b7.toString(16)} ${b6.toString(16)} ${
            b5.toString(16)} ${b4.toString(16)} ${
            b3.toString(16)} ${b2.toString(16)} ${
            b1.toString(16)} ${b0.toString(16)}"
    }

    val casesLong = listOf(
        0x0000000000000000.toLong() to SplitLong(
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00
        ),
        -1L to SplitLong(
            0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF
        ),
        0x00FF00FF00FF00FF to SplitLong(
            0x00, 0xFF, 0x00, 0xFF,
            0x00, 0xFF, 0x00, 0xFF
        ),
        -0x00FF00FF00FF0100 to SplitLong(
            0xFF, 0x00, 0xFF, 0x00,
            0xFF, 0x00, 0xFF, 0x00
        ),
        0x123456789ABCDEF0 to SplitLong(
            0x12, 0x34, 0x56, 0x78,
            0x9A, 0xBC, 0xDE, 0xF0
        )
    )

    describe("splitting an integer into bytes") {
        for ((input, expected) in casesLong) {
            given("$input") {
                val actual =
                    EndianType.BIG.converter.convertToByteArray(input, 8)
                        ?.let { SplitLong(it) }
                it("should return \"$expected\"") {
                    actual shouldEqual expected
                }
            }
        }
    }

    describe("combining bytes into an integer") {
        for ((expected, input) in casesLong) {
            given("$input") {
                val actual =
                    EndianType.BIG.converter.convertNumber(
                        0, input.array.sliceOver(), 8
                    )
                it("should return \"$input\"") {
                    actual shouldEqual expected
                }
            }
        }
    }
})
