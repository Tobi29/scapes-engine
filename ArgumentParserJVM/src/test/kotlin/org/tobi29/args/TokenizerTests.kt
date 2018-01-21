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

package org.tobi29.args

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.assertions.shouldEqual

object TokenizerTests : Spek({
    describe("tokenizing a string") {
        given("a string with tokens separated by spaces") {
            val tests by memoized {
                listOf("" to listOf(),
                        "\"" to listOf(),
                        "One Two Three" to listOf("One", "Two", "Three"),
                        "One \"Two Three\"" to listOf("One", "Two Three"),
                        "One 'Two Three'" to listOf("One", "Two Three"),
                        "One \"Two' Three\"" to listOf("One", "Two' Three"),
                        "One 'Two\" Three'" to listOf("One", "Two\" Three"),
                        "'One\" '\"Two' Three\"" to listOf("One\" Two' Three"),
                        "\"One' \"'Two\" Three'" to listOf("One' Two\" Three"),
                        "One \"\" Three" to listOf("One", "", "Three"))
            }

            for ((string, expected) in tests) {
                on("tokenizing $string") {
                    val tokens = string.tokenize()
                    it("should return $expected") {
                        tokens shouldEqual expected
                    }
                }
            }
        }
    }
})