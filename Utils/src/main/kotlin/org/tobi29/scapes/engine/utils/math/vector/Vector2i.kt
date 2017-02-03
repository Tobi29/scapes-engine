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

import org.tobi29.scapes.engine.utils.io.tag.MultiTag
import org.tobi29.scapes.engine.utils.io.tag.TagStructure
import org.tobi29.scapes.engine.utils.io.tag.setInt

open class Vector2i(val x: Int,
                    val y: Int) : MultiTag.Writeable {

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

    override fun write(): TagStructure {
        val tagStructure = TagStructure()
        tagStructure.setInt("X", x)
        tagStructure.setInt("Y", y)
        return tagStructure
    }

    override fun toString() = "$x $y"

    companion object {
        val ZERO = Vector2i(0, 0)
    }
}
