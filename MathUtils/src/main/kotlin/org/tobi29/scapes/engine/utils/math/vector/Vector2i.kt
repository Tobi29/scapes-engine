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

import org.tobi29.scapes.engine.utils.tag.*

open class Vector2i(val x: Int,
                    val y: Int) : TagMapWrite {

    constructor(vector: Vector2d) : this(vector.intX(), vector.intY())

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other is MutableVector2i) {
            return x == other.x && y == other.y
        }
        if (other !is Vector2i) {
            return false
        }
        return x == other.x && y == other.y
    }

    override fun write(map: ReadWriteTagMap) {
        map["X"] = x.toTag()
        map["Y"] = y.toTag()
    }

    override fun toString() = "$x $y"

    companion object {
        val ZERO = Vector2i(0, 0)
    }
}

fun MutableTag.toVector2i(): Vector2i? {
    val map = toMap() ?: return null
    val x = map["X"]?.toInt() ?: return null
    val y = map["Y"]?.toInt() ?: return null
    return Vector2i(x, y)
}
