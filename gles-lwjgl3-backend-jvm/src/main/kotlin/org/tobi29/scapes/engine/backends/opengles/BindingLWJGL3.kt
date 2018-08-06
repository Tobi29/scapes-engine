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

@file:Suppress("NOTHING_TO_INLINE", "unused")

package org.tobi29.scapes.engine.backends.opengles

import org.lwjgl.opengles.GLES20
import org.lwjgl.opengles.GLES30
import org.tobi29.io.*

actual inline fun GLESHandle.byteView(capacity: Int): ByteViewE =
    ByteBufferNative(capacity).viewE

actual inline fun GLESHandle.glBindFramebuffer(
    target: Int,
    framebuffer: Int
) = GLES20.glBindFramebuffer(target, framebuffer)

actual inline fun GLESHandle.glGenFramebuffers() = GLES20.glGenFramebuffers()

actual inline fun GLESHandle.glDeleteFramebuffers(framebuffer: Int) =
    GLES20.glDeleteFramebuffers(framebuffer)

actual inline fun GLESHandle.glClear(mask: Int) = GLES20.glClear(mask)

actual inline fun GLESHandle.glClearColor(
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float
) = GLES20.glClearColor(red, green, blue, alpha)

actual inline fun GLESHandle.glDeleteProgram(program: Int) =
    GLES20.glDeleteProgram(program)

actual inline fun GLESHandle.glUseProgram(program: Int) =
    GLES20.glUseProgram(program)

actual inline fun GLESHandle.glGenTextures() = GLES20.glGenTextures()

actual inline fun GLESHandle.glBindTexture(
    target: Int,
    texture: Int
) = GLES20.glBindTexture(target, texture)

actual inline fun GLESHandle.glDeleteTextures(texture: Int) =
    GLES20.glDeleteTextures(texture)

actual inline fun GLESHandle.glTexImage2D(
    target: GLEnum,
    level: Int,
    internalformat: GLEnum,
    width: Int,
    height: Int,
    border: Int,
    format: GLEnum,
    type: GLEnum,
    pixels: ByteViewRO?
) = GLES20.glTexImage2D(
    target, level, internalformat, width, height, border,
    format, type, pixels?.readAsNativeByteBuffer()
)

actual inline fun GLESHandle.glFramebufferTexture2D(
    target: Int,
    attachment: Int,
    textarget: Int,
    texture: Int,
    level: Int
) = GLES20.glFramebufferTexture2D(target, attachment, textarget, texture, level)

actual inline fun GLESHandle.glTexParameteri(
    target: Int,
    pname: Int,
    param: Int
) = GLES20.glTexParameteri(target, pname, param)

actual inline fun GLESHandle.glBindVertexArray(array: Int) =
    GLES30.glBindVertexArray(array)

actual inline fun GLESHandle.glGenVertexArrays() = GLES30.glGenVertexArrays()

actual inline fun GLESHandle.glDeleteVertexArrays(array: Int) =
    GLES30.glDeleteVertexArrays(array)

actual inline fun GLESHandle.glDrawArrays(
    mode: Int,
    first: Int,
    count: Int
) = GLES20.glDrawArrays(mode, first, count)

actual inline fun GLESHandle.glDrawArraysInstanced(
    mode: Int,
    first: Int,
    count: Int,
    primcount: Int
) = GLES30.glDrawArraysInstanced(mode, first, count, primcount)

actual inline fun GLESHandle.glDrawElements(
    mode: Int,
    count: Int,
    type: Int,
    indices: Int
) = GLES20.glDrawElements(mode, count, type, indices.toLong() and 0xFFFFFFFF)

actual inline fun GLESHandle.glGenBuffers() = GLES20.glGenBuffers()

actual inline fun GLESHandle.glDeleteBuffers(buffer: Int) =
    GLES20.glDeleteBuffers(buffer)

actual inline fun GLESHandle.glBindBuffer(
    target: Int,
    buffer: Int
) = GLES20.glBindBuffer(target, buffer)

actual inline fun GLESHandle.glBufferData(
    target: Int,
    size: Int,
    usage: Int
) = GLES20.glBufferData(target, size.toLong(), usage)

actual inline fun GLESHandle.glBufferData(
    target: Int,
    data: ByteViewRO,
    usage: Int
) = GLES20.glBufferData(target, data.readAsNativeByteBuffer(), usage)

actual inline fun GLESHandle.glBufferSubData(
    target: Int,
    offset: Int,
    data: ByteViewRO
) = GLES20.glBufferSubData(
    target, offset.toLong() and 0xFFFFFFFF, data.readAsNativeByteBuffer()
)

