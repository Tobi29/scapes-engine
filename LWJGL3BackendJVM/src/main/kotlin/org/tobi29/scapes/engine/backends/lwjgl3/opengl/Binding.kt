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

package org.tobi29.scapes.engine.backends.lwjgl3.opengl

import org.lwjgl.opengl.*
import org.tobi29.scapes.engine.utils.io.ByteViewRO
import org.tobi29.scapes.engine.utils.io.readAsNativeByteBuffer
import java.nio.ByteBuffer

const val NOOP = -1

inline fun noop() {}

const val GL_FRAMEBUFFER = GL30.GL_FRAMEBUFFER
const val GL_COLOR_BUFFER_BIT = GL11.GL_COLOR_BUFFER_BIT
const val GL_DEPTH_BUFFER_BIT = GL11.GL_DEPTH_BUFFER_BIT
const val GL_TEXTURE_2D = GL11.GL_TEXTURE_2D
const val GL_RGB = GL11.GL_RGB
const val GL_RGBA = GL11.GL_RGBA
const val GL_RGB16F = GL30.GL_RGB16F
const val GL_RGBA16F = GL30.GL_RGBA16F
const val GL_BYTE = GL11.GL_BYTE
const val GL_SHORT = GL11.GL_SHORT
const val GL_UNSIGNED_BYTE = GL11.GL_UNSIGNED_BYTE
const val GL_UNSIGNED_SHORT = GL11.GL_UNSIGNED_SHORT
const val GL_UNSIGNED_INT = GL11.GL_UNSIGNED_INT
const val GL_FLOAT = GL11.GL_FLOAT
const val GL_HALF_FLOAT = GL30.GL_HALF_FLOAT
const val GL_DEPTH_COMPONENT = GL11.GL_DEPTH_COMPONENT
const val GL_DEPTH_COMPONENT24 = GL14.GL_DEPTH_COMPONENT24
const val GL_COLOR_ATTACHMENT0 = GL30.GL_COLOR_ATTACHMENT0
const val GL_COLOR_ATTACHMENT1 = GL30.GL_COLOR_ATTACHMENT1
const val GL_COLOR_ATTACHMENT2 = GL30.GL_COLOR_ATTACHMENT2
const val GL_COLOR_ATTACHMENT3 = GL30.GL_COLOR_ATTACHMENT3
const val GL_COLOR_ATTACHMENT4 = GL30.GL_COLOR_ATTACHMENT4
const val GL_COLOR_ATTACHMENT5 = GL30.GL_COLOR_ATTACHMENT5
const val GL_COLOR_ATTACHMENT6 = GL30.GL_COLOR_ATTACHMENT6
const val GL_COLOR_ATTACHMENT7 = GL30.GL_COLOR_ATTACHMENT7
const val GL_COLOR_ATTACHMENT8 = GL30.GL_COLOR_ATTACHMENT8
const val GL_COLOR_ATTACHMENT9 = GL30.GL_COLOR_ATTACHMENT9
const val GL_COLOR_ATTACHMENT10 = GL30.GL_COLOR_ATTACHMENT10
const val GL_COLOR_ATTACHMENT11 = GL30.GL_COLOR_ATTACHMENT11
const val GL_COLOR_ATTACHMENT12 = GL30.GL_COLOR_ATTACHMENT12
const val GL_COLOR_ATTACHMENT13 = GL30.GL_COLOR_ATTACHMENT13
const val GL_COLOR_ATTACHMENT14 = GL30.GL_COLOR_ATTACHMENT14
const val GL_COLOR_ATTACHMENT15 = GL30.GL_COLOR_ATTACHMENT15
const val GL_COLOR_ATTACHMENT16 = GL30.GL_COLOR_ATTACHMENT16
const val GL_COLOR_ATTACHMENT17 = GL30.GL_COLOR_ATTACHMENT17
const val GL_COLOR_ATTACHMENT18 = GL30.GL_COLOR_ATTACHMENT18
const val GL_COLOR_ATTACHMENT19 = GL30.GL_COLOR_ATTACHMENT19
const val GL_COLOR_ATTACHMENT20 = GL30.GL_COLOR_ATTACHMENT20
const val GL_COLOR_ATTACHMENT21 = GL30.GL_COLOR_ATTACHMENT21
const val GL_COLOR_ATTACHMENT22 = GL30.GL_COLOR_ATTACHMENT22
const val GL_COLOR_ATTACHMENT23 = GL30.GL_COLOR_ATTACHMENT23
const val GL_COLOR_ATTACHMENT24 = GL30.GL_COLOR_ATTACHMENT24
const val GL_COLOR_ATTACHMENT25 = GL30.GL_COLOR_ATTACHMENT25
const val GL_COLOR_ATTACHMENT26 = GL30.GL_COLOR_ATTACHMENT26
const val GL_COLOR_ATTACHMENT27 = GL30.GL_COLOR_ATTACHMENT27
const val GL_COLOR_ATTACHMENT28 = GL30.GL_COLOR_ATTACHMENT28
const val GL_COLOR_ATTACHMENT29 = GL30.GL_COLOR_ATTACHMENT29
const val GL_COLOR_ATTACHMENT30 = GL30.GL_COLOR_ATTACHMENT30
const val GL_COLOR_ATTACHMENT31 = GL30.GL_COLOR_ATTACHMENT31
val GL_COLOR_ATTACHMENT = arrayListOf(
        GL_COLOR_ATTACHMENT0,
        GL_COLOR_ATTACHMENT1,
        GL_COLOR_ATTACHMENT2,
        GL_COLOR_ATTACHMENT3,
        GL_COLOR_ATTACHMENT4,
        GL_COLOR_ATTACHMENT5,
        GL_COLOR_ATTACHMENT6,
        GL_COLOR_ATTACHMENT7,
        GL_COLOR_ATTACHMENT8,
        GL_COLOR_ATTACHMENT9,
        GL_COLOR_ATTACHMENT10,
        GL_COLOR_ATTACHMENT11,
        GL_COLOR_ATTACHMENT12,
        GL_COLOR_ATTACHMENT13,
        GL_COLOR_ATTACHMENT14,
        GL_COLOR_ATTACHMENT15,
        GL_COLOR_ATTACHMENT16,
        GL_COLOR_ATTACHMENT17,
        GL_COLOR_ATTACHMENT18,
        GL_COLOR_ATTACHMENT19,
        GL_COLOR_ATTACHMENT20,
        GL_COLOR_ATTACHMENT21,
        GL_COLOR_ATTACHMENT22,
        GL_COLOR_ATTACHMENT23,
        GL_COLOR_ATTACHMENT24,
        GL_COLOR_ATTACHMENT25,
        GL_COLOR_ATTACHMENT26,
        GL_COLOR_ATTACHMENT27,
        GL_COLOR_ATTACHMENT28,
        GL_COLOR_ATTACHMENT29,
        GL_COLOR_ATTACHMENT30,
        GL_COLOR_ATTACHMENT31
)
const val GL_TEXTURE0 = GL13.GL_TEXTURE0
const val GL_TEXTURE1 = GL13.GL_TEXTURE1
const val GL_TEXTURE2 = GL13.GL_TEXTURE2
const val GL_TEXTURE3 = GL13.GL_TEXTURE3
const val GL_TEXTURE4 = GL13.GL_TEXTURE4
const val GL_TEXTURE5 = GL13.GL_TEXTURE5
const val GL_TEXTURE6 = GL13.GL_TEXTURE6
const val GL_TEXTURE7 = GL13.GL_TEXTURE7
const val GL_TEXTURE8 = GL13.GL_TEXTURE8
const val GL_TEXTURE9 = GL13.GL_TEXTURE9
const val GL_TEXTURE10 = GL13.GL_TEXTURE10
const val GL_TEXTURE11 = GL13.GL_TEXTURE11
const val GL_TEXTURE12 = GL13.GL_TEXTURE12
const val GL_TEXTURE13 = GL13.GL_TEXTURE13
const val GL_TEXTURE14 = GL13.GL_TEXTURE14
const val GL_TEXTURE15 = GL13.GL_TEXTURE15
const val GL_TEXTURE16 = GL13.GL_TEXTURE16
const val GL_TEXTURE17 = GL13.GL_TEXTURE17
const val GL_TEXTURE18 = GL13.GL_TEXTURE18
const val GL_TEXTURE19 = GL13.GL_TEXTURE19
const val GL_TEXTURE20 = GL13.GL_TEXTURE20
const val GL_TEXTURE21 = GL13.GL_TEXTURE21
const val GL_TEXTURE22 = GL13.GL_TEXTURE22
const val GL_TEXTURE23 = GL13.GL_TEXTURE23
const val GL_TEXTURE24 = GL13.GL_TEXTURE24
const val GL_TEXTURE25 = GL13.GL_TEXTURE25
const val GL_TEXTURE26 = GL13.GL_TEXTURE26
const val GL_TEXTURE27 = GL13.GL_TEXTURE27
const val GL_TEXTURE28 = GL13.GL_TEXTURE28
const val GL_TEXTURE29 = GL13.GL_TEXTURE29
const val GL_TEXTURE30 = GL13.GL_TEXTURE30
const val GL_TEXTURE31 = GL13.GL_TEXTURE31
val GL_TEXTURE = arrayListOf(
        GL_TEXTURE0,
        GL_TEXTURE1,
        GL_TEXTURE2,
        GL_TEXTURE3,
        GL_TEXTURE4,
        GL_TEXTURE5,
        GL_TEXTURE6,
        GL_TEXTURE7,
        GL_TEXTURE8,
        GL_TEXTURE9,
        GL_TEXTURE10,
        GL_TEXTURE11,
        GL_TEXTURE12,
        GL_TEXTURE13,
        GL_TEXTURE14,
        GL_TEXTURE15,
        GL_TEXTURE16,
        GL_TEXTURE17,
        GL_TEXTURE18,
        GL_TEXTURE19,
        GL_TEXTURE20,
        GL_TEXTURE21,
        GL_TEXTURE22,
        GL_TEXTURE23,
        GL_TEXTURE24,
        GL_TEXTURE25,
        GL_TEXTURE26,
        GL_TEXTURE27,
        GL_TEXTURE28,
        GL_TEXTURE29,
        GL_TEXTURE30,
        GL_TEXTURE31
)
const val GL_DEPTH_ATTACHMENT = GL30.GL_DEPTH_ATTACHMENT
const val GL_TEXTURE_MAG_FILTER = GL11.GL_TEXTURE_MAG_FILTER
const val GL_TEXTURE_MIN_FILTER = GL11.GL_TEXTURE_MIN_FILTER
const val GL_TEXTURE_WRAP_S = GL11.GL_TEXTURE_WRAP_S
const val GL_TEXTURE_WRAP_T = GL11.GL_TEXTURE_WRAP_T
const val GL_REPEAT = GL11.GL_REPEAT
const val GL_NEAREST = GL11.GL_NEAREST
const val GL_LINEAR = GL11.GL_LINEAR
const val GL_CLAMP_TO_EDGE = GL12.GL_CLAMP_TO_EDGE
const val GL_NEAREST_MIPMAP_LINEAR = GL11.GL_NEAREST_MIPMAP_LINEAR
const val GL_LINEAR_MIPMAP_LINEAR = GL11.GL_LINEAR_MIPMAP_LINEAR
const val GL_TEXTURE_MAX_LEVEL = GL12.GL_TEXTURE_MAX_LEVEL
const val GL_ARRAY_BUFFER = GL15.GL_ARRAY_BUFFER
const val GL_ELEMENT_ARRAY_BUFFER = GL15.GL_ELEMENT_ARRAY_BUFFER
const val GL_STATIC_DRAW = GL15.GL_STATIC_DRAW
const val GL_STREAM_DRAW = GL15.GL_STREAM_DRAW
const val GL_FRONT = GL11.GL_FRONT
const val GL_FRONT_AND_BACK = GL11.GL_FRONT_AND_BACK
const val GL_VIEWPORT = GL11.GL_VIEWPORT
const val GL_BLEND = GL11.GL_BLEND
const val GL_SRC_ALPHA = GL11.GL_SRC_ALPHA
const val GL_ONE_MINUS_SRC_ALPHA = GL11.GL_ONE_MINUS_SRC_ALPHA
const val GL_DST_ALPHA = GL11.GL_DST_ALPHA
const val GL_ONE_MINUS_DST_COLOR = GL11.GL_ONE_MINUS_DST_COLOR
const val GL_ONE_MINUS_SRC_COLOR = GL11.GL_ONE_MINUS_SRC_COLOR
const val GL_LINE = GL11.GL_LINE
const val GL_SCISSOR_TEST = GL11.GL_SCISSOR_TEST
const val GL_DEPTH_TEST = GL11.GL_DEPTH_TEST
const val GL_CULL_FACE = GL11.GL_CULL_FACE
const val GL_LEQUAL = GL11.GL_LEQUAL
const val GL_FILL = GL11.GL_FILL
const val GL_NO_ERROR = GL11.GL_NO_ERROR
const val GL_INVALID_ENUM = GL11.GL_INVALID_ENUM
const val GL_INVALID_VALUE = GL11.GL_INVALID_VALUE
const val GL_INVALID_OPERATION = GL11.GL_INVALID_OPERATION
const val GL_STACK_OVERFLOW = GL11.GL_STACK_OVERFLOW
const val GL_STACK_UNDERFLOW = GL11.GL_STACK_UNDERFLOW
const val GL_OUT_OF_MEMORY = GL11.GL_OUT_OF_MEMORY
const val GL_INVALID_FRAMEBUFFER_OPERATION = GL30.GL_INVALID_FRAMEBUFFER_OPERATION
const val GL_TABLE_TOO_LARGE = ARBImaging.GL_TABLE_TOO_LARGE

