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
import org.tobi29.scapes.engine.utils.io.ByteView
import org.tobi29.scapes.engine.utils.io.view
import org.tobi29.scapes.engine.utils.math.vector.Vector2i
import org.tobi29.scapes.engine.utils.splitToBytes

class MutableImage(val width: Int = 1,
                   val height: Int = 1,
                   val view: ByteView = ByteArray(width * height shl 2).view) {
    init {
        if (view.size != width * height shl 2) {
            throw IllegalArgumentException("Backing buffer sized incorrectly")
        }
    }

    operator fun set(x: Int,
                     y: Int,
                     value: Int) {
        if (x < 0 || y < 0 || x >= width || y >= height)
            throw IndexOutOfBoundsException("Coordinates outside of image")
        var i = (y * width + x) shl 2
        value.splitToBytes { b3, b2, b1, b0 ->
            view[i++] = b3
            view[i++] = b2
            view[i++] = b1
            view[i] = b0
        }
    }

    operator fun get(x: Int,
                     y: Int): Int {
        if (x < 0 || y < 0 || x >= width || y >= height)
            throw IndexOutOfBoundsException("Coordinates outside of image")
        var i = y * width + x
        return combineToInt(view[i++], view[i++], view[i++], view[i])
    }

    val size by lazy { Vector2i(width, height) }
}
