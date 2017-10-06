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

package org.tobi29.scapes.engine.tilemaps

import org.tobi29.scapes.engine.utils.graphics.Image
import org.tobi29.scapes.engine.utils.io.view

fun makeTransparent(image: Image,
                    transStr: String): Image {
    val str = if (!transStr.isEmpty() && transStr[0] == '#') {
        transStr.substring(1)
    } else {
        transStr
    }
    val colorInt = str.toInt(16)
    val transR = (colorInt shr 16 and 0xFF).toByte()
    val transG = (colorInt shr 8 and 0xFF).toByte()
    val transB = (colorInt and 0xFF).toByte()
    val buffer = image.view
    val filteredBuffer = ByteArray(buffer.size).view
    var position = 0
    var positionWrite = 0
    while (position < buffer.size) {
        val r = buffer.getByte(position++)
        val g = buffer.getByte(position++)
        val b = buffer.getByte(position++)
        val a = buffer.getByte(position++)
        if (transR == r && transG == g && transB == b) {
            repeat(4) { filteredBuffer.setByte(positionWrite++, 0) }
        } else {
            filteredBuffer.setByte(positionWrite++, r)
            filteredBuffer.setByte(positionWrite++, g)
            filteredBuffer.setByte(positionWrite++, b)
            filteredBuffer.setByte(positionWrite++, a)
        }
    }
    return Image(image.width, image.height, filteredBuffer)
}
