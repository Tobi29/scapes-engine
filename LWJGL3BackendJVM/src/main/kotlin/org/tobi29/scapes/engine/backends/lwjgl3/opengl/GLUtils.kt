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
import org.tobi29.io.IOException
import org.tobi29.io._rewind
import org.tobi29.logging.KLogging
import org.tobi29.scapes.engine.backends.lwjgl3.stackFrame
import org.tobi29.scapes.engine.graphics.FramebufferStatus
import org.tobi29.scapes.engine.graphics.RenderType
import org.tobi29.scapes.engine.shader.CompiledShader
import org.tobi29.scapes.engine.shader.Expression
import org.tobi29.scapes.engine.shader.ShaderException
import org.tobi29.scapes.engine.shader.Uniform
import org.tobi29.scapes.engine.shader.backend.glsl.GLSLGenerator
import org.tobi29.stdex.utf8ToString

internal object GLUtils : KLogging() {
    fun renderType(renderType: RenderType): Int {
        return when (renderType) {
            RenderType.TRIANGLES -> GL11.GL_TRIANGLES
            RenderType.LINES -> GL11.GL_LINES
            else -> throw IllegalArgumentException(
                    "Unknown render type: " + renderType)
        }
    }

    fun status(): FramebufferStatus {
        val status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)
        return when (status) {
            GL30.GL_FRAMEBUFFER_COMPLETE -> FramebufferStatus.COMPLETE
            GL30.GL_FRAMEBUFFER_UNSUPPORTED -> FramebufferStatus.UNSUPPORTED
            else -> FramebufferStatus.UNKNOWN
        }
    }


    fun drawbuffers(attachments: Int) {
        if (attachments < 0 || attachments > 15) {
            throw IllegalArgumentException(
                    "Attachments must be 0-15, was " + attachments)
        }
        stackFrame { stack ->
            val attachBuffer = stack.mallocInt(attachments)
            for (i in 0 until attachments) {
                attachBuffer.put(GL30.GL_COLOR_ATTACHMENT0 + i)
            }
            attachBuffer._rewind()
            GL20.glDrawBuffers(attachBuffer)
        }
    }

    fun printLogShader(id: Int) {
        val length = GL20.glGetShaderi(id, GL20.GL_INFO_LOG_LENGTH)
        if (length > 1) {
            stackFrame { stack ->
                val lengthBuffer = stack.mallocInt(1)
                lengthBuffer.put(0, length)
                val buffer = stack.malloc(length)
                GL20.glGetShaderInfoLog(id, lengthBuffer, buffer)
                val infoBytes = ByteArray(length)
                buffer.get(infoBytes)
                val out = infoBytes.utf8ToString()
                logger.info { "Shader log: $out" }
            }
        }
    }

    fun printLogProgram(id: Int) {
        val length = GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH)
        if (length > 1) {
            stackFrame { stack ->
                val lengthBuffer = stack.mallocInt(1)
                lengthBuffer.put(0, length)
                val buffer = stack.malloc(length)
                GL20.glGetProgramInfoLog(id, lengthBuffer, buffer)
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
