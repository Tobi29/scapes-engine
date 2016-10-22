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
import org.tobi29.scapes.engine.utils.io.tag.getInt
import org.tobi29.scapes.engine.utils.io.tag.setInt

class MutableVector3i(x: Int = 0, y: Int = 0, var z: Int = 0) : MutableVector2i(
        x, y) {

    constructor(vector: Vector3d) : this(vector.intX(), vector.intY(),
            vector.intZ()) {
    }

    constructor(vector: Vector3i) : this(vector.x, vector.y, vector.z) {
    }

    constructor(vector: MutableVector3d) : this(vector.intX(), vector.intY(),
            vector.intZ()) {
    }

    constructor(vector: MutableVector3i) : this(vector.x, vector.y, vector.z) {
    }

    override fun plus(a: Int): MutableVector3i {
        x += a
        y += a
        z += a
        return this
    }

    override fun minus(a: Int): MutableVector3i {
        x -= a
        y -= a
        z -= a
        return this
    }

    override fun multiply(a: Int): MutableVector3i {
        x *= a
        y *= a
        z *= a
        return this
    }

    override fun div(a: Int): MutableVector3i {
        x /= a
        y /= a
        z /= a
        return this
    }

    override fun set(x: Int,
                     y: Int): MutableVector3i {
        setX(x)
        setY(y)
        return this
    }

    override fun plusX(x: Int): MutableVector3i {
        this.x += x
        return this
    }

    override fun plusY(y: Int): MutableVector3i {
        this.y += y
        return this
    }

    override fun setX(x: Int): MutableVector3i {
        this.x = x
        return this
    }

    override fun setY(y: Int): MutableVector3i {
        this.y = y
        return this
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + z
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other is MutableVector3i) {
            return x == other.x && y == other.y && z == other.z
        }
        if (other !is Vector3i) {
            return false
        }
        return x == other.x && y == other.y && z == other.z
    }

    override fun plus(vector: Vector2i): MutableVector3i {
        x += vector.x
        y += vector.y
        return this
    }

    override fun minus(vector: Vector2i): MutableVector3i {
        x -= vector.x
        y -= vector.y
        return this
    }

    override fun multiply(vector: Vector2i): MutableVector3i {
        x *= vector.x
        y *= vector.y
        return this
    }

    override fun div(vector: Vector2i): MutableVector3i {
        x /= vector.x
        y /= vector.y
        return this
    }

    override fun set(a: Vector2i): MutableVector3i {
        x = a.x
        y = a.y
        return this
    }

    override fun now(): Vector3i {
        return Vector3i(x, y, z)
    }

    override fun write(): TagStructure {
        val tagStructure = super.write()
        tagStructure.setInt("Z", z)
        return tagStructure
    }

    override fun read(tagStructure: TagStructure) {
        super.read(tagStructure)
        val value: Int?
        value = tagStructure.getInt("Z")
        if (value != null) {
            z = value
        }
    }

    fun set(x: Int,
            y: Int,
            z: Int): MutableVector3i {
        setX(x)
        setY(y)
        setZ(z)
        return this
    }

    fun plusZ(z: Int): MutableVector3i {
        this.z += z
        return this
    }

    fun setZ(z: Int): MutableVector3i {
        this.z = z
        return this
    }

    operator fun plus(vector: Vector3i): MutableVector3i {
        x += vector.x
        y += vector.y
        z += vector.z
        return this
    }

    operator fun minus(vector: Vector3i): MutableVector3i {
        x -= vector.x
        y -= vector.y
        z -= vector.z
        return this
    }

    fun multiply(vector: Vector3i): MutableVector3i {
        x *= vector.x
        y *= vector.y
        z *= vector.z
        return this
    }

    operator fun div(vector: Vector3i): MutableVector3i {
        x /= vector.x
        y /= vector.y
        z /= vector.z
        return this
    }

    fun set(a: Vector3i): MutableVector3i {
        setX(a.x)
        setY(a.y)
        setZ(a.z)
        return this
    }
}
