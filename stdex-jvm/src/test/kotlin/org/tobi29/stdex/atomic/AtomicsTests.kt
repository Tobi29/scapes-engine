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
import org.spekframework.spek2.style.specification.describe
import org.tobi29.assertions.shouldEqual

object AtomicsTests : Spek({
    describe("an atomic reference array") {
        describe("constructing a new atomic") {
            val atomic = AtomicArray(16) { it.toString() }
            it("should contain the correct values") {
                for (i in 0 until atomic.length())
                    atomic[i] shouldEqual i.toString()
            }
        }
    }
    describe("an atomic boolean") {
        describe("constructing a new atomic") {
            val atomic = AtomicBoolean(false)
            it("should contain the correct value") {
                atomic.get() shouldEqual false
            }
        }
        describe("setting the value it should get updated") {
            val atomic = AtomicBoolean(false)
            atomic.set(true)
            it("should contain the correct value") {
                atomic.get() shouldEqual true
            }
        }
        describe("getting and setting the value it should get updated") {
            val atomic = AtomicBoolean(false)
            it("should return and contain the correct value") {
                atomic.getAndSet(true) shouldEqual false
                atomic.get() shouldEqual true
            }
        }
    }
    describe("an atomic int") {
        describe("constructing a new atomic") {
            val atomic = AtomicInt(0)
            it("should contain the correct value") {
                atomic.get() shouldEqual 0
            }
        }
        describe("setting the value it should get updated") {
            val atomic = AtomicInt(0)
            atomic.set(42)
            it("should contain the correct value") {
                atomic.get() shouldEqual 42
            }
        }
        describe("getting and setting the value it should get updated") {
            val atomic = AtomicInt(0)
            it("should return and contain the correct value") {
                atomic.getAndSet(42) shouldEqual 0
                atomic.get() shouldEqual 42
            }
        }
        describe("incrementing the value it should get updated") {
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
        describe("constructing a new atomic") {
            val atomic = AtomicDouble(0.0)
            it("should contain the correct value") {
                atomic.get() shouldEqual 0.0
            }
        }
        describe("setting the value it should get updated") {
            val atomic = AtomicDouble(0.0)
            atomic.set(42.0)
            it("should contain the correct value") {
                atomic.get() shouldEqual 42.0
            }
        }
        describe("getting and setting the value it should get updated") {
            val atomic = AtomicDouble(0.0)
            it("should return and contain the correct value") {
                atomic.getAndSet(42.0) shouldEqual 0.0
                atomic.get() shouldEqual 42.0
            }
        }
        describe("incrementing the value it should get updated") {
            val atomic = AtomicDouble(0.0)
            atomic.addAndGet(1.0)
            it("should return and contain the correct value") {
                atomic.get() shouldEqual 1.0
            }
        }
    }
})
