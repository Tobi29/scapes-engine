/*
 * Copyright 2012-2016 Tobi29
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
package org.tobi29.scapes.engine.backends.lwjgl3

import org.lwjgl.stb.STBTruetype
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.tobi29.scapes.engine.gui.GlyphRenderer
import org.tobi29.scapes.engine.utils.math.floor
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.math.sqr
import java.nio.ByteBuffer

class STBGlyphRenderer(private val font: STBFont, size: Int) : GlyphRenderer {
    private val tiles: Int
    private val pageTiles: Int
    private val pageTileBits: Int
    private val pageTileMask: Int
    private val glyphSize: Int
    private val imageSize: Int
    private val tileSize: Float
    private val size: Float
    private val scale: Float

    init {
        this.size = size.toFloat()
        val tileBits = 3
        tiles = 1 shl tileBits
        pageTileBits = tileBits shl 1
        pageTileMask = (1 shl pageTileBits) - 1
        pageTiles = 1 shl pageTileBits
        tileSize = 1.0f / tiles
        glyphSize = size shl 1
        imageSize = glyphSize shl tileBits
        scale = STBTruetype.stbtt_ScaleForMappingEmToPixels(font.info,
                size * 1.38f)
    }

    @Synchronized override fun page(id: Int,
                                    bufferSupplier: (Int) -> ByteBuffer): GlyphRenderer.GlyphPage {
        val stack = MemoryStack.stackGet()
        stack.push {
            MemoryUtil.memAlloc(sqr(glyphSize)).use { glyphBuffer ->
                val buffer = bufferSupplier(imageSize * imageSize shl 2)
                val width = FloatArray(pageTiles)
                var i = 0
                val offset = id shl pageTileBits
                val xb = stack.mallocInt(1)
                val yb = stack.mallocInt(1)
                val wb = stack.mallocInt(1)
                val hb = stack.mallocInt(1)
                for (y in 0..tiles - 1) {
                    val yy = y * glyphSize
                    for (x in 0..tiles - 1) {
                        val xx = x * glyphSize
                        val c = (i + offset).toChar()
                        STBTruetype.stbtt_GetCodepointHMetrics(font.info,
                                c.toInt(),
                                xb, yb)
                        val widthX = xb.get(0) * scale
                        if (!Character.isISOControl(c)) {
                            STBTruetype.stbtt_GetCodepointBox(font.info,
                                    c.toInt(),
                                    xb, yb, wb, hb)
                            val offsetX = size * 0.25f + xb.get(0) * scale
                            val offsetY = size * 1.5f - hb.get(0) * scale
                            // Has to be floor to align glyphs properly
                            var renderX = max(floor(offsetX), 0)
                            var renderY = max(floor(offsetY), 0)
                            val sizeX = glyphSize - renderX - 1
                            val sizeY = glyphSize - renderY - 1
                            renderX += xx
                            renderY += yy
                            MemoryUtil.memSet(
                                    MemoryUtil.memAddress(glyphBuffer), 0,
                                    glyphBuffer.remaining())
                            STBTruetype.stbtt_MakeCodepointBitmap(font.info,
                                    glyphBuffer,
                                    glyphSize, glyphSize, glyphSize, scale,
                                    scale, c.toInt())
                            for (yyy in 0..sizeY - 1) {
                                buffer.position(
                                        (renderY + yyy) * imageSize + renderX shl 2)
                                glyphBuffer.position(yyy * glyphSize)
                                for (xxx in 0..sizeX - 1) {
                                    val value = glyphBuffer.get()
                                    buffer.put(0xFF.toByte())
                                    buffer.put(0xFF.toByte())
                                    buffer.put(0xFF.toByte())
                                    buffer.put(value)
                                }
                            }
                            glyphBuffer.rewind()
                        }
                        width[i++] = widthX / size / 1.3f
                    }
                }
                buffer.rewind()
                return GlyphRenderer.GlyphPage(buffer, width, imageSize, tiles,
                        tileSize)
            }
        }
    }

    override fun pageID(character: Char): Int {
        return character.toInt() shr pageTileBits
    }

    override fun pageCode(character: Char): Int {
        return character.toInt() and pageTileMask
    }
}