inline fun glBindFramebuffer(target: Int,
                             framebuffer: Int) =
        GL30.glBindFramebuffer(target, framebuffer)

inline fun glGenFramebuffers() =
        GL30.glGenFramebuffers()

inline fun glDeleteFramebuffers(framebuffer: Int) =
        GL30.glDeleteFramebuffers(framebuffer)

inline fun glClear(mask: Int) =
        GL11.glClear(mask)

inline fun glClearColor(red: Float,
                        green: Float,
                        blue: Float,
                        alpha: Float) =
        GL11.glClearColor(red, green, blue, alpha)

inline fun glDeleteProgram(program: Int) =
        GL20.glDeleteProgram(program)

inline fun glUseProgram(program: Int) =
        GL20.glUseProgram(program)

inline fun glGenTextures() =
        GL11.glGenTextures()

inline fun glBindTexture(target: Int,
                         texture: Int) =
        GL11.glBindTexture(target, texture)

inline fun glDeleteTextures(texture: Int) =
        GL11.glDeleteTextures(texture)


inline fun glTexImage2D(target: Int,
                        level: Int,
                        internalformat: Int,
                        width: Int,
                        height: Int,
                        border: Int,
                        format: Int,
                        type: Int,
                        pixels: ByteBuffer?) =
        GL11.glTexImage2D(target, level, internalformat, width, height, border,
                format, type, pixels)

