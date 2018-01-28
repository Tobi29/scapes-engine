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

import org.khronos.webgl.Int32Array
import org.khronos.webgl.Uint8Array
import org.tobi29.io.*
import org.tobi29.stdex.asArray
import org.tobi29.stdex.asTypedArray
import org.tobi29.stdex.copy
import org.tobi29.stdex.toIntClamped
import org.khronos.webgl.WebGLRenderingContext as WGL1
import org.khronos.webgl2.WebGL2RenderingContext as WGL2

actual fun GLESHandle.byteView(capacity: Int): ByteViewE =
    ByteArray(capacity).run { if (NATIVE_ENDIAN == BIG_ENDIAN) viewBE else viewLE }

actual inline fun GLESHandle.glBindFramebuffer(
    target: GLEnum,
    framebuffer: GLFBO
) = wgl.bindFramebuffer(target, framebuffer.c)

actual inline fun GLESHandle.glGenFramebuffers() = wgl.createFramebuffer().c

actual inline fun GLESHandle.glDeleteFramebuffers(framebuffer: GLFBO) =
    wgl.deleteFramebuffer(framebuffer.c)

actual inline fun GLESHandle.glClear(mask: Int) = wgl.clear(mask)

actual inline fun GLESHandle.glClearColor(
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float
) = wgl.clearColor(red, green, blue, alpha)

actual inline fun GLESHandle.glDeleteProgram(program: GLProgram) =
    wgl.deleteProgram(program.c)

actual inline fun GLESHandle.glUseProgram(program: GLProgram) =
    wgl.useProgram(program.c)

actual inline fun GLESHandle.glGenTextures() = wgl.createTexture().c

actual inline fun GLESHandle.glBindTexture(
    target: GLEnum,
    texture: GLTexture
) = wgl.bindTexture(target, texture.c)

actual inline fun GLESHandle.glDeleteTextures(texture: GLTexture) =
    wgl.deleteTexture(texture.c)

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
) {
    val data = pixels?.asDataView()
    val buffer = when (type) {
        GL_UNSIGNED_BYTE -> data?.let {
            Uint8Array(data.buffer, data.byteOffset, data.byteLength)
        }
        else -> throw IllegalArgumentException("Unknown type")
    }
    wgl.texImage2D(
        target, level, internalformat, width, height, border,
        format, type, buffer
    )
}

actual inline fun GLESHandle.glFramebufferTexture2D(
    target: GLEnum,
    attachment: Int,
    textarget: GLEnum,
    texture: GLTexture,
    level: Int
) = wgl.framebufferTexture2D(target, attachment, textarget, texture.c, level)

actual inline fun GLESHandle.glTexParameteri(
    target: GLEnum,
    pname: GLEnum,
    param: Int
) = wgl.texParameteri(target, pname, param)

actual inline fun GLESHandle.glBindVertexArray(array: GLVAO) =
    wgl.bindVertexArray(array.c)

actual inline fun GLESHandle.glGenVertexArrays() = wgl.createVertexArray().c

actual inline fun GLESHandle.glDeleteVertexArrays(array: GLVAO) =
    wgl.deleteVertexArray(array.c)

actual inline fun GLESHandle.glDrawArrays(
    mode: Int,
    first: Int,
    count: Int
) = wgl.drawArrays(mode, first, count)

actual inline fun GLESHandle.glDrawArraysInstanced(
    mode: Int,
    first: Int,
    count: Int,
    primcount: Int
) = wgl.drawArraysInstanced(mode, first, count, primcount)

actual inline fun GLESHandle.glDrawElements(
    mode: Int,
    count: Int,
    type: Int,
    indices: Long
) = wgl.drawElements(mode, count, type, indices.toIntClamped())

actual inline fun GLESHandle.glGenBuffers() = wgl.createBuffer().c

actual inline fun GLESHandle.glDeleteBuffers(buffer: GLVBO) =
    wgl.deleteBuffer(buffer.c)

actual inline fun GLESHandle.glBindBuffer(
    target: GLEnum,
    buffer: GLVBO
) = wgl.bindBuffer(target, buffer.c)

actual inline fun GLESHandle.glBufferData(
    target: GLEnum,
    size: Int,
    usage: Int
) = wgl.bufferData(target, size, usage)

actual inline fun GLESHandle.glBufferData(
    target: GLEnum,
    data: ByteViewRO,
    usage: Int
) = wgl.bufferData(target, data.asDataView(), usage)

actual inline fun GLESHandle.glBufferSubData(
    target: GLEnum,
    offset: Long,
    data: ByteViewRO
) = wgl.bufferSubData(target, offset.toInt(), data.asDataView())

actual inline fun GLESHandle.glVertexAttribDivisor(
    index: Int,
    divisor: Int
) = wgl.vertexAttribDivisor(index, divisor)

