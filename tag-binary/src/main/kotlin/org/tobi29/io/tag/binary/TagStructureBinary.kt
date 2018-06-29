/*
 * Copyright 2012-2018 Tobi29
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

import org.tobi29.io.*
import org.tobi29.io.tag.TagList
import org.tobi29.io.tag.TagMap
import org.tobi29.io.tag.TagString
import org.tobi29.io.tag.write
import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.stdex.ThreadLocal

fun readBinary(stream: ReadableByteStream) = readBinary(
    stream, Int.MAX_VALUE,
    COMPRESSION_STREAM.get()
)

fun readBinary(
    stream: ReadableByteStream,
    allocationLimit: Int = Int.MAX_VALUE,
    compressionStream: MemoryStream = COMPRESSION_STREAM.get()
): TagMap {
    TagStructureReaderBinary(
        stream,
        allocationLimit, compressionStream
    ).let { reader ->
        return TagMap { reader.readMap(this) }
    }
}

fun TagMap.writeBinary(
    stream: WritableByteStream,
    compression: Byte = -1,
    useDictionary: Boolean = true,
    byteStream: MemoryStream = DATA_STREAM.get(),
    compressionStream: MemoryStream = COMPRESSION_STREAM.get()
) {
    TagStructureWriterBinary(
        stream, compression, useDictionary, byteStream,
        compressionStream
    ).let { writer ->
        writer.begin(this)
        write(writer)
        writer.end()
    }
}

// Header
internal val HEADER_MAGIC = byteArrayOf(
    'S'.toByte(), 'T'.toByte(),
    'A'.toByte(), 'G'.toByte()
)
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
internal val ID_TAG_STRING_REF: Byte = 0x71

internal fun readKey(
    stream: ReadableByteStream,
    dictionary: KeyDictionary?,
    allocationLimit: Int
): String {
    val alias = stream.get()
    return if (alias.toInt() == -1) {
        stream.getString(allocationLimit)
    } else {
        if (dictionary == null) {
            throw IOException("Dictionary key without dictionary")
        }
        val key = dictionary.getString(alias) ?: throw IOException(
            "Invalid key id: $alias"
        )
        if (key.length > allocationLimit) {
            throw IOException("No more allocations allowed for key")
        }
        key
    }
}

internal fun writeKey(
    value: String,
    stream: WritableByteStream,
    dictionary: KeyDictionary
) {
    val alias = dictionary.getId(value)
    if (alias == null) {
        stream.put(0xFF.toByte())
        stream.putString(value)
    } else {
        stream.put(alias)
    }
}

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
            addString(stream.getString())
        }
    }

    constructor(tagStructure: TagMap) {
        val keys = ConcurrentHashMap<String, StringOccurrence>()
        analyze(tagStructure, keys)
        if (keys.size > 255) {
            keys.entries.asSequence().sortedBy { it.value.count }.take(
                255
            ).map { it.key }.forEach { addString(it) }
        } else {
            keys.entries.asSequence().map { it.key }.forEach {
                addString(it)
            }
        }
    }

    private fun analyze(
        tagStructure: TagMap,
        strings: MutableMap<String, StringOccurrence>
    ) {
        for ((key, value) in tagStructure.value.entries) {
            stringOccurence(key, strings)
            if (value is TagMap) {
                analyze(value, strings)
            } else if (value is TagList) {
                value.value.forEach {
                    if (it is TagMap) {
                        analyze(it, strings)
                    }
                }
            } else if (value is TagString) {
                stringOccurence(value.value, strings)
            }
        }
    }

    private fun stringOccurence(
        string: String,
        strings: MutableMap<String, StringOccurrence>
    ) {
        val occurrence = strings[string]
        if (occurrence == null) {
            strings[string] = StringOccurrence(string)
        } else {
            occurrence.count += occurrence.length
        }
    }

    fun getString(alias: Byte): String? {
        return aliasKeyMap[alias]
    }

    private fun addString(key: String) {
        keyAliases.add(key)
        keyAliasMap[key] = currentId
        aliasKeyMap[currentId++] = key
    }

    fun getId(key: String): Byte? {
        return keyAliasMap[key]
    }

    fun write(output: WritableByteStream) {
        output.put(keyAliases.size.toByte())
        for (keyAlias in keyAliases) {
            output.putString(keyAlias)
        }
    }

    private class StringOccurrence(key: String) {
        val length: Int = key.length
        var count = 0

        init {
            count += length
        }
    }
}

private val DATA_STREAM = ThreadLocal {
    MemoryViewStreamDefault(growth = { it + 10485 })
}
private val COMPRESSION_STREAM = ThreadLocal {
    MemoryViewStreamDefault(growth = { it + 10485 })
}
