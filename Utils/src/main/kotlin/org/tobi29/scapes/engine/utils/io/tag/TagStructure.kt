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
import org.tobi29.scapes.engine.utils.forEachNonNull
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
        val tag = tags[key]
        if (tag is ByteArray) {
            return tag
        }
        if (tag is List<*>) {
            @Suppress("UNCHECKED_CAST")
            val list = tag as List<Any>
            val array = ByteArray(list.size)
            var i = 0
            list.forEach {
                if (it !is Number) {
                    return null
                }
                array[i++] = it.toByte()
            }
            return array
        }
        return null
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

    fun getList(key: String): List<Any>? {
        val tag = tags[key]
        if (tag is List<*>) {
            @Suppress("UNCHECKED_CAST")
            return tag as List<Any>
        }
        if (tag is ByteArray) {
            val list = ArrayList<Byte>(tag.size)
            tag.forEach { list.add(it) }
            return Collections.unmodifiableList(list)
        }
        return null
    }

    fun <E : MultiTag.Readable> getMultiTag(key: String,
                                            value: E): E {
        getStructure(key)?.let { value.read(it) }
        return value
    }

    val tagEntrySet: Set<Map.Entry<String, Any>>
        get() = Collections.unmodifiableSet(tags.entries)

    fun setUnit(key: String) {
        setObject(key, Unit)
    }

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

    fun <T : Any> setList(key: String,
                          value: List<T>): List<T> {
        tags.put(key, Collections.unmodifiableList(value))
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
            tag.tags.put(key, copy(value))
        }
        return tag
    }

    fun write(writer: TagStructureWriter) {
        writer.begin(this)
        writeData(writer)
        writer.end()
    }

    override fun toString(): String {
        val output = StringBuilder(tags.size * 64)
        var first = true
        output.append("{")
        for ((key, value) in tags) {
            if (first) {
                first = false
            } else {
                output.append(", ")
            }
            output.append(key)
            output.append(": ")
            if (value is ByteArray) {
                output.append(Arrays.toString(value))
            } else {
                output.append(value.toString())
            }
        }
        output.append("}")
        return output.toString()
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
            // This should check for tags missing in our structure that are in
            // the other one
            if (tags.size != other.tags.size) {
                return false
            }
            for ((key, value) in tags) {
                val otherTag = other.tags[key]
                if (value is ByteArray) {
                    if (otherTag is ByteArray) {
                        if (!Arrays.equals(value, otherTag)) {
                            return false
                        }
                    } else if (otherTag is List<*>) {
                        if (value.size != otherTag.size) {
                            return false
                        }
                        val iterator = otherTag.iterator()
                        for (element in value) {
                            if (element != iterator.next()) {
                                return false
                            }
                        }
                    } else {
                        return false
                    }
                } else if (value is List<*>) {
                    if (otherTag is ByteArray) {
                        if (value.size != otherTag.size) {
                            return false
                        }
                        val iterator = value.iterator()
                        for (element in otherTag) {
                            if (element != iterator.next()) {
                                return false
                            }
                        }
                    } else if (value != otherTag) {
                        return false
                    }
                } else {
                    if (value != otherTag) {
                        return false
                    }
                }
            }
            return true
        }
        return false
    }

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
            } else if (value is List<*>) {
                @Suppress("UNCHECKED_CAST")
                writeList(writer, key, value as List<Any>)
            } else {
                writer.writePrimitiveTag(key, value)
            }
        }
    }

    companion object {
        fun <T : Any> copy(value: T): T {
            if (value is TagStructure) {
                @Suppress("UNCHECKED_CAST")
                return value.copy() as T
            } else if (value is List<*>) {
                val list = ArrayList<Any>(value.size)
                value.forEachNonNull { child ->
                    if (child is TagStructure) {
                        list.add(child.copy())
                    } else {
                        list.add(child)
                    }
                }
                @Suppress("UNCHECKED_CAST")
                return list as T
            } else if (value is ByteArray) {
                val copy = ByteArray(value.size)
                System.arraycopy(value, 0, copy, 0, value.size)
                @Suppress("UNCHECKED_CAST")
                return copy as T
            } else {
                return value
            }
        }

        private fun writeList(writer: TagStructureWriter,
                              key: String,
                              list: List<Any>) {
            var size = list.size
            if (size > 0) {
                size--
                writer.beginList(key)
                for (i in 0..size - 1) {
                    val element = list[i]
                    if (element is TagStructure) {
                        if (element.tags.isEmpty()) {
                            writer.structureEmpty()
                        } else {
                            writer.beginListStructure()
                            element.writeData(writer)
                            writer.endStructure()
                        }
                    } else if (element is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        writeList(writer, element as List<Any>)
                    } else {
                        writer.writePrimitiveTag(element)
                    }
                }
                val element = list[size]
                if (element is TagStructure) {
                    if (element.tags.isEmpty()) {
                        writer.endListWithEmpty()
                    } else {
                        writer.beginListStructure()
                        element.writeData(writer)
                        writer.endListWithTerminate()
                    }
                } else if (element is List<*>) {
                    @Suppress("UNCHECKED_CAST")
                    writeList(writer, element as List<Any>)
                    writer.endList()
                } else {
                    writer.writePrimitiveTag(element)
                    writer.endList()
                }
            } else {
                writer.listEmpty(key)
            }
        }

        private fun writeList(writer: TagStructureWriter,
                              list: List<Any>) {
            var size = list.size
            if (size > 0) {
                size--
                writer.beginList()
                for (i in 0..size - 1) {
                    val element = list[i]
                    if (element is TagStructure) {
                        if (element.tags.isEmpty()) {
                            writer.structureEmpty()
                        } else {
                            writer.beginListStructure()
                            element.writeData(writer)
                            writer.endStructure()
                        }
                    } else if (element is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        writeList(writer, element as List<Any>)
                    } else {
                        writer.writePrimitiveTag(element)
                    }
                }
                val element = list[size]
                if (element is TagStructure) {
                    if (element.tags.isEmpty()) {
                        writer.endListWithEmpty()
                    } else {
                        writer.beginListStructure()
                        element.writeData(writer)
                        writer.endListWithTerminate()
                    }
                } else if (element is List<*>) {
                    @Suppress("UNCHECKED_CAST")
                    writeList(writer, element as List<Any>)
                    writer.endList()
                } else {
                    writer.writePrimitiveTag(element)
                    writer.endList()
                }
            } else {
                writer.listEmpty()
            }
        }
    }
}
