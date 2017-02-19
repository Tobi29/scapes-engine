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
import org.tobi29.scapes.engine.utils.io.tag.*
import org.tobi29.scapes.engine.utils.io.tag.json.readJSON
import org.tobi29.scapes.engine.utils.io.tag.json.writeJSON
import java.io.IOException
import java.util.*

private fun createTagMap(): TagMap {
    val random = Random()
    return TagMap {
        // All primitive tags
        this["Unit"] = Unit
        this["True"] = true
        this["False"] = false
        this["BytePos"] = 42.toByte()
        this["ByteNeg"] = -42.toByte()
        this["ShortPos"] = 12345.toShort()
        this["ShortNeg"] = 12345.toShort()
        this["IntPos"] = 12345678
        this["IntNeg"] = -12345678
        this["LongPos"] = 123456789069L
        this["LongNeg"] = -123456789069L
        this["Float"] = 0.25f
        this["Double"] = 0.25
        // TODO: Test BigInteger and BigDecimal
        val array = ByteArray(1024)
        random.nextBytes(array)
        this["Byte[]"] = array
        this["String"] = "◊Blah blah blah◊"
        // Filled structure and list
        this["Map"] = TagMap {
            for (i in 0..255) {
                this["Entry#$i"] = i.toByte()
            }
        }
        this["List"] = TagList {
            add(TagMap { this["Entry"] = 0.toByte() })
            add(TagMap())
            add(TagList {
                add("Entry#1")
                add("Entry#2")
            })
            add(TagList())
            add(Unit.toTag())
            add(0.toByte().toTag())
            add("String".toTag())
        }
        this["ListEndMap"] = TagList {
            add(TagMap { this["Entry#1"] = 1.toByte() })
            add(TagMap { this["Entry#2"] = 2.toByte() })
        }
        this["ListEndList"] = TagList {
            add(TagList {
                add("Entry#1")
                add("Entry#2")
            })
            add(TagList {
                add("Entry#1")
                add("Entry#2")
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

private fun checkWriteAndRead(map: TagMap): TagMap {
    val channel = ByteBufferStream()
    map.writeJSON(channel)
    channel.buffer().flip()
    return readJSON(ByteBufferStream(channel.buffer()))
}

object TagStructureJSONTests : Spek({
    describe("serialization for tag map") {
        given("any tag map") {
            val tagMapComplex by memoized { createTagMap() }
            on("writing and reading") {
                val tagMap = tagMapComplex
                val read = checkWriteAndRead(tagMap)
                it("should return an equal tag map") {
                    read shouldEqual tagMap
                }
            }
        }
        val invalidTag = TagMap { this["NaN"] = Double.NaN }
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
})
