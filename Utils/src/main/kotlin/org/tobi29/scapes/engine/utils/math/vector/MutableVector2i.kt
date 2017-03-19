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

import org.tobi29.scapes.engine.utils.tag.ReadTagMutableMap
import org.tobi29.scapes.engine.utils.tag.toInt

open class MutableVector2i(var x: Int = 0,
                           var y: Int = 0) {

    constructor(vector: Vector2d) : this(vector.intX(), vector.intY())

    constructor(vector: Vector2i) : this(vector.x, vector.y)

    constructor(vector: MutableVector2d) : this(vector.intX(), vector.intY())

    constructor(vector: MutableVector2i) : this(vector.x, vector.y)

    open operator fun plus(a: Int): MutableVector2i {
        x += a
        y += a
        return this
    }

    open operator fun minus(a: Int): MutableVector2i {
        x -= a
        y -= a
        return this
    }

    open fun multiply(a: Int): MutableVector2i {
        x *= a
        y *= a
        return this
    }

    open operator fun div(a: Int): MutableVector2i {
        x /= a
        y /= a
        return this
    }

    open fun set(x: Int,
                 y: Int): MutableVector2i {
        setX(x)
        setY(y)
        return this
    }

    open fun plusX(x: Int): MutableVector2i {
        this.x += x
        return this
    }

    open fun plusY(y: Int): MutableVector2i {
        this.y += y
        return this
    }

    open fun setX(x: Int): MutableVector2i {
        this.x = x
        return this
    }

    open fun setY(y: Int): MutableVector2i {
        this.y = y
        return this
    }

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

    open operator fun plus(vector: Vector2i): MutableVector2i {
        x += vector.x
        y += vector.y
        return this
    }

    open operator fun minus(vector: Vector2i): MutableVector2i {
        x -= vector.x
        y -= vector.y
        return this
    }

    open fun multiply(vector: Vector2i): MutableVector2i {
        x *= vector.x
        y *= vector.y
        return this
    }

    open operator fun div(vector: Vector2i): MutableVector2i {
        x /= vector.x
        y /= vector.y
        return this
    }

    open fun set(a: Vector2i): MutableVector2i {
        setX(a.x)
        setY(a.y)
        return this
    }

    open fun now(): Vector2i {
        return Vector2i(x, y)
    }

    open fun set(map: ReadTagMutableMap) {
        map["X"]?.toInt()?.let { x = it }
        map["Y"]?.toInt()?.let { y = it }
    }
}
