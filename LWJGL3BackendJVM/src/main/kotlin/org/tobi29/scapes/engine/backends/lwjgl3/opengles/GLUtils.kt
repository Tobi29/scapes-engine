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
import org.lwjgl.system.MemoryStack
import org.tobi29.scapes.engine.backends.lwjgl3.push
import org.tobi29.scapes.engine.graphics.FramebufferStatus
import org.tobi29.scapes.engine.graphics.RenderType
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.io._rewind
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.shader.CompiledShader
import org.tobi29.scapes.engine.utils.shader.Expression
import org.tobi29.scapes.engine.utils.shader.ShaderException
import org.tobi29.scapes.engine.utils.shader.Uniform
import org.tobi29.scapes.engine.utils.shader.backend.glsl.GLSLGenerator
import org.tobi29.scapes.engine.utils.utf8ToString

internal object GLUtils : KLogging() {
    fun renderType(renderType: RenderType): Int {
        when (renderType) {
            RenderType.TRIANGLES -> return GLES20.GL_TRIANGLES
            RenderType.LINES -> return GLES20.GL_LINES
            else -> throw IllegalArgumentException(
                    "Unknown render type: " + renderType)
        }
    }

    fun status(): FramebufferStatus {
        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        when (status) {
            GLES20.GL_FRAMEBUFFER_COMPLETE -> return FramebufferStatus.COMPLETE
            GLES20.GL_FRAMEBUFFER_UNSUPPORTED -> return FramebufferStatus.UNSUPPORTED
            else -> return FramebufferStatus.UNKNOWN
        }
    }

    fun drawbuffers(attachments: Int) {
        if (attachments < 0 || attachments > 15) {
            throw IllegalArgumentException(
                    "Attachments must be 0-15, was " + attachments)
        }
        val stack = MemoryStack.stackGet()
        stack.push {
            val attachBuffer = stack.mallocInt(attachments)
            for (i in 0 until attachments) {
                attachBuffer.put(GLES20.GL_COLOR_ATTACHMENT0 + i)
            }
            attachBuffer._rewind()
            GLES30.glDrawBuffers(attachBuffer)
        }
    }

    fun printLogShader(id: Int) {
        val stack = MemoryStack.stackGet()
        val length = GLES20.glGetShaderi(id, GLES20.GL_INFO_LOG_LENGTH)
        if (length > 1) {
            stack.push {
                val lengthBuffer = stack.mallocInt(1)
                lengthBuffer.put(0, length)
                val buffer = stack.malloc(length)
                GLES20.glGetShaderInfoLog(id, lengthBuffer, buffer)
                val infoBytes = ByteArray(length)
                buffer.get(infoBytes)
                val out = infoBytes.utf8ToString()
                logger.info { "Shader log: $out" }
            }
        }
    }

    fun printLogProgram(id: Int) {
        val stack = MemoryStack.stackGet()
        val length = GLES20.glGetProgrami(id, GLES20.GL_INFO_LOG_LENGTH)
        if (length > 1) {
            stack.push {
                val lengthBuffer = stack.mallocInt(1)
                lengthBuffer.put(0, length)
                val buffer = stack.malloc(length)
                GLES20.glGetProgramInfoLog(id, lengthBuffer, buffer)
                val infoBytes = ByteArray(length)
                buffer.get(infoBytes)
                val out = infoBytes.utf8ToString()
                logger.info { "Program log: $out" }
            }
        }
    }

    fun compileShader(shader: CompiledShader,
                      properties: Map<String, Expression>) =
            try {
                GLSLGenerator.generate(GLSLGenerator.Version.GLES_300, shader,
                        properties)
            } catch (e: ShaderException) {
                throw IOException(e)
            }

    fun createProgram(vertexSource: String,
                      fragmentSource: String,
                      uniforms: Array<Uniform?>): Pair<Int, IntArray> {
        val vertex = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertex, vertexSource)
        GLES20.glCompileShader(vertex)
        printLogShader(vertex)
        val fragment = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragment, fragmentSource)
        GLES20.glCompileShader(fragment)
        printLogShader(fragment)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertex)
        GLES20.glAttachShader(program, fragment)
        GLES20.glLinkProgram(program)
        if (GLES20.glGetProgrami(program,
                GLES20.GL_LINK_STATUS) != GLES20.GL_TRUE) {
            logger.error { "Failed to link status bar!" }
            printLogProgram(program)
        }
        val uniformLocations = IntArray(uniforms.size)
        for (i in uniforms.indices) {
            val uniform = uniforms[i]
            if (uniform == null) {
                uniformLocations[i] = -1
            } else {
                uniformLocations[i] = GLES20.glGetUniformLocation(program,
                        uniform.identifier.name)
            }
        }
        GLES20.glDetachShader(program, vertex)
        GLES20.glDetachShader(program, fragment)
        GLES20.glDeleteShader(vertex)
        GLES20.glDeleteShader(fragment)
        return Pair(program, uniformLocations)
    }
}
