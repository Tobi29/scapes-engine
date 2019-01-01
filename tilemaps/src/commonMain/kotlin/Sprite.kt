/*
 * Copyright 2012-2019 Tobi29
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

import org.tobi29.graphics.Bitmap
import org.tobi29.graphics.toBitmap
import org.tobi29.graphics.toTag
import org.tobi29.io.tag.*
import kotlin.collections.set

data class Sprite(val frames: List<Frame>) : TagMapWrite {
    constructor(vararg frames: Frame) : this(frames.toList())

    override fun write(map: ReadWriteTagMap) {
        map["Frames"] = TagList(
            frames.asSequence().map { it.toTag() })
    }
}

fun MutableTag.toSprite(): Sprite? {
    val map = toMap() ?: return null
    val frames = map["Frames"]?.toList() ?: return null
    return Sprite(frames.mapNotNull { it.toFrame() })
}

data class Frame(
    val duration: Double,
    val image: Bitmap<*, *>
) : TagMapWrite {
    override fun write(map: ReadWriteTagMap) {
        map["Duration"] = duration.toTag()
        map["Image"] = image.toTag()
    }
}

fun MutableTag.toFrame(): Frame? {
    val map = toMap() ?: return null
    val duration = map["Duration"]?.toDouble() ?: return null
    val image = map["Image"]?.toBitmap() ?: return null
    return Frame(duration, image)
}
