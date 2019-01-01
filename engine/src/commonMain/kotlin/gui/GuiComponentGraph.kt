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

package org.tobi29.scapes.engine.gui

import org.tobi29.arrays.FloatArray2
import org.tobi29.math.vector.Vector2d
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.RenderType
import org.tobi29.scapes.engine.graphics.Shader
import org.tobi29.scapes.engine.graphics.createVCI
import org.tobi29.stdex.assert
import org.tobi29.stdex.concurrent.withLock
import org.tobi29.stdex.math.ceilToInt
import org.tobi29.stdex.math.clamp
import kotlin.math.pow

class GuiComponentGraph(
    parent: GuiLayoutData,
    graphs: Int,
    private val r: FloatArray,
    private val g: FloatArray,
    private val b: FloatArray,
    private val a: FloatArray
) : GuiComponentHeavy(parent) {
    private val i = IntArray(graphs)
    private var data = FloatArray2(graphs, 0)

    init {
        assert { r.size == graphs }
        assert { g.size == graphs }
        assert { b.size == graphs }
        assert { a.size == graphs }
    }

    public override fun renderComponent(
        gl: GL,
        shader: Shader,
        size: Vector2d,
        pixelSize: Vector2d,
        delta: Double
    ) {
        val w = size.x.ceilToInt()
        val vertex = FloatArray(i.size * w * 3)
        val color = FloatArray(w * (i.size shl 2))
        val limit = w - 1
        val index = IntArray(w * (limit shl 1))
        lock.withLock {
            if (data.height != w) {
                data = FloatArray2(i.size, w)
            }
            for (i in 0 until i.size) {
                val offset = i * w
                for (j in 0 until w) {
                    var x = j + this.i[i]
                    if (x >= w) {
                        x -= w
                    }
                    x = clamp(x, 0, limit)
                    var k = (offset + j) * 3
                    vertex[k++] = j.toFloat()
                    vertex[k++] = (data[i, x] * size.y).toFloat()
                    vertex[k] = 0.0f
                    k = offset + j shl 2
                    color[k++] = r[i]
                    color[k++] = g[i]
                    color[k++] = b[i]
                    color[k] = a[i]
                }
                for (j in 0 until limit) {
                    val k = offset + j
                    var l = k shl 1
                    index[l++] = k
                    index[l] = k + 1
                }
            }
        }
        gl.textureEmpty.bind(gl)
        val model = engine.graphics.createVCI(
            vertex, color, index,
            RenderType.LINES
        )
        model.render(gl, shader)
        model.markAsDisposed()
    }

    fun addStamp(value: Double, graph: Int) {
        lock.withLock {
            if (data.height > 0) {
                val index = i[graph] % data.height
                data[graph, index] = (1.0 - value.pow(0.25)).toFloat()
                i[graph] = index + 1
            }
        }
    }
}
