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

package org.tobi29.scapes.engine.utils.tag

import org.tobi29.scapes.engine.utils.ConcurrentHashMap
import org.tobi29.scapes.engine.utils.ConcurrentMap
import org.tobi29.scapes.engine.utils.readOnly
import org.tobi29.scapes.engine.utils.synchronized

/**
 * Type shared by all tags
 */
sealed class MutableTag {
    /**
     * Returns the value of the tag
     */
    abstract val value: Any

    override fun equals(other: Any?) = when (other) {
        is MutableTag -> value == other.value
        else -> false
    }

    override fun hashCode() = value.hashCode()

    override fun toString() = value.toString()
}

/**
 * Type shared by all immutable tags
 */
sealed class Tag : MutableTag()

/**
 * Type shared by all primitive tags
 */
sealed class TagPrimitive : Tag()

/**
 * Tag for storing an [Unit]
 */
object TagUnit : TagPrimitive() {
    override val value get() = Unit
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

sealed class TagBoolean : TagPrimitive() {
    override abstract val value: Boolean

    object True : TagBoolean() {
        override val value get() = true
    }

    object False : TagBoolean() {
        override val value get() = false
    }
}

/**
 * Get the tag for the given value
 */
fun TagBoolean(value: Boolean) = if (value) TagBoolean.True else TagBoolean.False

/**
 * Get the tag for the given value
 */
inline fun Boolean.toTag() = TagBoolean(this)

sealed class TagNumber : TagPrimitive() {
    override abstract val value: Number

    override fun equals(other: Any?): Boolean {
        if (other is TagNumber) {
            return compareNumbers(value, other.value)
        }
        return super.equals(other)
    }

    override fun hashCode() = toInt()
}

abstract class TagInteger internal constructor() : TagNumber()

abstract class TagDecimal internal constructor() : TagNumber()

/**
 * Tag for storing a [kotlin.Byte]
 */
class TagByte
/**
 * Get the tag for the given value
 */
(override val value: Byte) : TagInteger()

/**
 * Get the tag for the given value
 */
inline fun Byte.toTag() = TagByte(this)

/**
 * Tag for storing a [kotlin.Short]
 */
class TagShort
/**
 * Get the tag for the given value
 */
(override val value: Short) : TagInteger()

/**
 * Get the tag for the given value
 */
inline fun Short.toTag() = TagShort(this)

/**
 * Tag for storing an [kotlin.Int]
 */
class TagInt
/**
 * Get the tag for the given value
 */
(override val value: Int) : TagInteger()

/**
 * Get the tag for the given value
 */
inline fun Int.toTag() = TagInt(this)

/**
 * Tag for storing a [kotlin.Long]
 */
class TagLong
/**
 * Get the tag for the given value
 */
(override val value: Long) : TagInteger()

/**
 * Get the tag for the given value
 */
inline fun Long.toTag() = TagLong(this)

/**
 * Tag for storing a [kotlin.Float]
 */
class TagFloat
/**
 * Get the tag for the given value
 */
(override val value: Float) : TagDecimal()

/**
 * Get the tag for the given value
 */
inline fun Float.toTag() = TagFloat(this)

/**
 * Tag for storing a [kotlin.Double]
 */
class TagDouble
/**
 * Get the tag for the given value
 */
(override val value: Double) : TagDecimal()

/**
 * Get the tag for the given value
 */
inline fun Double.toTag() = TagDouble(this)

/**
 * Tag for storing a [ByteArray]
 */
class TagByteArray
/**
 * Get the tag for the given value
 */
(internal val valueMut: ByteArray) : TagPrimitive() {
    override val value get() = valueMut.copyOf()

    override fun equals(other: Any?): Boolean {
        if (other is TagByteArray) {
            return valueMut contentEquals other.valueMut
        } else if (other is TagList) {
            if (valueMut.size != other.value.size) {
                return false
            }
            return valueMut.asSequence().zip(other.value.asSequence())
                    .all { (a, b) -> compareNumbers(a, b.value) }
        } else if (other is MutableTagList) {
            if (valueMut.size != other.value.size) {
                return false
            }
            return valueMut.asSequence().zip(other.value.asSequence())
                    .all { (a, b) -> compareNumbers(a, b.value) }
        }
        return false
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }

    override fun toString(): String {
        return value.joinToString(prefix = "[", postfix = "]")
    }
}

/**
 * Get the tag for the given value
 */
inline fun ByteArray.toTag() = TagByteArray(this)

/**
 * Tag for storing a [kotlin.String]
 */
class TagString
/**
 * Get the tag for the given value
 */
(override val value: String) : TagPrimitive()

/**
 * Get the tag for the given value
 */
inline fun String.toTag() = TagString(this)

typealias ReadTagMap = Map<String, Tag>

typealias ReadWriteTagMap = MutableMap<String, Tag>

typealias ReadTagMutableMap = Map<String, MutableTag>

typealias ReadWriteTagMutableMap = MutableMap<String, MutableTag>

// Need dummy parameter to allow having a function as a "constructor"
// We assume TagMap instances are always immutable
@Suppress("UNUSED_PARAMETER", "EqualsOrHashCode")
class TagMap internal constructor(unit: Unit,
                                  override val value: Map<String, Tag>) : Tag(), Map<String, Tag> by value {
    // ConcurrentHashMap checks values multiple times causing massive slowdown
    // with deeply nested structures
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TagMap) return value == other
        if (value.size != other.value.size) return false
        return value.all { (key, value) ->
            value == other.value[key]
        }
    }
}

