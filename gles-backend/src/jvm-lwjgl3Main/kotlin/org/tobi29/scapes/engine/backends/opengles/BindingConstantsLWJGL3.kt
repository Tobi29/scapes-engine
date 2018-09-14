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

import org.lwjgl.opengles.GLES20
import org.lwjgl.opengles.GLES30

actual inline val GL_FRAMEBUFFER get() = GLES20.GL_FRAMEBUFFER
actual inline val GL_COLOR_BUFFER_BIT get() = GLES20.GL_COLOR_BUFFER_BIT
actual inline val GL_DEPTH_BUFFER_BIT get() = GLES20.GL_DEPTH_BUFFER_BIT
actual inline val GL_TEXTURE_2D get() = GLES20.GL_TEXTURE_2D
actual inline val GL_RGB get() = GLES20.GL_RGB
actual inline val GL_RGBA get() = GLES20.GL_RGBA
actual inline val GL_RGB16F get() = GLES30.GL_RGB16F
actual inline val GL_RGBA16F get() = GLES30.GL_RGBA16F
actual inline val GL_BYTE get() = GLES20.GL_BYTE
actual inline val GL_SHORT get() = GLES20.GL_SHORT
actual inline val GL_UNSIGNED_BYTE get() = GLES20.GL_UNSIGNED_BYTE
actual inline val GL_UNSIGNED_SHORT get() = GLES20.GL_UNSIGNED_SHORT
actual inline val GL_UNSIGNED_INT get() = GLES20.GL_UNSIGNED_INT
actual inline val GL_FLOAT get() = GLES20.GL_FLOAT
actual inline val GL_HALF_FLOAT get() = GLES30.GL_HALF_FLOAT
actual inline val GL_DEPTH_COMPONENT get() = GLES20.GL_DEPTH_COMPONENT
actual inline val GL_DEPTH_COMPONENT24 get() = GLES30.GL_DEPTH_COMPONENT24

actual inline fun GL_COLOR_ATTACHMENT(i: Int): GLEnum {
    if (i < 0 || i > 31)
        throw IllegalArgumentException("Color attachment must be 0..31, was $i")
    return GLES20.GL_COLOR_ATTACHMENT0 + i
}

actual inline fun GL_TEXTURE(i: Int): GLEnum {
    if (i < 0 || i > 31)
        throw IllegalArgumentException("Active texture must be 0..31, was $i")
    return GLES20.GL_TEXTURE0 + i
}

actual inline val GL_DEPTH_ATTACHMENT get() = GLES20.GL_DEPTH_ATTACHMENT
actual inline val GL_TEXTURE_MAG_FILTER get() = GLES20.GL_TEXTURE_MAG_FILTER
actual inline val GL_TEXTURE_MIN_FILTER get() = GLES20.GL_TEXTURE_MIN_FILTER
actual inline val GL_TEXTURE_WRAP_S get() = GLES20.GL_TEXTURE_WRAP_S
actual inline val GL_TEXTURE_WRAP_T get() = GLES20.GL_TEXTURE_WRAP_T
actual inline val GL_REPEAT get() = GLES20.GL_REPEAT
actual inline val GL_NEAREST get() = GLES20.GL_NEAREST
actual inline val GL_LINEAR get() = GLES20.GL_LINEAR
actual inline val GL_CLAMP_TO_EDGE get() = GLES20.GL_CLAMP_TO_EDGE
actual inline val GL_NEAREST_MIPMAP_LINEAR get() = GLES20.GL_NEAREST_MIPMAP_LINEAR
actual inline val GL_LINEAR_MIPMAP_LINEAR get() = GLES20.GL_LINEAR_MIPMAP_LINEAR
actual inline val GL_TEXTURE_MAX_LEVEL get() = GLES30.GL_TEXTURE_MAX_LEVEL
actual inline val GL_ARRAY_BUFFER get() = GLES20.GL_ARRAY_BUFFER
actual inline val GL_ELEMENT_ARRAY_BUFFER get() = GLES20.GL_ELEMENT_ARRAY_BUFFER
actual inline val GL_STATIC_DRAW get() = GLES20.GL_STATIC_DRAW
actual inline val GL_STREAM_DRAW get() = GLES20.GL_STREAM_DRAW
actual inline val GL_FRONT get() = GLES20.GL_FRONT
actual inline val GL_FRONT_AND_BACK get() = GLES20.GL_FRONT_AND_BACK
actual inline val GL_VIEWPORT get() = GLES20.GL_VIEWPORT
actual inline val GL_BLEND get() = GLES20.GL_BLEND
actual inline val GL_SRC_ALPHA get() = GLES20.GL_SRC_ALPHA
actual inline val GL_ONE_MINUS_SRC_ALPHA get() = GLES20.GL_ONE_MINUS_SRC_ALPHA
actual inline val GL_DST_ALPHA get() = GLES20.GL_DST_ALPHA
actual inline val GL_ONE_MINUS_DST_COLOR get() = GLES20.GL_ONE_MINUS_DST_COLOR
actual inline val GL_ONE_MINUS_SRC_COLOR get() = GLES20.GL_ONE_MINUS_SRC_COLOR
actual inline val GL_SCISSOR_TEST get() = GLES20.GL_SCISSOR_TEST
actual inline val GL_DEPTH_TEST get() = GLES20.GL_DEPTH_TEST
actual inline val GL_CULL_FACE get() = GLES20.GL_CULL_FACE
actual inline val GL_LEQUAL get() = GLES20.GL_LEQUAL
actual inline val GL_NO_ERROR get() = GLES20.GL_NO_ERROR
actual inline val GL_INVALID_ENUM get() = GLES20.GL_INVALID_ENUM
actual inline val GL_INVALID_VALUE get() = GLES20.GL_INVALID_VALUE
actual inline val GL_INVALID_OPERATION get() = GLES20.GL_INVALID_OPERATION
actual inline val GL_OUT_OF_MEMORY get() = GLES20.GL_OUT_OF_MEMORY
actual inline val GL_INVALID_FRAMEBUFFER_OPERATION get() = GLES20.GL_INVALID_FRAMEBUFFER_OPERATION
actual inline val GL_FRAMEBUFFER_COMPLETE get() = GLES20.GL_FRAMEBUFFER_COMPLETE
actual inline val GL_FRAMEBUFFER_UNSUPPORTED get() = GLES20.GL_FRAMEBUFFER_UNSUPPORTED
actual inline val GL_TRIANGLES get() = GLES20.GL_TRIANGLES
actual inline val GL_LINES get() = GLES20.GL_LINES
actual inline val GL_VERTEX_SHADER get() = GLES20.GL_VERTEX_SHADER
actual inline val GL_FRAGMENT_SHADER get() = GLES20.GL_FRAGMENT_SHADER
actual inline val GL_LINK_STATUS get() = GLES20.GL_LINK_STATUS
