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

package org.tobi29.scapes.engine.graphics

import org.tobi29.utils.Either
import org.tobi29.utils.EitherLeft
import org.tobi29.utils.EitherRight

class ModelAttribute {
    val vertexType: VertexType
    val id: Int
    val length: Int
    val size: Int
    val divisor: Int
    val normalized: Boolean
    val data: Either<FloatArray, IntArray>
    var offset = 0

    constructor(
        id: Int,
        size: Int,
        array: IntArray,
        divisor: Int,
        vertexType: VertexType
    ) : this(
        id, size, array, array.size,
        divisor, vertexType
    )

    constructor(
        id: Int,
        size: Int,
        array: IntArray,
        length: Int,
        divisor: Int,
        vertexType: VertexType
    ) {
        this.id = id
        this.length = length
        this.size = size
        this.divisor = divisor
        this.vertexType = vertexType
        normalized = false
        data = EitherRight(array)
    }

    constructor(
        id: Int,
        size: Int,
        array: FloatArray,
        normalized: Boolean,
        divisor: Int,
        vertexType: VertexType
    ) : this(
        id, size, array,
        array.size, normalized, divisor, vertexType
    )

    constructor(
        id: Int,
        size: Int,
        array: FloatArray,
        length: Int,
        normalized: Boolean,
        divisor: Int,
        vertexType: VertexType
    ) {
        this.id = id
        this.length = length
        this.size = size
        this.normalized = normalized
        this.divisor = divisor
        this.vertexType = vertexType
        data = EitherLeft(array)
    }
}
