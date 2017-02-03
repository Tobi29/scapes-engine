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

import org.tobi29.scapes.engine.utils.io.ByteBufferStream
import org.tobi29.scapes.engine.utils.io.CompressionUtil
import org.tobi29.scapes.engine.utils.io.WritableByteStream
import org.tobi29.scapes.engine.utils.io.tag.TagStructure
import org.tobi29.scapes.engine.utils.io.tag.TagStructureWriter

import java.io.IOException

class TagStructureWriterBinary(private val stream: WritableByteStream,
                               private val compression: Byte,
                               private val useDictionary: Boolean,
                               private val byteStream: ByteBufferStream,
                               private val compressionStream: ByteBufferStream) : TagStructureBinary(), TagStructureWriter {
    private lateinit var dictionary: TagStructureBinary.KeyDictionary
    private lateinit var structureStream: WritableByteStream

    @Throws(IOException::class)
    override fun begin(root: TagStructure) {
        if (compression >= 0) {
            byteStream.buffer().clear()
            structureStream = byteStream
        } else {
            stream.put(TagStructureBinary.HEADER_MAGIC)
            stream.put(TagStructureBinary.HEADER_VERSION.toInt())
            stream.put(compression.toInt())
            structureStream = stream
        }
        if (useDictionary) {
            dictionary = TagStructureBinary.KeyDictionary(root)
        } else {
            dictionary = TagStructureBinary.KeyDictionary()
        }
        dictionary.write(structureStream)
    }

    @Throws(IOException::class)
    override fun end() {
        structureStream.put(TagStructureBinary.ID_STRUCTURE_TERMINATE.toInt())
        if (compression >= 0) {
            byteStream.buffer().flip()
            compressionStream.buffer().clear()
            CompressionUtil.compress(ByteBufferStream(byteStream.buffer()),
                    compressionStream)
            compressionStream.buffer().flip()
            byteStream.buffer().clear()
            stream.put(TagStructureBinary.HEADER_MAGIC)
            stream.put(TagStructureBinary.HEADER_VERSION.toInt())
            stream.put(compression.toInt())
            stream.putInt(compressionStream.buffer().remaining())
            stream.put(compressionStream.buffer())
            compressionStream.buffer().clear()
        }
    }

    @Throws(IOException::class)
    override fun beginStructure() {
    }

    @Throws(IOException::class)
    override fun beginStructure(key: String) {
        structureStream.put(TagStructureBinary.ID_STRUCTURE_BEGIN.toInt())
        TagStructureBinary.writeKey(key, structureStream, dictionary)
    }

    @Throws(IOException::class)
    override fun endStructure() {
        structureStream.put(TagStructureBinary.ID_STRUCTURE_TERMINATE.toInt())
    }

    @Throws(IOException::class)
    override fun structureEmpty() {
        structureStream.put(TagStructureBinary.ID_STRUCTURE_EMPTY.toInt())
    }

    @Throws(IOException::class)
    override fun structureEmpty(key: String) {
        structureStream.put(TagStructureBinary.ID_STRUCTURE_EMPTY.toInt())
        TagStructureBinary.writeKey(key, structureStream, dictionary)
    }

    @Throws(IOException::class)
    override fun beginList(key: String) {
        structureStream.put(TagStructureBinary.ID_LIST_BEGIN.toInt())
        TagStructureBinary.writeKey(key, structureStream, dictionary)
    }

    @Throws(IOException::class)
    override fun beginList() {
        structureStream.put(TagStructureBinary.ID_LIST_BEGIN.toInt())
    }

    override fun beginListStructure() {
        structureStream.put(TagStructureBinary.ID_STRUCTURE_BEGIN.toInt())
    }

    @Throws(IOException::class)
    override fun endListWithTerminate() {
        structureStream.put(TagStructureBinary.ID_LIST_TERMINATE.toInt())
    }

    @Throws(IOException::class)
    override fun endListWithEmpty() {
        structureStream.put(TagStructureBinary.ID_LIST_TERMINATE.toInt())
    }

    @Throws(IOException::class)
    override fun endList() {
        structureStream.put(TagStructureBinary.ID_LIST_TERMINATE.toInt())
    }

    @Throws(IOException::class)
    override fun listEmpty(key: String) {
        structureStream.put(TagStructureBinary.ID_LIST_EMPTY.toInt())
        TagStructureBinary.writeKey(key, structureStream, dictionary)
    }

    @Throws(IOException::class)
    override fun listEmpty() {
        structureStream.put(TagStructureBinary.ID_LIST_EMPTY.toInt())
    }

    @Throws(IOException::class)
    override fun writePrimitiveTag(key: String,
                                   tag: Any) {
        if (tag is Unit) {
            structureStream.put(TagStructureBinary.ID_TAG_UNIT.toInt())
            TagStructureBinary.writeKey(key, structureStream, dictionary)
        } else if (tag is Boolean) {
            structureStream.put(TagStructureBinary.ID_TAG_BOOLEAN.toInt())
            TagStructureBinary.writeKey(key, structureStream, dictionary)
            structureStream.put(if (tag) 1 else 0)
        } else if (tag is Byte) {
            structureStream.put(TagStructureBinary.ID_TAG_BYTE.toInt())
            TagStructureBinary.writeKey(key, structureStream, dictionary)
            structureStream.put(tag.toInt())
        } else if (tag is ByteArray) {
            structureStream.put(TagStructureBinary.ID_TAG_BYTE_ARRAY.toInt())
            TagStructureBinary.writeKey(key, structureStream, dictionary)
            structureStream.putByteArrayLong(tag)
        } else if (tag is Short) {
            structureStream.put(TagStructureBinary.ID_TAG_INT_16.toInt())
            TagStructureBinary.writeKey(key, structureStream, dictionary)
            structureStream.putShort(tag.toInt())
        } else if (tag is Int) {
            structureStream.put(TagStructureBinary.ID_TAG_INT_32.toInt())
            TagStructureBinary.writeKey(key, structureStream, dictionary)
            structureStream.putInt(tag)
        } else if (tag is Long) {
            structureStream.put(TagStructureBinary.ID_TAG_INT_64.toInt())
            TagStructureBinary.writeKey(key, structureStream, dictionary)
            structureStream.putLong(tag)
        } else if (tag is Float) {
            structureStream.put(TagStructureBinary.ID_TAG_FLOAT_32.toInt())
            TagStructureBinary.writeKey(key, structureStream, dictionary)
            structureStream.putFloat(tag)
        } else if (tag is Double) {
            structureStream.put(TagStructureBinary.ID_TAG_FLOAT_64.toInt())
            TagStructureBinary.writeKey(key, structureStream, dictionary)
            structureStream.putDouble(tag)
        } else if (tag is Number) {
            structureStream.put(TagStructureBinary.ID_TAG_FLOAT_64.toInt())
            TagStructureBinary.writeKey(key, structureStream, dictionary)
            structureStream.putDouble(tag.toDouble())
        } else if (tag is String) {
            structureStream.put(TagStructureBinary.ID_TAG_STRING.toInt())
            TagStructureBinary.writeKey(key, structureStream, dictionary)
            structureStream.putString(tag)
        } else {
            throw IOException("Invalid type: " + tag.javaClass)
        }
    }

    override fun writePrimitiveTag(tag: Any) {
        if (tag is Unit) {
            structureStream.put(TagStructureBinary.ID_TAG_UNIT.toInt())
        } else if (tag is Boolean) {
            structureStream.put(TagStructureBinary.ID_TAG_BOOLEAN.toInt())
            structureStream.put(if (tag) 1 else 0)
        } else if (tag is Byte) {
            structureStream.put(TagStructureBinary.ID_TAG_BYTE.toInt())
            structureStream.put(tag.toInt())
        } else if (tag is ByteArray) {
            structureStream.put(TagStructureBinary.ID_TAG_BYTE_ARRAY.toInt())
            structureStream.putByteArrayLong(tag)
        } else if (tag is Short) {
            structureStream.put(TagStructureBinary.ID_TAG_INT_16.toInt())
            structureStream.putShort(tag.toInt())
        } else if (tag is Int) {
            structureStream.put(TagStructureBinary.ID_TAG_INT_32.toInt())
            structureStream.putInt(tag)
        } else if (tag is Long) {
            structureStream.put(TagStructureBinary.ID_TAG_INT_64.toInt())
            structureStream.putLong(tag)
        } else if (tag is Float) {
            structureStream.put(TagStructureBinary.ID_TAG_FLOAT_32.toInt())
            structureStream.putFloat(tag)
        } else if (tag is Double) {
            structureStream.put(TagStructureBinary.ID_TAG_FLOAT_64.toInt())
            structureStream.putDouble(tag)
        } else if (tag is Number) {
            structureStream.put(TagStructureBinary.ID_TAG_FLOAT_64.toInt())
            structureStream.putDouble(tag.toDouble())
        } else if (tag is String) {
            structureStream.put(TagStructureBinary.ID_TAG_STRING.toInt())
            structureStream.putString(tag)
        } else {
            throw IOException("Invalid type: " + tag.javaClass)
        }
    }
}
