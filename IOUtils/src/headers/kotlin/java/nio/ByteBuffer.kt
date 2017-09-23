package java.nio

/**
 * **Note:** This shims to the native JVM buffers which cannot be
 * implemented using this header, custom extending it therefore is forbidden!
 */
@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header abstract class ByteBuffer(mark: Int,
                                 pos: Int,
                                 lim: Int,
                                 cap: Int
) : Buffer, Comparable<ByteBuffer> {
    abstract fun slice(): ByteBuffer

    abstract fun duplicate(): ByteBuffer

    abstract fun asReadOnlyBuffer(): ByteBuffer

    abstract fun get(): Byte

    abstract fun put(value: Byte): ByteBuffer

    abstract operator fun get(index: Int): Byte

    abstract fun put(index: Int,
                     value: Byte): ByteBuffer

    open fun get(dst: ByteArray): ByteBuffer

    open fun get(dst: ByteArray,
                 offset: Int,
                 length: Int): ByteBuffer

    open fun put(src: ByteBuffer): ByteBuffer

    open fun put(src: ByteArray): ByteBuffer

    open fun put(src: ByteArray,
                 offset: Int,
                 length: Int): ByteBuffer

    abstract fun compact(): ByteBuffer

    abstract fun order(): ByteOrder

    abstract fun order(order: ByteOrder): ByteBuffer

    abstract fun getChar(): Char

    abstract fun putChar(value: Char): ByteBuffer

    abstract fun getChar(index: Int): Char

    abstract fun putChar(index: Int,
                         value: Char): ByteBuffer

    abstract fun getShort(): Short

    abstract fun putShort(value: Short): ByteBuffer

    abstract fun getShort(index: Int): Short

    abstract fun putShort(index: Int,
                          value: Short): ByteBuffer

    abstract fun getInt(): Int

    abstract fun putInt(value: Int): ByteBuffer

    abstract fun getInt(index: Int): Int

    abstract fun putInt(index: Int,
                        value: Int): ByteBuffer

    abstract fun getLong(): Long

    abstract fun putLong(value: Long): ByteBuffer

    abstract fun getLong(index: Int): Long

    abstract fun putLong(index: Int,
                         value: Long): ByteBuffer

    abstract fun getFloat(): Float

    abstract fun putFloat(value: Float): ByteBuffer

    abstract fun getFloat(index: Int): Float

    abstract fun putFloat(index: Int,
                          value: Float): ByteBuffer

    abstract fun asFloatBuffer(): FloatBuffer

    abstract fun getDouble(): Double

    abstract fun putDouble(value: Double): ByteBuffer

    abstract fun getDouble(index: Int): Double

    abstract fun putDouble(index: Int,
                           value: Double): ByteBuffer

    override final fun array(): ByteArray
}

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header class ByteOrder private constructor(name: String)
