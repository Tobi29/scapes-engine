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

import org.tobi29.arrays.BytesRO
import org.tobi29.io.IOException
import org.tobi29.io.ReadSource
import org.tobi29.scapes.engine.MemoryBufferPinned
import org.tobi29.scapes.engine.asMemoryBuffer
import org.tobi29.scapes.engine.close
import org.tobi29.scapes.engine.graphics.Font
import org.tobi29.scapes.engine.gui.GlyphRenderer
import org.tobi29.scapes.engine.readAsMemoryBufferPinned

class STBFont(
    private val fontBufferPin: MemoryBufferPinned,
    internal val info: STBTTFontinfo
) : Font {
    override fun createGlyphRenderer(size: Int): GlyphRenderer =
        STBGlyphRenderer(this, size)

    override fun close() {
        fontBufferPin.close()
        info.close()
    }

    companion object {
        suspend fun loadFont(asset: ReadSource): STBFont =
            loadFont(asset.data())

        fun loadFont(font: BytesRO): STBFont {
            val fontBufferPin = font.readAsMemoryBufferPinned()
            val info = STBTTFontinfo()
            if (stbtt_InitFont(
                    info,
                    fontBufferPin.asMemoryBuffer(),
                    stbtt_GetFontOffsetForIndex(
                        fontBufferPin.asMemoryBuffer(), 0
                    )
                )) {
                return STBFont(fontBufferPin, info)
            }
            info.close()
            fontBufferPin.close()
            throw IOException("Failed to initialize font")
        }
    }
}
