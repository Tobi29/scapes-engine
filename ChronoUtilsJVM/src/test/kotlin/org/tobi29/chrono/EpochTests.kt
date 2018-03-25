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

package org.tobi29.chrono

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.assertions.shouldEqual
import java.util.*

object EpochTests : Spek({
    describe("converting a date to epoch offset back and forth") {
        val random = Random(123456789L)
        val cases = (0 until 100).map {
            val year = random.nextInt(20000) - 10000
            val month = Month.ofValue(random.nextInt(12) + 1)
            val date = Date(
                year,
                month,
                random.nextInt(month.length(year.isLeap)) + 1
            )
            val time = Time(
                random.nextInt(24),
                random.nextInt(60),
                random.nextInt(60),
                random.nextInt(1000000000)
            )
            DateTime(date, time)
        } + (0 until 100).map {
            val year = random.nextInt()
            val month = Month.ofValue(random.nextInt(12) + 1)
            val date = Date(
                year,
                month,
                random.nextInt(month.length(year.isLeap)) + 1
            )
            val time = Time(
                random.nextInt(24),
                random.nextInt(60),
                random.nextInt(60),
                random.nextInt(1000000000)
            )
            DateTime(date, time)
        } + DateTime(
            Date(Int.MAX_VALUE, Month.DECEMBER, 31),
            Time(23, 59, 59, 999999999)
        ) + DateTime(
            Date(Int.MAX_VALUE, Month.DECEMBER, 31),
            Time(23, 59, 59, 999999998)
        ) + DateTime(
            Date(Int.MIN_VALUE, Month.JANUARY, 1),
            Time(0, 0, 0, 1)
        ) + DateTime(
            Date(Int.MIN_VALUE, Month.JANUARY, 1),
            Time(0, 0, 0, 0)
        ) + DateTime(
            Date(-1, Month.JANUARY, 1),
            Time(0, 0, 0, 0)
        ) + DateTime(
            Date(0, Month.JANUARY, 1),
            Time(0, 0, 0, 0)
        ) + DateTime(
            Date(1, Month.JANUARY, 1),
            Time(0, 0, 0, 0)
        )
        cases.forEach { dateTime ->
            given("the date and time $dateTime") {
                on("converting to epoch offset and back") {
                    val epochOffset = dateTime.toEpochNanos()
                    val actualDateTime = epochOffset.toDateTime()
                    it("should return $dateTime") {
                        actualDateTime shouldEqual dateTime
                    }
                }
            }
        }
    }
})
