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

package org.tobi29.scapes.engine.backends.opengl

import org.lwjgl.opengl.*
import org.tobi29.arrays.Bytes
import org.tobi29.arrays.BytesRO
import org.tobi29.io.*

actual inline fun GLHandle.byteView(capacity: Int): ByteViewE =
    ByteBufferNative(capacity).viewE

actual inline fun GLHandle.glBindFramebuffer(
    target: GLEnum,
    framebuffer: Int
) = GL30.glBindFramebuffer(target, framebuffer)

actual inline fun GLHandle.glGenFramebuffers() = GL30.glGenFramebuffers()

actual inline fun GLHandle.glDeleteFramebuffers(framebuffer: Int) =
    GL30.glDeleteFramebuffers(framebuffer)

actual inline fun GLHandle.glClear(mask: Int) = GL11.glClear(mask)

actual inline fun GLHandle.glClearColor(
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float
) = GL11.glClearColor(red, green, blue, alpha)

actual inline fun GLHandle.glDeleteProgram(program: GLProgram) =
    GL20.glDeleteProgram(program)

actual inline fun GLHandle.glUseProgram(program: GLProgram) =
    GL20.glUseProgram(program)

actual inline fun GLHandle.glGenTextures() = GL11.glGenTextures()

actual inline fun GLHandle.glBindTexture(
    target: GLEnum,
    texture: Int
) = GL11.glBindTexture(target, texture)

actual inline fun GLHandle.glDeleteTextures(texture: Int) =
    GL11.glDeleteTextures(texture)

actual inline fun GLHandle.glTexImage2D(
    target: GLEnum,
    level: Int,
    internalformat: GLEnum,
    width: Int,
    height: Int,
    border: Int,
    format: GLEnum,
    type: GLEnum,
    pixels: BytesRO?
) = GL11.glTexImage2D(
    target, level, internalformat, width, height, border,
    format, type, pixels?.readAsNativeByteBuffer()
)

actual inline fun GLHandle.glFramebufferTexture2D(
    target: GLEnum,
    attachment: Int,
    textarget: GLEnum,
    texture: Int,
    level: Int
) = GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level)

actual inline fun GLHandle.glTexParameteri(
    target: GLEnum,
    pname: GLEnum,
    param: Int
) = GL11.glTexParameteri(target, pname, param)

actual inline fun GLHandle.glBindVertexArray(array: Int) =
    GL30.glBindVertexArray(array)

actual inline fun GLHandle.glGenVertexArrays() = GL30.glGenVertexArrays()

actual inline fun GLHandle.glDeleteVertexArrays(array: Int) =
    GL30.glDeleteVertexArrays(array)

actual inline fun GLHandle.glDrawArrays(
    mode: Int,
    first: Int,
    count: Int
) = GL11.glDrawArrays(mode, first, count)

actual inline fun GLHandle.glDrawArraysInstanced(
    mode: Int,
    first: Int,
    count: Int,
    primcount: Int
) = GL31.glDrawArraysInstanced(mode, first, count, primcount)

actual inline fun GLHandle.glDrawElements(
    mode: Int,
    count: Int,
    type: Int,
    indices: Int
) = GL11.glDrawElements(mode, count, type, indices.toLong() and 0xFFFFFFFF)

actual inline fun GLHandle.glGenBuffers() = GL15.glGenBuffers()

actual inline fun GLHandle.glDeleteBuffers(buffer: Int) =
    GL15.glDeleteBuffers(buffer)

actual inline fun GLHandle.glBindBuffer(
    target: GLEnum,
    buffer: Int
) = GL15.glBindBuffer(target, buffer)

actual inline fun GLHandle.glBufferData(
    target: GLEnum,
    size: Int,
    usage: Int
) = GL15.glBufferData(target, size.toLong(), usage)

actual inline fun GLHandle.glBufferData(
    target: GLEnum,
    data: BytesRO,
    usage: Int
) = GL15.glBufferData(target, data.readAsNativeByteBuffer(), usage)

actual inline fun GLHandle.glBufferSubData(
    target: GLEnum,
    offset: Int,
    data: BytesRO
) = GL15.glBufferSubData(
    target, offset.toLong() and 0xFFFFFFFF, data.readAsNativeByteBuffer()
)

actual inline fun GLHandle.glVertexAttribDivisor(
    index: Int,
    divisor: Int
) = GL33.glVertexAttribDivisor(index, divisor)

actual inline fun GLHandle.glEnableVertexAttribArray(index: Int) =
    GL20.glEnableVertexAttribArray(index)

