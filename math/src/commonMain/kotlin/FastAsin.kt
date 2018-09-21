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

import org.tobi29.stdex.math.HALF_PI
import org.tobi29.stdex.math.mix

val AsinTable = AsinLUT(12)

class AsinLUT(bits: Int) {
    private val count = 1 shl bits
    private val value2indexF: Float
    private val value2indexD: Double
    private val asin: FloatArray

    init {
        value2indexD = (count - 1) * 0.5
        value2indexF = value2indexD.toFloat()
        asin = FloatArray(count + 1)
        for (i in 0 until count) {
            asin[i] = kotlin.math.asin(i / value2indexD - 1.0).toFloat()
        }
        asin[count] = asin[count - 1]
    }

    fun asin(value: Float): Float {
        val fi = (value + 1.0f) * value2indexF
        val i = fi.toInt()
        if (i < 0 || i >= count) return Float.NaN
        return mix(asin[i], asin[i + 1], fi - i)
    }

    fun acos(value: Float): Float = HALF_PI.toFloat() - asin(value)

    fun asin(value: Double): Double {
        val fi = (value + 1.0) * value2indexD
        val i = fi.toInt()
        if (i < 0 || i >= count) return Double.NaN
        return mix(asin[i].toDouble(), asin[i + 1].toDouble(), fi - i)
    }

    fun acos(value: Double): Double = HALF_PI - asin(value)
}
