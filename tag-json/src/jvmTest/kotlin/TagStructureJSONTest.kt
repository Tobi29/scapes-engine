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

package org.tobi29.io.tag.json

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.assertions.shouldEqual
import org.tobi29.assertions.shouldThrow
import org.tobi29.assertions.suites.createTagMap
import org.tobi29.io.IOException
import org.tobi29.io.MemoryViewStreamDefault
import org.tobi29.io.classpath.ClasspathPath
import org.tobi29.io.tag.TagMap
import org.tobi29.io.tag.toTag

private fun checkWriteAndRead(
    map: TagMap,
    pretty: Boolean
): TagMap {
    val channel = MemoryViewStreamDefault()
    map.writeJSON(channel, pretty)
    channel.flip()
    return readJSON(channel)
}

object TagStructureJSONTests : Spek({
    describe("serialization for tag map") {
        describe("a normal tag structure") {
            val tagMapComplex by memoized {
                createTagMap(
                    floatSpecial = false
                )
            }
            describe("writing and reading, pretty") {
                val tagMap = tagMapComplex
                val read = checkWriteAndRead(
                    tagMap,
                    true
                )
                it("should return an equal tag map") {
                    read shouldEqual tagMap
                }
            }
            describe("writing and reading, ugly") {
                val tagMap = tagMapComplex
                val read = checkWriteAndRead(
                    tagMap,
                    false
                )
                it("should return an equal tag map") {
                    read shouldEqual tagMap
                }
            }
        }
        describe("a tag with an invalid number") {
            val invalidTag = TagMap { this["NaN"] = Double.NaN.toTag() }
            describe("writing") {
                it("should fail") {
                    shouldThrow<IOException> {
                        val channel = MemoryViewStreamDefault()
                        invalidTag.writeJSON(channel)
                    }
                }
            }
        }
    }
    describe("parsing json") {
        describe("normal json input") {
            val sample by memoized {
                ClasspathPath(this::class.java.classLoader, "sample.json")
            }
            describe("parsing the input") {
                it("should succeed") {
                    sample.readNow { readJSON(it) }
                }
            }
        }
        describe("overly deep json input") {
            val sample by memoized {
                ClasspathPath(this::class.java.classLoader, "overflow.json")
            }
            describe("parsing the input") {
                it("should fail") {
                    shouldThrow<IOException> { sample.readNow { readJSON(it) } }
                }
            }
        }
    }
})
