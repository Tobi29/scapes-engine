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

interface ReadVector3d : DoublesRO, TagMapWrite {
    val x: Double
    val y: Double
    val z: Double

    override val size: Int get() = 3

    override fun get(index: Int): Double = when (index) {
        0 -> x
        1 -> y
        2 -> z
        else -> throw IndexOutOfBoundsException("$index")
    }

    override fun write(map: ReadWriteTagMap) {
        map["X"] = x.toTag()
        map["Y"] = y.toTag()
        map["Z"] = z.toTag()
    }
}

data class Vector3d(
    override val x: Double,
    override val y: Double,
    override val z: Double
) : ReadVector3d {
    constructor(vector: Vector3i) : this(
        vector.x.toDouble(), vector.y.toDouble(), vector.z.toDouble()
    )

    override fun toString() = "$x $y $z"

    companion object {
        val ZERO = Vector3d(0.0, 0.0, 0.0)
    }
}

inline fun Vector3d.hasNaN(): Boolean =
    x.isNaN() || y.isNaN() || z.isNaN()

fun MutableTag.toVector3d(): Vector3d? {
    val map = toMap() ?: return null
    val x = map["X"]?.toDouble() ?: return null
    val y = map["Y"]?.toDouble() ?: return null
    val z = map["Z"]?.toDouble() ?: return null
    return Vector3d(x, y, z)
}
