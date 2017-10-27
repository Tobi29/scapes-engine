package org.tobi29.scapes.engine.utils.io

data class HeapResource(private val data: ByteViewRO? = null) : ReadSourceLocal {
    override fun channel() = dataNow().viewBE.let(::MemoryViewReadableStream)
            .let(::ReadableByteStreamChannel)

    override fun <R> readNow(reader: (ReadableByteStream) -> R): R {
        val stream = dataNow().viewBE.let(::MemoryViewReadableStream)
        return reader(stream)
    }

    override fun dataNow() = data ?: throw IOException("Entry does not exist")
}
