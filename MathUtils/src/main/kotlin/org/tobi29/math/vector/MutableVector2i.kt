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

data class MutableVector2i(var x: Int = 0,
                           var y: Int = 0) : Ints {
    constructor(vector: Vector2i) : this(vector.x, vector.y)

    constructor(vector: MutableVector2i) : this(vector.x, vector.y)

    override val size: Int get() = 3

    override fun get(index: Int): Int = when (index) {
        0 -> x
        1 -> y
        else -> throw IndexOutOfBoundsException("$index")
    }

    override fun set(index: Int,
                     value: Int): Unit = when (index) {
        0 -> x = value
        1 -> y = value
        else -> throw IndexOutOfBoundsException("$index")
    }

    operator fun plus(a: Int) = apply {
        x += a
        y += a
        return this
    }

    operator fun minus(a: Int) = apply {
        x -= a
        y -= a
        return this
    }

    fun multiply(a: Int) = apply {
        x *= a
        y *= a
        return this
    }

    operator fun div(a: Int) = apply {
        x /= a
        y /= a
        return this
    }

    fun setXY(x: Int,
              y: Int) = apply {
        setX(x)
        setY(y)
    }

    fun plusX(x: Int) = apply {
        this.x += x
        return this
    }

    fun plusY(y: Int) = apply {
        this.y += y
        return this
    }

    fun setX(x: Int) = apply {
        this.x = x
        return this
    }

    fun setY(y: Int) = apply {
        this.y = y
        return this
    }

    operator fun plus(vector: Vector2i) = apply {
        x += vector.x
        y += vector.y
        return this
    }

    operator fun minus(vector: Vector2i) = apply {
        x -= vector.x
        y -= vector.y
        return this
    }

    fun multiply(vector: Vector2i) = apply {
        x *= vector.x
        y *= vector.y
        return this
    }

    operator fun div(vector: Vector2i) = apply {
        x /= vector.x
        y /= vector.y
        return this
    }

    fun set(a: Vector2i) = apply {
        setX(a.x)
        setY(a.y)
        return this
    }

    fun now(): Vector2i = Vector2i(x, y)

    fun set(map: ReadTagMutableMap) {
        map["X"]?.toInt()?.let { x = it }
        map["Y"]?.toInt()?.let { y = it }
    }
}
