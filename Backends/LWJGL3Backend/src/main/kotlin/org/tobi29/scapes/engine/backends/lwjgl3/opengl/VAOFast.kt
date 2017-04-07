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
import org.tobi29.scapes.engine.graphics.RenderType
import org.tobi29.scapes.engine.graphics.Shader

internal class VAOFast(private val vbo: VBO,
                       private val length: Int,
                       private val renderType: RenderType) : VAO(
        vbo.engine) {
    private var arrayID = 0

    init {
        if (renderType == RenderType.TRIANGLES && length % 3 != 0) {
            throw IllegalArgumentException("Length not multiply of 3")
        } else if (renderType == RenderType.LINES && length % 2 != 0) {
            throw IllegalArgumentException("Length not multiply of 2")
        }
    }

    override fun render(gl: GL,
                        shader: Shader): Boolean {
        return render(gl, shader, length)
    }

    override fun render(gl: GL,
                        shader: Shader,
                        length: Int): Boolean {
        if (!ensureStored(gl)) {
            return false
        }
        gl.check()
        shader(gl, shader)
        glBindVertexArray(arrayID)
        glDrawArrays(GLUtils.renderType(renderType), 0, length)
        return true
    }

    override fun renderInstanced(gl: GL,
                                 shader: Shader,
                                 count: Int): Boolean {
        return renderInstanced(gl, shader, length, count)
    }

    override fun renderInstanced(gl: GL,
                                 shader: Shader,
                                 length: Int,
                                 count: Int): Boolean {
        if (!ensureStored(gl)) {
            return false
        }
        gl.check()
        shader(gl, shader)
        glBindVertexArray(arrayID)
        glDrawArraysInstanced(GLUtils.renderType(renderType), 0, length, count)
        return true
    }

    override fun store(gl: GL): Boolean {
        assert(!isStored)
        if (!vbo.canStore()) {
            return false
        }
        isStored = true
        gl.check()
        arrayID = glGenVertexArrays()
        glBindVertexArray(arrayID)
        vbo.store(gl, weak)
        detach = gl.vaoTracker.attach(this)
        return true
    }

    override fun dispose(gl: GL?) {
        if (!isStored) {
            return
        }
        if (gl != null) {
            gl.check()
            vbo.dispose(gl)
            glDeleteVertexArrays(arrayID)
        }
        isStored = false
        detach?.invoke()
        detach = null
        markAsDisposed = false
        vbo.reset()
    }
}
