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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.backends.opengles

import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl2.WebGL2RenderingContext

actual inline val GL_FRAMEBUFFER get() = WebGLRenderingContext.FRAMEBUFFER
actual inline val GL_COLOR_BUFFER_BIT get() = WebGLRenderingContext.COLOR_BUFFER_BIT
actual inline val GL_DEPTH_BUFFER_BIT get() = WebGLRenderingContext.DEPTH_BUFFER_BIT
actual inline val GL_TEXTURE_2D get() = WebGLRenderingContext.TEXTURE_2D
actual inline val GL_RGB get() = WebGLRenderingContext.RGB
actual inline val GL_RGBA get() = WebGLRenderingContext.RGBA
actual inline val GL_RGB16F get() = WebGL2RenderingContext.RGB16F
actual inline val GL_RGBA16F get() = WebGL2RenderingContext.RGBA16F
actual inline val GL_BYTE get() = WebGLRenderingContext.BYTE
actual inline val GL_SHORT get() = WebGLRenderingContext.SHORT
actual inline val GL_UNSIGNED_BYTE get() = WebGLRenderingContext.UNSIGNED_BYTE
actual inline val GL_UNSIGNED_SHORT get() = WebGLRenderingContext.UNSIGNED_SHORT
actual inline val GL_UNSIGNED_INT get() = WebGLRenderingContext.UNSIGNED_INT
actual inline val GL_FLOAT get() = WebGLRenderingContext.FLOAT
actual inline val GL_HALF_FLOAT get() = WebGL2RenderingContext.HALF_FLOAT
actual inline val GL_DEPTH_COMPONENT get() = WebGLRenderingContext.DEPTH_COMPONENT
actual inline val GL_DEPTH_COMPONENT24 get() = WebGL2RenderingContext.DEPTH_COMPONENT24

actual inline fun GL_COLOR_ATTACHMENT(i: Int): GLEnum {
    if (i < 0 || i > 15)
        throw IllegalArgumentException("Color attachment must be 0..15, was $i")
    return WebGLRenderingContext.COLOR_ATTACHMENT0 + i
}

actual inline fun GL_TEXTURE(i: Int): GLEnum {
    if (i < 0 || i > 31)
        throw IllegalArgumentException("Active texture must be 0..31, was $i")
    return WebGLRenderingContext.TEXTURE0 + i
}

actual inline val GL_DEPTH_ATTACHMENT get() = WebGLRenderingContext.DEPTH_ATTACHMENT
actual inline val GL_TEXTURE_MAG_FILTER get() = WebGLRenderingContext.TEXTURE_MAG_FILTER
actual inline val GL_TEXTURE_MIN_FILTER get() = WebGLRenderingContext.TEXTURE_MIN_FILTER
actual inline val GL_TEXTURE_WRAP_S get() = WebGLRenderingContext.TEXTURE_WRAP_S
actual inline val GL_TEXTURE_WRAP_T get() = WebGLRenderingContext.TEXTURE_WRAP_T
actual inline val GL_REPEAT get() = WebGLRenderingContext.REPEAT
actual inline val GL_NEAREST get() = WebGLRenderingContext.NEAREST
actual inline val GL_LINEAR get() = WebGLRenderingContext.LINEAR
actual inline val GL_CLAMP_TO_EDGE get() = WebGLRenderingContext.CLAMP_TO_EDGE
actual inline val GL_NEAREST_MIPMAP_LINEAR get() = WebGLRenderingContext.NEAREST_MIPMAP_LINEAR
actual inline val GL_LINEAR_MIPMAP_LINEAR get() = WebGLRenderingContext.LINEAR_MIPMAP_LINEAR
actual inline val GL_TEXTURE_MAX_LEVEL get() = WebGL2RenderingContext.TEXTURE_MAX_LEVEL
actual inline val GL_ARRAY_BUFFER get() = WebGLRenderingContext.ARRAY_BUFFER
actual inline val GL_ELEMENT_ARRAY_BUFFER get() = WebGLRenderingContext.ELEMENT_ARRAY_BUFFER
actual inline val GL_STATIC_DRAW get() = WebGLRenderingContext.STATIC_DRAW
actual inline val GL_STREAM_DRAW get() = WebGLRenderingContext.STREAM_DRAW
actual inline val GL_FRONT get() = WebGLRenderingContext.FRONT
actual inline val GL_FRONT_AND_BACK get() = WebGLRenderingContext.FRONT_AND_BACK
actual inline val GL_VIEWPORT get() = WebGLRenderingContext.VIEWPORT
actual inline val GL_BLEND get() = WebGLRenderingContext.BLEND
actual inline val GL_SRC_ALPHA get() = WebGLRenderingContext.SRC_ALPHA
actual inline val GL_ONE_MINUS_SRC_ALPHA get() = WebGLRenderingContext.ONE_MINUS_SRC_ALPHA
actual inline val GL_DST_ALPHA get() = WebGLRenderingContext.DST_ALPHA
actual inline val GL_ONE_MINUS_DST_COLOR get() = WebGLRenderingContext.ONE_MINUS_DST_COLOR
actual inline val GL_ONE_MINUS_SRC_COLOR get() = WebGLRenderingContext.ONE_MINUS_SRC_COLOR
actual inline val GL_SCISSOR_TEST get() = WebGLRenderingContext.SCISSOR_TEST
actual inline val GL_DEPTH_TEST get() = WebGLRenderingContext.DEPTH_TEST
actual inline val GL_CULL_FACE get() = WebGLRenderingContext.CULL_FACE
actual inline val GL_LEQUAL get() = WebGLRenderingContext.LEQUAL
actual inline val GL_NO_ERROR get() = WebGLRenderingContext.NO_ERROR
actual inline val GL_INVALID_ENUM get() = WebGLRenderingContext.INVALID_ENUM
actual inline val GL_INVALID_VALUE get() = WebGLRenderingContext.INVALID_VALUE
actual inline val GL_INVALID_OPERATION get() = WebGLRenderingContext.INVALID_OPERATION
actual inline val GL_OUT_OF_MEMORY get() = WebGLRenderingContext.OUT_OF_MEMORY
actual inline val GL_INVALID_FRAMEBUFFER_OPERATION get() = WebGLRenderingContext.INVALID_FRAMEBUFFER_OPERATION
actual inline val GL_FRAMEBUFFER_COMPLETE get() = WebGLRenderingContext.FRAMEBUFFER_COMPLETE
actual inline val GL_FRAMEBUFFER_UNSUPPORTED get() = WebGLRenderingContext.FRAMEBUFFER_UNSUPPORTED
actual inline val GL_TRIANGLES get() = WebGLRenderingContext.TRIANGLES
actual inline val GL_LINES get() = WebGLRenderingContext.LINES
actual inline val GL_VERTEX_SHADER get() = WebGLRenderingContext.VERTEX_SHADER
actual inline val GL_FRAGMENT_SHADER get() = WebGLRenderingContext.FRAGMENT_SHADER
actual inline val GL_LINK_STATUS get() = WebGLRenderingContext.LINK_STATUS
