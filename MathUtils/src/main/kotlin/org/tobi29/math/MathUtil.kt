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

package org.tobi29.math

import org.tobi29.math.vector.*
import org.tobi29.stdex.math.clamp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Returns the absolute values of [value]
 * @param value The value
 * @return [Vector2d] with absolute values of [value]
 */
inline fun abs(value: ReadVector2d): Vector2d {
    return Vector2d(abs(value.x), abs(value.y))
}

/**
 * Returns the absolute values of [value]
 * @param value The value
 * @return [Vector3d] with absolute values of [value]
 */
inline fun abs(value: ReadVector3d): Vector3d {
    return Vector3d(abs(value.x), abs(value.y), abs(value.z))
}

/**
 * Returns the smallest value in [value]
 * @param value The values
 * @return Smallest value in [value]
 */
inline fun min(value: ReadVector2d): Double {
    return min(value.x, value.y)
}

/**
 * Returns the smaller values between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return [Vector2d] with smaller values between [value1] and [value2]
 */
inline fun min(value1: ReadVector2d,
               value2: ReadVector2d): Vector2d {
    return Vector2d(min(value1.x, value2.x),
            min(value1.y, value2.y))
}

/**
 * Returns the smaller values between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return [Vector3d] with smaller values between [value1] and [value2]
 */
inline fun min(value1: ReadVector3d,
               value2: ReadVector3d): Vector3d {
    return Vector3d(min(value1.x, value2.x),
            min(value1.y, value2.y),
            min(value1.z, value2.z))
}

/**
 * Returns the greatest value in [value]
 * @param value The values
 * @return Smallest value in [value]
 */
inline fun max(value: ReadVector2d): Double {
    return max(value.x, value.y)
}

/**
 * Returns the greatest value in [value]
 * @param value The values
 * @return Smallest value in [value]
 */
inline fun max(value: ReadVector3d): Double {
    return max(max(value.x, value.y), value.z)
}

/**
 * Returns the greater values between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return [Vector2d] with greater values between [value1] and [value2]
 */
inline fun max(value1: ReadVector2d,
               value2: ReadVector2d): Vector2d {
    return Vector2d(max(value1.x, value2.x),
            max(value1.y, value2.y))
}

/**
 * Returns the greater values between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return [Vector3d] with greater values between [value1] and [value2]
 */
inline fun max(value1: ReadVector3d,
               value2: ReadVector3d): Vector3d {
    return Vector3d(max(value1.x, value2.x),
            max(value1.y, value2.y),
            max(value1.z, value2.z))
}

/**
 * Returns the greater values between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return [Vector2i] with greater values between [value1] and [value2]
 */
inline fun max(value1: ReadVector2i,
               value2: ReadVector2i): Vector2i {
    return Vector2i(max(value1.x, value2.x),
            max(value1.y, value2.y))
}

/**
 * Returns the greater values between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return [Vector3i] with greater values between [value1] and [value2]
 */
inline fun max(value1: ReadVector3i,
               value2: ReadVector3i): Vector3i {
    return Vector3i(max(value1.x, value2.x),
            max(value1.y, value2.y),
            max(value1.z, value2.z))
}

/**
 * Returns the clamped value of x in [value] between y in [value] and z in
 * [value]
 * @param value The values
 * @return Clamped value of x in [value]
 */
inline fun clamp(value: ReadVector3d): Double {
    return clamp(value.x, value.y, value.z)
}

/**
 * Returns [value2] if [value1] is less than [value2], [value3] if [value1] is
 * greater than [value3] or otherwise [value2]
 * @param value1 The value
 * @param value2 The minimum value
 * @param value3 The maximum value
 * @return [Vector2d] with [value1] forced into range between [value2] and [value3]
 */
inline fun clamp(value1: ReadVector2d,
                 value2: ReadVector2d,
                 value3: ReadVector2d): Vector2d {
    return Vector2d(clamp(value1.x, value2.x, value3.x),
            clamp(value1.y, value2.y, value3.y))
}

/**
 * Returns [value2] if [value1] is less than [value2], [value3] if [value1] is
 * greater than [value3] or otherwise [value2]
 * @param value1 The value
 * @param value2 The minimum value
 * @param value3 The maximum value
 * @return [Vector3d] with [value1] forced into range between [value2] and [value3]
 */
inline fun clamp(value1: ReadVector3d,
                 value2: ReadVector3d,
                 value3: ReadVector3d): Vector3d {
    return Vector3d(clamp(value1.x, value2.x, value3.x),
            clamp(value1.y, value2.y, value3.y),
            clamp(value1.z, value2.z, value3.z))
}

