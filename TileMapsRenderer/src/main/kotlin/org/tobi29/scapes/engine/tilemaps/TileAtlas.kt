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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.tilemaps

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.Texture
import org.tobi29.math.margin
import org.tobi29.math.vector.MutableVector2i
import org.tobi29.math.vector.Vector2i
import org.tobi29.tilemaps.Frame
import org.tobi29.tilemaps.Sprite
import org.tobi29.tilemaps.Tile
import org.tobi29.tilemaps.TileSets
import org.tobi29.graphics.MutableImage
import org.tobi29.graphics.assembleAtlas
import org.tobi29.graphics.set
import org.tobi29.graphics.toImage
import org.tobi29.io.ByteViewRO
import org.tobi29.utils.toArray
import org.tobi29.stdex.atomic.AtomicInt
import org.tobi29.stdex.math.floorToInt
import kotlin.math.max

class TileAtlas internal constructor(
        val texture: Texture,
        val maxSize: Vector2i,
        private val tiles: List<TileEntry?>,
        private val animations: List<TileAnimation>) {
    fun tile(tile: Tile): TileEntry? = tile(tile.id)
    fun tile(tile: Int): TileEntry? = tiles.getOrNull(tile)

    fun renderAnim(gl: GL) {
        texture.bind(gl)
        animations.forEach { it.render(gl) }
    }

    fun updateAnim(delta: Double) {
        animations.forEach { it.update(delta) }
    }
}

class TileEntry(val x: Int,
                val y: Int,
                val width: Int,
                val height: Int,
                atlasWidth: Int,
                atlasHeight: Int) {
    val textureX = x.toDouble() / atlasWidth
    val textureY = y.toDouble() / atlasHeight
    val textureWidth = width.toDouble() / atlasWidth
    val textureHeight = height.toDouble() / atlasHeight
}

inline fun TileEntry.atPixelX(value: Int): Double {
    return value / textureWidth
}

inline fun TileEntry.atPixelY(value: Int): Double {
    return value / textureHeight
}

inline fun TileEntry.atPixelMarginX(value: Int): Double {
    return marginX(atPixelX(value))
}

inline fun TileEntry.atPixelMarginY(value: Int): Double {
    return marginY(atPixelX(value))
}

inline fun TileEntry.marginX(value: Double,
                             margin: Double = 0.005): Double {
    return textureX + margin(value, margin) * textureWidth
}

inline fun TileEntry.marginY(value: Double,
                             margin: Double = 0.005): Double {
    return textureY + margin(value, margin) * textureHeight
}

internal class TileAnimation(sprite: Sprite,
                             private val x: Int,
                             private val y: Int,
                             private val width: Int,
                             private val height: Int) {
    private val newFrame = AtomicInt(-1)
    private val frames: Array<Pair<Double, ByteViewRO>>
    private var spin = 0.0

    init {
        frames = sprite.frames.asSequence().map { frame ->
            Pair(1.0 / frame.duration, frame.image.view)
        }.toArray()
    }

    fun render(gl: GL) {
        val frame = newFrame.getAndSet(-1)
        if (frame >= 0) {
            gl.replaceTextureMipMap(x, y, width, height, frames[frame].second)
        }
    }

    fun update(delta: Double) {
        if (frames.size <= 1) return

        val old = spin.floorToInt()
        val duration = frames[old].first

        spin = ((spin + delta * duration) % frames.size.toDouble())
                .coerceAtLeast(0.0)
        if (!spin.isFinite()) spin = 0.0

        val i = spin.floorToInt()
        if (old != i) newFrame.set(i)
    }
}

fun atlas(engine: ScapesEngine,
          tileSets: TileSets<*>): TileAtlas {
    val tiles = tileSets.tiles.map { tile ->
        val size = tile.sprite.frames.asSequence().map { it.image.size }
                .fold(Vector2i.ZERO) { a, b ->
                    Vector2i(max(a.x, b.x), max(a.y, b.y))
                }
        val scaledTile = if (tile.sprite.frames.any { it.image.size != size }) {
            Tile(Sprite(tile.sprite.frames.map { (duration, image) ->
                val scaledImage = if (image.size.x != size.x || image.size.y != size.y) {
                    val scaled = MutableImage(size.x, size.y)
                    for (y in 0 until size.x) {
                        for (x in 0 until size.y) {
                            scaled[x, y] = image[x * image.size.x / size.x, y * image.size.y / size.y]
                        }
                    }
                    scaled.toImage()
                } else image
                Frame(duration, scaledImage)
            }), tile.size, tile.id, tile.tileSet)
        } else tile
        Triple(scaledTile, size, MutableVector2i())
    }
    val atlasSize = assembleAtlas(
            tiles.asSequence().map { (_, size, position) -> size to position })
    val atlas = MutableImage(atlasSize.x, atlasSize.y)
    for ((tile, _, position) in tiles) {
        tile.sprite.frames.firstOrNull()?.let {
            atlas.set(position.x, position.y, it.image)
        }
    }
    val otiles = ArrayList<TileEntry?>(tiles.size)
    for ((tile, size, position) in tiles) {
        while (otiles.size <= tile.id) otiles.add(null)
        otiles[tile.id] = TileEntry(position.x, position.y, size.x, size.y,
                atlas.width, atlas.height)
    }
    val animations = tiles.asSequence().filter { it.first.sprite.frames.size > 1 }
            .map { (tile, size, position) ->
                TileAnimation(tile.sprite, position.x, position.y, size.x,
                        size.y)
            }.toList()
    return TileAtlas(engine.graphics.createTexture(atlas.toImage()),
            tiles.fold(Vector2i.ZERO) { a, (_, b, _) ->
                Vector2i(max(a.x, b.x), max(a.y, b.y))
            }, otiles, animations)
}
