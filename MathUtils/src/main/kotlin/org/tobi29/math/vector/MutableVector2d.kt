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

data class MutableVector2d(
        var x: Double = 0.0,
        var y: Double = 0.0
) : Doubles {
    constructor(vector: Vector2d) : this(vector.x, vector.y)

    constructor(vector: Vector2i) : this(vector.x.toDouble(),
            vector.y.toDouble())

    constructor(vector: MutableVector2d) : this(vector.x, vector.y)

    constructor(vector: MutableVector2i) : this(vector.x.toDouble(),
            vector.y.toDouble())

    override val size: Int get() = 2

    override fun get(index: Int): Double = when (index) {
        0 -> x
        1 -> y
        else -> throw IndexOutOfBoundsException("$index")
    }

    override fun set(index: Int,
                     value: Double): Unit = when (index) {
        0 -> x = value
        1 -> y = value
        else -> throw IndexOutOfBoundsException("$index")
    }

    operator fun plus(a: Double) = apply {
        x += a
        y += a
    }

    operator fun minus(a: Double) = apply {
        x -= a
        y -= a
    }

    fun times(a: Double) = apply {
        x *= a
        y *= a
    }

    operator fun div(a: Double) = apply {
        x /= a
        y /= a
    }

    operator fun unaryMinus() = apply {
        x = -x
        y = -y
    }

    fun setXY(x: Double,
              y: Double) = apply {
        setX(x)
        setY(y)
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

    fun set(a: Vector2d) = apply {
        setX(a.x)
        setY(a.y)
    }

    fun set(a: MutableVector2d) = apply {
        setX(a.x)
        setY(a.y)
    }

    fun now(): Vector2d = Vector2d(x, y)

    fun set(map: ReadTagMutableMap) {
        map["X"]?.toDouble()?.let { x = it }
        map["Y"]?.toDouble()?.let { y = it }
    }
}
