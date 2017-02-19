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

import org.tobi29.scapes.engine.utils.io.tag.ReadWriteTagMap
import org.tobi29.scapes.engine.utils.io.tag.TagMapWrite
import org.tobi29.scapes.engine.utils.io.tag.set
import org.tobi29.scapes.engine.utils.math.floor

open class Vector2d(val x: Double,
                    val y: Double) : TagMapWrite {
    constructor(vector: Vector2i) : this(vector.x + 0.5, vector.y + 0.5)

    fun intX(): Int {
        return floor(x)
    }

    fun floatX(): Float {
        return x.toFloat()
    }

    fun intY(): Int {
        return floor(y)
    }

    fun floatY(): Float {
        return y.toFloat()
    }

    open fun hasNaN(): Boolean {
        return x.isNaN() || y.isNaN()
    }

    override fun hashCode(): Int {
        var temp: Long
        temp = java.lang.Double.doubleToLongBits(x)
        var result = (temp xor temp.ushr(32)).toInt()
        temp = java.lang.Double.doubleToLongBits(y)
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
        if (other is MutableVector2d) {
            return x == other.doubleX() && y == other.doubleY()
        }
        if (other !is Vector2d) {
            return false
        }
        return x == other.x && y == other.y
    }

    override fun write(map: ReadWriteTagMap) {
        map["X"] = x
        map["Y"] = y
    }

    override fun toString() = "$x $y"

    companion object {
        val ZERO = Vector2d(0.0, 0.0)
    }
}
