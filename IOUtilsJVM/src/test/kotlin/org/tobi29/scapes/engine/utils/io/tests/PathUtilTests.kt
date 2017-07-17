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

package org.tobi29.scapes.engine.utils.io.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.io.UnixPathEnvironment

object PathUtilTests : Spek({
    UnixPathEnvironment.run {
        given("two unix paths") {
            val paths = listOf(
                    Pair("a", "b") to "a/b",
                    Pair("/a", "b") to "/a/b",
                    Pair("a", "/b") to "/b",
                    Pair("a/", "b") to "a/b",
                    Pair("a", "b/") to "a/b",
                    Pair("a/b", ".") to "a/b/.",
                    Pair("a/b", "..") to "a/b/..",
                    Pair("a/b", "./c") to "a/b/./c",
                    Pair("a/b", "../c") to "a/b/../c",
                    Pair("", "a") to "a",
                    Pair("", "./a") to "./a",
                    Pair("", "../a") to "../a",
                    Pair("a//b", "c") to "a/b/c",
                    Pair("a", "b//c") to "a/b/c",
                    Pair("a///////", "b////////") to "a/b"
            )
            for ((operands, expected) in paths) {
                on("resolving \"${operands.first}\" to \"${operands.second}\"") {
                    val path = operands.first.resolve(operands.second)
                    it("should return \"$expected\"") {
                        path shouldEqual expected
                    }
                }
            }
        }
        given("two unix paths") {
            val paths = listOf(
                    Pair("a/b", "a/b") to "",
                    Pair("a/b", "a/c") to "../c",
                    Pair("a/b", "a") to "..",
                    Pair("a", "a/b") to "b",
                    Pair("a/.", "a/b") to "b"
            )
            for ((operands, expected) in paths) {
                on("relativizing \"${operands.first}\" to \"${operands.second}\"") {
                    val path = operands.first.relativize(operands.second)
                    it("should return \"$expected\"") {
                        path shouldEqual expected
                    }
                }
                val operandsAbsolute = Pair("/${operands.first}",
                        "/${operands.second}")
                on("relativizing \"${operandsAbsolute.first}\" to \"${operandsAbsolute.second}\"") {
                    val path = operandsAbsolute.first.relativize(
                            operandsAbsolute.second)
                    it("should return \"$expected\"") {
                        path shouldEqual expected
                    }
                }
            }
        }
        given("a unix path") {
            val paths = listOf(
                    "a/b" to "a/b",
                    "a/b/." to "a/b",
                    "a/b/.." to "a",
                    "a/../../.." to "../..",
                    "/a/../../.." to "/"
            )
            for ((operand, expected) in paths) {
                on("normalizing \"$operand\"") {
                    val path = operand.normalize()
                    it("should return \"$expected\"") {
                        path shouldEqual expected
                    }
                }
            }
        }
    }
})