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

import org.tobi29.arrays.IntsRO
import org.tobi29.io.tag.*
import kotlin.collections.set

interface ReadVector3i : IntsRO, TagMapWrite {
    val x: Int
    val y: Int
    val z: Int

    override val size: Int get() = 3

    override fun get(index: Int): Int = when (index) {
        0 -> x
        1 -> y
        2 -> y
        else -> throw IndexOutOfBoundsException("$index")
    }

    override fun write(map: ReadWriteTagMap) {
        map["X"] = x.toTag()
        map["Y"] = y.toTag()
        map["Z"] = z.toTag()
    }
}

data class Vector3i(
    override val x: Int,
    override val y: Int,
    override val z: Int
) : ReadVector3i {
    override fun toString() = "$x $y $z"

    companion object {
        val ZERO = Vector3i(0, 0, 0)
    }
}

fun MutableTag.toVector3i(): Vector3i? {
    val map = toMap() ?: return null
    val x = map["X"]?.toInt() ?: return null
    val y = map["Y"]?.toInt() ?: return null
    val z = map["Z"]?.toInt() ?: return null
    return Vector3i(x, y, z)
}
