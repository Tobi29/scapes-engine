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

import org.tobi29.scapes.engine.graphics.*
import org.tobi29.scapes.engine.utils.graphics.Image
import org.tobi29.scapes.engine.utils.graphics.flipVertical
import org.tobi29.scapes.engine.utils.io.ByteBuffer
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.math.pow

class GLLWJGL3GLES(gos: GraphicsObjectSupplier) : GL(gos) {
    override fun checkError(message: String) {
        val error = glGetError()
        if (error != GL_NO_ERROR) {
            val errorName = when (error) {
                GL_INVALID_ENUM -> "Enum argument out of range"
                GL_INVALID_VALUE -> "Numeric argument out of range"
                GL_INVALID_OPERATION -> "Operation illegal in current state"
                GL_STACK_OVERFLOW -> "Command would cause a stack overflow"
                GL_STACK_UNDERFLOW -> "Command would cause a stack underflow"
                GL_OUT_OF_MEMORY -> "Not enough memory left to execute command"
                GL_INVALID_FRAMEBUFFER_OPERATION -> "Framebuffer object is not complete"
                GL_TABLE_TOO_LARGE -> "The specified table is too large"
                else -> "Unknown error code"
            }
            throw GraphicsException("$errorName in $message")
        }
    }

    override fun clear(r: Float,
                       g: Float,
                       b: Float,
                       a: Float) {
        glClearColor(r, g, b, a)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }

    override fun clearDepth() {
        glClear(GL_DEPTH_BUFFER_BIT)
    }

    override fun disableCulling() {
        glDisable(GL_CULL_FACE)
    }

    override fun disableDepthTest() {
        glDisable(GL_DEPTH_TEST)
    }

    override fun disableDepthMask() {
        glDepthMask(false)
    }

    override fun disableWireframe() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
    }

    override fun disableScissor() {
        glDisable(GL_SCISSOR_TEST)
    }

    override fun enableCulling() {
        glEnable(GL_CULL_FACE)
    }

    override fun enableDepthTest() {
        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)
    }

    override fun enableDepthMask() {
        glDepthMask(true)
    }

    override fun enableWireframe() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    }

    override fun enableScissor(x: Int,
                               y: Int,
                               width: Int,
                               height: Int) {
        glEnable(GL_SCISSOR_TEST)
        glScissor(x, contentHeight - y - height, width, height)
    }

    override fun setBlending(mode: BlendingMode) {
        when (mode) {
            BlendingMode.NONE -> glDisable(GL_BLEND)
            BlendingMode.NORMAL -> {
                glEnable(GL_BLEND)
                glBlendFunc(GL_SRC_ALPHA,
                        GL_ONE_MINUS_SRC_ALPHA)
            }
            BlendingMode.ADD -> {
                glEnable(GL_BLEND)
                glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA)
            }
            BlendingMode.INVERT -> {
                glEnable(GL_BLEND)
                glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR)
            }
        }
    }

    override fun setViewport(x: Int,
                             y: Int,
                             width: Int,
                             height: Int) {
        glViewport(x, y, width, height)
    }

    override fun getViewport(output: IntArray) {
        glGetIntegerv(GL_VIEWPORT, output)
    }

    override fun screenShot(x: Int,
                            y: Int,
                            width: Int,
                            height: Int): Image {
        val buffer = engine.allocate(width * height shl 2)
        glReadBuffer(GL_FRONT)
        glReadPixels(x, y, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer)
        flipVertical(width, height, buffer)
        return Image(width, height, buffer)
    }

    override fun screenShotFBO(fbo: Framebuffer): Image {
        val buffer = engine.allocate(fbo.width() * fbo.height() shl 2)
        glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)
        flipVertical(fbo.width(), fbo.height(), buffer)
        return Image(fbo.width(), fbo.height(), buffer)
    }

    override fun setAttribute1f(id: Int,
                                v0: Float) {
        glVertexAttrib1f(id, v0)
    }

    override fun setAttribute2f(id: Int,
                                v0: Float,
                                v1: Float) {
        glVertexAttrib2f(id, v0, v1)
    }

    override fun setAttribute3f(id: Int,
                                v0: Float,
                                v1: Float,
                                v2: Float) {
        glVertexAttrib3f(id, v0, v1, v2)
    }

    override fun setAttribute4f(id: Int,
                                v0: Float,
                                v1: Float,
                                v2: Float,
                                v3: Float) {
        glVertexAttrib4f(id, v0, v1, v2, v3)
    }

    override fun setAttribute1f(uniform: Int,
                                values: FloatArray) {
        glVertexAttrib1fv(uniform, values)
    }

    override fun setAttribute2f(uniform: Int,
                                values: FloatArray) {
        glVertexAttrib2fv(uniform, values)
    }

    override fun setAttribute3f(uniform: Int,
                                values: FloatArray) {
        glVertexAttrib3fv(uniform, values)
    }

    override fun setAttribute4f(uniform: Int,
                                values: FloatArray) {
        glVertexAttrib4fv(uniform, values)
    }

    override fun replaceTexture(x: Int,
                                y: Int,
                                width: Int,
                                height: Int,
                                buffer: ByteBuffer) {
        glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, width, height, GL_RGBA,
                GL_UNSIGNED_BYTE, buffer)
    }

    override fun replaceTextureMipMap(x: Int,
                                      y: Int,
                                      width: Int,
                                      height: Int,
                                      vararg buffers: ByteBuffer) {
        glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, width, height,
                GL_RGBA, GL_UNSIGNED_BYTE, buffers[0])
        for (i in 1..buffers.size - 1) {
            val scale = pow(2f, i.toFloat()).toInt()
            glTexSubImage2D(GL_TEXTURE_2D, i, x / scale, y / scale,
                    max(width / scale, 1),
                    max(height / scale, 1), GL_RGBA,
                    GL_UNSIGNED_BYTE, buffers[i])
        }
    }

    override fun activeTexture(i: Int) {
        if (i < 0 || i > 31) {
            throw IllegalArgumentException(
                    "Active Texture must be 0-31, was " + i)
        }
        glActiveTexture(GL_TEXTURE[i])
    }
}
