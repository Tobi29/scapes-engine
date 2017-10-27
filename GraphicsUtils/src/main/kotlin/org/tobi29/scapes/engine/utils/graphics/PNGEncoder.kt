package org.tobi29.scapes.engine.utils.graphics

import org.tobi29.scapes.engine.utils.io.*

// TODO: @Throws(IOException::class)
fun encodePNG(image: Image,
              stream: WritableByteStream,
              level: Int,
              alpha: Boolean) {
    stream.put(PNG_HEADER.view)
    stream.writeImage(image, level, alpha)
}

private fun WritableByteStream.writeImage(image: Image,
                                          level: Int,
                                          alpha: Boolean) {
    val header = ByteArray(13).viewBE.apply {
        setInt(0, image.width)
        setInt(4, image.height)
        setByte(8, 8)
        setByte(9, if (alpha) 6 else 2.toByte())
        setByte(10, 0)
        setByte(11, 0)
        setByte(12, 0)
    }
    writeChunk(TYPE_IHDR, header)
    val data = if (alpha) {
        BufferedReadChannelStream(object : ReadableByteChannel {
            private var i = -1
            private var y = 0
            private var position = 0

            // TODO: Test and optimize
            override fun read(buffer: ByteView): Int {
                if (y >= image.height) {
                    return -1
                }
                var positionWrite = 0
                while (positionWrite < buffer.size) {
                    if (y < image.height) {
                        if (i == -1) {
                            buffer[positionWrite++] = 0
                            i++
                        } else if (i < image.width shl 2) {
                            val length = (buffer.size - positionWrite)
                                    .coerceAtMost((image.width shl 2) - i)
                            image.view.getBytes(position,
                                    buffer.slice(positionWrite, length))
                            position += length
                            positionWrite += length
                            i += length
                        }
                        if (i >= image.width shl 2) {
                            i = -1
                            y++
                        }
                    } else break
                }
                return positionWrite
            }

            override fun isOpen() = true
            override fun close() {}
        })
    } else {
        BufferedReadChannelStream(object : ReadableByteChannel {
            private var x = -1
            private var y = 0
            private var i = 0
            private var position = 0

            // TODO: Test and optimize
            override fun read(buffer: ByteView): Int {
                if (y >= image.height) {
                    return -1
                }
                var positionWrite = 0
                while (positionWrite < buffer.size) {
                    if (y < image.height) {
                        if (x == -1) {
                            buffer[positionWrite++] = 0
                            x++
                        } else if (x < image.width) {
                            if (i < 3) {
                                buffer[positionWrite++] =
                                        image.view.getByte(position++)
                                i++
                            } else if (i < 4) {
                                position++
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
                    } else break
                }
                return positionWrite
            }

            override fun isOpen() = true
            override fun close() {}
        })
    }
    val write = BufferedWriteChannelStream(object : WritableByteChannel {
        override fun write(buffer: ByteViewRO): Int {
            writeChunk(TYPE_IDAT, buffer)
            return buffer.size
        }

        override fun isOpen() = true
        override fun close() {}
    })
    CompressionUtil.compress(data, write, level)
    write.flush()
    writeChunk(TYPE_IEND)
}

private fun WritableByteStream.writeChunk(type: Int,
                                          chunk: ByteViewRO? = null) {
    var crc = initChainCRC32()
    crc = chainCRC32(crc, (type ushr 24).toByte(), zlibTable)
    crc = chainCRC32(crc, (type ushr 16).toByte(), zlibTable)
    crc = chainCRC32(crc, (type ushr 8).toByte(), zlibTable)
    crc = chainCRC32(crc, type.toByte(), zlibTable)
    if (chunk == null) {
        putInt(0)
        putInt(type)
    } else {
        repeat(chunk.size) {
            crc = chainCRC32(crc, chunk[it], zlibTable)
        }
        putInt(chunk.size)
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