actual inline fun GLESHandle.glEnableVertexAttribArray(index: Int) =
    wgl.enableVertexAttribArray(index)

actual inline fun GLESHandle.glVertexAttribPointer(
    index: Int,
    size: Int,
    type: Int,
    normalized: Boolean,
    stride: Int,
    pointer: Int
) = wgl.vertexAttribPointer(index, size, type, normalized, stride, pointer)

actual inline fun GLESHandle.glVertexAttribIPointer(
    index: Int,
    size: Int,
    type: Int,
    stride: Int,
    pointer: Int
) = wgl.vertexAttribIPointer(index, size, type, stride, pointer)

actual inline fun GLESHandle.glEnable(target: GLEnum) = wgl.enable(target)

actual inline fun GLESHandle.glDisable(target: GLEnum) = wgl.disable(target)

actual inline fun GLESHandle.glDepthMask(flag: Boolean) = wgl.depthMask(flag)

actual inline fun GLESHandle.glDepthFunc(func: Int) = wgl.depthFunc(func)

actual inline fun GLESHandle.glScissor(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) = wgl.scissor(x, y, width, height)

actual inline fun GLESHandle.glViewport(
    x: Int,
    y: Int,
    w: Int,
    h: Int
) = wgl.viewport(x, y, w, h)

actual inline fun GLESHandle.glGetIntegerv(
    pname: GLEnum,
    params: IntArray
): Unit = (wgl.getParameter(pname) as Int32Array).let {
    copy(params, it.asArray())
}

actual inline fun GLESHandle.glReadBuffer(
    src: GLEnum
) = wgl.readBuffer(src)

actual inline fun GLESHandle.glReadPixels(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    format: Int,
    type: Int,
    pixels: ByteView
) = wgl.readPixels(x, y, width, height, format, type, pixels.asDataView())

actual inline fun GLESHandle.glBlendFunc(
    sfactor: Int,
    dfactor: Int
) = wgl.blendFunc(sfactor, dfactor)

actual inline fun GLESHandle.glTexSubImage2D(
    target: GLEnum,
    level: Int,
    xoffset: Int,
    yoffset: Int,
    width: Int,
    height: Int,
    format: Int,
    type: Int,
    pixels: ByteViewRO
) {
    val data = pixels?.asDataView()
    val buffer = when (type) {
        GL_UNSIGNED_BYTE -> data?.let {
            Uint8Array(data.buffer, data.byteOffset, data.byteLength)
        }
        else -> throw IllegalArgumentException("Unknown type")
    }
    wgl.texSubImage2D(
        target, level, xoffset, yoffset, width, height,
        format, type, buffer
    )
}

actual inline fun GLESHandle.glGetError() = wgl.getError()

actual inline fun GLESHandle.glActiveTexture(texture: Int) =
    wgl.activeTexture(texture)

actual inline fun GLESHandle.glUniform1f(
    location: GLUniform,
    v0: Float
) = wgl.uniform1f(location.c, v0)

actual inline fun GLESHandle.glUniform2f(
    location: GLUniform,
    v0: Float,
    v1: Float
) = wgl.uniform2f(location.c, v0, v1)

actual inline fun GLESHandle.glUniform3f(
    location: GLUniform,
    v0: Float,
    v1: Float,
    v2: Float
) = wgl.uniform3f(location.c, v0, v1, v2)

actual inline fun GLESHandle.glUniform4f(
    location: GLUniform,
    v0: Float,
    v1: Float,
    v2: Float,
    v3: Float
) = wgl.uniform4f(location.c, v0, v1, v2, v3)

actual inline fun GLESHandle.glUniform1i(
    location: GLUniform,
    v0: Int
) = wgl.uniform1i(location.c, v0)

actual inline fun GLESHandle.glUniform2i(
    location: GLUniform,
    v0: Int,
    v1: Int
) = wgl.uniform2i(location.c, v0, v1)

actual inline fun GLESHandle.glUniform3i(
    location: GLUniform,
    v0: Int,
    v1: Int,
    v2: Int
) = wgl.uniform3i(location.c, v0, v1, v2)

actual inline fun GLESHandle.glUniform4i(
    location: GLUniform,
    v0: Int,
    v1: Int,
    v2: Int,
    v3: Int
) = wgl.uniform4i(location.c, v0, v1, v2, v3)

actual inline fun GLESHandle.glUniform1fv(
    location: GLUniform,
    value: FloatArray
) = wgl.uniform1fv(location.c, value.asTypedArray())

actual inline fun GLESHandle.glUniform2fv(
    location: GLUniform,
    value: FloatArray
) = wgl.uniform2fv(location.c, value.asTypedArray())

actual inline fun GLESHandle.glUniform3fv(
    location: GLUniform,
    value: FloatArray
) = wgl.uniform3fv(location.c, value.asTypedArray())

