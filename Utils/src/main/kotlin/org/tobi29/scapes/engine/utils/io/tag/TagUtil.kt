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

import java8.util.Maps
import java.io.IOException
import java.util.*

inline fun MutableTag.toBoolean() = value as? Boolean

inline fun MutableTag.toNumber() = value as? Number

inline fun MutableTag.toByte() = toNumber()?.toByte()

inline fun MutableTag.toShort() = toNumber()?.toShort()

inline fun MutableTag.toInt() = toNumber()?.toInt()

inline fun MutableTag.toLong() = toNumber()?.toLong()

inline fun MutableTag.toFloat() = toNumber()?.toFloat()

inline fun MutableTag.toDouble() = toNumber()?.toDouble()

inline fun TagNumber.toByte() = value.toByte()

inline fun TagNumber.toShort() = value.toShort()

inline fun TagNumber.toInt() = value.toInt()

inline fun TagNumber.toLong() = value.toLong()

inline fun TagNumber.toFloat() = value.toFloat()

inline fun TagNumber.toDouble() = value.toDouble()

fun MutableTag.toMap() = when (this) {
    is TagMap -> this
    is MutableTagMap -> toTag()
    else -> null
}

fun MutableTag.asMap(): ReadTagMutableMap? = when (this) {
    is TagMap -> this
    is MutableTagMap -> this
    else -> null
}

fun MutableTag.toList() = when (this) {
    is TagList -> this
    is TagByteArray -> {
        val value = value
        ArrayList<Tag>(value.size).apply {
            value.forEach { add(it.toTag()) }
        }
    }
    else -> null
}

fun MutableTag.asList(): ReadTagMutableList? = when (this) {
    is TagList -> this
    is MutableTagList -> this
    else -> null
}

fun MutableTag.toByteArray() = when (this) {
    is TagByteArray -> value
    is TagList -> {
        ByteArray(size).also { array ->
            for ((i, element) in this.withIndex()) {
                array[i] = element.toByte() ?: return null
            }
        }
    }
    else -> null
}

fun Sequence<Tag>.toTag() = TagList {
    this@toTag.forEach { add(it) }
}

@JvmName("toTagMut")
fun Sequence<MutableTag>.toTag() = TagList {
    this@toTag.forEach { add(it.toTag()) }
}

fun UUID.toTag() = TagMap {
    this["Most"] = mostSignificantBits
    this["Least"] = leastSignificantBits
}

inline fun ReadTagMutableMap.map(key: String) = this[key]?.asMap()

inline fun ReadTagMutableMap.mapMut(key: String): MutableTagMap {
    return Maps.compute(this, key) { _, value ->
        when (value) {
            is TagMap -> value.toMutTag()
            is MutableTagMap -> value
            else -> MutableTagMap()
        }
    } as MutableTagMap
}

inline fun <R> ReadTagMutableMap.syncMapMut(key: String,
                                            block: (MutableTagMap) -> R): R {
    mapMut(key).let { map ->
        synchronized(map) {
            return block(map)
        }
    }
}

inline fun ReadTagMutableMap.list(key: String) = this[key]?.asList()

inline fun ReadTagMutableMap.listMut(key: String): MutableTagList {
    return Maps.compute(this, key) { _, value ->
        when (value) {
            is TagList -> value.toMutTag()
            is MutableTagList -> value
            else -> MutableTagList()
        }
    } as MutableTagList
}

fun MutableTag.toTag(): Tag = when (this) {
    is Tag -> this
    is MutableTagMap -> toTag()
    is MutableTagList -> toTag()
    else -> throw IOException("Invalid type: ${this::class}")
}

fun MutableTagMap.toTag() = TagMap {
    for ((key, value) in this@toTag) {
        this[key] = value.toTag()
    }
}

fun MutableTagList.toTag() = TagList {
    for (element in this@toTag) {
        add(element)
    }
}

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Tag) = TagList {
    elements.forEach { add(it) }
}

fun UUID(map: ReadTagMutableMap): UUID? {
    val most = map["Most"]?.toLong()
    val least = map["Least"]?.toLong()
    if (most == null || least == null) {
        return null
    }
    return UUID(most, least)
}

fun UUID(str: String): UUID? {
    try {
        return UUID.fromString(str)
    } catch (e: IllegalArgumentException) {
        return null
    }
}

inline fun MutableTag.toUUID(): UUID? {
    toMap()?.let(::UUID)?.let { return it }
    toString().let(::UUID)?.let { return it }
    return null
}
