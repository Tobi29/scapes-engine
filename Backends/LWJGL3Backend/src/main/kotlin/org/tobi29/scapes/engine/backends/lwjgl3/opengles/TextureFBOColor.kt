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

package org.tobi29.scapes.engine.backends.lwjgl3.opengles

import org.lwjgl.opengles.GLES20
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.TextureFilter
import org.tobi29.scapes.engine.graphics.TextureWrap
import java.nio.ByteBuffer

internal class TextureFBOColor(engine: ScapesEngine,
                               width: Int,
                               height: Int,
                               minFilter: TextureFilter,
                               magFilter: TextureFilter,
                               wrapS: TextureWrap,
                               wrapT: TextureWrap,
                               private val alpha: Boolean,
                               private val hdr: Boolean) : TextureFBO(
        engine, width, height, null, 0, minFilter, magFilter, wrapS, wrapT) {

    fun attach(gl: GL,
               i: Int) {
        if (i < 0 || i > 31) {
            throw IllegalArgumentException(
                    "Color Attachment must be 0-31, was " + i)
        }
        store(gl)
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0 + i, GLES20.GL_TEXTURE_2D,
                textureID, 0)
    }

    override fun texture(gl: GL) {
        assert(isStored)
        gl.check()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)
        setFilter()
        // TODO: Is this broken on radeonsi or how should this be done?
        // Note: OESTextureHalfFloat.GL_HALF_FLOAT_OES does not work either
        /*if (hdr) {
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0,
                    if (alpha) GLES30.GL_RGBA16F else GLES30.GL_RGB16F,
                    buffer.width, buffer.height, 0,
                    if (alpha) GLES20.GL_RGBA else GLES20.GL_RGB,
                    GLES30.GL_HALF_FLOAT, null as ByteBuffer?)
        } else {*/
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0,
                if (alpha) GLES20.GL_RGBA else GLES20.GL_RGB, buffer.width,
                buffer.height, 0,
                if (alpha) GLES20.GL_RGBA else GLES20.GL_RGB,
                GLES20.GL_UNSIGNED_BYTE, null as ByteBuffer?)
        //}
    }
}
