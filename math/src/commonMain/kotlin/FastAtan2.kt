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

/*
 * Taken from http://www.java-gaming.org/index.php?topic=14647.0
 */
package org.tobi29.math

import kotlin.math.PI
import kotlin.math.atan

val Atan2Table = Atan2LUT(1024)

class Atan2LUT(
    private val size: Int
) {
    private val tablePPY = FloatArray(size + 1)
    private val tablePPX = FloatArray(size + 1)
    private val tablePNY = FloatArray(size + 1)
    private val tablePNX = FloatArray(size + 1)
    private val tableNPY = FloatArray(size + 1)
    private val tableNPX = FloatArray(size + 1)
    private val tableNNY = FloatArray(size + 1)
    private val tableNNX = FloatArray(size + 1)

    init {
        val stretch = PI.toFloat()
        for (i in 0..size) {
            val f = i.toFloat() / size
            tablePPY[i] = (atan(f.toDouble()) * stretch / PI).toFloat()
            tablePPX[i] = stretch * 0.5f - tablePPY[i]
            tablePNY[i] = -tablePPY[i]
            tablePNX[i] = tablePPY[i] - stretch * 0.5f
            tableNPY[i] = stretch - tablePPY[i]
            tableNPX[i] = tablePPY[i] + stretch * 0.5f
            tableNNY[i] = tablePPY[i] - stretch
            tableNNX[i] = -stretch * 0.5f - tablePPY[i]
        }
    }

    fun atan2(
        y: Float, x: Float
    ): Float = if (x >= 0.0f) {
        if (y >= 0.0f) {
            if (x >= y) {
                tablePPY[(size * y / x + 0.5f).toInt()]
            } else {
                tablePPX[(size * x / y + 0.5f).toInt()]
            }
        } else {
            if (x >= -y) {
                tablePNY[(-size * y / x + 0.5f).toInt()]
            } else {
                tablePNX[(-size * x / y + 0.5f).toInt()]
            }
        }
    } else {
        if (y >= 0.0f) {
            if (-x >= y) {
                tableNPY[(-size * y / x + 0.5f).toInt()]
            } else {
                tableNPX[(-size * x / y + 0.5f).toInt()]
            }
        } else {
            if (x <= y) {
                tableNNY[(size * y / x + 0.5f).toInt()]
            } else {
                tableNNX[(size * x / y + 0.5f).toInt()]
            }
        }
    }

    fun atan2(
        y: Double, x: Double
    ): Double = if (x >= 0.0) {
        if (y >= 0.0) {
            if (x >= y) {
                tablePPY[(size * y / x + 0.5).toInt()].toDouble()
            } else {
                tablePPX[(size * x / y + 0.5).toInt()].toDouble()
            }
        } else {
            if (x >= -y) {
                tablePNY[(-size * y / x + 0.5).toInt()].toDouble()
            } else {
                tablePNX[(-size * x / y + 0.5).toInt()].toDouble()
            }
        }
    } else {
        if (y >= 0.0) {
            if (-x >= y) {
                tableNPY[(-size * y / x + 0.5).toInt()].toDouble()
            } else {
                tableNPX[(-size * x / y + 0.5).toInt()].toDouble()
            }
        } else {
            if (x <= y) {
                tableNNY[(size * y / x + 0.5).toInt()].toDouble()
            } else {
                tableNNX[(size * x / y + 0.5).toInt()].toDouble()
            }
        }
    }
}
