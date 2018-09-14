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

package org.tobi29.scapes.engine.backends.opengles

import org.tobi29.io.IOException
import org.tobi29.logging.KLogging
import org.tobi29.scapes.engine.graphics.FramebufferStatus
import org.tobi29.scapes.engine.graphics.RenderType
import org.tobi29.scapes.engine.shader.CompiledShader
import org.tobi29.scapes.engine.shader.Expression
import org.tobi29.scapes.engine.shader.ShaderException
import org.tobi29.scapes.engine.shader.Uniform
import org.tobi29.scapes.engine.shader.backend.glsl.GLSLGenerator

val RenderType.enum: Int
    get() = when (this) {
        RenderType.TRIANGLES -> GL_TRIANGLES
        RenderType.LINES -> GL_LINES
        else -> throw IllegalArgumentException("Unknown render type: $this")
    }

fun GLESHandle.status(): FramebufferStatus {
    val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
    return when (status) {
        GL_FRAMEBUFFER_COMPLETE -> FramebufferStatus.COMPLETE
        GL_FRAMEBUFFER_UNSUPPORTED -> FramebufferStatus.UNSUPPORTED
        else -> FramebufferStatus.UNKNOWN
    }
}

fun GLESHandle.drawbuffers(attachments: Int) {
    if (attachments < 0 || attachments > 15) {
        throw IllegalArgumentException(
            "Attachments must be 0-15, was $attachments"
        )
    }
    glDrawBuffers(IntArray(attachments) { GL_COLOR_ATTACHMENT(it) })
}

fun GLESHandle.printLogShader(id: GLShader) {
    val log = glGetShaderInfoLog(id)?.takeIf { it.isNotEmpty() } ?: return
    GLLogger.logger.info { "Shader log: $log" }
}

fun GLESHandle.printLogProgram(id: GLProgram) {
    val log = glGetProgramInfoLog(id)?.takeIf { it.isNotEmpty() } ?: return
    GLLogger.logger.info { "Program log: $log" }
}

fun GLESHandle.compileShader(
    shader: CompiledShader,
    properties: Map<String, Expression>
) = try {
    GLSLGenerator.generate(
        GLSLGenerator.Version.GLES_300, shader,
        properties
    )
} catch (e: ShaderException) {
    throw IOException(e)
}

fun GLESHandle.createProgram(
    vertexSource: String,
    fragmentSource: String,
    uniforms: Array<Uniform?>
): Pair<GLProgram, GLUniformArray> {
    val vertex = glCreateShader(GL_VERTEX_SHADER)
    glShaderSource(vertex, vertexSource)
    glCompileShader(vertex)
    printLogShader(vertex)
    val fragment = glCreateShader(GL_FRAGMENT_SHADER)
    glShaderSource(fragment, fragmentSource)
    glCompileShader(fragment)
    printLogShader(fragment)
    val program = glCreateProgram()
    glAttachShader(program, vertex)
    glAttachShader(program, fragment)
    glLinkProgram(program)
    if (!glGetProgramb(program, GL_LINK_STATUS)) {
        GLLogger.logger.error { "Failed to link status bar!" }
        printLogProgram(program)
    }
    val uniformLocations = glUniformArray(uniforms.size) { i ->
        val uniform = uniforms[i]
        if (uniform == null) {
            GLUniform_EMPTY
        } else {
            glGetUniformLocation(
                program, uniform.identifier.name
            )
        }
    }
    glDetachShader(program, vertex)
    glDetachShader(program, fragment)
    glDeleteShader(vertex)
    glDeleteShader(fragment)
    return Pair(program, uniformLocations)
}

internal object GLLogger : KLogging()
