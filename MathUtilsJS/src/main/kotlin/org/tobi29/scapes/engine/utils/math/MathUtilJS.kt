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

import kotlin.js.*

impl inline fun abs(value: Int) = if (value < 0) -value else value

impl inline fun abs(value: Long) = if (value < 0) -value else value

impl inline fun abs(value: Float) = if (value < 0.0f) -value else value

impl inline fun abs(value: Double) = Math.abs(value)

impl inline fun min(value1: Int,
                    value2: Int) = Math.min(value1, value2)

impl inline fun min(value1: Long,
                    value2: Long) = Math.min(value1, value2)

impl inline fun min(value1: Float,
                    value2: Float) = Math.min(value1, value2)

impl inline fun min(value1: Double,
                    value2: Double) = Math.min(value1, value2)

impl inline fun max(value1: Int,
                    value2: Int) = Math.max(value1, value2)

impl inline fun max(value1: Long,
                    value2: Long) = Math.max(value1, value2)

impl inline fun max(value1: Float,
                    value2: Float) = Math.max(value1, value2)

impl inline fun max(value1: Double,
                    value2: Double) = Math.max(value1, value2)

impl inline fun sqrt(value: Float) = sqrt(value.toDouble()).toFloat()

impl inline fun sqrt(value: Double) = Math.sqrt(value)

impl inline fun cbrt(value: Float) = cbrt(value.toDouble()).toFloat()

impl inline fun cbrt(value: Double) = pow(value, 1.0 / 3.0)

impl inline fun floor(value: Float): Int {
    val int = value.toInt()
    return if (value >= 0.0f || int.toFloat() == value) int else int - 1
}

impl inline fun floor(value: Double) = Math.floor(value)

impl inline fun floorL(value: Double): Long {
    val int = value.toLong()
    return if (value >= 0.0 || int.toDouble() == value) int else int - 1
}

@Suppress("UnsafeCastFromDynamic")
impl inline fun floorD(value: Double): Double = MathD.floor(value)

impl inline fun round(value: Float) = floor(value + 0.5f)

impl inline fun round(value: Double) = floor(value + 0.5)

impl inline fun roundL(value: Double) = floorL(value + 0.5f)

@Suppress("UnsafeCastFromDynamic")
impl inline fun roundD(value: Double): Double = MathD.round(value)

impl inline fun ceil(value: Float): Int {
    val int = value.toInt()
    return if (value <= 0.0f || int.toFloat() == value) int else int + 1
}

impl inline fun ceil(value: Double) = Math.ceil(value)

impl inline fun ceilL(value: Double): Long {
    val int = value.toLong()
    return if (value <= 0.0 || int.toDouble() == value) int else int + 1
}

@Suppress("UnsafeCastFromDynamic")
impl inline fun ceilD(value: Double): Double = MathD.ceil(value)

impl inline fun sin(value: Float) = sin(value.toDouble()).toFloat()

impl inline fun sin(value: Double) = Math.sin(value)

impl inline fun asin(value: Float) = asin(value.toDouble()).toFloat()

impl inline fun asin(value: Double) = Math.asin(value)

impl inline fun cos(value: Float) = cos(value.toDouble()).toFloat()

impl inline fun cos(value: Double) = Math.cos(value)

impl inline fun acos(value: Float) = acos(value.toDouble()).toFloat()

impl inline fun acos(value: Double) = Math.acos(value)

impl inline fun tan(value: Float) = tan(value.toDouble()).toFloat()

impl inline fun tan(value: Double) = Math.tan(value)

impl inline fun tanh(value: Float) = tanh(value.toDouble()).toFloat()

impl fun tanh(value: Double) = tanhImpl(value)

@Suppress("UnsafeCastFromDynamic")
private val tanhImpl: (Double) -> Double = if (MathD.tanh !== undefined) MathD.tanh
else { value ->
    val a = Math.exp(+value)
    val b = Math.exp(-value)
    when {
        a == Double.POSITIVE_INFINITY -> 1.0
        b == Double.POSITIVE_INFINITY -> -1.0
        else -> (a - b) / (a + b)
    }
}

impl inline fun atan(value: Float) = atan(value.toDouble()).toFloat()

impl inline fun atan(value: Double) = Math.atan(value)

impl inline fun atan2(value1: Float,
                      value2: Float) =
        atan2(value1.toDouble(), value2.toDouble()).toFloat()

impl inline fun atan2(value1: Double,
                      value2: Double) = Math.atan2(value1, value2)

impl inline fun pow(value1: Float,
                    value2: Float) =
        pow(value1.toDouble(), value2.toDouble()).toFloat()

impl inline fun pow(value1: Double,
                    value2: Double) = Math.pow(value1, value2)

impl fun clz(value: Int) = clzImpl(value)

@Suppress("UnsafeCastFromDynamic")
private val clzImpl: (Int) -> Int = if (MathD.clz !== undefined) MathD.clz
else { value: Int ->
    if (value == 0) 32
    else MathD.floor(MathD.log(
            value ushr 0) * MathD.LOG2E).let { it: Int -> 31 - it }
}

@PublishedApi
internal inline val MathD
    get() = Math.asDynamic()
