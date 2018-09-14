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

package org.tobi29.scapes.engine.backends.stb.font

import org.tobi29.scapes.engine.allocateMemoryBuffer
import org.tobi29.scapes.engine.gui.GlyphRenderer
import org.tobi29.stdex.isISOControl
import org.tobi29.stdex.math.floorToInt
import kotlin.math.max
import kotlin.math.roundToInt

class STBGlyphRenderer(
    private val font: STBFont,
    size: Int
) : GlyphRenderer {
    private val tiles: Int
    private val pageTiles: Int
    private val pageTileBits: Int
    private val pageTileMask: Int
    private val glyphSize: Int
    private val imageSize: Int
    private val tileSize: Double
    private val size: Double = size.toDouble()
    private val scale: Double

    init {
        val tileBits = if (size < 16) {
            4
        } else if (size < 32) {
            3
        } else if (size < 64) {
            2
        } else if (size < 128) {
            1
        } else {
            0
        }
        tiles = 1 shl tileBits
        pageTileBits = tileBits shl 1
        pageTileMask = (1 shl pageTileBits) - 1
        pageTiles = 1 shl pageTileBits
        tileSize = 1.0 / tiles
        glyphSize = size shl 1
        imageSize = glyphSize shl tileBits
        scale = stbtt_ScaleForMappingEmToPixels(
            font.info, size.toFloat()
        ).toDouble()
    }

    override fun pageInfo(id: Int): GlyphRenderer.GlyphPage {
        val width = IntArray(pageTiles)
        val offset = id shl pageTileBits
        val xb = IntArray(1)
        val yb = IntArray(1)
        for (i in 0 until pageTiles) {
            val c = i + offset
            stbtt_GetCodepointHMetrics(font.info, c, xb, yb)
            width[i] = (xb[0] * scale).roundToInt()
        }
        return GlyphRenderer.GlyphPage(width, imageSize, tiles, tileSize)
    }

    override suspend fun page(id: Int): ByteArray {
        val buffer = ByteArray(imageSize * imageSize shl 2)
        val glyphBuffer = allocateMemoryBuffer(glyphSize * glyphSize)
        var i = 0
        val offset = id shl pageTileBits
        val xb = IntArray(1)
        val yb = IntArray(1)
        val wb = IntArray(1)
        val hb = IntArray(1)
        for (y in 0 until tiles) {
            val yy = y * glyphSize
            for (x in 0 until tiles) {
                val xx = x * glyphSize
                val c = i + offset
                if (!c.isISOControl()) {
                    stbtt_GetCodepointBitmapBox(
                        font.info,
                        c, scale.toFloat(), scale.toFloat(), xb, yb, wb, hb
                    )
                    val offsetX = max(
                        (size * 0.5).floorToInt() + xb[0], 0
                    )
                    val offsetY = max(
                        (size * 1.4).floorToInt() + yb[0], 0
                    )
                    val sizeX = glyphSize - offsetX - 1
                    val sizeY = glyphSize - offsetY - 1
                    val renderX = offsetX + xx
                    val renderY = offsetY + yy
                    // TODO: Optimize
                    for (i in 0 until glyphBuffer.size) {
                        glyphBuffer[i] = 0
                    }
                    stbtt_MakeCodepointBitmap(
                        font.info,
                        glyphBuffer, glyphSize, glyphSize,
                        glyphSize, scale.toFloat(), scale.toFloat(),
                        c
                    )
                    for (yyy in 0 until sizeY) {
                        var j = (renderY + yyy) * imageSize + renderX shl 2
                        var k = yyy * glyphSize
                        for (xxx in 0 until glyphSize) {
                            val value = glyphBuffer[k++]
                            buffer[j++] = 0xFF.toByte()
                            buffer[j++] = 0xFF.toByte()
                            buffer[j++] = 0xFF.toByte()
                            buffer[j++] = value
                        }
                    }
                }
                i++
            }
        }
        return buffer
    }

    override fun pageID(character: Char): Int =
        character.toInt() shr pageTileBits

    override fun pageCode(character: Char): Int =
        character.toInt() and pageTileMask
}
