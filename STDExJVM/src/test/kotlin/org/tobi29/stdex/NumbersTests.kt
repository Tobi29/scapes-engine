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

package org.tobi29.stdex

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.data_driven.data
import org.tobi29.assertions.on
import org.tobi29.assertions.shouldEqual

object NumbersTests : Spek({
    data class SplitShort(
        val b1: Byte, val b0: Byte
    ) {
        constructor(
            b1: Int, b0: Int
        ) : this(
            b1.toByte(), b0.toByte()
        )

        override fun toString(): String =
            "${b1.toString(16)} ${b0.toString(16)}"
    }

    data class SplitInt(
        val b3: Byte, val b2: Byte, val b1: Byte, val b0: Byte
    ) {
        constructor(
            b3: Int, b2: Int, b1: Int, b0: Int
        ) : this(
            b3.toByte(), b2.toByte(), b1.toByte(), b0.toByte()
        )

        override fun toString(): String =
            "${b3.toString(16)} ${b2.toString(16)} ${
            b1.toString(16)} ${b0.toString(16)}"
    }

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

        override fun toString(): String =
            "${b7.toString(16)} ${b6.toString(16)} ${
            b5.toString(16)} ${b4.toString(16)} ${
            b3.toString(16)} ${b2.toString(16)} ${
            b1.toString(16)} ${b0.toString(16)}"
    }

    describe("various operations on numbers") {
        on(
            { a -> "splitting short $a into bytes" },
            data(
                0x0000.toShort(),
                SplitShort(0x00, 0x00)
            ),
            data(
                0xFFFF.toShort(),
                SplitShort(0xFF, 0xFF)
            ),
            data(
                0x00FF.toShort(),
                SplitShort(0x00, 0xFF)
            ),
            data(
                0xFF00.toShort(),
                SplitShort(0xFF, 0x00)
            ),
            data(
                0x1234.toShort(),
                SplitShort(0x12, 0x34)
            )
        ) { a, expected ->
            val actual = a.splitToBytes { o1, o0 ->
                SplitShort(o1, o0)
            }
            it("should return $expected") {
                actual shouldEqual expected
            }
        }
        on(
            { a -> "splitting integer $a into bytes" },
            data(
                0x00000000,
                SplitInt(0x00, 0x00, 0x00, 0x00)
            ),
            data(
                0xFFFFFFFF.toInt(),
                SplitInt(0xFF, 0xFF, 0xFF, 0xFF)
            ),
            data(
                0x00FF00FF,
                SplitInt(0x00, 0xFF, 0x00, 0xFF)
            ),
            data(
                0xFF00FF00.toInt(),
                SplitInt(0xFF, 0x00, 0xFF, 0x00)
            ),
            data(
                0x12345678,
                SplitInt(0x12, 0x34, 0x56, 0x78)
            )
        ) { a, expected ->
            val actual = a.splitToBytes { o3, o2, o1, o0 ->
                SplitInt(o3, o2, o1, o0)
            }
            it("should return $expected") {
                actual shouldEqual expected
            }
        }
        on(
            { a -> "splitting long $a into bytes" },
            data(
                0x0000000000000000L,
                SplitLong(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
            ),
            data(
                -1L /* 0xFFFFFFFFFFFFFFFFL */,
                SplitLong(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF)
            ),
            data(
                0x00FF00FF00FF00FFL,
                SplitLong(0x00, 0xFF, 0x00, 0xFF, 0x00, 0xFF, 0x00, 0xFF)
            ),
            data(
                -0x00FF00FF00FF0100L /* 0xFF00FF00FF00FF00L */,
                SplitLong(0xFF, 0x00, 0xFF, 0x00, 0xFF, 0x00, 0xFF, 0x00)
            ),
            data(
                0x123456789ABCDEF0L,
                SplitLong(0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0)
            )
        ) { a, expected ->
            val actual = a.splitToBytes { o7, o6, o5, o4, o3, o2, o1, o0 ->
                SplitLong(o7, o6, o5, o4, o3, o2, o1, o0)
            }
            it("should return $expected") {
                actual shouldEqual expected
            }
        }
    }
})
