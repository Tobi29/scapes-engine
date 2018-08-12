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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.io.tag

import org.tobi29.arrays.BytesRO
import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.stdex.ConcurrentMap
import org.tobi29.stdex.readOnly
import org.tobi29.stdex.synchronized

/**
 * Type shared by all tags
 */
expect sealed class MutableTag {
    /**
     * Returns the value of the tag
     */
    abstract val value: Any
}

/**
 * Type shared by all immutable tags
 */
expect sealed class Tag : MutableTag

/**
 * Type shared by all primitive tags
 */
expect sealed class TagPrimitive : Tag

/**
 * Tag for storing an [Unit]
 */
expect object TagUnit : TagPrimitive {
    override val value: Unit
}

expect sealed class TagBoolean : TagPrimitive {
    abstract override val value: Boolean

    object True : TagBoolean

    object False : TagBoolean
}

expect sealed class TagNumber : TagPrimitive {
    abstract override val value: Number
}

expect abstract class TagInteger internal constructor() : TagNumber

expect abstract class TagDecimal internal constructor() : TagNumber

/**
 * Tag for storing a [kotlin.Byte]
 */
expect class TagByte(value: Byte) : TagInteger {
    override val value: Byte
}

/**
 * Tag for storing a [kotlin.Short]
 */
expect class TagShort(value: Short) : TagInteger {
    override val value: Short
}

/**
 * Tag for storing an [kotlin.Int]
 */
expect class TagInt(value: Int) : TagInteger {
    override val value: Int
}

/**
 * Tag for storing a [kotlin.Long]
 */
expect class TagLong(value: Long) : TagInteger {
    override val value: Long
}

/**
 * Tag for storing a [kotlin.Float]
 */
expect class TagFloat(value: Float) : TagDecimal {
    override val value: Float
}

/**
 * Tag for storing a [kotlin.Double]
 */
expect class TagDouble(value: Double) : TagDecimal {
    override val value: Double
}

/**
 * Tag for storing a [ByteArray]
 */
expect class TagByteArray(value: BytesRO) : TagPrimitive {
    constructor(value: ByteArray)

    override val value: BytesRO
}

/**
 * Tag for storing a [kotlin.String]
 */
expect class TagString(value: String) : TagPrimitive {
    override val value: String
}

expect class TagMap @PublishedApi internal constructor(
    unit: Unit,
    value: Map<String, Tag>
) : Tag, Map<String, Tag> {
    override val value: Map<String, Tag>
}

expect class MutableTagMap internal constructor(
    value: ConcurrentHashMap<String, MutableTag>
) : MutableTag, ConcurrentMap<String, MutableTag> {
    override val value: ConcurrentHashMap<String, MutableTag>
}

expect class TagList @PublishedApi internal constructor(
    unit: Unit,
    value: List<Tag>
) : Tag, List<Tag> {
    override val value: List<Tag>
}

expect class MutableTagList internal constructor(
    value: MutableList<MutableTag>
) : MutableTag, MutableList<MutableTag> {
    override val value: MutableList<MutableTag>
}

/**
 * Get the tag for the given value
 */
@Suppress("UNUSED_PARAMETER")
fun TagUnit(value: Unit) = TagUnit

/**
 * Get the tag for the given value
 */
inline fun Unit.toTag() = TagUnit(this)

/**
 * Get the tag for the given value
 */
fun TagBoolean(value: Boolean) =
    if (value) TagBoolean.True else TagBoolean.False

/**
 * Get the tag for the given value
 */
inline fun Boolean.toTag() = TagBoolean(this)

/**
 * Get the tag for the given value
 */
inline fun Byte.toTag() = TagByte(this)

/**
 * Get the tag for the given value
 */
inline fun Short.toTag() = TagShort(this)

/**
 * Get the tag for the given value
 */
inline fun Int.toTag() = TagInt(this)

/**
 * Get the tag for the given value
 */
inline fun Long.toTag() = TagLong(this)

/**
 * Get the tag for the given value
 */
inline fun Float.toTag() = TagFloat(this)

/**
 * Get the tag for the given value
 */
inline fun Double.toTag() = TagDouble(this)

/**
 * Get the tag for the given value
 */
inline fun BytesRO.toTag() = TagByteArray(this)

/**
 * Get the tag for the given value
 */
inline fun ByteArray.toTag() = TagByteArray(this)

/**
 * Get the tag for the given value
 */
inline fun String.toTag() = TagString(this)

typealias ReadTagMap = Map<String, Tag>

typealias ReadWriteTagMap = MutableMap<String, Tag>

typealias ReadTagMutableMap = Map<String, MutableTag>

typealias ReadWriteTagMutableMap = MutableMap<String, MutableTag>

inline fun TagMap(block: MutableMap<String, Tag>.() -> Unit): TagMap =
    TagMap(Unit, ConcurrentHashMap<String, Tag>().apply(block).readOnly())

fun TagMap(value: Map<String, Tag> = emptyMap()): TagMap = TagMap {
    putAll(value)
}

fun ReadTagMap.toTag(): TagMap = TagMap(this)

fun MutableTagMap(block: MutableMap<String, MutableTag>.() -> Unit): MutableTagMap =
    MutableTagMap(ConcurrentHashMap<String, MutableTag>().apply(block))

fun MutableTagMap(value: Map<String, MutableTag> = emptyMap()): MutableTagMap =
    MutableTagMap { putAll(value) }

fun ReadTagMap.toMutTag(): MutableTagMap = MutableTagMap(this)

typealias ReadTagList = List<Tag>

typealias ReadWriteTagList = MutableList<Tag>

typealias ReadTagMutableList = List<MutableTag>

typealias ReadWriteTagMutableList = MutableList<MutableTag>

inline fun TagList(block: MutableList<Tag>.() -> Unit): TagList =
    TagList(Unit, ArrayList<Tag>().apply(block).readOnly())

fun TagList(value: List<Tag> = emptyList()): TagList =
    TagList { addAll(value) }

fun TagList(value: Sequence<Tag>): TagList =
    TagList { value.forEach { add(it) } }

fun ReadTagList.toTag(): TagList = TagList(this)

fun MutableTagList(block: MutableList<MutableTag>.() -> Unit): MutableTagList =
    MutableTagList(ArrayList<MutableTag>().apply(block).synchronized())

fun MutableTagList(value: List<MutableTag> = emptyList()): MutableTagList =
    MutableTagList { addAll(value) }

fun List<Tag>.toMutTag(): MutableTagList = MutableTagList(this)

interface TagWrite {
    fun toTag(): Tag
}

interface TagMapWrite : TagWrite {
    fun write(map: ReadWriteTagMap)

    override fun toTag() = TagMap { write(this) }
}

interface TagListWrite : TagWrite {
    fun write(list: MutableList<Tag>)

    override fun toTag() = TagList { write(this) }
}

internal fun compareNumbers(first: Any, second: Any) =
    if (first is Number && second is Number) compareNumbers(first, second)
    else false

private fun compareNumbers(first: Number, second: Number): Boolean =
    compareNumbersSameType(convertNumberToType(first, second), first) &&
            compareNumbersSameType(convertNumberToType(second, first), second)

private fun compareNumbersSameType(first: Number, second: Number): Boolean {
    if (first is Float && second is Float) {
        return first == second || first.isNaN() && second.isNaN()
    } else if (first is Double && second is Double) {
        return first == second || first.isNaN() && second.isNaN()
    }
    return first == second
}

internal expect fun convertNumberToType(type: Number, convert: Number): Number
