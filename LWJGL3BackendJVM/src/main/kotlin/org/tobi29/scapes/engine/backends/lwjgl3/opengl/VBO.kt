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

package org.tobi29.scapes.engine.backends.lwjgl3.opengl

import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.GraphicsObjectSupplier
import org.tobi29.scapes.engine.graphics.ModelAttribute
import org.tobi29.scapes.engine.graphics.VertexType
import org.tobi29.math.FastMath
import org.tobi29.stdex.assert
import org.tobi29.io.ByteViewE
import org.tobi29.io.ByteViewRO
import kotlin.math.round

internal class VBO(val gos: GraphicsObjectSupplier,
                   attributes: List<ModelAttribute>,
                   length: Int) {
    private val stride: Int
    private val attributes = ArrayList<ModelAttributeData>()
    private var data: ByteViewRO? = null
    private var vertexID = 0
    private var stored = false

    init {
        var stride = 0
        for (attribute in attributes) {
            if (attribute.length != length * attribute.size) {
                throw IllegalArgumentException(
                        "Inconsistent attribute data length")
            }
            this.attributes.add(ModelAttributeData(attribute, stride))
            attribute.offset = stride
            val size = attribute.size * attribute.vertexType.bytes
            stride += (size - 1 or 0x03) + 1
        }
        this.stride = stride
        val vertexBuffer = gos.container.allocateNative(length * stride)
        attributes.forEach { addToBuffer(it, length, vertexBuffer) }
        data = vertexBuffer
    }

    fun stride(): Int {
        return stride
    }

    fun replaceBuffer(gl: GL,
                      buffer: ByteViewRO) {
        gl.check()
        data = buffer
        if (!stored) return
        glBindBuffer(GL_ARRAY_BUFFER, vertexID)
        glBufferData(GL_ARRAY_BUFFER, buffer.size, GL_STREAM_DRAW)
        glBufferSubData(GL_ARRAY_BUFFER, 0, buffer)
    }

    private fun storeAttribute(gl: GL,
                               attribute: ModelAttributeData) {
        gl.check()
        glEnableVertexAttribArray(attribute.id)
        if (attribute.integer) {
            when (attribute.vertexType) {
                VertexType.FLOAT -> glVertexAttribIPointer(attribute.id,
                        attribute.size,
                        GL_FLOAT, stride, attribute.offset)
                VertexType.HALF_FLOAT -> glVertexAttribIPointer(
                        attribute.id, attribute.size,
                        GL_HALF_FLOAT, stride, attribute.offset)
                VertexType.BYTE -> glVertexAttribIPointer(attribute.id,
                        attribute.size,
                        GL_BYTE, stride, attribute.offset)
                VertexType.UNSIGNED_BYTE -> glVertexAttribIPointer(
                        attribute.id, attribute.size,
                        GL_UNSIGNED_BYTE, stride,
                        attribute.offset)
                VertexType.SHORT -> glVertexAttribIPointer(attribute.id,
                        attribute.size,
                        GL_SHORT, stride, attribute.offset)
                VertexType.UNSIGNED_SHORT -> glVertexAttribIPointer(
                        attribute.id, attribute.size,
                        GL_UNSIGNED_SHORT, stride,
                        attribute.offset)
                else -> throw IllegalArgumentException("Unknown vertex type!")
            }
        } else {
            when (attribute.vertexType) {
                VertexType.FLOAT -> glVertexAttribPointer(attribute.id,
                        attribute.size,
                        GL_FLOAT, attribute.normalized, stride,
                        attribute.offset)
                VertexType.HALF_FLOAT -> glVertexAttribPointer(
                        attribute.id, attribute.size,
                        GL_HALF_FLOAT, attribute.normalized, stride,
                        attribute.offset)
                VertexType.BYTE -> glVertexAttribPointer(attribute.id,
                        attribute.size,
                        GL_BYTE, attribute.normalized, stride,
                        attribute.offset)
                VertexType.UNSIGNED_BYTE -> glVertexAttribPointer(
                        attribute.id, attribute.size,
                        GL_UNSIGNED_BYTE, attribute.normalized, stride,
                        attribute.offset)
                VertexType.SHORT -> glVertexAttribPointer(attribute.id,
                        attribute.size,
                        GL_SHORT, attribute.normalized, stride,
                        attribute.offset)
                VertexType.UNSIGNED_SHORT -> glVertexAttribPointer(
                        attribute.id, attribute.size,
                        GL_UNSIGNED_SHORT, attribute.normalized,
                        stride, attribute.offset)
                else -> throw IllegalArgumentException("Unknown vertex type!")
            }
        }
        glVertexAttribDivisor(attribute.id, attribute.divisor)
    }

    private fun addToBuffer(attribute: ModelAttribute,
                            vertices: Int,
                            buffer: ByteViewE) {
        val floatArray = attribute.floatArray
        if (floatArray == null) {
            val byteArray = attribute.byteArray ?: throw IllegalArgumentException(
                    "Attribute contains no data")
            when (attribute.vertexType) {
                VertexType.BYTE, VertexType.UNSIGNED_BYTE ->
                    buffer.storeBytes({ byteArray[it].toByte() },
                            vertices, attribute.offset, attribute.size, stride)
                VertexType.SHORT, VertexType.UNSIGNED_SHORT ->
                    buffer.storeShorts({ byteArray[it].toShort() },
                            vertices, attribute.offset, attribute.size, stride)
                else -> throw IllegalArgumentException(
                        "Invalid array in vao attribute!")
            }
        } else {
            when (attribute.vertexType) {
                VertexType.FLOAT ->
                    buffer.storeFloats({ floatArray[it] },
                            vertices, attribute.offset, attribute.size, stride)
                VertexType.HALF_FLOAT ->
                    buffer.storeShorts(
                            { FastMath.convertFloatToHalf(floatArray[it]) },
                            vertices, attribute.offset, attribute.size, stride)
                VertexType.BYTE -> if (attribute.normalized) {
                    buffer.storeBytes(
                            { round(floatArray[it] * 127.0f).toByte() },
                            vertices, attribute.offset, attribute.size, stride)
                } else {
                    buffer.storeBytes(
                            { round(floatArray[it]).toByte() },
                            vertices, attribute.offset, attribute.size, stride)
                }
                VertexType.UNSIGNED_BYTE -> if (attribute.normalized) {
                    buffer.storeBytes(
                            { round(floatArray[it] * 255.0f).toByte() },
                            vertices, attribute.offset, attribute.size, stride)
                } else {
                    buffer.storeBytes(
                            { round(floatArray[it]).toByte() },
                            vertices, attribute.offset, attribute.size, stride)
                }
                VertexType.SHORT -> if (attribute.normalized) {
                    buffer.storeShorts(
                            { round(floatArray[it] * 32768.0f).toShort() },
                            vertices, attribute.offset, attribute.size, stride)
                } else {
                    buffer.storeShorts(
                            { round(floatArray[it]).toShort() },
                            vertices, attribute.offset, attribute.size, stride)
                }
                VertexType.UNSIGNED_SHORT -> if (attribute.normalized) {
                    buffer.storeShorts(
                            { round(floatArray[it] * 65535.0f).toShort() },
                            vertices, attribute.offset, attribute.size, stride)
                } else {
                    buffer.storeShorts(
                            { round(floatArray[it]).toShort() },
                            vertices, attribute.offset, attribute.size, stride)
                }
                else -> throw IllegalArgumentException(
                        "Invalid array in vao attribute!")
            }
        }
    }

    fun canStore(): Boolean {
        return data != null
    }

    fun store(gl: GL,
              weak: Boolean) {
        assert { !stored }
        val data = data ?: throw IllegalStateException(
                "VBO cannot be stored anymore")
        stored = true
        gl.check()
        vertexID = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vertexID)
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
        attributes.forEach { storeAttribute(gl, it) }
        if (weak) {
            this.data = null
        }
    }

    fun dispose(gl: GL) {
        assert { stored }
        stored = false
        gl.check()
        glDeleteBuffers(vertexID)
    }

    fun reset() {
        stored = false
    }

    private class ModelAttributeData(attribute: ModelAttribute,
                                     val offset: Int) {
        val vertexType = attribute.vertexType
        val id = attribute.id
        val size = attribute.size
        val divisor = attribute.divisor
        val normalized = attribute.normalized
        val integer = attribute.byteArray != null
    }
}

