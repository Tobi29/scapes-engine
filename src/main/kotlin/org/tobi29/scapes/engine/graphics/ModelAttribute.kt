/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.graphics

class ModelAttribute {
    private val vertexType: VertexType
    private val id: Int
    private val length: Int
    private val size: Int
    private val divisor: Int
    private val normalized: Boolean
    private val floatArray: FloatArray?
    private val byteArray: IntArray?
    private var offset = 0

    constructor(id: Int, size: Int, array: IntArray, divisor: Int,
                vertexType: VertexType) : this(id, size, array, array.size,
            divisor, vertexType) {
    }

    constructor(id: Int, size: Int, array: IntArray, length: Int,
                divisor: Int, vertexType: VertexType) {
        this.id = id
        this.length = length
        this.size = size
        this.divisor = divisor
        this.vertexType = vertexType
        normalized = true
        byteArray = array
        floatArray = null
    }

    constructor(id: Int, size: Int, array: FloatArray, normalized: Boolean,
                divisor: Int, vertexType: VertexType) : this(id, size, array,
            array.size, normalized, divisor, vertexType) {
    }

    constructor(id: Int, size: Int, array: FloatArray, length: Int,
                normalized: Boolean, divisor: Int, vertexType: VertexType) {
        this.id = id
        this.length = length
        this.size = size
        this.normalized = normalized
        this.divisor = divisor
        this.vertexType = vertexType
        floatArray = array
        byteArray = null
    }

    fun vertexType(): VertexType {
        return vertexType
    }

    fun id(): Int {
        return id
    }

    fun length(): Int {
        return length
    }

    fun size(): Int {
        return size
    }

    fun divisor(): Int {
        return divisor
    }

    fun normalized(): Boolean {
        return normalized
    }

    fun floatArray(): FloatArray? {
        return floatArray
    }

    fun intArray(): IntArray? {
        return byteArray
    }

    fun setOffset(offset: Int) {
        this.offset = offset
    }

    fun offset(): Int {
        return offset
    }
}
