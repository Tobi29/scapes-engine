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

package org.tobi29.tilemaps.tiled

import org.tobi29.arrays.Array2
import org.tobi29.graphics.Bitmap
import org.tobi29.math.vector.Vector2i
import org.tobi29.tilemaps.Tile
import org.tobi29.tilemaps.TileSets

data class TMXMap(
    val size: Vector2i,
    val tileDimensions: Vector2i,
    val tileSets: TileSets<Tile>,
    val tileMetaData: Map<Tile, TMXTileMetaData>,
    val properties: Map<String, String>,
    val layers: List<TMXLayer>
)

data class TMXTileMetaData(
    val tile: Tile,
    val properties: Map<String, String> = emptyMap(),
    val objects: TMXObjectLayer? = null
) {
    fun isEmpty(): Boolean = properties.isEmpty() && objects == null
}

sealed class TMXLayer {
    abstract val name: String
    abstract val properties: Map<String, String>
}

data class TMXObjectLayer(
    override val name: String,
    override val properties: Map<String, String>,
    val objects: List<TMXMapObject>
) : TMXLayer()

class TMXTileLayer(
    override val name: String,
    val offset: Vector2i,
    val visible: Boolean,
    val opacity: Double,
    override val properties: Map<String, String>,
    private val map: () -> Array2<Tile?>,
    val tileProperties: Map<Vector2i, Map<String, String>>
) : TMXLayer() {
    fun createMap() = map()
}

data class TMXMapObject(
    val pos: Vector2i,
    val shape: TMXShape,
    val properties: Map<String, String>,
    val name: String,
    val type: String,
    val image: Bitmap<*, *>?,
    val tile: Tile?
)

sealed class TMXShape {
    data class Ellipse(val size: Vector2i) : TMXShape()

    data class Rectangle(val size: Vector2i) : TMXShape()

    data class Polygon(val points: List<Vector2i>) : TMXShape()

    data class Polyline(val points: List<Vector2i>) : TMXShape()
}
