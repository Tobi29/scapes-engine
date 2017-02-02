/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.utils.io.tag.binary.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.io.ByteBufferStream
import org.tobi29.scapes.engine.utils.io.tag.*
import org.tobi29.scapes.engine.utils.io.tag.binary.TagStructureBinary
import java.util.*

private fun createTagStructure(): TagStructure {
    val random = Random()
    return structure {
        // All primitive tags
        setUnit("Unit")
        setBoolean("Boolean", random.nextBoolean())
        setByte("Byte", random.nextInt(0x100).toByte())
        setShort("Short", random.nextInt(0x10000).toShort())
        setInt("Integer", random.nextInt())
        setLong("Long", random.nextLong())
        setFloat("Float", random.nextFloat())
        setDouble("Double", random.nextDouble())
        setDouble("NaN", Double.NaN)
        setDouble("PosInf", Double.POSITIVE_INFINITY)
        setDouble("NegInf", Double.NEGATIVE_INFINITY)
        val array = ByteArray(1024)
        random.nextBytes(array)
        setByteArray("Byte[]", *array)
        setString("String", "◊Blah blah blah◊")
        // Filled structure and list
        setStructure("Structure") {
            for (i in 0..255) {
                setByte("Entry#" + i, i.toByte())
            }
        }
        setList("List") {
            add(structure { setByte("Entry", 0) })
            add(structure {})
            add(list {
                add("Entry#1")
                add("Entry#2")
            })
            add(emptyList<Any>())
            add(Unit)
            add(0)
            add("String")
        }
        setList("ListEndStructure") {
            add(structure { setInt("Entry#1", 1) })
            add(structure { setInt("Entry#2", 2) })
        }
        setList("ListEndList") {
            add(list {
                add("Entry#1")
                add("Entry#2")
            })
            add(list {
                add("Entry#1")
                add("Entry#2")
            })
        }
        setList("ListEndEmptyList") {
            add(emptyList<Any>())
            add(emptyList<Any>())
        }
        // Empty structure and list
        structure("EmptyStructure")
        setList("EmptyList")
    }
}

private fun checkWriteAndRead(structure: TagStructure,
                              compression: Byte): TagStructure {
    val channel = ByteBufferStream()
    TagStructureBinary.write(channel, structure, compression)
    channel.buffer().flip()
    return TagStructureBinary.read(ByteBufferStream(channel.buffer()))
}

object TagStructureBinaryTest : Spek({
    describe("serialization for tag structures") {
        given("any tag structure") {
            val tagStructureComplex by memoized { createTagStructure() }
            on("writing and reading, uncompressed") {
                val tagStructure = tagStructureComplex
                val read = checkWriteAndRead(tagStructure, -1)
                it("should return an equal tag structure") {
                    read shouldEqual tagStructure
                }
            }
            on("writing and reading, compressed") {
                val tagStructure = tagStructureComplex
                val read = checkWriteAndRead(tagStructure, 1)
                it("should return an equal tag structure") {
                    read shouldEqual tagStructure
                }
            }
        }
        given("a tag structure with more than 255 different keys") {
            val tagStructureManyKeys by memoized {
                structure {
                    repeat(512) { setInt("Entry#$it", it) }
                }
            }
            on("writing and reading, uncompressed") {
                val tagStructure = tagStructureManyKeys
                val read = checkWriteAndRead(tagStructure, -1)
                it("should return an equal tag structure") {
                    read shouldEqual tagStructure
                }
            }
        }
    }
})
