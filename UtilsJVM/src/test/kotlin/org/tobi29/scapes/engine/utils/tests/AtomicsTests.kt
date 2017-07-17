package org.tobi29.scapes.engine.utils.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.AtomicBoolean
import org.tobi29.scapes.engine.utils.AtomicInteger

object AtomicsTests : Spek({
    given("an atomic boolean") {
        on("constructing a new atomic") {
            val atomic = AtomicBoolean()
            it("should contain the correct value") {
                atomic.get() shouldEqual false
            }
        }
        on("setting the value it should get updated") {
            val atomic = AtomicBoolean()
            atomic.set(true)
            it("should contain the correct value") {
                atomic.get() shouldEqual true
            }
        }
        on("getting and setting the value it should get updated") {
            val atomic = AtomicBoolean()
            it("should return and contain the correct value") {
                atomic.getAndSet(true) shouldEqual false
                atomic.get() shouldEqual true
            }
        }
    }
    given("an atomic int") {
        on("constructing a new atomic") {
            val atomic = AtomicInteger()
            it("should contain the correct value") {
                atomic.get() shouldEqual 0
            }
        }
        on("setting the value it should get updated") {
            val atomic = AtomicInteger()
            atomic.set(42)
            it("should contain the correct value") {
                atomic.get() shouldEqual 42
            }
        }
        on("getting and setting the value it should get updated") {
            val atomic = AtomicInteger()
            it("should return and contain the correct value") {
                atomic.getAndSet(42) shouldEqual 0
                atomic.get() shouldEqual 42
            }
        }
        on("incrementing the value it should get updated") {
            val atomic = AtomicInteger()
            atomic.incrementAndGet()
            atomic.incrementAndGet()
            atomic.decrementAndGet()
            atomic.addAndGet(2)
            it("should return and contain the correct value") {
                atomic.get() shouldEqual 3
            }
        }
    }
})
