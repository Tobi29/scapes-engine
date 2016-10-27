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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils.math

import org.tobi29.scapes.engine.utils.math.vector.Vector2d
import org.tobi29.scapes.engine.utils.math.vector.Vector3d

/**
 * Estimate for `e`
 */
val E = Math.E

/**
 * Estimate for `pi * 0.5`
 */
val HALF_PI = Math.PI * 0.5

/**
 * Estimate for `pi`
 */
val PI = Math.PI

/**
 * Estimate for `pi * 2.0`
 * Equivalent to the maximum of a full circle in radians
 */
val TWO_PI = Math.PI * 2.0

/**
 * Converts radians to degrees by multiplying
 */
val RAD_2_DEG = 180.0 / PI

/**
 * Converts degrees into radians by multiplying
 */
val DEG_2_RAD = PI / 180.0

/**
 * Returns the absolute values of [value]
 * @param value The value
 * @return [Vector2d] with absolute values of [value]
 */
inline fun abs(value: Vector2d): Vector2d {
    return Vector2d(abs(value.x), abs(value.y))
}

/**
 * Returns the absolute values of [value]
 * @param value The value
 * @return [Vector3d] with absolute values of [value]
 */
inline fun abs(value: Vector3d): Vector3d {
    return Vector3d(abs(value.x), abs(value.y),
            abs(value.z))
}

/**
 * Returns the absolute value of [value]
 * @param value The value
 * @return Absolute value of [value]
 */
inline fun abs(value: Int): Int {
    return Math.abs(value)
}

/**
 * Returns the absolute value of [value]
 * @param value The value
 * @return Absolute value of [value]
 */
inline fun abs(value: Long): Long {
    return Math.abs(value)
}

/**
 * Returns the absolute value of [value]
 * @param value The value
 * @return Absolute value of [value]
 */
inline fun abs(value: Float): Float {
    return Math.abs(value)
}

/**
 * Returns the absolute value of [value]
 * @param value The value
 * @return Absolute value of [value]
 */
inline fun abs(value: Double): Double {
    return Math.abs(value)
}

/**
 * Returns the smallest value in [value]
 * @param value The values
 * @return Smallest value in [value]
 */
inline fun min(value: Vector2d): Double {
    return min(value.x, value.y)
}

/**
 * Returns the smaller values between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return [Vector2d] with smaller values between [value1] and [value2]
 */
inline fun min(value1: Vector2d,
               value2: Vector2d): Vector2d {
    return Vector2d(min(value1.x, value2.x),
            min(value1.y, value2.y))
}

/**
 * Returns the smaller values between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return [Vector3d] with smaller values between [value1] and [value2]
 */
inline fun min(value1: Vector3d,
               value2: Vector3d): Vector3d {
    return Vector3d(min(value1.x, value2.x),
            min(value1.y, value2.y),
            min(value1.z, value2.z))
}

/**
 * Returns the smaller value between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return Smaller value between [value1] and [value2]
 */
inline fun min(value1: Int,
               value2: Int): Int {
    return if (value1 < value2) value1 else value2
}

/**
 * Returns the smaller value between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return Smaller value between [value1] and [value2]
 */
inline fun min(value1: Long,
               value2: Long): Long {
    return if (value1 < value2) value1 else value2
}

/**
 * Returns the smaller value between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return Smaller value between [value1] and [value2]
 */
inline fun min(value1: Float,
               value2: Float): Float {
    return if (value1 < value2) value1 else value2
}

/**
 * Returns the smaller value between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return Smaller value between [value1] and [value2]
 */
inline fun min(value1: Double,
               value2: Double): Double {
    return if (value1 < value2) value1 else value2
}

/**
 * Returns the greatest value in [value]
 * @param value The values
 * @return Smallest value in [value]
 */
inline fun max(value: Vector2d): Double {
    return max(value.x, value.y)
}

/**
 * Returns the greatest value in [value]
 * @param value The values
 * @return Smallest value in [value]
 */
inline fun max(value: Vector3d): Double {
    return max(max(value.x, value.y), value.z)
}

/**
 * Returns the greater values between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return [Vector2d] with greater values between [value1] and [value2]
 */
inline fun max(value1: Vector2d,
               value2: Vector2d): Vector2d {
    return Vector2d(max(value1.x, value2.x),
            max(value1.y, value2.y))
}

/**
 * Returns the greater values between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return [Vector3d] with greater values between [value1] and [value2]
 */
inline fun max(value1: Vector3d,
               value2: Vector3d): Vector3d {
    return Vector3d(max(value1.x, value2.x),
            max(value1.y, value2.y),
            max(value1.z, value2.z))
}

/**
 * Returns the greater value between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return Greater value between [value1] and [value2]
 */
inline fun max(value1: Int,
               value2: Int): Int {
    return if (value1 > value2) value1 else value2
}

