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

package org.tobi29.scapes.engine.backends.lwjgl3.opengl

import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.GraphicsObjectSupplier
import org.tobi29.scapes.engine.graphics.TextureFilter
import org.tobi29.scapes.engine.graphics.TextureWrap
import org.tobi29.scapes.engine.utils.assert

internal class TextureFBOColor(gos: GraphicsObjectSupplier,
                               width: Int,
                               height: Int,
                               minFilter: TextureFilter,
                               magFilter: TextureFilter,
                               wrapS: TextureWrap,
                               wrapT: TextureWrap,
                               private val alpha: Boolean,
                               private val hdr: Boolean) : TextureFBO(
        gos, width, height, null, 0, minFilter, magFilter, wrapS, wrapT) {

    fun attach(gl: GL,
               i: Int) {
        if (i < 0 || i > 31) {
            throw IllegalArgumentException(
                    "Color Attachment must be 0-31, was " + i)
        }
        store(gl)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT[i],
                GL_TEXTURE_2D, textureID, 0)
    }

    override fun texture(gl: GL) {
        assert { isStored }
        gl.check()
        glBindTexture(GL_TEXTURE_2D, textureID)
        setFilter()
        if (hdr) {
            glTexImage2D(GL_TEXTURE_2D, 0,
                    if (alpha) GL_RGBA16F else GL_RGB16F,
                    buffer.width, buffer.height, 0,
                    if (alpha) GL_RGBA else GL_RGB,
                    GL_HALF_FLOAT, null)
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0,
                    if (alpha) GL_RGBA else GL_RGB, buffer.width,
                    buffer.height, 0, if (alpha) GL_RGBA else GL_RGB,
                    GL_UNSIGNED_BYTE, null)
        }
    }
}
