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

package org.tobi29.scapes.engine.backends.js

import org.khronos.webgl.get
import org.tobi29.io.ReadSource
import org.tobi29.io.readAsInt8Array
import org.tobi29.math.threadLocalRandom
import org.tobi29.scapes.engine.graphics.Font
import org.tobi29.scapes.engine.gui.GlyphRenderer
import org.tobi29.stdex.isISOControl
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.Document
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.EventTarget
import kotlin.browser.document
import kotlin.coroutines.experimental.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.experimental.intrinsics.suspendCoroutineOrReturn
import kotlin.js.Promise
import kotlin.math.roundToInt

external class FontFace(
    family: String,
    source: dynamic,
    descriptors: dynamic
) {
    fun load(): Promise<FontFace>
}

abstract external class FontFaceSet : EventTarget {
    fun add(font: FontFace): FontFaceSet
    fun delete(font: FontFace): FontFace?
    fun clear()
}

@Suppress("UnsafeCastFromDynamic")
inline val Document.fonts: FontFaceSet
    get() = asDynamic().fonts

class JSFont(private val font: Pair<String, FontFace>) : Font {
    override fun createGlyphRenderer(size: Int): GlyphRenderer {
        return CanvasJSGlyphRenderer(
            font,
            size
        )
    }

    companion object {
        suspend fun loadFont(asset: ReadSource): JSFont {
            val random = threadLocalRandom()
            val familyPrefix = "scapesengine"
            val family = StringBuilder(familyPrefix.length + 16).apply {
                append(familyPrefix)
                repeat(16) { append('a' + random.nextInt(26)) }
            }.toString()
            val uri = asset.toUri()
            val face = if (uri == null)
                FontFace(
                    family,
                    asset.data().readAsInt8Array(),
                    object {})
            else FontFace(
                family,
                "url($uri)",
                object {})
            return suspendCoroutineOrReturn { cont ->
                face.load().then({
                    cont.resume(
                        JSFont(
                            family to it
                        )
                    )
                }, { cont.resumeWithException(it) })
                COROUTINE_SUSPENDED
            }
        }
    }
}

class CanvasJSGlyphRenderer(
    private val font: Pair<String, FontFace>,
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
    }

    override fun pageInfo(id: Int): GlyphRenderer.GlyphPage {
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        val width = IntArray(pageTiles)
        document.fonts.add(font.second)
        try {
            val context = canvas.getContext("2d") as CanvasRenderingContext2D
            context.font = "${size}px \"${font.first}\""
            val offset = id shl pageTileBits
            for (i in 0 until pageTiles) {
                val c = i + offset
                val glyph = context.measureText(c.toChar().toString())
                width[i] = glyph.width.roundToInt()
            }
        } finally {
            document.fonts.delete(font.second)
        }
        return GlyphRenderer.GlyphPage(width, imageSize, tiles, tileSize)
    }

    override suspend fun page(id: Int): ByteArray {
        val buffer = ByteArray(imageSize * imageSize shl 2)
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        canvas.width = glyphSize
        canvas.height = glyphSize
        document.fonts.add(font.second)
        try {
            val context = canvas.getContext("2d") as CanvasRenderingContext2D
            context.font = "${size}px \"${font.first}\""
            var i = 0
            val offset = id shl pageTileBits
            for (y in 0 until tiles) {
                val yy = y * glyphSize
                for (x in 0 until tiles) {
                    val xx = x * glyphSize
                    val c = i + offset
                    if (!c.isISOControl()) {
                        context.clearRect(
                            0.0, 0.0, glyphSize.toDouble(),
                            glyphSize.toDouble()
                        )
                        context.fillStyle = "rgb(255,255,255)"
                        context.fillText(
                            c.toChar().toString(), size * 0.5,
                            size * 1.4, size
                        )
                        val glyphImageData = context.getImageData(
                            0.0, 0.0,
                            glyphSize.toDouble(), glyphSize.toDouble()
                        )
                        val glyphData = glyphImageData.data
                        var j = 0
                        for (yyy in 0 until glyphSize) {
                            var k = (yy + yyy) * imageSize + xx shl 2
                            for (xxx in 0 until glyphSize) {
                                buffer[k++] = glyphData[j++]
                                buffer[k++] = glyphData[j++]
                                buffer[k++] = glyphData[j++]
                                buffer[k++] = glyphData[j++]
                            }
                        }
                    }
                    i++
                }
            }
        } finally {
            document.fonts.delete(font.second)
        }
        return buffer
    }

    override fun pageID(character: Char): Int {
        return character.toInt() shr pageTileBits
    }

    override fun pageCode(character: Char): Int {
        return character.toInt() and pageTileMask
    }
}
