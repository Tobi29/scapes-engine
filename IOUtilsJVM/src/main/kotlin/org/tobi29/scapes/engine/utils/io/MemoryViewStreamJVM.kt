package org.tobi29.scapes.engine.utils.io

import java.nio.ByteBuffer

val DefaultJavaBufferMemoryViewProvider: MemoryViewProvider<ByteBufferView> =
        JavaBufferMemoryViewProvider(::ByteBuffer)

fun JavaBufferMemoryViewProvider(
        bufferProvider: (Int) -> ByteBuffer
): MemoryViewProvider<ByteBufferView> = {
    bufferProvider(it).let {
        if (it.order() == BIG_ENDIAN) ByteBufferViewBE(
                it)
        else ByteBufferViewLE(it)
    }
}
