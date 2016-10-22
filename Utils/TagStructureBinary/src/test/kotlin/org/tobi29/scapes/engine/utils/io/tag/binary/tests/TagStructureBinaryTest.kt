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

import org.junit.Assert
import org.junit.Test
import org.tobi29.scapes.engine.utils.io.ByteBufferStream
import org.tobi29.scapes.engine.utils.io.tag.*
import org.tobi29.scapes.engine.utils.io.tag.binary.TagStructureBinary
import java.io.IOException
import java.util.*

class TagStructureBinaryTest {
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
        tagStructure.setDouble("NaN", Double.NaN)
        tagStructure.setDouble("PosInf", Double.POSITIVE_INFINITY)
        tagStructure.setDouble("NegInf", Double.NEGATIVE_INFINITY)
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
    @Throws(IOException::class)
    fun testUncompressedBinaryFile() {
        val tagStructure = createTagStructure()
        val channel = ByteBufferStream()
        TagStructureBinary.write(channel, tagStructure, (-1).toByte())
        channel.buffer().flip()
        val read = TagStructure()
        TagStructureBinary.read(ByteBufferStream(channel.buffer()), read)
        Assert.assertEquals("Read structure doesn't match written one",
                tagStructure, read)
    }

    @Test
    @Throws(IOException::class)
    fun testCompressedBinaryFile() {
        val tagStructure = createTagStructure()
        val channel = ByteBufferStream()
        TagStructureBinary.write(channel, tagStructure, 1.toByte())
        channel.buffer().flip()
        val read = TagStructure()
        TagStructureBinary.read(ByteBufferStream(channel.buffer()), read)
        Assert.assertEquals("Read structure doesn't match written one",
                tagStructure, read)
    }
}
