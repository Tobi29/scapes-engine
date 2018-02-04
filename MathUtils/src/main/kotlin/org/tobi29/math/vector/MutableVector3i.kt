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

import org.tobi29.arrays.Ints
import org.tobi29.io.tag.ReadTagMutableMap
import org.tobi29.io.tag.toInt

data class MutableVector3i(
    var x: Int = 0,
    var y: Int = 0,
    var z: Int = 0
) : Ints {
    constructor(vector: Vector3i) : this(vector.x, vector.y, vector.z)

    constructor(vector: MutableVector3i) : this(vector.x, vector.y, vector.z)

    override val size: Int get() = 3

    fun now(): Vector3i = Vector3i(x, y, z)

    override fun get(index: Int): Int = when (index) {
        0 -> x
        1 -> y
        2 -> z
        else -> throw IndexOutOfBoundsException("$index")
    }

    override fun set(
        index: Int,
        value: Int
    ): Unit = when (index) {
        0 -> x = value
        1 -> y = value
        2 -> z = value
        else -> throw IndexOutOfBoundsException("$index")
    }

    fun setX(x: Int) = apply {
        this.x = x
    }

    fun setY(y: Int) = apply {
        this.y = y
    }

    fun setZ(z: Int) = apply {
        this.z = z
    }

    fun setXYZ(
        x: Int,
        y: Int,
        z: Int
    ) = apply {
        this.x = x
        this.y = y
        this.z = z
    }

    fun set(a: Vector3i) = apply {
        x = a.x
        y = a.y
        z = a.z
    }

    fun negate() = apply {
        x = -x
        y = -y
        z = -z
    }

    fun add(a: Int) = apply {
        x += a
        y += a
        z += a
    }

    fun addX(x: Int) = apply {
        this.x += x
    }

    fun addY(y: Int) = apply {
        this.y += y
    }

    fun addZ(z: Int) = apply {
        this.z += z
    }

    fun add(vector: Vector3i) = apply {
        x += vector.x
        y += vector.y
        z += vector.z
    }

    fun add(vector: MutableVector3i) = apply {
        x += vector.x
        y += vector.y
        z += vector.z
    }

    fun subtract(a: Int) = apply {
        x -= a
        y -= a
        z -= a
    }

    fun subtract(vector: Vector3i) = apply {
        x -= vector.x
        y -= vector.y
        z -= vector.z
    }

    fun subtract(vector: MutableVector3i) = apply {
        x -= vector.x
        y -= vector.y
        z -= vector.z
    }

    fun multiply(a: Int) = apply {
        x *= a
        y *= a
        z *= a
    }

    fun multiply(vector: Vector3i) = apply {
        x *= vector.x
        y *= vector.y
        z *= vector.z
    }

    fun multiply(vector: MutableVector3i) = apply {
        x *= vector.x
        y *= vector.y
        z *= vector.z
    }

    fun divide(a: Int) = apply {
        x /= a
        y /= a
        z /= a
    }

    fun divide(vector: Vector3i) = apply {
        x /= vector.x
        y /= vector.y
        z /= vector.z
    }

    fun divide(vector: MutableVector3i) = apply {
        x /= vector.x
        y /= vector.y
        z /= vector.z
    }

    fun set(map: ReadTagMutableMap) {
        map["X"]?.toInt()?.let { x = it }
        map["Y"]?.toInt()?.let { y = it }
        map["Z"]?.toInt()?.let { z = it }
    }
}
