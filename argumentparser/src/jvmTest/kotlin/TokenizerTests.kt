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

package org.tobi29.args

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.assertions.data
import org.tobi29.assertions.shouldEqual

object TokenizerTests : Spek({
    describe("tokenizing a string") {
        data(
            { a -> "tokenizing $a" },
            data("", listOf()),
            data("\"", listOf()),
            data("One Two Three", listOf("One", "Two", "Three")),
            data("One \"Two Three\"", listOf("One", "Two Three")),
            data("One 'Two Three'", listOf("One", "Two Three")),
            data("One \"Two' Three\"", listOf("One", "Two' Three")),
            data("One 'Two\" Three'", listOf("One", "Two\" Three")),
            data("'One\" '\"Two' Three\"", listOf("One\" Two' Three")),
            data("\"One' \"'Two\" Three'", listOf("One' Two\" Three")),
            data("One \"\" Three", listOf("One", "", "Three"))
        ) { a, expected ->
            val actual = a.tokenize()
            it("should return $expected") {
                actual shouldEqual expected
            }
        }
    }
})