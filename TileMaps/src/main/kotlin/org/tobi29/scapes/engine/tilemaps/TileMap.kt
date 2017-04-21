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

package org.tobi29.scapes.engine.tilemaps

import org.tobi29.scapes.engine.utils.assert
import org.tobi29.scapes.engine.utils.io.ByteBuffer
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2i
import org.tobi29.scapes.engine.utils.math.vector.Vector2i
import org.tobi29.scapes.engine.utils.tag.*

class TileMap(val tileSets: TileSets,
              size: Vector2i = Vector2i.ZERO) : TagMapWrite {
    private val sizeMut = MutableVector2i(size)
    val size: Vector2i
        get() = sizeMut.now()
    private var map = arrayOfNulls<Tile>(size.x * size.y)

    val isEmpty: Boolean
        get() {
            for (p in 0..1) {
                for (y in 0..sizeMut.y - 1) {
                    var x = p
                    while (x < sizeMut.x) {
                        if (tile(x, y) != null) {
                            return false
                        }
                        x += 2
                    }
                }
            }
            return true
        }

    fun tile(x: Int,
             y: Int): Tile? {
        if (x < 0 || y < 0 || x >= sizeMut.x || y >= sizeMut.y) {
            return null
        }
        return map[y * sizeMut.x + x]
    }

    fun tile(x: Int,
             y: Int,
             tile: Tile?) {
        if (x < 0 || y < 0 || x >= sizeMut.x || y >= sizeMut.y) {
            return
        }
        map[y * sizeMut.x + x] = tile
    }

    fun clone(): TileMap {
        val clone = TileMap(tileSets, size)
        System.arraycopy(map, 0, clone.map, 0, map.size)
        return clone
    }

    override fun write(map: ReadWriteTagMap) {
        map["Size"] = sizeMut.now()
        map["Tiles"] = ByteArray(this.map.size shl 2).apply {
            val buffer = ByteBuffer.wrap(this)
            this@TileMap.map.forEach { buffer.putInt(it?.id ?: -1) }
            assert { !buffer.hasRemaining() }
        }
    }

    fun read(map: TagMap) {
        map["Size"]?.toMap()?.let {
            sizeMut.set(it)
            this.map = arrayOfNulls<Tile>(sizeMut.x * sizeMut.y)
        }
        map["Tiles"]?.toByteArray()?.let { tiles ->
            if (tiles.size != this.map.size shl 2) {
                throw IllegalArgumentException("Tile array has invalid size")
            }
            val buffer = ByteBuffer.wrap(tiles)
            for (i in this.map.indices) {
                this.map[i] = tileSets.tile(buffer.int)
            }
        }
    }
}