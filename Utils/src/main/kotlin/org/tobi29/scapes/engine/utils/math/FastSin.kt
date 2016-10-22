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

/*
 * Taken from http://www.java-gaming.org/index.php?topic=24191.0
 */
package org.tobi29.scapes.engine.utils.math

object FastSin {
    private val BITS = 12
    private val MASK: Int
    private val RAD_2_INDEX: Double
    private val SIN: FloatArray

    init {
        MASK = (-1 shl BITS).inv()
        val count = MASK + 1
        RAD_2_INDEX = count / FastMath.TWO_PI
        SIN = FloatArray(count)
        for (i in 0..count - 1) {
            SIN[i] = Math.sin((i + 0.5f) / count * FastMath.TWO_PI).toFloat()
        }
        // Set exact values
        SIN[0] = 0.0f
        SIN[count shr 2] = 1.0f
        SIN[count shr 1] = 0.0f
        SIN[(count shr 2) * 3] = -1.0f
    }

    fun sin(value: Double): Double {
        return SIN[(value * RAD_2_INDEX).toInt() and MASK].toDouble()
    }

    fun cos(value: Double): Double {
        return sin(value + FastMath.HALF_PI)
    }
}
