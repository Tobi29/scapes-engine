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
package org.tobi29.scapes.engine.utils.math.matrix

import org.tobi29.scapes.engine.utils.math.*
import org.tobi29.scapes.engine.utils.math.vector.Vector3d

import java.nio.ByteBuffer

class Matrix4f {

    private val values = FloatArray(16)

    fun values(): FloatArray {
        return values
    }

    fun putInto(buffer: ByteBuffer) {
        for (value in values) {
            buffer.putFloat(value)
        }
    }

    fun copy(matrix: Matrix4f) {
        System.arraycopy(matrix.values, 0, values, 0, values.size)
    }

    fun identity() {
        values[V00] = 1.0f
        values[V01] = 0.0f
        values[V02] = 0.0f
        values[V03] = 0.0f
        values[V10] = 0.0f
        values[V11] = 1.0f
        values[V12] = 0.0f
        values[V13] = 0.0f
        values[V20] = 0.0f
        values[V21] = 0.0f
        values[V22] = 1.0f
        values[V23] = 0.0f
        values[V30] = 0.0f
        values[V31] = 0.0f
        values[V32] = 0.0f
        values[V33] = 1.0f
    }

    fun scale(x: Float,
              y: Float,
              z: Float) {
        for (i in 0..3) {
            values[i] = values[i] * x
        }
        for (i in 4..7) {
            values[i] = values[i] * y
        }
        for (i in 8..11) {
            values[i] = values[i] * z
        }
    }

    fun translate(x: Float,
                  y: Float,
                  z: Float) {
        values[V30] = values[V30] + values[V00] * x + values[V10] * y +
                values[V20] * z
        values[V31] = values[V31] + values[V01] * x + values[V11] * y +
                values[V21] * z
        values[V32] = values[V32] + values[V02] * x + values[V12] * y +
                values[V22] * z
        values[V33] = values[V33] + values[V03] * x + values[V13] * y +
                values[V23] * z
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
        val v03 = values[V03]
        val v10 = values[V10]
        val v11 = values[V11]
        val v12 = values[V12]
        val v13 = values[V13]
        val v20 = values[V20]
        val v21 = values[V21]
        val v22 = values[V22]
        val v23 = values[V23]
        val t00 = v00 * f00 + v10 * f01 + v20 * f02
        val t01 = v01 * f00 + v11 * f01 + v21 * f02
        val t02 = v02 * f00 + v12 * f01 + v22 * f02
        val t03 = v03 * f00 + v13 * f01 + v23 * f02
        val t10 = v00 * f10 + v10 * f11 + v20 * f12
        val t11 = v01 * f10 + v11 * f11 + v21 * f12
        val t12 = v02 * f10 + v12 * f11 + v22 * f12
        val t13 = v03 * f10 + v13 * f11 + v23 * f12
        values[V20] = v00 * f20 + v10 * f21 + v20 * f22
        values[V21] = v01 * f20 + v11 * f21 + v21 * f22
        values[V22] = v02 * f20 + v12 * f21 + v22 * f22
        values[V23] = v03 * f20 + v13 * f21 + v23 * f22
        values[V00] = t00
        values[V01] = t01
        values[V02] = t02
        values[V03] = t03
        values[V10] = t10
        values[V11] = t11
        values[V12] = t12
        values[V13] = t13
    }

    fun multiply(o: Matrix4f,
                 d: Matrix4f) {
        val v00 = values[V00]
        val v01 = values[V01]
        val v02 = values[V02]
        val v03 = values[V03]
        val v10 = values[V10]
        val v11 = values[V11]
        val v12 = values[V12]
        val v13 = values[V13]
        val v20 = values[V20]
        val v21 = values[V21]
        val v22 = values[V22]
        val v23 = values[V23]
        val v30 = values[V30]
        val v31 = values[V31]
        val v32 = values[V32]
        val v33 = values[V33]
        val o00 = o.values[V00]
        val o01 = o.values[V01]
        val o02 = o.values[V02]
        val o03 = o.values[V03]
        val o10 = o.values[V10]
        val o11 = o.values[V11]
        val o12 = o.values[V12]
        val o13 = o.values[V13]
        val o20 = o.values[V20]
        val o21 = o.values[V21]
        val o22 = o.values[V22]
        val o23 = o.values[V23]
        val o30 = o.values[V30]
        val o31 = o.values[V31]
        val o32 = o.values[V32]
        val o33 = o.values[V33]
        d.values[V00] = v00 * o00 + v10 * o01 + v20 * o02 + v30 * o03
        d.values[V01] = v01 * o00 + v11 * o01 + v21 * o02 + v31 * o03
        d.values[V02] = v02 * o00 + v12 * o01 + v22 * o02 + v32 * o03
        d.values[V03] = v03 * o00 + v13 * o01 + v23 * o02 + v33 * o03
        d.values[V10] = v00 * o10 + v10 * o11 + v20 * o12 + v30 * o13
        d.values[V11] = v01 * o10 + v11 * o11 + v21 * o12 + v31 * o13
        d.values[V12] = v02 * o10 + v12 * o11 + v22 * o12 + v32 * o13
        d.values[V13] = v03 * o10 + v13 * o11 + v23 * o12 + v33 * o13
        d.values[V20] = v00 * o20 + v10 * o21 + v20 * o22 + v30 * o23
        d.values[V21] = v01 * o20 + v11 * o21 + v21 * o22 + v31 * o23
        d.values[V22] = v02 * o20 + v12 * o21 + v22 * o22 + v32 * o23
        d.values[V23] = v03 * o20 + v13 * o21 + v23 * o22 + v33 * o23
        d.values[V30] = v00 * o30 + v10 * o31 + v20 * o32 + v30 * o33
        d.values[V31] = v01 * o30 + v11 * o31 + v21 * o32 + v31 * o33
        d.values[V32] = v02 * o30 + v12 * o31 + v22 * o32 + v32 * o33
        d.values[V33] = v03 * o30 + v13 * o31 + v23 * o32 + v33 * o33
    }

