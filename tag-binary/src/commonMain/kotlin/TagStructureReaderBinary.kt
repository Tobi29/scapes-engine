/*
 * Copyright 2012-2019 Tobi29
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

import org.tobi29.arrays.sliceOver
import org.tobi29.io.IOException
import org.tobi29.io.LimitedBufferStream
import org.tobi29.io.MemoryStream
import org.tobi29.io.ReadableByteStream
import org.tobi29.io.compression.deflate.inflate
import org.tobi29.io.tag.Tag
import org.tobi29.io.tag.TagList
import org.tobi29.io.tag.TagMap
import org.tobi29.io.tag.toTag

class TagStructureReaderBinary(
    stream: ReadableByteStream,
    private var allocationLimit: Int,
    compressionStream: MemoryStream
) {
    private var dictionary: KeyDictionary? = null
    private val structureBuffer: ReadableByteStream

    init {
        val magic = ByteArray(HEADER_MAGIC.size)
        stream.get(magic.sliceOver())
        if (!(magic contentEquals HEADER_MAGIC)) {
            throw IOException(
                "Not in tag format! (Magic-Header: ${magic.joinToString()})"
            )
        }
        val version = stream.get()
        if (version > HEADER_VERSION) {
            throw IOException(
                "Unsupported version or not in tag format! (Version: $version)"
            )
        }
        val compression = stream.get()
        structureBuffer = if (compression >= 0) {
            val len = stream.getInt()
            compressionStream.reset()
            inflate(LimitedBufferStream(stream, len), compressionStream)
            compressionStream.flip()
            compressionStream
        } else {
            stream
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
                readKey(
                    structureBuffer, dictionary,
                    allocationLimit
                )
            )
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
                    map[key] = Unit.toTag()
                }
                ID_TAG_BOOLEAN -> {
                    allocate(1)
                    map[key] = (structureBuffer.get() != 0.toByte()).toTag()
                }
                ID_TAG_BYTE -> {
                    allocate(1)
                    map[key] = structureBuffer.get().toTag()
                }
                ID_TAG_INT_16 -> {
                    allocate(2)
                    map[key] = structureBuffer.getShort().toTag()
                }
                ID_TAG_INT_32 -> {
                    allocate(4)
                    map[key] = structureBuffer.getInt().toTag()
                }
                ID_TAG_INT_64 -> {
                    allocate(8)
                    map[key] = structureBuffer.getLong().toTag()
                }
                ID_TAG_FLOAT_32 -> {
                    allocate(4)
                    map[key] = structureBuffer.getFloat().toTag()
                }
                ID_TAG_FLOAT_64 -> {
                    allocate(8)
                    map[key] = structureBuffer.getDouble().toTag()
                }
                ID_TAG_BYTE_ARRAY -> {
                    map[key] = allocate(
                        structureBuffer.getByteArrayLong(
                            allocationLimit
                        )
                    ).toTag()
                }
                ID_TAG_STRING -> {
                    map[key] = allocate(
                        structureBuffer.getString(allocationLimit)
                    ).toTag()
                }
                ID_TAG_STRING_REF -> {
                    map[key] = allocate(
                        structureBuffer.get().let { id ->
                            dictionary?.getString(id)
                                    ?: throw IOException("Invalid reference id: $id")
                        }
                    ).toTag()
                }
                else -> throw IOException(
                    "Not in tag format! (Invalid component-id: $componentID)"
                )
            }
        }
    }

    fun readListElement(list: MutableList<Tag>): Boolean {
        val componentID = structureBuffer.get()
        when (componentID) {
            ID_STRUCTURE_BEGIN -> {
                allocate(16) // Those are heavy, do not want too many
                var terminate = false
                list.add(TagMap {
                    terminate = readMap(this)
                })
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
                list.add(Unit.toTag())
            }
            ID_TAG_BOOLEAN -> {
                allocate(1)
                list.add((structureBuffer.get() != 0.toByte()).toTag())
            }
            ID_TAG_BYTE -> {
                allocate(1)
                list.add(structureBuffer.get().toTag())
            }
            ID_TAG_INT_16 -> {
                allocate(2)
                list.add(structureBuffer.getShort().toTag())
            }
            ID_TAG_INT_32 -> {
                allocate(4)
                list.add(structureBuffer.getInt().toTag())
            }
            ID_TAG_INT_64 -> {
                allocate(8)
                list.add(structureBuffer.getLong().toTag())
            }
            ID_TAG_FLOAT_32 -> {
                allocate(4)
                list.add(structureBuffer.getFloat().toTag())
            }
            ID_TAG_FLOAT_64 -> {
                allocate(8)
                list.add(structureBuffer.getDouble().toTag())
            }
            ID_TAG_BYTE_ARRAY -> {
                list.add(
                    allocate(
                        structureBuffer.getByteArrayLong(allocationLimit)
                    ).toTag()
                )
            }
            ID_TAG_STRING -> {
                list.add(
                    allocate(
                        structureBuffer.getString(allocationLimit)
                    ).toTag()
                )
            }
            ID_TAG_STRING_REF -> {
                list.add(
                    allocate(
                        structureBuffer.get().let { id ->
                            dictionary?.getString(id)
                                    ?: throw IOException("Invalid reference id: $id")
                        }
                    ).toTag()
                )
            }
            ID_LIST_TERMINATE -> return true
            else -> {
                throw IOException(
                    "Not in tag format! (Invalid component-id: $componentID)"
                )
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
        if (array.size > allocationLimit) {
            throw IOException("No more allocations allowed for reference")
        }
        allocationLimit -= array.size
        return array
    }

    private fun allocate(str: String): String {
        if (allocationLimit == Int.MAX_VALUE) {
            return str
        }
        if (str.length > allocationLimit) {
            throw IOException("No more allocations allowed for reference")
        }
        allocationLimit -= str.length
        return str
    }
}
