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

package org.tobi29.scapes.engine.utils.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.toRegexReplace
import org.tobi29.scapes.engine.utils.toReplace

object StringUtilTests : Spek({
    describe("doing a batch string replace on a string") {
        given("replacements and strings") {
            val replacements = listOf(
                    "ABC" to "123",
                    "BCD" to "ABC").toReplace()
            val strs = listOf(
                    "ABCD" to "123D",
                    "BCD" to "ABC")

            for ((str, expected) in strs) {
                on("applying the replacements on \"$str\"") {
                    val replaced = replacements(str)
                    it("should return \"$expected\"") {
                        replaced shouldEqual expected
                    }
                }
            }
        }
    }
    describe("doing a batch regex replace on a string") {
        given("replacements and strings") {
            val replacements = sequenceOf(
                    "[0-8]+" to "A",
                    "[0-9]+" to "B").toRegexReplace()
            val strs = listOf(
                    "123" to "A",
                    "1239123" to "ABA")

            for ((str, expected) in strs) {
                on("applying the replacements on \"$str\"") {
                    val replaced = replacements(str)
                    it("should return \"$expected\"") {
                        replaced shouldEqual expected
                    }
                }
            }
        }
    }
})
