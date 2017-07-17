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

package org.tobi29.scapes.engine.utils.io.tag.json.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.test.assertions.shouldThrow
import org.tobi29.scapes.engine.utils.io.ByteBufferStream
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.io.classpath.ClasspathPath
import org.tobi29.scapes.engine.utils.io.tag.json.readJSON
import org.tobi29.scapes.engine.utils.io.tag.json.writeJSON
import org.tobi29.scapes.engine.utils.tag.TagList
import org.tobi29.scapes.engine.utils.tag.TagMap
import org.tobi29.scapes.engine.utils.tag.toTag
import java.util.*

private fun createTagMap(): TagMap {
    val random = Random()
    return TagMap {
        // All primitive tags
        this["Unit"] = Unit.toTag()
        this["True"] = true.toTag()
        this["False"] = false.toTag()
        this["BytePos"] = 42.toByte().toTag()
        this["ByteNeg"] = (-42).toByte().toTag()
        this["ShortPos"] = 12345.toShort().toTag()
        this["ShortNeg"] = 12345.toShort().toTag()
        this["IntPos"] = 12345678.toTag()
        this["IntNeg"] = (-12345678).toTag()
        this["LongPos"] = 123456789069L.toTag()
        this["LongNeg"] = (-123456789069L).toTag()
        this["Float"] = 0.25f.toTag()
        this["Double"] = 0.25.toTag()
        // this["NaN"] = Double.NaN.toTag()
        // this["InfPos"] = Double.POSITIVE_INFINITY.toTag()
        // this["InfNeg"] = Double.NEGATIVE_INFINITY.toTag()
        // TODO: Test BigInteger and BigDecimal
        val array = ByteArray(1024)
        random.nextBytes(array)
        this["Byte[]"] = array.toTag()
        this["String"] = "◊Blah \u000c\t\n\r\u0000 blah◊".toTag()
        // Filled structure and list
        this["Map"] = TagMap {
            for (i in 0..255) {
                this["Entry#$i"] = i.toByte().toTag()
            }
        }
        this["List"] = TagList {
            add(TagMap { this["Entry"] = 0.toByte().toTag() })
            add(TagMap())
            add(TagList {
                add("Entry#1".toTag())
                add("Entry#2".toTag())
            })
            add(TagList())
            add(Unit.toTag())
            add(0.toByte().toTag())
            add("String".toTag())
        }
        this["ListEndMap"] = TagList {
            add(TagMap { this["Entry#1"] = 1.toByte().toTag() })
            add(TagMap { this["Entry#2"] = 2.toByte().toTag() })
        }
        this["ListEndList"] = TagList {
            add(TagList {
                add("Entry#1".toTag())
                add("Entry#2".toTag())
            })
            add(TagList {
                add("Entry#1".toTag())
                add("Entry#2".toTag())
            })
        }
        this["ListEndEmptyList"] = TagList {
            add(TagList())
            add(TagList())
        }
        // Empty structure and list
        this["EmptyMap"] = TagMap()
        this["EmptyList"] = TagList()
    }
}

private fun checkWriteAndRead(map: TagMap,
                              pretty: Boolean): TagMap {
    val channel = ByteBufferStream()
    map.writeJSON(channel, pretty)
    channel.buffer().flip()
    return readJSON(ByteBufferStream(channel.buffer()))
}

object TagStructureJSONTests : Spek({
    describe("serialization for tag map") {
        given("any tag map") {
            val tagMapComplex by memoized { createTagMap() }
            on("writing and reading, pretty") {
                val tagMap = tagMapComplex
                val read = checkWriteAndRead(tagMap, true)
                it("should return an equal tag map") {
                    read shouldEqual tagMap
                }
            }
            on("writing and reading, ugly") {
                val tagMap = tagMapComplex
                val read = checkWriteAndRead(tagMap, false)
                it("should return an equal tag map") {
                    read shouldEqual tagMap
                }
            }
        }
        val invalidTag = TagMap { this["NaN"] = Double.NaN.toTag() }
        given("a tag with an invalid number") {
            on("writing") {
                it("should fail") {
                    shouldThrow<IOException> {
                        val channel = ByteBufferStream()
                        invalidTag.writeJSON(channel)
                    }
                }
            }
        }
    }
    describe("parsing json") {
        given("any valid json input") {
            val sample by memoized {
                ClasspathPath(this::class.java.classLoader, "sample.json")
            }
            on("parsing the input") {
                it("should succeed") {
                    sample.read { readJSON(it) }
                }
            }
        }
        given("overly deep json input") {
            val sample by memoized {
                ClasspathPath(this::class.java.classLoader, "overflow.json")
            }
            on("parsing the input") {
                it("should fail") {
                    shouldThrow<IOException> { sample.read { readJSON(it) } }
                }
            }
        }
    }
})