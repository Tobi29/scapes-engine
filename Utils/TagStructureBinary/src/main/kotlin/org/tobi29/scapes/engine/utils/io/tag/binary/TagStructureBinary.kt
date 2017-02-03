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

import org.tobi29.scapes.engine.utils.ThreadLocal
import org.tobi29.scapes.engine.utils.io.ByteBufferStream
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.io.WritableByteStream
import org.tobi29.scapes.engine.utils.io.tag.TagStructure
import org.tobi29.scapes.engine.utils.limit
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class TagStructureBinary {
    internal class KeyDictionary {
        val keyAliases: MutableList<String> = ArrayList()
        val keyAliasMap: MutableMap<String, Byte> = ConcurrentHashMap()
        val aliasKeyMap: MutableMap<Byte, String> = ConcurrentHashMap()
        private var currentId: Byte = 0

        constructor()

        constructor(stream: ReadableByteStream) {
            var length = stream.get().toInt()
            if (length < 0) {
                length += 256
            }
            while (length-- > 0) {
                addKeyAlias(stream.string)
            }
        }

        constructor(tagStructure: TagStructure) {
            val keys = ConcurrentHashMap<String, KeyOccurrence>()
            analyze(tagStructure, keys)
            if (keys.size > 255) {
                keys.entries.asSequence().sortedBy { it.value.count }.limit(
                        255).map { it.key }.forEach { addKeyAlias(it) }
            } else {
                keys.entries.asSequence().map { it.key }.forEach {
                    addKeyAlias(it)
                }
            }
        }

        private fun analyze(tagStructure: TagStructure,
                            keys: MutableMap<String, KeyOccurrence>) {
            for ((key, value) in tagStructure.tagEntrySet) {
                val occurrence = keys[key]
                if (occurrence == null) {
                    keys.put(key, KeyOccurrence(key))
                } else {
                    occurrence.count += occurrence.length
                }
                if (value is TagStructure) {
                    analyze(value, keys)
                } else if (value is List<*>) {
                    value.forEach {
                        if (it is TagStructure) {
                            analyze(it, keys)
                        }
                    }
                }
            }
        }

        fun getKey(alias: Byte): String? {
            return aliasKeyMap[alias]
        }

        private fun addKeyAlias(key: String) {
            keyAliases.add(key)
            keyAliasMap.put(key, currentId)
            aliasKeyMap.put(currentId++, key)
        }

        fun getAlias(key: String): Byte? {
            return keyAliasMap[key]
        }

        fun write(output: WritableByteStream) {
            output.put(keyAliases.size)
            for (keyAlias in keyAliases) {
                output.putString(keyAlias)
            }
        }

        private class KeyOccurrence(key: String) {
            val length: Int
            var count = 0

            init {
                length = key.length
                count += length
            }
        }
    }

    companion object {
        // Header
        internal val HEADER_MAGIC = byteArrayOf('S'.toByte(), 'T'.toByte(),
                'A'.toByte(), 'G'.toByte())
        internal val HEADER_VERSION: Byte = 0x1
        // Components
        //  Structure
        internal val ID_STRUCTURE_BEGIN: Byte = 0x10
        internal val ID_STRUCTURE_TERMINATE: Byte = 0x11
        internal val ID_STRUCTURE_EMPTY: Byte = 0x12
        //  List
        internal val ID_LIST_BEGIN: Byte = 0x20
        internal val ID_LIST_TERMINATE: Byte = 0x21
        internal val ID_LIST_EMPTY: Byte = 0x22
        //  Tags
        //   Unit
        internal val ID_TAG_UNIT: Byte = 0x30
        //   Boolean
        internal val ID_TAG_BOOLEAN: Byte = 0x31
        //   Integer
        internal val ID_TAG_BYTE: Byte = 0x40
        internal val ID_TAG_INT_16: Byte = 0x41
        internal val ID_TAG_INT_32: Byte = 0x42
        internal val ID_TAG_INT_64: Byte = 0x43
        //   Float
        internal val ID_TAG_FLOAT_32: Byte = 0x50
        internal val ID_TAG_FLOAT_64: Byte = 0x51
        //   Array
        internal val ID_TAG_BYTE_ARRAY: Byte = 0x60
        //   String
        internal val ID_TAG_STRING: Byte = 0x70
        private val DATA_STREAM = ThreadLocal {
            ByteBufferStream(growth = { it + 1048576 })
        }
        private val COMPRESSION_STREAM = ThreadLocal {
            ByteBufferStream(growth = { it + 1048576 })
        }

        internal fun readKey(stream: ReadableByteStream,
                             dictionary: KeyDictionary?,
                             allocationLimit: Int): String {
            val alias = stream.get()
            if (alias.toInt() == -1) {
                return stream.getString(allocationLimit)
            } else {
                if (dictionary == null) {
                    throw IOException("Dictionary key without dictionary")
                }
                val key = dictionary.getKey(alias) ?: throw IOException(
                        "Invalid key id: $alias")
                if (key.length > allocationLimit) {
                    throw IOException("No more allocations allowed for key")
                }
                return key
            }
        }

        internal fun writeKey(key: String,
                              stream: WritableByteStream,
                              dictionary: KeyDictionary) {
            val alias = dictionary.getAlias(key)
            if (alias == null) {
                stream.put(0xFF)
                stream.putString(key)
            } else {
                stream.put(alias.toInt())
            }
        }

        @Throws(IOException::class)
        fun write(stream: WritableByteStream,
                  tagStructure: TagStructure,
                  compression: Byte = -1,
                  useDictionary: Boolean = true,
                  byteStream: ByteBufferStream = DATA_STREAM.get(),
                  compressionStream: ByteBufferStream = COMPRESSION_STREAM.get()): TagStructure {
            tagStructure.write(TagStructureWriterBinary(stream, compression,
                    useDictionary, byteStream, compressionStream))
            return tagStructure
        }

        @Throws(IOException::class)
        fun read(stream: ReadableByteStream,
                 tagStructure: TagStructure = TagStructure(),
                 allocationLimit: Int = Int.MAX_VALUE,
                 compressionStream: ByteBufferStream = COMPRESSION_STREAM.get()): TagStructure {
            val reader = TagStructureReaderBinary(stream, compressionStream,
                    allocationLimit)
            reader.readStructure(tagStructure)
            return tagStructure
        }
    }
}
