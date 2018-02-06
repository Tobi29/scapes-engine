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

package org.tobi29.math.vector

import org.tobi29.arrays.Doubles
import org.tobi29.io.tag.ReadTagMutableMap
import org.tobi29.io.tag.toDouble

data class MutableVector3d(
    override var x: Double = 0.0,
    override var y: Double = 0.0,
    override var z: Double = 0.0
) : ReadVector3d, Doubles {
    constructor(vector: Vector3d) : this(vector.x, vector.y, vector.z)

    constructor(vector: Vector3i) : this(
        vector.x.toDouble(),
        vector.y.toDouble(), vector.z.toDouble()
    )

    constructor(vector: MutableVector3d) : this(vector.x, vector.y, vector.z)

    constructor(vector: MutableVector3i) : this(
        vector.x.toDouble(),
        vector.y.toDouble(), vector.z.toDouble()
    )

    fun now(): Vector3d = Vector3d(x, y, z)

    override fun set(
        index: Int,
        value: Double
    ): Unit = when (index) {
        0 -> x = value
        1 -> y = value
        2 -> z = value
        else -> throw IndexOutOfBoundsException("$index")
    }

    fun setX(x: Double) = apply {
        this.x = x
    }

    fun setY(y: Double) = apply {
        this.y = y
    }

    fun setZ(z: Double) = apply {
        this.z = z
    }

    fun setXYZ(
        x: Double,
        y: Double,
        z: Double
    ) = apply {
        this.x = x
        this.y = y
        this.z = z
    }

    fun set(a: Vector3d) = apply {
        x = a.x
        y = a.y
        z = a.z
    }

    fun negate() = apply {
        x = -x
        y = -y
        z = -z
    }

    fun add(a: Double) = apply {
        x += a
        y += a
        z += a
    }

    fun addX(x: Double) = apply {
        this.x += x
    }

    fun addY(y: Double) = apply {
        this.y += y
    }

    fun addZ(z: Double) = apply {
        this.z += z
    }

    fun add(vector: Vector3d) = apply {
        x += vector.x
        y += vector.y
        z += vector.z
    }

    fun add(vector: MutableVector3d) = apply {
        x += vector.x
        y += vector.y
        z += vector.z
    }

    fun subtract(a: Double) = apply {
        x -= a
        y -= a
        z -= a
    }

    fun subtract(vector: Vector3d) = apply {
        x -= vector.x
        y -= vector.y
        z -= vector.z
    }

    fun subtract(vector: MutableVector3d) = apply {
        x -= vector.x
        y -= vector.y
        z -= vector.z
    }

    fun multiply(a: Double) = apply {
        x *= a
        y *= a
        z *= a
    }

    fun multiply(vector: Vector3d) = apply {
        x *= vector.x
        y *= vector.y
        z *= vector.z
    }

    fun multiply(vector: MutableVector3d) = apply {
        x *= vector.x
        y *= vector.y
        z *= vector.z
    }

    fun divide(a: Double) = apply {
        x /= a
        y /= a
        z /= a
    }

    fun divide(vector: Vector3d) = apply {
        x /= vector.x
        y /= vector.y
        z /= vector.z
    }

    fun divide(vector: MutableVector3d) = apply {
        x /= vector.x
        y /= vector.y
        z /= vector.z
    }

    fun set(map: ReadTagMutableMap) {
        map["X"]?.toDouble()?.let { x = it }
        map["Y"]?.toDouble()?.let { y = it }
        map["Z"]?.toDouble()?.let { z = it }
    }
}
