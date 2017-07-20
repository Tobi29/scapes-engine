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

package org.tobi29.scapes.engine.tilemaps.renderer

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.Texture
import org.tobi29.scapes.engine.graphics.TextureAtlasEngine
import org.tobi29.scapes.engine.graphics.TextureAtlasEngineEntry
import org.tobi29.scapes.engine.tilemaps.Sprite
import org.tobi29.scapes.engine.tilemaps.Tile
import org.tobi29.scapes.engine.tilemaps.TileSets
import org.tobi29.scapes.engine.utils.AtomicInteger
import org.tobi29.scapes.engine.utils.graphics.Image
import org.tobi29.scapes.engine.utils.math.floor
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.math.remP
import org.tobi29.scapes.engine.utils.math.vector.Vector2i
import org.tobi29.scapes.engine.utils.toArray
import kotlin.collections.set

class TileAtlas(engine: ScapesEngine) : TextureAtlasEngine<TileAtlasEntry>(
        engine) {
    private val tiles = ArrayList<TileAtlasEntry?>()
    var maxSize = Vector2i.ZERO
        private set

    fun registerTile(tile: Tile): TileAtlasEntry {
        tile(tile.id)?.let { return it }
        val texture = TileAtlasEntry(tile.sprite, tile.size.x, tile.size.y,
                engine, { texture })
        textures["${tile.id}"] = texture
        while (tiles.size <= tile.id) {
            tiles.add(null)
        }
        tiles[tile.id] = texture
        maxSize = max(maxSize, tile.size)
        return texture
    }

    fun tile(id: Int): TileAtlasEntry? {
        if (id < 0 || id >= tiles.size) {
            return null
        }
        return tiles[id]
    }

    fun renderAnim(gl: GL) {
        tiles.forEach { it?.renderAnim(gl) }
    }

    fun updateAnim(delta: Double) {
        tiles.forEach { it?.updateAnim(delta) }
    }
}

open class TileAtlasEntry(sprite: Sprite,
                          width: Int,
                          height: Int,
                          engine: ScapesEngine,
                          texture: () -> Texture) : TextureAtlasEngineEntry(
        sprite.frames.firstOrNull()?.image?.buffer, width, height, texture) {
    private val newFrame = AtomicInteger(-1)
    private val frames: Array<Pair<Double, Image>>
    private var spin = 0.0

    init {
        frames = sprite.frames.asSequence().map { frame ->
            val buffer = engine.allocate(
                    frame.image.width * frame.image.height shl 2)
            buffer.put(frame.image.buffer)
            buffer.rewind()
            val image = Image(frame.image.width, frame.image.height, buffer)
            Pair(1.0 / frame.duration, image)
        }.toArray()
    }

    fun renderAnim(gl: GL) {
        val frame = newFrame.getAndSet(-1)
        if (frame >= 0) {
            texture().bind(gl)
            val image = frames[frame].second
            gl.replaceTextureMipMap(x, y, image.width, image.height,
                    image.buffer)
        }
    }

    fun updateAnim(delta: Double) {
        if (frames.size <= 1) {
            return
        }
        val old = floor(spin)
        spin = (spin + delta * frames[old].first) remP frames.size.toDouble()
        val i = floor(spin)
        if (old != i) {
            newFrame.set(i)
        }
    }
}

fun atlas(engine: ScapesEngine,
          tileSets: TileSets<*>): TileAtlas {
    val atlas = TileAtlas(engine)
    tileSets.tiles.forEach { atlas.registerTile(it) }
    atlas.init()
    atlas.initTexture(0)
    return atlas
}
