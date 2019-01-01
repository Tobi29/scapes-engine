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

object VersionTests : Spek({
    describe("versions") {
        data(
            { a, b -> "comparing $a with $b" },
            data(Version(1, 1, 1), Version(1, 1, 1), 0),
            data(Version(2, 1, 1), Version(1, 2, 2), 1),
            data(Version(1, 2, 2), Version(2, 1, 1), -1),
            data(Version(1, 2, 1), Version(1, 1, 2), 1),
            data(Version(1, 1, 2), Version(1, 2, 1), -1),
            data(Version(1, 1, 2), Version(1, 1, 1), 1),
            data(Version(1, 1, 1), Version(1, 1, 2), -1)
        ) { a, b, expect ->
            val actual = a.compareTo(b)
            it("should return $expect") {
                actual shouldEqual expect
            }
        }
        data(
            { a, b -> "checking if $a is greater than $b" },
            data(Version(1, 1, 1), Version(1, 1, 1), false),
            data(Version(2, 1, 1), Version(1, 1, 1), true),
            data(Version(1, 1, 1), Version(2, 1, 1), false)
        ) { a, b, expect ->
            val actual = a > b
            it("should return $expect") {
                actual shouldEqual expect
            }
        }
    }
})
