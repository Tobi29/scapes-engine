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

package org.tobi29.tilemaps

import org.tobi29.math.vector.Vector2i
import org.tobi29.math.vector.toVector2i
import org.tobi29.io.tag.*

open class Tile(val sprite: Sprite,
                val size: Vector2i,
                val id: Int,
                val tileSet: String) : TagMapWrite {
    override fun write(map: ReadWriteTagMap) {
        map["Sprite"] = sprite.toTag()
        map["Size"] = size.toTag()
        map["ID"] = id.toTag()
        map["TileSet"] = tileSet.toTag()
    }
}

fun MutableTag.toTile(): Tile? {
    val map = toMap() ?: return null
    val sprite = map["Sprite"]?.toSprite() ?: return null
    val size = map["Size"]?.toVector2i() ?: return null
    val id = map["ID"]?.toInt() ?: return null
    val tileSet = map["TileSet"]?.toString() ?: return null
    return Tile(sprite, size, id, tileSet)
}
