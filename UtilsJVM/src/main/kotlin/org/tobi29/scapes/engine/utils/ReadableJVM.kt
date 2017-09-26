package org.tobi29.scapes.engine.utils

import java.nio.CharBuffer

impl interface ReadablePlatform : java.lang.Readable {
    impl fun read(): Char

    impl fun read(array: CharArray,
                  offset: Int,
                  size: Int)

    override fun read(cb: CharBuffer): Int {
        val position = cb.position()
        if (cb.hasArray()) {
            read(cb.array(), cb.arrayOffset() + position, cb.remaining())
        } else {
            while (cb.hasRemaining()) {
                cb.put(read())
            }
        }
        return cb.position() - position
    }
}

/**
 * Wraps a [java.lang.Readable] into a [Readable]
 * @receiver The readable to wrap
 * @return A readable which forwards all calls to the given one
 */
fun java.lang.Readable.toReadable(): Readable = when (this) {
    is Readable -> this
    else -> object : Readable {
        private val single = CharBuffer.allocate(1)

        override fun read(): Char {
            single.clear()
            read(single)
            single.flip()
            return single.get()
        }

        override fun read(array: CharArray,
                          offset: Int,
                          size: Int) {
            this@toReadable.read(CharBuffer.wrap(array, offset, size))
        }
    }
}

/**
 * Wraps a [Readable] into a [java.lang.Readable]
 * @receiver The readable to wrap
 * @return A readable which forwards all calls to the given one
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Readable.toJavaReadable(): java.lang.Readable =
        this as java.lang.Readable
