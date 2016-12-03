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

import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import org.tobi29.scapes.engine.graphics.Font
import org.tobi29.scapes.engine.gui.GlyphRenderer
import org.tobi29.scapes.engine.utils.io.asBuffer
import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource
import org.tobi29.scapes.engine.utils.io.process
import java.io.IOException
import java.nio.ByteBuffer

class STBFont(internal val container: ContainerLWJGL3,
              internal val fontBuffer: ByteBuffer,
              internal val info: STBTTFontinfo) : Font {

    override fun createGlyphRenderer(size: Int): GlyphRenderer {
        return STBGlyphRenderer(this, size)
    }

    companion object {
        fun fromFont(container: ContainerLWJGL3,
                     font: ReadSource): STBFont? {
            try {
                val buffer = font.read { process(it, asBuffer()) }
                val fontBuffer = container.allocate(buffer.remaining())
                fontBuffer.put(buffer)
                fontBuffer.flip()
                val infoBuffer = STBTTFontinfo.create()
                if (STBTruetype.stbtt_InitFont(infoBuffer, fontBuffer)) {
                    return STBFont(container, fontBuffer, infoBuffer)
                }
            } catch (e: IOException) {
            }

            return null
        }
    }
}
