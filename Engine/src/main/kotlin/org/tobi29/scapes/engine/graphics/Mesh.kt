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
package org.tobi29.scapes.engine.graphics

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.utils.math.min

class Mesh(private val triangles: Boolean = false,
           private val color: Boolean = true) {
    private var pos = 0
    private var remaining = 0
    private var vertexArray = EMPTY_FLOAT
    private var colorArray = EMPTY_FLOAT
    private var textureArray = EMPTY_FLOAT
    private var normalArray = EMPTY_FLOAT
    private var r = 0.0
    private var g = 0.0
    private var b = 0.0
    private var a = 0.0
    private var tx = 0.0
    private var ty = 0.0
    private var nx = 0.0
    private var ny = 0.0
    private var nz = 0.0

    fun color(r: Double,
              g: Double,
              b: Double,
              a: Double) {
        this.r = r
        this.g = g
        this.b = b
        this.a = a
    }

    fun texture(tx: Double,
                ty: Double) {
        this.tx = tx
        this.ty = ty
    }

    fun normal(nx: Double,
               ny: Double,
               nz: Double) {
        this.nx = nx
        this.ny = ny
        this.nz = nz
    }

    fun vertex(x: Double,
               y: Double,
               z: Double) {
        addVertex(x, y, z, r, g, b, a, tx, ty, nx, ny, nz)
    }

    fun addRectangle(minX: Double,
                     minY: Double,
                     maxX: Double,
                     maxY: Double,
                     z: Double,
                     minTX: Double,
                     minTY: Double,
                     maxTX: Double,
                     maxTY: Double,
                     r: Double,
                     g: Double,
                     b: Double,
                     a: Double) {
        if (triangles) {
            addVertex(minX, minY, z, r, g, b, a, minTX, minTY)
            addVertex(minX, maxY, z, r, g, b, a, minTX, maxTY)
            addVertex(maxX, minY, z, r, g, b, a, maxTX, minTY)
            addVertex(maxX, minY, z, r, g, b, a, maxTX, minTY)
            addVertex(minX, maxY, z, r, g, b, a, minTX, maxTY)
            addVertex(maxX, maxY, z, r, g, b, a, maxTX, maxTY)
        } else {
            addVertex(minX, minY, z, r, g, b, a, minTX, minTY)
            addVertex(minX, maxY, z, r, g, b, a, minTX, maxTY)
            addVertex(maxX, minY, z, r, g, b, a, maxTX, minTY)
            addVertex(maxX, maxY, z, r, g, b, a, maxTX, maxTY)
        }
    }

    fun addVertex(x: Double,
                  y: Double,
                  z: Double,
                  tx: Double,
                  ty: Double) {
        addVertex(x, y, z, 1.0, 1.0, 1.0, 1.0, tx, ty, 0.0, 0.0, 1.0)
    }

    fun addVertex(x: Double,
                  y: Double,
                  z: Double,
                  r: Double,
                  g: Double,
                  b: Double,
                  a: Double,
                  tx: Double,
                  ty: Double,
                  nx: Double = 0.0,
                  ny: Double = 0.0,
                  nz: Double = 1.0) {
        if (remaining <= 0) {
            val growth: Int
            if (pos == 0) {
                growth = START_SIZE
            } else {
                growth = pos
            }
            changeArraySize(pos + growth)
            remaining += growth
        }
        var i = pos * 3
        vertexArray[i++] = x.toFloat()
        vertexArray[i++] = y.toFloat()
        vertexArray[i] = z.toFloat()
        if (color) {
            i = pos shl 2
            colorArray[i++] = r.toFloat()
            colorArray[i++] = g.toFloat()
            colorArray[i++] = b.toFloat()
            colorArray[i] = a.toFloat()
        }
        i = pos shl 1
        textureArray[i++] = tx.toFloat()
        textureArray[i] = ty.toFloat()
        i = pos * 3
        normalArray[i++] = nx.toFloat()
        normalArray[i++] = ny.toFloat()
        normalArray[i] = nz.toFloat()
        pos++
        remaining--
    }

    private fun changeArraySize(size: Int) {
        val newVertexArray = FloatArray(size * 3)
        val newColorArray = FloatArray(size shl 2)
        val newTextureArray = FloatArray(size shl 1)
        val newNormalArray = FloatArray(size * 3)
        System.arraycopy(vertexArray, 0, newVertexArray, 0,
                min(vertexArray.size, newVertexArray.size))
        if (color) {
            System.arraycopy(colorArray, 0, newColorArray, 0,
                    min(colorArray.size, newColorArray.size))
        }
        System.arraycopy(textureArray, 0, newTextureArray, 0,
                min(textureArray.size, newTextureArray.size))
        System.arraycopy(normalArray, 0, newNormalArray, 0,
                min(normalArray.size, newNormalArray.size))
        vertexArray = newVertexArray
        colorArray = newColorArray
        textureArray = newTextureArray
        normalArray = newNormalArray
    }

    fun finish(engine: ScapesEngine): Model {
        changeArraySize(pos)
        if (triangles) {
            val model: Model
            if (color) {
                model = engine.graphics.createVCTN(vertexArray, colorArray,
                        textureArray, normalArray, RenderType.TRIANGLES)
            } else {
                model = engine.graphics.createVTN(vertexArray, textureArray,
                        normalArray, RenderType.TRIANGLES)
            }
            return model
        } else {
            val indexArray = IntArray((pos * 1.5).toInt())
            var i = 0
            var p = 0
            while (i < indexArray.size) {
                indexArray[i++] = p
                indexArray[i++] = p + 1
                indexArray[i++] = p + 2
                indexArray[i++] = p
                indexArray[i++] = p + 2
                indexArray[i++] = p + 3
                p += 4
            }
            val model: Model
            if (color) {
                model = engine.graphics.createVCTNI(vertexArray, colorArray,
                        textureArray, normalArray, indexArray,
                        RenderType.TRIANGLES)
            } else {
                model = engine.graphics.createVTNI(vertexArray, textureArray,
                        normalArray, indexArray, RenderType.TRIANGLES)
            }
            return model
        }
    }

    companion object {
        private val EMPTY_FLOAT = floatArrayOf()
        private val START_SIZE = 6 * 20
    }
}
