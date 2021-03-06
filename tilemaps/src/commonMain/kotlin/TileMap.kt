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

import org.tobi29.arrays.Array2
import org.tobi29.arrays.array2OfNulls
import org.tobi29.arrays.fill
import org.tobi29.io.viewBE
import org.tobi29.io.tag.*
import kotlin.collections.forEach
import kotlin.collections.set

fun Array2<out Tile?>.write(map: ReadWriteTagMap) {
    map["Width"] = width.toTag()
    map["Height"] = height.toTag()
    map["Tiles"] = ByteArray(size shl 2).apply {
        val buffer = viewBE
        var i = 0
        this@write.forEach { buffer.setInt(i, it?.id ?: -1).also { i += 4 } }
    }.toTag()
}

inline fun <reified T : Tile> MutableTag.toTileMap(tileSets: TileSets<T>): Array2<T?>? {
    return toTileMap(tileSets) { width, height -> array2OfNulls(width, height) }
}

fun <T : Tile> MutableTag.toTileMap(tileSets: TileSets<T>,
                                    arraySupplier: (Int, Int) -> Array2<T?>): Array2<T?>? {
    val map = toMap() ?: return null
    val width = map["Width"]?.toInt() ?: return null
    val height = map["Height"]?.toInt() ?: return null
    val array = arraySupplier(width, height)
    map["Tiles"]?.toByteArray()?.let { tiles ->
        if (tiles.size != array.size shl 2) {
            throw IllegalArgumentException("Tile array has invalid size")
        }
        val buffer = tiles.viewBE
        var i = 0
        array.fill { _, _ ->
            tileSets.tile(buffer.getInt(i)).also { i += 4 }
        }
    }
    return array
}