private inline fun ByteViewE.storeBytes(source: (Int) -> Byte,
                                        vertices: Int,
                                        offset: Int,
                                        size: Int,
                                        stride: Int) =
        store({ i, j -> setByte(j, source(i)) }, vertices, offset, size,
                stride, 1)

private inline fun ByteViewE.storeShorts(source: (Int) -> Short,
                                         vertices: Int,
                                         offset: Int,
                                         size: Int,
                                         stride: Int) =
        store({ i, j -> setShort(j, source(i)) }, vertices, offset, size,
                stride, 2)

private inline fun ByteViewE.storeInts(source: (Int) -> Int,
                                       vertices: Int,
                                       offset: Int,
                                       size: Int,
                                       stride: Int) =
        store({ i, j -> setInt(j, source(i)) }, vertices, offset, size,
                stride, 4)

private inline fun ByteViewE.storeFloats(source: (Int) -> Float,
                                         vertices: Int,
                                         offset: Int,
                                         size: Int,
                                         stride: Int) =
        store({ i, j -> setFloat(j, source(i)) }, vertices, offset, size,
                stride, 4)

private inline fun store(copy: (Int, Int) -> Unit,
                         vertices: Int,
                         offset: Int,
                         size: Int,
                         stride: Int,
                         typeStride: Int) {
    for (i in 0 until vertices) {
        val o = i * size
        var k = offset + i * stride
        for (j in o until o + size) {
            copy(j, k)
            k += typeStride
        }
    }
}
