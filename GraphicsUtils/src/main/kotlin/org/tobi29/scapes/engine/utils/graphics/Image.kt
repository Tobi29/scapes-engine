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

import org.tobi29.scapes.engine.utils.io.*
import org.tobi29.scapes.engine.utils.math.vector.Vector2i
import org.tobi29.scapes.engine.utils.tag.*

class Image(val width: Int = 1,
            val height: Int = 1,
            buffer: ByteBuffer = ByteBuffer(
                    width * height shl 2)) : TagMapWrite {
    val buffer = buffer
        get() = field.asReadOnlyBuffer()

    val size by lazy { Vector2i(width, height) }

    init {
        if (buffer.remaining() != width * height shl 2) {
            throw IllegalArgumentException("Backing buffer sized incorrectly")
        }
    }

    override fun write(map: ReadWriteTagMap) {
        map["Width"] = width.toTag()
        map["Height"] = height.toTag()
        map["RGBA"] = buffer.asArray().toTag()
    }
}

fun MutableTag.toImage(bufferProvider: ByteBufferProvider = DefaultByteBufferProvider): Image? {
    val map = toMap() ?: return null
    val width = map["Width"]?.toInt() ?: return null
    val height = map["Height"]?.toInt() ?: return null
    val rgba = map["RGBA"]?.toByteArray() ?: return null
    val buffer = bufferProvider.reallocate(rgba.asByteBuffer())
    return Image(width, height, buffer)
}