/**
 * Returns the greater value between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return Greater value between [value1] and [value2]
 */
inline fun max(value1: Long,
               value2: Long): Long {
    return if (value1 > value2) value1 else value2
}

/**
 * Returns the greater value between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return Greater value between [value1] and [value2]
 */
inline fun max(value1: Float,
               value2: Float): Float {
    return if (value1 > value2) value1 else value2
}

/**
 * Returns the greater value between [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return Greater value between [value1] and [value2]
 */
inline fun max(value1: Double,
               value2: Double): Double {
    return if (value1 > value2) value1 else value2
}

/**
 * Returns the clamped value of x in [value] between y in [value] and z in
 * [value]
 * @param value The values
 * @return Clamped value of x in [value]
 */
inline fun clamp(value: Vector3d): Double {
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
inline fun clamp(value1: Vector2d,
                 value2: Vector2d,
                 value3: Vector2d): Vector2d {
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
inline fun clamp(value1: Vector3d,
                 value2: Vector3d,
                 value3: Vector3d): Vector3d {
    return Vector3d(clamp(value1.x, value2.x, value3.x),
            clamp(value1.y, value2.y, value3.y),
            clamp(value1.z, value2.z, value3.z))
}

/**
 * Returns [value2] if [value1] is less than [value2], [value3] if [value1] is
 * greater than [value3] or otherwise [value2]
 * @param value1 The value
 * @param value2 The minimum value
 * @param value3 The maximum value
 * @return [value1] forced into range between [value2] and [value3]
 */
inline fun clamp(value1: Int,
                 value2: Int,
                 value3: Int): Int {
    return max(value2, min(value3, value1))
}

/**
 * Returns [value2] if [value1] is less than [value2], [value3] if [value1] is
 * greater than [value3] or otherwise [value2]
 * @param value1 The value
 * @param value2 The minimum value
 * @param value3 The maximum value
 * @return [value1] forced into range between [value2] and [value3]
 */
inline fun clamp(value1: Long,
                 value2: Long,
                 value3: Long): Long {
    return max(value2, min(value3, value1))
}

/**
 * Returns [value2] if [value1] is less than [value2], [value3] if [value1] is
 * greater than [value3] or otherwise [value2]
 * @param value1 The value
 * @param value2 The minimum value
 * @param value3 The maximum value
 * @return [value1] forced into range between [value2] and [value3]
 */
inline fun clamp(value1: Float,
                 value2: Float,
                 value3: Float): Float {
    return max(value2, min(value3, value1))
}

/**
 * Returns [value2] if [value1] is less than [value2], [value3] if [value1] is
 * greater than [value3] or otherwise [value2]
 * @param value1 The value
 * @param value2 The minimum value
 * @param value3 The maximum value
 * @return [value1] forced into range between [value2] and [value3]
 */
inline fun clamp(value1: Double,
                 value2: Double,
                 value3: Double): Double {
    return max(value2, min(value3, value1))
}

/**
 * Returns [value1] and [value2] mixed together using [ratio]
 *
 * Passing a [ratio] of `0.0` will make it return [value1] and `1.0` [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @param ratio The ratio, should be in range `0.0` to `1.0`
 * @return [value1] and [value2] mixed together
 */
inline fun mix(value1: Float,
               value2: Float,
               ratio: Float): Float {
    return (1.0f - ratio) * value1 + ratio * value2
}

/**
 * Returns [value1] and [value2] mixed together using [ratio]
 *
 * Passing a [ratio] of `0.0` will make it return [value1] and `1.0` [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @param ratio The ratio, should be in range `0.0` to `1.0`
 * @return [value1] and [value2] mixed together
 */
inline fun mix(value1: Double,
               value2: Double,
               ratio: Double): Double {
    return (1.0 - ratio) * value1 + ratio * value2
}

/**
 * Returns the square value of [value]
 * @param value The value
 * @return Square value of [value]
 */
inline fun sqr(value: Int): Int {
    return value * value
}

/**
 * Returns the square value of [value]
 * @param value The value
 * @return Square value of [value]
 */
inline fun sqr(value: Long): Long {
    return value * value
}

/**
 * Returns the square value of [value]
 * @param value The value
 * @return Square value of [value]
 */
inline fun sqr(value: Float): Float {
    return value * value
}

/**
 * Returns the square value of [value]
 * @param value The value
 * @return Square value of [value]
 */
inline fun sqr(value: Double): Double {
    return value * value
}

/**
 * Returns the square value of [value], negative if [value] is negative
 * @param value The value
 * @return Square value of [value], negative if [value] is negative
 */
inline fun sqrNoAbs(value: Int): Int {
    val sqr = sqr(value)
    return if (value < 0) -sqr else sqr
}

/**
 * Returns the square value of [value], negative if [value] is negative
 * @param value The value
 * @return Square value of [value], negative if [value] is negative
 */
inline fun sqrNoAbs(value: Long): Long {
    val sqr = sqr(value)
    return if (value < 0) -sqr else sqr
}

/**
 * Returns the square value of [value], negative if [value] is negative
 * @param value The value
 * @return Square value of [value], negative if [value] is negative
 */
inline fun sqrNoAbs(value: Double): Double {
    val sqr = sqr(value)
    return if (value < 0) -sqr else sqr
}

/**
 * Returns the square value of [value], negative if [value] is negative
 * @param value The value
 * @return Square value of [value], negative if [value] is negative
 */
inline fun sqrNoAbs(value: Float): Float {
    val sqr = sqr(value)
    return if (value < 0) -sqr else sqr
}

/**
 * Returns the square value of [value]
 * @param value The value
 * @return Square value of [value]
 */
inline fun cbe(value: Int): Int {
    return value * value * value
}

/**
 * Returns the cube value of [value]
 * @param value The value
 * @return Cube value of [value]
 */
inline fun cbe(value: Long): Long {
    return value * value * value
}

/**
 * Returns the cube value of [value]
 * @param value The value
 * @return Cube value of [value]
 */
inline fun cbe(value: Float): Float {
    return value * value * value
}

/**
 * Returns the cube value of [value]
 * @param value The value
 * @return Cube value of [value]
 */
inline fun cbe(value: Double): Double {
    return value * value * value
}

/**
 * Returns the square-root value of [value]
 * @param value The value
 * @return Square-root value of [value]
 */
inline fun sqrt(value: Float): Float {
    return sqrt(value.toDouble()).toFloat()
}

/**
 * Returns the square-root value of [value]
 * @param value The value
 * @return Square-root value of [value]
 */
inline fun sqrt(value: Double): Double {
    return Math.sqrt(value)
}

/**
 * Returns the cube-root value of [value]
 * @param value The value
 * @return Cube-root value of [value]
 */
inline fun cbrt(value: Float): Float {
    return cbrt(value.toDouble()).toFloat()
}

/**
 * Returns the cube-root value of [value]
 * @param value The value
 * @return Cube-root value of [value]
 */
inline fun cbrt(value: Double): Double {
    return Math.cbrt(value)
}

/**
 * Returns the next integer below [value]
 * @param value The value
 * @return Next integer below [value]
 */
inline fun floor(value: Float): Int {
    val int = value.toInt()
    return if (value >= 0.0f || int.toFloat() == value) int else int - 1
}

/**
 * Returns the next integer below [value]
 * @param value The value
 * @return Next integer below [value]
 */
inline fun floor(value: Double): Int {
    return Math.floor(value).toInt()
}

/**
 * Returns the nearest integer [value]
 * @param value The value
 * @return Nearest integer below [value]
 */
inline fun round(value: Float): Int {
    return round(value.toDouble())
}

/**
 * Returns the nearest integer [value]
 * @param value The value
 * @return Nearest integer below [value]
 */
inline fun round(value: Double): Int {
    return Math.round(value).toInt()
}

/**
 * Returns the next integer above [value]
 * @param value The value
 * @return Next integer above [value]
 */
inline fun ceil(value: Float): Int {
    val int = value.toInt()
    return if (value <= 0.0f || int.toFloat() == value) int else int + 1
}

/**
 * Returns the next integer above [value]
 * @param value The value
 * @return Next integer above [value]
 */
inline fun ceil(value: Double): Int {
    return Math.ceil(value).toInt()
}

/**
 * Converts the [Float] from degrees into radians
 * @return Value of the [Float] in radians
 */
inline fun Float.toRad(): Float {
    return toDouble().toRad().toFloat()
}

/**
 * Converts the [Double] from degrees into radians
 * @return Value of the [Double] in radians
 */
inline fun Double.toRad(): Double {
    return this * DEG_2_RAD
}

/**
 * Converts the [Float] from radians into degrees
 * @return Value of the [Float] in degrees
 */
inline fun Float.toDeg(): Float {
    return toDouble().toDeg().toFloat()
}

/**
 * Converts the [Double] from radians into degrees
 * @return Value of the [Double] in degrees
 */
inline fun Double.toDeg(): Double {
    return this * RAD_2_DEG
}

/**
 * Computes the sin of [value]
 * @param value Value in radians
 * @return Result between `-1.0` and `1.0`
 */
inline fun sin(value: Float): Float {
    return sin(value.toDouble()).toFloat()
}

/**
 * Computes the sin of [value]
 * @param value Value in radians
 * @return Result between `-1.0` and `1.0`
 */
inline fun sin(value: Double): Double {
    return Math.sin(value)
}

/**
 * Computes the sin of [value] using a less accurate table
 * @param value Value in radians
 * @return Result between `-1.0` and `1.0`
 */
inline fun sinTable(value: Float): Float {
    return sinTable(value.toDouble()).toFloat()
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
 * Computes the asin of [value]
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
inline fun asin(value: Float): Float {
    return asin(value.toDouble()).toFloat()
}

/**
 * Computes the asin of [value]
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
inline fun asin(value: Double): Double {
    return Math.sin(value)
}

/**
 * Computes the asin of [value] using a less accurate table
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
inline fun asinTable(value: Float): Float {
    return asinTable(value.toDouble()).toFloat()
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
 * Computes the cos of [value]
 * @param value Value in radians
 * @return Result between `-1.0` and `1.0`
 */
inline fun cos(value: Float): Float {
    return cos(value.toDouble()).toFloat()
}

/**
 * Computes the cos of [value]
 * @param value Value in radians
 * @return Result between `-1.0` and `1.0`
 */
inline fun cos(value: Double): Double {
    return Math.cos(value)
}

/**
 * Computes the cos of [value] using a less accurate table
 * @param value Value in radians
 * @return Result between `-1.0` and `1.0`
 */
inline fun cosTable(value: Float): Float {
    return cosTable(value.toDouble()).toFloat()
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
 * Computes the acos of [value]
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
inline fun acos(value: Float): Float {
    return acos(value.toDouble()).toFloat()
}

/**
 * Computes the acos of [value]
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
inline fun acos(value: Double): Double {
    return Math.cos(value)
}

/**
 * Computes the acos of [value] using a less accurate table
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
inline fun acosTable(value: Float): Float {
    return acosTable(value.toDouble()).toFloat()
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
 * Computes the tan of [value]
 * @param value Value in radians
 * @return Result between `-1.0` and `1.0`
 */
inline fun tan(value: Float): Float {
    return tan(value.toDouble()).toFloat()
}

/**
 * Computes the tan of [value]
 * @param value Value in radians
 * @return Result between `-1.0` and `1.0`
 */
inline fun tan(value: Double): Double {
    return Math.tan(value)
}

/**
 * Computes the tanh of [value]
 * @param value The value to use
 * @return Result between `-1.0` and `1.0`
 */
inline fun tanh(value: Float): Float {
    return tanh(value.toDouble()).toFloat()
}

/**
 * Computes the tanh of [value]
 * @param value The value to use
 * @return Result between `-1.0` and `1.0`
 */
inline fun tanh(value: Double): Double {
    return Math.tanh(value)
}

/**
 * Computes the atan of [value]
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
inline fun atan(value: Float): Float {
    return atan(value.toDouble()).toFloat()
}

/**
 * Computes the atan of [value]
 * @param value Value between `-1.0` and `1.0`
 * @return Result between `0.0` and `pi` in radians or `NaN` if an invalid [value] was passed
 */
inline fun atan(value: Double): Double {
    return Math.tan(value)
}

/**
 * Computes the atan2 of [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return The atan2 of [value1] and [value2]
 */
inline fun atan2(value1: Float,
                 value2: Float): Float {
    return atan2(value1.toDouble(), value2.toDouble()).toFloat()
}

/**
 * Computes the atan2 of [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return The atan2 of [value1] and [value2]
 */
inline fun atan2(value1: Double,
                 value2: Double): Double {
    return Math.atan2(value1, value2)
}

/**
 * Computes the atan2 of [value1] and [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @return The atan2 of [value1] and [value2]
 */
inline fun atan2Fast(value1: Float,
                     value2: Float): Float {
    return atan2Fast(value1.toDouble(), value2.toDouble()).toFloat()
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
 * Computes [value1] to the power of [value2]
 * @param value1 The base value
 * @param value2 The exponent
 * @return [value1] to the power of [value2]
 */
inline fun pow(value1: Float,
               value2: Float): Float {
    return pow(value1.toDouble(), value2.toDouble()).toFloat()
}

/**
 * Computes [value1] to the power of [value2]
 * @param value1 The base value
 * @param value2 The exponent
 * @return [value1] to the power of [value2]
 */
inline fun pow(value1: Double,
               value2: Double): Double {
    return Math.pow(value1, value2)
}

/**
 * Computes the difference between the angles [value1] and [value2]
 * @param value1 The first value in degrees
 * @param value2 The second value in degrees
 * @return Returns the difference in range `-180.0` and `180.0`
 */
inline fun angleDiff(value1: Float,
                     value2: Float): Float {
    return angleDiff(value1.toDouble(), value2.toDouble()).toFloat()
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
    return diff(value1.toDouble(), value2.toDouble(),
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
 * Computes the logarithm with base 2 of [value]
 * @param value The value
 * @return Returns the logarithm with base 2 of [value]
 */
inline fun lb(value: Int): Int {
    return FastMath.lb(value)
}
