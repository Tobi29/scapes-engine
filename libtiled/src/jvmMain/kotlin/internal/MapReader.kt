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

package org.tobi29.tilemaps.tiled.internal

import org.tobi29.arrays.Array2
import org.tobi29.arrays.array2OfNulls
import org.tobi29.base64.fromBase64
import org.tobi29.graphics.Bitmap
import org.tobi29.io.IOException
import org.tobi29.io.Path
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.Vector2i
import org.tobi29.math.vector.plus
import org.tobi29.stdex.readOnly
import org.tobi29.tilemaps.Tile
import org.tobi29.tilemaps.TileSets
import org.tobi29.tilemaps.tiled.*
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream

internal suspend fun Document.readTMXMap(
    assetProvider: Path,
    warn: (String) -> Unit
): TMXMap {
    val mapNode = documentElement

    if (mapNode.nodeName != "map") throw IOException("Not a valid tmx map file.")

    var mapSize = mapNode.getAttributeVector2i("width", "height")
            ?: Vector2i.ZERO

    val orientation =
        mapNode.getAttributeValue("orientation")?.toLowerCase()
                ?: "orthogonal"
    val tileDimensions =
        mapNode.requireAttributeVector2i("tilewidth", "tileheight")

    if (mapSize.x <= 0 || mapSize.y <= 0) {
        // Maybe this map is still using the dimensions element
        val l = getElementsByTagName("dimensions")
        var i = 0
        while (true) {
            val item = l.item(i) ?: break
            if (item.parentNode === mapNode) {
                mapSize = item.getAttributeVector2i("width", "height")
                        ?: Vector2i.ZERO
            }
            i++
        }
    }

    if (mapSize.x <= 0 || mapSize.y <= 0) {
        throw IOException("Couldn't locate map dimensions.")
    }

    when (orientation) {
        "orthogonal" -> {
        }
        else -> throw IOException("Unsupported orientation: $orientation")
    }

    val properties = mapNode.readProperties()

    val l = getElementsByTagName("tileset")
    var i = 0
    val tiles = HashMap<Int, Tile>()
    val tileMetaData = HashMap<Tile, TMXTileMetaData>()
    while (true) {
        val item = l.item(i) ?: break
        item.readTileset(tileDimensions, assetProvider, warn).asSequence()
            .filterNotNull().forEach { tile ->
                tiles[tile.tile.id] = tile.tile
                if (!tile.isEmpty()) tileMetaData[tile.tile] = tile
            }
        i++
    }
    val tileSets = TileSets(tiles)

    val layers = ArrayList<TMXLayer>()
    mapNode.childNodes.forEachElement { child ->
        when (child.nodeNameL) {
            "layer" ->
                layers.add(child.readLayer(tiles, mapSize, warn))
            "objectgroup" ->
                layers.add(child.readbjectGroup(tiles, assetProvider, warn))
            "tileset", "terraintype", "wangsets" -> {
                // Ignore
            }
            else -> warn("Unknown layer kind: ${child.nodeName}")
        }
    }
    return TMXMap(
        mapSize, tileDimensions, tileSets, tileMetaData, properties, layers
    )
}

private fun Node.readLayer(
    tiles: Map<Int, Tile>,
    mapSize: Vector2i,
    warn: (String) -> Unit
): TMXTileLayer {
    val name = getAttributeValue("name") ?: ""
    val size = getAttributeVector2i("width", "height") ?: Vector2i(
        mapSize.x, mapSize.y
    )
    val offset = getAttributeVector2i("x", "y") ?: Vector2i.ZERO

    val opacity = getAttributeDouble("opacity") ?: 1.0
    val visible = (getAttributeInt("visible") ?: 1) != 0

    val properties = readProperties()

    val map = array2OfNulls<Tile>(size.x, size.y)
    val tileProperties = HashMap<Vector2i, Map<String, String>>()

    childNodes.forEachElement { child ->
        when (child.nodeNameL) {
            "data" -> {
                when (child.getAttributeValue("encoding")?.toLowerCase()) {
                    "base64" -> child.readBase64Data(tiles, map, warn)
                    "csv" -> child.readCsvData(tiles, map, warn)
                    null -> child.readXmlData(tiles, map, warn)
                    else -> throw IOException(
                        "Invalid encoding for tile layer ${
                        child.getAttributeValue("encoding")}"
                    )
                }
            }
            "tileproperties" -> {
                var tpn: Node? = child.firstChild
                while (tpn != null) {
                    if ("tile".equals(tpn.nodeName, ignoreCase = true)) {
                        val x = tpn.getAttributeInt("x") ?: -1
                        val y = tpn.getAttributeInt("y") ?: -1
                        val tip = tpn.readProperties()

                        tileProperties[Vector2i(x, y)] = tip.readOnly()
                    }
                    tpn = tpn.nextSibling
                }
            }
        }
    }

    return TMXTileLayer(
        name, offset, visible, opacity, properties,
        { map.copyOf() }, tileProperties
    )
}

