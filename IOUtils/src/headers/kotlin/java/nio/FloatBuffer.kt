package java.nio

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