actual inline fun GLESHandle.glVertexAttribDivisor(
    index: Int,
    divisor: Int
) = GLES30.glVertexAttribDivisor(index, divisor)

actual inline fun GLESHandle.glEnableVertexAttribArray(index: Int) =
    GLES20.glEnableVertexAttribArray(index)

actual inline fun GLESHandle.glVertexAttribPointer(
    index: Int,
    size: Int,
    type: Int,
    normalized: Boolean,
    stride: Int,
    pointer: Int
) = GLES20.glVertexAttribPointer(
    index, size, type, normalized, stride,
    pointer.toLong()
)

actual inline fun GLESHandle.glVertexAttribIPointer(
    index: Int,
    size: Int,
    type: Int,
    stride: Int,
    pointer: Int
) = GLES30.glVertexAttribIPointer(index, size, type, stride, pointer.toLong())

actual inline fun GLESHandle.glEnable(target: Int) = GLES20.glEnable(target)

actual inline fun GLESHandle.glDisable(target: Int) = GLES20.glDisable(target)

actual inline fun GLESHandle.glDepthMask(flag: Boolean) =
    GLES20.glDepthMask(flag)

actual inline fun GLESHandle.glDepthFunc(func: Int) = GLES20.glDepthFunc(func)

actual inline fun GLESHandle.glScissor(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) = GLES20.glScissor(x, y, width, height)

actual inline fun GLESHandle.glViewport(
    x: Int,
    y: Int,
    w: Int,
    h: Int
) = GLES20.glViewport(x, y, w, h)

actual inline fun GLESHandle.glGetIntegerv(
    pname: Int,
    params: IntArray
) = GLES20.glGetIntegerv(pname, params)

actual inline fun GLESHandle.glReadBuffer(
    src: GLEnum
) = GLES30.glReadBuffer(src)

actual inline fun GLESHandle.glReadPixels(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    format: Int,
    type: Int,
    pixels: ByteView
) = pixels.mutateAsNativeByteBuffer {
    GLES20.glReadPixels(x, y, width, height, format, type, it)
}

actual inline fun GLESHandle.glBlendFunc(
    sfactor: Int,
    dfactor: Int
) = GLES20.glBlendFunc(sfactor, dfactor)

actual inline fun GLESHandle.glTexSubImage2D(
    target: Int,
    level: Int,
    xoffset: Int,
    yoffset: Int,
    width: Int,
    height: Int,
    format: Int,
    type: Int,
    pixels: ByteViewRO
) = GLES20.glTexSubImage2D(
    target, level, xoffset, yoffset, width, height,
    format, type, pixels.readAsNativeByteBuffer()
)

actual inline fun GLESHandle.glGetError() = GLES20.glGetError()

actual inline fun GLESHandle.glActiveTexture(texture: Int) =
    GLES20.glActiveTexture(texture)

actual inline fun GLESHandle.glUniform1f(
    location: Int,
    v0: Float
) = GLES20.glUniform1f(location, v0)

actual inline fun GLESHandle.glUniform2f(
    location: Int,
    v0: Float,
    v1: Float
) = GLES20.glUniform2f(location, v0, v1)

actual inline fun GLESHandle.glUniform3f(
    location: Int,
    v0: Float,
    v1: Float,
    v2: Float
) = GLES20.glUniform3f(location, v0, v1, v2)

actual inline fun GLESHandle.glUniform4f(
    location: Int,
    v0: Float,
    v1: Float,
    v2: Float,
    v3: Float
) = GLES20.glUniform4f(location, v0, v1, v2, v3)

actual inline fun GLESHandle.glUniform1i(
    location: Int,
    v0: Int
) = GLES20.glUniform1i(location, v0)

actual inline fun GLESHandle.glUniform2i(
    location: Int,
    v0: Int,
    v1: Int
) = GLES20.glUniform2i(location, v0, v1)

actual inline fun GLESHandle.glUniform3i(
    location: Int,
    v0: Int,
    v1: Int,
    v2: Int
) = GLES20.glUniform3i(location, v0, v1, v2)

actual inline fun GLESHandle.glUniform4i(
    location: Int,
    v0: Int,
    v1: Int,
    v2: Int,
    v3: Int
) = GLES20.glUniform4i(location, v0, v1, v2, v3)

actual inline fun GLESHandle.glUniform1fv(
    location: Int,
    value: FloatArray
) = GLES20.glUniform1fv(location, value)

actual inline fun GLESHandle.glUniform2fv(
    location: Int,
    value: FloatArray
) = GLES20.glUniform2fv(location, value)

actual inline fun GLESHandle.glUniform3fv(
    location: Int,
    value: FloatArray
) = GLES20.glUniform3fv(location, value)

