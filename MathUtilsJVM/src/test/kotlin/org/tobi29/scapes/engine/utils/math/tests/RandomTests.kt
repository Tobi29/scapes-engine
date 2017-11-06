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
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.math.Random
import kotlin.test.assertTrue

object RandomTests : Spek({
    given("a random") {
        for (max in 0..10) {
            for (min in -10..max) {
                on("generating random ints in range $min..$max") {
                    val counts = IntArray(max - min + 1)
                    val random = Random(
                            12345L + max * 10000L + min)
                    val count = counts.size * 100
                    for (i in 0 until count) {
                        val r = random.nextInt(min, max)
                        if (r in min..max) {
                            counts[r - min]++
                        }
                        it("should be inside the given range") {
                            assertTrue { r in min..max }
                        }
                    }
                    it("should have similar counts") {
                        val cavg = count / counts.size
                        val jitter = cavg shr 1
                        val cmin = cavg - jitter
                        val cmax = cavg + jitter
                        for (c in counts) {
                            assertTrue { c in cmin..cmax }
                        }
                    }
                }
                on("generating random longs in range $min..$max") {
                    val counts = IntArray(max - min + 1)
                    val random = Random(
                            12345L + max * 10000L + min)
                    val count = counts.size * 100
                    for (i in 0 until count) {
                        val r = random.nextLong(min.toLong(),
                                max.toLong()).toInt()
                        if (r in min..max) {
                            counts[r - min]++
                        }
                        it("should be inside the given range") {
                            assertTrue { r in min..max }
                        }
                    }
                    it("should have similar counts") {
                        val cavg = count / counts.size
                        val jitter = cavg shr 1
                        val cmin = cavg - jitter
                        val cmax = cavg + jitter
                        for (c in counts) {
                            assertTrue { c in cmin..cmax }
                        }
                    }
                }
            }
        }
    }
})
