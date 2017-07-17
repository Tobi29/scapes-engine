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
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.io.WritableByteStream
import org.tobi29.scapes.engine.utils.tag.*

class TagStructureWriterBinary(private val stream: WritableByteStream,
                               private val compression: Byte,
                               private val useDictionary: Boolean,
                               private val byteStream: ByteBufferStream,
                               private val compressionStream: ByteBufferStream) : TagStructureWriter {
    private lateinit var dictionary: KeyDictionary
    private lateinit var structureStream: WritableByteStream

    // TODO: @Throws(IOException::class)
    override fun begin(root: TagMap) {
        if (compression >= 0) {
            byteStream.buffer().clear()
            structureStream = byteStream
        } else {
            stream.put(HEADER_MAGIC)
            stream.put(HEADER_VERSION)
            stream.put(compression)
            structureStream = stream
        }
        if (useDictionary) {
            dictionary = KeyDictionary(root)
        } else {
            dictionary = KeyDictionary()
        }
        dictionary.write(structureStream)
    }

    // TODO: @Throws(IOException::class)
    override fun end() {
        structureStream.put(ID_STRUCTURE_TERMINATE)
        if (compression >= 0) {
            byteStream.buffer().flip()
            compressionStream.buffer().clear()
            CompressionUtil.compress(ByteBufferStream(byteStream.buffer()),
                    compressionStream)
            compressionStream.buffer().flip()
            byteStream.buffer().clear()
            stream.put(HEADER_MAGIC)
            stream.put(HEADER_VERSION)
            stream.put(compression)
            stream.putInt(compressionStream.buffer().remaining())
            stream.put(compressionStream.buffer())
            compressionStream.buffer().clear()
        }
    }

    // TODO: @Throws(IOException::class)
    override fun beginStructure() {
    }

    // TODO: @Throws(IOException::class)
    override fun beginStructure(key: String) {
        structureStream.put(ID_STRUCTURE_BEGIN)
        writeKey(key, structureStream, dictionary)
    }

    // TODO: @Throws(IOException::class)
    override fun endStructure() {
        structureStream.put(ID_STRUCTURE_TERMINATE)
    }

    // TODO: @Throws(IOException::class)
    override fun structureEmpty() {
        structureStream.put(ID_STRUCTURE_EMPTY)
    }

    // TODO: @Throws(IOException::class)
    override fun structureEmpty(key: String) {
        structureStream.put(ID_STRUCTURE_EMPTY)
        writeKey(key, structureStream, dictionary)
    }

    // TODO: @Throws(IOException::class)
    override fun beginList(key: String) {
        structureStream.put(ID_LIST_BEGIN)
        writeKey(key, structureStream, dictionary)
    }

    // TODO: @Throws(IOException::class)
    override fun beginList() {
        structureStream.put(ID_LIST_BEGIN)
    }

    override fun beginListStructure() {
        structureStream.put(ID_STRUCTURE_BEGIN)
    }

    // TODO: @Throws(IOException::class)
    override fun endListWithTerminate() {
        structureStream.put(ID_LIST_TERMINATE)
    }

    // TODO: @Throws(IOException::class)
    override fun endListWithEmpty() {
        structureStream.put(ID_LIST_TERMINATE)
    }

    // TODO: @Throws(IOException::class)
    override fun endList() {
        structureStream.put(ID_LIST_TERMINATE)
    }

    // TODO: @Throws(IOException::class)
    override fun listEmpty(key: String) {
        structureStream.put(ID_LIST_EMPTY)
        writeKey(key, structureStream, dictionary)
    }

    // TODO: @Throws(IOException::class)
    override fun listEmpty() {
        structureStream.put(ID_LIST_EMPTY)
    }

    // TODO: @Throws(IOException::class)
    override fun writePrimitiveTag(key: String,
                                   tag: TagPrimitive) {
        when (tag) {
            is TagUnit -> {
                structureStream.put(ID_TAG_UNIT)
                writeKey(key, structureStream, dictionary)
            }
            is TagBoolean -> {
                structureStream.put(ID_TAG_BOOLEAN)
                writeKey(key, structureStream, dictionary)
                structureStream.put(if (tag.value) 1.toByte() else 0)
            }
            is TagByte -> {
                structureStream.put(ID_TAG_BYTE)
                writeKey(key, structureStream, dictionary)
                structureStream.put(tag.value)
            }
            is TagShort -> {
                structureStream.put(ID_TAG_INT_16)
                writeKey(key, structureStream, dictionary)
                structureStream.putShort(tag.value)
            }
            is TagInt -> {
                structureStream.put(ID_TAG_INT_32)
                writeKey(key, structureStream, dictionary)
                structureStream.putInt(tag.value)
            }
            is TagLong -> {
                structureStream.put(ID_TAG_INT_64)
                writeKey(key, structureStream, dictionary)
                structureStream.putLong(tag.value)
            }
            is TagFloat -> {
                structureStream.put(ID_TAG_FLOAT_32)
                writeKey(key, structureStream, dictionary)
                structureStream.putFloat(tag.value)
            }
            is TagDouble -> {
                structureStream.put(ID_TAG_FLOAT_64)
                writeKey(key, structureStream, dictionary)
                structureStream.putDouble(tag.value)
            }
            is TagNumber -> {
                // TODO: Support big integer and big decimal natively
                structureStream.put(ID_TAG_FLOAT_64)
                writeKey(key, structureStream, dictionary)
                structureStream.putDouble(tag.value.toDouble())
            }
            is TagString -> {
                structureStream.put(ID_TAG_STRING)
                writeKey(key, structureStream, dictionary)
                structureStream.putString(tag.value)
            }
            is TagByteArray -> {
                structureStream.put(ID_TAG_BYTE_ARRAY)
                writeKey(key, structureStream, dictionary)
                structureStream.putByteArrayLong(tag.value)
            }
            else -> throw IOException("Invalid type: ${tag::class}")
        }
    }

    override fun writePrimitiveTag(tag: TagPrimitive) {
        when (tag) {
            is TagUnit -> {
                structureStream.put(ID_TAG_UNIT)
            }
            is TagBoolean -> {
                structureStream.put(ID_TAG_BOOLEAN)
                structureStream.put(if (tag.value) 1.toByte() else 0)
            }
            is TagByte -> {
                structureStream.put(ID_TAG_BYTE)
                structureStream.put(tag.value)
            }
            is TagShort -> {
                structureStream.put(ID_TAG_INT_16)
                structureStream.putShort(tag.value)
            }
            is TagInt -> {
                structureStream.put(ID_TAG_INT_32)
                structureStream.putInt(tag.value)
            }
            is TagLong -> {
                structureStream.put(ID_TAG_INT_64)
                structureStream.putLong(tag.value)
            }
            is TagFloat -> {
                structureStream.put(ID_TAG_FLOAT_32)
                structureStream.putFloat(tag.value)
            }
            is TagDouble -> {
                structureStream.put(ID_TAG_FLOAT_64)
                structureStream.putDouble(tag.value)
            }
            is TagNumber -> {
                // TODO: Support big integer and big decimal natively
                structureStream.put(ID_TAG_FLOAT_64)
                structureStream.putDouble(tag.value.toDouble())
            }
            is TagString -> {
                structureStream.put(ID_TAG_STRING)
                structureStream.putString(tag.value)
            }
            is TagByteArray -> {
                structureStream.put(ID_TAG_BYTE_ARRAY)
                structureStream.putByteArrayLong(tag.value)
            }
            else -> throw IOException("Invalid type: ${tag::class}")
        }
    }
}
