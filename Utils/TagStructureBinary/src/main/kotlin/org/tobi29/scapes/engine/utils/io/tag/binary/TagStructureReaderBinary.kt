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

package org.tobi29.scapes.engine.utils.io.tag.binary

import org.tobi29.scapes.engine.utils.io.ByteBufferStream
import org.tobi29.scapes.engine.utils.io.CompressionUtil
import org.tobi29.scapes.engine.utils.io.LimitedBufferStream
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.io.tag.TagStructure
import java.io.IOException
import java.util.*

class TagStructureReaderBinary(stream: ReadableByteStream,
                               compressionStream: ByteBufferStream,
                               private var allocationLimit: Int) : TagStructureBinary() {
    private var dictionary: TagStructureBinary.KeyDictionary? = null
    private val structureBuffer: ReadableByteStream

    init {
        val magic = ByteArray(TagStructureBinary.HEADER_MAGIC.size)
        stream[magic]
        if (!Arrays.equals(magic, magic)) {
            throw IOException("Not in tag format! (Magic-Header: " +
                    Arrays.toString(magic) + ')')
        }
        val version = stream.get()
        if (version > TagStructureBinary.HEADER_VERSION) {
            throw IOException(
                    "Unsupported version or not in tag format! (Version: " +
                            version + ')')
        }
        val compression = stream.get()
        if (compression >= 0) {
            val len = stream.int
            compressionStream.buffer().clear()
            CompressionUtil.decompress(LimitedBufferStream(stream, len),
                    compressionStream)
            compressionStream.buffer().flip()
            structureBuffer = ByteBufferStream(compressionStream.buffer())
        } else {
            structureBuffer = stream
        }
        dictionary = TagStructureBinary.KeyDictionary(structureBuffer)
    }

    fun readStructure(tagStructure: TagStructure): Boolean {
        while (true) {
            val componentID = structureBuffer.get()
            if (componentID == TagStructureBinary.ID_STRUCTURE_TERMINATE) {
                return false
            } else if (componentID == TagStructureBinary.ID_LIST_TERMINATE) {
                return true
            }
            val key = allocate(
                    TagStructureBinary.readKey(structureBuffer, dictionary,
                            allocationLimit))
            when (componentID) {
                TagStructureBinary.ID_STRUCTURE_BEGIN -> {
                    allocate(16) // Those are heavy, do not want too many
                    val childStructure = TagStructure()
                    if (readStructure(childStructure)) {
                        throw IOException("List termination in structure")
                    }
                    tagStructure.setStructure(key, childStructure)
                }
                TagStructureBinary.ID_STRUCTURE_EMPTY -> {
                    allocate(16) // Those are heavy, do not want too many
                    tagStructure.setStructure(key, TagStructure())
                }
                TagStructureBinary.ID_LIST_BEGIN -> {
                    allocate(4) // Structure + list size
                    val list = ArrayList<TagStructure>()
                    while (!readListElement(list)) {
                    }
                    tagStructure.setList(key, list)
                }
                TagStructureBinary.ID_LIST_EMPTY -> {
                    allocate(4) // Those are fairly light on their own
                    tagStructure.setList(key, emptyList())
                }
                TagStructureBinary.ID_TAG_BOOLEAN -> {
                    allocate(1)
                    tagStructure.setBoolean(key,
                            structureBuffer.get() != 0.toByte())
                }
                TagStructureBinary.ID_TAG_BYTE -> {
                    allocate(1)
                    tagStructure.setNumber(key, structureBuffer.get())
                }
                TagStructureBinary.ID_TAG_BYTE_ARRAY -> {
                    tagStructure.setByteArray(key, *allocate(
                            structureBuffer.getByteArrayLong(allocationLimit)))
                }
                TagStructureBinary.ID_TAG_INT_16 -> {
                    allocate(2)
                    tagStructure.setNumber(key, structureBuffer.short)
                }
                TagStructureBinary.ID_TAG_INT_32 -> {
                    allocate(4)
                    tagStructure.setNumber(key, structureBuffer.int)
                }
                TagStructureBinary.ID_TAG_INT_64 -> {
                    allocate(8)
                    tagStructure.setNumber(key, structureBuffer.long)
                }
                TagStructureBinary.ID_TAG_FLOAT_32 -> {
                    allocate(4)
                    tagStructure.setNumber(key, structureBuffer.float)
                }
                TagStructureBinary.ID_TAG_FLOAT_64 -> {
                    allocate(8)
                    tagStructure.setNumber(key, structureBuffer.double)
                }
                TagStructureBinary.ID_TAG_STRING -> {
                    tagStructure.setString(key, allocate(
                            structureBuffer.getString(allocationLimit)))
                }
                else -> throw IOException(
                        "Not in tag format! (Invalid component-id: $componentID)")
            }
        }
    }

    fun readListElement(list: MutableList<TagStructure>): Boolean {
        allocate(16) // Those are heavy, do not want too many
        val tagStructure = TagStructure()
        val terminate = readStructure(tagStructure)
        list.add(tagStructure)
        return terminate
    }

    private fun allocate(amount: Int) {
        if (allocationLimit == Int.MAX_VALUE) {
            return
        }
        if (allocationLimit < amount) {
            allocationLimit -= amount
        } else {
            throw IOException("No more allocations allowed")
        }
    }

    private fun allocate(array: ByteArray): ByteArray {
        if (allocationLimit == Int.MAX_VALUE) {
            return array
        }
        allocationLimit -= array.size
        assert(allocationLimit >= 0)
        return array
    }

    private fun allocate(str: String): String {
        if (allocationLimit == Int.MAX_VALUE) {
            return str
        }
        allocationLimit -= str.length
        assert(allocationLimit >= 0)
        return str
    }
}
