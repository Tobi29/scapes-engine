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

import org.tobi29.arrays.Int2ByteArrayRO
import org.tobi29.arrays.IntsRO2
import org.tobi29.io.ByteViewRO
import org.tobi29.io.readAsByteArray
import org.tobi29.io.tag.*
import org.tobi29.io.view
import org.tobi29.stdex.splitToBytes

fun Bitmap<*, *>.toTag(): TagMap = TagMap {
    this["Width"] = width.toTag()
    this["Height"] = height.toTag()
    when (format) {
        RGBA -> this["RGBA"] = cast(RGBA)!!.data.unwrapByteArray().toTag()
    }
}

fun MutableTag.toBitmap(): Bitmap<*, *>? {
    val map = toMap() ?: return null
    val width = map["Width"]?.toInt() ?: return null
    val height = map["Height"]?.toInt() ?: return null
    map["RGBA"]?.toByteArray()?.let {
        return IntByteViewBitmap(it.view, width, height, RGBA)
    }
    return null
}

private fun IntsRO2.unwrapByteArray(): ByteArray = when (this) {
    is Int2ByteArrayRO<*> -> {
        val array = array
        when (array) {
            is ByteViewRO -> array.readAsByteArray()
            else -> ByteArray(array.size) { array[it] }
        }
    }
    else -> ByteArray(width * height shl 2).apply {
        var i = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                this@unwrapByteArray[x, y].splitToBytes { b3, b2, b1, b0 ->
                    this[i++] = b3
                    this[i++] = b2
                    this[i++] = b1
                    this[i++] = b0
                }
            }
        }
    }
}
