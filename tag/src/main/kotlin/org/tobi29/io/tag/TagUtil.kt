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

package org.tobi29.io.tag

import org.tobi29.arrays.readAsByteArray
import org.tobi29.arrays.toByteArray
import org.tobi29.stdex.InlineUtility
import org.tobi29.stdex.computeAlways

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun MutableTag.toBoolean() = value as? Boolean

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun MutableTag.toNumber() = value as? Number

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun MutableTag.toByte() = toNumber()?.toByte()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun MutableTag.toShort() = toNumber()?.toShort()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun MutableTag.toInt() = toNumber()?.toInt()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun MutableTag.toLong() = toNumber()?.toLong()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun MutableTag.toFloat() = toNumber()?.toFloat()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun MutableTag.toDouble() = toNumber()?.toDouble()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun TagNumber.toByte() = value.toByte()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun TagNumber.toShort() = value.toShort()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun TagNumber.toInt() = value.toInt()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun TagNumber.toLong() = value.toLong()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun TagNumber.toFloat() = value.toFloat()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun TagNumber.toDouble() = value.toDouble()

fun MutableTag.toMap(): TagMap? = when (this) {
    is TagMap -> this
    is MutableTagMap -> toTag()
    else -> null
}

fun MutableTag.asMap(): ReadTagMutableMap? = when (this) {
    is TagMap -> this
    is MutableTagMap -> this
    else -> null
}

fun MutableTag.toList(): List<Tag>? = when (this) {
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
    is TagByteArray -> value.toByteArray()
    is TagList -> {
        ByteArray(size).also { array ->
            for ((i, element) in this.withIndex()) {
                array[i] = element.toByte() ?: return null
            }
        }
    }
    else -> null
}

fun MutableTag.readAsByteArray() = when (this) {
    is TagByteArray -> value.readAsByteArray()
    else -> toByteArray()
}

fun Sequence<MutableTag>.toTag() = TagList {
    this@toTag.forEach { add(it.toTag()) }
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun ReadTagMutableMap.map(key: String) = this[key]?.asMap()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun ReadWriteTagMutableMap.mapMut(key: String): MutableTagMap {
    return computeAlways(key) { _, value ->
        when (value) {
            is TagMap -> value.toMutTag()
            is MutableTagMap -> value
            else -> MutableTagMap()
        }
    } as MutableTagMap
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun ReadTagMutableMap.list(key: String) = this[key]?.asList()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun ReadWriteTagMutableMap.listMut(key: String): MutableTagList {
    return computeAlways(key) { _, value ->
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
    else -> error("Impossible")
}

fun MutableTagMap.toTag() = TagMap {
    for ((key, value) in this@toTag) {
        this[key] = value.toTag()
    }
}

fun MutableTagList.toTag() = TagList {
    for (element in this@toTag) {
        add(element.toTag())
    }
}

/** Returns a new read-only [TagList] of given element */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun tagListOf(vararg elements: Tag) = TagList {
    elements.forEach { add(it) }
}
