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

package org.tobi29.scapes.engine.backends.opengles

import org.tobi29.arrays.BytesRO
import org.tobi29.graphics.generateMipMapsNullable
import org.tobi29.io.ByteViewE
import org.tobi29.scapes.engine.graphics.*
import org.tobi29.stdex.assert
import org.tobi29.stdex.atomic.AtomicBoolean

internal open class TextureGL(
    protected val glh: GLESHandle,
    width: Int,
    height: Int,
    buffer: BytesRO?,
    protected val mipmaps: Int,
    minFilter: TextureFilter,
    magFilter: TextureFilter,
    wrapS: TextureWrap,
    wrapT: TextureWrap
) : Texture {
    override val gos: GraphicsObjectSupplier get() = glh
    protected val dirtyFilter = AtomicBoolean(true)
    protected val dirtyBuffer = AtomicBoolean(true)
    protected var textureID =
        GLTexture_EMPTY
    protected var minFilter = TextureFilter.NEAREST
    protected var magFilter = TextureFilter.NEAREST
    protected var wrapS = TextureWrap.REPEAT
    protected var wrapT = TextureWrap.REPEAT
    override var isStored = false
    protected var markAsDisposed = false
    protected var used: Long = 0
    protected var detach: (() -> Unit)? = null
    protected var buffer =
        TextureBuffer(
            emptyArray(),
            width,
            height
        )

    init {
        this.minFilter = minFilter
        this.magFilter = magFilter
        this.wrapS = wrapS
        this.wrapT = wrapT
        setBuffer(buffer)
    }

    override fun bind(gl: GL) {
        ensureStored(gl)
        gl.check()
        glh.glBindTexture(GL_TEXTURE_2D, textureID)
        setFilter()
    }

    protected fun setFilter() {
        if (dirtyFilter.getAndSet(false)) {
            if (mipmaps > 0) {
                when (minFilter) {
                    TextureFilter.NEAREST -> glh.glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_MIN_FILTER,
                        GL_NEAREST_MIPMAP_LINEAR
                    )
                    TextureFilter.LINEAR -> glh.glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_MIN_FILTER,
                        GL_LINEAR_MIPMAP_LINEAR
                    )
                    else -> throw IllegalArgumentException(
                        "Illegal texture-filter!"
                    )
                }
            } else {
                when (minFilter) {
                    TextureFilter.NEAREST -> glh.glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_MIN_FILTER,
                        GL_NEAREST
                    )
                    TextureFilter.LINEAR -> glh.glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_MIN_FILTER,
                        GL_LINEAR
                    )
                    else -> throw IllegalArgumentException(
                        "Illegal texture-filter!"
                    )
                }
            }
            when (magFilter) {
                TextureFilter.NEAREST -> glh.glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_MAG_FILTER,
                    GL_NEAREST
                )
                TextureFilter.LINEAR -> glh.glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_MAG_FILTER,
                    GL_LINEAR
                )
                else -> throw IllegalArgumentException(
                    "Illegal texture-filter!"
                )
            }
            when (wrapS) {
                TextureWrap.REPEAT -> glh.glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_S,
                    GL_REPEAT
                )
                TextureWrap.CLAMP -> glh.glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_S,
                    GL_CLAMP_TO_EDGE
                )
                else -> throw IllegalArgumentException("Illegal texture-wrap!")
            }
            when (wrapT) {
                TextureWrap.REPEAT -> glh.glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_T,
                    GL_REPEAT
                )
                TextureWrap.CLAMP -> glh.glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_T,
                    GL_CLAMP_TO_EDGE
                )
                else -> throw IllegalArgumentException("Illegal texture-wrap!")
            }
        }
    }

    override fun markDisposed() {
        markAsDisposed = true
    }

    override fun width(): Int {
        return buffer.width
    }

    override fun height(): Int {
        return buffer.height
    }

    override fun setWrap(
        wrapS: TextureWrap,
        wrapT: TextureWrap
    ) {
        this.wrapS = wrapS
        this.wrapT = wrapT
        dirtyFilter.set(true)
    }

    override fun setFilter(
        magFilter: TextureFilter,
        minFilter: TextureFilter
    ) {
        this.magFilter = magFilter
        this.minFilter = minFilter
        dirtyFilter.set(true)
    }

    override fun buffer(i: Int): BytesRO? {
        return buffer.buffers[i]
    }

    override fun setBuffer(buffer: BytesRO?) {
        setBuffer(buffer, this.buffer.width, this.buffer.height)
    }

    override fun setBuffer(
        buffer: BytesRO?,
        width: Int,
        height: Int
    ) {
        this.buffer = buffer(buffer, width, height)
        dirtyBuffer.set(true)
    }

    override fun ensureStored(gl: GL): Boolean {
        if (dirtyBuffer.getAndSet(false) && isStored) {
            texture(gl)
        }
        if (!isStored) {
            store(gl)
        }
        used = gl.timestamp
        return true
    }

    override fun ensureDisposed(gl: GL) {
        if (isStored) {
            dispose(gl)
        }
    }

    override fun isUsed(time: Long) =
        time - used < 1000000000L && !markAsDisposed

    override fun dispose(gl: GL?) {
        if (!isStored) {
            return
        }
        if (gl != null) {
            gl.check()
            glh.glDeleteTextures(textureID)
        }
        isStored = false
        detach?.invoke()
        detach = null
        markAsDisposed = false
    }

    protected open fun store(gl: GL) {
        assert { !isStored }
        isStored = true
        gl.check()
        textureID = glh.glGenTextures()
        texture(gl)
        dirtyFilter.set(true)
        detach = gl.textureTracker.attach(this)
    }

    protected open fun texture(gl: GL) {
        assert { isStored }
        gl.check()
        glh.glBindTexture(GL_TEXTURE_2D, textureID)
        setFilter()
        if (buffer.buffers.size > 1) {
            glh.glTexParameteri(
                GL_TEXTURE_2D,
                GL_TEXTURE_MAX_LEVEL,
                buffer.buffers.size - 1
            )
            glh.glTexImage2D(
                GL_TEXTURE_2D, 0,
                GL_RGBA, buffer.width,
                buffer.height, 0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                buffer.buffers[0]
            )
            for (i in 1 until buffer.buffers.size) {
                glh.glTexImage2D(
                    GL_TEXTURE_2D, i,
                    GL_RGBA,
                    (buffer.width shr i).coerceAtLeast(1),
                    (buffer.height shr i).coerceAtLeast(1), 0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE, buffer.buffers[i]
                )
            }
        } else {
            glh.glTexImage2D(
                GL_TEXTURE_2D, 0,
                GL_RGBA, buffer.width,
                buffer.height, 0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                buffer.buffers[0]
            )
        }
    }

    private fun buffer(
        buffer: BytesRO?,
        width: Int,
        height: Int
    ): TextureBuffer {
        return TextureBuffer(
            generateMipMapsNullable(
                buffer,
                { glh.byteView(it) }, width, height, mipmaps,
                minFilter == TextureFilter.LINEAR
            ), width, height
        )
    }

    protected class TextureBuffer(
        val buffers: Array<ByteViewE?>,
        val width: Int,
        val height: Int
    )
}
