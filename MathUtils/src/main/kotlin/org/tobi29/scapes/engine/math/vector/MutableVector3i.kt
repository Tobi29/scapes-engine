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

import org.tobi29.scapes.engine.utils.Ints
import org.tobi29.scapes.engine.utils.tag.ReadTagMutableMap
import org.tobi29.scapes.engine.utils.tag.toInt

data class MutableVector3i(var x: Int = 0,
                           var y: Int = 0,
                           var z: Int = 0) : Ints {
    constructor(vector: Vector3i) : this(vector.x, vector.y, vector.z)

    constructor(vector: MutableVector3i) : this(vector.x, vector.y, vector.z)

    override val size: Int get() = 3

    override fun get(index: Int): Int = when (index) {
        0 -> x
        1 -> y
        2 -> z
        else -> throw IndexOutOfBoundsException("$index")
    }

    override fun set(index: Int,
                     value: Int): Unit = when (index) {
        0 -> x = value
        1 -> y = value
        2 -> z = value
        else -> throw IndexOutOfBoundsException("$index")
    }

    fun plus(a: Int) = apply {
        x += a
        y += a
        z += a
    }

    fun minus(a: Int) = apply {
        x -= a
        y -= a
        z -= a
    }

    fun multiply(a: Int) = apply {
        x *= a
        y *= a
        z *= a
    }

    fun div(a: Int) = apply {
        x /= a
        y /= a
        z /= a
    }

    fun plusX(x: Int) = apply {
        this.x += x
    }

    fun plusY(y: Int) = apply {
        this.y += y
    }

    fun setX(x: Int) = apply {
        this.x = x
    }

    fun setY(y: Int) = apply {
        this.y = y
    }

    fun plus(vector: Vector2i) = apply {
        x += vector.x
        y += vector.y
    }

    fun minus(vector: Vector2i) = apply {
        x -= vector.x
        y -= vector.y
    }

    fun multiply(vector: Vector2i) = apply {
        x *= vector.x
        y *= vector.y
    }

    fun div(vector: Vector2i) = apply {
        x /= vector.x
        y /= vector.y
    }

    fun set(a: Vector2i) = apply {
        x = a.x
        y = a.y
    }

    fun now(): Vector3i = Vector3i(x, y, z)

    fun setXYZ(x: Int,
               y: Int,
               z: Int) = apply {
        setX(x)
        setY(y)
        setZ(z)
    }

    fun plusZ(z: Int) = apply {
        this.z += z
    }

    fun setZ(z: Int) = apply {
        this.z = z
    }

    operator fun plus(vector: Vector3i) = apply {
        x += vector.x
        y += vector.y
        z += vector.z
    }

    operator fun minus(vector: Vector3i) = apply {
        x -= vector.x
        y -= vector.y
        z -= vector.z
    }

    fun multiply(vector: Vector3i) = apply {
        x *= vector.x
        y *= vector.y
        z *= vector.z
    }

    operator fun div(vector: Vector3i) = apply {
        x /= vector.x
        y /= vector.y
        z /= vector.z
    }

    fun set(a: Vector3i) = apply {
        setX(a.x)
        setY(a.y)
        setZ(a.z)
    }

    fun set(map: ReadTagMutableMap) {
        map["X"]?.toInt()?.let { x = it }
        map["Y"]?.toInt()?.let { y = it }
        map["Z"]?.toInt()?.let { z = it }
    }
}