actual inline fun GLHandle.glVertexAttribPointer(
    index: Int,
    size: Int,
    type: Int,
    normalized: Boolean,
    stride: Int,
    pointer: Int
) = GL20.glVertexAttribPointer(
    index, size, type, normalized, stride,
    pointer.toLong()
)

actual inline fun GLHandle.glVertexAttribIPointer(
    index: Int,
    size: Int,
    type: Int,
    stride: Int,
    pointer: Int
) = GL30.glVertexAttribIPointer(index, size, type, stride, pointer.toLong())

actual inline fun GLHandle.glEnable(target: GLEnum) = GL11.glEnable(target)

actual inline fun GLHandle.glDisable(target: GLEnum) = GL11.glDisable(target)

actual inline fun GLHandle.glDepthMask(flag: Boolean) = GL11.glDepthMask(flag)

actual inline fun GLHandle.glPolygonMode(
    face: Int,
    mode: Int
) = GL11.glPolygonMode(face, mode)

actual inline fun GLHandle.glDepthFunc(func: Int) = GL11.glDepthFunc(func)

actual inline fun GLHandle.glScissor(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) =
    GL11.glScissor(x, y, width, height)

actual inline fun GLHandle.glViewport(
    x: Int,
    y: Int,
    w: Int,
    h: Int
) =
    GL11.glViewport(x, y, w, h)

actual inline fun GLHandle.glGetIntegerv(
    pname: GLEnum,
    params: IntArray
) = GL11.glGetIntegerv(pname, params)

actual inline fun GLHandle.glReadBuffer(src: Int) = GL11.glReadBuffer(src)

actual inline fun GLHandle.glReadPixels(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    format: Int,
    type: Int,
    pixels: Bytes
) = pixels.mutateAsNativeByteBuffer {
    GL11.glReadPixels(x, y, width, height, format, type, it)
}

actual inline fun GLHandle.glBlendFunc(
    sfactor: Int,
    dfactor: Int
) = GL11.glBlendFunc(sfactor, dfactor)

actual inline fun GLHandle.glTexSubImage2D(
    target: GLEnum,
    level: Int,
    xoffset: Int,
    yoffset: Int,
    width: Int,
    height: Int,
    format: Int,
    type: Int,
    pixels: BytesRO
) = GL11.glTexSubImage2D(
    target, level, xoffset, yoffset, width, height,
    format, type, pixels.readAsNativeByteBuffer()
)

actual inline fun GLHandle.glGetTexImage(
    tex: Int,
    level: Int,
    format: Int,
    type: Int,
    pixels: Bytes
) = pixels.mutateAsNativeByteBuffer {
    GL11.glGetTexImage(tex, level, format, type, it)
}

actual inline fun GLHandle.glGetError() = GL11.glGetError()

actual inline fun GLHandle.glActiveTexture(texture: Int) =
    GL13.glActiveTexture(texture)

actual inline fun GLHandle.glUniform1f(
    location: Int,
    v0: Float
) = GL20.glUniform1f(location, v0)

actual inline fun GLHandle.glUniform2f(
    location: Int,
    v0: Float,
    v1: Float
) = GL20.glUniform2f(location, v0, v1)

actual inline fun GLHandle.glUniform3f(
    location: Int,
    v0: Float,
    v1: Float,
    v2: Float
) = GL20.glUniform3f(location, v0, v1, v2)

actual inline fun GLHandle.glUniform4f(
    location: Int,
    v0: Float,
    v1: Float,
    v2: Float,
    v3: Float
) = GL20.glUniform4f(location, v0, v1, v2, v3)

actual inline fun GLHandle.glUniform1i(
    location: Int,
    v0: Int
) = GL20.glUniform1i(location, v0)

actual inline fun GLHandle.glUniform2i(
    location: Int,
    v0: Int,
    v1: Int
) = GL20.glUniform2i(location, v0, v1)

actual inline fun GLHandle.glUniform3i(
    location: Int,
    v0: Int,
    v1: Int,
    v2: Int
) = GL20.glUniform3i(location, v0, v1, v2)

actual inline fun GLHandle.glUniform4i(
    location: Int,
    v0: Int,
    v1: Int,
    v2: Int,
    v3: Int
) = GL20.glUniform4i(location, v0, v1, v2, v3)

actual inline fun GLHandle.glUniform1fv(
    location: Int,
    value: FloatArray
) = GL20.glUniform1fv(location, value)

actual inline fun GLHandle.glUniform2fv(
    location: Int,
    value: FloatArray
) = GL20.glUniform2fv(location, value)

actual inline fun GLHandle.glUniform3fv(
    location: Int,
    value: FloatArray
) = GL20.glUniform3fv(location, value)

