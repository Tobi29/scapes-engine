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
package org.tobi29.math.matrix

import org.tobi29.arrays.DoubleArray2
import org.tobi29.math.cosTable
import org.tobi29.math.sinTable
import org.tobi29.math.vector.MutableVector3d
import org.tobi29.math.vector.Vector3d
import org.tobi29.math.vector.dot
import org.tobi29.stdex.copy
import org.tobi29.stdex.math.toRad
import kotlin.math.cos
import kotlin.math.sin

class Matrix3d() {
    val values = DoubleArray(9)
    private val values2 = DoubleArray2(3, 3, values)

    constructor(xx: Double,
                xy: Double,
                xz: Double,
                yx: Double,
                yy: Double,
                yz: Double,
                zx: Double,
                zy: Double,
                zz: Double) : this() {
        set(xx, xy, xz, yx, yy, yz, zx, zy, zz)
    }

    operator fun get(x: Int,
                     y: Int) = values2[y, x]

    operator fun set(x: Int,
                     y: Int,
                     value: Double) {
        values2[y, x] = value
    }

    fun set(matrix: Matrix3d) {
        copy(matrix.values, values)
    }

    fun identity() = set(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0)

    fun set(xx: Double,
            xy: Double,
            xz: Double,
            yx: Double,
            yy: Double,
            yz: Double,
            zx: Double,
            zy: Double,
            zz: Double) {
        values[0] = xx
        values[1] = yx
        values[2] = zx
        values[3] = xy
        values[4] = yy
        values[5] = zy
        values[6] = xz
        values[7] = yz
        values[8] = zz
    }

    fun scale(x: Double,
              y: Double,
              z: Double) {
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

    fun rotate(angle: Double,
               x: Double,
               y: Double,
               z: Double) {
        rotateRad(angle.toRad(), x, y, z)
    }

    fun rotateRad(angle: Double,
                  x: Double,
                  y: Double,
                  z: Double) {
        val cos = cosTable(angle)
        val sin = sinTable(angle)
        rotate(cos, sin, x, y, z)
    }

    fun rotateAccurate(angle: Double,
                       x: Double,
                       y: Double,
                       z: Double) {
        rotateAccurateRad(angle.toRad(), x, y, z)
    }

    fun rotateAccurateRad(angle: Double,
                          x: Double,
                          y: Double,
                          z: Double) {
        val cos = cos(angle)
        val sin = sin(angle)
        rotate(cos, sin, x, y, z)
    }

    private fun rotate(cos: Double,
                       sin: Double,
                       x: Double,
                       y: Double,
                       z: Double) {
        val oneMinusCos = 1.0 - cos
        val mxy = x * y
        val myz = y * z
        val mxz = x * z
        val xSin = x * sin
        val ySin = y * sin
        val zSin = z * sin
        val f00 = x * x * oneMinusCos + cos
        val f01 = mxy * oneMinusCos + zSin
        val f02 = mxz * oneMinusCos - ySin
        val f10 = mxy * oneMinusCos - zSin
        val f11 = y * y * oneMinusCos + cos
        val f12 = myz * oneMinusCos + xSin
        val f20 = mxz * oneMinusCos + ySin
        val f21 = myz * oneMinusCos - xSin
        val f22 = z * z * oneMinusCos + cos
        val v00 = xx
        val v01 = xy
        val v02 = xz
        val v10 = yx
        val v11 = yy
        val v12 = yz
        val v20 = zx
        val v21 = zy
        val v22 = zz
        val t00 = v00 * f00 + v10 * f01 + v20 * f02
        val t01 = v01 * f00 + v11 * f01 + v21 * f02
        val t02 = v02 * f00 + v12 * f01 + v22 * f02
        val t10 = v00 * f10 + v10 * f11 + v20 * f12
        val t11 = v01 * f10 + v11 * f11 + v21 * f12
        val t12 = v02 * f10 + v12 * f11 + v22 * f12
        zx = v00 * f20 + v10 * f21 + v20 * f22
        zy = v01 * f20 + v11 * f21 + v21 * f22
        zz = v02 * f20 + v12 * f21 + v22 * f22
        xx = t00
        xy = t01
        xz = t02
        yx = t10
        yy = t11
        yz = t12
    }

    fun multiply(o: Matrix3d,
                 d: Matrix3d) {
        val v00 = xx
        val v01 = xy
        val v02 = xz
        val v10 = yx
        val v11 = yy
        val v12 = yz
        val v20 = zx
        val v21 = zy
        val v22 = zz
        val o00 = o.xx
        val o01 = o.xy
        val o02 = o.xz
        val o10 = o.yx
        val o11 = o.yy
        val o12 = o.yz
        val o20 = o.zx
        val o21 = o.zy
        val o22 = o.zz
        d.xx = v00 * o00 + v10 * o01 + v20 * o02
        d.xy = v01 * o00 + v11 * o01 + v21 * o02
        d.xz = v02 * o00 + v12 * o01 + v22 * o02
        d.yx = v00 * o10 + v10 * o11 + v20 * o12
        d.yy = v01 * o10 + v11 * o11 + v21 * o12
        d.yz = v02 * o10 + v12 * o11 + v22 * o12
        d.zx = v00 * o20 + v10 * o21 + v20 * o22
        d.zy = v01 * o20 + v11 * o21 + v21 * o22
        d.zz = v02 * o20 + v12 * o21 + v22 * o22
    }

    fun multiply(v: Vector3d): Vector3d =
            matrix3dMultiply(xx, xy, xz, yx, yy, yz, zx, zy, zz, v.x, v.y, v.z,
                    ::Vector3d)

    fun multiply(v: MutableVector3d,
                 out: MutableVector3d = v): MutableVector3d {
        val x = v.x
        val y = v.y
        val z = v.z
        val v1 = xx * x + yx * y + zx * z
        val v2 = xy * x + yy * y + zy * z
        val v3 = xz * x + yz * y + zz * z
        out.setXYZ(v1, v2, v3)
        return out
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Matrix3d) return false
        return values contentEquals other.values
    }

    override fun hashCode(): Int = values.contentHashCode()
}

inline fun <R> matrix3dMultiply(
        xx: Double,
        xy: Double,
        xz: Double,
        yx: Double,
        yy: Double,
        yz: Double,
        zx: Double,
        zy: Double,
        zz: Double,
        x: Double,
        y: Double,
        z: Double,
        output: (Double, Double, Double) -> R): R =
        output(dot(xx, yx, zx, x, y, z), dot(xy, yy, zy, x, y, z),
                dot(xz, yz, zz, x, y, z))

inline var Matrix3d.xx: Double
    get() = get(0, 0)
    set(value) = set(0, 0, value)
inline var Matrix3d.yx: Double
    get() = get(1, 0)
    set(value) = set(1, 0, value)
inline var Matrix3d.zx: Double
    get() = get(2, 0)
    set(value) = set(2, 0, value)
inline var Matrix3d.xy: Double
    get() = get(0, 1)
    set(value) = set(0, 1, value)
inline var Matrix3d.yy: Double
    get() = get(1, 1)
    set(value) = set(1, 1, value)
inline var Matrix3d.zy: Double
    get() = get(2, 1)
    set(value) = set(2, 1, value)
inline var Matrix3d.xz: Double
    get() = get(0, 2)
    set(value) = set(0, 2, value)
inline var Matrix3d.yz: Double
    get() = get(1, 2)
    set(value) = set(1, 2, value)
inline var Matrix3d.zz: Double
    get() = get(2, 2)
    set(value) = set(2, 2, value)