actual inline fun GLESHandle.glUniform4fv(
    location: GLUniform,
    value: FloatArray
) = wgl.uniform4fv(location.c, value.asTypedArray())

actual inline fun GLESHandle.glUniform1iv(
    location: GLUniform,
    value: IntArray
) = wgl.uniform1iv(location.c, value.asTypedArray())

actual inline fun GLESHandle.glUniform2iv(
    location: GLUniform,
    value: IntArray
) =
    wgl.uniform2iv(location.c, value.asTypedArray())

actual inline fun GLESHandle.glUniform3iv(
    location: GLUniform,
    value: IntArray
) = wgl.uniform3iv(location.c, value.asTypedArray())

actual inline fun GLESHandle.glUniform4iv(
    location: GLUniform,
    value: IntArray
) = wgl.uniform4iv(location.c, value.asTypedArray())

actual inline fun GLESHandle.glUniformMatrix2fv(
    location: GLUniform,
    transpose: Boolean,
    value: FloatArray
) = wgl.uniformMatrix2fv(location.c, transpose, value.asTypedArray())

actual inline fun GLESHandle.glUniformMatrix3fv(
    location: GLUniform,
    transpose: Boolean,
    value: FloatArray
) = wgl.uniformMatrix3fv(location.c, transpose, value.asTypedArray())

actual inline fun GLESHandle.glUniformMatrix4fv(
    location: GLUniform,
    transpose: Boolean,
    value: FloatArray
) = wgl.uniformMatrix4fv(location.c, transpose, value.asTypedArray())

actual inline fun GLESHandle.glVertexAttrib1f(
    location: Int,
    v0: Float
) = wgl.vertexAttrib1f(location, v0)

actual inline fun GLESHandle.glVertexAttrib2f(
    location: Int,
    v0: Float,
    v1: Float
) = wgl.vertexAttrib2f(location, v0, v1)

actual inline fun GLESHandle.glVertexAttrib3f(
    location: Int,
    v0: Float,
    v1: Float,
    v2: Float
) = wgl.vertexAttrib3f(location, v0, v1, v2)

actual inline fun GLESHandle.glVertexAttrib4f(
    location: Int,
    v0: Float,
    v1: Float,
    v2: Float,
    v3: Float
) = wgl.vertexAttrib4f(location, v0, v1, v2, v3)

actual inline fun GLESHandle.glVertexAttrib1fv(
    location: Int,
    value: FloatArray
) = wgl.vertexAttrib1fv(location, value)

actual inline fun GLESHandle.glVertexAttrib2fv(
    location: Int,
    value: FloatArray
) = wgl.vertexAttrib2fv(location, value)

actual inline fun GLESHandle.glVertexAttrib3fv(
    location: Int,
    value: FloatArray
) = wgl.vertexAttrib3fv(location, value)

actual inline fun GLESHandle.glVertexAttrib4fv(
    location: Int,
    value: FloatArray
) = wgl.vertexAttrib4fv(location, value)

actual inline fun GLESHandle.glCheckFramebufferStatus(
    target: GLEnum
) = wgl.checkFramebufferStatus(target)

// TODO: Optimize
actual inline fun GLESHandle.glDrawBuffers(
    bufs: IntArray
) = wgl.drawBuffers(Array(bufs.size) { bufs[it] })

actual inline fun GLESHandle.glGetShaderInfoLog(
    shader: GLShader
) = wgl.getShaderInfoLog(shader.c)

actual inline fun GLESHandle.glGetProgramInfoLog(
    program: GLProgram
) = wgl.getProgramInfoLog(program.c)

actual inline fun GLESHandle.glGetProgramb(
    program: GLProgram,
    pname: GLEnum
): Boolean = wgl.getProgramParameter(program.c, pname) as Boolean

actual inline fun GLESHandle.glShaderSource(
    shader: GLShader,
    string: String
) = wgl.shaderSource(shader.c, string)

actual inline fun GLESHandle.glCompileShader(
    shader: GLShader
) = wgl.compileShader(shader.c)

actual inline fun GLESHandle.glAttachShader(
    program: GLProgram,
    shader: GLShader
) = wgl.attachShader(program.c, shader.c)

actual inline fun GLESHandle.glLinkProgram(
    program: GLProgram
) = wgl.linkProgram(program.c)

actual inline fun GLESHandle.glCreateShader(
    type: Int
) = wgl.createShader(type).c

actual inline fun GLESHandle.glCreateProgram(
) = wgl.createProgram().c

actual inline fun GLESHandle.glGetUniformLocation(
    program: GLProgram,
    name: String
) = wgl.getUniformLocation(program.c, name).c

actual inline fun GLESHandle.glDeleteShader(
    shader: GLShader
) = wgl.deleteShader(shader.c)

actual inline fun GLESHandle.glDetachShader(
    program: GLProgram,
    shader: GLShader
) = wgl.detachShader(program.c, shader.c)
