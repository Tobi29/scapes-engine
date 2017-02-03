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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils.io.tag

import java.util.*

inline fun structure(block: TagStructure.() -> Unit): TagStructure {
    val structure = TagStructure()
    block(structure)
    return structure
}

inline fun list(block: MutableList<Any>.() -> Unit): ArrayList<Any> {
    val list = ArrayList<Any>()
    block(list)
    return list
}

inline fun TagStructure.setStructure(key: String) {
    setStructure(key, TagStructure())
}

inline fun TagStructure.setStructure(key: String,
                                     block: TagStructure.() -> Unit) {
    setStructure(key, structure(block))
}

inline fun TagStructure.setList(key: String) {
    setList(key, ArrayList<Any>())
}

inline fun TagStructure.setList(key: String,
                                block: MutableList<Any>.() -> Unit) {
    setList(key, list(block))
}

inline fun TagStructure.getList(key: String,
                                block: (Any) -> Unit) {
    getList(key)?.let { list ->
        list.forEach { block(it) }
    }
}

inline fun TagStructure.getListStructure(key: String,
                                         block: (TagStructure) -> Unit) {
    getList(key) { element ->
        if (element is TagStructure) {
            block(element)
        }
    }
}

inline fun TagStructure.getListUnit(key: String,
                                    block: (Unit) -> Unit) {
    getList(key) { element ->
        if (element is Unit) {
            block(element)
        }
    }
}

inline fun TagStructure.getListBoolean(key: String,
                                       block: (Boolean) -> Unit) {
    getList(key) { element ->
        if (element is Boolean) {
            block(element)
        }
    }
}

inline fun TagStructure.getListByte(key: String,
                                    block: (Byte) -> Unit) {
    getList(key) { element ->
        if (element is Byte) {
            block(element)
        }
    }
}

inline fun TagStructure.getListShort(key: String,
                                     block: (Short) -> Unit) {
    getList(key) { element ->
        if (element is Short) {
            block(element)
        }
    }
}

inline fun TagStructure.getListInt(key: String,
                                   block: (Int) -> Unit) {
    getList(key) { element ->
        if (element is Int) {
            block(element)
        }
    }
}

inline fun TagStructure.getListLong(key: String,
                                    block: (Long) -> Unit) {
    getList(key) { element ->
        if (element is Long) {
            block(element)
        }
    }
}

inline fun TagStructure.getListFloat(key: String,
                                     block: (Float) -> Unit) {
    getList(key) { element ->
        if (element is Float) {
            block(element)
        }
    }
}

inline fun TagStructure.getListDouble(key: String,
                                      block: (Double) -> Unit) {
    getList(key) { element ->
        if (element is Double) {
            block(element)
        }
    }
}

inline fun TagStructure.getListString(key: String,
                                      block: (String) -> Unit) {
    getList(key) { element ->
        if (element is String) {
            block(element)
        }
    }
}

inline fun TagStructure.getByte(key: String): Byte? {
    return cast(getNumber(key), Number::toByte)
}

inline fun TagStructure.setByte(key: String,
                                value: Byte) {
    setNumber(key, value)
}

inline fun TagStructure.getShort(key: String): Short? {
    return cast(getNumber(key), Number::toShort)
}

inline fun TagStructure.setShort(key: String,
                                 value: Short) {
    setNumber(key, value)
}

inline fun TagStructure.getInt(key: String): Int? {
    return cast(getNumber(key), Number::toInt)
}

inline fun TagStructure.setInt(key: String,
                               value: Int) {
    setNumber(key, value)
}

inline fun TagStructure.getLong(key: String): Long? {
    return cast(getNumber(key), Number::toLong)
}

inline fun TagStructure.setLong(key: String,
                                value: Long) {
    setNumber(key, value)
}

inline fun TagStructure.getFloat(key: String): Float? {
    return cast(getNumber(key), Number::toFloat)
}

inline fun TagStructure.setFloat(key: String,
                                 value: Float) {
    setNumber(key, value)
}

inline fun TagStructure.getDouble(key: String): Double? {
    return cast(getNumber(key), Number::toDouble)
}

inline fun TagStructure.setDouble(key: String,
                                  value: Double) {
    setNumber(key, value)
}

inline fun TagStructure.getUUID(key: String): UUID? {
    val str = getString(key)
    if (str != null) {
        try {
            return UUID.fromString(str)
        } catch (e: IllegalArgumentException) {
        }
    }
    val tagStructure = getStructure(key) ?: return null
    return MultiTag.readUUID(tagStructure)
}

inline fun TagStructure.setUUID(key: String,
                                value: UUID) {
    setStructure(key, MultiTag.writeUUID(value))
}

inline fun TagStructure.getProperties(key: String): Properties? {
    val tagStructure = getStructure(key) ?: return null
    return MultiTag.readProperties(tagStructure)
}

inline fun TagStructure.getProperties(key: String,
                                      properties: Properties) {
    val tagStructure = getStructure(key) ?: return
    properties.clear()
    MultiTag.readProperties(tagStructure, properties)
}

inline fun TagStructure.setProperties(key: String,
                                      value: Properties) {
    setStructure(key, MultiTag.writeProperties(value))
}

inline fun <reified E> cast(value: Number?,
                            convert: (Number) -> E): E? {
    if (value == null) {
        return null
    }
    if (value is E) {
        return value
    }
    return convert(value)
}
