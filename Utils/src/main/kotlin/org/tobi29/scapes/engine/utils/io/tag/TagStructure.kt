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

package org.tobi29.scapes.engine.utils.io.tag

import java8.util.concurrent.ConcurrentMaps
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class TagStructure {
    private var tags = ConcurrentHashMap<String, Any>()

    fun getBoolean(key: String): Boolean? {
        return getObject(key)
    }

    fun getNumber(key: String): Number? {
        return getObject(key)
    }

    fun getByteArray(key: String): ByteArray? {
        return getObject(key)
    }

    fun getString(key: String): String? {
        return getObject(key)
    }

    private inline fun <reified T> getObject(key: String): T? {
        val tag = tags[key]
        return if (tag is T) tag else null
    }

    fun structure(key: String): TagStructure {
        return ConcurrentMaps.compute(tags, key) { key, value ->
            if (value is TagStructure) {
                value
            } else {
                TagStructure()
            }
        } as TagStructure
    }

    fun getStructure(key: String): TagStructure? {
        return getObject(key)
    }

    fun getList(key: String): List<TagStructure>? {
        return getObject(key)
    }

    fun <E : MultiTag.Readable> getMultiTag(key: String,
                                            value: E): E {
        getStructure(key)?.let { value.read(it) }
        return value
    }

    val tagEntrySet: Set<Map.Entry<String, Any>>
        get() = Collections.unmodifiableSet(tags.entries)

    fun setBoolean(key: String,
                   value: Boolean): Boolean {
        return setObject(key, value)
    }

    fun setNumber(key: String,
                  value: Number): Number {
        return setObject(key, value)
    }

    fun setByteArray(key: String,
                     vararg value: Byte): ByteArray {
        tags.put(key, value)
        return value
    }

    fun setString(key: String,
                  value: String): String {
        tags.put(key, value)
        return value
    }

    fun setStructure(key: String,
                     value: TagStructure): TagStructure {
        tags.put(key, value)
        return value
    }

    fun setList(key: String,
                value: List<TagStructure>): List<TagStructure> {
        val list = StructureList(value.size)
        list.addAll(value)
        tags.put(key, list)
        return value
    }

    fun <E : MultiTag.Writeable> setMultiTag(key: String,
                                             value: E): E {
        setStructure(key, value.write())
        return value
    }

    private inline fun <reified T : Any> setObject(key: String,
                                                   value: T): T {
        tags.put(key, value)
        return value
    }

    fun move(from: String,
             to: String) {
        val tag = tags.remove(from)
        if (tag != null) {
            tags.put(to, tag)
        }
    }

    fun remove(key: String) {
        tags.remove(key)
    }

    fun has(key: String): Boolean {
        return tags.containsKey(key)
    }

    fun copy(): TagStructure {
        val tag = TagStructure()
        for ((key, value) in tags) {
            if (value is TagStructure) {
                tag.tags.put(key, value.copy())
            } else if (value is StructureList) {
                val list = StructureList(value.size)
                for (child in value) {
                    list.add(child.copy())
                }
                tag.tags.put(key, list)
            } else if (value is ByteArray) {
                val copy = ByteArray(value.size)
                System.arraycopy(value, 0, copy, 0, value.size)
                tag.tags.put(key, copy)
            } else {
                tag.tags.put(key, value)
            }
        }
        return tag
    }

    @Throws(IOException::class)
    fun write(writer: TagStructureWriter) {
        writer.begin(this)
        writeData(writer)
        writer.end()
    }

    override fun hashCode(): Int {
        var hash = 31
        for ((key, value) in tags) {
            if (value is ByteArray) {
                hash += 31 * hash + Arrays.hashCode(value)
            } else {
                hash += 31 * hash + key.hashCode()
                hash += 31 * hash + value.hashCode()
            }
        }
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (other is TagStructure) {
            for ((key, value) in tags) {
                if (value is ByteArray) {
                    val otherTag = other.tags[key]
                    if (otherTag is ByteArray) {
                        if (!Arrays.equals(value, otherTag)) {
                            return false
                        }
                    } else {
                        return false
                    }
                } else {
                    if (value != other.tags[key]) {
                        return false
                    }
                }
            }
            return true
        }
        return false
    }

    @Throws(IOException::class)
    private fun writeData(writer: TagStructureWriter) {
        for ((key, value) in tags) {
            if (value is TagStructure) {
                if (value.tags.isEmpty()) {
                    writer.structureEmpty(key)
                } else {
                    writer.beginStructure(key)
                    value.writeData(writer)
                    writer.endStructure()
                }
            } else if (value is StructureList) {
                var size = value.size
                if (size > 0) {
                    size--
                    writer.beginList(key)
                    for (i in 0..size - 1) {
                        val structure = value[i]
                        if (structure.tags.isEmpty()) {
                            writer.structureEmpty()
                        } else {
                            writer.beginStructure()
                            structure.writeData(writer)
                            writer.endStructure()
                        }
                    }
                    val structure = value[size]
                    if (structure.tags.isEmpty()) {
                        writer.endListWithEmpty()
                    } else {
                        writer.beginStructure()
                        structure.writeData(writer)
                        writer.endListWidthTerminate()
                    }
                } else {
                    writer.listEmpty(key)
                }
            } else {
                writer.writeTag(key, value)
            }
        }
    }

    class StructureList : ArrayList<TagStructure> {
        internal constructor(size: Int = 10) : super(size) {
        }

        override fun clone(): Any {
            return super.clone()
        }
    }
}
