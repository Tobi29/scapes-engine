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
import org.tobi29.arrays.asBytesRO
import org.tobi29.arrays.sliceOver
import org.tobi29.io.readAsByteArray
import org.tobi29.io.tag.*

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
        return Ints2BytesBitmap(it.sliceOver(), width, height, RGBA)
    }
    return null
}

private fun IntsRO2.unwrapByteArray(): ByteArray =
    asBytesRO().readAsByteArray()
