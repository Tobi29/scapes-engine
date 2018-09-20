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

package org.tobi29.assertions.suites

import org.spekframework.spek2.style.specification.Suite
import org.tobi29.assertions.*

fun Suite.implyMapBehaviour(
    constructor: () -> MutableMap<String, String>
) {
    val makeEmpty = { constructor() }
    val makeBasic = {
        makeEmpty().apply {
            this["A"] = "1"
            this["B"] = "2"
            this["C"] = "3"
            this["D"] = "4"
        }
    }
    val inputsAny = listOf(makeEmpty, makeBasic)
    inputsAny.forEachResult { map ->
        val add = "C" to "3"
        describe("after adding \"$add\" to $map") {
            val initialSize = map.size
            val modified = map.put(add.first, add.second) == null
            val expectedSize =
                (if (modified) initialSize + 1 else initialSize)..
                        (initialSize + 1)
            it("should contain key \"${add.first}\"") {
                map shouldContainKey add.first
            }
            it("should have a size in range $expectedSize") {
                expectedSize shouldContain map.size
            }
        }
    }
    inputsAny.forEachResult { map ->
        val add = listOf("C" to "3", "D" to "4", "E" to "5")
        describe("after adding \"$add\" to $map") {
            val initialSize = map.size
            map.putAll(add)
            val expectedSize =
                (initialSize)..
                        (initialSize + add.size)
            it("should contain keys \"${add.map { it.first }}\"") {
                map shouldContainKeyAll add.map { it.first }
            }
            it("should have a size in range $expectedSize") {
                expectedSize shouldContain map.size
            }
        }
    }
    inputsAny.forEachResult { map ->
        val remove = "C"
        describe("after removing \"$remove\" from $map") {
            val initialSize = map.size
            val modified = map.remove(remove) != null
            val expectedSize =
                (initialSize - 1)..
                        (if (modified) initialSize - 1 else initialSize)
            it("should contain key \"$remove\"") {
                map shouldNotContainKey remove
            }
            it("should have a size in range $expectedSize") {
                expectedSize shouldContain map.size
            }
        }
    }
}
