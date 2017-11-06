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

package org.tobi29.scapes.engine.math.vector

import org.tobi29.scapes.engine.utils.math.floorToInt
import org.tobi29.scapes.engine.utils.tag.ReadTagMutableMap
import org.tobi29.scapes.engine.utils.tag.toDouble

class MutableVector3d(x: Double = 0.0,
                      y: Double = 0.0,
                      var z: Double = 0.0) : MutableVector2d(
        x, y) {

    constructor(vector: Vector3d) : this(vector.x, vector.y, vector.z)

    constructor(vector: Vector3i) : this(vector.x.toDouble() + 0.5,
            vector.y.toDouble() + 0.5, vector.z.toDouble() + 0.5)

    constructor(vector: MutableVector3d) : this(vector.doubleX(),
            vector.doubleY(), vector.doubleZ())

    constructor(vector: MutableVector3i) : this(vector.x.toDouble() + 0.5,
            vector.y.toDouble() + 0.5, vector.z.toDouble() + 0.5)

    override fun plus(a: Int): MutableVector3d {
        x += a.toDouble()
        y += a.toDouble()
        z += a.toDouble()
        return this
    }

    override fun plus(a: Long): MutableVector3d {
        x += a.toDouble()
        y += a.toDouble()
        z += a.toDouble()
        return this
    }

    override fun plus(a: Float): MutableVector3d {
        x += a.toDouble()
        y += a.toDouble()
        z += a.toDouble()
        return this
    }

    override fun plus(a: Double): MutableVector3d {
        x += a
        y += a
        z += a
        return this
    }

    override fun minus(a: Int): MutableVector3d {
        x -= a.toDouble()
        y -= a.toDouble()
        z -= a.toDouble()
        return this
    }

    override fun minus(a: Long): MutableVector3d {
        x -= a.toDouble()
        y -= a.toDouble()
        z -= a.toDouble()
        return this
    }

    override fun minus(a: Float): MutableVector3d {
        x -= a.toDouble()
        y -= a.toDouble()
        z -= a.toDouble()
        return this
    }

    override fun minus(a: Double): MutableVector3d {
        x -= a
        y -= a
        z -= a
        return this
    }

    override fun times(a: Int): MutableVector3d {
        x *= a.toDouble()
        y *= a.toDouble()
        z *= a.toDouble()
        return this
    }

    override fun times(a: Long): MutableVector3d {
        x *= a.toDouble()
        y *= a.toDouble()
        z *= a.toDouble()
        return this
    }

    override fun times(a: Float): MutableVector3d {
        x *= a.toDouble()
        y *= a.toDouble()
        z *= a.toDouble()
        return this
    }

    override fun times(a: Double): MutableVector3d {
        x *= a
        y *= a
        z *= a
        return this
    }

    override fun div(a: Int): MutableVector3d {
        x /= a.toDouble()
        y /= a.toDouble()
        z /= a.toDouble()
        return this
    }

    override fun div(a: Long): MutableVector3d {
        x /= a.toDouble()
        y /= a.toDouble()
        z /= a.toDouble()
        return this
    }

    override fun div(a: Float): MutableVector3d {
        x /= a.toDouble()
        y /= a.toDouble()
        z /= a.toDouble()
        return this
    }

    override fun div(a: Double): MutableVector3d {
        x /= a
        y /= a
        z /= a
        return this
    }

    override fun set(x: Int,
                     y: Int): MutableVector3d {
        setX(x)
        setY(y)
        return this
    }

    override fun set(x: Long,
                     y: Long): MutableVector3d {
        setX(x)
        setY(y)
        return this
    }

    override fun set(x: Float,
                     y: Float): MutableVector3d {
        setX(x)
        setY(y)
        return this
    }

    override fun set(x: Double,
                     y: Double): MutableVector3d {
        setX(x)
        setY(y)
        return this
    }

    override fun setX(x: Int): MutableVector3d {
        this.x = x.toDouble()
        return this
    }

    override fun setX(x: Long): MutableVector3d {
        this.x = x.toDouble()
        return this
    }

    override fun setX(x: Float): MutableVector3d {
        this.x = x.toDouble()
        return this
    }

    override fun setX(x: Double): MutableVector3d {
        this.x = x
        return this
    }

    override fun plusX(x: Int): MutableVector3d {
        this.x += x.toDouble()
        return this
    }

    override fun plusX(x: Long): MutableVector3d {
        this.x += x.toDouble()
        return this
    }

    override fun plusX(x: Float): MutableVector3d {
        this.x += x.toDouble()
        return this
    }

    override fun plusX(x: Double): MutableVector3d {
        this.x += x
        return this
    }

    override fun setY(y: Int): MutableVector3d {
        this.y = y.toDouble()
        return this
    }

    override fun setY(y: Long): MutableVector3d {
        this.y = y.toDouble()
        return this
    }

    override fun setY(y: Float): MutableVector3d {
        this.y = y.toDouble()
        return this
    }

    override fun setY(y: Double): MutableVector3d {
        this.y = y
        return this
    }

    override fun plusY(y: Int): MutableVector3d {
        this.y += y.toDouble()
        return this
    }

    override fun plusY(y: Long): MutableVector3d {
        this.y += y.toDouble()
        return this
    }

    override fun plusY(y: Float): MutableVector3d {
        this.y += y.toDouble()
        return this
    }

    override fun plusY(y: Double): MutableVector3d {
        this.y += y
        return this
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other is MutableVector3d) {
            return x == other.doubleX() && y == other.doubleY() &&
                    z == other.doubleZ()
        }
        if (other !is Vector3d) {
            return false
        }
        return x == other.x && y == other.y && z == other.z
    }

    override operator fun plus(vector: Vector2d) = apply {
        x += vector.x
        y += vector.y
    }

    override operator fun plus(vector: MutableVector2d) = apply {
        x += vector.x
        y += vector.y
    }

    override operator fun minus(vector: Vector2d) = apply {
        x -= vector.x
        y -= vector.y
    }

    override operator fun minus(vector: MutableVector2d) = apply {
        x -= vector.x
        y -= vector.y
    }

    override operator fun times(vector: Vector2d) = apply {
        x *= vector.x
        y *= vector.y
    }

    override operator fun times(vector: MutableVector2d) = apply {
        x *= vector.x
        y *= vector.y
    }

    override operator fun div(vector: Vector2d) = apply {
        x /= vector.x
        y /= vector.y
    }

    override operator fun div(vector: MutableVector2d) = apply {
        x /= vector.x
        y /= vector.y
    }

    override fun unaryMinus() = apply {
        x = -x
        y = -y
        z = -z
    }

    override fun set(a: Vector2d) = apply {
        setX(a.x)
        setY(a.y)
    }

    override fun now(): Vector3d {
        return Vector3d(x, y, z)
    }

    fun set(x: Int,
            y: Int,
            z: Int): MutableVector3d {
        setX(x)
        setY(y)
        setZ(z)
        return this
    }

    fun set(x: Long,
            y: Long,
            z: Long): MutableVector3d {
        setX(x)
        setY(y)
        setZ(z)
        return this
    }

    fun set(x: Float,
            y: Float,
            z: Float): MutableVector3d {
        setX(x)
        setY(y)
        setZ(z)
        return this
    }

    fun set(x: Double,
            y: Double,
            z: Double): MutableVector3d {
        setX(x)
        setY(y)
        setZ(z)
        return this
    }

    fun setZ(z: Int): MutableVector3d {
        this.z = z.toDouble()
        return this
    }

    fun setZ(z: Long): MutableVector3d {
        this.z = z.toDouble()
        return this
    }

    fun setZ(z: Float): MutableVector3d {
        this.z = z.toDouble()
        return this
    }

    fun setZ(z: Double): MutableVector3d {
        this.z = z
        return this
    }

    fun plusZ(z: Int): MutableVector3d {
        this.z += z.toDouble()
        return this
    }

    fun plusZ(z: Long): MutableVector3d {
        this.z += z.toDouble()
        return this
    }

    fun plusZ(z: Float): MutableVector3d {
        this.z += z.toDouble()
        return this
    }

    fun plusZ(z: Double): MutableVector3d {
        this.z += z
        return this
    }

    fun intZ(): Int {
        return z.floorToInt()
    }

    fun floatZ(): Float {
        return z.toFloat()
    }

    fun doubleZ(): Double {
        return z
    }

    operator fun plus(vector: Vector3d) = apply {
        x += vector.x
        y += vector.y
        z += vector.z
    }

    operator fun plus(vector: MutableVector3d) = apply {
        x += vector.x
        y += vector.y
        z += vector.z
    }

    operator fun minus(vector: Vector3d) = apply {
        x -= vector.x
        y -= vector.y
        z -= vector.z
    }

    operator fun minus(vector: MutableVector3d) = apply {
        x -= vector.x
        y -= vector.y
        z -= vector.z
    }

    operator fun times(vector: Vector3d) = apply {
        x *= vector.x
        y *= vector.y
        z *= vector.z
    }

    operator fun times(vector: MutableVector3d) = apply {
        x *= vector.x
        y *= vector.y
        z *= vector.z
    }

    operator fun div(vector: Vector3d) = apply {
        x /= vector.x
        y /= vector.y
        z /= vector.z
    }

    operator fun div(vector: MutableVector3d) = apply {
        x /= vector.x
        y /= vector.y
        z /= vector.z
    }

    fun set(a: Vector3d): MutableVector3d {
        setX(a.x)
        setY(a.y)
        setZ(a.z)
        return this
    }

    override fun set(map: ReadTagMutableMap) {
        super.set(map)
        map["Z"]?.toDouble()?.let { z = it }
    }
}
