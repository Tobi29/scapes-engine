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

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack
import org.tobi29.scapes.engine.backends.lwjgl3.push
import org.tobi29.scapes.engine.graphics.FramebufferStatus
import org.tobi29.scapes.engine.graphics.RenderType
import org.tobi29.scapes.engine.utils.IOException
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.shader.CompiledShader
import org.tobi29.scapes.engine.utils.shader.Expression
import org.tobi29.scapes.engine.utils.shader.ShaderException
import org.tobi29.scapes.engine.utils.shader.Uniform
import org.tobi29.scapes.engine.utils.shader.backend.glsl.GLSLGenerator

internal object GLUtils : KLogging() {
    fun renderType(renderType: RenderType): Int {
        when (renderType) {
            RenderType.TRIANGLES -> return GL11.GL_TRIANGLES
            RenderType.LINES -> return GL11.GL_LINES
            else -> throw IllegalArgumentException(
                    "Unknown render type: " + renderType)
        }
    }

    fun status(): FramebufferStatus {
        val status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)
        when (status) {
            GL30.GL_FRAMEBUFFER_COMPLETE -> return FramebufferStatus.COMPLETE
            GL30.GL_FRAMEBUFFER_UNSUPPORTED -> return FramebufferStatus.UNSUPPORTED
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
            for (i in 0..attachments - 1) {
                attachBuffer.put(GL30.GL_COLOR_ATTACHMENT0 + i)
            }
            attachBuffer.rewind()
            GL20.glDrawBuffers(attachBuffer)
        }
    }

    fun printLogShader(id: Int) {
        val stack = MemoryStack.stackGet()
        val length = GL20.glGetShaderi(id, GL20.GL_INFO_LOG_LENGTH)
        if (length > 1) {
            stack.push {
                val lengthBuffer = stack.mallocInt(1)
                lengthBuffer.put(0, length)
                val buffer = stack.malloc(length)
                GL20.glGetShaderInfoLog(id, lengthBuffer, buffer)
                val infoBytes = ByteArray(length)
                buffer.get(infoBytes)
                val out = String(infoBytes)
                logger.info { "Shader log: $out" }
            }
        }
    }

    fun printLogProgram(id: Int) {
        val stack = MemoryStack.stackGet()
        val length = GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH)
        if (length > 1) {
            stack.push {
                val lengthBuffer = stack.mallocInt(1)
                lengthBuffer.put(0, length)
                val buffer = stack.malloc(length)
                GL20.glGetProgramInfoLog(id, lengthBuffer, buffer)
                val infoBytes = ByteArray(length)
                buffer.get(infoBytes)
                val out = String(infoBytes)
                logger.info { "Program log: $out" }
            }
        }
    }

    fun compileShader(shader: CompiledShader,
                      properties: Map<String, Expression>) =
            try {
                GLSLGenerator.generate(GLSLGenerator.Version.GL_330, shader,
                        properties)
            } catch (e: ShaderException) {
                throw IOException(e)
            }

    fun createProgram(vertexSource: String,
                      fragmentSource: String,
                      uniforms: Array<Uniform?>): Pair<Int, IntArray> {
        val vertex = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(vertex, vertexSource)
        GL20.glCompileShader(vertex)
        printLogShader(vertex)
        val fragment = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        GL20.glShaderSource(fragment, fragmentSource)
        GL20.glCompileShader(fragment)
        printLogShader(fragment)
        val program = GL20.glCreateProgram()
        GL20.glAttachShader(program, vertex)
        GL20.glAttachShader(program, fragment)
        GL20.glLinkProgram(program)
        if (GL20.glGetProgrami(program,
                GL20.GL_LINK_STATUS) != GL11.GL_TRUE) {
            logger.error { "Failed to link status bar!" }
            printLogProgram(program)
        }
        val uniformLocations = IntArray(uniforms.size)
        for (i in uniforms.indices) {
            val uniform = uniforms[i]
            if (uniform == null) {
                uniformLocations[i] = -1
            } else {
                uniformLocations[i] = GL20.glGetUniformLocation(program,
                        uniform.identifier.name)
            }
        }
        GL20.glDetachShader(program, vertex)
        GL20.glDetachShader(program, fragment)
        GL20.glDeleteShader(vertex)
        GL20.glDeleteShader(fragment)
        return Pair(program, uniformLocations)
    }
}