actual inline fun GLHandle.glUniform4fv(
    location: Int,
    value: FloatArray
) = GL20.glUniform4fv(location, value)

actual inline fun GLHandle.glUniform1iv(
    location: Int,
    value: IntArray
) = GL20.glUniform1iv(location, value)

actual inline fun GLHandle.glUniform2iv(
    location: Int,
    value: IntArray
) =
    GL20.glUniform2iv(location, value)

actual inline fun GLHandle.glUniform3iv(
    location: Int,
    value: IntArray
) = GL20.glUniform3iv(location, value)

actual inline fun GLHandle.glUniform4iv(
    location: Int,
    value: IntArray
) = GL20.glUniform4iv(location, value)

actual inline fun GLHandle.glUniformMatrix2fv(
    location: Int,
    transpose: Boolean,
    value: FloatArray
) = GL20.glUniformMatrix2fv(location, transpose, value)

actual inline fun GLHandle.glUniformMatrix3fv(
    location: Int,
    transpose: Boolean,
    value: FloatArray
) = GL20.glUniformMatrix3fv(location, transpose, value)

actual inline fun GLHandle.glUniformMatrix4fv(
    location: Int,
    transpose: Boolean,
    value: FloatArray
) = GL20.glUniformMatrix4fv(location, transpose, value)

actual inline fun GLHandle.glVertexAttrib1f(
    location: Int,
    v0: Float
) = GL20.glVertexAttrib1f(location, v0)

actual inline fun GLHandle.glVertexAttrib2f(
    location: Int,
    v0: Float,
    v1: Float
) = GL20.glVertexAttrib2f(location, v0, v1)

actual inline fun GLHandle.glVertexAttrib3f(
    location: Int,
    v0: Float,
    v1: Float,
    v2: Float
) = GL20.glVertexAttrib3f(location, v0, v1, v2)

actual inline fun GLHandle.glVertexAttrib4f(
    location: Int,
    v0: Float,
    v1: Float,
    v2: Float,
    v3: Float
) = GL20.glVertexAttrib4f(location, v0, v1, v2, v3)

actual inline fun GLHandle.glVertexAttrib1fv(
    location: Int,
    value: FloatArray
) = GL20.glVertexAttrib1fv(location, value)

actual inline fun GLHandle.glVertexAttrib2fv(
    location: Int,
    value: FloatArray
) = GL20.glVertexAttrib2fv(location, value)

actual inline fun GLHandle.glVertexAttrib3fv(
    location: Int,
    value: FloatArray
) = GL20.glVertexAttrib3fv(location, value)

actual inline fun GLHandle.glVertexAttrib4fv(
    location: Int,
    value: FloatArray
) = GL20.glVertexAttrib4fv(location, value)

actual inline fun GLHandle.glCheckFramebufferStatus(
    target: GLEnum
) = GL30.glCheckFramebufferStatus(target)

actual inline fun GLHandle.glDrawBuffers(
    bufs: IntArray
) = GL20.glDrawBuffers(bufs)

actual inline fun GLHandle.glGetShaderInfoLog(
    shader: GLShader
): String? = GL20.glGetShaderInfoLog(shader)

actual inline fun GLHandle.glGetProgramInfoLog(
    program: GLProgram
): String? = GL20.glGetProgramInfoLog(program)

actual inline fun GLHandle.glGetProgramb(
    program: GLProgram,
    pname: GLEnum
): Boolean = GL20.glGetProgrami(program, pname) == GL11.GL_TRUE

actual inline fun GLHandle.glShaderSource(
    shader: GLShader,
    string: String
) = GL20.glShaderSource(shader, string)

actual inline fun GLHandle.glCompileShader(
    shader: GLShader
) = GL20.glCompileShader(shader)

actual inline fun GLHandle.glAttachShader(
    program: GLProgram,
    shader: GLShader
) = GL20.glAttachShader(program, shader)

actual inline fun GLHandle.glLinkProgram(
    program: GLProgram
) = GL20.glLinkProgram(program)

actual inline fun GLHandle.glCreateShader(
    type: Int
) = GL20.glCreateShader(type)

actual inline fun GLHandle.glCreateProgram(
) = GL20.glCreateProgram()

actual inline fun GLHandle.glGetUniformLocation(
    program: GLProgram,
    name: String
) = GL20.glGetUniformLocation(program, name)

actual inline fun GLHandle.glDeleteShader(
    shader: GLShader
) = GL20.glDeleteShader(shader)

actual inline fun GLHandle.glDetachShader(
    program: GLProgram,
    shader: GLShader
) = GL20.glDetachShader(program, shader)