inline fun glFramebufferTexture2D(target: Int,
                                  attachment: Int,
                                  textarget: Int,
                                  texture: Int,
                                  level: Int) =
        GL30.glFramebufferTexture2D(target, attachment, textarget, texture,
                level)

inline fun glTexParameteri(target: Int,
                           pname: Int,
                           param: Int) =
        GL11.glTexParameteri(target, pname, param)

inline fun glBindVertexArray(array: Int) =
        GL30.glBindVertexArray(array)

inline fun glGenVertexArrays() =
        GL30.glGenVertexArrays()

inline fun glDeleteVertexArrays(array: Int) =
        GL30.glDeleteVertexArrays(array)

inline fun glDrawArrays(mode: Int,
                        first: Int,
                        count: Int) =
        GL11.glDrawArrays(mode, first, count)

inline fun glDrawArraysInstanced(mode: Int,
                                 first: Int,
                                 count: Int,
                                 primcount: Int) =
        GL31.glDrawArraysInstanced(mode, first, count, primcount)

inline fun glDrawElements(mode: Int,
                          count: Int,
                          type: Int,
                          indices: Long) =
        GL11.glDrawElements(mode, count, type, indices)

inline fun glGenBuffers() =
        GL15.glGenBuffers()

inline fun glDeleteBuffers(buffer: Int) =
        GL15.glDeleteBuffers(buffer)

