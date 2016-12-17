/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.utils.graphics

import ar.com.hjg.pngj.*
import org.tobi29.scapes.engine.utils.ByteBuffer
import org.tobi29.scapes.engine.utils.io.ByteStreamInputStream
import org.tobi29.scapes.engine.utils.io.ByteStreamOutputStream
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.io.WritableByteStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

@Throws(IOException::class)
fun decodePNG(stream: ReadableByteStream,
              supplier: (Int) -> ByteBuffer = ::ByteBuffer): Image {
    return decodePNG(ByteStreamInputStream(stream), supplier)
}

@Throws(IOException::class)
fun decodePNG(streamIn: InputStream,
              supplier: (Int) -> ByteBuffer = ::ByteBuffer): Image {
    try {
        val reader = PngReaderByte(streamIn)
        val width = reader.imgInfo.cols
        val height = reader.imgInfo.rows
        val fillAlpha = !reader.imgInfo.alpha
        val buffer = supplier(width * height shl 2)
        if (fillAlpha) {
            for (i in 0..height - 1) {
                val line = reader.readRowByte()
                val array = line.scanlineByte
                var j = 0
                while (j < array.size) {
                    buffer.put(array[j++])
                    buffer.put(array[j++])
                    buffer.put(array[j++])
                    buffer.put(0xFF.toByte())
                }
            }
        } else {
            for (i in 0..height - 1) {
                val line = reader.readRowByte()
                buffer.put(line.scanlineByte)
            }
        }
        reader.end()
        buffer.rewind()
        return Image(width, height, buffer)
    } catch (e: PngjException) {
        throw IOException(e)
    }
}

@Throws(IOException::class)
fun encodePNG(image: Image,
              stream: WritableByteStream,
              level: Int,
              alpha: Boolean) {
    encodePNG(image, ByteStreamOutputStream(stream), level, alpha)
}

@Throws(IOException::class)
fun encodePNG(image: Image,
              streamOut: OutputStream,
              level: Int,
              alpha: Boolean) {
    try {
        val width = image.width
        val height = image.height
        val buffer = image.buffer
        val info = ImageInfo(width, height, 8, alpha)
        val writer = PngWriter(streamOut, info)
        writer.setCompLevel(level)
        val line = ImageLineByte(info)
        val scanline = line.scanline
        if (alpha) {
            for (y in height - 1 downTo 0) {
                buffer.position(y * width shl 2)
                var x = 0
                while (x < scanline.size) {
                    scanline[x++] = buffer.get()
                    scanline[x++] = buffer.get()
                    scanline[x++] = buffer.get()
                    scanline[x++] = buffer.get()
                }
                writer.writeRow(line)
            }
        } else {
            for (y in height - 1 downTo 0) {
                buffer.position(y * width shl 2)
                var x = 0
                while (x < scanline.size) {
                    scanline[x++] = buffer.get()
                    scanline[x++] = buffer.get()
                    scanline[x++] = buffer.get()
                    buffer.get()
                }
                writer.writeRow(line)
            }
        }
        writer.end()
    } catch (e: PngjException) {
        throw IOException(e)
    }
}
