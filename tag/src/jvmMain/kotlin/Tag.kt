/*
 * Copyright 2012-2019 Tobi29
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

@file:JvmName("TagJVMKt")

package org.tobi29.io.tag

import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.asIterable
import org.tobi29.arrays.asSequence
import org.tobi29.arrays.sliceOver
import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.stdex.ConcurrentMap
import org.tobi29.stdex.JvmStatic
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger

actual sealed class MutableTag {
    actual abstract val value: Any

    override fun equals(other: Any?) = when (other) {
        is MutableTag -> value == other.value
        else -> false
    }

    override fun hashCode() = value.hashCode()

    override fun toString() = value.toString()
}

actual sealed class Tag : MutableTag(), Serializable

actual sealed class TagPrimitive : Tag() {
    companion object {
        @JvmStatic
        val serialVersionUID = -2475234891118926228L
    }
}

actual object TagUnit : TagPrimitive() {
    actual override val value get() = Unit
    @JvmStatic
    val serialVersionUID = -5369451749866803356L
}

actual sealed class TagBoolean : TagPrimitive() {
    actual abstract override val value: Boolean

    actual object True : TagBoolean() {
        override val value get() = true
        @JvmStatic
        val serialVersionUID = 7990636346201599705L
    }

    actual object False : TagBoolean() {
        override val value get() = false
        @JvmStatic
        val serialVersionUID = -7901220856926138444L
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
) : TagInteger() {
    companion object {
        @JvmStatic
        val serialVersionUID = 2390053412981975486L
    }
}

actual class TagShort actual constructor(
    actual override val value: Short
) : TagInteger() {
    companion object {
        @JvmStatic
        val serialVersionUID = -6922708837260681233L
    }
}

actual class TagInt actual constructor(
    actual override val value: Int
) : TagInteger() {
    companion object {
        @JvmStatic
        val serialVersionUID = -249870242253757835L
    }
}

actual class TagLong actual constructor(
    actual override val value: Long
) : TagInteger() {
    companion object {
        @JvmStatic
        val serialVersionUID = 7503919212315682021L
    }
}

actual class TagFloat actual constructor(
    actual override val value: Float
) : TagDecimal() {
    companion object {
        @JvmStatic
        val serialVersionUID = 751169379483013951L
    }
}

actual class TagDouble actual constructor(
    actual override val value: Double
) : TagDecimal() {
    companion object {
        @JvmStatic
        val serialVersionUID = -1216034762779183955L
    }
}

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
        return value.asIterable().joinToString(prefix = "[", postfix = "]")
    }

    companion object {
        @JvmStatic
        val serialVersionUID = 2874195371702503711L
    }
}

actual class TagString actual constructor(
    actual override val value: String
) : TagPrimitive() {
    companion object {
        @JvmStatic
        val serialVersionUID = 8904382934285309746L
    }
}

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

    companion object {
        @JvmStatic
        val serialVersionUID = 6497328703409250822L
    }
}

actual class MutableTagMap internal actual constructor(
    actual override val value: ConcurrentHashMap<String, MutableTag>
) : MutableTag(), ConcurrentMap<String, MutableTag> by value {
    companion object {
        @JvmStatic
        val serialVersionUID = -537574595235747087L
    }
}

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

    companion object {
        @JvmStatic
        val serialVersionUID = 5283135699965364527L
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

    companion object {
        @JvmStatic
        val serialVersionUID = -7805353035746471424L
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
        is BigInteger -> try {
            BigInteger(convert.toString())
        } catch (e: NumberFormatException) {
            BigDecimal(convert.toString()).toBigInteger()
        }
        is BigDecimal -> BigDecimal(convert.toString())
        else -> throw IllegalArgumentException(
            "Invalid number type: ${type::class}"
        )
    }
}
