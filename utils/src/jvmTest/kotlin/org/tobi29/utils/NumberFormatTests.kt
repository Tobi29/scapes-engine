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

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.assertions.data
import org.tobi29.assertions.shouldEqual

object NumberFormatTests : Spek({
    describe("converting a floating point number ") {
        data class Expected(
            val decimal0: String,
            val decimal6: String,
            val decimal8: String,
            val exponential0: String,
            val exponential6: String,
            val exponential8: String
        )

        val cases = arrayOf(
            data(
                1.0000000000000000,
                Expected(
                    "1", "1.000000", "1.00000000", "1e+00",
                    "1.000000e+00", "1.00000000e+00"
                )
            ),
            data(
                0.2400000000000000,
                Expected(
                    "0", "0.240000", "0.24000000",
                    "2e-01", "2.400000e-01", "2.40000000e-01"
                )
            ),
            data(
                0.1000000000000000,
                Expected(
                    "0", "0.100000", "0.10000000",
                    "1e-01", "1.000000e-01", "1.00000000e-01"
                )
            ),
            data(
                4.0000000000000000,
                Expected(
                    "4", "4.000000", "4.00000000",
                    "4e+00", "4.000000e+00", "4.00000000e+00"
                )
            ),
            data(
                5.0000000000000000,
                Expected(
                    "5", "5.000000", "5.00000000",
                    "5e+00", "5.000000e+00", "5.00000000e+00"
                )
            ),
            data(
                1000000.0000000000000000,
                Expected(
                    "1000000", "1000000.000000", "1000000.00000000",
                    "1e+06", "1.000000e+06", "1.00000000e+06"
                )
            ),
            data(
                0.0000001000000000,
                Expected(
                    "0", "0.000000", "0.00000010",
                    "1e-07", "1.000000e-07", "1.00000000e-07"
                )
            )
        )

        data({ a -> "formatting $a with precision 0" }, *cases) { a, expected ->
            val decimal0 = a.toStringDecimal(0)
            it("should return ${expected.decimal0}") {
                decimal0 shouldEqual expected.decimal0
            }
        }

        data({ a -> "formatting $a with precision 6" }, *cases) { a, expected ->
            val decimal6 = a.toStringDecimal(6)
            it("should return ${expected.decimal0}") {
                decimal6 shouldEqual expected.decimal6
            }
        }

        data({ a -> "formatting $a with precision 8" }, *cases) { a, expected ->
            val decimal8 = a.toStringDecimal(8)
            it("should return ${expected.decimal0}") {
                decimal8 shouldEqual expected.decimal8
            }
        }

        data({ a -> "formatting $a with precision 0" }, *cases) { a, expected ->
            val exponential0 = a.toStringExponential(0)
            it("should return ${expected.exponential0}") {
                exponential0 shouldEqual expected.exponential0
            }
        }

        data({ a -> "formatting $a with precision 6" }, *cases) { a, expected ->
            val exponential6 = a.toStringExponential(6)
            it("should return ${expected.exponential0}") {
                exponential6 shouldEqual expected.exponential6
            }
        }

        data({ a -> "formatting $a with precision 8" }, *cases) { a, expected ->
            val exponential8 = a.toStringExponential(8)
            it("should return ${expected.exponential0}") {
                exponential8 shouldEqual expected.exponential8
            }
        }
    }
})
