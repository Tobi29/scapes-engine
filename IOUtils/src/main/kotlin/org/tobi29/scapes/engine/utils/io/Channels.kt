package org.tobi29.scapes.engine.utils.io

import org.tobi29.scapes.engine.utils.toIntClamped

interface ReadableByteChannel : Channel {
    fun read(buffer: ByteView): Int

    fun skip(length: Long): Long {
        val buffer = ByteArray(length.coerceAtMost(4096).toInt()).view
        while (length > 0) {
            val read = read(buffer.slice(0,
                    buffer.size.coerceAtMost(length.toIntClamped())))
            if (read == -1) {
                throw IOException("End of stream")
            }
            if (read == 0) {
                return length
            }
        }
        return length
    }
}

fun ReadableByteChannel.read(stream: MemoryViewStream<*>): Int =
        read(stream.bufferSlice()).also {
            if (it > 0) stream.position(stream.position() + it)
        }

interface WritableByteChannel : Channel {
    fun write(buffer: ByteViewRO): Int
}

fun WritableByteChannel.write(stream: MemoryViewStream<*>): Int =
        write(stream.bufferSlice()).also {
            if (it > 0) stream.position(stream.position() + it)
        }

interface ByteChannel : ReadableByteChannel, WritableByteChannel

interface SeekableByteChannel : ByteChannel {
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

    /**
     * @throws IOException
     */
    fun remaining(): Long = size() - position()

    override fun skip(length: Long) = length.also {
        position(position() + length)
    }
}
