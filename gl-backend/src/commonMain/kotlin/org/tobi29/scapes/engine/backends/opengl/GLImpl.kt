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

package org.tobi29.scapes.engine.backends.opengl

import net.gitout.ktbindings.gl.*
import org.tobi29.arrays.BytesRO
import org.tobi29.graphics.Bitmap
import org.tobi29.graphics.Ints2BytesBitmap
import org.tobi29.graphics.RGBA
import org.tobi29.graphics.flipVertical
import org.tobi29.logging.KLogger
import org.tobi29.scapes.engine.allocateMemoryBuffer
import org.tobi29.scapes.engine.graphics.*
import org.tobi29.scapes.engine.shader.CompiledShader
import org.tobi29.scapes.engine.shader.Expression
import org.tobi29.stdex.assert
import org.tobi29.stdex.maskAll
import org.tobi29.stdex.printerrln

enum class GLDebugMode {
    OFF,
    VERBOSE
}

class GLImpl<out G : GL33>(
    private val glInit: () -> G,
    private val glDispose: (G) -> Unit,
    private val checkRenderCall: () -> Boolean,
    private val debugMode: GLDebugMode = GLDebugMode.OFF
) : GL() {
    override val vaoTracker = GraphicsObjectTracker<Model>()
    override val textureTracker = GraphicsObjectTracker<Texture>()
    override val fboTracker = GraphicsObjectTracker<Framebuffer>()
    override val shaderTracker = GraphicsObjectTracker<Shader>()
    private val currentFBO = CurrentFBO()
    private var debugCallback: GLDebugMessageCallback? = null
    private var _gl: G? = null
    internal val gl: G get() = _gl!!

    override fun init() {
        _gl = glInit()

        logger.info {
            "OpenGL: ${gl.glGetString(GL_VERSION)
            } (Vendor: ${gl.glGetString(GL_VENDOR)
            }, Renderer: ${gl.glGetString(GL_RENDERER)})"
        }

        (gl as? GL43)?.let { gl ->
            val flags = IntArray(1)
            gl.glGetIntegerv(GL_CONTEXT_FLAGS, flags)
            if (flags[0].maskAll(GL_CONTEXT_FLAG_DEBUG_BIT)) {
                when (debugMode) {
                    GLDebugMode.OFF -> {
                    }
                    GLDebugMode.VERBOSE -> {
                        val debugCallback =
                            GLDebugMessageCallback { source, type, id,
                                                     severity, message, _ ->
                                printerrln("$source $type $severity: $message")
                            }
                        this.debugCallback = debugCallback
                        gl.glEnable(GL_DEBUG_OUTPUT)
                        gl.glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS)
                        gl.glDebugMessageCallback(debugCallback, 0L)
                        gl.glDebugMessageControl(
                            GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, null, true
                        )
                    }
                }
            }
        }
    }

    override fun dispose() {
        vaoTracker.disposeAll(this)
        textureTracker.disposeAll(this)
        fboTracker.disposeAll(this)
        shaderTracker.disposeAll(this)
        (gl as? GL43)?.let { gl ->
            val flags = IntArray(1)
            gl.glGetIntegerv(GL_CONTEXT_FLAGS, flags)
            if (flags[0].maskAll(GL_CONTEXT_FLAG_DEBUG_BIT)) {
                when (debugMode) {
                    GLDebugMode.OFF -> {
                    }
                    GLDebugMode.VERBOSE -> {
                        gl.glDisable(GL_DEBUG_OUTPUT)
                        gl.glDisable(GL_DEBUG_OUTPUT_SYNCHRONOUS)
                    }
                }
            }
        }
        debugCallback?.close()
        debugCallback = null
        glDispose(gl)
        _gl = null
    }

    override fun checkError(message: String) {
        val error = gl.glGetError()
        if (error != GL_NO_ERROR) {
            val errorName = when (error) {
                GL_INVALID_ENUM -> "Enum argument out of range"
                GL_INVALID_VALUE -> "Numeric argument out of range"
                GL_INVALID_OPERATION -> "Operation illegal in current state"
                GL_OUT_OF_MEMORY -> "Not enough memory left to execute command"
                GL_INVALID_FRAMEBUFFER_OPERATION -> "Framebuffer object is not complete"
                else -> "Unknown error code"
            }
            throw GraphicsException("$errorName in $message")
        }
    }

    override fun isRenderCall(): Boolean = checkRenderCall()

    override fun createTexture(
        width: Int, height: Int,
        buffer: BytesRO,
        mipmaps: Int,
        minFilter: TextureFilter, magFilter: TextureFilter,
        wrapS: TextureWrap, wrapT: TextureWrap
    ): Texture = TextureGL(
        this, width, height, buffer, mipmaps, minFilter, magFilter, wrapS, wrapT
    )

    override fun createFramebuffer(
        width: Int, height: Int,
        colorAttachments: Int, depth: Boolean,
        hdr: Boolean, alpha: Boolean,
        minFilter: TextureFilter, magFilter: TextureFilter
    ): Framebuffer = FBO(
        this, currentFBO, width, height, colorAttachments, depth, hdr, alpha,
        minFilter, magFilter
    )

    override fun createModelFast(
        attributes: List<ModelAttribute>, length: Int,
        renderType: RenderType
    ): Model {
        val vbo = VBO(this, attributes, length)
        return VAOFast(vbo, length, renderType)
    }

    override fun createModelStatic(
        attributes: List<ModelAttribute>, length: Int,
        index: IntArray, indexLength: Int,
        renderType: RenderType
    ): Model {
        val vbo = VBO(this, attributes, length)
        return VAOStatic(vbo, index, indexLength, renderType)
    }

    override fun createModelHybrid(
        attributes: List<ModelAttribute>, length: Int,
        attributesStream: List<ModelAttribute>, lengthStream: Int,
        renderType: RenderType
    ): ModelHybrid {
        val vbo = VBO(this, attributes, length)
        val vboStream = VBO(this, attributesStream, lengthStream)
        return VAOHybrid(vbo, vboStream, renderType)
    }

    override fun createShader(
        shader: CompiledShader, properties: Map<String, Expression>
    ): Shader = ShaderGL(this, shader, properties)

    override fun clear(r: Float, g: Float, b: Float, a: Float) {
        gl.glClearColor(r, g, b, a)
        gl.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }

    override fun clearDepth() {
        gl.glClear(GL_DEPTH_BUFFER_BIT)
    }

    override fun disableCulling() {
        gl.glDisable(GL_CULL_FACE)
    }

    override fun disableDepthTest() {
        gl.glDisable(GL_DEPTH_TEST)
    }

    override fun disableDepthMask() {
        gl.glDepthMask(false)
    }

    override fun disableWireframe() {
        // Not supported
    }

    override fun disableScissor() {
        gl.glDisable(GL_SCISSOR_TEST)
    }

    override fun enableCulling() {
        gl.glEnable(GL_CULL_FACE)
    }

    override fun enableDepthTest() {
        gl.glEnable(GL_DEPTH_TEST)
        gl.glDepthFunc(GL_LEQUAL)
    }

    override fun enableDepthMask() {
        gl.glDepthMask(true)
    }

    override fun enableWireframe() {
        // Not supported
    }

    override fun enableScissor(
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) {
        gl.glEnable(GL_SCISSOR_TEST)
        gl.glScissor(x, contentHeight - y - height, width, height)
    }

    override fun setBlending(mode: BlendingMode) {
        when (mode) {
            BlendingMode.NONE -> gl.glDisable(GL_BLEND)
            BlendingMode.NORMAL -> {
                gl.glEnable(GL_BLEND)
                gl.glBlendFunc(
                    GL_SRC_ALPHA,
                    GL_ONE_MINUS_SRC_ALPHA
                )
            }
            BlendingMode.ADD -> {
                gl.glEnable(GL_BLEND)
                gl.glBlendFunc(
                    GL_SRC_ALPHA,
                    GL_DST_ALPHA
                )
            }
            BlendingMode.INVERT -> {
                gl.glEnable(GL_BLEND)
                gl.glBlendFunc(
                    GL_ONE_MINUS_DST_COLOR,
                    GL_ONE_MINUS_SRC_COLOR
                )
            }
        }
    }

    override fun setViewport(
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) {
        gl.glViewport(x, y, width, height)
    }

    override fun getViewport(output: IntArray) {
        gl.glGetIntegerv(GL_VIEWPORT, output)
    }

    override fun getFrontBuffer(
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): Bitmap<*, *> {
        gl.glReadBuffer(GL_FRONT)
        return readPixels(x, y, width, height)
    }

    override fun getFBOColorBuffer(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        attachment: Int
    ): Bitmap<*, *> {
        gl.glReadBuffer(GL_COLOR_ATTACHMENT(attachment))
        return readPixels(x, y, width, height)
    }

    override fun getFBODepthBuffer(
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): Bitmap<*, *> {
        gl.glReadBuffer(GL_DEPTH_ATTACHMENT)
        return readPixels(x, y, width, height)
    }

    private fun readPixels(
        x: Int, y: Int, width: Int, height: Int
    ): Bitmap<*, *> {
        val buffer = allocateMemoryBuffer(width * height shl 2)
        gl.glReadPixels(
            x, y, width, height,
            GL_RGBA,
            GL_UNSIGNED_BYTE, buffer.asDataBuffer()
        )
        return Ints2BytesBitmap(buffer, width, height, RGBA).apply {
            flipVertical()
        }
    }

    override fun setAttribute1f(
        id: Int,
        v0: Float
    ) {
        gl.glVertexAttrib1f(id.toUInt(), v0)
    }

    override fun setAttribute2f(
        id: Int,
        v0: Float,
        v1: Float
    ) {
        gl.glVertexAttrib2f(id.toUInt(), v0, v1)
    }

    override fun setAttribute3f(
        id: Int,
        v0: Float,
        v1: Float,
        v2: Float
    ) {
        gl.glVertexAttrib3f(id.toUInt(), v0, v1, v2)
    }

    override fun setAttribute4f(
        id: Int,
        v0: Float,
        v1: Float,
        v2: Float,
        v3: Float
    ) {
        gl.glVertexAttrib4f(id.toUInt(), v0, v1, v2, v3)
    }

    override fun setAttribute1f(
        uniform: Int,
        values: FloatArray
    ) {
        gl.glVertexAttrib1fv(uniform.toUInt(), values)
    }

    override fun setAttribute2f(
        uniform: Int,
        values: FloatArray
    ) {
        gl.glVertexAttrib2fv(uniform.toUInt(), values)
    }

    override fun setAttribute3f(
        uniform: Int,
        values: FloatArray
    ) {
        gl.glVertexAttrib3fv(uniform.toUInt(), values)
    }

    override fun setAttribute4f(
        uniform: Int,
        values: FloatArray
    ) {
        gl.glVertexAttrib4fv(uniform.toUInt(), values)
    }

    override fun replaceTexture(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        buffer: BytesRO
    ) {
        gl.glTexSubImage2D(
            GL_TEXTURE_2D, 0, x, y, width, height,
            GL_RGBA,
            GL_UNSIGNED_BYTE, buffer.asDataBuffer()
        )
    }

    override fun replaceTextureMipMap(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        vararg buffers: BytesRO
    ) {
        gl.glTexSubImage2D(
            GL_TEXTURE_2D, 0, x, y, width, height,
            GL_RGBA,
            GL_UNSIGNED_BYTE, buffers[0].asDataBuffer()
        )
        for (i in 1 until buffers.size) {
            val scale = 1 shl i
            gl.glTexSubImage2D(
                GL_TEXTURE_2D, i, x / scale, y / scale,
                (width / scale).coerceAtLeast(1),
                (height / scale).coerceAtLeast(1),
                GL_RGBA,
                GL_UNSIGNED_BYTE, buffers[i].asDataBuffer()
            )
        }
    }

    override fun activeTexture(i: Int) {
        gl.glActiveTexture(GL_TEXTURE(i))
    }

    private companion object {
        private val logger = KLogger<GLImpl<*>>()
    }
}

private const val FBO_STACK_SIZE = 64

class CurrentFBO {
    private val stack = Array(FBO_STACK_SIZE) { emptyGLFramebuffer }
    private var index = 0

    fun push(fbo: GLFramebuffer) {
        if (index >= stack.size - 1)
            throw IllegalStateException("Framebuffer stack overflowed")
        stack[++index] = fbo
    }

    fun pop(fbo: GLFramebuffer): GLFramebuffer {
        if (index <= 0)
            throw IllegalStateException("Framebuffer stack underflowed")
        assert { stack[index] == fbo }
        return stack[--index]
    }
}
