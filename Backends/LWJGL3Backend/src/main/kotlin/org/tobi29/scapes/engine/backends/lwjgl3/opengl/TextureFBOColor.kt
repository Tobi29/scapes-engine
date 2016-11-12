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

package org.tobi29.scapes.engine.backends.lwjgl3.opengl

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.TextureFilter
import org.tobi29.scapes.engine.graphics.TextureWrap

import java.nio.ByteBuffer

internal class TextureFBOColor(engine: ScapesEngine, width: Int, height: Int,
                               minFilter: TextureFilter, magFilter: TextureFilter, wrapS: TextureWrap,
                               wrapT: TextureWrap, private val alpha: Boolean, private val hdr: Boolean) : TextureFBO(
        engine, width, height, null, 0, minFilter, magFilter, wrapS, wrapT) {

    fun attach(gl: GL,
               i: Int) {
        if (i < 0 || i > 31) {
            throw IllegalArgumentException(
                    "Color Attachment must be 0-31, was " + i)
        }
        store(gl)
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,
                GL30.GL_COLOR_ATTACHMENT0 + i, GL11.GL_TEXTURE_2D, textureID, 0)
    }

    override fun texture(gl: GL) {
        assert(isStored)
        gl.check()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID)
        if (hdr) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0,
                    if (alpha) GL30.GL_RGBA16F else GL30.GL_RGB16F,
                    buffer.width, buffer.height, 0,
                    if (alpha) GL11.GL_RGBA else GL11.GL_RGB,
                    GL30.GL_HALF_FLOAT, null as ByteBuffer?)
        } else {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0,
                    if (alpha) GL11.GL_RGBA else GL11.GL_RGB, buffer.width,
                    buffer.height, 0, if (alpha) GL11.GL_RGBA else GL11.GL_RGB,
                    GL11.GL_UNSIGNED_BYTE, null as ByteBuffer?)
        }
    }
}
