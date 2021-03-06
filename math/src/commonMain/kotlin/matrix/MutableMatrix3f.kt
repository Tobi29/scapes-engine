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
package org.tobi29.math.matrix

import org.tobi29.arrays.FloatArray2
import org.tobi29.arrays.Floats2
import org.tobi29.arrays.FloatsRO2
import org.tobi29.math.cosTable
import org.tobi29.math.sinTable
import org.tobi29.math.vector.Vector3d
import org.tobi29.math.vector.dot
import org.tobi29.stdex.copy
import org.tobi29.stdex.math.toRad
import kotlin.math.cos
import kotlin.math.sin

class MutableMatrix3f(
    val array: FloatArray2 = FloatArray2(3, 3)
) : Floats2 by array {
    init {
        require(array.width == 3) { "Array has invalid width" }
        require(array.height == 3) { "Array has invalid height" }
    }

    fun set(matrix: MutableMatrix3f) {
        copy(matrix.array.array, array.array)
    }

    fun identity() = set(
        1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 1.0f
    )

    fun set(
        xx: Float, xy: Float, xz: Float,
        yx: Float, yy: Float, yz: Float,
        zx: Float, zy: Float, zz: Float
    ) {
        this.xx = xx
        this.yx = yx
        this.zx = zx
        this.xy = xy
        this.yy = yy
        this.zy = zy
        this.xz = xz
        this.yz = yz
        this.zz = zz
    }

    fun scale(
        x: Float = 1.0f,
        y: Float = 1.0f,
        z: Float = 1.0f
    ) {
        for (i in 0 until width) {
            this[i, 0] = this[i, 0] * x
            this[i, 1] = this[i, 1] * y
            this[i, 2] = this[i, 2] * z
        }
    }

    fun rotate(
        angle: Float,
        x: Float,
        y: Float,
        z: Float
    ) {
        rotateRad(angle.toRad(), x, y, z)
    }

    fun rotateRad(
        angle: Float,
        x: Float,
        y: Float,
        z: Float
    ) {
        val cos = cosTable(angle)
        val sin = sinTable(angle)
        rotate(cos, sin, x, y, z)
    }

    fun rotateAccurate(
        angle: Double,
        x: Float,
        y: Float,
        z: Float
    ) {
        rotateAccurateRad(angle.toRad(), x, y, z)
    }

    fun rotateAccurateRad(
        angle: Double,
        x: Float,
        y: Float,
        z: Float
    ) {
        val cos = cos(angle).toFloat()
        val sin = sin(angle).toFloat()
        rotate(cos, sin, x, y, z)
    }

    private fun rotate(
        cos: Float,
        sin: Float,
        x: Float,
        y: Float,
        z: Float
    ) {
        val oneMinusCos = 1.0f - cos
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

    fun multiply(
        o: MutableMatrix3f,
        d: MutableMatrix3f
    ) {
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
        matrix3dMultiply(
            xx.toDouble(), xy.toDouble(), xz.toDouble(),
            yx.toDouble(), yy.toDouble(), yz.toDouble(), zx.toDouble(),
            zy.toDouble(), zz.toDouble(), v.x, v.y, v.z, ::Vector3d
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FloatsRO2) return false
        return array == other
    }

    override fun hashCode(): Int = array.hashCode()
}

inline fun <R> matrix3fMultiply(
    xx: Float, xy: Float, xz: Float,
    yx: Float, yy: Float, yz: Float,
    zx: Float, zy: Float, zz: Float,
    x: Float, y: Float, z: Float,
    output: (Float, Float, Float) -> R
): R = output(
    dot(xx, yx, zx, x, y, z),
    dot(xy, yy, zy, x, y, z),
    dot(xz, yz, zz, x, y, z)
)

inline var MutableMatrix3f.xx: Float
    get() = get(0, 0)
    set(value) = set(0, 0, value)
inline var MutableMatrix3f.yx: Float
    get() = get(0, 1)
    set(value) = set(0, 1, value)
inline var MutableMatrix3f.zx: Float
    get() = get(0, 2)
    set(value) = set(0, 2, value)
inline var MutableMatrix3f.xy: Float
    get() = get(1, 0)
    set(value) = set(1, 0, value)
inline var MutableMatrix3f.yy: Float
    get() = get(1, 1)
    set(value) = set(1, 1, value)
inline var MutableMatrix3f.zy: Float
    get() = get(1, 2)
    set(value) = set(1, 2, value)
inline var MutableMatrix3f.xz: Float
    get() = get(2, 0)
    set(value) = set(2, 0, value)
inline var MutableMatrix3f.yz: Float
    get() = get(2, 1)
    set(value) = set(2, 1, value)
inline var MutableMatrix3f.zz: Float
    get() = get(2, 2)
    set(value) = set(2, 2, value)
