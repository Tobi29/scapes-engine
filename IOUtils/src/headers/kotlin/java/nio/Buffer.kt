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
