/*
 * Copyright 2012-2018 Tobi29
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

data class MutableVector2i(
    override var x: Int = 0,
    override var y: Int = 0
) : ReadVector2i, Ints {
    constructor(vector: ReadVector2i) : this(vector.x, vector.y)

    fun now(): Vector2i = Vector2i(x, y)

    override fun set(
        index: Int,
        value: Int
    ): Unit = when (index) {
        0 -> x = value
        1 -> y = value
        else -> throw IndexOutOfBoundsException("$index")
    }

    fun setX(x: Int) = apply {
        this.x = x
    }

    fun setY(y: Int) = apply {
        this.y = y
    }

    fun setXY(
        x: Int,
        y: Int
    ) = apply {
        this.x = x
        this.y = y
    }

    fun set(a: ReadVector2i) = apply {
        x = a.x
        y = a.y
    }

    fun set(map: ReadTagMutableMap) {
        map["X"]?.toInt()?.let { x = it }
        map["Y"]?.toInt()?.let { y = it }
    }
}