inline fun glBindBuffer(target: Int,
                        buffer: Int) =
        GL15.glBindBuffer(target, buffer)

inline fun glBufferData(target: Int,
                        size: Int,
                        usage: Int) =
        GL15.glBufferData(target, size.toLong(), usage)

inline fun glBufferData(target: Int,
                        data: ByteViewRO,
                        usage: Int) =
        GL15.glBufferData(target, data.readAsNativeByteBuffer(), usage)

inline fun glBufferSubData(target: Int,
                           offset: Long,
                           data: ByteViewRO) =
        GL15.glBufferSubData(target, offset, data.readAsNativeByteBuffer())

inline fun glVertexAttribDivisor(index: Int,
                                 divisor: Int) =
        GL33.glVertexAttribDivisor(index, divisor)

inline fun glEnableVertexAttribArray(index: Int) =
        GL20.glEnableVertexAttribArray(index)

inline fun glVertexAttribPointer(index: Int,
                                 size: Int,
                                 type: Int,
                                 normalized: Boolean,
                                 stride: Int,
                                 pointer: Int) =
        GL20.glVertexAttribPointer(index, size, type, normalized, stride,
                pointer.toLong())

inline fun glVertexAttribIPointer(index: Int,
                                  size: Int,
                                  type: Int,
                                  stride: Int,
                                  pointer: Int) =
        GL30.glVertexAttribIPointer(index, size, type, stride, pointer.toLong())

