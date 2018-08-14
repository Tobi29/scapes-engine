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

import org.tobi29.arrays.Bytes
import org.tobi29.arrays.BytesRO
import org.tobi29.io.ByteViewE

expect fun GLHandle.byteView(capacity: Int): ByteViewE

expect fun GLHandle.glBindFramebuffer(target: GLEnum, framebuffer: GLFBO)

expect fun GLHandle.glGenFramebuffers(): GLFBO

expect fun GLHandle.glDeleteFramebuffers(framebuffer: GLFBO)

expect fun GLHandle.glClear(mask: GLEnum)

expect fun GLHandle.glClearColor(
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float
)

expect fun GLHandle.glDeleteProgram(program: GLProgram)

expect fun GLHandle.glUseProgram(program: GLProgram)

expect fun GLHandle.glGenTextures(): GLTexture

expect fun GLHandle.glBindTexture(target: GLEnum, texture: GLTexture)

expect fun GLHandle.glDeleteTextures(texture: GLTexture)

expect fun GLHandle.glTexImage2D(
    target: GLEnum,
    level: Int,
    internalformat: Int,
    width: Int,
    height: Int,
    border: Int,
    format: GLEnum,
    type: GLEnum,
    pixels: BytesRO?
)

expect fun GLHandle.glFramebufferTexture2D(
    target: GLEnum,
    attachment: GLEnum,
    textarget: GLEnum,
    texture: GLTexture,
    level: Int
)

expect fun GLHandle.glTexParameteri(
    target: GLEnum,
    pname: GLEnum,
    param: Int
)

expect fun GLHandle.glBindVertexArray(array: GLVAO)

expect fun GLHandle.glGenVertexArrays(): GLVAO

expect fun GLHandle.glDeleteVertexArrays(array: GLVAO)

expect fun GLHandle.glDrawArrays(mode: GLEnum, first: Int, count: Int)

expect fun GLHandle.glDrawArraysInstanced(
    mode: GLEnum,
    first: Int,
    count: Int,
    primcount: Int
)

expect fun GLHandle.glDrawElements(
    mode: GLEnum,
    count: Int,
    type: Int,
    indices: Int
)

expect fun GLHandle.glGenBuffers(): GLVBO

expect fun GLHandle.glDeleteBuffers(buffer: GLVBO)

expect fun GLHandle.glBindBuffer(
    target: GLEnum,
    buffer: GLVBO
)

expect fun GLHandle.glBufferData(
    target: GLEnum,
    size: Int,
    usage: Int
)

expect fun GLHandle.glBufferData(
    target: GLEnum,
    data: BytesRO,
    usage: Int
)

expect fun GLHandle.glBufferSubData(
    target: GLEnum,
    offset: Int,
    data: BytesRO
)

expect fun GLHandle.glVertexAttribDivisor(
    index: Int,
    divisor: Int
)

expect fun GLHandle.glEnableVertexAttribArray(index: Int)

expect fun GLHandle.glVertexAttribPointer(
    index: Int,
    size: Int,
    type: Int,
    normalized: Boolean,
    stride: Int,
    pointer: Int
)

expect fun GLHandle.glVertexAttribIPointer(
    index: Int,
    size: Int,
    type: Int,
    stride: Int,
    pointer: Int
)

expect fun GLHandle.glEnable(target: GLEnum)

expect fun GLHandle.glDisable(target: GLEnum)

expect fun GLHandle.glDepthMask(flag: Boolean)

expect fun GLHandle.glPolygonMode(
    face: Int,
    mode: Int
)

expect fun GLHandle.glDepthFunc(func: Int)

expect fun GLHandle.glScissor(
    x: Int,
    y: Int,
    width: Int,
    height: Int
)

expect fun GLHandle.glViewport(
    x: Int,
    y: Int,
    w: Int,
    h: Int
)

expect fun GLHandle.glGetIntegerv(
    pname: GLEnum,
    params: IntArray
)

expect fun GLHandle.glReadBuffer(src: Int)

expect fun GLHandle.glReadPixels(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    format: GLEnum,
    type: GLEnum,
    pixels: Bytes
)

expect fun GLHandle.glBlendFunc(
    sfactor: Int,
    dfactor: Int
)

expect fun GLHandle.glTexSubImage2D(
    target: GLEnum,
    level: Int,
    xoffset: Int,
    yoffset: Int,
    width: Int,
    height: Int,
    format: GLEnum,
    type: GLEnum,
    pixels: BytesRO
)

