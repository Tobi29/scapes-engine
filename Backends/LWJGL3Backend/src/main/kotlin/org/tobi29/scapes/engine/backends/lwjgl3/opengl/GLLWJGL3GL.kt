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

import org.lwjgl.opengl.*
import org.tobi29.scapes.engine.Container
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.*
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.utils.graphics.Image
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.math.pow
import org.tobi29.scapes.engine.utils.shader.CompiledShader
import java.nio.ByteBuffer
import java.nio.FloatBuffer

class GLLWJGL3GL(engine: ScapesEngine, container: Container) : GL(engine,
        container) {

    override fun createTexture(width: Int,
                               height: Int,
                               buffer: ByteBuffer,
                               mipmaps: Int,
                               minFilter: TextureFilter,
                               magFilter: TextureFilter,
                               wrapS: TextureWrap,
                               wrapT: TextureWrap): Texture {
        return TextureGL(engine, width, height, buffer, mipmaps, minFilter,
                magFilter, wrapS, wrapT)
    }

    override fun createFramebuffer(width: Int,
                                   height: Int,
                                   colorAttachments: Int,
                                   depth: Boolean,
                                   hdr: Boolean,
                                   alpha: Boolean): Framebuffer {
        return FBO(engine, width, height, colorAttachments, depth, hdr,
                alpha)
    }

    override fun createModelFast(attributes: List<ModelAttribute>,
                                 length: Int,
                                 renderType: RenderType): Model {
        val vbo = VBO(engine, attributes, length)
        return VAOFast(vbo, length, renderType)
    }

    override fun createModelStatic(attributes: List<ModelAttribute>,
                                   length: Int,
                                   index: IntArray,
                                   indexLength: Int,
                                   renderType: RenderType): Model {
        val vbo = VBO(engine, attributes, length)
        return VAOStatic(vbo, index, indexLength, renderType)
    }

    override fun createModelHybrid(attributes: List<ModelAttribute>,
                                   length: Int,
                                   attributesStream: List<ModelAttribute>,
                                   lengthStream: Int,
                                   renderType: RenderType): ModelHybrid {
        val vbo = VBO(engine, attributes, length)
        val vboStream = VBO(engine, attributesStream, lengthStream)
        return VAOHybrid(vbo, vboStream, renderType)
    }

    override fun createShader(shader: CompiledShader,
                              information: ShaderCompileInformation): Shader {
        return ShaderGL(shader, information)
    }

    override fun checkError(message: String) {
        val error = GL11.glGetError()
        if (error != GL11.GL_NO_ERROR) {
            val errorName: String
            when (error) {
                GL11.GL_INVALID_ENUM -> errorName = "Enum argument out of range"
                GL11.GL_INVALID_VALUE -> errorName = "Numeric argument out of range"
                GL11.GL_INVALID_OPERATION -> errorName = "Operation illegal in current state"
                GL11.GL_STACK_OVERFLOW -> errorName = "Command would cause a stack overflow"
                GL11.GL_STACK_UNDERFLOW -> errorName = "Command would cause a stack underflow"
                GL11.GL_OUT_OF_MEMORY -> errorName = "Not enough memory left to execute command"
                GL30.GL_INVALID_FRAMEBUFFER_OPERATION -> errorName = "Framebuffer object is not complete"
                ARBImaging.GL_TABLE_TOO_LARGE -> errorName = "The specified table is too large"
                else -> errorName = "Unknown error code"
            }
            throw GraphicsException(errorName + " in " + message)
        }
    }

    override fun clear(r: Float,
                       g: Float,
                       b: Float,
                       a: Float) {
        GL11.glClearColor(r, g, b, a)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }

    override fun clearDepth() {
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT)
    }

    override fun disableCulling() {
        GL11.glDisable(GL11.GL_CULL_FACE)
    }

    override fun disableDepthTest() {
        GL11.glDisable(GL11.GL_DEPTH_TEST)
    }

    override fun disableDepthMask() {
        GL11.glDepthMask(false)
    }

    override fun disableWireframe() {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)
    }

    override fun disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }

    override fun enableCulling() {
        GL11.glEnable(GL11.GL_CULL_FACE)
    }

    override fun enableDepthTest() {
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthFunc(GL11.GL_LEQUAL)
    }

    override fun enableDepthMask() {
        GL11.glDepthMask(true)
    }

    override fun enableWireframe() {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
    }

    override fun enableScissor(x: Int,
                               y: Int,
                               width: Int,
                               height: Int) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        val h = engine.container.contentHeight() / 540.0
        GL11.glScissor((x * h).toInt(),
                ((540.0 - y.toDouble() - height.toDouble()) * h).toInt(),
                (width * h).toInt(), (height * h).toInt())
    }

    override fun setBlending(mode: BlendingMode) {
        when (mode) {
            BlendingMode.NONE -> GL11.glDisable(GL11.GL_BLEND)
            BlendingMode.NORMAL -> {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA,
                        GL11.GL_ONE_MINUS_SRC_ALPHA)
            }
            BlendingMode.ADD -> {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA)
            }
            BlendingMode.INVERT -> {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR,
                        GL11.GL_ONE_MINUS_SRC_COLOR)
            }
        }
    }

    override fun viewport(x: Int,
                          y: Int,
                          width: Int,
                          height: Int) {
        GL11.glViewport(x, y, width, height)
    }

    override fun screenShot(x: Int,
                            y: Int,
                            width: Int,
                            height: Int): Image {
        GL11.glReadBuffer(GL11.GL_FRONT)
        val buffer = container.allocate(width * height shl 2)
        GL11.glReadPixels(x, y, width, height, GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE, buffer)
        return Image(width, height, buffer)
    }

    override fun screenShotFBO(fbo: Framebuffer): Image {
        val buffer = container.allocate(fbo.width() * fbo.height() shl 2)
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE, buffer)
        return Image(fbo.width(), fbo.height(), buffer)
    }

    override fun setAttribute1f(id: Int,
                                v0: Float) {
        GL20.glVertexAttrib1f(id, v0)
    }

    override fun setAttribute2f(id: Int,
                                v0: Float,
                                v1: Float) {
        GL20.glVertexAttrib2f(id, v0, v1)
    }

    override fun setAttribute3f(id: Int,
                                v0: Float,
                                v1: Float,
                                v2: Float) {
        GL20.glVertexAttrib3f(id, v0, v1, v2)
    }

    override fun setAttribute4f(id: Int,
                                v0: Float,
                                v1: Float,
                                v2: Float,
                                v3: Float) {
        GL20.glVertexAttrib4f(id, v0, v1, v2, v3)
    }

    override fun setAttribute2f(uniform: Int,
                                values: FloatBuffer) {
        GL20.glVertexAttrib2fv(uniform, values)
    }

    override fun setAttribute3f(uniform: Int,
                                values: FloatBuffer) {
        GL20.glVertexAttrib3fv(uniform, values)
    }

    override fun setAttribute4f(uniform: Int,
                                values: FloatBuffer) {
        GL20.glVertexAttrib4fv(uniform, values)
    }

    override fun replaceTexture(x: Int,
                                y: Int,
                                width: Int,
                                height: Int,
                                buffer: ByteBuffer) {
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, x, y, width, height,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer)
    }

    override fun replaceTextureMipMap(x: Int,
                                      y: Int,
                                      width: Int,
                                      height: Int,
                                      vararg buffers: ByteBuffer?) {
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, x, y, width, height,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffers[0])
        for (i in 1..buffers.size - 1) {
            val scale = pow(2f, i.toFloat()).toInt()
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, i, x / scale, y / scale,
                    max(width / scale, 1),
                    max(height / scale, 1), GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE, buffers[i])
        }
    }

    override fun activeTexture(i: Int) {
        if (i < 0 || i > 31) {
            throw IllegalArgumentException(
                    "Active Texture must be 0-31, was " + i)
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + i)
    }
}
