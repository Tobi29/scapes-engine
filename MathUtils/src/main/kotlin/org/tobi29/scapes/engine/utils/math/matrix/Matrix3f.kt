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
package org.tobi29.scapes.engine.utils.math.matrix

import org.tobi29.scapes.engine.utils.copy
import org.tobi29.scapes.engine.utils.math.*
import org.tobi29.scapes.engine.utils.math.vector.Vector3d

class Matrix3f {
    private val values = FloatArray(9)

    fun values(): FloatArray {
        return values
    }

    fun copy(matrix: Matrix3f) {
        copy(matrix.values, values)
    }

    fun identity() {
        values[V00] = 1.0f
        values[V01] = 0.0f
        values[V02] = 0.0f
        values[V10] = 0.0f
        values[V11] = 1.0f
        values[V12] = 0.0f
        values[V20] = 0.0f
        values[V21] = 0.0f
        values[V22] = 1.0f
    }

    fun scale(x: Float,
              y: Float,
              z: Float) {
        for (i in 0..2) {
            values[i] = values[i] * x
        }
        for (i in 3..5) {
            values[i] = values[i] * y
        }
        for (i in 6..8) {
            values[i] = values[i] * z
        }
    }

    fun rotate(angle: Float,
               x: Float,
               y: Float,
               z: Float) {
        rotateRad(angle.toRad(), x, y, z)
    }

    fun rotateRad(angle: Float,
                  x: Float,
                  y: Float,
                  z: Float) {
        val cos = cosTable(angle)
        val sin = sinTable(angle)
        rotate(cos, sin, x, y, z)
    }

    fun rotateAccurate(angle: Double,
                       x: Float,
                       y: Float,
                       z: Float) {
        rotateAccurateRad(angle.toRad(), x, y, z)
    }

    fun rotateAccurateRad(angle: Double,
                          x: Float,
                          y: Float,
                          z: Float) {
        val cos = cos(angle).toFloat()
        val sin = sin(angle).toFloat()
        rotate(cos, sin, x, y, z)
    }

    private fun rotate(cos: Float,
                       sin: Float,
                       x: Float,
                       y: Float,
                       z: Float) {
        val oneMinusCos = 1.0f - cos
        val xy = x * y
        val yz = y * z
        val xz = x * z
        val xSin = x * sin
        val ySin = y * sin
        val zSin = z * sin
        val f00 = x * x * oneMinusCos + cos
        val f01 = xy * oneMinusCos + zSin
        val f02 = xz * oneMinusCos - ySin
        val f10 = xy * oneMinusCos - zSin
        val f11 = y * y * oneMinusCos + cos
        val f12 = yz * oneMinusCos + xSin
        val f20 = xz * oneMinusCos + ySin
        val f21 = yz * oneMinusCos - xSin
        val f22 = z * z * oneMinusCos + cos
        val v00 = values[V00]
        val v01 = values[V01]
        val v02 = values[V02]
        val v10 = values[V10]
        val v11 = values[V11]
        val v12 = values[V12]
        val v20 = values[V20]
        val v21 = values[V21]
        val v22 = values[V22]
        val t00 = v00 * f00 + v10 * f01 + v20 * f02
        val t01 = v01 * f00 + v11 * f01 + v21 * f02
        val t02 = v02 * f00 + v12 * f01 + v22 * f02
        val t10 = v00 * f10 + v10 * f11 + v20 * f12
        val t11 = v01 * f10 + v11 * f11 + v21 * f12
        val t12 = v02 * f10 + v12 * f11 + v22 * f12
        values[V20] = v00 * f20 + v10 * f21 + v20 * f22
        values[V21] = v01 * f20 + v11 * f21 + v21 * f22
        values[V22] = v02 * f20 + v12 * f21 + v22 * f22
        values[V00] = t00
        values[V01] = t01
        values[V02] = t02
        values[V10] = t10
        values[V11] = t11
        values[V12] = t12
    }

    fun multiply(o: Matrix3f,
                 d: Matrix3f) {
        val v00 = values[V00]
        val v01 = values[V01]
        val v02 = values[V02]
        val v10 = values[V10]
        val v11 = values[V11]
        val v12 = values[V12]
        val v20 = values[V20]
        val v21 = values[V21]
        val v22 = values[V22]
        val o00 = o.values[V00]
        val o01 = o.values[V01]
        val o02 = o.values[V02]
        val o10 = o.values[V10]
        val o11 = o.values[V11]
        val o12 = o.values[V12]
        val o20 = o.values[V20]
        val o21 = o.values[V21]
        val o22 = o.values[V22]
        d.values[V00] = v00 * o00 + v10 * o01 + v20 * o02
        d.values[V01] = v01 * o00 + v11 * o01 + v21 * o02
        d.values[V02] = v02 * o00 + v12 * o01 + v22 * o02
        d.values[V10] = v00 * o10 + v10 * o11 + v20 * o12
        d.values[V11] = v01 * o10 + v11 * o11 + v21 * o12
        d.values[V12] = v02 * o10 + v12 * o11 + v22 * o12
        d.values[V20] = v00 * o20 + v10 * o21 + v20 * o22
        d.values[V21] = v01 * o20 + v11 * o21 + v21 * o22
        d.values[V22] = v02 * o20 + v12 * o21 + v22 * o22
    }

    fun multiply(v: Vector3d): Vector3d {
        val x = v.x
        val y = v.y
        val z = v.z
        val v1 = values[V00] * x + values[V10] * y + values[V20] * z
        val v2 = values[V01] * x + values[V11] * y + values[V21] * z
        val v3 = values[V02] * x + values[V12] * y + values[V22] * z
        return Vector3d(v1, v2, v3)
    }

    private companion object {
        private const val V00 = 0
        private const val V01 = 1
        private const val V02 = 2
        private const val V10 = 3
        private const val V11 = 4
        private const val V12 = 5
        private const val V20 = 6
        private const val V21 = 7
        private const val V22 = 8
    }
}
