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

package org.tobi29.assertions.suites

import org.spekframework.spek2.style.gherkin.FeatureBody
import org.tobi29.assertions.*

fun FeatureBody.implyMapBehaviour(
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
    val inputsAny by memoized { listOf(makeEmpty, makeBasic) }
    Scenario("adding one entry to a map") {
        val add = "C" to "3"
        inputsAny.forEachResult { map ->
            val initialSize = map.size
            var modified = false
            Given("the map $map") {}
            When("adding \"$add\" to the map") {
                modified = map.put(add.first, add.second) == null
            }
            Then("should contain key \"${add.first}\"") {
                map shouldContainKey add.first
            }
            And("should have an expected size") {
                val expectedSize =
                    (if (modified) initialSize + 1 else initialSize)..
                            (initialSize + 1)
                expectedSize shouldContain map.size
            }
        }
    }
    Scenario("adding entries to a map") {
        inputsAny.forEachResult { map ->
            val add = listOf("C" to "3", "D" to "4", "E" to "5")
            val initialSize = map.size
            Given("the map $map") {}
            When("adding \"$add\" to the map") {
                map.putAll(add)
            }
            Then("should contain keys \"${add.map { it.first }}\"") {
                map shouldContainKeyAll add.map { it.first }
            }
            And("should have an expected size") {
                val expectedSize =
                    (initialSize)..
                            (initialSize + add.size)
                expectedSize shouldContain map.size
            }
        }
    }
    Scenario("removing one entry from a map") {
        inputsAny.forEachResult { map ->
            val remove = "C"
            val initialSize = map.size
            var modified = false
            Given("the map $map") {}
            When("removing \"$remove\" from the map") {
                modified = map.remove(remove) != null
            }
            Then("should contain key \"$remove\"") {
                map shouldNotContainKey remove
            }
            And("should have an expected size") {
                val expectedSize =
                    (initialSize - 1)..
                            (if (modified) initialSize - 1 else initialSize)
                expectedSize shouldContain map.size
            }
        }
    }
}
