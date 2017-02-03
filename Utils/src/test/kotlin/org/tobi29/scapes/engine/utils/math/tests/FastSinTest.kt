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
import org.tobi29.scapes.engine.utils.math.FastSin

object FastSinTests : Spek({
    describe("table sin") {
        given("any number") {
            var rv = -8.0
            while (rv <= 8.0) {
                val r = rv
                on("calculating the sin") {
                    val sin = FastSin.sin(r)
                    it("should be close to the mathematical sin") {
                        val expected = Math.sin(r)
                        sin.shouldEqual(expected, 0.03)
                    }
                }
                rv += 0.0009765625
            }
        }
    }
    describe("table cos") {
        given("any number") {
            var rv = -8.0
            while (rv <= 8.0) {
                val r = rv
                on("calculating the cos") {
                    val sin = FastSin.cos(r)
                    it("should be close to the mathematical cos") {
                        val expected = Math.cos(r)
                        sin.shouldEqual(expected, 0.03)
                    }
                }
                rv += 0.0009765625
            }
        }
    }
})
