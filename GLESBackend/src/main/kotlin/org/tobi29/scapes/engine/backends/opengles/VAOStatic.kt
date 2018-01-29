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

import org.tobi29.io.ByteView
import org.tobi29.io.ByteViewRO
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.ModelIndexed
import org.tobi29.scapes.engine.graphics.RenderType
import org.tobi29.scapes.engine.graphics.Shader
import org.tobi29.stdex.assert

internal class VAOStatic(
    private val vbo: VBO,
    index: IntArray,
    private val length: Int,
    private val renderType: RenderType
) : VAO(vbo.glh),
    ModelIndexed {
    private var data: ByteView? = null
    private var indexID = GLVBO_EMPTY
    private var arrayID = GLVAO_EMPTY

    init {
        if (renderType == RenderType.TRIANGLES && length % 3 != 0) {
            throw IllegalArgumentException("Length not multiply of 3")
        } else if (renderType == RenderType.LINES && length % 2 != 0) {
            throw IllegalArgumentException("Length not multiply of 2")
        }
        val indexBuffer = vbo.glh.container.allocateNative(length shl 1)
        for (i in 0 until length) {
            indexBuffer.setShort(i shl 1, index[i].toShort())
        }
        data = indexBuffer
    }

    override fun render(
        gl: GL,
        shader: Shader
    ): Boolean {
        return render(gl, shader, length)
    }

    override fun render(
        gl: GL,
        shader: Shader,
        length: Int
    ): Boolean {
        if (!ensureStored(gl)) {
            return false
        }
        gl.check()
        shader(gl, shader)
        glh.glBindVertexArray(arrayID)
        glh.glDrawElements(
            glh.renderType(renderType), length,
            GL_UNSIGNED_SHORT, 0
        )
        return true
    }

    override fun renderInstanced(
        gl: GL,
        shader: Shader,
        count: Int
    ): Boolean {
        throw UnsupportedOperationException(
            "Cannot render indexed VAO with length parameter"
        )
    }

    override fun renderInstanced(
        gl: GL,
        shader: Shader,
        length: Int,
        count: Int
    ): Boolean {
        throw UnsupportedOperationException(
            "Cannot render indexed VAO with length parameter"
        )
    }

    override fun store(gl: GL): Boolean {
        assert { !isStored }
        val data = data ?: return false
        if (!vbo.canStore()) {
            return false
        }
        isStored = true
        gl.check()
        arrayID = glh.glGenVertexArrays()
        glh.glBindVertexArray(arrayID)
        vbo.store(gl, weak)
        indexID = glh.glGenBuffers()
        glh.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexID)
        glh.glBufferData(
            GL_ELEMENT_ARRAY_BUFFER, data,
            GL_STATIC_DRAW
        )
        detach = gl.vaoTracker.attach(this)
        if (weak) {
            this.data = null
        }
        return true
    }

    override fun dispose(gl: GL?) {
        if (!isStored) {
            return
        }
        if (gl != null) {
            gl.check()
            vbo.dispose(gl)
            glh.glDeleteBuffers(indexID)
            glh.glDeleteVertexArrays(arrayID)
        }
        isStored = false
        detach?.invoke()
        detach = null
        markDisposed = false
        vbo.reset()
    }

    override fun buffer(
        gl: GL,
        buffer: ByteViewRO
    ) {
        vbo.replaceBuffer(gl, buffer)
    }

    override val stride get() = vbo.stride()

    override fun bufferIndices(
        gl: GL,
        buffer: ByteViewRO
    ) {
        ensureStored(gl)
        glh.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexID)
        glh.glBufferData(
            GL_ELEMENT_ARRAY_BUFFER, buffer,
            GL_STREAM_DRAW
        )
    }
}