internal suspend fun Node.readbjectGroup(
    tiles: Map<Int, Tile>,
    assetProvider: Path,
    warn: (String) -> Unit
): TMXObjectLayer {
    val name = getAttributeValue("name") ?: ""
    val properties = readProperties()
    val offset = getAttributeVector2d("x", "y") ?: Vector2d.ZERO

    val list = ArrayList<TMXMapObject>()
    childNodes.forEachElement { child ->
        when (child.nodeNameL) {
            "object" -> {
                list.add(
                    child.readMapObject(offset, tiles, assetProvider, warn)
                )
            }
        }
    }
    return TMXObjectLayer(name, properties, list)
}

private fun Node.readBase64Data(
    tiles: Map<Int, Tile>,
    map: Array2<Tile?>,
    warn: (String) -> Unit
) {
    val cdata = firstChild
    if (cdata != null) {
        val dec = cdata.nodeValue.trim { it <= ' ' }.fromBase64()
        val bStreamIn = ByteArrayInputStream(dec)
        val streamIn =
            when (getAttributeValue("compression")?.toLowerCase()) {
                "gzip" -> GZIPInputStream(bStreamIn, map.width * map.height * 4)
                "zlib" -> InflaterInputStream(bStreamIn)
                null, "" -> bStreamIn
                else -> throw IOException(
                    "Unrecognized compression method ${
                    getAttributeValue("compression")}"
                )
            }

        for (y in 0 until map.height) {
            for (x in 0 until map.width) {
                var tileId = 0
                tileId = tileId or streamIn.read()
                tileId = tileId or (streamIn.read() shl 8)
                tileId = tileId or (streamIn.read() shl 16)
                tileId = tileId or (streamIn.read() shl 24)

                map[x, y] = getTileForTileGID(tileId, tiles, { warn(it) })
            }
        }
    }
}

private fun Node.readCsvData(
    tiles: Map<Int, Tile>,
    map: Array2<Tile?>,
    warn: (String) -> Unit
) {
    val csvText =
        when (getAttributeValue("compression")?.toLowerCase()) {
            null, "" -> textContent
            else -> throw IOException(
                "Unrecognized compression method ${
                getAttributeValue("compression")}"
            )
        }

    val csvTileIds =
        csvText.trim { it <= ' ' }    // trim 'space', 'tab', 'newline'. pay attention to additional unicode chars like \u2028, \u2029, \u0085 if necessary
            .split("[\\s]*,[\\s]*".toRegex()).dropLastWhile(
                String::isEmpty
            ).toTypedArray()

    if (csvTileIds.size != map.width * map.height) {
        throw IOException(
            "Number of tiles does not match the layer's width and height"
        )
    }

    for (y in 0 until map.height) {
        for (x in 0 until map.width) {
            val sTileId = csvTileIds[x + y * map.height]
            val tileId = Integer.parseInt(sTileId)

            map[x, y] = getTileForTileGID(tileId, tiles, warn)
        }
    }
}

private fun Node.readXmlData(
    tiles: Map<Int, Tile>,
    map: Array2<Tile?>,
    warn: (String) -> Unit
) {
    var x = 0
    var y = 0
    childNodes.forEachElement { child ->
        when (child.nodeNameL) {
            "tile" -> {
                val tileId = child.getAttributeInt("gid") ?: -1
                map[x, y] = getTileForTileGID(tileId, tiles, warn)
                x++
                if (x == map.width) {
                    x = 0
                    y++
                }
                if (y == map.height) return
            }
        }
    }
}

private suspend fun Node.readMapObject(
    offset: Vector2d,
    tiles: Map<Int, Tile>,
    assetProvider: Path,
    warn: (String) -> Unit
): TMXMapObject {
    val name = getAttributeValue("name") ?: "Object"
    val type = getAttributeValue("type") ?: ""
    val gid = getAttributeInt("gid") ?: -1
    val pos = (getAttributeVector2d("x", "y") ?: Vector2d.ZERO) + offset
    val width = getAttributeDouble("width") ?: 0.0
    val height = getAttributeDouble("height") ?: 0.0

    var shape: TMXShape = TMXShape.Rectangle(Vector2d(width, height))
    val tile = getTileForTileGID(gid, tiles, warn)
    var image: Pair<Bitmap<*, *>, Vector2i>? = null

    val properties = readProperties()

    val children = childNodes
    loop@ for (i in 0 until children.length) {
        val child = children.item(i)
        when (child.nodeNameL) {
            "image" -> {
                image = child.readImage(assetProvider)
                break@loop
            }
            "ellipse" -> {
                shape = TMXShape.Ellipse(Vector2d(width, height))
            }
            "polygon" -> {
                val pointsAttribute = child.requireAttributeValue("points")
                shape = TMXShape.Polygon(pointsAttribute.parseVector2dList())
            }
            "polyline" -> {
                val pointsAttribute = child.requireAttributeValue("points")
                shape = TMXShape.Polyline(pointsAttribute.parseVector2dList())
            }
        }
    }

    return TMXMapObject(pos, shape, properties, name, type, image?.first, tile)
}

private fun getTileForTileGID(
    tileId: Int,
    tiles: Map<Int, Tile>,
    warn: (String) -> Unit
): Tile? {
    if (tileId <= 0) {
        return null
    }
    val tile = tiles[tileId]
    if (tile == null) {
        warn("Invalid tile-id: $tileId")
    }
    return tile
}