inline fun glEnable(target: Int) =
        GL11.glEnable(target)

inline fun glDisable(target: Int) =
        GL11.glDisable(target)

inline fun glDepthMask(flag: Boolean) =
        GL11.glDepthMask(flag)

inline fun glPolygonMode(face: Int,
                         mode: Int) =
        GL11.glPolygonMode(face, mode)

inline fun glDepthFunc(func: Int) =
        GL11.glDepthFunc(func)

inline fun glScissor(x: Int,
                     y: Int,
                     width: Int,
                     height: Int) =
        GL11.glScissor(x, y, width, height)

inline fun glViewport(x: Int,
                      y: Int,
                      w: Int,
                      h: Int) =
        GL11.glViewport(x, y, w, h)

inline fun glGetIntegerv(pname: Int,
                         params: IntArray) =
        GL11.glGetIntegerv(pname, params)

inline fun glReadBuffer(src: Int) =
        GL11.glReadBuffer(src)

inline fun glReadPixels(x: Int,
                        y: Int,
                        width: Int,
                        height: Int,
                        format: Int,
                        type: Int,
                        pixels: ByteBuffer) =
        GL11.glReadPixels(x, y, width, height, format, type, pixels)

inline fun glBlendFunc(sfactor: Int,
                       dfactor: Int) =
        GL11.glBlendFunc(sfactor, dfactor)

inline fun glTexSubImage2D(target: Int,
                           level: Int,
                           xoffset: Int,
                           yoffset: Int,
                           width: Int,
                           height: Int,
                           format: Int,
                           type: Int,
                           pixels: ByteBuffer) =
        GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height,
                format, type, pixels)

inline fun glGetTexImage(tex: Int,
                         level: Int,
                         format: Int,
                         type: Int,
                         pixels: ByteBuffer) =
        GL11.glGetTexImage(tex, level, format, type, pixels)

inline fun glGetError() =
        GL11.glGetError()

inline fun glActiveTexture(texture: Int) =
        GL13.glActiveTexture(texture)