/**
 * Computes the sin of [value] using a less accurate table
 * @param value Value in radians
 * @return Result between `-1.0` and `1.0`
 */
inline fun sinTable(value: Float): Float {
    return FastSin.sin(value)
}

/**
 * Computes the sin of [value] using a less accurate table
 * @param value Value in radians
 * @return Result between `-1.0` and `1.0`
 */
inline fun sinTable(value: Double): Double {
    return FastSin.sin(value)
}

/**
 * Computes the asin of [value] using a less accurate table
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
inline fun asinTable(value: Float): Float {
    return FastAsin.asin(value)
}

/**
 * Computes the asin of [value] using a less accurate table
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
inline fun asinTable(value: Double): Double {
    return FastAsin.asin(value)
}

/**
 * Computes the cos of [value] using a less accurate table
 * @param value Value in radians
 * @return Result between `-1.0` and `1.0`
 */
inline fun cosTable(value: Float): Float {
    return FastSin.cos(value)
}

/**
 * Computes the cos of [value] using a less accurate table
 * @param value Value in radians
 * @return Result between `-1.0` and `1.0`
 */
inline fun cosTable(value: Double): Double {
    return FastSin.cos(value)
}

/**
 * Computes the acos of [value] using a less accurate table
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
inline fun acosTable(value: Float): Float {
    return FastAsin.acos(value)
}

/**
 * Computes the acos of [value] using a less accurate table
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
inline fun acosTable(value: Double): Double {
    return FastAsin.acos(value)
}

/**
 * Computes the atan2 of [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return The atan2 of [value1] and [value2]
 */
inline fun atan2Fast(value1: Float,
                     value2: Float): Float {
    return atan2Fast(value1.toDouble(),
            value2.toDouble()).toFloat()
}

/**
 * Computes the atan2 of [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return The atan2 of [value1] and [value2]
 */
inline fun atan2Fast(value1: Double,
                     value2: Double): Double {
    return FastAtan2.atan2(value1, value2)
}

/**
 * Computes the difference between the angles [value1] and [value2]
 * @param value1 The first value in degrees
 * @param value2 The second value in degrees
 * @return Returns the difference in range `-180.0` and `180.0`
 */
inline fun angleDiff(value1: Float,
                     value2: Float): Float {
    return angleDiff(value1.toDouble(),
            value2.toDouble()).toFloat()
}

/**
 * Computes the difference between the angles [value1] and [value2]
 * @param value1 The first value in degrees
 * @param value2 The second value in degrees
 * @return Returns the difference in range `-180.0` and `180.0`
 */
inline fun angleDiff(value1: Double,
                     value2: Double): Double {
    return diff(value1, value2, 360.0)
}

/**
 * Computes the difference between [value1] and [value2] with the assumption
 * that they wrap around at [modulus], useful for computing differences of
 * angles
 * @param value1 The first value
 * @param value2 The second value
 * @param modulus The modulus to use
 * @return Returns the difference in range `-[modulus] / 2` and `[modulus] / 2`
 */
inline fun diff(value1: Float,
                value2: Float,
                modulus: Float): Float {
    return diff(value1.toDouble(),
            value2.toDouble(),
            modulus.toDouble()).toFloat()
}

/**
 * Computes the difference between [value1] and [value2] with the assumption
 * that they wrap around at [modulus], useful for computing differences of
 * angles
 * @param value1 The first value
 * @param value2 The second value
 * @param modulus The modulus to use
 * @return Returns the difference in range `-[modulus] / 2` and `[modulus] / 2`
 */
inline fun diff(value1: Double,
                value2: Double,
                modulus: Double): Double {
    return FastMath.diff(value1, value2, modulus)
}

/**
 * Moves values between `0.0` and `1.0` into range `margin` and
 * `1.0 - margin` by scaling and offsetting them
 * @param value The value to transform
 * @param margin The margin on the sides
 * @return Returns the value after the linear transformation
 */
inline fun margin(value: Float,
                  margin: Float): Float {
    return margin + value * (1.0f - margin * 2.0f)
}

/**
 * Moves values between `0.0` and `1.0` into range `margin` and
 * `1.0 - margin` by scaling and offsetting them
 * @param value The value to transform
 * @param margin The margin on the sides
 * @return Returns the value after the linear transformation
 */
inline fun margin(value: Double,
                  margin: Double): Double {
    return margin + value * (1.0 - margin * 2.0)
}

/**
 * Computes the next higher power of two for the given value
 * @param value The value
 * @return Returns the smallest higher power of two greater or equal to value
 */
inline fun nextPowerOfTwo(value: Int): Int {
    return FastMath.nextPowerOfTwo(value)
}
