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

package org.tobi29.scapes.engine.backends.lwjgl3.opengles

import org.lwjgl.opengles.GLES20
import org.lwjgl.opengles.GLES30
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.ModelAttribute
import org.tobi29.scapes.engine.graphics.VertexType
import org.tobi29.scapes.engine.utils.math.FastMath
import org.tobi29.scapes.engine.utils.math.round
import java.nio.ByteBuffer
import java.util.*

internal class VBO(val engine: ScapesEngine,
                   attributes: List<ModelAttribute>,
                   length: Int) {
    private val stride: Int
    private val attributes = ArrayList<ModelAttributeData>()
    private var data: ByteBuffer? = null
    private var vertexID = 0
    private var stored = false

    init {
        var stride = 0
        for (attribute in attributes) {
            if (attribute.length() != length * attribute.size()) {
                throw IllegalArgumentException(
                        "Inconsistent attribute data length")
            }
            this.attributes.add(ModelAttributeData(attribute, stride))
            attribute.setOffset(stride)
            val size = attribute.size() * attribute.vertexType().bytes()
            stride += (size - 1 or 0x03) + 1
        }
        this.stride = stride
        val vertexBuffer = engine.allocate(length * stride)
        attributes.forEach { addToBuffer(it, length, vertexBuffer) }
        data = vertexBuffer
    }

    fun stride(): Int {
        return stride
    }

    fun replaceBuffer(gl: GL,
                      buffer: ByteBuffer) {
        assert(stored)
        gl.check()
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexID)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, buffer.capacity().toLong(),
                GLES20.GL_STREAM_DRAW)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, buffer)
    }

    private fun storeAttribute(gl: GL,
                               attribute: ModelAttributeData) {
        gl.check()
        GLES20.glEnableVertexAttribArray(attribute.id)
        if (attribute.integer) {
            when (attribute.vertexType) {
                VertexType.FLOAT -> GLES30.glVertexAttribIPointer(attribute.id,
                        attribute.size,
                        GLES20.GL_FLOAT, stride, attribute.offset.toLong())
                VertexType.HALF_FLOAT -> GLES30.glVertexAttribIPointer(
                        attribute.id, attribute.size,
                        GLES30.GL_HALF_FLOAT, stride, attribute.offset.toLong())
                VertexType.BYTE -> GLES30.glVertexAttribIPointer(attribute.id,
                        attribute.size,
                        GLES20.GL_BYTE, stride, attribute.offset.toLong())
                VertexType.UNSIGNED_BYTE -> GLES30.glVertexAttribIPointer(
                        attribute.id, attribute.size,
                        GLES20.GL_UNSIGNED_BYTE, stride,
                        attribute.offset.toLong())
                VertexType.SHORT -> GLES30.glVertexAttribIPointer(attribute.id,
                        attribute.size,
                        GLES20.GL_SHORT, stride, attribute.offset.toLong())
                VertexType.UNSIGNED_SHORT -> GLES30.glVertexAttribIPointer(
                        attribute.id, attribute.size,
                        GLES20.GL_UNSIGNED_SHORT, stride,
                        attribute.offset.toLong())
                else -> throw IllegalArgumentException("Unknown vertex type!")
            }
        } else {
            when (attribute.vertexType) {
                VertexType.FLOAT -> GLES20.glVertexAttribPointer(attribute.id,
                        attribute.size,
                        GLES20.GL_FLOAT, attribute.normalized, stride,
                        attribute.offset.toLong())
                VertexType.HALF_FLOAT -> GLES20.glVertexAttribPointer(
                        attribute.id, attribute.size,
                        GLES30.GL_HALF_FLOAT, attribute.normalized, stride,
                        attribute.offset.toLong())
                VertexType.BYTE -> GLES20.glVertexAttribPointer(attribute.id,
                        attribute.size,
                        GLES20.GL_BYTE, attribute.normalized, stride,
                        attribute.offset.toLong())
                VertexType.UNSIGNED_BYTE -> GLES20.glVertexAttribPointer(
                        attribute.id, attribute.size,
                        GLES20.GL_UNSIGNED_BYTE, attribute.normalized, stride,
                        attribute.offset.toLong())
                VertexType.SHORT -> GLES20.glVertexAttribPointer(attribute.id,
                        attribute.size,
                        GLES20.GL_SHORT, attribute.normalized, stride,
                        attribute.offset.toLong())
                VertexType.UNSIGNED_SHORT -> GLES20.glVertexAttribPointer(
                        attribute.id, attribute.size,
                        GLES20.GL_UNSIGNED_SHORT, attribute.normalized,
                        stride, attribute.offset.toLong())
                else -> throw IllegalArgumentException("Unknown vertex type!")
            }
        }
        GLES30.glVertexAttribDivisor(attribute.id, attribute.divisor)
    }

    private fun addToBuffer(attribute: ModelAttribute,
                            vertices: Int,
                            buffer: ByteBuffer) {
        val floatArray = attribute.floatArray()
        if (floatArray == null) {
            val intArray = attribute.intArray() ?: throw IllegalArgumentException(
                    "Attribute contains no data")
            when (attribute.vertexType()) {
                VertexType.BYTE, VertexType.UNSIGNED_BYTE -> for (i in 0..vertices - 1) {
                    val `is` = i * attribute.size()
                    buffer.position(attribute.offset() + i * stride)
                    for (j in 0..attribute.size() - 1) {
                        val ij = `is` + j
                        buffer.put(intArray[ij].toByte())
                    }
                }
                VertexType.SHORT, VertexType.UNSIGNED_SHORT -> for (i in 0..vertices - 1) {
                    val `is` = i * attribute.size()
                    buffer.position(attribute.offset() + i * stride)
                    for (j in 0..attribute.size() - 1) {
                        val ij = `is` + j
                        buffer.putShort(intArray[ij].toShort())
                    }
                }
                else -> throw IllegalArgumentException(
                        "Invalid array in vao attribute!")
            }
        } else {
            when (attribute.vertexType()) {
                VertexType.FLOAT -> for (i in 0..vertices - 1) {
                    val `is` = i * attribute.size()
                    buffer.position(attribute.offset() + i * stride)
                    for (j in 0..attribute.size() - 1) {
                        val ij = `is` + j
                        buffer.putFloat(floatArray[ij])
                    }
                }
                VertexType.HALF_FLOAT -> for (i in 0..vertices - 1) {
                    val `is` = i * attribute.size()
                    buffer.position(attribute.offset() + i * stride)
                    for (j in 0..attribute.size() - 1) {
                        val ij = `is` + j
                        buffer.putShort(FastMath.convertFloatToHalf(
                                floatArray[ij]))
                    }
                }
                VertexType.BYTE -> if (attribute.normalized()) {
                    for (i in 0..vertices - 1) {
                        val `is` = i * attribute.size()
                        buffer.position(attribute.offset() + i * stride)
                        for (j in 0..attribute.size() - 1) {
                            val ij = `is` + j
                            buffer.put(round(floatArray[ij] * 127.0f).toByte())
                        }
                    }
                } else {
                    for (i in 0..vertices - 1) {
                        val `is` = i * attribute.size()
                        buffer.position(attribute.offset() + i * stride)
                        for (j in 0..attribute.size() - 1) {
                            val ij = `is` + j
                            buffer.put(round(floatArray[ij]).toByte())
                        }
                    }
                }
                VertexType.UNSIGNED_BYTE -> if (attribute.normalized()) {
                    for (i in 0..vertices - 1) {
                        val `is` = i * attribute.size()
                        buffer.position(attribute.offset() + i * stride)
                        for (j in 0..attribute.size() - 1) {
                            val ij = `is` + j
                            buffer.put(round(floatArray[ij] * 255.0f).toByte())
                        }
                    }
                } else {
                    for (i in 0..vertices - 1) {
                        val `is` = i * attribute.size()
                        buffer.position(attribute.offset() + i * stride)
                        for (j in 0..attribute.size() - 1) {
                            val ij = `is` + j
                            buffer.put(round(floatArray[ij]).toByte())
                        }
                    }
                }
                VertexType.SHORT -> if (attribute.normalized()) {
                    for (i in 0..vertices - 1) {
                        val `is` = i * attribute.size()
                        buffer.position(attribute.offset() + i * stride)
                        for (j in 0..attribute.size() - 1) {
                            val ij = `is` + j
                            buffer.putShort(
                                    round(floatArray[ij] * 32768.0f).toShort())
                        }
                    }
                } else {
                    for (i in 0..vertices - 1) {
                        val `is` = i * attribute.size()
                        buffer.position(attribute.offset() + i * stride)
                        for (j in 0..attribute.size() - 1) {
                            val ij = `is` + j
                            buffer.putShort(round(floatArray[ij]).toShort())
                        }
                    }
                }
                VertexType.UNSIGNED_SHORT -> if (attribute.normalized()) {
                    for (i in 0..vertices - 1) {
                        val `is` = i * attribute.size()
                        buffer.position(attribute.offset() + i * stride)
                        for (j in 0..attribute.size() - 1) {
                            val ij = `is` + j
                            buffer.putShort(
                                    round(floatArray[ij] * 65535.0f).toShort())
                        }
                    }
                } else {
                    for (i in 0..vertices - 1) {
                        val `is` = i * attribute.size()
                        buffer.position(attribute.offset() + i * stride)
                        for (j in 0..attribute.size() - 1) {
                            val ij = `is` + j
                            buffer.putShort(round(floatArray[ij]).toShort())
                        }
                    }
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
        assert(!stored)
        val data = data ?: throw IllegalStateException(
                "VBO cannot be stored anymore")
        stored = true
        gl.check()
        data.rewind()
        vertexID = GLES20.glGenBuffers()
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexID)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, data, GLES20.GL_STATIC_DRAW)
        attributes.forEach { storeAttribute(gl, it) }
        if (weak) {
            this.data = null
        }
    }

    fun dispose(gl: GL) {
        assert(stored)
        stored = false
        gl.check()
        GLES20.glDeleteBuffers(vertexID)
    }

    fun reset() {
        stored = false
    }

    private class ModelAttributeData(attribute: ModelAttribute,
                                     val offset: Int) {
        val vertexType: VertexType
        val id: Int
        val size: Int
        val divisor: Int
        val normalized: Boolean
        val integer: Boolean

        init {
            vertexType = attribute.vertexType()
            id = attribute.id()
            size = attribute.size()
            normalized = attribute.normalized()
            divisor = attribute.divisor()
            integer = attribute.intArray() != null
        }
    }
}
