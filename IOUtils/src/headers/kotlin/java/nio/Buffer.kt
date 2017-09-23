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
    fun capacity(): Int

    fun position(): Int

    fun position(newPosition: Int): Buffer

    fun limit(): Int

    fun limit(newLimit: Int): Buffer

    fun mark(): Buffer

    fun reset(): Buffer

    fun clear(): Buffer

    fun flip(): Buffer

    fun rewind(): Buffer

    fun remaining(): Int

    fun hasRemaining(): Boolean

    abstract fun isReadOnly(): Boolean

    abstract fun hasArray(): Boolean

    abstract fun array(): Any
}
