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
import org.tobi29.scapes.engine.utils.io.tag.setInt

class Vector3i(x: Int, y: Int, val z: Int) : Vector2i(x, y) {

    constructor(vector: Vector3d) : this(vector.intX(), vector.intY(),
            vector.intZ()) {
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

    override fun write(): TagStructure {
        val tagStructure = super.write()
        tagStructure.setInt("Z", z)
        return tagStructure
    }

    override fun toString() = "$x $y $z"

    companion object {
        val ZERO = Vector3i(0, 0, 0)
    }
}
