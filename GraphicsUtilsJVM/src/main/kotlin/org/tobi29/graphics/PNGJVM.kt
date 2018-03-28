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

import ar.com.hjg.pngj.PngReaderByte
import ar.com.hjg.pngj.PngjException
import org.tobi29.io.*
import java.io.InputStream

actual suspend fun decodePng(asset: ReadSource): Bitmap<*, *> {
    return asset.readAsync { decodePng(it) }
}

actual suspend fun decodePng(stream: ReadableByteStream): Bitmap<*, *> {
    return decodePNG(ByteStreamInputStream(stream))
}

// TODO: @Throws(IOException::class)
fun decodePNG(streamIn: InputStream): Bitmap<*, *> {
    try {
        val reader = PngReaderByte(streamIn)
        val width = reader.imgInfo.cols
        val height = reader.imgInfo.rows
        val fillAlpha = !reader.imgInfo.alpha
        val buffer = ByteArray(width * height shl 2).view
        var positionWrite = 0
        if (fillAlpha) {
            for (i in 0 until height) {
                val line = reader.readRowByte()
                val array = line.scanlineByte
                var j = 0
                while (j < array.size) {
                    buffer.setByte(positionWrite++, array[j++])
                    buffer.setByte(positionWrite++, array[j++])
                    buffer.setByte(positionWrite++, array[j++])
                    buffer.setByte(positionWrite++, 0xFF.toByte())
                }
            }
        } else {
            for (i in 0 until height) {
                val line = reader.readRowByte()
                buffer.setBytes(positionWrite, line.scanlineByte.view)
                positionWrite += line.scanlineByte.size
            }
        }
        reader.end()
        return IntByteViewBitmap(buffer, width, height, RGBA)
    } catch (e: PngjException) {
        throw IOException(e)
    }
}
