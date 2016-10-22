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

import java8.util.stream.Stream
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.*
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.stream

internal class FBO(engine: ScapesEngine, width: Int, height: Int, colorAttachments: Int,
                   depth: Boolean, hdr: Boolean, alpha: Boolean) : Framebuffer {
    private val texturesColor: Array<TextureFBOColor>
    private val textureDepth: TextureFBODepth?
    private var detach: Function0<Unit>? = null
    private var framebufferID = 0
    private var width = 0
    private var currentWidth = 0
    private var height = 0
    private var currentHeight = 0
    private var lastFBO = 0
    private var used: Long = 0
    override var isStored = false
    private var markAsDisposed = false

    init {
        this.width = max(width, 1)
        this.height = max(height, 1)
        texturesColor = Array(colorAttachments) {
            TextureFBOColor(engine, width, height,
                    TextureFilter.LINEAR, TextureFilter.LINEAR,
                    TextureWrap.CLAMP, TextureWrap.CLAMP, alpha, hdr)
        }
        if (depth) {
            textureDepth = TextureFBODepth(engine, width, height,
                    TextureFilter.LINEAR, TextureFilter.LINEAR,
                    TextureWrap.CLAMP, TextureWrap.CLAMP)
        } else {
            textureDepth = null
        }
    }

    override fun deactivate(gl: GL) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastFBO)
        gl.currentFBO = lastFBO
        lastFBO = 0
    }

    override fun activate(gl: GL) {
        ensureStored(gl)
        bind(gl)
    }

    override fun width(): Int {
        return currentWidth
    }

    override fun height(): Int {
        return currentHeight
    }

    override fun setSize(width: Int,
                         height: Int) {
        this.width = width
        this.height = height
    }

    override fun texturesColor(): Stream<Texture> {
        return stream(*texturesColor)
    }

    override fun textureColor(i: Int): Texture {
        return texturesColor[i]
    }

    override fun textureDepth(): Texture {
        if (textureDepth == null) {
            throw IllegalStateException("FBO has no depth buffer")
        }
        return textureDepth
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
        used = System.currentTimeMillis()
        return true
    }

    override fun ensureDisposed(gl: GL) {
        if (isStored) {
            dispose(gl)
            reset()
        }
    }

    override fun isUsed(time: Long): Boolean {
        return time - used < 1000 && !markAsDisposed
    }

    override fun dispose(gl: GL) {
        gl.check()
        GL30.glDeleteFramebuffers(framebufferID)
        for (textureColor in texturesColor) {
            textureColor.dispose(gl)
        }
        textureDepth?.dispose(gl)
    }

    override fun reset() {
        assert(isStored)
        isStored = false
        detach?.invoke()
        detach = null
        framebufferID = 0
        lastFBO = 0
        markAsDisposed = false
        for (textureColor in texturesColor) {
            textureColor.reset()
        }
        textureDepth?.reset()
    }

    private fun store(gl: GL) {
        assert(!isStored)
        isStored = true
        gl.check()
        framebufferID = GL30.glGenFramebuffers()
        bind(gl)
        textureDepth?.attach(gl)
        for (i in texturesColor.indices) {
            texturesColor[i].attach(gl, i)
        }
        GLUtils.drawbuffers(texturesColor.size)
        val status = GLUtils.status()
        if (status !== FramebufferStatus.COMPLETE) {
            // TODO: Add error handling
            println(status)
        }
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        deactivate(gl)
        detach = gl.fboTracker().attach(this)
    }

    private fun bind(gl: GL) {
        assert(isStored)
        gl.check()
        lastFBO = gl.currentFBO()
        gl.currentFBO = framebufferID
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferID)
    }
}
