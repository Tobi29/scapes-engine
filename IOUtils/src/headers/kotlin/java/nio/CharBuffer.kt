package java.nio

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
