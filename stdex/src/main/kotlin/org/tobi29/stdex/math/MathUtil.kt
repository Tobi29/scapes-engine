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

package org.tobi29.stdex.math

import org.tobi29.stdex.Constant
import org.tobi29.stdex.InlineUtility
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

/**
 * Estimate for `pi * 0.5`
 */
@Constant
inline val HALF_PI
    get() = PI * 0.5

/**
 * Estimate for `pi * 2.0`
 * Equivalent to the maximum of a full circle in radians
 */
@Constant
inline val TWO_PI
    get() = PI * 2.0

/**
 * Converts radians to degrees by multiplying
 */
@Constant
inline val RAD_2_DEG
    get() = 180.0 / PI

/**
 * Converts degrees into radians by multiplying
 */
@Constant
inline val DEG_2_RAD
    get() = PI / 180.0

/**
 * Returns [value2] if [value1] is less than [value2], [value3] if [value1] is
 * greater than [value3] or otherwise [value2]
 * @param value1 The value
 * @param value2 The minimum value
 * @param value3 The maximum value
 * @return [value1] forced into range between [value2] and [value3]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun clamp(value1: Int, value2: Int, value3: Int): Int =
    max(value2, min(value3, value1))

/**
 * Returns [value2] if [value1] is less than [value2], [value3] if [value1] is
 * greater than [value3] or otherwise [value2]
 * @param value1 The value
 * @param value2 The minimum value
 * @param value3 The maximum value
 * @return [value1] forced into range between [value2] and [value3]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun clamp(value1: Long, value2: Long, value3: Long): Long =
    max(value2, min(value3, value1))

/**
 * Returns [value2] if [value1] is less than [value2], [value3] if [value1] is
 * greater than [value3] or otherwise [value2]
 * @param value1 The value
 * @param value2 The minimum value
 * @param value3 The maximum value
 * @return [value1] forced into range between [value2] and [value3]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun clamp(value1: Float, value2: Float, value3: Float): Float =
    max(value2, min(value3, value1))

/**
 * Returns [value2] if [value1] is less than [value2], [value3] if [value1] is
 * greater than [value3] or otherwise [value2]
 * @param value1 The value
 * @param value2 The minimum value
 * @param value3 The maximum value
 * @return [value1] forced into range between [value2] and [value3]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun clamp(value1: Double, value2: Double, value3: Double): Double =
    max(value2, min(value3, value1))

/**
 * Returns [value1] and [value2] mixed together using [ratio]
 *
 * Passing a [ratio] of `0.0` will make it return [value1] and `1.0` [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @param ratio The ratio, should be in range `0.0` to `1.0`
 * @return [value1] and [value2] mixed together
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun mix(value1: Float, value2: Float, ratio: Float): Float =
    (1.0f - ratio) * value1 + ratio * value2


/**
 * Returns [value1] and [value2] mixed together using [ratio]
 *
 * Passing a [ratio] of `0.0` will make it return [value1] and `1.0` [value2]
 * @param value1 The first value
 * @param value2 The second value
 * @param ratio The ratio, should be in range `0.0` to `1.0`
 * @return [value1] and [value2] mixed together
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun mix(value1: Double, value2: Double, ratio: Double): Double =
    (1.0 - ratio) * value1 + ratio * value2

/**
 * Returns the square value of [value]
 * @param value The value
 * @return Square value of [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun sqr(value: Int): Int = value * value

/**
 * Returns the square value of [value]
 * @param value The value
 * @return Square value of [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun sqr(value: Long): Long = value * value

/**
 * Returns the square value of [value]
 * @param value The value
 * @return Square value of [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun sqr(value: Float): Float = value * value

/**
 * Returns the square value of [value]
 * @param value The value
 * @return Square value of [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun sqr(value: Double): Double = value * value

/**
 * Returns the square value of [value], negative if [value] is negative
 * @param value The value
 * @return Square value of [value], negative if [value] is negative
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun sqrNoAbs(value: Int): Int {
    val sqr = sqr(value)
    return if (value < 0) -sqr else sqr
}

/**
 * Returns the square value of [value], negative if [value] is negative
 * @param value The value
 * @return Square value of [value], negative if [value] is negative
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun sqrNoAbs(value: Long): Long {
    val sqr = sqr(value)
    return if (value < 0) -sqr else sqr
}

/**
 * Returns the square value of [value], negative if [value] is negative
 * @param value The value
 * @return Square value of [value], negative if [value] is negative
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun sqrNoAbs(value: Double): Double {
    val sqr = sqr(value)
    return if (value < 0) -sqr else sqr
}

/**
 * Returns the square value of [value], negative if [value] is negative
 * @param value The value
 * @return Square value of [value], negative if [value] is negative
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun sqrNoAbs(value: Float): Float {
    val sqr = sqr(value)
    return if (value < 0) -sqr else sqr
}

/**
 * Returns the square value of [value]
 * @param value The value
 * @return Square value of [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun cbe(value: Int): Int = value * value * value

/**
 * Returns the cube value of [value]
 * @param value The value
 * @return Cube value of [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun cbe(value: Long): Long = value * value * value

/**
 * Returns the cube value of [value]
 * @param value The value
 * @return Cube value of [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun cbe(value: Float): Float = value * value * value

/**
 * Returns the cube value of [value]
 * @param value The value
 * @return Cube value of [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun cbe(value: Double): Double = value * value * value

/**
 * Computes the logarithm with base 2 of [value]
 * @param value The value
 * @return Returns the logarithm with base 2 of [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun lb(value: Int): Int {
    if (value == 0) {
        throw IllegalArgumentException("Calling lb on 0 is not allowed")
    }
    return 31 - clz(value)
}

/**
 * Computes the modulus of the given number and [value]
 * Unlike the normal modulo operator this always returns a positive value
 * @param value The divisor
 * @receiver The dividend
 * @return Returns the modulus of the given number and [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline infix fun Float.remP(value: Float): Float {
    val mod = this % value
    return if (mod < 0.0f) {
        mod + value
    } else {
        mod
    }
}

/**
 * Computes the modulus of the given number and [value]
 * Unlike the normal modulo operator this always returns a positive value
 * @param value The divisor
 * @receiver The dividend
 * @return Returns the modulus of the given number and [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline infix fun Double.remP(value: Double): Double {
    val mod = this % value
    return if (mod < 0.0) {
        mod + value
    } else {
        mod
    }
}

/**
 * Computes the modulus of the given number and [value]
 * Unlike the normal modulo operator this always returns a positive value
 * @param value The divisor
 * @receiver The dividend
 * @return Returns the modulus of the given number and [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline infix fun Int.remP(value: Int): Int {
    val mod = this % value
    return if (mod < 0) {
        mod + value
    } else {
        mod
    }
}

/**
 * Computes the modulus of the given number and [value]
 * Unlike the normal modulo operator this always returns a positive value
 * @param value The divisor
 * @receiver The dividend
 * @return Returns the modulus of the given number and [value]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline infix fun Long.remP(value: Long): Long {
    val mod = this % value
    return if (mod < 0L) {
        mod + value
    } else {
        mod
    }
}

/**
 * Returns the cube-root value of [value]
 * @param value The value
 * @return Cube-root value of [value]
 */
