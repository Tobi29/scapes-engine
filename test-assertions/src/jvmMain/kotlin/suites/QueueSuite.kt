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
import org.tobi29.assertions.shouldContainAll
import org.tobi29.stdex.Queue

fun FeatureBody.implyQueueBehaviour(
    constructor: () -> Queue<String>
) {
    implyCollectionBehaviour(constructor)
    Scenario("filling and draining a queue") {
        val queue = constructor()
        val elements = listOf("A", "D", "B", "C")
        Given("an empty queue") {}
        When("adding \"$elements\" to the queue") {
            elements.forEach { queue.add(it) }
        }
        Then("should contain \"$elements\"") {
            queue shouldContainAll elements
        }
        val drained = ArrayList<String>()
        When("draining the queue") {
            while (true) {
                drained.add(queue.poll() ?: break)
            }
        }
        And("should return all elements") {
            drained shouldContainAll elements
        }
    }
}
