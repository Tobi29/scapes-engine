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
import org.tobi29.stdex.math.toRad
import kotlin.math.cos
import kotlin.math.sin

object FastSinTests : Spek({
    describe("table sin") {
        it("should be close to the mathematical sin") {
            var r = -8.0
            while (r <= 8.0) {
                val sin = FastSin.sin(r)
                val expected = sin(r)
                sin.shouldEqual(expected, 0.002)
                r += 0.0009765625
            }
            val big = 1e10
            r = big
            repeat(10000) {
                val sin = FastSin.sin(r)
                val expected = sin(r)
                sin.shouldEqual(expected, 0.002)
                r += 0.1
            }
        }
        it("should be equal to the mathematical sin") {
            for ((r, expected) in sequenceOf(
                (-360.0).toRad() to 0.0,
                (-270.0).toRad() to 1.0,
                (-180.0).toRad() to 0.0,
                (-90.0).toRad() to -1.0,
                0.0.toRad() to 0.0,
                90.0.toRad() to 1.0,
                180.0.toRad() to 0.0,
                270.0.toRad() to -1.0,
                360.0.toRad() to 0.0
            )) {
                val sin = FastSin.sin(r)
                sin shouldEqual expected
            }
        }
    }
    describe("table cos") {
        it("should be close to the mathematical cos") {
            var r = -8.0
            while (r <= 8.0) {
                val cos = FastSin.cos(r)
                val expected = cos(r)
                cos.shouldEqual(expected, 0.002)
                r += 0.0009765625
            }
            val big = 1e10
            r = big
            repeat(10000) {
                val cos = FastSin.cos(r)
                val expected = cos(r)
                cos.shouldEqual(expected, 0.002)
                r += 0.1
            }
        }
        it("should be equal to the mathematical cos") {
            for ((r, expected) in sequenceOf(
                (-360.0).toRad() to 1.0,
                (-270.0).toRad() to 0.0,
                (-180.0).toRad() to -1.0,
                (-90.0).toRad() to 0.0,
                0.0.toRad() to 1.0,
                90.0.toRad() to 0.0,
                180.0.toRad() to -1.0,
                270.0.toRad() to 0.0,
                360.0.toRad() to 1.0
            )) {
                val sin = FastSin.cos(r)
                sin shouldEqual expected
            }
        }
    }
})
