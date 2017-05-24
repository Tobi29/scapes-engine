package java.nio.channels

/*
@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header interface Channel : Closeable {
    fun isOpen(): Boolean

    /**
     * @throws IOException
     */
    override fun close()
}

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header interface ByteChannel : ReadableByteChannel, WritableByteChannel

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header interface ReadableByteChannel : Channel {
    /**
     * @throws IOException
     */
    fun read(dst: ByteBuffer): Int
}

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header interface WritableByteChannel : Channel {
    /**
     * @throws IOException
     */
    fun write(src: ByteBuffer): Int
}

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header interface SeekableByteChannel : ByteChannel {
    /**
     * @throws IOException
     */
    fun position(): Long

    /**
     * @throws IOException
     */
    fun position(newPosition: Long): SeekableByteChannel

    /**
     * @throws IOException
     */
    fun size(): Long

    /**
     * @throws IOException
     */
    fun truncate(size: Long): SeekableByteChannel
}

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header interface GatheringByteChannel : WritableByteChannel {
    /**
     * @throws IOException
     */
    fun write(srcs: Array<java.nio.ByteBuffer>,
              offset: Int,
              length: Int): Long

    /**
     * @throws IOException
     */
    fun write(srcs: Array<java.nio.ByteBuffer>): Long
}

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header interface ScatteringByteChannel : ReadableByteChannel {
    /**
     * @throws IOException
     */
    fun read(dsts: Array<java.nio.ByteBuffer>,
             offset: Int,
             length: Int): Long

    /**
     * @throws IOException
     */
    fun read(dsts: Array<java.nio.ByteBuffer>): Long
}

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header interface InterruptibleChannel : Channel
*/
