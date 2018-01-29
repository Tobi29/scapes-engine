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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.backends.opengl

import org.lwjgl.opengl.*

actual inline val GL_FRAMEBUFFER get() = GL30.GL_FRAMEBUFFER
actual inline val GL_COLOR_BUFFER_BIT get() = GL11.GL_COLOR_BUFFER_BIT
actual inline val GL_DEPTH_BUFFER_BIT get() = GL11.GL_DEPTH_BUFFER_BIT
actual inline val GL_TEXTURE_2D get() = GL11.GL_TEXTURE_2D
actual inline val GL_RGB get() = GL11.GL_RGB
actual inline val GL_RGBA get() = GL11.GL_RGBA
actual inline val GL_RGB16F get() = GL30.GL_RGB16F
actual inline val GL_RGBA16F get() = GL30.GL_RGBA16F
actual inline val GL_BYTE get() = GL11.GL_BYTE
actual inline val GL_SHORT get() = GL11.GL_SHORT
actual inline val GL_UNSIGNED_BYTE get() = GL11.GL_UNSIGNED_BYTE
actual inline val GL_UNSIGNED_SHORT get() = GL11.GL_UNSIGNED_SHORT
actual inline val GL_UNSIGNED_INT get() = GL11.GL_UNSIGNED_INT
actual inline val GL_FLOAT get() = GL11.GL_FLOAT
actual inline val GL_HALF_FLOAT get() = GL30.GL_HALF_FLOAT
actual inline val GL_DEPTH_COMPONENT get() = GL11.GL_DEPTH_COMPONENT
actual inline val GL_DEPTH_COMPONENT24 get() = GL14.GL_DEPTH_COMPONENT24

actual inline fun GL_COLOR_ATTACHMENT(i: Int): GLEnum {
    if (i < 0 || i > 31)
        throw IllegalArgumentException("Color attachment must be 0..31, was $i")
    return GL30.GL_COLOR_ATTACHMENT0 + i
}

actual inline fun GL_TEXTURE(i: Int): GLEnum {
    if (i < 0 || i > 31)
        throw IllegalArgumentException("Active texture must be 0..31, was $i")
    return GL13.GL_TEXTURE0 + i
}

actual inline val GL_DEPTH_ATTACHMENT get() = GL30.GL_DEPTH_ATTACHMENT
actual inline val GL_TEXTURE_MAG_FILTER get() = GL11.GL_TEXTURE_MAG_FILTER
actual inline val GL_TEXTURE_MIN_FILTER get() = GL11.GL_TEXTURE_MIN_FILTER
actual inline val GL_TEXTURE_WRAP_S get() = GL11.GL_TEXTURE_WRAP_S
actual inline val GL_TEXTURE_WRAP_T get() = GL11.GL_TEXTURE_WRAP_T
actual inline val GL_REPEAT get() = GL11.GL_REPEAT
actual inline val GL_NEAREST get() = GL11.GL_NEAREST
actual inline val GL_LINEAR get() = GL11.GL_LINEAR
actual inline val GL_CLAMP_TO_EDGE get() = GL12.GL_CLAMP_TO_EDGE
actual inline val GL_NEAREST_MIPMAP_LINEAR get() = GL11.GL_NEAREST_MIPMAP_LINEAR
actual inline val GL_LINEAR_MIPMAP_LINEAR get() = GL11.GL_LINEAR_MIPMAP_LINEAR
actual inline val GL_TEXTURE_MAX_LEVEL get() = GL12.GL_TEXTURE_MAX_LEVEL
actual inline val GL_ARRAY_BUFFER get() = GL15.GL_ARRAY_BUFFER
actual inline val GL_ELEMENT_ARRAY_BUFFER get() = GL15.GL_ELEMENT_ARRAY_BUFFER
actual inline val GL_STATIC_DRAW get() = GL15.GL_STATIC_DRAW
actual inline val GL_STREAM_DRAW get() = GL15.GL_STREAM_DRAW
actual inline val GL_FRONT get() = GL11.GL_FRONT
actual inline val GL_FRONT_AND_BACK get() = GL11.GL_FRONT_AND_BACK
actual inline val GL_VIEWPORT get() = GL11.GL_VIEWPORT
actual inline val GL_BLEND get() = GL11.GL_BLEND
actual inline val GL_SRC_ALPHA get() = GL11.GL_SRC_ALPHA
actual inline val GL_ONE_MINUS_SRC_ALPHA get() = GL11.GL_ONE_MINUS_SRC_ALPHA
actual inline val GL_DST_ALPHA get() = GL11.GL_DST_ALPHA
actual inline val GL_ONE_MINUS_DST_COLOR get() = GL11.GL_ONE_MINUS_DST_COLOR
actual inline val GL_ONE_MINUS_SRC_COLOR get() = GL11.GL_ONE_MINUS_SRC_COLOR
actual inline val GL_LINE get() = GL11.GL_LINE
actual inline val GL_SCISSOR_TEST get() = GL11.GL_SCISSOR_TEST
actual inline val GL_DEPTH_TEST get() = GL11.GL_DEPTH_TEST
actual inline val GL_CULL_FACE get() = GL11.GL_CULL_FACE
actual inline val GL_LEQUAL get() = GL11.GL_LEQUAL
actual inline val GL_FILL get() = GL11.GL_FILL
actual inline val GL_NO_ERROR get() = GL11.GL_NO_ERROR
actual inline val GL_INVALID_ENUM get() = GL11.GL_INVALID_ENUM
actual inline val GL_INVALID_VALUE get() = GL11.GL_INVALID_VALUE
actual inline val GL_INVALID_OPERATION get() = GL11.GL_INVALID_OPERATION
actual inline val GL_STACK_OVERFLOW get() = GL11.GL_STACK_OVERFLOW
actual inline val GL_STACK_UNDERFLOW get() = GL11.GL_STACK_UNDERFLOW
actual inline val GL_OUT_OF_MEMORY get() = GL11.GL_OUT_OF_MEMORY
actual inline val GL_INVALID_FRAMEBUFFER_OPERATION get() = GL30.GL_INVALID_FRAMEBUFFER_OPERATION
actual inline val GL_TABLE_TOO_LARGE get() = ARBImaging.GL_TABLE_TOO_LARGE
actual inline val GL_FRAMEBUFFER_COMPLETE get() = GL30.GL_FRAMEBUFFER_COMPLETE
actual inline val GL_FRAMEBUFFER_UNSUPPORTED get() = GL30.GL_FRAMEBUFFER_UNSUPPORTED
actual inline val GL_TRIANGLES get() = GL11.GL_TRIANGLES
actual inline val GL_LINES get() = GL11.GL_LINES
actual inline val GL_VERTEX_SHADER get() = GL20.GL_VERTEX_SHADER
actual inline val GL_FRAGMENT_SHADER get() = GL20.GL_FRAGMENT_SHADER
actual inline val GL_LINK_STATUS get() = GL20.GL_LINK_STATUS
