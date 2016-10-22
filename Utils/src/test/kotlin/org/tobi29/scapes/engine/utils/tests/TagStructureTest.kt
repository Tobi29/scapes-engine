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
        val tagStructure = TagStructure()
        // All primitive tags
        tagStructure.setBoolean("Boolean", random.nextBoolean())
        tagStructure.setByte("Byte", random.nextInt(0x100).toByte())
        val array = ByteArray(1024)
        random.nextBytes(array)
        tagStructure.setByteArray("Byte[]", *array)
        tagStructure.setShort("Short", random.nextInt(0x10000).toShort())
        tagStructure.setInt("Integer", random.nextInt())
        tagStructure.setLong("Long", random.nextLong())
        tagStructure.setFloat("Float", random.nextFloat())
        tagStructure.setDouble("Double", random.nextDouble())
        tagStructure.setString("String", "◊Blah blah blah◊")
        // Filled structure and list
        val childStructure = tagStructure.structure("Structure")
        for (i in 0..255) {
            childStructure.setByte("Entry#" + i, i.toByte())
        }
        val childList = ArrayList<TagStructure>()
        for (i in 0..255) {
            val listEntry = TagStructure()
            listEntry.setInt("Entry#" + i, i)
            childList.add(listEntry)
        }
        tagStructure.setList("List", childList)
        // Empty structure and list
        tagStructure.structure("EmptyStructure")
        tagStructure.setList("EmptyList", ArrayList<TagStructure>())
        return tagStructure
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
}
