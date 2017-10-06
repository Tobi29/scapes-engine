package org.tobi29.scapes.engine.utils.io.filesystem

import org.tobi29.scapes.engine.utils.io.JavaByteChannel

fun java.nio.channels.FileChannel.toChannel(): FileChannel =
        object : JavaFileChannel {
            override val java = this@toChannel
        }

interface JavaFileChannel : JavaByteChannel, FileChannel {
    override val java: java.nio.channels.SeekableByteChannel

    override fun position() = java.position()

    override fun position(newPosition: Long) = apply {
        java.position(newPosition)
    }

    override fun size() = java.size()

    override fun truncate(size: Long) = apply {
        java.truncate(size)
    }

    override fun isOpen(): Boolean = super<JavaByteChannel>.isOpen()

    override fun close() = super<JavaByteChannel>.close()
}
