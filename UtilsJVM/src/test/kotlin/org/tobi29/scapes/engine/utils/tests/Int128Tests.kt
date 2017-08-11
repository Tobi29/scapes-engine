package org.tobi29.scapes.engine.utils.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.Int128
import org.tobi29.scapes.engine.utils.toInt128
import java.math.BigInteger
import java.util.*

object Int128Tests : Spek({
    describe("converting a number to string and back") {
        given("a number") {
            on("converting it to a string and back") {
                val random = Random(123456L)
                repeat(10000) {
                    val number = Int128(random.nextLong(), random.nextLong())
                    val str = number.toString()
                    val result = str.toInt128()
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
                val a = Int128(random.nextLong(), random.nextLong())
                val b = Int128(random.nextLong(), random.nextLong())
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
                val a = Int128(random.nextLong(), random.nextLong())
                val b = Int128(random.nextLong(), random.nextLong())
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
                val a = Int128(random.nextLong(), random.nextLong())
                val b = Int128(random.nextLong(), random.nextLong())
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
                val a = Int128(random.nextLong(), random.nextLong())
                val b = Int128(random.nextLong(), random.nextLong())
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
                    "-170141183460469231731687303715884105728" to "1",
                    "-170141183460469231731687303715884105728" to "2",
                    "-170141183460469231731687303715884105728" to "3",
                    "-170141183460469231731687303715884105728" to "-1",
                    "-170141183460469231731687303715884105728" to "-2",
                    "-170141183460469231731687303715884105728" to "-3",
                    "170141183460469231731687303715884105727" to "1",
                    "170141183460469231731687303715884105727" to "2",
                    "170141183460469231731687303715884105727" to "3",
                    "170141183460469231731687303715884105727" to "-2",
                    "170141183460469231731687303715884105727" to "-3")) {
                val result = (a.toInt128() / b.toInt128()).toString()
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
                val a = Int128(random.nextLong(), random.nextLong())
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
                val a = Int128(random.nextLong(), random.nextLong())
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
    if (rem >= BigInteger("170141183460469231731687303715884105728")) {
        rem -= BigInteger("340282366920938463463374607431768211456")
    }
    return rem
}