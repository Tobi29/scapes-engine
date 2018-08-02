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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.graphics.png

import org.tobi29.arrays.Bytes
import org.tobi29.arrays.HeapBytes
import org.tobi29.arrays.IntsRO2
import org.tobi29.arrays.sliceOver
import org.tobi29.graphics.*
import org.tobi29.io.WritableByteStream
import org.tobi29.io.compression.deflate.DeflateHandle
import org.tobi29.io.compression.deflate.process
import org.tobi29.io.compression.deflate.processFinish
import org.tobi29.io.use
import org.tobi29.io.viewBE
import org.tobi29.stdex.splitToBytes

inline fun encodePng(
    image: Bitmap<*, *>,
    stream: WritableByteStream,
    level: Int = 9,
    alpha: Boolean = true
) {
    DeflateHandle(level).use { deflate ->
        encodePng(image, stream, deflate, alpha)
    }
}

fun encodePng(
    image: Bitmap<*, *>,
    stream: WritableByteStream,
    deflate: DeflateHandle,
    alpha: Boolean = true
) {
    stream.put(PNG_HEADER.sliceOver())
    stream.writeImage(image, deflate, alpha)
}

private fun WritableByteStream.writeImage(
    image: Bitmap<*, *>,
    deflate: DeflateHandle,
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
    val encode = encodeScanlines(image, alpha)
    val outputBuffer = ByteArray(16384).sliceOver()
    while (true) {
        val scanline = encode() ?: break
        deflate.process(scanline, outputBuffer) { buffer ->
            writeChunk(TYPE_IDAT, buffer)
        }
    }
    deflate.processFinish(outputBuffer) { buffer ->
        writeChunk(TYPE_IDAT, buffer)
    }
    writeChunk(TYPE_IEND)
}

private fun encodeScanlines(
    image: Bitmap<*, *>,
    alpha: Boolean
) = when (image.format) {
    is RGBA -> if (alpha) encodeScanlinesRGBA8888(image.cast(RGBA)!!)
    else encodeScanlinesRGB888(image.cast(RGBA)!!)
}

private fun encodeScanlinesRGBA8888(
    image: Bitmap<IntsRO2, RGBA>
) = encodeScanlines(image, 0, 4) { pixel, bytes, i ->
    pixel.splitToBytes { r, g, b, a ->
        bytes[i + 0] = r
        bytes[i + 1] = g
        bytes[i + 2] = b
        bytes[i + 3] = a
    }
}

private fun encodeScanlinesRGB888(
    image: Bitmap<IntsRO2, RGBA>
) = encodeScanlines(image, 0, 3) { pixel, bytes, i ->
    pixel.splitToBytes { r, g, b, _ ->
        bytes[i + 0] = r
        bytes[i + 1] = g
        bytes[i + 2] = b
    }
}

private inline fun encodeScanlines(
    image: Bitmap<IntsRO2, ColorFormatInt>,
    filter: Byte,
    bytesPerPixel: Int,
    crossinline encode: (Int, Bytes, Int) -> Unit
): () -> HeapBytes? {
    val data = image.data
    val width = data.width
    val height = data.height
    val scanline = width * bytesPerPixel
    val bytes = ByteArray(scanline + 1).sliceOver()
    var y = 0
    return {
        if (y >= height) null else {
            bytes[0] = filter
            for (x in 0 until width) {
                encode(image[x, y], bytes, x * bytesPerPixel + 1)
            }
            y++
            bytes
        }
    }
}
