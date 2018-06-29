/*
 * Copyright 2012-2018 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.graphics

import org.tobi29.arrays.IntsRO2
import org.tobi29.checksums.chainCrc32
import org.tobi29.checksums.finishChainCrc32
import org.tobi29.checksums.initChainCrc32
import org.tobi29.checksums.tableCrc32
import org.tobi29.io.*
import org.tobi29.stdex.splitToBytes

fun encodePng(
    image: Bitmap<*, *>,
    stream: WritableByteStream,
    level: Int,
    alpha: Boolean
) {
    stream.put(PNG_HEADER.view)
    stream.writeImage(image, level, alpha)
}

private fun WritableByteStream.writeImage(
    image: Bitmap<*, *>,
    level: Int,
    alpha: Boolean
) {
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
    val data = encodeScanlines(image, alpha)
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

private fun encodeScanlines(
    image: Bitmap<*, *>,
    alpha: Boolean
) = when (image.format) {
    is RGBA -> encodeScanlinesRGBA8888(image.cast(RGBA)!!, alpha)
}

private fun encodeScanlinesRGBA8888(
    image: Bitmap<IntsRO2, RGBA>,
    alpha: Boolean
) = if (alpha) {
    BufferedReadChannelStream(object : ReadableByteChannel {
        private var x = -1
        private var y = 0
        private var i = 0
        private var color = 0

        override fun read(buffer: ByteView): Int {
            if (y >= image.height) return -1
            var positionWrite = 0
            while (positionWrite < buffer.size) {
                if (y < image.height) {
                    if (x == -1) {
                        buffer[positionWrite++] = 0
                        x++
                    } else if (x < image.width) {
                        if (i == 0) color = image[x, y]
                        buffer[positionWrite++] = when (i++) {
                            0 -> {
                                color.splitToBytes { v, _, _, _ -> v }
                            }
                            1 -> {
                                color.splitToBytes { _, v, _, _ -> v }
                            }
                            2 -> {
                                color.splitToBytes { _, _, v, _ -> v }
                            }
                            3 -> {
                                i = 0
                                x++
                                color.splitToBytes { _, _, _, v -> v }
                            }
                            else -> error("Impossible")
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
} else {
    BufferedReadChannelStream(object : ReadableByteChannel {
        private var x = -1
        private var y = 0
        private var i = 0
        private var color = 0

        override fun read(buffer: ByteView): Int {
            if (y >= image.height) return -1
            var positionWrite = 0
            while (positionWrite < buffer.size) {
                if (y < image.height) {
                    if (x == -1) {
                        buffer[positionWrite++] = 0
                        x++
                    } else if (x < image.width) {
                        if (i == 0) color = image[x, y]
                        buffer[positionWrite++] = when (i++) {
                            0 -> {
                                color.splitToBytes { v, _, _, _ -> v }
                            }
                            1 -> {
                                color.splitToBytes { _, v, _, _ -> v }
                            }
                            2 -> {
                                i = 0
                                x++
                                color.splitToBytes { _, _, v, _ -> v }
                            }
                            else -> error("Impossible")
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

private fun WritableByteStream.writeChunk(
    type: Int,
    chunk: ByteViewRO? = null
) {
    var crc = initChainCrc32()
    crc = chainCrc32(crc, (type ushr 24).toByte(), zlibTable)
    crc = chainCrc32(crc, (type ushr 16).toByte(), zlibTable)
    crc = chainCrc32(crc, (type ushr 8).toByte(), zlibTable)
    crc = chainCrc32(crc, type.toByte(), zlibTable)
    if (chunk == null) {
        putInt(0)
        putInt(type)
    } else {
        crc = chainCrc32(crc, chunk, zlibTable)
        putInt(chunk.size)
        putInt(type)
        put(chunk)
    }
    putInt(crc.finishChainCrc32())
}

private val PNG_HEADER = byteArrayOf(
    0x89.toByte(), 0x50.toByte(),
    0x4E.toByte(), 0x47.toByte(), 0x0D.toByte(), 0x0A.toByte(),
    0x1A.toByte(), 0x0A.toByte()
)
private const val TYPE_IHDR = 0x49484452
private const val TYPE_IDAT = 0x49444154
private const val TYPE_IEND = 0x49454e44

private val zlibTable = tableCrc32(0xedb88320.toInt())
