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

package org.tobi29.scapes.engine.utils.io.tag.binary

import org.tobi29.scapes.engine.utils.IOException
import org.tobi29.scapes.engine.utils.assert
import org.tobi29.scapes.engine.utils.equals
import org.tobi29.scapes.engine.utils.io.ByteBufferStream
import org.tobi29.scapes.engine.utils.io.CompressionUtil
import org.tobi29.scapes.engine.utils.io.LimitedBufferStream
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.tag.*

class TagStructureReaderBinary(stream: ReadableByteStream,
                               private var allocationLimit: Int,
                               compressionStream: ByteBufferStream) {
    private var dictionary: KeyDictionary? = null
    private val structureBuffer: ReadableByteStream

    init {
        val magic = ByteArray(HEADER_MAGIC.size)
        stream[magic]
        if (!(magic equals magic)) {
            throw IOException(
                    "Not in tag format! (Magic-Header: ${magic.joinToString()})")
        }
        val version = stream.get()
        if (version > HEADER_VERSION) {
            throw IOException(
                    "Unsupported version or not in tag format! (Version: $version)")
        }
        val compression = stream.get()
        if (compression >= 0) {
            val len = stream.getInt()
            compressionStream.buffer().clear()
            CompressionUtil.decompress(LimitedBufferStream(stream, len),
                    compressionStream)
            compressionStream.buffer().flip()
            structureBuffer = ByteBufferStream(compressionStream.buffer())
        } else {
            structureBuffer = stream
        }
        dictionary = KeyDictionary(structureBuffer)
    }

    fun readMap(map: MutableMap<String, Tag>): Boolean {
        while (true) {
            val componentID = structureBuffer.get()
            if (componentID == ID_STRUCTURE_TERMINATE) {
                return false
            } else if (componentID == ID_LIST_TERMINATE) {
                return true
            }
            val key = allocate(
                    readKey(structureBuffer, dictionary,
                            allocationLimit))
            when (componentID) {
                ID_STRUCTURE_BEGIN -> {
                    allocate(16) // Those are heavy, do not want too many
                    map[key] = TagMap { readMap(this) }
                }
                ID_STRUCTURE_EMPTY -> {
                    allocate(16) // Those are heavy, do not want too many
                    map[key] = TagMap()
                }
                ID_LIST_BEGIN -> {
                    allocate(4) // Structure + list size
                    map[key] = TagList {
                        while (!readListElement(this)) {
                        }
                    }
                }
                ID_LIST_EMPTY -> {
                    allocate(4) // Those are fairly light on their own
                    map[key] = TagList()
                }
                ID_TAG_UNIT -> {
                    allocate(1)
                    map[key] = Unit
                }
                ID_TAG_BOOLEAN -> {
                    allocate(1)
                    map[key] = structureBuffer.get() != 0.toByte()
                }
                ID_TAG_BYTE -> {
                    allocate(1)
                    map[key] = structureBuffer.get()
                }
                ID_TAG_INT_16 -> {
                    allocate(2)
                    map[key] = structureBuffer.getShort()
                }
                ID_TAG_INT_32 -> {
                    allocate(4)
                    map[key] = structureBuffer.getInt()
                }
                ID_TAG_INT_64 -> {
                    allocate(8)
                    map[key] = structureBuffer.getLong()
                }
                ID_TAG_FLOAT_32 -> {
                    allocate(4)
                    map[key] = structureBuffer.getFloat()
                }
                ID_TAG_FLOAT_64 -> {
                    allocate(8)
                    map[key] = structureBuffer.getDouble()
                }
                ID_TAG_BYTE_ARRAY -> {
                    map[key] = allocate(
                            structureBuffer.getByteArrayLong(allocationLimit))
                }
                ID_TAG_STRING -> {
                    map[key] = allocate(
                            structureBuffer.getString(allocationLimit))
                }
                else -> throw IOException(
                        "Not in tag format! (Invalid component-id: $componentID)")
            }
        }
    }

    fun readListElement(list: MutableList<Tag>): Boolean {
        val componentID = structureBuffer.get()
        when (componentID) {
            ID_STRUCTURE_BEGIN -> {
                allocate(16) // Those are heavy, do not want too many
                var terminate = false
                list.add(TagMap { terminate = readMap(this) })
                if (terminate) {
                    return true
                }
            }
            ID_STRUCTURE_EMPTY -> {
                allocate(16) // Those are heavy, do not want too many
                list.add(TagMap())
            }
            ID_LIST_BEGIN -> {
                allocate(4) // Structure + list size
                list.add(TagList {
                    while (!readListElement(this)) {
                    }
                })
            }
            ID_LIST_EMPTY -> {
                allocate(4) // Those are fairly light on their own
                list.add(TagList())
            }
            ID_TAG_UNIT -> {
                allocate(1)
                list.add(Unit)
            }
            ID_TAG_BOOLEAN -> {
                allocate(1)
                list.add(structureBuffer.get() != 0.toByte())
            }
            ID_TAG_BYTE -> {
                allocate(1)
                list.add(structureBuffer.get())
            }
            ID_TAG_INT_16 -> {
                allocate(2)
                list.add(structureBuffer.getShort())
            }
            ID_TAG_INT_32 -> {
                allocate(4)
                list.add(structureBuffer.getInt())
            }
            ID_TAG_INT_64 -> {
                allocate(8)
                list.add(structureBuffer.getLong())
            }
            ID_TAG_FLOAT_32 -> {
                allocate(4)
                list.add(structureBuffer.getFloat())
            }
            ID_TAG_FLOAT_64 -> {
                allocate(8)
                list.add(structureBuffer.getDouble())
            }
            ID_TAG_BYTE_ARRAY -> {
                list.add(allocate(
                        structureBuffer.getByteArrayLong(allocationLimit)))
            }
            ID_TAG_STRING -> {
                list.add(allocate(structureBuffer.getString(allocationLimit)))
            }
            ID_LIST_TERMINATE -> return true
            else -> {
                throw IOException(
                        "Not in tag format! (Invalid component-id: $componentID)")
            }
        }
        return false
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
        assert { allocationLimit >= 0 }
        return array
    }

    private fun allocate(str: String): String {
        if (allocationLimit == Int.MAX_VALUE) {
            return str
        }
        allocationLimit -= str.length
        assert { allocationLimit >= 0 }
        return str
    }
}
