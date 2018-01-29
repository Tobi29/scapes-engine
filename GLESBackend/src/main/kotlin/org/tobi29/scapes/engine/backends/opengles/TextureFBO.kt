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

package org.tobi29.scapes.engine.backends.opengles

import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.TextureFilter
import org.tobi29.scapes.engine.graphics.TextureWrap
import org.tobi29.stdex.assert

internal abstract class TextureFBO(
    glh: GLESHandle,
    width: Int,
    height: Int,
    mipmaps: Int,
    minFilter: TextureFilter,
    magFilter: TextureFilter,
    wrapS: TextureWrap,
    wrapT: TextureWrap
) : TextureGL(
    glh, width, height, null, mipmaps, minFilter, magFilter, wrapS, wrapT
) {
    fun resize(width: Int, height: Int, gl: GL) {
        setBuffer(null, width, height)
        texture(gl)
    }

    override fun bind(gl: GL) {
        if (!isStored) {
            return
        }
        gl.check()
        glh.glBindTexture(GL_TEXTURE_2D, textureID)
        setFilter()
    }

    override fun markDisposed() {
        throw UnsupportedOperationException(
            "FBO texture should not be disposed"
        )
    }

    override fun ensureStored(gl: GL): Boolean {
        throw UnsupportedOperationException(
            "FBO texture can only be managed by framebuffer"
        )
    }

    override fun ensureDisposed(gl: GL) {
        throw UnsupportedOperationException(
            "FBO texture can only be managed by framebuffer"
        )
    }

    override fun isUsed(time: Long) = isStored

    override fun store(gl: GL) {
        assert { !isStored }
        isStored = true
        gl.check()
        textureID = glh.glGenTextures()
        texture(gl)
        dirtyFilter.set(true)
    }
}
