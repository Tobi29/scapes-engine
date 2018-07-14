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

object MutableStringTests : Spek({
    describe("a mutable string") {
        data(
            { initial, append -> "appending \"$append\" to \"$initial\"" },
            data("A", "B", "AB"),
            data("ABC", "DEF", "ABCDEF")
        ) { initial, append, expect ->
            val actual = initial.toMutableString().append(append).toString()
            it("should return \"$expect\"") {
                actual shouldEqual expect
            }
        }
        data(
            { initial, insert, pos -> "inserting \"$insert\" into \"$initial\" at $pos" },
            data("A", "B", 0, "BA"),
            data("A", "B", 1, "AB"),
            data("ABC", "DEF", 1, "ADEFBC")
        ) { initial, insert, pos, expect ->
            val actual =
                initial.toMutableString().insert(pos, insert).toString()
            it("should return \"$expect\"") {
                actual shouldEqual expect
            }
        }
        data(
            { initial, range -> "deleting the range $range from \"$initial\"" },
            data("A", 0..0, ""),
            data("ABC", 0..0, "BC"),
            data("ABC", 1..1, "AC"),
            data("ABC", 2..2, "AB"),
            data("ABCDEF", 0..2, "DEF"),
            data("ABCDEF", 1..4, "AF"),
            data("ABCDEF", 3..5, "ABC")
        ) { initial, range, expect ->
            val actual = initial.toMutableString().delete(range).toString()
            it("should return \"$expect\"") {
                actual shouldEqual expect
            }
        }
    }
})