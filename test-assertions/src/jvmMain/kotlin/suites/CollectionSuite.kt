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

fun FeatureBody.implyCollectionBehaviour(
    constructor: () -> MutableCollection<String>
) {
    val makeEmpty = { constructor() }
    val makeBasic = {
        makeEmpty().apply {
            add("A")
            add("B")
            add("C")
            add("D")
        }
    }
    val inputsAny by memoized { listOf(makeEmpty, makeBasic) }
    Scenario("adding one element to a collection") {
        val add = "C"
        inputsAny.forEachResult { collection ->
            val initialSize = collection.size
            var modified = false
            Given("the collection $collection") {}
            When("adding \"$add\" to the collection") {
                modified = collection.add(add)
            }
            Then("should contain \"$add\"") {
                collection shouldContain add
            }
            And("should have an expected size") {
                val expectedSize =
                    (if (modified) initialSize + 1 else initialSize)..
                            (initialSize + 1)
                expectedSize shouldContain collection.size
            }
        }
    }
    Scenario("adding elements to a collection") {
        val add = listOf("C", "D", "E")
        inputsAny.forEachResult { collection ->
            val initialSize = collection.size
            var modified = false
            Given("the collection $collection") {}
            When("adding \"$add\" to the collection") {
                modified = collection.addAll(add)
            }
            Then("should contain \"$add\"") {
                collection shouldContainAll add
            }
            And("should have an expected size") {
                val expectedSize =
                    (if (modified) initialSize + 1 else initialSize)..
                            (initialSize + add.size)
                expectedSize shouldContain collection.size
            }
        }
    }
    Scenario("removing one element from a collection") {
        val remove = "C"
        inputsAny.forEachResult { collection ->
            val initialSize = collection.size
            var modified = false
            Given("the collection $collection") {}
            When("removing \"$remove\" from the collection") {
                modified = collection.remove(remove)
            }
            Then("should not contain \"$remove\"") {
                collection shouldNotContain remove
            }
            And("should have an expected size") {
                val expectedSize =
                    (initialSize - 1)..
                            (if (modified) initialSize - 1 else initialSize)
                expectedSize shouldContain collection.size
            }
        }
    }
    Scenario("removing elements from a collection") {
        val remove = listOf("C", "D", "E")
        inputsAny.forEachResult { collection ->
            val initialSize = collection.size
            var modified = false
            Given("the collection $collection") {}
            When("removing \"$remove\" from the collection") {
                modified = collection.removeAll(remove)
            }
            Then("should not contain \"$remove\"") {
                collection shouldNotContainAny remove
            }
            And("should have an expected size") {
                val expectedSize =
                    (initialSize - remove.size)..
                            (if (modified) initialSize - 1 else initialSize)
                expectedSize shouldContain collection.size
            }
        }
    }
}
