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

import org.tobi29.graphics.Bitmap
import org.tobi29.io.ByteStreamInputStream
import org.tobi29.io.IOException
import org.tobi29.io.Path
import org.tobi29.io.ReadableByteStream
import org.tobi29.math.vector.Vector2i
import org.tobi29.tilemaps.Frame
import org.tobi29.tilemaps.Sprite
import org.tobi29.tilemaps.Tile
import org.tobi29.tilemaps.cut
import org.tobi29.tilemaps.tiled.TMXObjectLayer
import org.tobi29.tilemaps.tiled.TMXTileMetaData
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.SAXException
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

internal suspend fun ReadableByteStream.readTilesetFile(
    tileDimensions: Vector2i,
    path: Path,
    firstGid: Int,
    warn: (String) -> Unit
): List<TMXTileMetaData?> = ByteStreamInputStream(this).readTilesetFile(
    tileDimensions, path, firstGid, warn
)

internal suspend fun InputStream.readTilesetFile(
    tileDimensions: Vector2i,
    path: Path,
    firstGid: Int,
    warn: (String) -> Unit
): List<TMXTileMetaData?> = try {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val tsDoc = builder.parse(this, "")
    tsDoc.readTilesetFile(tileDimensions, path, firstGid, warn)
} catch (e: SAXException) {
    throw IOException(e)
}

private suspend fun Document.readTilesetFile(
    tileDimensions: Vector2i,
    path: Path,
    firstGid: Int,
    warn: (String) -> Unit
): List<TMXTileMetaData?> {
    val tsNodeList = getElementsByTagName("tileset")

    val tsNode = tsNodeList.item(0)
            ?: throw IOException("No node in external tileset")

    return tsNode.readTileset(tileDimensions, path, firstGid, warn)
}

internal suspend fun Node.readTileset(
    tileDimensions: Vector2i,
    path: Path,
    warn: (String) -> Unit
): List<TMXTileMetaData?> {
    val firstGid = getAttributeInt("firstgid") ?: 1
    return readTileset(tileDimensions, path, firstGid, warn)
}

internal suspend fun Node.readTileset(
    tileDimensions: Vector2i,
    path: Path,
    firstGid: Int,
    warn: (String) -> Unit
): List<TMXTileMetaData?> {
    val source = getAttributeValue("source")

    return if (source != null) {
        val asset = path[source]
        asset.readAsync {
            it.readTilesetFile(
                tileDimensions, asset.parent ?: path, firstGid, warn
            )
        }
    } else {
        val name = getAttributeValue("name") ?: ""
        val tileWidth = getAttributeInt("tilewidth") ?: tileDimensions.x
        val tileHeight = getAttributeInt("tileheight") ?: tileDimensions.y
        val tileSpacing = getAttributeInt("spacing") ?: 0
        val tileMargin = getAttributeInt("margin") ?: 0

        val tiles = HashMap<Int, TileBuilder?>()
        var hasTilesetImage = false

        childNodes.forEachElement { child ->
            when (child.nodeNameL) {
                "image" -> {
                    if (hasTilesetImage) {
                        // We should be able to handle those fine, so issuing
                        // this warning ought to be enough
                        warn("Multiple image elements in tileset")
                    }
                    hasTilesetImage = true

                    val (image, _) = child.readImage(path)
                    cut(
                        image, tileWidth, tileHeight, tileSpacing,
                        tileMargin, firstGid, name
                    ).forEach { tiles[it.id] = TileBuilder(it) }
                }
                "tile" -> {
                    val tile = child.readTile(firstGid, name, path)
                    if (tile.id !in tiles) tiles[tile.id] = TileBuilder(tile)
                }
            }
        }
        childNodes.forEachElement { child ->
            when (child.nodeNameL) {
                "tile" -> child.readTile(firstGid, name, tiles, path, warn)

            }
        }
        tiles.values.mapNotNull { it?.toMetaData() }
    }
}

private suspend fun Node.readTile(
    firstGid: Int,
    tileset: String,
    path: Path
): Tile {
    val id = firstGid + requireAttributeInt("id")

    var image: Pair<Bitmap<*, *>, Vector2i>? = null
    childNodes.forEachElement { child ->
        when (child.nodeNameL) {
            "image" -> {
                image = child.readImage(path)
            }
        }
    }
    val sprite = Sprite(
        if (image == null) {
            emptyList()
        } else {
            listOf(Frame(1.0, image!!.first))
        }
    )
    return Tile(sprite, image?.second ?: Vector2i.ZERO, id, tileset)
}

private suspend fun Node.readTile(
    firstGid: Int,
    tileset: String,
    tiles: MutableMap<Int, TileBuilder?>,
    assetProvider: Path,
    warn: (String) -> Unit
) {
    val id = firstGid + requireAttributeInt("id")

    val tile = tiles[id]
            ?: TileBuilder(id, tileset).also { tiles[id] = it }
    childNodes.forEachElement { child ->
        when (child.nodeNameL) {
            "properties" -> {
                child.readProperties(tile.properties)
            }
            "animation" -> {
                val animation = child.unmarshalAnimation()
                val frames = animation.map {
                    val animationTile = tiles[firstGid + it.id]
                            ?: throw IOException(
                                "Invalid animation tile: ${it.id}"
                            )
                    val frame = animationTile.sprite?.frames?.firstOrNull()
                            ?: throw IOException(
                                "No frames on tile: ${animationTile.id}"
                            )
                    Frame(it.duration, frame.image)
                }
                tile.sprite = Sprite(frames)
            }
            "objectgroup" -> {
                tile.objects =
                        child.readbjectGroup(emptyMap(), assetProvider, warn)
            }
        }
    }
}

private fun Node.unmarshalAnimation(): List<TileFrame> {
    val frames = ArrayList<TileFrame>()

    childNodes.forEachElement { child ->
        when (child.nodeNameL) {
            "frame" -> {
                frames.add(child.unmarshalFrame())
            }
        }
    }

    return frames
}

private fun Node.unmarshalFrame(): TileFrame {
    val id = requireAttributeInt("tileid")
    val duration = requireAttributeInt("duration")
    return TileFrame(id, duration / 1000.0)
}

private class TileFrame(
    val id: Int,
    val duration: Double
)

private class TileBuilder(
    val id: Int,
    val tileset: String
) {
    var sprite: Sprite? = null
    var size: Vector2i = Vector2i.ZERO
    val properties = HashMap<String, String>()
    var objects: TMXObjectLayer? = null

    constructor(
        sprite: Sprite,
        size: Vector2i,
        id: Int,
        tileset: String,
        properties: Map<String, String>? = null
    ) : this(id, tileset) {
        this.sprite = sprite
        this.size = size
        properties?.let { this.properties.putAll(it) }
    }

    constructor(tile: Tile) : this(
        tile.sprite, tile.size, tile.id,
        tile.tileSet
    )

    private fun toTile(): Tile {
        val sprite = sprite ?: throw IOException("Missing sprite")
        val size = size
        return Tile(sprite, size, id, tileset)
    }

    fun toMetaData(): TMXTileMetaData {
        val properties = properties
        val objects = objects
        return TMXTileMetaData(
            toTile(),
            properties,
            objects
        )
    }
}
