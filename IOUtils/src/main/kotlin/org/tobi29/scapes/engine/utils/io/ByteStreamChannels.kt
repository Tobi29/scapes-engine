package org.tobi29.scapes.engine.utils.io

class WritableByteStreamChannel(
        private val stream: WritableByteStream
) : WritableByteChannel {
    override fun write(buffer: ByteViewRO): Int {
        stream.put(buffer)
        return buffer.size
    }

    override fun isOpen() = true
    override fun close() {}
}

class ReadableByteStreamChannel(
        private val stream: ReadableByteStream
) : ReadableByteChannel {
    override fun read(buffer: ByteView): Int {
        return stream.getSome(buffer)
    }

    override fun isOpen() = true
    override fun close() {}
}