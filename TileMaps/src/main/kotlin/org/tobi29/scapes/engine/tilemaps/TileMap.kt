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

import org.tobi29.scapes.engine.utils.Array2
import org.tobi29.scapes.engine.utils.array2OfNulls
import org.tobi29.scapes.engine.utils.assert
import org.tobi29.scapes.engine.utils.fill
import org.tobi29.scapes.engine.utils.io.ByteBuffer
import org.tobi29.scapes.engine.utils.tag.*

fun Array2<out Tile?>.write(map: ReadWriteTagMutableMap) {
    map["Width"] = width.toTag()
    map["Height"] = height.toTag()
    map["Tiles"] = ByteArray(size shl 2).apply {
        val buffer = ByteBuffer.wrap(this)
        this@write.forEach { buffer.putInt(it?.id ?: -1) }
        assert { !buffer.hasRemaining() }
    }.toTag()
}

fun MutableTag.toTileMap(tileSets: TileSets): Array2<Tile?>? {
    val map = toMap() ?: return null
    val width = map["Width"]?.toInt() ?: return null
    val height = map["Height"]?.toInt() ?: return null
    val array = array2OfNulls<Tile>(width, height)
    map["Tiles"]?.toByteArray()?.let { tiles ->
        if (tiles.size != array.size shl 2) {
            throw IllegalArgumentException("Tile array has invalid size")
        }
        val buffer = ByteBuffer.wrap(tiles)
        array.fill { _, _ -> tileSets.tile(buffer.int) }
    }
    return array
}
