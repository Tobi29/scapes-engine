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

package org.tobi29.tilemaps

import org.tobi29.graphics.Bitmap
import org.tobi29.graphics.get
import org.tobi29.graphics.height
import org.tobi29.graphics.width
import org.tobi29.math.vector.Vector2i
import org.tobi29.utils.Iterator

fun cut(
    image: Bitmap<*, *>,
    tileWidth: Int,
    tileHeight: Int,
    tileSpacing: Int,
    tileMargin: Int,
    id: Int,
    tileset: String
) = run {
    val size = Vector2i(tileWidth, tileHeight)
    Sequence {
        var i = id
        var nextX = tileMargin
        var nextY = tileMargin
        Iterator {
            if (nextY + tileHeight + tileMargin > image.height) {
                return@Iterator null
            }
            val frame = Frame(0.0, image.get(nextX, nextY, size.x, size.y))
            val tile = Tile(Sprite(frame), size, i++, tileset)
            nextX += tileWidth + tileSpacing
            if (nextX + tileWidth + tileMargin > image.width) {
                nextX = tileMargin
                nextY += tileHeight + tileSpacing
            }
            return@Iterator tile
        }
    }
}
