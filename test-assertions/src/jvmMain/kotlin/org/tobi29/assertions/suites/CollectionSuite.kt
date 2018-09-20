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

fun Suite.implyCollectionBehaviour(
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
    val inputsAny = listOf(makeEmpty, makeBasic)
    inputsAny.forEachResult { collection ->
        val add = "C"
        describe("after adding \"$add\" to $collection") {
            val initialSize = collection.size
            val modified = collection.add(add)
            val expectedSize =
                (if (modified) initialSize + 1 else initialSize)..
                        (initialSize + 1)
            it("should contain \"$add\"") {
                collection shouldContain add
            }
            it("should have a size in range $expectedSize") {
                expectedSize shouldContain collection.size
            }
        }
    }
    inputsAny.forEachResult { collection ->
        val add = listOf("C", "D", "E")
        describe("after adding \"$add\" to $collection") {
            val initialSize = collection.size
            val modified = collection.addAll(add)
            val expectedSize =
                (if (modified) initialSize + 1 else initialSize)..
                        (initialSize + add.size)
            it("should contain \"$add\"") {
                collection shouldContainAll add
            }
            it("should have a size in range $expectedSize") {
                expectedSize shouldContain collection.size
            }
        }
    }
    inputsAny.forEachResult { collection ->
        val remove = "C"
        describe("after removing \"$remove\" from $collection") {
            val initialSize = collection.size
            val modified = collection.remove(remove)
            val expectedSize =
                (initialSize - 1)..
                        (if (modified) initialSize - 1 else initialSize)
            it("should contain \"$remove\"") {
                collection shouldNotContain remove
            }
            it("should have a size in range $expectedSize") {
                expectedSize shouldContain collection.size
            }
        }
    }
    inputsAny.forEachResult { collection ->
        val remove = listOf("C", "D", "E")
        describe("after removing \"$remove\" from $collection") {
            val initialSize = collection.size
            val modified = collection.removeAll(remove)
            val expectedSize =
                (initialSize - remove.size)..
                        (if (modified) initialSize - 1 else initialSize)
            it("should contain \"$remove\"") {
                collection shouldNotContainAll remove
            }
            it("should have a size in range $expectedSize") {
                expectedSize shouldContain collection.size
            }
        }
    }
}
