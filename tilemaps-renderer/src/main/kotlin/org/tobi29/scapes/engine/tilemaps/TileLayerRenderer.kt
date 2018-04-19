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
    val atlas: TileAtlas,
    private val cx: Int = 4,
    private val cy: Int = 4
) {
    private val cw = 1 shl cx
    private val ch = 1 shl cy
    private val vlimit = Vector2d(
        (cw * tileDimensions.x).toDouble(),
        (ch * tileDimensions.y).toDouble()
    )
    private val addAbove = Vector2i(
        atlas.maxSize.x / tileDimensions.x,
        atlas.maxSize.y / tileDimensions.y
    )
    private val chunksOffset = MutableVector2i(0, 0)
    private var chunks = Array2<TileLayerRendererChunk>(0, 0, arrayOf())

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
                { it, _, _ -> it.model?.markAsDisposed() },
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
        atlas.texture.bind(gl)
        for (yy in sy..ey) {
            val yyy = ((yy shl cy) * tileDimensions.y - origin.y).toFloat()
            for (xx in sx..ex) {
                chunks[xx - chunksOffset.x, yy - chunksOffset.y].model?.let { model ->
                    val xxx =
                        ((xx shl cx) * tileDimensions.x - origin.x).toFloat()
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
        var model: Model? = null

        init {
            prepare()
        }

        fun prepare() {
            val mesh = Mesh(false, false)
            val xc = x shl cx
            val yc = y shl cy
            for (yy in 0..ch + addAbove.y) {
                val yyy = yc + yy
                for (xx in -addAbove.x..cw) {
                    val xxx = xc + xx

                    val tile = map.getOrNull(xxx, yyy) ?: continue
                    val entry = atlas.tile(tile) ?: continue

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
            model = mesh.finish(engine.graphics)
        }
    }
}
