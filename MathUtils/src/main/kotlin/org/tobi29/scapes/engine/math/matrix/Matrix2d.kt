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
package org.tobi29.scapes.engine.math.matrix

import org.tobi29.scapes.engine.math.vector.MutableVector2d
import org.tobi29.scapes.engine.math.vector.Vector2d
import org.tobi29.scapes.engine.math.vector.dot
import org.tobi29.scapes.engine.utils.DoubleArray2
import org.tobi29.scapes.engine.utils.copy

class Matrix2d() {
    val values = DoubleArray(4)
    private val values2 = DoubleArray2(2, 2, values)

    constructor(xx: Double,
                xy: Double,
                yx: Double,
                yy: Double) : this() {
        set(xx, xy, yx, yy)
    }

    operator fun get(x: Int,
                     y: Int) = values2[y, x]

    operator fun set(x: Int,
                     y: Int,
                     value: Double) {
        values2[y, x] = value
    }

    fun set(matrix: Matrix2d) {
        copy(matrix.values, values)
    }

    fun identity() = set(1.0, 0.0, 0.0, 1.0)

    fun set(xx: Double,
            xy: Double,
            yx: Double,
            yy: Double) {
        values[0] = xx
        values[1] = yx
        values[2] = xy
        values[3] = yy
    }

    fun scale(x: Double,
              y: Double) {
        for (i in 0..1) {
            values[i] = values[i] * x
        }
        for (i in 2..3) {
            values[i] = values[i] * y
        }
    }

    fun multiply(o: Matrix2d,
                 d: Matrix2d = o) {
        val v00 = xx
        val v01 = xy
        val v10 = yx
        val v11 = yy
        val o00 = o.xx
        val o01 = o.xy
        val o10 = o.yx
        val o11 = o.yy
        d.xx = v00 * o00 + v10 * o01
        d.xy = v01 * o00 + v11 * o01
        d.yx = v00 * o10 + v10 * o11
        d.yy = v01 * o10 + v11 * o11
    }

    fun multiply(v: Vector2d): Vector2d {
        val x = v.x
        val y = v.y
        val v1 = xx * x + yx * y
        val v2 = xy * x + yy * y
        return Vector2d(v1, v2)
    }

    fun multiply(v: MutableVector2d,
                 out: MutableVector2d = v): MutableVector2d {
        val x = v.x
        val y = v.y
        val v1 = xx * x + yx * y
        val v2 = xy * x + yy * y
        out.set(v1, v2)
        return out
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Matrix2d) return false
        return values contentEquals other.values
    }

    override fun hashCode(): Int = values.contentHashCode()
}

inline fun <R> matrix2dMultiply(
        xx: Double,
        xy: Double,
        yx: Double,
        yy: Double,
        x: Double,
        y: Double,
        output: (Double, Double) -> R): R =
        output(dot(xx, yx, x, y), dot(xy, yy, x, y))

inline var Matrix2d.xx: Double
    get() = get(0, 0)
    set(value) = set(0, 0, value)
inline var Matrix2d.yx: Double
    get() = get(1, 0)
    set(value) = set(1, 0, value)
inline var Matrix2d.xy: Double
    get() = get(0, 1)
    set(value) = set(0, 1, value)
inline var Matrix2d.yy: Double
    get() = get(1, 1)
    set(value) = set(1, 1, value)
