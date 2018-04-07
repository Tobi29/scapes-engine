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

package org.tobi29.stdex

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.data_driven.data
import org.tobi29.assertions.on
import org.tobi29.assertions.shouldEqual

object StringUtilTests : Spek({
    describe("doing a batch string replace on a string") {
        listOf(
            "ABC" to "123",
            "BCD" to "ABC"
        ).toReplace().let { replacements ->
            on(
                { a -> "applying the replacements on \"$a\"" },
                data("ABCD", "123D"),
                data("BCD", "ABC")
            ) { a, expected ->
                val actual = replacements(a)
                it("should return \"$expected\"") {
                    actual shouldEqual expected
                }
            }
        }
        sequenceOf(
            "[0-8]+" to "A",
            "[0-9]+" to "B"
        ).toRegexReplace().let { replacements ->
            on(
                { a -> "applying the replacements on \"$a\"" },
                data("123", "A"),
                data("1239123", "ABA")
            ) { a, expected ->
                val actual = replacements(a)
                it("should return \"$expected\"") {
                    actual shouldEqual expected
                }
            }
        }
    }
})
