package org.tobi29.scapes.engine.utils

import java.nio.CharBuffer

/**
 * Wraps a [java.lang.Readable] into a [Readable]
 * @receiver The readable to wrap
 * @return A readable which forwards all calls to the given one
 */
fun java.lang.Readable.toReadable(): Readable = when (this) {
    is SEReadable -> se
    else -> object : JavaReadable {
        override val java = this@toReadable
    }
}

/**
 * Wraps a [Readable] into a [java.lang.Readable]
 * @receiver The readable to wrap
 * @return A readable which forwards all calls to the given one
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Readable.toJavaReadable(): java.lang.Readable = when (this) {
    is JavaReadable -> java
    else -> object : SEReadable {
        override val se = this@toJavaReadable
    }
}

interface JavaReadable : Readable {
    val java: java.lang.Readable

    override fun read(): Char {
        val single = CharBuffer.allocate(1)
        java.read(single)
        single.flip()
        return single.get()
    }

    override fun read(array: CharArray,
                      offset: Int,
                      size: Int) {
        java.read(CharBuffer.wrap(array, offset, size))
    }
}

interface SEReadable : java.lang.Readable {
    val se: Readable

    override fun read(cb: CharBuffer): Int {
        val position = cb.position()
        if (cb.hasArray()) {
            se.read(cb.array(), cb.arrayOffset() + position, cb.remaining())
        } else {
            while (cb.hasRemaining()) {
                cb.put(se.read())
            }
        }
        return cb.position() - position
    }
}
