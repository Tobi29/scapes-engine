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
package org.tobi29.scapes.engine.utils.math

object FastAsin {
    private val BITS = 14
    private val MASK: Int
    private val VALUE_2_INDEX: Double
    private val ASIN: FloatArray

    init {
        MASK = (-1 shl BITS).inv()
        val count = MASK + 1
        VALUE_2_INDEX = (count - 1) * 0.5
        ASIN = FloatArray(count)
        for (i in 0..count - 1) {
            ASIN[i] = Math.asin(i / VALUE_2_INDEX - 1.0).toFloat()
        }
    }

    fun asin(value: Double): Double {
        val i = ((value + 1.0) * VALUE_2_INDEX).toInt()
        if (i < 0 || i >= ASIN.size) {
            return Double.NaN
        }
        return ASIN[i].toDouble()
    }

    fun acos(value: Double): Double {
        return HALF_PI - asin(value)
    }
}
