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

package org.tobi29.math

import org.tobi29.math.vector.*
import org.tobi29.stdex.InlineUtility
import org.tobi29.stdex.math.clamp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Returns the absolute values of [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun abs(value: ReadVector2i): Vector2i =
    value.map { abs(it) }

/**
 * Returns the absolute values of [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun abs(value: ReadVector2d): Vector2d =
    value.map { abs(it) }

/**
 * Returns the absolute values of [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun abs(value: ReadVector3i): Vector3i =
    value.map { abs(it) }

/**
 * Returns the absolute values of [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun abs(value: ReadVector3d): Vector3d =
    value.map { abs(it) }

/**
 * Returns the smallest value in [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun min(value: ReadVector2i): Int =
    min(value.x, value.y)

/**
 * Returns the smallest value in [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun min(value: ReadVector2d): Double =
    min(value.x, value.y)

/**
 * Returns the smallest value in [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun min(value: ReadVector3i): Int =
    min(min(value.x, value.y), value.z)

/**
 * Returns the smallest value in [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun min(value: ReadVector3d): Double =
    min(min(value.x, value.y), value.z)

/**
 * Returns the smaller values between [value1] and [value2]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun min(value1: ReadVector2i, value2: ReadVector2i): Vector2i =
    Vector2i(
        min(value1.x, value2.x),
        min(value1.y, value2.y)
    )

/**
 * Returns the smaller values between [value1] and [value2]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun min(value1: ReadVector2d, value2: ReadVector2d): Vector2d =
    Vector2d(
        min(value1.x, value2.x),
        min(value1.y, value2.y)
    )

/**
 * Returns the smaller values between [value1] and [value2]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun min(value1: ReadVector3i, value2: ReadVector3i): Vector3i =
    Vector3i(
        min(value1.x, value2.x),
        min(value1.y, value2.y),
        min(value1.z, value2.z)
    )

/**
 * Returns the smaller values between [value1] and [value2]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun min(value1: ReadVector3d, value2: ReadVector3d): Vector3d =
    Vector3d(
        min(value1.x, value2.x),
        min(value1.y, value2.y),
        min(value1.z, value2.z)
    )

/**
 * Returns the greatest value in [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun max(value: ReadVector2i): Int =
    max(value.x, value.y)

/**
 * Returns the greatest value in [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun max(value: ReadVector2d): Double =
    max(value.x, value.y)

/**
 * Returns the greatest value in [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun max(value: ReadVector3i): Int =
    max(max(value.x, value.y), value.z)

/**
 * Returns the greatest value in [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun max(value: ReadVector3d): Double =
    max(max(value.x, value.y), value.z)

/**
 * Returns the greater values between [value1] and [value2]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun max(value1: ReadVector2i, value2: ReadVector2i): Vector2i =
    Vector2i(
        max(value1.x, value2.x),
        max(value1.y, value2.y)
    )

/**
 * Returns the greater values between [value1] and [value2]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun max(value1: ReadVector2d, value2: ReadVector2d): Vector2d =
    Vector2d(
        max(value1.x, value2.x),
        max(value1.y, value2.y)
    )

/**
 * Returns the greater values between [value1] and [value2]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun max(value1: ReadVector3i, value2: ReadVector3i): Vector3i =
    Vector3i(
        max(value1.x, value2.x),
        max(value1.y, value2.y),
        max(value1.z, value2.z)
    )

/**
 * Returns the greater values between [value1] and [value2]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun max(value1: ReadVector3d, value2: ReadVector3d): Vector3d =
    Vector3d(
        max(value1.x, value2.x),
        max(value1.y, value2.y),
        max(value1.z, value2.z)
    )

/**
 * Returns the clamped value of x between y and z in [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun clamp(value: ReadVector3i): Int =
    clamp(value.x, value.y, value.z)

/**
 * Returns the clamped value of x between y and z in [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun clamp(value: ReadVector3d): Double =
    clamp(value.x, value.y, value.z)

/**
 * Returns the clamped value of [value1] between [value2] and [value3] for
 * each element
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun clamp(
    value1: ReadVector2i, value2: ReadVector2i, value3: ReadVector2i
): Vector2i = Vector2i(
    clamp(value1.x, value2.x, value3.x),
    clamp(value1.y, value2.y, value3.y)
)

/**
 * Returns the clamped value of [value1] between [value2] and [value3] for
 * each element
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun clamp(
    value1: ReadVector2d, value2: ReadVector2d, value3: ReadVector2d
): Vector2d = Vector2d(
    clamp(value1.x, value2.x, value3.x),
    clamp(value1.y, value2.y, value3.y)
)

/**
 * Returns the clamped value of [value1] between [value2] and [value3] for
 * each element
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun clamp(
    value1: ReadVector3i, value2: ReadVector3i, value3: ReadVector3i
): Vector3i = Vector3i(
    clamp(value1.x, value2.x, value3.x),
    clamp(value1.y, value2.y, value3.y),
    clamp(value1.z, value2.z, value3.z)
)

/**
 * Returns the clamped value of [value1] between [value2] and [value3] for
 * each element
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun clamp(
    value1: ReadVector3d, value2: ReadVector3d, value3: ReadVector3d
): Vector3d = Vector3d(
    clamp(value1.x, value2.x, value3.x),
    clamp(value1.y, value2.y, value3.y),
    clamp(value1.z, value2.z, value3.z)
)

/**
 * Computes the asin of [value] using a less accurate table
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun asinTable(value: Float): Float {
    return AsinTable.asin(value)
}

/**
 * Computes the asin of [value] using a less accurate table
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun asinTable(value: Double): Double {
    return AsinTable.asin(value)
}

/**
 * Computes the acos of [value] using a less accurate table
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun acosTable(value: Float): Float {
    return AsinTable.acos(value)
}

/**
 * Computes the acos of [value] using a less accurate table
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun acosTable(value: Double): Double {
    return AsinTable.acos(value)
}

/**
 * Computes the atan2 of [value1] and [value2] using a less accurate table
 * @param value1 The first value
 * @param value2 The second value
 * @return The atan2 of [value1] and [value2]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun atan2Table(
    value1: Float,
    value2: Float
): Float {
    return Atan2Table.atan2(value1, value2)
}

/**
 * Computes the atan2 of [value1] and [value2] using a less accurate table
 * @param value1 The first value
 * @param value2 The second value
 * @return The atan2 of [value1] and [value2]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun atan2Table(
    value1: Double,
    value2: Double
): Double {
    return Atan2Table.atan2(value1, value2)
}

/**
 * Computes the difference between the angles [value1] and [value2]
 * @return Returns the difference in range `-180.0` and `180.0`
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun angleDiff(value1: Float, value2: Float): Float =
    diff(value1, value2, 360.0f)

/**
 * Computes the difference between the angles [value1] and [value2]
 * @return Returns the difference in range `-180.0` and `180.0`
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun angleDiff(value1: Double, value2: Double): Double =
    diff(value1, value2, 360.0)

/**
 * Computes the difference between [value1] and [value2] with the assumption
 * that they wrap around at [modulus], useful for computing differences of
 * angles
 * @return Returns the difference in range `-[modulus] / 2` and `[modulus] / 2`
 */
fun diff(value1: Float, value2: Float, modulus: Float): Float {
    var diff = (value2 - value1) % modulus
    val h = modulus * 0.5f
    while (diff > h) {
        diff -= modulus
    }
    while (diff <= -h) {
        diff += modulus
    }
    return diff
}

/**
 * Computes the difference between [value1] and [value2] with the assumption
 * that they wrap around at [modulus], useful for computing differences of
 * angles
 * @return Returns the difference in range `-[modulus] / 2` and `[modulus] / 2`
 */
fun diff(value1: Double, value2: Double, modulus: Double): Double {
    var diff = (value2 - value1) % modulus
    val h = modulus * 0.5
    while (diff > h) {
        diff -= modulus
    }
    while (diff <= -h) {
        diff += modulus
    }
    return diff
}

/**
 * Moves values between `0.0` and `1.0` into range `margin` and
 * `1.0 - margin` by scaling and offsetting them
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun margin(value: Float, margin: Float): Float =
    margin + value * (1.0f - margin * 2.0f)

/**
 * Moves values between `0.0` and `1.0` into range `margin` and
 * `1.0 - margin` by scaling and offsetting them
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun margin(value: Double, margin: Double): Double =
    margin + value * (1.0 - margin * 2.0)

/**
 * Computes the next higher power of two for the given value
 */
fun nextPowerOfTwo(value: Int): Int {
    var output = value - 1
    output = output or (output shr 1)
    output = output or (output shr 2)
    output = output or (output shr 4)
    output = output or (output shr 8)
    output = output or (output shr 16)
    return output + 1
}

/**
 * Converts a 32-bit IEEE 754 floating point number to a 16-bit one
 */
fun Float.toHalfFloatShort(): Short {
    val bits = toRawBits()
    val sign = bits.ushr(16) and 0x8000
    var value = (bits and 0x7fffffff) + 0x1000
    if (value >= 0x47800000) {
        if (bits and 0x7fffffff >= 0x47800000) {
            if (value < 0x7f800000) {
                return (sign or 0x7c00).toShort()
            }
            return (sign or 0x7c00 or (bits and 0x007fffff).ushr(
                13
            )).toShort()
        }
        return (sign or 0x7bff).toShort()
    }
    if (value >= 0x38800000) {
        return (sign or (value - 0x38000000).ushr(13)).toShort()
    }
    if (value < 0x33000000) {
        return sign.toShort()
    }
    value = (bits and 0x7fffffff).ushr(23)
    return (sign or ((bits and 0x7fffff or 0x800000) + 0x800000.ushr(
        value - 102
    )).ushr(126 - value)).toShort()
}
