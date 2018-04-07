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

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.assertions.shouldEqual

object AtomicsTests : Spek({
    describe("an atomic boolean") {
        on("constructing a new atomic") {
            val atomic = AtomicBoolean(false)
            it("should contain the correct value") {
                atomic.get() shouldEqual false
            }
        }
        on("setting the value it should get updated") {
            val atomic = AtomicBoolean(false)
            atomic.set(true)
            it("should contain the correct value") {
                atomic.get() shouldEqual true
            }
        }
        on("getting and setting the value it should get updated") {
            val atomic = AtomicBoolean(false)
            it("should return and contain the correct value") {
                atomic.getAndSet(true) shouldEqual false
                atomic.get() shouldEqual true
            }
        }
    }
    describe("an atomic int") {
        on("constructing a new atomic") {
            val atomic = AtomicInt(0)
            it("should contain the correct value") {
                atomic.get() shouldEqual 0
            }
        }
        on("setting the value it should get updated") {
            val atomic = AtomicInt(0)
            atomic.set(42)
            it("should contain the correct value") {
                atomic.get() shouldEqual 42
            }
        }
        on("getting and setting the value it should get updated") {
            val atomic = AtomicInt(0)
            it("should return and contain the correct value") {
                atomic.getAndSet(42) shouldEqual 0
                atomic.get() shouldEqual 42
            }
        }
        on("incrementing the value it should get updated") {
            val atomic = AtomicInt(0)
            atomic.incrementAndGet()
            atomic.incrementAndGet()
            atomic.decrementAndGet()
            atomic.addAndGet(2)
            it("should return and contain the correct value") {
                atomic.get() shouldEqual 3
            }
        }
    }
    describe("an atomic double") {
        on("constructing a new atomic") {
            val atomic = AtomicDouble(0.0)
            it("should contain the correct value") {
                atomic.get() shouldEqual 0.0
            }
        }
        on("setting the value it should get updated") {
            val atomic = AtomicDouble(0.0)
            atomic.set(42.0)
            it("should contain the correct value") {
                atomic.get() shouldEqual 42.0
            }
        }
        on("getting and setting the value it should get updated") {
            val atomic = AtomicDouble(0.0)
            it("should return and contain the correct value") {
                atomic.getAndSet(42.0) shouldEqual 0.0
                atomic.get() shouldEqual 42.0
            }
        }
        on("incrementing the value it should get updated") {
            val atomic = AtomicDouble(0.0)
            atomic.addAndGet(1.0)
            it("should return and contain the correct value") {
                atomic.get() shouldEqual 1.0
            }
        }
    }
})
