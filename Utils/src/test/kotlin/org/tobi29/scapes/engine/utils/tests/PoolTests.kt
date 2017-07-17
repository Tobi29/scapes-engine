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
package org.tobi29.scapes.engine.utils.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldContain
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.test.assertions.shouldNotContain
import org.tobi29.scapes.engine.test.assertions.shouldThrow
import org.tobi29.scapes.engine.utils.Pool

object PoolTests : Spek({
    given("a pool with 4 allocated") {
        val pool4Alloc by memoized {
            Pool { StringHolder() }.apply {
                for (i in 0..3) {
                    push()
                }
                reset()
            }
        }
        given("a pool filled with 4 5 6 1 2 3") {
            val pool = pool4Alloc
            pool.push().set("4")
            pool.push().set("5")
            pool.push().set("6")
            pool.push().set("1")
            pool.push().set("2")
            pool.push().set("3")
            it("equals 456123 when mapped into a string") {
                pool.asSequence().map { it.str }.reduce(
                        String::plus) shouldEqual "456123"
            }
            it("equals 123456 when mapped and sorted into a string") {
                pool.asSequence().map { it.str }.sorted().reduce(
                        String::plus) shouldEqual "123456"
            }
        }
        given("a pool filled with 1 2 3") {
            val pool123 by memoized {
                pool4Alloc.apply {
                    push().set("1")
                    push().set("2")
                    push().set("3")
                }
            }
            on("popping elements out and adding one back") {
                val pool = pool123
                it("should return the correct elements and throw when empty") {
                    pool.pop()?.str shouldEqual "2"
                    pool.pop()?.str shouldEqual "1"
                    pool.push().set("4")
                    pool.pop()?.str shouldEqual "1"
                    pool.pop()?.str shouldEqual null
                    pool.isEmpty() shouldEqual true
                    shouldThrow<NoSuchElementException> {
                        pool.pop()
                    }
                }
            }
            on("checking if an element is contained, removing it and checking again") {
                val pool = pool123
                it("should first contain 2 and then not") {
                    pool shouldContain StringHolder("2")
                    pool.remove(StringHolder("2"))
                    pool shouldNotContain StringHolder("2")
                }
            }
        }
    }
})

private data class StringHolder(var str: String = "") {
    fun set(str: String) {
        this.str = str
    }
}
