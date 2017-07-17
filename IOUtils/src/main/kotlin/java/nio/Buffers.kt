package java.nio

/**
 * **Note:** This shims to the native JVM buffers which cannot be
 * implemented using this header, custom extending it therefore is forbidden!
 */
@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header abstract class Buffer(mark: Int,
                             position: Int,
                             limit: Int,
                             capacity: Int) {
    open fun capacity(): Int

    open fun position(): Int

    open fun position(newPosition: Int): Buffer

    open fun limit(): Int

    open fun limit(newLimit: Int): Buffer

    open fun mark(): Buffer

    open fun reset(): Buffer

    open fun clear(): Buffer

    open fun flip(): Buffer

    open fun rewind(): Buffer

    open fun remaining(): Int

    open fun hasRemaining(): Boolean

    abstract fun isReadOnly(): Boolean

    open fun hasArray(): Boolean

    open fun array(): Any
}

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

    abstract fun put(b: Byte): ByteBuffer

    abstract operator fun get(index: Int): Byte

    abstract fun put(index: Int,
                     b: Byte): ByteBuffer

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

    abstract val char: Char

    abstract fun putChar(value: Char): ByteBuffer

    abstract fun getChar(index: Int): Char

    abstract fun putChar(index: Int,
                         value: Char): ByteBuffer

    abstract val short: Short

    abstract fun putShort(value: Short): ByteBuffer

    abstract fun getShort(index: Int): Short

    abstract fun putShort(index: Int,
                          value: Short): ByteBuffer

    abstract val int: Int

    abstract fun putInt(value: Int): ByteBuffer

    abstract fun getInt(index: Int): Int

    abstract fun putInt(index: Int,
                        value: Int): ByteBuffer

    abstract val long: Long

    abstract fun putLong(value: Long): ByteBuffer

    abstract fun getLong(index: Int): Long

    abstract fun putLong(index: Int,
                         value: Long): ByteBuffer

    abstract val float: Float

    abstract fun putFloat(value: Float): ByteBuffer

    abstract fun getFloat(index: Int): Float

    abstract fun putFloat(index: Int,
                          value: Float): ByteBuffer

    abstract fun asFloatBuffer(): FloatBuffer

    abstract val double: Double

    abstract fun putDouble(value: Double): ByteBuffer

    abstract fun getDouble(index: Int): Double

    abstract fun putDouble(index: Int,
                           value: Double): ByteBuffer

    override final fun array(): ByteArray
}

/**
 * **Note:** This shims to the native JVM buffers which cannot be
 * implemented using this header, custom extending it therefore is forbidden!
 */
@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header abstract class FloatBuffer(
        mark: Int,
        pos: Int,
        lim: Int,
        cap: Int
) : Buffer, Comparable<FloatBuffer> {
    abstract fun slice(): FloatBuffer

    abstract fun duplicate(): FloatBuffer

    abstract fun asReadOnlyBuffer(): FloatBuffer

    abstract fun get(): Float

    abstract fun put(f: Float): FloatBuffer

    abstract operator fun get(index: Int): Float

    abstract fun put(index: Int,
                     f: Float): FloatBuffer

    open fun get(dst: FloatArray): FloatBuffer

    open fun get(dst: FloatArray,
                 offset: Int,
                 length: Int): FloatBuffer

    open fun put(src: FloatBuffer): FloatBuffer

    open fun put(src: FloatArray): FloatBuffer

    open fun put(src: FloatArray,
                 offset: Int,
                 length: Int): FloatBuffer

    abstract fun compact(): FloatBuffer

    override fun compareTo(other: FloatBuffer): Int

    override final fun array(): FloatArray
}

/**
 * **Note:** This shims to the native JVM buffers which cannot be
 * implemented using this header, custom extending it therefore is forbidden!
 */
@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header abstract class CharBuffer(
        mark: Int,
        pos: Int,
        lim: Int,
        cap: Int
) : Buffer, Comparable<CharBuffer>, Appendable {
    abstract fun slice(): CharBuffer

    abstract fun duplicate(): CharBuffer

    abstract fun asReadOnlyBuffer(): CharBuffer

    abstract fun get(): Char

    abstract fun put(f: Char): CharBuffer

    abstract operator fun get(index: Int): Char

    abstract fun put(index: Int,
                     f: Char): CharBuffer

    open fun get(dst: CharArray): CharBuffer

    open fun get(dst: CharArray,
                 offset: Int,
                 length: Int): CharBuffer

    open fun put(src: CharBuffer): CharBuffer

    open fun put(src: CharArray): CharBuffer

    open fun put(src: CharArray,
                 offset: Int,
                 length: Int): CharBuffer

    open  fun put(src: String,
                  start: Int,
                  end: Int): CharBuffer

    open fun put(src: String): CharBuffer

    abstract fun compact(): CharBuffer

    override fun compareTo(other: CharBuffer): Int

    override final fun array(): CharArray
}

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header class ByteOrder private constructor(name: String)
