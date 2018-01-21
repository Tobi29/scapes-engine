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

import org.tobi29.stdex.readOnly
import org.tobi29.io.tag.*

class TileSets<out T : Tile>(private val tilesMap: Map<Int, T>) : TagMapWrite {
    val tiles = tilesMap.values.readOnly()

    fun tile(id: Int): T? {
        if (id == -1) {
            return null
        }
        return tilesMap[id]
    }

    override fun write(map: ReadWriteTagMap) {
        map["Tiles"] = TagList(
                tiles.asSequence().map { it.toTag() })
    }
}

fun MutableTag.toTileSets(): TileSets<Tile>? {
    val map = toMap() ?: return null
    val tiles = map["Tiles"]?.toList() ?: return null
    return TileSets(tiles.asSequence()
            .mapNotNull { it.toTile() }
            .map { it.id to it }.toMap())
}
