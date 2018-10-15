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

package org.tobi29.io

import org.khronos.webgl.*
import org.tobi29.arrays.HeapBytes
import org.tobi29.stdex.asArray
import org.tobi29.stdex.asTypedArray

sealed class TypedView(
    val buffer: ArrayBuffer,
    val bufferOffset: Int,
    offset: Int,
    size: Int
) : HeapBytes(
    Int8Array(buffer, bufferOffset).asArray(), offset, size
), HeapView, ByteViewE {
    constructor(
        array: ByteArray,
        offset: Int,
        size: Int
    ) : this(
        array.asTypedArray().buffer,
        array.asTypedArray().byteOffset, offset, size
    )

    constructor(
        shortArray: ShortArray,
        offset: Int,
        size: Int
    ) : this(
        shortArray.asTypedArray().buffer,
        shortArray.asTypedArray().byteOffset, offset, size
    )

    constructor(
        charArray: CharArray,
        offset: Int,
        size: Int
    ) : this(
        charArray.asTypedArray().buffer,
        charArray.asTypedArray().byteOffset, offset, size
    )

    constructor(
        intArray: IntArray,
        offset: Int,
        size: Int
    ) : this(
        intArray.asTypedArray().buffer,
        intArray.asTypedArray().byteOffset, offset, size
    )

    constructor(
        floatArray: FloatArray,
        offset: Int,
        size: Int
    ) : this(
        floatArray.asTypedArray().buffer,
        floatArray.asTypedArray().byteOffset, offset, size
    )

    constructor(
        doubleArray: DoubleArray,
        offset: Int,
        size: Int
    ) : this(
        doubleArray.asTypedArray().buffer,
        doubleArray.asTypedArray().byteOffset, offset, size
    )

    protected val view = DataView(buffer, bufferOffset, size)

    abstract override fun slice(index: Int): TypedView

    abstract override fun slice(index: Int, size: Int): TypedView

    inline val byteTypedArray: Int8Array
        get() = Int8Array(buffer, bufferOffset)

    val shortArray: ShortArray get() = shortTypedArray.asArray()
    inline val shortTypedArray: Int16Array
        get() = Int16Array(buffer, bufferOffset)

    val charArray: CharArray get() = charTypedArray.asArray()
    inline val charTypedArray: Uint16Array
        get() = Uint16Array(buffer, bufferOffset)

    val intArray: IntArray get() = intTypedArray.asArray()
    inline val intTypedArray: Int32Array
        get() = Int32Array(buffer, bufferOffset)

    val floatArray: FloatArray get() = floatTypedArray.asArray()
    inline val floatTypedArray: Float32Array
        get() = Float32Array(buffer, bufferOffset)

    val doubleArray: DoubleArray get() = doubleTypedArray.asArray()
    inline val doubleTypedArray: Float64Array
        get() = Float64Array(buffer, bufferOffset)
}

class TypedViewBE(
    buffer: ArrayBuffer,
    bufferOffset: Int,
    offset: Int,
    size: Int
) : TypedView(
    buffer, bufferOffset, offset, size
), ByteViewBE {
    constructor(
        array: ByteArray,
        offset: Int,
        size: Int
    ) : this(
        array.asTypedArray().buffer,
        array.asTypedArray().byteOffset, offset, size
    )

    constructor(
        shortArray: ShortArray,
        offset: Int,
        size: Int
    ) : this(
        shortArray.asTypedArray().buffer,
        shortArray.asTypedArray().byteOffset, offset, size
    )

    constructor(
        charArray: CharArray,
        offset: Int,
        size: Int
    ) : this(
        charArray.asTypedArray().buffer,
        charArray.asTypedArray().byteOffset, offset, size
    )

    constructor(
        intArray: IntArray,
        offset: Int,
        size: Int
    ) : this(
        intArray.asTypedArray().buffer,
        intArray.asTypedArray().byteOffset, offset, size
    )

    constructor(
        floatArray: FloatArray,
        offset: Int,
        size: Int
    ) : this(
        floatArray.asTypedArray().buffer,
        floatArray.asTypedArray().byteOffset, offset, size
    )

    constructor(
        doubleArray: DoubleArray,
        offset: Int,
        size: Int
    ) : this(
        doubleArray.asTypedArray().buffer,
        doubleArray.asTypedArray().byteOffset, offset, size
    )

    override fun slice(index: Int) = slice(index, size - index)

    override fun slice(index: Int, size: Int): TypedViewBE =
        prepareSlice(index, size) { offset, length ->
            TypedViewBE(buffer, bufferOffset, offset, length)
        }

    override fun getShort(index: Int): Short = view.getInt16(index, false)

    override fun setShort(index: Int, value: Short) =
        view.setInt16(index, value, false)

    override fun getInt(index: Int): Int = view.getInt32(index, false)

    override fun setInt(index: Int, value: Int) =
        view.setInt32(index, value, false)

    override fun getFloat(index: Int): Float = view.getFloat32(index, false)

    override fun setFloat(index: Int, value: Float) =
        view.setFloat32(index, value, false)

    override fun getDouble(index: Int): Double = view.getFloat64(index, false)

    override fun setDouble(index: Int, value: Double) =
        view.setFloat64(index, value, false)
}

class TypedViewLE(
    buffer: ArrayBuffer,
    bufferOffset: Int,
    offset: Int,
    size: Int
) : TypedView(
    buffer, bufferOffset, offset, size
), ByteViewLE {
    constructor(
        array: ByteArray,
        offset: Int,
        size: Int
    ) : this(
        array.asTypedArray().buffer,
        array.asTypedArray().byteOffset, offset, size
    )

    constructor(
        shortArray: ShortArray,
        offset: Int,
        size: Int
    ) : this(
        shortArray.asTypedArray().buffer,
        shortArray.asTypedArray().byteOffset, offset, size
    )

    constructor(
        charArray: CharArray,
        offset: Int,
        size: Int
    ) : this(
        charArray.asTypedArray().buffer,
        charArray.asTypedArray().byteOffset, offset, size
    )

    constructor(
        intArray: IntArray,
        offset: Int,
        size: Int
    ) : this(
        intArray.asTypedArray().buffer,
        intArray.asTypedArray().byteOffset, offset, size
    )

    constructor(
        floatArray: FloatArray,
        offset: Int,
        size: Int
    ) : this(
        floatArray.asTypedArray().buffer,
        floatArray.asTypedArray().byteOffset, offset, size
    )

    constructor(
        doubleArray: DoubleArray,
        offset: Int,
        size: Int
    ) : this(
        doubleArray.asTypedArray().buffer,
        doubleArray.asTypedArray().byteOffset, offset, size
    )

    override fun slice(index: Int) = slice(index, size - index)

    override fun slice(index: Int, size: Int): TypedViewLE =
        prepareSlice(index, size) { offset, length ->
            TypedViewLE(buffer, bufferOffset, offset, length)
        }

    override fun getShort(index: Int): Short = view.getInt16(index, true)

    override fun setShort(index: Int, value: Short) =
        view.setInt16(index, value, true)

    override fun getInt(index: Int): Int = view.getInt32(index, true)

    override fun setInt(index: Int, value: Int) =
        view.setInt32(index, value, true)

    override fun getFloat(index: Int): Float = view.getFloat32(index, true)

    override fun setFloat(index: Int, value: Float) =
        view.setFloat32(index, value, true)

    override fun getDouble(index: Int): Double = view.getFloat64(index, true)

    override fun setDouble(index: Int, value: Double) =
        view.setFloat64(index, value, true)
}