expect fun cbrt(value: Float): Float

/**
 * Returns the cube-root value of [value]
 * @param value The value
 * @return Cube-root value of [value]
 */
expect fun cbrt(value: Double): Double

/**
 * Returns the next integer below the given value
 * @receiver value The value
 * @return Next integer below the given value
 */
expect fun Float.floorToInt(): Int

/**
 * Returns the next integer below the given value
 * @receiver The value
 * @return Next integer below the given value
 */
expect fun Float.floorToLong(): Long

/**
 * Returns the next integer below the given value
 * @receiver value The value
 * @return Next integer below the given value
 */
expect fun Double.floorToInt(): Int

/**
 * Returns the next integer below the given value
 * @receiver The value
 * @return Next integer below the given value
 */
expect fun Double.floorToLong(): Long

/**
 * Returns the next integer above the given value
 * @receiver value The value
 * @return Next integer above the given value
 */
expect fun Float.ceilToInt(): Int

/**
 * Returns the next integer above the given value
 * @receiver The value
 * @return Next integer above the given value
 */
expect fun Float.ceilToLong(): Long

/**
 * Returns the next integer above the given value
 * @receiver value The value
 * @return Next integer above the given value
 */
expect fun Double.ceilToInt(): Int

/**
 * Returns the next integer above the given value
 * @receiver The value
 * @return Next integer above the given value
 */
expect fun Double.ceilToLong(): Long

/**
 * Counts the amount of leading zeros
 * @param value The value
 * @return The amount of leading zeros in range 0..32
 */
expect fun clz(value: Int): Int

/**
 * Converts the [Float] from degrees into radians
 * @return Value of the [Float] in radians
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Float.toRad(): Float = this * DEG_2_RAD.toFloat()

/**
 * Converts the [Double] from degrees into radians
 * @return Value of the [Double] in radians
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Double.toRad(): Double = this * DEG_2_RAD

/**
 * Converts the [Float] from radians into degrees
 * @return Value of the [Float] in degrees
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Float.toDeg(): Float = this * RAD_2_DEG.toFloat()

/**
 * Converts the [Double] from radians into degrees
 * @return Value of the [Double] in degrees
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Double.toDeg(): Double = this * RAD_2_DEG
