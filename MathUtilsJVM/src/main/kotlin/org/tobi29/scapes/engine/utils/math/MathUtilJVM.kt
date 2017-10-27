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

package org.tobi29.scapes.engine.utils.math

import org.tobi29.scapes.engine.utils.toIntClamped

actual inline fun abs(value: Int) = Math.abs(value)

actual inline fun abs(value: Long) = Math.abs(value)

actual inline fun abs(value: Float) = Math.abs(value)

actual inline fun abs(value: Double) = Math.abs(value)

actual inline fun min(value1: Int,
                    value2: Int) = Math.min(value1, value2)

actual inline fun min(value1: Long,
                    value2: Long) = Math.min(value1, value2)

actual inline fun min(value1: Float,
                    value2: Float) = Math.min(value1, value2)

actual inline fun min(value1: Double,
                    value2: Double) = Math.min(value1, value2)

actual inline fun max(value1: Int,
                    value2: Int) = Math.max(value1, value2)

actual inline fun max(value1: Long,
                    value2: Long) = Math.max(value1, value2)

actual inline fun max(value1: Float,
                    value2: Float) = Math.max(value1, value2)

actual inline fun max(value1: Double,
                    value2: Double) = Math.max(value1, value2)

actual inline fun sqrt(value: Float) = sqrt(value.toDouble()).toFloat()

actual inline fun sqrt(value: Double) = Math.sqrt(value)

actual inline fun cbrt(value: Float) = cbrt(value.toDouble()).toFloat()

actual inline fun cbrt(value: Double) = Math.cbrt(value)

actual inline fun floor(value: Float): Int {
    val int = value.toInt()
    return if (value >= 0.0f || int.toFloat() == value) int else int - 1
}

actual inline fun floor(value: Double): Int {
    val int = value.toInt()
    return if (value >= 0.0 || int.toDouble() == value) int else int - 1
}

actual inline fun floorL(value: Double): Long {
    val int = value.toLong()
    return if (value >= 0.0 || int.toDouble() == value) int else int - 1
}

actual inline fun floorD(value: Double) = Math.floor(value)

actual inline fun round(value: Float) = round(value.toDouble())

actual inline fun round(value: Double) = roundL(value).toIntClamped()

actual inline fun roundL(value: Double) = Math.round(value)

actual inline fun roundD(value: Double) =
        if (value > Long.MAX_VALUE || value < Long.MIN_VALUE) value
        else roundL(value).toDouble()

actual inline fun ceil(value: Float): Int {
    val int = value.toInt()
    return if (value <= 0.0f || int.toFloat() == value) int else int + 1
}

actual inline fun ceil(value: Double): Int {
    val int = value.toInt()
    return if (value <= 0.0 || int.toDouble() == value) int else int + 1
}

actual inline fun ceilL(value: Double): Long {
    val int = value.toLong()
    return if (value <= 0.0 || int.toDouble() == value) int else int + 1
}

actual inline fun ceilD(value: Double) = Math.ceil(value)


actual inline fun sin(value: Float) = sin(value.toDouble()).toFloat()

actual inline fun sin(value: Double) = Math.sin(value)

actual inline fun asin(value: Float) = asin(value.toDouble()).toFloat()

actual inline fun asin(value: Double) = Math.asin(value)

actual inline fun cos(value: Float) = cos(value.toDouble()).toFloat()

actual inline fun cos(value: Double) = Math.cos(value)

actual inline fun acos(value: Float) = acos(value.toDouble()).toFloat()

actual inline fun acos(value: Double) = Math.acos(value)

actual inline fun tan(value: Float) = tan(value.toDouble()).toFloat()

actual inline fun tan(value: Double) = Math.tan(value)

actual inline fun tanh(value: Float) = tanh(value.toDouble()).toFloat()

actual inline fun tanh(value: Double) = Math.tanh(value)

actual inline fun atan(value: Float) = atan(value.toDouble()).toFloat()

actual inline fun atan(value: Double) = Math.atan(value)

actual inline fun atan2(value1: Float,
                      value2: Float) =
        atan2(value1.toDouble(), value2.toDouble()).toFloat()

actual inline fun atan2(value1: Double,
                      value2: Double) = Math.atan2(value1, value2)

actual inline fun pow(value1: Float,
                    value2: Float) =
        pow(value1.toDouble(), value2.toDouble()).toFloat()

actual inline fun pow(value1: Double,
                    value2: Double) = Math.pow(value1, value2)

actual inline fun clz(value: Int): Int = Integer.numberOfLeadingZeros(value)
