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

/*
 * Taken from http://www.java-gaming.org/index.php?topic=14647.0
 */
package org.tobi29.scapes.engine.utils.math

object FastAtan2 {
    private val SIZE = 1024
    private val STRETCH = Math.PI.toFloat()
    private val EZIS = -SIZE
    private val ATAN2_TABLE_PPY = FloatArray(SIZE + 1)
    private val ATAN2_TABLE_PPX = FloatArray(SIZE + 1)
    private val ATAN2_TABLE_PNY = FloatArray(SIZE + 1)
    private val ATAN2_TABLE_PNX = FloatArray(SIZE + 1)
    private val ATAN2_TABLE_NPY = FloatArray(SIZE + 1)
    private val ATAN2_TABLE_NPX = FloatArray(SIZE + 1)
    private val ATAN2_TABLE_NNY = FloatArray(SIZE + 1)
    private val ATAN2_TABLE_NNX = FloatArray(SIZE + 1)

    init {
        for (i in 0..SIZE) {
            val f = i.toFloat() / SIZE
            ATAN2_TABLE_PPY[i] = (StrictMath.atan(
                    f.toDouble()) * STRETCH / StrictMath.PI).toFloat()
            ATAN2_TABLE_PPX[i] = STRETCH * 0.5f - ATAN2_TABLE_PPY[i]
            ATAN2_TABLE_PNY[i] = -ATAN2_TABLE_PPY[i]
            ATAN2_TABLE_PNX[i] = ATAN2_TABLE_PPY[i] - STRETCH * 0.5f
            ATAN2_TABLE_NPY[i] = STRETCH - ATAN2_TABLE_PPY[i]
            ATAN2_TABLE_NPX[i] = ATAN2_TABLE_PPY[i] + STRETCH * 0.5f
            ATAN2_TABLE_NNY[i] = ATAN2_TABLE_PPY[i] - STRETCH
            ATAN2_TABLE_NNX[i] = -STRETCH * 0.5f - ATAN2_TABLE_PPY[i]
        }
    }

    fun atan2(y: Double,
              x: Double): Double {
        if (x >= 0) {
            if (y >= 0) {
                if (x >= y) {
                    return ATAN2_TABLE_PPY[(SIZE * y / x + 0.5).toInt()].toDouble()
                } else {
                    return ATAN2_TABLE_PPX[(SIZE * x / y + 0.5).toInt()].toDouble()
                }
            } else {
                if (x >= -y) {
                    return ATAN2_TABLE_PNY[(EZIS * y / x + 0.5).toInt()].toDouble()
                } else {
                    return ATAN2_TABLE_PNX[(EZIS * x / y + 0.5).toInt()].toDouble()
                }
            }
        } else {
            if (y >= 0) {
                if (-x >= y) {
                    return ATAN2_TABLE_NPY[(EZIS * y / x + 0.5).toInt()].toDouble()
                } else {
                    return ATAN2_TABLE_NPX[(EZIS * x / y + 0.5).toInt()].toDouble()
                }
            } else {
                if (x <= y) {
                    return ATAN2_TABLE_NNY[(SIZE * y / x + 0.5).toInt()].toDouble()
                } else {
                    return ATAN2_TABLE_NNX[(SIZE * x / y + 0.5).toInt()].toDouble()
                }
            }
        }
    }
}
