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
import org.tobi29.stdex.assert

internal class TextureFBODepth(
        gos: GraphicsObjectSupplier,
        width: Int,
        height: Int,
        minFilter: TextureFilter,
        magFilter: TextureFilter,
        wrapS: TextureWrap,
        wrapT: TextureWrap
) : TextureFBO(gos, width, height, 0, minFilter, magFilter, wrapS, wrapT) {

    fun attach(gl: GL) {
        store(gl)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                GL_TEXTURE_2D, textureID, 0)
    }

    override fun texture(gl: GL) {
        assert { isStored }
        gl.check()
        glBindTexture(GL_TEXTURE_2D, textureID)
        setFilter()
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24,
                buffer.width, buffer.height, 0, GL_DEPTH_COMPONENT,
                GL_UNSIGNED_INT, null)
    }
}
