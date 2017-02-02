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
package org.tobi29.scapes.engine.utils.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.io.tag.*
import java.util.*

private fun createTagStructure(): TagStructure {
    val random = Random(0)
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

object TagStructureTests : Spek({
    describe("a tag structure") {
        val tagStructureComplex by memoized { createTagStructure() }
        given("any tag structure") {
            on("copy") {
                val tagStructure = tagStructureComplex
                val copy = tagStructure.copy()
                it("should reproduce the tag structure") {
                    copy shouldEqual tagStructure
                }
            }
        }
        on("remap") {
            val tagStructure = structure {
                setString("Replace", "Value")
                setString("Keep", "Value")
                setStructure("Get") {
                    setString("Check", "Value")
                }
            }
            val testStructure = structure {
                setStructure("Add")
                setStructure("Replace")
                setString("Keep", "Value")
                setStructure("Get") {
                    setString("Check", "Value")
                }
            }
            tagStructure.structure("Add")
            tagStructure.structure("Replace")
            tagStructure.structure("Get")
            it("should result in equal structure") {
                tagStructure shouldEqual testStructure
            }
        }
        on("inserting an array and retrieving it as a list") {
            val tagStructure = TagStructure()
            tagStructure.setByteArray("Array", 0, 1, 2, 3, 4)
            it("should return an equal list") {
                tagStructure.getList("Array") shouldEqual listOf<Byte>(0, 1, 2,
                        3, 4)
            }
        }
        on("inserting a list and retrieving it as an array") {
            val tagStructure = TagStructure()
            tagStructure.setList("List", listOf<Byte>(0, 1, 2, 3, 4))
            it("should return an equal array") {
                tagStructure.getByteArray("List") shouldEqual byteArrayOf(0, 1,
                        2, 3, 4)
            }
        }
        on("inserting an array into one tag structure and an equal list into another") {
            val tagStructure1 = TagStructure()
            tagStructure1.setByteArray("Array", 0, 1, 2, 3, 4)
            tagStructure1.setList("List", listOf<Byte>(0, 1, 2, 3, 4))
            val tagStructure2 = TagStructure()
            tagStructure2.setByteArray("List", 0, 1, 2, 3, 4)
            tagStructure2.setList("Array", listOf<Byte>(0, 1, 2, 3, 4))
            it("should make equal tag structures") {
                tagStructure2 shouldEqual tagStructure1
            }
        }
    }
})
