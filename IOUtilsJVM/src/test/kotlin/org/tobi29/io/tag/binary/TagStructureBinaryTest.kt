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

package org.tobi29.io.tag.binary

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.assertions.shouldEqual
import org.tobi29.io.MemoryViewStreamDefault
import org.tobi29.io.tag.TagList
import org.tobi29.io.tag.TagMap
import org.tobi29.io.tag.toTag
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
        this["NaN"] = Double.NaN.toTag()
        this["InfPos"] = Double.POSITIVE_INFINITY.toTag()
        this["InfNeg"] = Double.NEGATIVE_INFINITY.toTag()
        // TODO: Test BigInteger and BigDecimal
        val array = ByteArray(1024)
        random.nextBytes(array)
        this["Byte[]"] = array.toTag()
        this["String"] = "◊Blah blah blah◊".toTag()
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
        this["EmptyMapInList"] = TagList {
            add(TagMap())
        }
        this["EmptyMapInListInList"] = TagList {
            add(TagList {
                add(TagMap())
            })
        }
        this["EmptyListInMap"] = TagMap { this["List"] = TagList() }
    }
}

private fun checkWriteAndRead(map: TagMap,
                              compression: Byte = -1): TagMap {
    val channel = MemoryViewStreamDefault()
    map.writeBinary(channel, compression)
    channel.flip()
    return readBinary(channel)
}

object TagStructureBinaryTests : Spek({
    describe("serialization for tag structures") {
        given("any tag structure") {
            val tagMapComplex by memoized { createTagMap() }
            on("writing and reading, uncompressed") {
                val tagMap = tagMapComplex
                val read = checkWriteAndRead(tagMap,
                        -1)
                it("should return an equal tag structure") {
                    read shouldEqual tagMap
                }
            }
            on("writing and reading, compressed") {
                val tagMap = tagMapComplex
                val read = checkWriteAndRead(tagMap, 1)
                it("should return an equal tag structure") {
                    read shouldEqual tagMap
                }
            }
        }
        given("a tag structure with more than 255 different keys") {
            val tagMapManyKeys by memoized {
                TagMap {
                    repeat(512) { this["Entry#$it"] = it.toTag() }
                }
            }
            on("writing and reading, uncompressed") {
                val tagMap = tagMapManyKeys
                val read = checkWriteAndRead(tagMap,
                        -1)
                it("should return an equal tag structure") {
                    read shouldEqual tagMap
                }
            }
        }
    }
})
