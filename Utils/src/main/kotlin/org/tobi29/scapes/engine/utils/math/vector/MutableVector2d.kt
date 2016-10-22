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

import org.tobi29.scapes.engine.utils.io.tag.MultiTag
import org.tobi29.scapes.engine.utils.io.tag.TagStructure
import org.tobi29.scapes.engine.utils.io.tag.getDouble
import org.tobi29.scapes.engine.utils.io.tag.setDouble
import org.tobi29.scapes.engine.utils.math.floor

open class MutableVector2d(var x: Double = 0.0, var y: Double = 0.0) : MultiTag.ReadAndWrite {

    constructor(vector: Vector2d) : this(vector.x, vector.y) {
    }

    constructor(vector: Vector2i) : this(vector.x.toDouble() + 0.5,
            vector.y.toDouble() + 0.5) {
    }

    constructor(vector: MutableVector2d) : this(vector.doubleX(),
            vector.doubleY()) {
    }

    constructor(vector: MutableVector2i) : this(vector.x.toDouble() + 0.5,
            vector.y.toDouble() + 0.5) {
    }

    open operator fun plus(a: Int): MutableVector2d {
        x += a.toDouble()
        y += a.toDouble()
        return this
    }

    open operator fun plus(a: Long): MutableVector2d {
        x += a.toDouble()
        y += a.toDouble()
        return this
    }

    open operator fun plus(a: Float): MutableVector2d {
        x += a.toDouble()
        y += a.toDouble()
        return this
    }

    open operator fun plus(a: Double): MutableVector2d {
        x += a
        y += a
        return this
    }

    open operator fun minus(a: Int): MutableVector2d {
        x -= a.toDouble()
        y -= a.toDouble()
        return this
    }

    open operator fun minus(a: Long): MutableVector2d {
        x -= a.toDouble()
        y -= a.toDouble()
        return this
    }

    open operator fun minus(a: Float): MutableVector2d {
        x -= a.toDouble()
        y -= a.toDouble()
        return this
    }

    open operator fun minus(a: Double): MutableVector2d {
        x -= a
        y -= a
        return this
    }

    open fun multiply(a: Int): MutableVector2d {
        x *= a.toDouble()
        y *= a.toDouble()
        return this
    }

    open fun multiply(a: Long): MutableVector2d {
        x *= a.toDouble()
        y *= a.toDouble()
        return this
    }

    open fun multiply(a: Float): MutableVector2d {
        x *= a.toDouble()
        y *= a.toDouble()
        return this
    }

    open fun multiply(a: Double): MutableVector2d {
        x *= a
        y *= a
        return this
    }

    open operator fun div(a: Int): MutableVector2d {
        x /= a.toDouble()
        y /= a.toDouble()
        return this
    }

    open operator fun div(a: Long): MutableVector2d {
        x /= a.toDouble()
        y /= a.toDouble()
        return this
    }

    open operator fun div(a: Float): MutableVector2d {
        x /= a.toDouble()
        y /= a.toDouble()
        return this
    }

    open operator fun div(a: Double): MutableVector2d {
        x /= a
        y /= a
        return this
    }

    open fun set(x: Int,
                 y: Int): MutableVector2d {
        setX(x)
        setY(y)
        return this
    }

    open fun set(x: Long,
                 y: Long): MutableVector2d {
        setX(x)
        setY(y)
        return this
    }

    open fun set(x: Float,
                 y: Float): MutableVector2d {
        setX(x)
        setY(y)
        return this
    }

    open fun set(x: Double,
                 y: Double): MutableVector2d {
        setX(x)
        setY(y)
        return this
    }

    open fun setX(x: Int): MutableVector2d {
        this.x = x.toDouble()
        return this
    }

    open fun setX(x: Long): MutableVector2d {
        this.x = x.toInt().toDouble()
        return this
    }

    open fun setX(x: Float): MutableVector2d {
        this.x = x.toDouble()
        return this
    }

    open fun setX(x: Double): MutableVector2d {
        this.x = x
        return this
    }

    open fun plusX(x: Int): MutableVector2d {
        this.x += x.toDouble()
        return this
    }

    open fun plusX(x: Long): MutableVector2d {
        this.x += x.toDouble()
        return this
    }

    open fun plusX(x: Float): MutableVector2d {
        this.x += x.toDouble()
        return this
    }

    open fun plusX(x: Double): MutableVector2d {
        this.x += x
        return this
    }

    open fun setY(y: Int): MutableVector2d {
        this.y = y.toDouble()
        return this
    }

    open fun setY(y: Long): MutableVector2d {
        this.y = y.toInt().toDouble()
        return this
    }

    open fun setY(y: Float): MutableVector2d {
        this.y = y.toDouble()
        return this
    }

    open fun setY(y: Double): MutableVector2d {
        this.y = y
        return this
    }

    open fun plusY(y: Int): MutableVector2d {
        this.y += y.toDouble()
        return this
    }

    open fun plusY(y: Long): MutableVector2d {
        this.y += y.toDouble()
        return this
    }

    open fun plusY(y: Float): MutableVector2d {
        this.y += y.toDouble()
        return this
    }

    open fun plusY(y: Double): MutableVector2d {
        this.y += y
        return this
    }

    fun intX(): Int {
        return floor(x)
    }

    fun floatX(): Float {
        return x.toFloat()
    }

    fun doubleX(): Double {
        return x
    }

    fun intY(): Int {
        return floor(y)
    }

    fun floatY(): Float {
        return y.toFloat()
    }

    fun doubleY(): Double {
        return y
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

    open operator fun plus(vector: Vector2d): MutableVector2d {
        x += vector.x
        y += vector.y
        return this
    }

    open operator fun minus(vector: Vector2d): MutableVector2d {
        x -= vector.x
        y -= vector.y
        return this
    }

    open fun multiply(vector: Vector2d): MutableVector2d {
        x *= vector.x
        y *= vector.y
        return this
    }

    open operator fun div(vector: Vector2d): MutableVector2d {
        x /= vector.x
        y /= vector.y
        return this
    }

    open fun set(a: Vector2d): MutableVector2d {
        setX(a.x)
        setY(a.y)
        return this
    }

    open fun now(): Vector2d {
        return Vector2d(x, y)
    }

    override fun write(): TagStructure {
        val tagStructure = TagStructure()
        tagStructure.setDouble("X", x)
        tagStructure.setDouble("Y", y)
        return tagStructure
    }

    override fun read(tagStructure: TagStructure) {
        var value: Double?
        value = tagStructure.getDouble("X")
        if (value != null) {
            x = value
        }
        value = tagStructure.getDouble("Y")
        if (value != null) {
            y = value
        }
    }
}