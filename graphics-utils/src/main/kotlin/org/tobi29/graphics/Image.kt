/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.graphics

import org.tobi29.arrays.Int2ByteArrayRO
import org.tobi29.io.ByteViewRO
import org.tobi29.io.asByteArray
import org.tobi29.io.ro
import org.tobi29.io.tag.*
import org.tobi29.io.view
import org.tobi29.math.vector.Vector2i

class Image(
    val bitmap: IntByteViewBitmap<RGBA>
) : TagMapWrite {
    constructor(
        width: Int = 1,
        height: Int = 1,
        buffer: ByteViewRO = ByteArray(width * height shl 2).view
    ) : this(BitmapC(Int2ByteArrayRO(buffer.ro, width, height), RGBA)) {
        if (buffer.size != width * height shl 2) {
            throw IllegalArgumentException("Backing buffer sized incorrectly")
        }
    }

    val width get() = bitmap.width
    val height get() = bitmap.height
    val view get() = bitmap.data.array
    val size get() = Vector2i(width, height)

    operator fun get(x: Int, y: Int): Int = bitmap[x, y]

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
