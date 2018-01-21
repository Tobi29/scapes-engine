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

import org.tobi29.arrays.Array2
import org.tobi29.arrays.array2OfNulls
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.*
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.Vector2i
import org.tobi29.tilemaps.Tile
import org.tobi29.stdex.math.ceilToInt
import org.tobi29.stdex.math.clamp
import org.tobi29.stdex.math.floorToInt

class TileLayerRenderer(val engine: ScapesEngine,
                        val tileDimensions: Vector2i,
                        val map: Array2<out Tile?>,
                        val atlas: TileAtlas,
                        private val cx: Int = 4,
                        private val cy: Int = 4) {
    private val cw = 1 shl cx
    private val ch = 1 shl cy
    private val vlimit = Vector2d((cw * tileDimensions.x).toDouble(),
            (ch * tileDimensions.y).toDouble())
    private val addAbove = Vector2i(
            (atlas.maxSize.x - 1) / tileDimensions.x,
            (atlas.maxSize.y - 1) / tileDimensions.y)
    private var width = 0
    private var height = 0
    private lateinit var chunks: Array2<TileLayerRendererChunk?>

    init {
        resize()
    }

    fun render(gl: GL,
               shader: Shader,
               origin: Vector2d,
               size: Vector2d) {
        val x = origin.x / tileDimensions.x
        val y = origin.y / tileDimensions.y
        val w = size.x / tileDimensions.x
        val h = size.y / tileDimensions.y
        val sx = clamp(x.floorToInt() shr cx, 0, width - 1)
        val sy = clamp(y.floorToInt() shr cy, 0, height - 1)
        val dx = clamp((x + w).ceilToInt() shr cx, 0, width - 1)
        val dy = clamp((y + h).ceilToInt() shr cy, 0, height - 1)
        atlas.texture.bind(gl)
        for (yy in sy..dy) {
            val yyy = ((yy shl cy) * tileDimensions.y - origin.y).toFloat()
            for (xx in sx..dx) {
                chunks[xx, yy]?.model?.let { model ->
                    val xxx = ((xx shl cx) * tileDimensions.x - origin.x).toFloat()
                    gl.matrixStack.push { matrix ->
                        matrix.translate(xxx, yyy, 0.0f)
                        model.render(gl, shader)
                    }
                }
            }
        }
    }

    private fun resize() {
        width = (map.width - 1 shr cx) + 1
        height = map.height
        chunks = array2OfNulls(width, height)
        for (y in 0 until height) {
            val yy = y shl cy
            for (x in 0 until width) {
                val xx = x shl cx
                val chunk = TileLayerRendererChunk(xx, yy)
                chunk.prepare()
                chunks[x, y] = chunk
            }
        }
    }

    private inner class TileLayerRendererChunk(private val x: Int,
                                               private val y: Int) {
        var model: Model? = null

        init {
            prepare()
        }

        fun prepare() {
            val mesh = Mesh(false, false)
            for (yy in 0..ch + addAbove.y) {
                val yyy = y + yy
                for (xx in -addAbove.x..cw) {
                    val xxx = x + xx

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
