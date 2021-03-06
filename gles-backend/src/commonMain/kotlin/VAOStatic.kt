/*
 * Copyright 2012-2019 Tobi29
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

import net.gitout.ktbindings.gles.*
import org.tobi29.arrays.Bytes
import org.tobi29.arrays.BytesRO
import org.tobi29.scapes.engine.allocateMemoryBuffer
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
    private var data: Bytes? = null
    private var indexID = emptyGLBuffer
    private var arrayID = emptyGLVertexArrayObject

    init {
        if (renderType == RenderType.TRIANGLES && length % 3 != 0) {
            throw IllegalArgumentException("Length not multiply of 3")
        } else if (renderType == RenderType.LINES && length % 2 != 0) {
            throw IllegalArgumentException("Length not multiply of 2")
        }
        val indexBuffer = allocateMemoryBuffer(length shl 1)
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
        glh.gl.glBindVertexArray(arrayID)
        glh.gl.glDrawElements(
            renderType.enum, length,
            GL_UNSIGNED_SHORT, 0u
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
        arrayID = glh.gl.glCreateVertexArray()
        glh.gl.glBindVertexArray(arrayID)
        vbo.store(gl, weak)
        indexID = glh.gl.glCreateBuffer()
        glh.gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexID)
        glh.gl.glBufferData(
            GL_ELEMENT_ARRAY_BUFFER, data.asDataBuffer(),
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
            glh.gl.glDeleteBuffer(indexID)
            glh.gl.glDeleteVertexArray(arrayID)
        }
        isStored = false
        detach?.invoke()
        detach = null
        markDisposed = false
        vbo.reset()
    }

    override fun buffer(
        gl: GL,
        buffer: BytesRO
    ) {
        vbo.replaceBuffer(gl, buffer)
    }

    override val stride get() = vbo.stride()

    override fun bufferIndices(
        gl: GL,
        buffer: BytesRO
    ) {
        ensureStored(gl)
        glh.gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexID)
        glh.gl.glBufferData(
            GL_ELEMENT_ARRAY_BUFFER, buffer.asDataBuffer(),
            GL_STREAM_DRAW
        )
    }
}
