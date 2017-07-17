/*
 * Copyright 2012-2017 Tobi29
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
import org.tobi29.scapes.engine.utils.io.*
import java.io.InputStream
import java.io.OutputStream

// TODO: @Throws(IOException::class)
impl fun decodePNG(stream: ReadableByteStream,
                   bufferProvider: ByteBufferProvider): Image {
    return decodePNG(ByteStreamInputStream(stream), bufferProvider)
}

// TODO: @Throws(IOException::class)
fun decodePNG(streamIn: InputStream,
              bufferProvider: ByteBufferProvider): Image {
    try {
        val reader = PngReaderByte(streamIn)
        val width = reader.imgInfo.cols
        val height = reader.imgInfo.rows
        val fillAlpha = !reader.imgInfo.alpha
        val buffer = bufferProvider.allocate(width * height shl 2)
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
