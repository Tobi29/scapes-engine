/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.chrono

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.assertions.data
import org.tobi29.assertions.shouldEqual
import java.util.*

object EpochTests : Spek({
    describe("converting a date to epoch offset back and forth") {
        val random = Random(123456789L)
        data(
            { a -> "converting $a to epoch and back" },
            *((0 until 100).map {
                val year = random.nextInt(20000) - 10000
                val month = Month.of(random.nextInt(12) + 1)
                Date(
                    year,
                    month,
                    random.nextInt(month.length(year.isLeap)) + 1
                ) to Time(
                    random.nextInt(24),
                    random.nextInt(60),
                    random.nextInt(60),
                    random.nextInt(1000000000)
                )
            } + (0 until 100).map {
                val year = random.nextInt()
                val month = Month.of(random.nextInt(12) + 1)
                Date(
                    year,
                    month,
                    random.nextInt(month.length(year.isLeap)) + 1
                ) to Time(
                    random.nextInt(24),
                    random.nextInt(60),
                    random.nextInt(60),
                    random.nextInt(1000000000)
                )
            } + listOf(
                Date(Int.MAX_VALUE, Month.DECEMBER, 31) to
                        Time(23, 59, 59, 999999999),
                Date(Int.MAX_VALUE, Month.DECEMBER, 31) to
                        Time(23, 59, 59, 999999998),
                Date(Int.MIN_VALUE, Month.JANUARY, 1) to
                        Time(0, 0, 0, 1),
                Date(Int.MIN_VALUE, Month.JANUARY, 1) to
                        Time(0, 0, 0, 0),
                Date(-1, Month.JANUARY, 1) to
                        Time(0, 0, 0, 0),
                Date(0, Month.JANUARY, 1) to
                        Time(0, 0, 0, 0),
                Date(1, Month.JANUARY, 1) to
                        Time(0, 0, 0, 0)
            )).map { data(it, it) }.toTypedArray()
        ) { a, expect ->
            val actual = a.toEpochNanos().toDateTime()
            it("should return $expect") {
                actual shouldEqual expect
            }
        }
    }
})
