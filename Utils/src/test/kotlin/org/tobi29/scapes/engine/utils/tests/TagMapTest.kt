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
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.io.tag.*

object TagMapTests : Spek({
    describe("a tag map") {
        on("remap") {
            val tagStructure = MutableTagMap {
                this["Replace"] = "Value"
                this["Keep"] = "Value"
                this["Get"] = TagMap {
                    this["Check"] = "Value"
                }
            }
            val testStructure = TagMap {
                this["Add"] = TagMap()
                this["Replace"] = TagMap()
                this["Keep"] = "Value"
                this["Get"] = TagMap {
                    this["Check"] = "Value"
                }
            }
            tagStructure.mapMut("Add")
            tagStructure.mapMut("Replace")
            tagStructure.mapMut("Get")
            it("should result in equal map") {
                tagStructure shouldEqual testStructure
            }
        }
        on("inserting an array and retrieving it as a list") {
            val tagStructure = TagMap {
                this["Array"] = byteArrayOf(0, 1, 2, 3, 4)
            }
            it("should return an equal list") {
                tagStructure["Array"]?.toList() shouldEqual tagListOf(0, 1,
                        2, 3, 4)
            }
        }
        on("inserting a list and retrieving it as an array") {
            val tagStructure = TagMap {
                this["List"] = tagListOf(0, 1, 2, 3, 4)
            }
            it("should return an equal array") {
                tagStructure["List"]?.toByteArray() shouldEqual byteArrayOf(0,
                        1, 2, 3, 4)
            }
        }
        on("inserting an array into one tag map and an equal list into another") {
            val tagStructure1 = TagMap {
                this["Array"] = byteArrayOf(0, 1, 2, 3, 4)
                this["List"] = tagListOf(0, 1, 2, 3, 4)
            }
            val tagStructure2 = TagMap {
                this["List"] = byteArrayOf(0, 1, 2, 3, 4)
                this["Array"] = tagListOf(0, 1, 2, 3, 4)
            }
            it("should make equal tag maps") {
                tagStructure2 shouldEqual tagStructure1
            }
        }
        val tagsEqual = listOf(
                Pair(TagMap {
                    this["Data"] = byteArrayOf(1, 2, 3, 4)
                }, TagMap {
                    this["Data"] = tagListOf(1, 2, 3, 4)
                })
        )
        for (tag in tagsEqual) {
            it("should equal") {
                tag.first shouldEqual tag.second
            }
            it("should give the same hash code") {
                tag.first.hashCode() shouldEqual tag.second.hashCode()
            }
        }
    }
})
