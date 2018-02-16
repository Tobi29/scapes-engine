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
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.assertions.shouldEqual

object NumberFormatTests : Spek({
    data class Test(
        val input: Double,
        val decimal0: String,
        val decimal6: String,
        val decimal8: String,
        val exponential0: String,
        val exponential6: String,
        val exponential8: String
    )

    val cases = listOf(
        Test(
            1.0000000000000000,
            "1", "1.000000", "1.00000000",
            "1e+00", "1.000000e+00", "1.00000000e+00"
        ),
        Test(
            0.2500000000000000,
            "0", "0.250000", "0.25000000",
            "2e-01", "2.500000e-01", "2.50000000e-01"
        ),
        Test(
            0.1000000000000000,
            "0", "0.100000", "0.10000000",
            "1e-01", "1.000000e-01", "1.00000000e-01"
        ),
        Test(
            4.0000000000000000,
            "4", "4.000000", "4.00000000",
            "4e+00", "4.000000e+00", "4.00000000e+00"
        ),
        Test(
            5.0000000000000000,
            "5", "5.000000", "5.00000000",
            "5e+00", "5.000000e+00", "5.00000000e+00"
        ),
        Test(
            1000000.0000000000000000,
            "1000000", "1000000.000000", "1000000.00000000",
            "1e+06", "1.000000e+06", "1.00000000e+06"
        ),
        Test(
            0.0000001000000000,
            "0", "0.000000", "0.00000010",
            "1e-07", "1.000000e-07", "1.00000000e-07"
        )
    )

    describe("converting a floating point number ") {
        for ((input,
                expectedDecimal0, expectedDecimal6, expectedDecimal8,
                expectedExponential0, expectedExponential6, expectedExponential8
        ) in cases) {
            given("$input") {
                on("formatting with precision 0") {
                    val decimal0 = input.toStringDecimal(0)
                    it("should return $expectedDecimal0") {
                        decimal0 shouldEqual expectedDecimal0
                    }
                }

                on("formatting with precision 6") {
                    val decimal6 = input.toStringDecimal(6)
                    it("should return $expectedDecimal0") {
                        decimal6 shouldEqual expectedDecimal6
                    }
                }

                on("formatting with precision 8") {
                    val decimal8 = input.toStringDecimal(8)
                    it("should return $expectedDecimal0") {
                        decimal8 shouldEqual expectedDecimal8
                    }
                }

                on("formatting with precision 0") {
                    val exponential0 = input.toStringExponential(0)
                    it("should return $expectedExponential0") {
                        exponential0 shouldEqual expectedExponential0
                    }
                }

                on("formatting with precision 6") {
                    val exponential6 = input.toStringExponential(6)
                    it("should return $expectedExponential0") {
                        exponential6 shouldEqual expectedExponential6
                    }
                }

                on("formatting with precision 8") {
                    val exponential8 = input.toStringExponential(8)
                    it("should return $expectedExponential0") {
                        exponential8 shouldEqual expectedExponential8
                    }
                }
            }
        }
    }
})
