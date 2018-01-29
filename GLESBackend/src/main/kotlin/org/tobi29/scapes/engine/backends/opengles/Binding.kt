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

@file:Suppress("NOTHING_TO_INLINE", "unused")

package org.tobi29.scapes.engine.backends.opengles

import org.tobi29.io.ByteView
import org.tobi29.io.ByteViewE
import org.tobi29.io.ByteViewRO


expect fun GLESHandle.byteView(capacity: Int): ByteViewE

expect fun GLESHandle.glBindFramebuffer(target: GLEnum, framebuffer: GLFBO)

expect fun GLESHandle.glGenFramebuffers(): GLFBO

expect fun GLESHandle.glDeleteFramebuffers(framebuffer: GLFBO)

expect fun GLESHandle.glClear(mask: GLEnum)

expect fun GLESHandle.glClearColor(
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float
)

expect fun GLESHandle.glDeleteProgram(program: GLProgram)

expect fun GLESHandle.glUseProgram(program: GLProgram)

expect fun GLESHandle.glGenTextures(): GLTexture

expect fun GLESHandle.glBindTexture(target: GLEnum, texture: GLTexture)

expect fun GLESHandle.glDeleteTextures(texture: GLTexture)

expect fun GLESHandle.glTexImage2D(
    target: GLEnum,
    level: Int,
    internalformat: Int,
    width: Int,
    height: Int,
    border: Int,
    format: GLEnum,
    type: GLEnum,
    pixels: ByteViewRO?
)

expect fun GLESHandle.glFramebufferTexture2D(
    target: GLEnum,
    attachment: GLEnum,
    textarget: GLEnum,
    texture: GLTexture,
    level: Int
)

expect fun GLESHandle.glTexParameteri(
    target: GLEnum,
    pname: GLEnum,
    param: Int
)

expect fun GLESHandle.glBindVertexArray(array: GLVAO)

expect fun GLESHandle.glGenVertexArrays(): GLVAO

expect fun GLESHandle.glDeleteVertexArrays(array: GLVAO)

expect fun GLESHandle.glDrawArrays(mode: GLEnum, first: Int, count: Int)

expect fun GLESHandle.glDrawArraysInstanced(
    mode: GLEnum,
    first: Int,
    count: Int,
    primcount: Int
)

expect fun GLESHandle.glDrawElements(
    mode: GLEnum,
    count: Int,
    type: Int,
    indices: Long
)

expect fun GLESHandle.glGenBuffers(): GLVBO

expect fun GLESHandle.glDeleteBuffers(buffer: GLVBO)

expect fun GLESHandle.glBindBuffer(
    target: GLEnum,
    buffer: GLVBO
)

expect fun GLESHandle.glBufferData(
    target: GLEnum,
    size: Int,
    usage: Int
)

expect fun GLESHandle.glBufferData(
    target: GLEnum,
    data: ByteViewRO,
    usage: Int
)

expect fun GLESHandle.glBufferSubData(
    target: GLEnum,
    offset: Long,
    data: ByteViewRO
)

expect fun GLESHandle.glVertexAttribDivisor(
    index: Int,
    divisor: Int
)

expect fun GLESHandle.glEnableVertexAttribArray(index: Int)

expect fun GLESHandle.glVertexAttribPointer(
    index: Int,
    size: Int,
    type: Int,
    normalized: Boolean,
    stride: Int,
    pointer: Int
)

expect fun GLESHandle.glVertexAttribIPointer(
    index: Int,
    size: Int,
    type: Int,
    stride: Int,
    pointer: Int
)

expect fun GLESHandle.glEnable(target: GLEnum)

expect fun GLESHandle.glDisable(target: GLEnum)

expect fun GLESHandle.glDepthMask(flag: Boolean)

expect fun GLESHandle.glDepthFunc(func: Int)

expect fun GLESHandle.glScissor(
    x: Int,
    y: Int,
    width: Int,
    height: Int
)

expect fun GLESHandle.glViewport(
    x: Int,
    y: Int,
    w: Int,
    h: Int
)

expect fun GLESHandle.glGetIntegerv(
    pname: GLEnum,
    params: IntArray
)

expect fun GLESHandle.glReadBuffer(
    src: GLEnum
)

expect fun GLESHandle.glReadPixels(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    format: GLEnum,
    type: GLEnum,
    pixels: ByteView
)

expect fun GLESHandle.glBlendFunc(
    sfactor: Int,
    dfactor: Int
)

expect fun GLESHandle.glTexSubImage2D(
    target: GLEnum,
    level: Int,
    xoffset: Int,
    yoffset: Int,
    width: Int,
    height: Int,
    format: GLEnum,
    type: GLEnum,
    pixels: ByteViewRO
)

