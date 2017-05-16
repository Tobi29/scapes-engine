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

package org.tobi29.scapes.engine.utils.math.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.math.ceil
import org.tobi29.scapes.engine.utils.math.floor
import org.tobi29.scapes.engine.utils.math.lb
import org.tobi29.scapes.engine.utils.math.round

object MathUtilTests : Spek({
    describe("floor") {
        val numbersDouble = listOf(
                Pair(-1.5, -2),
                Pair(-1.0, -1),
                Pair(-0.5, -1),
                Pair(0.0, 0),
                Pair(0.5, 0),
                Pair(1.0, 1),
                Pair(1.5, 1))
        val numbersFloat = numbersDouble.map { (a, b) -> Pair(a.toFloat(), b) }
        given("any float number") {
            for (number in numbersFloat) {
                on("calculating the floor") {
                    val floor = floor(number.first)
                    it("should equal the mathematical floor") {
                        floor shouldEqual number.second
                    }
                }
            }
        }
        given("any double number") {
            for (number in numbersDouble) {
                on("calculating the floor") {
                    val floor = floor(number.first)
                    it("should equal the mathematical floor") {
                        floor shouldEqual number.second
                    }
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
                Pair(1.5, 2))
        val numbersFloat = numbersDouble.map { (a, b) -> Pair(a.toFloat(), b) }
        given("any float number") {
            for (number in numbersFloat) {
                on("calculating the ceil") {
                    val ceil = ceil(number.first)
                    it("should equal the mathematical ceil") {
                        ceil shouldEqual number.second
                    }
                }
            }
        }
        given("any double number") {
            for (number in numbersDouble) {
                on("calculating the ceil") {
                    val ceil = ceil(number.first)
                    it("should equal the mathematical ceil") {
                        ceil shouldEqual number.second
                    }
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
                Pair(0.6, 1))
        val numbersFloat = numbersDouble.map { (a, b) -> Pair(a.toFloat(), b) }
        given("any float number") {
            for (number in numbersFloat) {
                on("calculating the round") {
                    val round = round(number.first)
                    it("should equal the mathematical round") {
                        round shouldEqual number.second
                    }
                }
            }
        }
        given("any double number") {
            for (number in numbersDouble) {
                on("calculating the round") {
                    val round = round(number.first)
                    it("should equal the mathematical round") {
                        round shouldEqual number.second
                    }
                }
            }
        }
    }
    describe("log base 2") {
        given("any number") {
            for (i in 0..30) {
                on("calculating 2 to the power of it and then the log base 2") {
                    val j = 1 shl i
                    val k = lb(j)
                    it("should produce the original number") {
                        k shouldEqual i
                    }
                }
            }
        }
    }
})
