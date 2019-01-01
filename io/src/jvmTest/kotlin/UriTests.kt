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

package org.tobi29.io

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import org.tobi29.assertions.shouldEqual

object UriTests : Spek({
    Feature("a uri") {
        val base = Uri("http://a/b/c/d;p?q")
        for ((path, expected) in listOf(
            "g:h" to "g:h",
            "g" to "http://a/b/c/g",
            "./g" to "http://a/b/c/g",
            "g/" to "http://a/b/c/g/",
            "/g" to "http://a/g",
            "//g" to "http://g",
            "?y" to "http://a/b/c/?y",
            "g?y" to "http://a/b/c/g?y",
            // FIXME: "#s" to "http://a/b/c/d;p?q#s", // "(current document)#s",
            "g#s" to "http://a/b/c/g#s",
            "g?y#s" to "http://a/b/c/g?y#s",
            ";x" to "http://a/b/c/;x",
            "g;x" to "http://a/b/c/g;x",
            "g;x?y#s" to "http://a/b/c/g;x?y#s",
            "." to "http://a/b/c/",
            "./" to "http://a/b/c/",
            ".." to "http://a/b/",
            "../" to "http://a/b/",
            "../g" to "http://a/b/g",
            "../.." to "http://a/",
            "../../" to "http://a/",
            "../../g" to "http://a/g",
            "../../../g" to "http://a/../g",
            "../../../../g" to "http://a/../../g",
            "/./g" to "http://a/./g",
            "/../g" to "http://a/../g",
            "g." to "http://a/b/c/g.",
            ".g" to "http://a/b/c/.g",
            "g.." to "http://a/b/c/g..",
            "..g" to "http://a/b/c/..g",
            "./../g" to "http://a/b/g",
            "./g/." to "http://a/b/c/g/",
            "g/./h" to "http://a/b/c/g/h",
            "g/../h" to "http://a/b/c/h",
            "g;x=1/./y" to "http://a/b/c/g;x=1/y",
            "g;x=1/../y" to "http://a/b/c/y",
            "g?y/./x" to "http://a/b/c/g?y/./x",
            "g?y/../x" to "http://a/b/c/g?y/../x",
            "g#s/./x" to "http://a/b/c/g#s/./x",
            "g#s/../x" to "http://a/b/c/g#s/../x"
        ).map { (a, b) -> Uri(a) to Uri(b) }) {
            Scenario("resolving two uris") {
                When("resolving \"$base\" with \"$path\"") {
                }
                Then("should return \"$expected\"") {
                    base.resolve(path) shouldEqual expected
                }
            }
        }
    }
})

