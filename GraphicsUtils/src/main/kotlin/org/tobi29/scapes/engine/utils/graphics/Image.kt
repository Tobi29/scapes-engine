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

package org.tobi29.scapes.engine.utils.graphics

import org.tobi29.scapes.engine.utils.combineToInt
import org.tobi29.scapes.engine.utils.io.ByteViewRO
import org.tobi29.scapes.engine.utils.io.asByteArray
import org.tobi29.scapes.engine.utils.io.ro
import org.tobi29.scapes.engine.utils.io.view
import org.tobi29.scapes.engine.math.vector.Vector2i
import org.tobi29.scapes.engine.utils.tag.*

class Image(
        val width: Int = 1,
        val height: Int = 1,
        buffer: ByteViewRO = ByteArray(width * height shl 2).view
) : TagMapWrite {
    init {
        if (buffer.size != width * height shl 2) {
            throw IllegalArgumentException("Backing buffer sized incorrectly")
        }
    }

    val view = buffer.ro

    val size by lazy { Vector2i(width, height) }

    operator fun get(x: Int,
                     y: Int): Int {
        if (x < 0 || y < 0 || x >= width || y >= height)
            throw IndexOutOfBoundsException("Coordinates outside of image")
        var i = (y * width + x) shl 2
        return combineToInt(view[i++], view[i++], view[i++], view[i])
    }

    override fun write(map: ReadWriteTagMap) {
        map["Width"] = width.toTag()
        map["Height"] = height.toTag()
        map["RGBA"] = view.asByteArray().toTag()
    }
}

fun MutableTag.toImage(): Image? {
    val map = toMap() ?: return null
    val width = map["Width"]?.toInt() ?: return null
    val height = map["Height"]?.toInt() ?: return null
    val rgba = map["RGBA"]?.toByteArray() ?: return null
    return Image(width, height, rgba.view)
}