fun TagMap(block: MutableMap<String, Tag>.() -> Unit): TagMap = TagMap(
        Unit, ConcurrentHashMap<String, Tag>().apply(block).readOnly())

fun TagMap(value: Map<String, Tag> = emptyMap()) = TagMap {
    putAll(value)
}

fun ReadTagMap.toTag() = TagMap(this)

class MutableTagMap internal constructor(override val value: ConcurrentHashMap<String, MutableTag>) : MutableTag(), ConcurrentMap<String, MutableTag> by value

fun MutableTagMap(block: MutableMap<String, MutableTag>.() -> Unit) = MutableTagMap(
        ConcurrentHashMap<String, MutableTag>().apply(block))

fun MutableTagMap(value: Map<String, MutableTag> = emptyMap()) = MutableTagMap {
    putAll(value)
}

fun ReadTagMap.toMutTag() = MutableTagMap(this)

typealias ReadTagList = List<Tag>

typealias ReadWriteTagList = MutableList<Tag>

typealias ReadTagMutableList = List<MutableTag>

typealias ReadWriteTagMutableList = MutableList<MutableTag>

// The hashCode() of the alternative checks should be identical
// Need dummy parameter to allow having a function as a "constructor"
@Suppress("EqualsOrHashCode", "UNUSED_PARAMETER")
class TagList internal constructor(unit: Unit,
                                   override val value: List<Tag>) : Tag(), List<Tag> by value {
    override fun equals(other: Any?): Boolean {
        if (other is TagList) {
            return value == other.value
        } else if (other is MutableTagList) {
            return value == other.value
        } else if (other is TagByteArray) {
            if (value.size != other.valueMut.size) {
                return false
            }
            return value.asSequence().zip(other.valueMut.asSequence())
                    .all { (a, b) -> compareNumbers(a.value, b) }
        }
        return false
    }
}

fun TagList(block: MutableList<Tag>.() -> Unit): TagList = TagList(Unit,
        ArrayList<Tag>().apply(block).readOnly())

fun TagList(value: List<Tag> = emptyList()) = TagList { addAll(value) }

fun TagList(value: Sequence<Tag>) = TagList { value.forEach { add(it) } }

fun ReadTagList.toTag() = TagList(this)

// The hashCode() of the alternative checks should be identical
@Suppress("EqualsOrHashCode")
class MutableTagList internal constructor(override val value: MutableList<MutableTag>) : MutableTag(), MutableList<MutableTag> by value {
    override fun equals(other: Any?): Boolean {
        if (other is TagList) {
            return value == other.value
        } else if (other is MutableTagList) {
            return value == other.value
        } else if (other is TagByteArray) {
            if (value.size != other.valueMut.size) {
                return false
            }
            return value.asSequence().zip(other.valueMut.asSequence())
                    .all { (a, b) -> compareNumbers(a.value, b) }
        }
        return false
    }
}

fun MutableTagList(block: MutableList<MutableTag>.() -> Unit) = MutableTagList(
        ArrayList<MutableTag>().apply(block).synchronized())

fun MutableTagList(value: List<MutableTag> = emptyList()) = MutableTagList {
    addAll(value)
}

fun List<Tag>.toMutTag() = MutableTagList(this)

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

private fun compareNumbers(first: Any,
                           second: Any) = if (first is Number && second is Number) {
    compareNumbers(first, second)
} else {
    false
}

private fun compareNumbers(first: Number,
                           second: Number): Boolean {
    return compareNumbersSameType(convertNumberToType(first, second),
            first) && compareNumbersSameType(
            convertNumberToType(second, first), second)
}

private fun compareNumbersSameType(first: Number,
                                   second: Number): Boolean {
    if (first is Float && second is Float) {
        return first == second || first.isNaN() && second.isNaN()
    } else if (first is Double && second is Double) {
        return first == second || first.isNaN() && second.isNaN()
    }
    return first == second
}