    fun multiply(v: Vector3d): Vector3d {
        val x = v.x
        val y = v.y
        val z = v.z
        val w = 1.0
        val v1 = values[V00] * x + values[V10] * y + values[V20] * z +
                values[V30] * w
        val v2 = values[V01] * x + values[V11] * y + values[V21] * z +
                values[V31] * w
        val v3 = values[V02] * x + values[V12] * y + values[V22] * z +
                values[V32] * w
        return Vector3d(v1, v2, v3)
    }

    fun perspective(fov: Float,
                    aspectRatio: Float,
                    near: Float,
                    far: Float) {
        val delta = far - near
        val cotangent = 1.0f / tan((fov / 2.0f).toRad())
        values[V00] = cotangent / aspectRatio
        values[V11] = cotangent
        val value2 = -(far + near) / delta
        values[V22] = value2
        val value1 = -1.0f
        values[V23] = value1
        val value = -2.0f * near * far / delta
        values[V32] = value
        values[V33] = 0.0f
    }

    fun orthogonal(x: Float,
                   y: Float,
                   width: Float,
                   height: Float,
                   zNear: Float = -1024.0f,
                   zFar: Float = 1024.0f) {
        val left = x
        val right = x + width
        val bottom = y + height
        val top = y

        values[V00] = 2.0f / (right - left)
        values[V01] = 0.0f
        values[V02] = 0.0f
        values[V03] = 0.0f
        values[V10] = 0.0f
        values[V11] = 2.0f / (top - bottom)
        values[V12] = 0.0f
        values[V13] = 0.0f
        values[V20] = 0.0f
        values[V21] = 0.0f
        values[V22] = 2.0f / (zFar - zNear)
        values[V23] = 0.0f
        val value2 = -(right + left) / (right - left)
        values[V30] = value2
        val value1 = -(top + bottom) / (top - bottom)
        values[V31] = value1
        val value = -(zFar + zNear) / (zFar - zNear)
        values[V32] = value
        values[V33] = 1.0f
    }

    fun invert(temp: Matrix4f,
               out: Matrix4f): Boolean {
        if (temp !== this) {
            temp.copy(this)
        }
        out.identity()
        for (i in 0..3) {
            val i4 = i shl 2
            var swap = i
            for (j in i + 1..3) {
                if (Math.abs(temp.values[(j shl 2) + i]) > Math.abs(
                        temp.values[i4 + i])) {
                    swap = j
                }
            }
            if (swap != i) {
                val swap4 = swap shl 2
                for (k in 0..3) {
                    var t = temp.values[i4 + k]
                    temp.values[i4 + k] = temp.values[swap4 + k]
                    temp.values[swap4 + k] = t
                    t = out.values[i4 + k]
                    out.values[i4 + k] = out.values[swap4 + k]
                    out.values[swap4 + k] = t
                }
            }
            if (temp.values[i4 + i] == 0f) {
                return false
            }
            var t = temp.values[i4 + i]
            for (k in 0..3) {
                temp.values[i4 + k] = temp.values[i4 + k] / t
                out.values[i4 + k] = out.values[i4 + k] / t
            }
            for (j in 0..3) {
                if (j != i) {
                    val j4 = j shl 2
                    t = temp.values[j4 + i]
                    for (k in 0..3) {
                        temp.values[j4 + k] = temp.values[j4 + k] - temp.values[i4 + k] * t
                        out.values[j4 + k] = out.values[j4 + k] - out.values[i4 + k] * t
                    }
                }
            }
        }
        return true
    }

    private companion object {
        private const val V00 = 0
        private const val V01 = 1
        private const val V02 = 2
        private const val V03 = 3
        private const val V10 = 4
        private const val V11 = 5
        private const val V12 = 6
        private const val V13 = 7
        private const val V20 = 8
        private const val V21 = 9
        private const val V22 = 10
        private const val V23 = 11
        private const val V30 = 12
        private const val V31 = 13
        private const val V32 = 14
        private const val V33 = 15
    }
}
