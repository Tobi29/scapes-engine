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

import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.sliceOver
import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.stdex.ConcurrentMap

actual sealed class MutableTag {
    actual abstract val value: Any

    override fun equals(other: Any?) = when (other) {
        is MutableTag -> value == other.value
        else -> false
    }

    override fun hashCode() = value.hashCode()

    override fun toString() = value.toString()
}

actual sealed class Tag : MutableTag()

actual sealed class TagPrimitive : Tag()

actual object TagUnit : TagPrimitive() {
    actual override val value get() = Unit
}

actual sealed class TagBoolean : TagPrimitive() {
    actual abstract override val value: Boolean

    actual object True : TagBoolean() {
        override val value get() = true
    }

    actual object False : TagBoolean() {
        override val value get() = false
    }
}

actual sealed class TagNumber : TagPrimitive() {
    actual abstract override val value: Number

    override fun equals(other: Any?): Boolean {
        if (other is TagNumber) {
            return compareNumbers(value, other.value)
        }
        return super.equals(other)
    }

    override fun hashCode() = toInt()
}

actual abstract class TagInteger internal actual constructor() : TagNumber()

actual abstract class TagDecimal internal actual constructor() : TagNumber()

actual class TagByte actual constructor(
    actual override val value: Byte
) : TagInteger()

actual class TagShort actual constructor(
    actual override val value: Short
) : TagInteger()

actual class TagInt actual constructor(
    actual override val value: Int
) : TagInteger()

actual class TagLong actual constructor(
    actual override val value: Long
) : TagInteger()

actual class TagFloat actual constructor(
    actual override val value: Float
) : TagDecimal()

actual class TagDouble actual constructor(
    actual override val value: Double
) : TagDecimal()

actual class TagByteArray actual constructor(
    actual override val value: BytesRO
) : TagPrimitive() {
    actual constructor(value: ByteArray) : this(value.sliceOver())

    override fun equals(other: Any?): Boolean {
        if (other is TagByteArray) {
            return value == other.value
        } else if (other is TagList) {
            if (value.size != other.value.size) {
                return false
            }
            return value.asSequence().zip(other.value.asSequence())
                .all { (a, b) ->
                    compareNumbers(a, b.value)
                }
        } else if (other is MutableTagList) {
            if (value.size != other.value.size) {
                return false
            }
            return value.asSequence().zip(other.value.asSequence())
                .all { (a, b) ->
                    compareNumbers(a, b.value)
                }
        }
        return false
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return value.joinToString(prefix = "[", postfix = "]")
    }
}

actual class TagString actual constructor(
    actual override val value: String
) : TagPrimitive()

// Need dummy parameter to allow having a function as a "constructor"
// We assume TagMap instances are always immutable
@Suppress("UNUSED_PARAMETER", "EqualsOrHashCode")
actual class TagMap @PublishedApi internal actual constructor(
    unit: Unit,
    actual override val value: Map<String, Tag>
) : Tag(), Map<String, Tag> by value {
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

actual class MutableTagMap internal actual constructor(
    actual override val value: ConcurrentHashMap<String, MutableTag>
) : MutableTag(), ConcurrentMap<String, MutableTag> by value

// The hashCode() of the alternative checks should be identical
// Need dummy parameter to allow having a function as a "constructor"
@Suppress("EqualsOrHashCode", "UNUSED_PARAMETER")
actual class TagList @PublishedApi internal actual constructor(
    unit: Unit,
    actual override val value: List<Tag>
) : Tag(), List<Tag> by value {
    override fun equals(other: Any?) = when (other) {
        is TagList -> value == other.value
        is MutableTagList -> value == other.value
        is TagByteArray -> {
            value.size == other.value.size &&
                    value.asSequence().zip(other.value.asSequence())
                        .all { (a, b) -> compareNumbers(a.value, b) }
        }
        else -> false
    }
}

// The hashCode() of the alternative checks should be identical
@Suppress("EqualsOrHashCode")
actual class MutableTagList internal actual constructor(
    actual override val value: MutableList<MutableTag>
) : MutableTag(), MutableList<MutableTag> by value {
    override fun equals(other: Any?) = when (other) {
        is TagList -> value == other.value
        is MutableTagList -> value == other.value
        is TagByteArray -> {
            value.size == other.value.size &&
                    value.asSequence().zip(other.value.asSequence())
                        .all { (a, b) -> compareNumbers(a.value, b) }
        }
        else -> false
    }
}

internal actual fun convertNumberToType(
    type: Number,
    convert: Number
): Number {
    return when (type) {
        is Byte -> convert.toByte()
        is Short -> convert.toShort()
        is Int -> convert.toInt()
        is Long -> convert.toLong()
        is Float -> convert.toFloat()
        is Double -> convert.toDouble()
        else -> throw IllegalArgumentException(
            "Invalid number type: ${type::class}"
        )
    }
}