inline fun glUniform1f(location: Int,
                       v0: Float) =
        GL20.glUniform1f(location, v0)

inline fun glUniform2f(location: Int,
                       v0: Float,
                       v1: Float) =
        GL20.glUniform2f(location, v0, v1)

inline fun glUniform3f(location: Int,
                       v0: Float,
                       v1: Float,
                       v2: Float) =
        GL20.glUniform3f(location, v0, v1, v2)

inline fun glUniform4f(location: Int,
                       v0: Float,
                       v1: Float,
                       v2: Float,
                       v3: Float) =
        GL20.glUniform4f(location, v0, v1, v2, v3)

inline fun glUniform1i(location: Int,
                       v0: Int) =
        GL20.glUniform1i(location, v0)

inline fun glUniform2i(location: Int,
                       v0: Int,
                       v1: Int) =
        GL20.glUniform2i(location, v0, v1)

inline fun glUniform3i(location: Int,
                       v0: Int,
                       v1: Int,
                       v2: Int) =
        GL20.glUniform3i(location, v0, v1, v2)

inline fun glUniform4i(location: Int,
                       v0: Int,
                       v1: Int,
                       v2: Int,
                       v3: Int) =
        GL20.glUniform4i(location, v0, v1, v2, v3)

inline fun glUniform1fv(location: Int,
                        value: FloatArray) =
        GL20.glUniform1fv(location, value)

inline fun glUniform2fv(location: Int,
                        value: FloatArray) =
        GL20.glUniform2fv(location, value)

inline fun glUniform3fv(location: Int,
                        value: FloatArray) =
        GL20.glUniform3fv(location, value)

inline fun glUniform4fv(location: Int,
                        value: FloatArray) =
        GL20.glUniform4fv(location, value)

inline fun glUniform1iv(location: Int,
                        value: IntArray) =
        GL20.glUniform1iv(location, value)

inline fun glUniform2iv(location: Int,
                        value: IntArray) =
        GL20.glUniform2iv(location, value)

inline fun glUniform3iv(location: Int,
                        value: IntArray) =
        GL20.glUniform3iv(location, value)

inline fun glUniform4iv(location: Int,
                        value: IntArray) =
        GL20.glUniform4iv(location, value)

inline fun glUniformMatrix2fv(location: Int,
                              transpose: Boolean,
                              value: FloatArray) =
        GL20.glUniformMatrix2fv(location, transpose, value)

inline fun glUniformMatrix3fv(location: Int,
                              transpose: Boolean,
                              value: FloatArray) =
        GL20.glUniformMatrix3fv(location, transpose, value)

inline fun glUniformMatrix4fv(location: Int,
                              transpose: Boolean,
                              value: FloatArray) =
        GL20.glUniformMatrix4fv(location, transpose, value)

inline fun glVertexAttrib1f(location: Int,
                            v0: Float) =
        GL20.glVertexAttrib1f(location, v0)

inline fun glVertexAttrib2f(location: Int,
                            v0: Float,
                            v1: Float) =
        GL20.glVertexAttrib2f(location, v0, v1)

inline fun glVertexAttrib3f(location: Int,
                            v0: Float,
                            v1: Float,
                            v2: Float) =
        GL20.glVertexAttrib3f(location, v0, v1, v2)

inline fun glVertexAttrib4f(location: Int,
                            v0: Float,
                            v1: Float,
                            v2: Float,
                            v3: Float) =
        GL20.glVertexAttrib4f(location, v0, v1, v2, v3)

inline fun glVertexAttrib1fv(location: Int,
                             value: FloatArray) =
        GL20.glVertexAttrib1fv(location, value)

inline fun glVertexAttrib2fv(location: Int,
                             value: FloatArray) =
        GL20.glVertexAttrib2fv(location, value)

inline fun glVertexAttrib3fv(location: Int,
                             value: FloatArray) =
        GL20.glVertexAttrib3fv(location, value)

inline fun glVertexAttrib4fv(location: Int,
                             value: FloatArray) =
        GL20.glVertexAttrib4fv(location, value)
