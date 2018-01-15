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

import org.tobi29.scapes.engine.utils.Doubles
import org.tobi29.scapes.engine.utils.tag.ReadTagMutableMap
import org.tobi29.scapes.engine.utils.tag.toDouble

data class MutableVector3d(
        var x: Double = 0.0,
        var y: Double = 0.0,
        var z: Double = 0.0
) : Doubles {
    constructor(vector: Vector3d) : this(vector.x, vector.y, vector.z)

    constructor(vector: Vector3i) : this(vector.x.toDouble(),
            vector.y.toDouble(), vector.z.toDouble())

    constructor(vector: MutableVector3d) : this(vector.x, vector.y, vector.z)

    constructor(vector: MutableVector3i) : this(vector.x.toDouble(),
            vector.y.toDouble(), vector.z.toDouble())

    override val size: Int get() = 3

    override fun get(index: Int): Double = when (index) {
        0 -> x
        1 -> y
        2 -> z
        else -> throw IndexOutOfBoundsException("$index")
    }

    override fun set(index: Int,
                     value: Double): Unit = when (index) {
        0 -> x = value
        1 -> y = value
        2 -> z = value
        else -> throw IndexOutOfBoundsException("$index")
    }

    operator fun plus(a: Double) = apply {
        x += a
        y += a
        z += a
    }

    operator fun minus(a: Double) = apply {
        x -= a
        y -= a
        z -= a
    }

    operator fun times(a: Double) = apply {
        x *= a
        y *= a
        z *= a
    }

    operator fun div(a: Double) = apply {
        x /= a
        y /= a
        z /= a
    }

    fun setX(x: Double) = apply {
        this.x = x
    }

    fun plusX(x: Double) = apply {
        this.x += x
    }

    fun setY(y: Double) = apply {
        this.y = y
    }

    fun plusY(y: Double) = apply {
        this.y += y
    }

    operator fun plus(vector: Vector2d) = apply {
        x += vector.x
        y += vector.y
    }

    operator fun plus(vector: MutableVector2d) = apply {
        x += vector.x
        y += vector.y
    }

    operator fun minus(vector: Vector2d) = apply {
        x -= vector.x
        y -= vector.y
    }

    operator fun minus(vector: MutableVector2d) = apply {
        x -= vector.x
        y -= vector.y
    }

    operator fun times(vector: Vector2d) = apply {
        x *= vector.x
        y *= vector.y
    }

    operator fun times(vector: MutableVector2d) = apply {
        x *= vector.x
        y *= vector.y
    }

    operator fun div(vector: Vector2d) = apply {
        x /= vector.x
        y /= vector.y
    }

    operator fun div(vector: MutableVector2d) = apply {
        x /= vector.x
        y /= vector.y
    }

    fun unaryMinus() = apply {
        x = -x
        y = -y
        z = -z
    }

    fun set(a: Vector2d) = apply {
        setX(a.x)
        setY(a.y)
    }

    fun now(): Vector3d = Vector3d(x, y, z)

    fun setXYZ(x: Double,
               y: Double,
               z: Double) = apply {
        setX(x)
        setY(y)
        setZ(z)
    }

    fun setZ(z: Double) = apply {
        this.z = z
    }

    fun plusZ(z: Double) = apply {
        this.z += z
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

    fun set(a: Vector3d) = apply {
        setX(a.x)
        setY(a.y)
        setZ(a.z)
    }

    fun set(map: ReadTagMutableMap) {
        map["X"]?.toDouble()?.let { x = it }
        map["Y"]?.toDouble()?.let { y = it }
        map["Z"]?.toDouble()?.let { z = it }
    }
}
