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
import org.lwjgl.opengl.GL12
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.TextureFilter
import org.tobi29.scapes.engine.graphics.TextureWrap

import java.nio.ByteBuffer

internal abstract class TextureFBO(engine: ScapesEngine, width: Int, height: Int,
                                   buffer: ByteBuffer?, mipmaps: Int, minFilter: TextureFilter,
                                   magFilter: TextureFilter, wrapS: TextureWrap, wrapT: TextureWrap) : TextureGL(
        engine, width, height, buffer, mipmaps, minFilter, magFilter, wrapS,
        wrapT) {

    fun resize(width: Int,
               height: Int,
               gl: GL) {
        setBuffer(null, width, height)
        texture(gl)
    }

    override fun bind(gl: GL) {
        if (!isStored) {
            return
        }
        gl.check()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID)
        if (dirtyFilter.getAndSet(false)) {
            if (mipmaps > 0) {
                when (minFilter) {
                    TextureFilter.NEAREST -> GL11.glTexParameteri(
                            GL11.GL_TEXTURE_2D,
                            GL11.GL_TEXTURE_MIN_FILTER,
                            GL11.GL_NEAREST_MIPMAP_LINEAR)
                    TextureFilter.LINEAR -> GL11.glTexParameteri(
                            GL11.GL_TEXTURE_2D,
                            GL11.GL_TEXTURE_MIN_FILTER,
                            GL11.GL_LINEAR_MIPMAP_LINEAR)
                    else -> throw IllegalArgumentException(
                            "Illegal texture-filter!")
                }
            } else {
                when (minFilter) {
                    TextureFilter.NEAREST -> GL11.glTexParameteri(
                            GL11.GL_TEXTURE_2D,
                            GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
                    TextureFilter.LINEAR -> GL11.glTexParameteri(
                            GL11.GL_TEXTURE_2D,
                            GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
                    else -> throw IllegalArgumentException(
                            "Illegal texture-filter!")
                }
            }
            when (magFilter) {
                TextureFilter.NEAREST -> GL11.glTexParameteri(
                        GL11.GL_TEXTURE_2D,
                        GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
                TextureFilter.LINEAR -> GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                        GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
                else -> throw IllegalArgumentException(
                        "Illegal texture-filter!")
            }
            when (wrapS) {
                TextureWrap.REPEAT -> GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                        GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
                TextureWrap.CLAMP -> GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                        GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
                else -> throw IllegalArgumentException("Illegal texture-wrap!")
            }
            when (wrapT) {
                TextureWrap.REPEAT -> GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                        GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
                TextureWrap.CLAMP -> GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                        GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
                else -> throw IllegalArgumentException("Illegal texture-wrap!")
            }
        }
    }

    override fun markDisposed() {
        throw UnsupportedOperationException(
                "FBO texture should not be disposed")
    }

    override fun ensureStored(gl: GL): Boolean {
        throw UnsupportedOperationException(
                "FBO texture can only be managed by framebuffer")
    }

    override fun ensureDisposed(gl: GL) {
        throw UnsupportedOperationException(
                "FBO texture can only be managed by framebuffer")
    }

    override fun isUsed(time: Long): Boolean {
        return isStored
    }

    override fun reset() {
        assert(isStored)
        isStored = false
        markAsDisposed = false
    }

    override fun store(gl: GL) {
        assert(!isStored)
        isStored = true
        gl.check()
        textureID = GL11.glGenTextures()
        texture(gl)
        dirtyFilter.set(true)
    }
}
