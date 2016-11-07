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

import org.junit.Assert
import org.junit.Test
import org.tobi29.scapes.engine.utils.io.tag.*
import java.util.*

class TagStructureTest {
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

    @Test
    fun testCopy() {
        val tagStructure = createTagStructure()
        val copy = tagStructure.copy()
        Assert.assertEquals("Copied structure doesn't match original one",
                tagStructure, copy)
    }

    @Test
    fun testRemap() {
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

        Assert.assertEquals(
                "Resulting structure after remapping test different from expected",
                testStructure, tagStructure)
    }

    @Test
    fun testByteArrayConversion() {
        val tagStructure = TagStructure()
        tagStructure.setByteArray("Array", 0, 1, 2, 3, 4)
        tagStructure.setList("List", listOf<Byte>(0, 1, 2, 3, 4))

        Assert.assertEquals("Lists do not equal", listOf<Byte>(0, 1, 2, 3, 4),
                tagStructure.getList("Array"))
        Assert.assertArrayEquals("Arrays do not equal",
                byteArrayOf(0, 1, 2, 3, 4), tagStructure.getByteArray("List"))
    }

    @Test
    fun testByteArrayEquals() {
        val tagStructure1 = TagStructure()
        tagStructure1.setByteArray("Array", 0, 1, 2, 3, 4)
        tagStructure1.setList("List", listOf<Byte>(0, 1, 2, 3, 4))
        val tagStructure2 = TagStructure()
        tagStructure2.setByteArray("List", 0, 1, 2, 3, 4)
        tagStructure2.setList("Array", listOf<Byte>(0, 1, 2, 3, 4))

        Assert.assertEquals("Structure do not equal", tagStructure1,
                tagStructure2)
    }
}
