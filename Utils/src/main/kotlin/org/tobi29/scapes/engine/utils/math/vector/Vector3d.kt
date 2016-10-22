/*
 * Copyright 2012-2016 Tobi29
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

import org.tobi29.scapes.engine.utils.io.tag.TagStructure
import org.tobi29.scapes.engine.utils.io.tag.setDouble
import org.tobi29.scapes.engine.utils.math.floor

class Vector3d(x: Double, y: Double, val z: Double) : Vector2d(x, y) {

    constructor(vector: Vector3i) : this(vector.x + 0.5, vector.y + 0.5,
            vector.z + 0.5) {
    }

    override fun hasNaN(): Boolean {
        return x.isNaN() || y.isNaN() || z.isNaN()
    }

    override fun hashCode(): Int {
        var temp: Long
        temp = java.lang.Double.doubleToLongBits(x)
        var result = (temp xor temp.ushr(32)).toInt()
        temp = java.lang.Double.doubleToLongBits(y)
        result = 31 * result + (temp xor temp.ushr(32)).toInt()
        temp = java.lang.Double.doubleToLongBits(z)
        result = 31 * result + (temp xor temp.ushr(32)).toInt()
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

    override fun write(): TagStructure {
        val tagStructure = super.write()
        tagStructure.setDouble("Z", z)
        return tagStructure
    }

    fun intZ(): Int {
        return floor(z)
    }

    fun floatZ(): Float {
        return z.toFloat()
    }

    companion object {
        val ZERO = Vector3d(0.0, 0.0, 0.0)
    }
}
