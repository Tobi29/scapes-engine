package org.tobi29.scapes.engine.utils.io

typealias ReadableBuffer = java.lang.Readable

interface Readable : ReadableBuffer {
    fun read(): Char

    override fun read(cb: CharBuffer): Int {
        val position = cb.position()
        while (cb.hasRemaining()) {
            cb.put(read())
        }
        return cb.position() - position
    }
}

fun ReadableBuffer.toReadable(): Readable = when (this) {
    is Readable -> toReadable()
    else -> object : Readable {
        private val single = CharBuffer(1)

        override fun read(): Char {
            single.clear()
            read(single)
            single.flip()
            return single.get()
        }

        override fun read(cb: CharBuffer): Int = this@toReadable.read(cb)
    }
}

fun Readable.toReadable(): Readable = this
