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

import org.tobi29.stdex.assert

expect class GLESHandle : GOSGLES
typealias GLEnum = Int

expect class GLTexture

expect val GLTexture_EMPTY: GLTexture

expect class GLShader

expect val GLShader_EMPTY: GLShader

expect class GLProgram

expect val GLProgram_EMPTY: GLProgram

expect class GLFBO

expect val GLFBO_EMPTY: GLFBO

expect class GLFBOArray(
    size: Int,
    init: (Int) -> GLFBO
) {
    val size: Int
    operator fun get(index: Int): GLFBO
    operator fun set(index: Int, value: GLFBO)
}

expect fun glFBOArray(size: Int, init: (Int) -> GLFBO): GLFBOArray

expect class GLVAO

expect val GLVAO_EMPTY: GLVAO

expect class GLVBO

expect val GLVBO_EMPTY: GLVBO

expect class GLUniform

expect val GLUniform_EMPTY: GLUniform

expect class GLUniformArray(
    size: Int,
    init: (Int) -> GLUniform
) {
    val size: Int
    operator fun get(index: Int): GLUniform
    operator fun set(index: Int, value: GLUniform)
}

expect fun glUniformArray(size: Int, init: (Int) -> GLUniform): GLUniformArray

private const val FBO_STACK_SIZE = 64

class CurrentFBO {
    private val stack = glFBOArray(FBO_STACK_SIZE) { GLFBO_EMPTY }
    private var index = 0

    fun push(fbo: GLFBO) {
        if (index >= stack.size - 1)
            throw IllegalStateException("Framebuffer stack overflowed")
        stack[++index] = fbo
    }

    fun pop(fbo: GLFBO): GLFBO {
        if (index <= 0)
            throw IllegalStateException("Framebuffer stack underflowed")
        assert { stack[index] == fbo }
        return stack[--index]
    }
}
