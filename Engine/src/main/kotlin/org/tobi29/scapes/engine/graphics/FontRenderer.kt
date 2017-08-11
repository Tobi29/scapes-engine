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
import org.tobi29.scapes.engine.gui.GlyphRenderer
import org.tobi29.scapes.engine.gui.GuiRenderBatch
import org.tobi29.scapes.engine.gui.GuiUtils
import org.tobi29.scapes.engine.utils.computeAbsent
import org.tobi29.scapes.engine.utils.copy
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.math.floor
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.math.round
import org.tobi29.scapes.engine.utils.math.vector.Vector2d

class FontRenderer(private val engine: ScapesEngine,
                   private val font: Font) {
    private val pageCache = HashMap<Int, GlyphPages>()

    fun render(output: MeshOutput,
               text: String?,
               size: Float): TextInfo {
        if (text == null) {
            return EMPTY_TEXT_INFO
        }
        return render(output, text, size, 0, text.length)
    }

    fun render(output: MeshOutput,
               text: String?,
               size: Float,
               start: Int,
               end: Int): TextInfo {
        if (text == null) {
            return EMPTY_TEXT_INFO
        }
        return render(output, text, size, Float.MAX_VALUE, start, end)
    }

    fun render(output: MeshOutput,
               text: String?,
               size: Float,
               limit: Float): TextInfo {
        if (text == null) {
            return EMPTY_TEXT_INFO
        }
        return render(output, text, size, limit, 0, text.length)
    }

    fun render(output: MeshOutput,
               text: String?,
               size: Float,
               limit: Float,
               start: Int,
               end: Int): TextInfo {
        if (text == null || start == -1) {
            return EMPTY_TEXT_INFO
        }
        return render(output, text, size, size, limit, start, end)
    }

    fun render(output: MeshOutput,
               text: String?,
               width: Float,
               height: Float,
               limit: Float): TextInfo {
        if (text == null) {
            return EMPTY_TEXT_INFO
        }
        return render(output, text, width, height, limit, 0, text.length)
    }

    fun render(output: MeshOutput,
               text: String?,
               width: Float,
               height: Float,
               limit: Float,
               start: Int,
               end: Int): TextInfo {
        if (text == null || start == -1) {
            return EMPTY_TEXT_INFO
        }
        return render(output, text, width, height, height, limit, start, end)
    }

    fun render(output: MeshOutput,
               text: String?,
               width: Float,
               height: Float,
               line: Float,
               limit: Float): TextInfo {
        if (text == null) {
            return EMPTY_TEXT_INFO
        }
        return render(output, text, width, height, line, limit, 0,
                text.length)
    }

    fun render(output: MeshOutput,
               text: String?,
               width: Float,
               height: Float,
               line: Float,
               limit: Float,
               start: Int,
               end: Int): TextInfo {
        return synchronized(this) {
            if (text == null || start == -1) {
                return@synchronized EMPTY_TEXT_INFO
            }
            val size = output.size(height.toDouble())
            if (size <= 0) {
                return@synchronized EMPTY_TEXT_INFO
            }
            val pages = pageCache.computeAbsent(size) {
                GlyphPages(font.createGlyphRenderer(size))
            }
            val ratio = width / size
            var textWidth = 0.0
            var length = 0
            var xx = 0.0
            var yy = 0.0
            for (i in 0 until text.length) {
                val letter = text[i]
                if (letter == '\n') {
                    xx = 0.0
                    yy += line
                    length++
                } else {
                    val id = pages.renderer.pageID(letter)
                    val pageLetter = pages.renderer.pageCode(letter)
                    val page = pages[id]
                    val actualWidth = page.width[pageLetter] * ratio
                    if (xx + actualWidth > limit) {
                        break
                    }
                    if (i in start..(end - 1)) {
                        output.rectangle(floor(xx).toDouble(),
                                floor(yy).toDouble(),
                                width.toDouble(),
                                height.toDouble(), actualWidth.toDouble(), page,
                                pageLetter)
                    }
                    xx += actualWidth
                    textWidth = max(textWidth, xx)
                    length++
                }
            }
            TextInfo(text, Vector2d(textWidth, yy + height), length)
        }
    }

    interface MeshOutput {
        fun size(height: Double): Int

        fun rectangle(xx: Double,
                      yy: Double,
                      width: Double,
                      height: Double,
                      letterWidth: Double,
                      page: GlyphPage,
                      pageLetter: Int)
    }

    class TextInfo(val text: String,
                   val size: Vector2d,
                   val length: Int)

    class GlyphPage(val texture: Texture,
                    val width: IntArray,
                    val tiles: Int,
                    val tileSize: Double)

    private inner class GlyphPages(val renderer: GlyphRenderer) {
        private var pages = emptyArray<GlyphPage?>()

        operator fun get(id: Int): GlyphPage {
            if (id < pages.size) {
                pages[id]?.let { return it }
            }
            val pageInfo = renderer.pageInfo(id)
            val imageSize = pageInfo.size
            val texture = engine.graphics.createTexture(1, 1,
                    engine.allocate(4), 0, TextureFilter.LINEAR,
                    TextureFilter.LINEAR,
                    TextureWrap.CLAMP, TextureWrap.CLAMP)
            engine.taskExecutor.runTask({
                texture.setBuffer(renderer.page(id, engine), imageSize,
                        imageSize)
            }, "Render-Glyph-Page")
            if (pages.size <= id) {
                val newPages = arrayOfNulls<GlyphPage>(id + 1)
                copy(pages, newPages)
                pages = newPages
            }
            val glyphPage = GlyphPage(texture, pageInfo.width, pageInfo.tiles,
                    pageInfo.tileSize)
            pages[id] = glyphPage
            return glyphPage
        }
    }

    companion object : KLogging() {
        val EMPTY_TEXT_INFO = TextInfo("", Vector2d.ZERO, 0)

        fun to(renderer: GuiRenderBatch,
               r: Float,
               g: Float,
               b: Float,
               a: Float): MeshOutput {
            return to(renderer, 0.0f, 0.0f, r, g, b, a)
        }

        fun to(renderer: GuiRenderBatch,
               x: Float,
               y: Float,
               r: Float,
               g: Float,
               b: Float,
               a: Float): MeshOutput {
            return to(renderer, x, y, false, r, g, b, a)
        }

        fun to(renderer: GuiRenderBatch,
               x: Float,
               y: Float,
               cropped: Boolean,
               r: Float,
               g: Float,
               b: Float,
               a: Float): MeshOutput {
            val pixelSize = renderer.pixelSize.floatY()
            if (cropped) {
                return object : MeshOutput {
                    override fun size(height: Double): Int {
                        return round(height / pixelSize)
                    }

                    override fun rectangle(xx: Double,
                                           yy: Double,
                                           width: Double,
                                           height: Double,
                                           letterWidth: Double,
                                           page: GlyphPage,
                                           pageLetter: Int) {
                        val xxx = x + xx
                        val yyy = y + yy
                        val w = letterWidth
                        val h = height
                        val tx = (pageLetter % page.tiles + 0.25) * page.tileSize
                        val ty = (pageLetter / page.tiles + 0.25) * page.tileSize
                        val tw = (letterWidth / width) * page.tileSize * 0.5
                        val th = page.tileSize * 0.5
                        renderer.texture(page.texture, 0)
                        GuiUtils.rectangle(renderer,
                                xxx.toFloat(),
                                yyy.toFloat(),
                                (xxx + w).toFloat(),
                                (yyy + h).toFloat(),
                                tx.toFloat(),
                                ty.toFloat(),
                                (tx + tw).toFloat(),
                                (ty + th).toFloat(),
                                r, g, b, a)
                    }
                }
            } else {
                return object : MeshOutput {
                    override fun size(height: Double): Int {
                        return round(height / pixelSize)
                    }

                    override fun rectangle(xx: Double,
                                           yy: Double,
                                           width: Double,
                                           height: Double,
                                           letterWidth: Double,
                                           page: GlyphPage,
                                           pageLetter: Int) {
                        val xxx = x + xx - width * 0.5
                        val yyy = y + yy - height * 0.5
                        val w = width * 2.0
                        val h = height * 2.0
                        val tx = pageLetter % page.tiles * page.tileSize
                        val ty = pageLetter / page.tiles * page.tileSize
                        val tw = page.tileSize
                        val th = page.tileSize
                        renderer.texture(page.texture, 0)
                        GuiUtils.rectangle(renderer,
                                xxx.toFloat(),
                                yyy.toFloat(),
                                (xxx + w).toFloat(),
                                (yyy + h).toFloat(),
                                tx.toFloat(),
                                ty.toFloat(),
                                (tx + tw).toFloat(),
                                (ty + th).toFloat(),
                                r, g, b, a)
                    }
                }
            }
        }

        fun to(): MeshOutput {
            return object : MeshOutput {
                override fun size(height: Double): Int {
                    return 16
                }

                override fun rectangle(xx: Double,
                                       yy: Double,
                                       width: Double,
                                       height: Double,
                                       letterWidth: Double,
                                       page: GlyphPage,
                                       pageLetter: Int) {
                }
            }
        }
    }
}
