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

package org.tobi29.stdex.atomic

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import org.tobi29.assertions.shouldEqual

object AtomicsTests : Spek({
    Feature("an atomic reference array") {
        val atomic by memoized { AtomicArray(16) { it.toString() } }
        Scenario("constructing a new atomic") {
            Then("should contain the correct values") {
                for (i in 0 until atomic.length())
                    atomic[i] shouldEqual i.toString()
            }
        }
    }
    Feature("an atomic boolean") {
        val atomic by memoized { AtomicBoolean(false) }
        Scenario("constructing a new atomic") {
            Then("should contain the correct value") {
                atomic.get() shouldEqual false
            }
        }
        Scenario("setting the value it should get updated") {
            When("setting it to \"true\"") {
                atomic.set(true)
            }
            Then("should contain the correct value") {
                atomic.get() shouldEqual true
            }
        }
        Scenario("getting and setting the value it should get updated") {
            Then("should return and contain the correct value") {
                atomic.getAndSet(true) shouldEqual false
                atomic.get() shouldEqual true
            }
        }
    }
    Feature("an atomic int") {
        val atomic by memoized { AtomicInt(0) }
        Scenario("constructing a new atomic") {
            Then("should contain the correct value") {
                atomic.get() shouldEqual 0
            }
        }
        Scenario("setting the value it should get updated") {
            When("setting it to \"42\"") {
                atomic.set(42)
            }
            Then("should contain the correct value") {
                atomic.get() shouldEqual 42
            }
        }
        Scenario("getting and setting the value it should get updated") {
            Then("should return and contain the correct value") {
                atomic.getAndSet(42) shouldEqual 0
                atomic.get() shouldEqual 42
            }
        }
        Scenario("incrementing the value it should get updated") {
            When("doing various increment and decrement operations") {
                atomic.incrementAndGet()
                atomic.incrementAndGet()
                atomic.decrementAndGet()
                atomic.addAndGet(2)
            }
            Then("should return and contain the correct value") {
                atomic.get() shouldEqual 3
            }
        }
    }
    Feature("an atomic double") {
        val atomic by memoized { AtomicDouble(0.0) }
        Scenario("constructing a new atomic") {
            Then("should contain the correct value") {
                atomic.get() shouldEqual 0.0
            }
        }
        Scenario("setting the value it should get updated") {
            When("setting it to \"42.0\"") {
                atomic.set(42.0)
            }
            Then("should contain the correct value") {
                atomic.get() shouldEqual 42.0
            }
        }
        Scenario("getting and setting the value it should get updated") {
            Then("should return and contain the correct value") {
                atomic.getAndSet(42.0) shouldEqual 0.0
                atomic.get() shouldEqual 42.0
            }
        }
        Scenario("incrementing the value it should get updated") {
            When("adding \"1.0\" to it") {
                atomic.addAndGet(1.0)
            }
            Then("should return and contain the correct value") {
                atomic.get() shouldEqual 1.0
            }
        }
    }
})
