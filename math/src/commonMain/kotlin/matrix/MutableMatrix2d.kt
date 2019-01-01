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

import org.tobi29.arrays.DoubleArray2
import org.tobi29.arrays.Doubles2
import org.tobi29.arrays.DoublesRO2
import org.tobi29.math.vector.MutableVector2d
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.dot
import org.tobi29.stdex.copy

class MutableMatrix2d(
    val array: DoubleArray2 = DoubleArray2(2, 2)
) : Doubles2 by array {
    init {
        require(array.width == 2) { "Array has invalid width" }
        require(array.height == 2) { "Array has invalid height" }
    }

    constructor(
        xx: Double, xy: Double,
        yx: Double, yy: Double
    ) : this() {
        set(xx, xy, yx, yy)
    }

    fun set(matrix: MutableMatrix2d) {
        copy(matrix.array.array, array.array)
    }

    fun identity() = set(
        1.0, 0.0,
        0.0, 1.0
    )

    fun set(
        xx: Double, xy: Double,
        yx: Double, yy: Double
    ) {
        this.xx = xx
        this.yx = yx
        this.xy = xy
        this.yy = yy
    }

    fun scale(
        x: Double = 1.0,
        y: Double = 1.0
    ) {
        for (i in 0 until width) {
            this[i, 0] = this[i, 0] * x
            this[i, 1] = this[i, 1] * y
        }
    }

    fun multiply(o: MutableMatrix2d, d: MutableMatrix2d = o) {
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

    fun multiply(
        v: MutableVector2d, out: MutableVector2d = v
    ): MutableVector2d {
        val x = v.x
        val y = v.y
        val v1 = xx * x + yx * y
        val v2 = xy * x + yy * y
        out.setXY(v1, v2)
        return out
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DoublesRO2) return false
        return array == other
    }

    override fun hashCode(): Int = array.hashCode()

    // TODO: Remove after 0.0.14

    @Deprecated("Use array.array", ReplaceWith("array.array"))
    inline val values: DoubleArray
        get() = array.array
}

inline fun <R> matrix2dMultiply(
    xx: Double, xy: Double,
    yx: Double, yy: Double,
    x: Double, y: Double,
    output: (Double, Double) -> R
): R = output(
    dot(xx, yx, x, y),
    dot(xy, yy, x, y)
)

inline var MutableMatrix2d.xx: Double
    get() = get(0, 0)
    set(value) = set(0, 0, value)
inline var MutableMatrix2d.yx: Double
    get() = get(0, 1)
    set(value) = set(0, 1, value)
inline var MutableMatrix2d.xy: Double
    get() = get(1, 0)
    set(value) = set(1, 0, value)
inline var MutableMatrix2d.yy: Double
    get() = get(1, 1)
    set(value) = set(1, 1, value)

// TODO: Remove after 0.0.14

@Deprecated(
    "Use MutableMatrix2d",
    ReplaceWith("MutableMatrix2d", "org.tobi29.math.matrix.MutableMatrix2d")
)
typealias Matrix2d = MutableMatrix2d
