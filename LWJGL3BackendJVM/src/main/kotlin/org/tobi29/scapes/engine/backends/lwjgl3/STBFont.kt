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
package org.tobi29.scapes.engine.backends.lwjgl3

import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import org.tobi29.scapes.engine.ScapesEngineBackend
import org.tobi29.scapes.engine.graphics.Font
import org.tobi29.scapes.engine.gui.GlyphRenderer
import org.tobi29.scapes.engine.utils.io.*
import java.nio.ByteBuffer

class STBFont(internal val container: ScapesEngineBackend,
              internal val fontBuffer: ByteBuffer,
              internal val info: STBTTFontinfo) : Font {

    override fun createGlyphRenderer(size: Int): GlyphRenderer {
        return STBGlyphRenderer(this, size)
    }

    companion object {
        suspend fun loadFont(backend: ScapesEngineBackend,
                             asset: ReadSource): STBFont =
                loadFont(backend, asset.data())

        fun loadFont(backend: ScapesEngineBackend,
                     font: ByteViewRO): STBFont {
            val fontBuffer = ByteBufferNative(font.size)
            fontBuffer.put(font.readAsByteBuffer())
            fontBuffer._flip()
            val infoBuffer = STBTTFontinfo.create()
            if (STBTruetype.stbtt_InitFont(infoBuffer, fontBuffer)) {
                return STBFont(backend, fontBuffer, infoBuffer)
            }
            throw IOException("Failed to initialize font")
        }
    }
}
