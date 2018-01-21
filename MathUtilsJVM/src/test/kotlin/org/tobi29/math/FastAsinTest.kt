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

package org.tobi29.math

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.tobi29.assertions.shouldEqual
import kotlin.math.acos
import kotlin.math.asin

object FastAsinTests : Spek({
    describe("table asin") {
        given("any number") {
            it("should be close to the mathematical asin") {
                var r = -8.0
                while (r <= 8.0) {
                    val asin = FastAsin.asin(r)
                    val expected = asin(r)
                    asin.shouldEqual(expected, 0.007)
                    r += 0.0009765625
                }
            }
        }
    }
    describe("table acos") {
        given("any number") {
            it("should be close to the mathematical acos") {
                var r = -8.0
                while (r <= 8.0) {
                    val acos = FastAsin.acos(r)
                    val expected = acos(r)
                    acos.shouldEqual(expected, 0.007)
                    r += 0.0009765625
                }
            }
        }
    }
})
