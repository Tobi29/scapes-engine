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

package org.tobi29.scapes.engine.utils.math.vector

import org.tobi29.scapes.engine.utils.math.floor
import org.tobi29.scapes.engine.utils.tag.ReadWriteTagMap
import org.tobi29.scapes.engine.utils.tag.toTag
import kotlin.collections.set

class Vector3d(x: Double,
               y: Double,
               val z: Double) : Vector2d(x, y) {

    constructor(vector: Vector3i) : this(vector.x + 0.5, vector.y + 0.5,
            vector.z + 0.5)

    override fun hasNaN(): Boolean {
        return x.isNaN() || y.isNaN() || z.isNaN()
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other is MutableVector3d) {
            return x == other.doubleX() && y == other.doubleY() &&
                    z == other.doubleZ()
        }
        if (other !is Vector3d) {
            return false
        }
        return x == other.x && y == other.y && z == other.z
    }

    fun intZ(): Int {
        return floor(z)
    }

    fun floatZ(): Float {
        return z.toFloat()
    }

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Z"] = z.toTag()
    }

    override fun toString() = "$x $y $z"

    companion object {
        val ZERO = Vector3d(0.0, 0.0, 0.0)
    }
}
