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
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.RenderType
import org.tobi29.scapes.engine.graphics.Shader
import java.nio.ByteBuffer

internal class VAOStatic(private val vbo: VBO,
                         index: IntArray,
                         private val length: Int,
                         private val renderType: RenderType) : VAO(
        vbo.engine) {
    private var data: ByteBuffer? = null
    private var indexID = 0
    private var arrayID = 0

    init {
        if (renderType == RenderType.TRIANGLES && length % 3 != 0) {
            throw IllegalArgumentException("Length not multiply of 3")
        } else if (renderType == RenderType.LINES && length % 2 != 0) {
            throw IllegalArgumentException("Length not multiply of 2")
        }
        val indexBuffer = engine.allocate(length shl 1)
        for (i in 0..length - 1) {
            indexBuffer.putShort(index[i].toShort())
        }
        data = indexBuffer
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
        GLES30.glBindVertexArray(arrayID)
        shader(gl, shader)
        GLES20.glDrawElements(GLUtils.renderType(renderType), length,
                GLES20.GL_UNSIGNED_SHORT, 0)
        return true
    }

    override fun renderInstanced(gl: GL,
                                 shader: Shader,
                                 count: Int): Boolean {
        throw UnsupportedOperationException(
                "Cannot render indexed VAO with length parameter")
    }

    override fun renderInstanced(gl: GL,
                                 shader: Shader,
                                 length: Int,
                                 count: Int): Boolean {
        throw UnsupportedOperationException(
                "Cannot render indexed VAO with length parameter")
    }

    override fun reset() {
        super.reset()
        vbo.reset()
    }

    override fun store(gl: GL): Boolean {
        assert(!isStored)
        val data = data ?: return false
        if (!vbo.canStore()) {
            return false
        }
        isStored = true
        gl.check()
        arrayID = GLES30.glGenVertexArrays()
        GLES30.glBindVertexArray(arrayID)
        vbo.store(gl, weak)
        data.rewind()
        indexID = GLES20.glGenBuffers()
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexID)
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, data,
                GLES20.GL_STATIC_DRAW)
        detach = gl.vaoTracker().attach(this)
        if (weak) {
            this.data = null
        }
        return true
    }

    override fun dispose(gl: GL) {
        assert(isStored)
        gl.check()
        vbo.dispose(gl)
        GLES20.glDeleteBuffers(indexID)
        GLES30.glDeleteVertexArrays(arrayID)
    }
}