expect fun GLHandle.glGetTexImage(
    tex: Int,
    level: Int,
    format: Int,
    type: Int,
    pixels: Bytes
)

expect fun GLHandle.glGetError(): GLEnum

expect fun GLHandle.glActiveTexture(texture: Int)

expect fun GLHandle.glUniform1f(
    location: GLUniform,
    v0: Float
)

expect fun GLHandle.glUniform2f(
    location: GLUniform,
    v0: Float,
    v1: Float
)

expect fun GLHandle.glUniform3f(
    location: GLUniform,
    v0: Float,
    v1: Float,
    v2: Float
)

expect fun GLHandle.glUniform4f(
    location: GLUniform,
    v0: Float,
    v1: Float,
    v2: Float,
    v3: Float
)

expect fun GLHandle.glUniform1i(
    location: GLUniform,
    v0: Int
)

expect fun GLHandle.glUniform2i(
    location: GLUniform,
    v0: Int,
    v1: Int
)

expect fun GLHandle.glUniform3i(
    location: GLUniform,
    v0: Int,
    v1: Int,
    v2: Int
)

expect fun GLHandle.glUniform4i(
    location: GLUniform,
    v0: Int,
    v1: Int,
    v2: Int,
    v3: Int
)

expect fun GLHandle.glUniform1fv(
    location: GLUniform,
    value: FloatArray
)

expect fun GLHandle.glUniform2fv(
    location: GLUniform,
    value: FloatArray
)

expect fun GLHandle.glUniform3fv(
    location: GLUniform,
    value: FloatArray
)

expect fun GLHandle.glUniform4fv(
    location: GLUniform,
    value: FloatArray
)

expect fun GLHandle.glUniform1iv(
    location: GLUniform,
    value: IntArray
)

expect fun GLHandle.glUniform2iv(
    location: GLUniform,
    value: IntArray
)

expect fun GLHandle.glUniform3iv(
    location: GLUniform,
    value: IntArray
)

expect fun GLHandle.glUniform4iv(
    location: GLUniform,
    value: IntArray
)

expect fun GLHandle.glUniformMatrix2fv(
    location: GLUniform,
    transpose: Boolean,
    value: FloatArray
)

expect fun GLHandle.glUniformMatrix3fv(
    location: GLUniform,
    transpose: Boolean,
    value: FloatArray
)

expect fun GLHandle.glUniformMatrix4fv(
    location: GLUniform,
    transpose: Boolean,
    value: FloatArray
)

expect fun GLHandle.glVertexAttrib1f(
    location: Int,
    v0: Float
)

expect fun GLHandle.glVertexAttrib2f(
    location: Int,
    v0: Float,
    v1: Float
)

expect fun GLHandle.glVertexAttrib3f(
    location: Int,
    v0: Float,
    v1: Float,
    v2: Float
)

expect fun GLHandle.glVertexAttrib4f(
    location: Int,
    v0: Float,
    v1: Float,
    v2: Float,
    v3: Float
)

expect fun GLHandle.glVertexAttrib1fv(
    location: Int,
    value: FloatArray
)

expect fun GLHandle.glVertexAttrib2fv(
    location: Int,
    value: FloatArray
)

expect fun GLHandle.glVertexAttrib3fv(
    location: Int,
    value: FloatArray
)

expect fun GLHandle.glVertexAttrib4fv(
    location: Int,
    value: FloatArray
)

expect fun GLHandle.glCheckFramebufferStatus(
    target: GLEnum
): GLEnum

expect fun GLHandle.glDrawBuffers(
    bufs: IntArray
)

expect fun GLHandle.glGetShaderInfoLog(
    shader: GLShader
): String?

expect fun GLHandle.glGetProgramInfoLog(
    program: GLProgram
): String?

expect fun GLHandle.glGetProgramb(
    program: GLProgram,
    pname: GLEnum
): Boolean

expect fun GLHandle.glShaderSource(
    shader: GLShader,
    string: String
)

expect fun GLHandle.glCompileShader(
    shader: GLShader
)

expect fun GLHandle.glAttachShader(
    program: GLProgram,
    shader: GLShader
)

expect fun GLHandle.glLinkProgram(
    program: GLProgram
)

expect fun GLHandle.glCreateShader(
    type: GLEnum
): GLShader

expect fun GLHandle.glCreateProgram(
): GLProgram

expect fun GLHandle.glGetUniformLocation(
    program: GLProgram,
    name: String
): GLUniform

expect fun GLHandle.glDeleteShader(
    shader: GLShader
)

expect fun GLHandle.glDetachShader(
    program: GLProgram,
    shader: GLShader
)