actual inline fun GLESHandle.glUniform4fv(
    location: Int,
    value: FloatArray
) = GLES20.glUniform4fv(location, value)

actual inline fun GLESHandle.glUniform1iv(
    location: Int,
    value: IntArray
) = GLES20.glUniform1iv(location, value)

actual inline fun GLESHandle.glUniform2iv(
    location: Int,
    value: IntArray
) =
    GLES20.glUniform2iv(location, value)

actual inline fun GLESHandle.glUniform3iv(
    location: Int,
    value: IntArray
) = GLES20.glUniform3iv(location, value)

actual inline fun GLESHandle.glUniform4iv(
    location: Int,
    value: IntArray
) = GLES20.glUniform4iv(location, value)

actual inline fun GLESHandle.glUniformMatrix2fv(
    location: Int,
    transpose: Boolean,
    value: FloatArray
) = GLES20.glUniformMatrix2fv(location, transpose, value)

actual inline fun GLESHandle.glUniformMatrix3fv(
    location: Int,
    transpose: Boolean,
    value: FloatArray
) = GLES20.glUniformMatrix3fv(location, transpose, value)

actual inline fun GLESHandle.glUniformMatrix4fv(
    location: Int,
    transpose: Boolean,
    value: FloatArray
) = GLES20.glUniformMatrix4fv(location, transpose, value)

actual inline fun GLESHandle.glVertexAttrib1f(
    location: Int,
    v0: Float
) = GLES20.glVertexAttrib1f(location, v0)

actual inline fun GLESHandle.glVertexAttrib2f(
    location: Int,
    v0: Float,
    v1: Float
) = GLES20.glVertexAttrib2f(location, v0, v1)

actual inline fun GLESHandle.glVertexAttrib3f(
    location: Int,
    v0: Float,
    v1: Float,
    v2: Float
) = GLES20.glVertexAttrib3f(location, v0, v1, v2)

actual inline fun GLESHandle.glVertexAttrib4f(
    location: Int,
    v0: Float,
    v1: Float,
    v2: Float,
    v3: Float
) = GLES20.glVertexAttrib4f(location, v0, v1, v2, v3)

actual inline fun GLESHandle.glVertexAttrib1fv(
    location: Int,
    value: FloatArray
) = GLES20.glVertexAttrib1fv(location, value)

actual inline fun GLESHandle.glVertexAttrib2fv(
    location: Int,
    value: FloatArray
) = GLES20.glVertexAttrib2fv(location, value)

actual inline fun GLESHandle.glVertexAttrib3fv(
    location: Int,
    value: FloatArray
) = GLES20.glVertexAttrib3fv(location, value)

actual inline fun GLESHandle.glVertexAttrib4fv(
    location: Int,
    value: FloatArray
) = GLES20.glVertexAttrib4fv(location, value)

actual inline fun GLESHandle.glCheckFramebufferStatus(
    target: Int
) = GLES20.glCheckFramebufferStatus(target)

actual inline fun GLESHandle.glDrawBuffers(
    bufs: IntArray
) = GLES30.glDrawBuffers(bufs)

actual inline fun GLESHandle.glGetShaderInfoLog(
    shader: Int
): String? = GLES20.glGetShaderInfoLog(shader)

actual inline fun GLESHandle.glGetProgramInfoLog(
    program: Int
): String? = GLES20.glGetProgramInfoLog(program)

actual inline fun GLESHandle.glGetProgramb(
    program: GLProgram,
    pname: GLEnum
): Boolean = GLES20.glGetProgrami(program, pname) == GLES20.GL_TRUE

actual inline fun GLESHandle.glShaderSource(
    shader: Int,
    string: String
) = GLES20.glShaderSource(shader, string)

actual inline fun GLESHandle.glCompileShader(
    shader: Int
) = GLES20.glCompileShader(shader)

actual inline fun GLESHandle.glAttachShader(
    program: Int,
    shader: Int
) = GLES20.glAttachShader(program, shader)

actual inline fun GLESHandle.glLinkProgram(
    program: Int
) = GLES20.glLinkProgram(program)

actual inline fun GLESHandle.glCreateShader(
    type: Int
) = GLES20.glCreateShader(type)

actual inline fun GLESHandle.glCreateProgram(
) = GLES20.glCreateProgram()

actual inline fun GLESHandle.glGetUniformLocation(
    program: Int,
    name: String
) = GLES20.glGetUniformLocation(program, name)

actual inline fun GLESHandle.glDeleteShader(
    shader: Int
) = GLES20.glDeleteShader(shader)

actual inline fun GLESHandle.glDetachShader(
    program: Int,
    shader: Int
) = GLES20.glDetachShader(program, shader)
