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
import org.tobi29.scapes.engine.Container
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.*
import org.tobi29.scapes.engine.utils.graphics.Image
import org.tobi29.scapes.engine.utils.graphics.flipVertical
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.math.pow
import org.tobi29.scapes.engine.utils.shader.CompiledShader
import java.nio.ByteBuffer
import java.nio.FloatBuffer

class GLLWJGL3GLES(engine: ScapesEngine,
                   container: Container) : GL(engine,
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
                                   alpha: Boolean,
                                   minFilter: TextureFilter,
                                   magFilter: TextureFilter): Framebuffer {
        return FBO(engine, width, height, colorAttachments, depth, hdr,
                alpha, minFilter, magFilter)
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
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            val errorName: String
            when (error) {
                GLES20.GL_INVALID_ENUM -> errorName = "Enum argument out of range"
                GLES20.GL_INVALID_VALUE -> errorName = "Numeric argument out of range"
                GLES20.GL_INVALID_OPERATION -> errorName = "Operation illegal in current state"
                GLES20.GL_OUT_OF_MEMORY -> errorName = "Not enough memory left to execute command"
                GLES20.GL_INVALID_FRAMEBUFFER_OPERATION -> errorName = "Framebuffer object is not complete"
                else -> errorName = "Unknown error code"
            }
            throw GraphicsException(errorName + " in " + message)
        }
    }

    override fun clear(r: Float,
                       g: Float,
                       b: Float,
                       a: Float) {
        GLES20.glClearColor(r, g, b, a)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    override fun clearDepth() {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)
    }

    override fun disableCulling() {
        GLES20.glDisable(GLES20.GL_CULL_FACE)
    }

    override fun disableDepthTest() {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
    }

    override fun disableDepthMask() {
        GLES20.glDepthMask(false)
    }

    override fun disableWireframe() {
        //GLES20.glPolygonMode(GLES20.GL_FRONT_AND_BACK, GLES20.GL_FILL)
    }

    override fun disableScissor() {
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST)
    }

    override fun enableCulling() {
        GLES20.glEnable(GLES20.GL_CULL_FACE)
    }

    override fun enableDepthTest() {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
    }

    override fun enableDepthMask() {
        GLES20.glDepthMask(true)
    }

    override fun enableWireframe() {
        //GLES20.glPolygonMode(GLES20.GL_FRONT_AND_BACK, GLES20.GL_LINE)
    }

    override fun enableScissor(x: Int,
                               y: Int,
                               width: Int,
                               height: Int) {
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST)
        val h = engine.container.contentHeight()
        GLES20.glScissor(x, h - y - height, width, height)
    }

    override fun setBlending(mode: BlendingMode) {
        when (mode) {
            BlendingMode.NONE -> GLES20.glDisable(GLES20.GL_BLEND)
            BlendingMode.NORMAL -> {
                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,
                        GLES20.GL_ONE_MINUS_SRC_ALPHA)
            }
            BlendingMode.ADD -> {
                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA)
            }
            BlendingMode.INVERT -> {
                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glBlendFunc(GLES20.GL_ONE_MINUS_DST_COLOR,
                        GLES20.GL_ONE_MINUS_SRC_COLOR)
            }
        }
    }

    override fun setViewport(x: Int,
                             y: Int,
                             width: Int,
                             height: Int) {
        GLES20.glViewport(x, y, width, height)
    }

    override fun getViewport(output: IntArray) {
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, output)
    }

    override fun screenShot(x: Int,
                            y: Int,
                            width: Int,
                            height: Int): Image {
        val buffer = container.allocate(width * height shl 2)
        GLES30.glReadBuffer(GLES20.GL_FRONT)
        GLES20.glReadPixels(x, y, width, height, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, buffer)
        flipVertical(width, height, buffer)
        return Image(width, height, buffer)
    }

    override fun screenShotFBO(fbo: Framebuffer): Image {
        val buffer = container.allocate(fbo.width() * fbo.height() shl 2)
        //GLES30.glGetTexImage(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
        //        GLES20.GL_UNSIGNED_BYTE, buffer)
        flipVertical(fbo.width(), fbo.height(), buffer)
        return Image(fbo.width(), fbo.height(), buffer)
    }

    override fun setAttribute1f(id: Int,
                                v0: Float) {
        GLES20.glVertexAttrib1f(id, v0)
    }

    override fun setAttribute2f(id: Int,
                                v0: Float,
                                v1: Float) {
        GLES20.glVertexAttrib2f(id, v0, v1)
    }

    override fun setAttribute3f(id: Int,
                                v0: Float,
                                v1: Float,
                                v2: Float) {
        GLES20.glVertexAttrib3f(id, v0, v1, v2)
    }

    override fun setAttribute4f(id: Int,
                                v0: Float,
                                v1: Float,
                                v2: Float,
                                v3: Float) {
        GLES20.glVertexAttrib4f(id, v0, v1, v2, v3)
    }

    override fun setAttribute2f(uniform: Int,
                                values: FloatBuffer) {
        GLES20.glVertexAttrib2fv(uniform, values)
    }

    override fun setAttribute3f(uniform: Int,
                                values: FloatBuffer) {
        GLES20.glVertexAttrib3fv(uniform, values)
    }

    override fun setAttribute4f(uniform: Int,
                                values: FloatBuffer) {
        GLES20.glVertexAttrib4fv(uniform, values)
    }

    override fun replaceTexture(x: Int,
                                y: Int,
                                width: Int,
                                height: Int,
                                buffer: ByteBuffer) {
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, x, y, width, height,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)
    }

    override fun replaceTextureMipMap(x: Int,
                                      y: Int,
                                      width: Int,
                                      height: Int,
                                      vararg buffers: ByteBuffer?) {
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, x, y, width, height,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffers[0])
        for (i in 1..buffers.size - 1) {
            val scale = pow(2f, i.toFloat()).toInt()
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, i, x / scale,
                    y / scale,
                    max(width / scale, 1),
                    max(height / scale, 1), GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, buffers[i])
        }
    }

    override fun activeTexture(i: Int) {
        if (i < 0 || i > 31) {
            throw IllegalArgumentException(
                    "Active Texture must be 0-31, was " + i)
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i)
    }
}
