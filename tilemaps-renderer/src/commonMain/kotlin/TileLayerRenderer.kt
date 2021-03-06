/*
 * Copyright 2012-2019 Tobi29
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

import org.tobi29.arrays.Array2
import org.tobi29.arrays.indices
import org.tobi29.arrays.shift
import org.tobi29.math.vector.MutableVector2i
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.Vector2i
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.*
import org.tobi29.stdex.assert
import org.tobi29.stdex.math.ceilToInt
import org.tobi29.stdex.math.clamp
import org.tobi29.stdex.math.floorToInt
import org.tobi29.tilemaps.Tile

class TileLayerRenderer(
    val engine: ScapesEngine,
    val tileDimensions: Vector2i,
    val map: Array2<out Tile?>,
    val atlas: Array<TileAtlas>,
    val atlasBits: Int,
    private val cx: Int = 4,
    private val cy: Int = 4
) {
    private val cw = 1 shl cx
    private val ch = 1 shl cy
    private val addAbove = Vector2i(
        (atlas.maxBy { it.maxSize.x }?.maxSize?.x ?: 0) / tileDimensions.x,
        (atlas.maxBy { it.maxSize.y }?.maxSize?.y ?: 0) / tileDimensions.y
    )
    private val chunksOffset = MutableVector2i(0, 0)
    private var chunks = Array2<TileLayerRendererChunk>(0, 0, arrayOf())

    constructor(
        engine: ScapesEngine,
        tileDimensions: Vector2i,
        map: Array2<out Tile?>,
        atlas: TileAtlas,
        cx: Int = 4,
        cy: Int = 4
    ) : this(engine, tileDimensions, map, arrayOf(atlas), 31, cx, cy)

    fun render(
        gl: GL,
        shader: Shader,
        origin: Vector2d,
        size: Vector2d,
        prerenderSize: Vector2d = Vector2d.ZERO
    ) {
        val x = origin.x / tileDimensions.x - addAbove.x
        val y = origin.y / tileDimensions.y - addAbove.y
        val w = size.x / tileDimensions.x + addAbove.x
        val h = size.y / tileDimensions.y + addAbove.y
        val pw = prerenderSize.x / tileDimensions.x
        val ph = prerenderSize.y / tileDimensions.y
        val sx = x.floorToInt() shr cx
        val sy = y.floorToInt() shr cy
        val ex = (x + w).ceilToInt() shr cx
        val ey = (y + h).ceilToInt() shr cy
        val ox = (x - pw).floorToInt() shr cx
        val oy = (y - ph).floorToInt() shr cy
        val dx = ((w + pw * 2.0).ceilToInt() shr cx) + 2
        val dy = ((h + ph * 2.0).ceilToInt() shr cy) + 2
        if (chunks.width != dx || chunks.height != dy) {
            chunksOffset.x = ox
            chunksOffset.y = oy
            chunks = Array2(dx, dy) { xx, yy ->
                TileLayerRendererChunk(
                    xx + chunksOffset.x, yy + chunksOffset.y
                ).apply { prepare() }
            }
        } else {
            val shiftX = chunksOffset.x - ox
            val shiftY = chunksOffset.y - oy
            chunksOffset.x = ox
            chunksOffset.y = oy
            chunks.shift(
                shiftX,
                shiftY,
                { it, _, _ -> it.model?.forEach { it.second.markAsDisposed() } },
                { xx, yy ->
                    TileLayerRendererChunk(
                        xx + chunksOffset.x, yy + chunksOffset.y
                    ).apply { prepare() }
                })
            assert {
                var flag = true
                chunks.indices { x, y ->
                    flag = flag && chunks[x, y].let {
                        it.x == x + chunksOffset.x && it.y == y + chunksOffset.y
                    }
                }
                flag
            }
        }
        var lastTexture: Texture? = null
        for (yy in sy..ey) {
            val yyy = ((yy shl cy) * tileDimensions.y - origin.y).toFloat()
            for (xx in sx..ex) {
                chunks[xx - chunksOffset.x, yy - chunksOffset.y].model
                    ?.forEach { (texture, model) ->
                        val xxx =
                            ((xx shl cx) * tileDimensions.x - origin.x).toFloat()
                        if (lastTexture !== texture) {
                            texture.bind(gl)
                            lastTexture = texture
                        }
                        gl.matrixStack.push { matrix ->
                            matrix.translate(xxx, yyy, 0.0f)
                            model.render(gl, shader)
                        }
                    }
            }
        }
    }

    private inner class TileLayerRendererChunk(
        val x: Int,
        val y: Int
    ) {
        var model: Array<Pair<Texture, Model>>? = null

        init {
            prepare()
        }

        fun prepare() {
            model = map.prepareToMeshes(
                engine, atlas, atlasBits,
                tileDimensions, x shl cx, y shl cy, cw, ch,
                Vector2i(addAbove.x, 0), Vector2i(0, addAbove.y)
            )
        }
    }
}

fun Array2<out Tile?>.prepareToMeshes(
    engine: ScapesEngine, atlas: Array<TileAtlas>, atlasBits: Int,
    tileDimensions: Vector2i, x: Int, y: Int, width: Int, height: Int,
    overscanMin: Vector2i = Vector2i.ZERO, overscanMax: Vector2i = Vector2i.ZERO
): Array<Pair<Texture, Model>> {
    val meshes = ArrayList<Pair<Texture, Mesh>>()
    var currentAtlas: TileAtlas? = null
    var mesh: Mesh? = null

    val atlasMask = (1 shl atlasBits) - 1
    val vlimit = Vector2d(
        (width * tileDimensions.x).toDouble(),
        (height * tileDimensions.y).toDouble()
    )

    for (yy in -overscanMin.y..height + overscanMax.y) {
        val yyy = y + yy
        for (xx in -overscanMin.x..width + overscanMax.x) {
            val xxx = x + xx

            val tile = getOrNull(xxx, yyy) ?: continue
            val nextAtlas = atlas[tile.id ushr atlasBits]
            if (nextAtlas !== currentAtlas || mesh == null) {
                mesh = Mesh(false, false)
                currentAtlas = nextAtlas
                meshes.add(currentAtlas.texture to mesh)
            }
            val entry = currentAtlas.tile(tile.id and atlasMask) ?: continue

            val yo = tileDimensions.y - tile.size.y
            val vx = (xx * tileDimensions.x).toDouble()
            val vy = (yy * tileDimensions.y + yo).toDouble()
            val vxh = vx + tile.size.x
            val vyh = vy + tile.size.y
            val vx1 = clamp(vx, 0.0, vlimit.x)
            val vy1 = clamp(vy, 0.0, vlimit.y)
            val vx2 = clamp(vxh, 0.0, vlimit.x)
            val vy2 = clamp(vyh, 0.0, vlimit.y)

            if (vx1 != vx2 && vy1 != vy2) {
                val tx1 = entry.marginX((vx1 - vx) / tile.size.x)
                val ty1 = entry.marginY((vy1 - vy) / tile.size.y)
                val tx2 = entry.marginX((vx2 - vx) / tile.size.x)
                val ty2 = entry.marginY((vy2 - vy) / tile.size.y)

                mesh.addVertex(vx1, vy1, 0.0, tx1, ty1)
                mesh.addVertex(vx2, vy1, 0.0, tx2, ty1)
                mesh.addVertex(vx2, vy2, 0.0, tx2, ty2)
                mesh.addVertex(vx1, vy2, 0.0, tx1, ty2)
            }
        }
    }
    return Array(meshes.size) {
        meshes[it].let { (texture, mesh) ->
            texture to mesh.finish(engine.graphics)
        }
    }
}
