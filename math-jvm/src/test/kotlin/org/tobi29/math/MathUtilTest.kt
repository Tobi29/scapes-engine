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

package org.tobi29.math

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.assertions.shouldEqual
import org.tobi29.stdex.math.ceilToInt
import org.tobi29.stdex.math.floorToInt
import org.tobi29.stdex.math.lb
import kotlin.math.roundToInt

object MathUtilTests : Spek({
    describe("floor") {
        val numbersDouble = listOf(
            Pair(-1.5, -2),
            Pair(-1.0, -1),
            Pair(-0.5, -1),
            Pair(0.0, 0),
            Pair(0.5, 0),
            Pair(1.0, 1),
            Pair(1.5, 1)
        )
        val numbersFloat = numbersDouble.map { (a, b) -> Pair(a.toFloat(), b) }
        for (number in numbersFloat) {
            describe("calculating the floor of ${number.first}") {
                val floor = (number.first).floorToInt()
                it("should equal the mathematical floor") {
                    floor shouldEqual number.second
                }
            }
        }
        for (number in numbersDouble) {
            describe("calculating the floor of ${number.first}") {
                val floor = (number.first).floorToInt()
                it("should equal the mathematical floor") {
                    floor shouldEqual number.second
                }
            }
        }
    }
    describe("ceil") {
        val numbersDouble = listOf(
            Pair(-1.5, -1),
            Pair(-1.0, -1),
            Pair(-0.5, 0),
            Pair(0.0, 0),
            Pair(0.5, 1),
            Pair(1.0, 1),
            Pair(1.5, 2)
        )
        val numbersFloat = numbersDouble.map { (a, b) -> Pair(a.toFloat(), b) }
        for (number in numbersFloat) {
            describe("calculating the ceil of ${number.first}") {
                val ceil = number.first.ceilToInt()
                it("should equal the mathematical ceil") {
                    ceil shouldEqual number.second
                }
            }
        }
        for (number in numbersDouble) {
            describe("calculating the ceil of ${number.first}") {
                val ceil = number.first.ceilToInt()
                it("should equal the mathematical ceil") {
                    ceil shouldEqual number.second
                }
            }
        }
    }
    describe("round") {
        val numbersDouble = listOf(
            Pair(-0.6, -1),
            Pair(-0.5, 0),
            Pair(-0.4, 0),
            Pair(0.4, 0),
            Pair(0.5, 1),
            Pair(0.6, 1)
        )
        val numbersFloat = numbersDouble.map { (a, b) -> Pair(a.toFloat(), b) }
        for (number in numbersFloat) {
            describe("calculating the round of ${number.first}") {
                val round = (number.first).roundToInt()
                it("should equal the mathematical round") {
                    round shouldEqual number.second
                }
            }
        }
        for (number in numbersDouble) {
            describe("calculating the round of ${number.first}") {
                val round = (number.first).roundToInt()
                it("should equal the mathematical round") {
                    round shouldEqual number.second
                }
            }
        }
    }
    describe("log base 2") {
        for (i in 0..30) {
            describe("calculating 2 to the power of $i and then the log base 2") {
                val j = 1 shl i
                val k = lb(j)
                it("should produce the original number") {
                    k shouldEqual i
                }
            }
        }
    }
})
