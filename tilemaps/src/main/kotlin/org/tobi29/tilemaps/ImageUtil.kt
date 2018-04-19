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

package org.tobi29.tilemaps

import org.tobi29.arrays.Int2ByteArray
import org.tobi29.arrays.IntsRO2
import org.tobi29.graphics.*
import org.tobi29.io.view
import org.tobi29.stdex.JvmName
import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.splitToBytes

fun Bitmap<*, *>.makeTransparent(
    transStr: String
): Bitmap<*, *> {
    val str = if (!transStr.isEmpty() && transStr[0] == '#') {
        transStr.substring(1)
    } else {
        transStr
    }
    val colorInt = str.toInt(16)
    return when (format) {
        RGBA -> cast(RGBA)!!.makeTransparent(colorInt)
    }
}

@JvmName("makeTransparentRGBA")
private fun Bitmap<IntsRO2, RGBA>.makeTransparent(
    colorInt: Int
): Bitmap<*, *> {
    val transR = (colorInt shr 16 and 0xFF).toByte()
    val transG = (colorInt shr 8 and 0xFF).toByte()
    val transB = (colorInt and 0xFF).toByte()
    val filteredData =
        Int2ByteArray(ByteArray(width * height shl 2).view, width, height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            data[x, y].splitToBytes { r, g, b, a ->
                if (transR == r && transG == g && transB == b) {
                    filteredData[x, y] = combineToInt(0, 0, 0, 0)
                } else {
                    filteredData[x, y] = combineToInt(r, g, b, a)
                }
            }
        }
    }
    return BitmapC(filteredData, RGBA)
}
