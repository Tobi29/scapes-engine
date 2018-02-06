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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.math.vector

import org.tobi29.arrays.DoublesRO
import org.tobi29.io.tag.*
import kotlin.collections.set

interface ReadVector2d : DoublesRO, TagMapWrite {
    val x: Double
    val y: Double

    override val size: Int get() = 2

    override fun get(index: Int): Double = when (index) {
        0 -> x
        1 -> y
        else -> throw IndexOutOfBoundsException("$index")
    }

    override fun write(map: ReadWriteTagMap) {
        map["X"] = x.toTag()
        map["Y"] = y.toTag()
    }
}

data class Vector2d(
    override val x: Double,
    override val y: Double
) : ReadVector2d {
    constructor(vector: Vector2i) : this(
        vector.x.toDouble(), vector.y.toDouble()
    )

    override fun toString() = "$x $y"

    companion object {
        val ZERO = Vector2d(0.0, 0.0)
    }
}

inline fun Vector2d.hasNaN(): Boolean =
    x.isNaN() || y.isNaN()

fun MutableTag.toVector2d(): Vector2d? {
    val map = toMap() ?: return null
    val x = map["X"]?.toDouble() ?: return null
    val y = map["Y"]?.toDouble() ?: return null
    return Vector2d(x, y)
}
