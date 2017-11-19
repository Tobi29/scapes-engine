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

package org.tobi29.scapes.engine.utils.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.UInt128
import org.tobi29.scapes.engine.utils.toUInt128
import java.math.BigInteger
import java.util.*

object UUInt128Tests : Spek({
    describe("converting a number to string and back") {
        given("a number") {
            on("converting it to a string and back") {
                val random = Random(123456L)
                repeat(10000) {
                    val number = UInt128(random.nextLong(), random.nextLong())
                    val str = number.toString()
                    val result = str.toUInt128()
                    it("should return the same number") {
                        result shouldEqual number
                    }
                }
            }
        }
    }
    describe("adding 128-bit signed integers") {
        given("two numbers") {
            val random = Random(1234567L)
            repeat(10000) {
                val a = UInt128(random.nextLong(), random.nextLong())
                val b = UInt128(random.nextLong(), random.nextLong())
                val result = (a + b).toString()
                val expected = simulateOverflow(
                        BigInteger(a.toString()) + BigInteger(
                                b.toString())).toString()
                it("should return the correct result") {
                    result shouldEqual expected
                }
            }
        }
    }
    describe("subtracting 128-bit signed integers") {
        given("two numbers") {
            val random = Random(1234567L)
            repeat(10000) {
                val a = UInt128(random.nextLong(), random.nextLong())
                val b = UInt128(random.nextLong(), random.nextLong())
                val result = (a - b).toString()
                val expected = simulateOverflow(
                        BigInteger(a.toString()) - BigInteger(
                                b.toString())).toString()
                it("should return the correct result") {
                    result shouldEqual expected
                }
            }
        }
    }
    describe("multiplying 128-bit signed integers") {
        given("two numbers") {
            val random = Random(1234567L)
            repeat(10000) {
                val a = UInt128(random.nextLong(), random.nextLong())
                val b = UInt128(random.nextLong(), random.nextLong())
                val result = (a * b).toString()
                val expected = simulateOverflow(
                        BigInteger(a.toString()) * BigInteger(
                                b.toString())).toString()
                it("should return the correct result") {
                    result shouldEqual expected
                }
            }
        }
    }
    describe("dividing 128-bit signed integers") {
        given("two numbers") {
            val random = Random(1234567L)
            repeat(10000) {
                val a = UInt128(random.nextLong(), random.nextLong())
                val b = UInt128(random.nextLong(), random.nextLong())
                val result = (a / b).toString()
                val expected = simulateOverflow(
                        BigInteger(a.toString()) / BigInteger(
                                b.toString())).toString()
                it("should return the correct result") {
                    result shouldEqual expected
                }
            }
        }
        given("two numbers near limits") {
            for ((a, b) in listOf(
                    "170141183460469231731687303715884105727" to "1",
                    "170141183460469231731687303715884105727" to "2",
                    "170141183460469231731687303715884105727" to "3")) {
                val result = (a.toUInt128() / b.toUInt128()).toString()
                val expected = simulateOverflow(
                        BigInteger(a) / BigInteger(b)).toString()
                it("should return the correct result") {
                    result shouldEqual expected
                }
            }
        }
    }
    describe("left shifting 128-bit signed integers") {
        given("two numbers") {
            val random = Random(1234567L)
            repeat(10000) {
                val a = UInt128(random.nextLong(), random.nextLong())
                val b = random.nextInt(64)
                val result = (a shl b).toString()
                val expected = simulateOverflow(
                        BigInteger(a.toString()).shiftLeft(b)).toString()
                it("should return the correct result") {
                    result shouldEqual expected
                }
            }
        }
    }
    describe("right shifting 128-bit signed integers") {
        given("two numbers") {
            val random = Random(1234567L)
            repeat(10000) {
                val a = UInt128(random.nextLong(), random.nextLong())
                val b = random.nextInt(64)
                val result = (a shr b).toString()
                val expected = simulateOverflow(
                        BigInteger(a.toString()).shiftRight(b)).toString()
                it("should return the correct result") {
                    result shouldEqual expected
                }
            }
        }
    }
})

private fun simulateOverflow(value: BigInteger): BigInteger {
    var rem = value.rem(BigInteger("340282366920938463463374607431768211456"))
    if (rem < BigInteger.ZERO) {
        rem += BigInteger("340282366920938463463374607431768211456")
    }
    return rem
}