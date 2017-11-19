package org.tobi29.scapes.engine.utils.io

import org.khronos.webgl.*
import org.tobi29.scapes.engine.utils.HeapByteArraySlice
import org.tobi29.scapes.engine.utils.asArray

@Suppress("UnsafeCastFromDynamic")
sealed class TypedView(
        val buffer: ArrayBuffer,
        val bufferOffset: Int,
        offset: Int,
        size: Int
) : HeapByteArraySlice(Int8Array(buffer, bufferOffset).asArray(), offset,
        size), HeapView {
    constructor(array: ByteArray,
                offset: Int,
                size: Int) : this(array.js.buffer,
            array.js.byteOffset, offset, size)

    constructor(shortArray: ShortArray,
                offset: Int,
                size: Int) : this(shortArray.js.buffer,
            shortArray.js.byteOffset, offset, size)

    constructor(charArray: CharArray,
                offset: Int,
                size: Int) : this(charArray.js.buffer,
            charArray.js.byteOffset, offset, size)

    constructor(intArray: IntArray,
                offset: Int,
                size: Int) : this(intArray.js.buffer,
            intArray.js.byteOffset, offset, size)

    constructor(floatArray: FloatArray,
                offset: Int,
                size: Int) : this(floatArray.js.buffer,
            floatArray.js.byteOffset, offset, size)

    constructor(doubleArray: DoubleArray,
                offset: Int,
                size: Int) : this(doubleArray.js.buffer,
            doubleArray.js.byteOffset, offset, size)

    protected val view = DataView(buffer, bufferOffset, size)

    override abstract fun slice(index: Int): TypedView

    override abstract fun slice(index: Int,
                                size: Int): TypedView

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

private inline val ByteArray.js: Int8Array get() = asDynamic()
private inline val ShortArray.js: Int16Array get() = asDynamic()
private inline val CharArray.js: Uint16Array get() = asDynamic()
private inline val IntArray.js: Int32Array get() = asDynamic()
private inline val FloatArray.js: Float32Array get() = asDynamic()
private inline val DoubleArray.js: Float64Array get() = asDynamic()

class TypedViewBE(
        buffer: ArrayBuffer,
        bufferOffset: Int,
        offset: Int,
        size: Int
) : TypedView(buffer, bufferOffset, offset, size), ByteViewBE {
    constructor(array: ByteArray,
                offset: Int,
                size: Int) : this(array.js.buffer,
            array.js.byteOffset, offset, size)

    constructor(shortArray: ShortArray,
                offset: Int,
                size: Int) : this(shortArray.js.buffer,
            shortArray.js.byteOffset, offset, size)

    constructor(charArray: CharArray,
                offset: Int,
                size: Int) : this(charArray.js.buffer,
            charArray.js.byteOffset, offset, size)

    constructor(intArray: IntArray,
                offset: Int,
                size: Int) : this(intArray.js.buffer,
            intArray.js.byteOffset, offset, size)

    constructor(floatArray: FloatArray,
                offset: Int,
                size: Int) : this(floatArray.js.buffer,
            floatArray.js.byteOffset, offset, size)

    constructor(doubleArray: DoubleArray,
                offset: Int,
                size: Int) : this(doubleArray.js.buffer,
            doubleArray.js.byteOffset, offset, size)

    override fun slice(index: Int) = slice(index, size - index)

    override fun slice(index: Int,
                       size: Int): TypedViewBE =
            prepareSlice(index, size) { offset, length ->
                TypedViewBE(buffer, bufferOffset, offset, length)
            }

    override fun getShort(index: Int): Short = view.getInt16(index, false)
    override fun setShort(index: Int,
                          value: Short) = view.setInt16(index, value, false)

    override fun getInt(index: Int): Int = view.getInt32(index, false)
    override fun setInt(index: Int,
                        value: Int) = view.setInt32(index, value, false)

    override fun getFloat(index: Int): Float = view.getFloat32(index, false)
    override fun setFloat(index: Int,
                          value: Float) = view.setFloat32(index, value, false)

    override fun getDouble(index: Int): Double = view.getFloat64(index, false)
    override fun setDouble(index: Int,
                           value: Double) = view.setFloat64(index, value, false)
}

class TypedViewLE(
        buffer: ArrayBuffer,
        bufferOffset: Int,
        offset: Int,
        size: Int
) : TypedView(buffer, bufferOffset, offset, size), ByteViewLE {
    constructor(array: ByteArray,
                offset: Int,
                size: Int) : this(array.js.buffer,
            array.js.byteOffset, offset, size)

    constructor(shortArray: ShortArray,
                offset: Int,
                size: Int) : this(shortArray.js.buffer,
            shortArray.js.byteOffset, offset, size)

    constructor(charArray: CharArray,
                offset: Int,
                size: Int) : this(charArray.js.buffer,
            charArray.js.byteOffset, offset, size)

    constructor(intArray: IntArray,
                offset: Int,
                size: Int) : this(intArray.js.buffer,
            intArray.js.byteOffset, offset, size)

    constructor(floatArray: FloatArray,
                offset: Int,
                size: Int) : this(floatArray.js.buffer,
            floatArray.js.byteOffset, offset, size)

    constructor(doubleArray: DoubleArray,
                offset: Int,
                size: Int) : this(doubleArray.js.buffer,
            doubleArray.js.byteOffset, offset, size)

    override fun slice(index: Int) = slice(index, size - index)

    override fun slice(index: Int,
                       size: Int): TypedViewLE =
            prepareSlice(index, size) { offset, length ->
                TypedViewLE(buffer, bufferOffset, offset, length)
            }

    override fun getShort(index: Int): Short = view.getInt16(index, true)
    override fun setShort(index: Int,
                          value: Short) = view.setInt16(index, value, true)

    override fun getInt(index: Int): Int = view.getInt32(index, true)
    override fun setInt(index: Int,
                        value: Int) = view.setInt32(index, value, true)

    override fun getFloat(index: Int): Float = view.getFloat32(index, true)
    override fun setFloat(index: Int,
                          value: Float) = view.setFloat32(index, value, true)

    override fun getDouble(index: Int): Double = view.getFloat64(index, true)
    override fun setDouble(index: Int,
                           value: Double) = view.setFloat64(index, value, true)
}
