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
import org.lwjgl.opengles.GLES30
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.TextureFilter
import org.tobi29.scapes.engine.graphics.TextureWrap
import java.nio.ByteBuffer

internal class TextureFBODepth(engine: ScapesEngine, width: Int, height: Int,
                               minFilter: TextureFilter, magFilter: TextureFilter, wrapS: TextureWrap,
                               wrapT: TextureWrap) : TextureFBO(engine, width,
        height, null, 0, minFilter, magFilter, wrapS, wrapT) {

    fun attach(gl: GL) {
        store(gl)
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, textureID, 0)
    }

    override fun texture(gl: GL) {
        assert(isStored)
        gl.check()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)
        setFilter()
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT24,
                buffer.width, buffer.height, 0, GLES20.GL_DEPTH_COMPONENT,
                GLES20.GL_UNSIGNED_INT, null as ByteBuffer?)
    }
}
