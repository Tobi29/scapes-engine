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

import org.tobi29.scapes.engine.graphics.*
import org.tobi29.stdex.assert
import org.tobi29.stdex.readOnly

internal class FBO(
    private val glh: GLESHandle,
    private val currentFBO: CurrentFBO,
    width: Int,
    height: Int,
    colorAttachments: Int,
    depth: Boolean,
    hdr: Boolean,
    alpha: Boolean,
    minFilter: TextureFilter,
    magFilter: TextureFilter
) : Framebuffer {
    override val gos: GraphicsObjectSupplier get() = glh
    override val texturesColor: List<TextureFBOColor>
    override val textureDepth: TextureFBODepth?
    private var detach: (() -> Unit)? = null
    private var framebufferID = GLFBO_EMPTY
    private var width = 0
    private var currentWidth = 0
    private var height = 0
    private var currentHeight = 0
    private var used = 0L
    override var isStored = false
    private var markAsDisposed = false

    init {
        this.width = width.coerceAtLeast(1)
        this.height = height.coerceAtLeast(1)
        texturesColor = (0 until colorAttachments).map {
            TextureFBOColor(
                glh, width, height, minFilter, magFilter,
                TextureWrap.CLAMP, TextureWrap.CLAMP, alpha, hdr
            )
        }.readOnly()
        textureDepth = if (depth) {
            TextureFBODepth(
                glh, width, height, minFilter,
                magFilter, TextureWrap.CLAMP, TextureWrap.CLAMP
            )
        } else {
            null
        }
    }

    override fun deactivate(gl: GL) {
        gl.check()
        assert { isStored }
        unbind(gl)
    }

    override fun activate(gl: GL) {
        gl.check()
        ensureStored(gl)
        bind(gl)
    }

    override fun width(): Int {
        return currentWidth
    }

    override fun height(): Int {
        return currentHeight
    }

    override fun setSize(
        width: Int,
        height: Int
    ) {
        this.width = width
        this.height = height
    }

    override fun ensureStored(gl: GL): Boolean {
        if (!isStored) {
            store(gl)
        }
        val width = this.width
        val height = this.height
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width
            currentHeight = height
            textureDepth?.resize(width, height, gl)
            for (textureColor in texturesColor) {
                textureColor.resize(width, height, gl)
            }
            bind(gl)
            gl.clear(0.0f, 0.0f, 0.0f, 0.0f)
            deactivate(gl)
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
            glh.glDeleteFramebuffers(framebufferID)
        }
        isStored = false
        detach?.invoke()
        detach = null
        framebufferID = GLFBO_EMPTY
        markAsDisposed = false
        for (textureColor in texturesColor) {
            textureColor.dispose(gl)
        }
        textureDepth?.dispose(gl)
    }

    private fun store(gl: GL) {
        assert { !isStored }
        isStored = true
        gl.check()
        framebufferID = glh.glGenFramebuffers()
        bind(gl)
        textureDepth?.attach(gl)
        for (i in texturesColor.indices) {
            texturesColor[i].attach(gl, i)
        }
        glh.drawbuffers(texturesColor.size)
        val status = glh.status()
        if (status !== FramebufferStatus.COMPLETE) {
            // TODO: Add error handling
            println(status)
        }
        glh.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        glh.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        unbind(gl)
        detach = gl.fboTracker.attach(this)
    }

    private fun bind(gl: GL) {
        gl.check()
        currentFBO.push(framebufferID)
        glh.glBindFramebuffer(GL_FRAMEBUFFER, framebufferID)
    }

    private fun unbind(gl: GL) {
        gl.check()
        val previous = currentFBO.pop(framebufferID)
        glh.glBindFramebuffer(GL_FRAMEBUFFER, previous)
    }
}
