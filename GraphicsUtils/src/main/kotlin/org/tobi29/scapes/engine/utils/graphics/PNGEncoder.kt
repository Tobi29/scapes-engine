package org.tobi29.scapes.engine.utils.graphics

import org.tobi29.scapes.engine.utils.io.*

// TODO: @Throws(IOException::class)
fun encodePNG(image: Image,
              stream: WritableByteStream,
              level: Int,
              alpha: Boolean) {
    stream.put(PNG_HEADER)
    stream.writeImage(image, level, alpha)
}

private fun WritableByteStream.writeImage(image: Image,
                                          level: Int,
                                          alpha: Boolean) {
    val header = ByteBuffer(13).apply {
        putInt(image.width)
        putInt(image.height)
        put(8)
        put(if (alpha) 6 else 2.toByte())
        put(0)
        put(0)
        put(0)
        flip()
    }
    writeChunk(TYPE_IHDR, header)
    val data = if (alpha) {
        val buffer = image.buffer
        BufferedReadChannelStream(object : ReadableByteChannel {
            private var x = -1
            private var y = 0

            // TODO: Test and optimize
            override fun read(dst: ByteBuffer): Int {
                if (y >= image.height) {
                    return -1
                }
                val pos = dst.position()
                while (dst.hasRemaining()) {
                    if (y < image.height) {
                        if (x == -1) {
                            dst.put(0)
                            x++
                        } else if (x < image.width) {
                            val length = dst.remaining().coerceAtMost(
                                    image.width - x)
                            val limit = buffer.limit()
                            buffer.limit(buffer.position() + length)
                            dst.put(buffer)
                            buffer.limit(limit)
                            x += length
                        }
                        if (x >= image.width) {
                            x = -1
                            y++
                        }
                    } else {
                        break
                    }
                }
                return dst.position() - pos
            }

            override fun isOpen() = true
            override fun close() {}
        })
    } else {
        val buffer = image.buffer
        BufferedReadChannelStream(object : ReadableByteChannel {
            private var x = -1
            private var y = 0
            private var i = 0

            // TODO: Test and optimize
            override fun read(dst: ByteBuffer): Int {
                if (y >= image.height) {
                    return -1
                }
                val pos = dst.position()
                while (dst.hasRemaining()) {
                    if (y < image.height) {
                        if (x == -1) {
                            dst.put(0)
                            x++
                        } else if (x < image.width) {
                            if (i < 3) {
                                dst.put(buffer.get())
                                i++
                            } else if (i < 4) {
                                buffer.position(buffer.position() + 1)
                                i++
                            } else {
                                i = 0
                                x++
                            }
                        }
                        if (x >= image.width) {
                            x = -1
                            y++
                        }
                    } else {
                        break
                    }
                }
                return dst.position() - pos
            }

            override fun isOpen() = true
            override fun close() {}
        })
    }
    val write = BufferedWriteChannelStream(object : WritableByteChannel {
        override fun write(src: ByteBuffer): Int {
            val pos = src.position()
            writeChunk(TYPE_IDAT, src)
            return src.position() - pos
        }

        override fun isOpen() = true
        override fun close() {}
    })
    CompressionUtil.compress(data, write, level)
    write.flush()
    writeChunk(TYPE_IEND)
}

private fun WritableByteStream.writeChunk(type: Int,
                                          chunk: ByteBuffer? = null) {
    var crc = initChainCRC32()
    crc = chainCRC32(crc, (type ushr 24).toByte(), zlibTable)
    crc = chainCRC32(crc, (type ushr 16).toByte(), zlibTable)
    crc = chainCRC32(crc, (type ushr 8).toByte(), zlibTable)
    crc = chainCRC32(crc, type.toByte(), zlibTable)
    if (chunk == null) {
        putInt(0)
        putInt(type)
    } else {
        crc = computeChainCRC32(crc, chunk, zlibTable)
        chunk.flip()
        putInt(chunk.remaining())
        putInt(type)
        put(chunk)
    }
    putInt(crc.finishChainCRC32())
}

private val PNG_HEADER = byteArrayOf(0x89.toByte(), 0x50.toByte(),
        0x4E.toByte(), 0x47.toByte(), 0x0D.toByte(), 0x0A.toByte(),
        0x1A.toByte(), 0x0A.toByte())
private const val TYPE_IHDR = 0x49484452
private const val TYPE_IDAT = 0x49444154
private const val TYPE_IEND = 0x49454e44

private val zlibTable = tableCRC32(0xedb88320.toInt())
