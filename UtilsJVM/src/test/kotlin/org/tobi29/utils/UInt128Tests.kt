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

package org.tobi29.utils

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.data_driven.data
import org.tobi29.assertions.on
import org.tobi29.assertions.shouldEqual
import java.math.BigInteger
import java.util.*

object UInt128Tests : Spek({
    describe("128-bit signed integers") {
        val random = Random(1234567L)
        on(
            { a -> "converting $a to a string and back" },
            *(0 until 100).map { random.randomUInt128() }.map { a ->
                data(a, a)
            }.toTypedArray()
        ) { a, expect ->
            val actual = a.toString().toUInt128()
            it("should return $expect") {
                actual shouldEqual expect
            }
        }
        on(
            { a, b -> "adding $a to $b" },
            *(0 until 100).map { random.randomUInt128Pair() }.map { (a, b) ->
                data(
                    a, b,
                    simulateOverflow(
                        BigInteger(a.toString()) + BigInteger(b.toString())
                    ).toString()
                )
            }.toTypedArray()
        ) { a, b, expect ->
            val actual = (a + b).toString()
            it("should return $expect") {
                actual shouldEqual expect
            }
        }
        on(
            { a, b -> "subtracting $a from $b" },
            *(0 until 100).map { random.randomUInt128Pair() }.map { (a, b) ->
                data(
                    a, b,
                    simulateOverflow(
                        BigInteger(a.toString()) - BigInteger(b.toString())
                    ).toString()
                )
            }.toTypedArray()
        ) { a, b, expect ->
            val actual = (a - b).toString()
            it("should return $expect") {
                actual shouldEqual expect
            }
        }
        on(
            { a, b -> "multiplying $a by $b" },
            *(0 until 100).map { random.randomUInt128Pair() }.map { (a, b) ->
                data(
                    a, b,
                    simulateOverflow(
                        BigInteger(a.toString()) * BigInteger(b.toString())
                    ).toString()
                )
            }.toTypedArray()
        ) { a, b, expect ->
            val actual = (a * b).toString()
            it("should return $expect") {
                actual shouldEqual expect
            }
        }
        on(
            { a, b -> "dividing $a by $b" },
            *((0 until 100).map { random.randomUInt128Pair() } +
                    listOf(
                        "170141183460469231731687303715884105727" to "1",
                        "170141183460469231731687303715884105727" to "2",
                        "170141183460469231731687303715884105727" to "3"
                    ).map { (a, b) -> a.toUInt128() to b.toUInt128() }
                    ).map { (a, b) ->
                data(
                    a, b,
                    simulateOverflow(
                        BigInteger(a.toString()) / BigInteger(b.toString())
                    ).toString()
                )
            }.toTypedArray()
        ) { a, b, expect ->
            val actual = (a / b).toString()
            it("should return $expect") {
                actual shouldEqual expect
            }
        }
        on(
            { a, b -> "shifting $a $b to the left" },
            *(0 until 100).map { random.randomUInt128() to random.nextInt(64) }.map { (a, b) ->
                data(
                    a, b,
                    simulateOverflow(
                        BigInteger(a.toString()).shiftLeft(b)
                    ).toString()
                )
            }.toTypedArray()
        ) { a, b, expect ->
            val actual = (a shl b).toString()
            it("should return $expect") {
                actual shouldEqual expect
            }
        }
        on(
            { a, b -> "shifting $a $b to the right" },
            *(0 until 100).map { random.randomUInt128() to random.nextInt(64) }.map { (a, b) ->
                data(
                    a, b,
                    simulateOverflow(
                        BigInteger(a.toString()).shiftRight(b)
                    ).toString()
                )
            }.toTypedArray()
        ) { a, b, expect ->
            val actual = (a shr b).toString()
            it("should return $expect") {
                actual shouldEqual expect
            }
        }
    }
})

private fun Random.randomUInt128Pair() = randomUInt128() to randomUInt128()

private fun Random.randomUInt128() = UInt128(nextLong(), nextLong())

private fun simulateOverflow(value: BigInteger): BigInteger {
    var rem = value.rem(BigInteger("340282366920938463463374607431768211456"))
    if (rem < BigInteger.ZERO) {
        rem += BigInteger("340282366920938463463374607431768211456")
    }
    return rem
}