expect fun GLESHandle.glGetError(): GLEnum

expect fun GLESHandle.glActiveTexture(texture: Int)

expect fun GLESHandle.glUniform1f(
    location: GLUniform,
    v0: Float
)

expect fun GLESHandle.glUniform2f(
    location: GLUniform,
    v0: Float,
    v1: Float
)

expect fun GLESHandle.glUniform3f(
    location: GLUniform,
    v0: Float,
    v1: Float,
    v2: Float
)

expect fun GLESHandle.glUniform4f(
    location: GLUniform,
    v0: Float,
    v1: Float,
    v2: Float,
    v3: Float
)

expect fun GLESHandle.glUniform1i(
    location: GLUniform,
    v0: Int
)

expect fun GLESHandle.glUniform2i(
    location: GLUniform,
    v0: Int,
    v1: Int
)

expect fun GLESHandle.glUniform3i(
    location: GLUniform,
    v0: Int,
    v1: Int,
    v2: Int
)

expect fun GLESHandle.glUniform4i(
    location: GLUniform,
    v0: Int,
    v1: Int,
    v2: Int,
    v3: Int
)

expect fun GLESHandle.glUniform1fv(
    location: GLUniform,
    value: FloatArray
)

expect fun GLESHandle.glUniform2fv(
    location: GLUniform,
    value: FloatArray
)

expect fun GLESHandle.glUniform3fv(
    location: GLUniform,
    value: FloatArray
)

expect fun GLESHandle.glUniform4fv(
    location: GLUniform,
    value: FloatArray
)

expect fun GLESHandle.glUniform1iv(
    location: GLUniform,
    value: IntArray
)

expect fun GLESHandle.glUniform2iv(
    location: GLUniform,
    value: IntArray
)

expect fun GLESHandle.glUniform3iv(
    location: GLUniform,
    value: IntArray
)

expect fun GLESHandle.glUniform4iv(
    location: GLUniform,
    value: IntArray
)

expect fun GLESHandle.glUniformMatrix2fv(
    location: GLUniform,
    transpose: Boolean,
    value: FloatArray
)

expect fun GLESHandle.glUniformMatrix3fv(
    location: GLUniform,
    transpose: Boolean,
    value: FloatArray
)

expect fun GLESHandle.glUniformMatrix4fv(
    location: GLUniform,
    transpose: Boolean,
    value: FloatArray
)

expect fun GLESHandle.glVertexAttrib1f(
    location: Int,
    v0: Float
)

expect fun GLESHandle.glVertexAttrib2f(
    location: Int,
    v0: Float,
    v1: Float
)

expect fun GLESHandle.glVertexAttrib3f(
    location: Int,
    v0: Float,
    v1: Float,
    v2: Float
)

expect fun GLESHandle.glVertexAttrib4f(
    location: Int,
    v0: Float,
    v1: Float,
    v2: Float,
    v3: Float
)

expect fun GLESHandle.glVertexAttrib1fv(
    location: Int,
    value: FloatArray
)

expect fun GLESHandle.glVertexAttrib2fv(
    location: Int,
    value: FloatArray
)

expect fun GLESHandle.glVertexAttrib3fv(
    location: Int,
    value: FloatArray
)

expect fun GLESHandle.glVertexAttrib4fv(
    location: Int,
    value: FloatArray
)

expect fun GLESHandle.glCheckFramebufferStatus(
    target: GLEnum
): GLEnum

expect fun GLESHandle.glDrawBuffers(
    bufs: IntArray
)

expect fun GLESHandle.glGetShaderInfoLog(
    shader: GLShader
): String?

expect fun GLESHandle.glGetProgramInfoLog(
    program: GLProgram
): String?

expect fun GLESHandle.glGetProgramb(
    program: GLProgram,
    pname: GLEnum
): Boolean

expect fun GLESHandle.glShaderSource(
    shader: GLShader,
    string: String
)

expect fun GLESHandle.glCompileShader(
    shader: GLShader
)

expect fun GLESHandle.glAttachShader(
    program: GLProgram,
    shader: GLShader
)

expect fun GLESHandle.glLinkProgram(
    program: GLProgram
)

expect fun GLESHandle.glCreateShader(
    type: GLEnum
): GLShader

expect fun GLESHandle.glCreateProgram(
): GLProgram

expect fun GLESHandle.glGetUniformLocation(
    program: GLProgram,
    name: String
): GLUniform

expect fun GLESHandle.glDeleteShader(
    shader: GLShader
)

expect fun GLESHandle.glDetachShader(
    program: GLProgram,
    shader: GLShader
)
