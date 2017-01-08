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

import org.tobi29.scapes.engine.utils.math.margin
import java.nio.ByteBuffer

open class TextureAtlasEntry(var buffer: ByteBuffer?,
                             val width: Int,
                             val height: Int) {
    var x = 0
        internal set
    var y = 0
        internal set
    var textureX = 0.0
        internal set
    var textureY = 0.0
        internal set
    var textureWidth = 0.0
        internal set
    var textureHeight = 0.0
        internal set
}

@Suppress("NOTHING_TO_INLINE")
inline fun TextureAtlasEntry.atPixelX(value: Int): Double {
    return value / textureWidth
}

@Suppress("NOTHING_TO_INLINE")
inline fun TextureAtlasEntry.atPixelY(value: Int): Double {
    return value / textureHeight
}

@Suppress("NOTHING_TO_INLINE")
inline fun TextureAtlasEntry.atPixelMarginX(value: Int): Double {
    return marginX(atPixelX(value))
}

@Suppress("NOTHING_TO_INLINE")
inline fun TextureAtlasEntry.atPixelMarginY(value: Int): Double {
    return marginY(atPixelX(value))
}

@Suppress("NOTHING_TO_INLINE")
inline fun TextureAtlasEntry.marginX(value: Double,
                                     margin: Double = 0.005): Double {
    return textureX + margin(value, margin) * textureWidth
}

@Suppress("NOTHING_TO_INLINE")
inline fun TextureAtlasEntry.marginY(value: Double,
                                     margin: Double = 0.005): Double {
    return textureY + margin(value, margin) * textureHeight
}
