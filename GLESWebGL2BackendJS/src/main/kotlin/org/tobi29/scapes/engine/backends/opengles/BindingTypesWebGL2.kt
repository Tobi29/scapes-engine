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

import org.khronos.webgl2.WebGL2RenderingContext
import org.tobi29.scapes.engine.Container

actual class GLESHandle(
    val wgl: WebGL2RenderingContext,
    container: Container
) : GOSGLES(container) {
    override val glh: GLESHandle get() = this
}

actual typealias GLTexture = WebGLTexture

@Suppress("UnsafeCastFromDynamic")
actual inline val GLTexture_EMPTY: GLTexture
    get() = js("null")

actual typealias GLShader = WebGLShader

@Suppress("UnsafeCastFromDynamic")
actual inline val GLShader_EMPTY: GLShader
    get() = js("null")

actual typealias GLProgram = WebGLProgram

@Suppress("UnsafeCastFromDynamic")
actual inline val GLProgram_EMPTY: GLProgram
    get() = js("null")

actual typealias GLFBO = WebGLFramebuffer

actual class GLFBOArray(private val array: Array<GLFBO>) {
    actual constructor(
        size: Int,
        init: (Int) -> GLFBO
    ) : this(Array(size, init))

    actual val size: Int get() = array.size
    actual operator fun get(index: Int): GLFBO = array[index]
    actual operator fun set(index: Int, value: GLFBO) =
        array.set(index, value)
}

actual inline fun glFBOArray(
    size: Int,
    init: (Int) -> GLFBO
): GLFBOArray = GLFBOArray(Array(size) { init(it) })

@Suppress("UnsafeCastFromDynamic")
actual inline val GLFBO_EMPTY: GLFBO
    get() = js("null")

actual typealias GLVAO = WebGLVertexArrayObject

@Suppress("UnsafeCastFromDynamic")
actual inline val GLVAO_EMPTY: GLVAO
    get() = js("null")

actual typealias GLVBO = WebGLBuffer

@Suppress("UnsafeCastFromDynamic")
actual inline val GLVBO_EMPTY: GLVBO
    get() = js("null")

actual typealias GLUniform = WebGLUniformLocation

@Suppress("UnsafeCastFromDynamic")
actual inline val GLUniform_EMPTY: GLUniform
    get() = js("null")

actual class GLUniformArray(private val array: Array<GLUniform>) {
    actual constructor(
        size: Int,
        init: (Int) -> GLUniform
    ) : this(Array(size, init))

    actual val size: Int get() = array.size
    actual operator fun get(index: Int): GLUniform = array[index]
    actual operator fun set(index: Int, value: GLUniform) =
        array.set(index, value)
}

actual inline fun glUniformArray(
    size: Int,
    init: (Int) -> GLUniform
): GLUniformArray = GLUniformArray(Array(size) { init(it) })

abstract external class WebGLObject
external class WebGLBuffer : WebGLObject
external class WebGLFramebuffer : WebGLObject
external class WebGLProgram : WebGLObject
external class WebGLShader : WebGLObject
external class WebGLTexture : WebGLObject
external class WebGLVertexArrayObject : WebGLObject
external class WebGLUniformLocation

inline val WebGLBuffer.c: org.khronos.webgl.WebGLBuffer
    @Suppress("UnsafeCastFromDynamic") get() = asDynamic()
inline val org.khronos.webgl.WebGLBuffer?.c: WebGLBuffer
    @Suppress("UnsafeCastFromDynamic") get() = asDynamic()

inline val WebGLFramebuffer.c: org.khronos.webgl.WebGLFramebuffer
    @Suppress("UnsafeCastFromDynamic") get() = asDynamic()
inline val org.khronos.webgl.WebGLFramebuffer?.c: WebGLFramebuffer
    @Suppress("UnsafeCastFromDynamic") get() = asDynamic()

inline val WebGLProgram.c: org.khronos.webgl.WebGLProgram
    @Suppress("UnsafeCastFromDynamic") get() = asDynamic()
inline val org.khronos.webgl.WebGLProgram?.c: WebGLProgram
    @Suppress("UnsafeCastFromDynamic") get() = asDynamic()

inline val WebGLShader.c: org.khronos.webgl.WebGLShader
    @Suppress("UnsafeCastFromDynamic") get() = asDynamic()
inline val org.khronos.webgl.WebGLShader?.c: WebGLShader
    @Suppress("UnsafeCastFromDynamic") get() = asDynamic()

inline val WebGLTexture.c: org.khronos.webgl.WebGLTexture
    @Suppress("UnsafeCastFromDynamic") get() = asDynamic()
inline val org.khronos.webgl.WebGLTexture?.c: WebGLTexture
    @Suppress("UnsafeCastFromDynamic") get() = asDynamic()

inline val WebGLVertexArrayObject.c: org.khronos.webgl2.WebGLVertexArrayObject
    @Suppress("UnsafeCastFromDynamic") get() = asDynamic()
inline val org.khronos.webgl2.WebGLVertexArrayObject?.c: WebGLVertexArrayObject
    @Suppress("UnsafeCastFromDynamic") get() = asDynamic()

inline val WebGLUniformLocation.c: org.khronos.webgl.WebGLUniformLocation
    @Suppress("UnsafeCastFromDynamic") get() = asDynamic()
inline val org.khronos.webgl.WebGLUniformLocation?.c: WebGLUniformLocation
    @Suppress("UnsafeCastFromDynamic") get() = asDynamic()
