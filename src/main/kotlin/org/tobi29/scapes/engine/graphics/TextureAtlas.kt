/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.graphics

import mu.KLogging
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.utils.ByteBuffer
import org.tobi29.scapes.engine.utils.graphics.*
import org.tobi29.scapes.engine.utils.io.filesystem.write
import org.tobi29.scapes.engine.utils.math.min
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

abstract class TextureAtlas<T : TextureAtlasEntry>(val engine: ScapesEngine, minBits: Int) {
    protected val textures = ConcurrentHashMap<String, T>()
    protected val sources = ConcurrentHashMap<String, Image>()
    protected val minBits: Int
    protected val minSize: Int
    var texture: Texture? = null
        protected set

    init {
        this.minBits = 4
        minSize = 1 shl minBits
    }

    protected fun path(paths: Array<out String>): String {
        val pathBuilder = StringBuilder(paths[0])
        for (i in 1..paths.size - 1) {
            pathBuilder.append('\n').append(paths[i])
        }
        return pathBuilder.toString()
    }

    protected fun load(path: String): Image {
        return load(arrayOf(path))
    }

    protected fun load(paths: Array<out String>): Image {
        var buffer: ByteBuffer
        var width: Int
        var height: Int
        try {
            val source2 = sources[paths[0]]
            val source: Image
            if (source2 == null) {
                source = engine.files[paths[0]].read { decodePNG(it) }
                sources.put(paths[0], source)
            } else {
                source = source2
            }
            width = source.width
            height = source.height
            if (paths.size > 1) {
                buffer = ByteBuffer(width * height shl 2)
                buffer.put(source.buffer)
                buffer.rewind()
                source.buffer.rewind()
                for (i in 1..paths.size - 1) {
                    val layer2 = sources[paths[i]]
                    val layer: Image
                    if (layer2 == null) {
                        layer = engine.files[paths[i]].read { decodePNG(it) }
                        sources.put(paths[i], layer)
                    } else {
                        layer = layer2
                    }
                    if (layer.width != source.width || layer.height != source.height) {
                        logger.warn { "Invalid size for layered texture from: ${paths[i]}" }
                        continue
                    }
                    var bufferR: Int
                    var bufferG: Int
                    var bufferB: Int
                    var bufferA: Int
                    var layerR: Int
                    var layerG: Int
                    var layerB: Int
                    var layerA: Int
                    val layerBuffer = layer.buffer
                    while (layerBuffer.hasRemaining()) {
                        layerR = layerBuffer.get().toInt() and 0xFF
                        layerG = layerBuffer.get().toInt() and 0xFF
                        layerB = layerBuffer.get().toInt() and 0xFF
                        layerA = layerBuffer.get().toInt() and 0xFF
                        if (layerA == 255) {
                            buffer.put(layerR.toByte())
                            buffer.put(layerG.toByte())
                            buffer.put(layerB.toByte())
                            buffer.put(layerA.toByte())
                        } else if (layerA != 0) {
                            buffer.mark()
                            bufferR = buffer.get().toInt() and 0xFF
                            bufferG = buffer.get().toInt() and 0xFF
                            bufferB = buffer.get().toInt() and 0xFF
                            bufferA = buffer.get().toInt() and 0xFF
                            buffer.reset()
                            val a = layerA / 255.0
                            val oneMinusA = 1.0 - a
                            buffer.put(
                                    (bufferR * oneMinusA + layerR * a).toByte())
                            buffer.put(
                                    (bufferG * oneMinusA + layerG * a).toByte())
                            buffer.put(
                                    (bufferB * oneMinusA + layerB * a).toByte())
                            buffer.put(min(bufferA + layerA, 255).toByte())
                        } else {
                            buffer.position(buffer.position() + 4)
                        }
                    }
                    buffer.rewind()
                }
            } else {
                buffer = source.buffer
            }
        } catch (e: IOException) {
            logger.error { "Failed to load texture: $e" }
            buffer = ByteBuffer(0x400)
            width = minSize
            height = minSize
        }
        return Image(width, height, buffer)
    }

    fun init(engine: ScapesEngine): Int {
        // TODO: Clean up
        val textureList = textures.values.asSequence()
                .sortedBy { it.resolution }.toMutableList<T?>()
        textureList.add(0, null)
        var size = 16
        var atlas = Array(size) { IntArray(size) }
        var tiles = 0
        var boundary = 0
        var x = 0
        var y = 0
        for (i in 1..textureList.size - 1) {
            val texture = textureList[i]!!
            val textureTiles = (texture.resolution() - 1 shr minBits) + 1
            if (textureTiles != tiles) {
                x = 0
                y = 0
                tiles = textureTiles
                boundary = size - tiles
            }
            var flag = true
            while (flag) {
                if (atlas[x][y] == 0 && x <= boundary && y <= boundary) {
                    for (yy in 0..tiles - 1) {
                        val yyy = yy + y
                        for (xx in 0..tiles - 1) {
                            if (xx == 0 && yy == 0) {
                                atlas[x][y] = i
                            } else {
                                atlas[xx + x][yyy] = -1
                            }
                        }
                    }
                    flag = false
                }
                x += textureTiles
                if (x >= size) {
                    y += textureTiles
                    x = 0
                }
                if (y >= size) {
                    y = 0
                    size = size shl 1
                    boundary = size - tiles
                    val newAtlas = Array(size) { IntArray(size) }
                    for (j in atlas.indices) {
                        System.arraycopy(atlas[j], 0, newAtlas[j], 0,
                                atlas[j].size)
                    }
                    atlas = newAtlas
                }
            }
        }
        val imageSize = size shl minBits
        y = 0
        val image = MutableImage(imageSize, imageSize)
        while (y < size) {
            val yy = y shl minBits
            x = 0
            while (x < size) {
                val i = atlas[x][y]
                if (i > 0) {
                    val xx = x shl minBits
                    val texture = textureList[i]!!
                    texture.x = x.toDouble() / size
                    texture.y = y.toDouble() / size
                    texture.tileX = xx
                    texture.tileY = yy
                    texture.size = texture.resolution.toDouble() / imageSize
                    texture.buffer?.let {
                        image.set(xx, yy, texture.resolution,
                                texture.resolution, it)
                    }
                    texture.buffer = null
                }
                x++
            }
            y++
        }
        texture = engine.graphics.createTexture(image.toImage(), 4)
        sources.clear()
        write(engine.home.resolve("$this.png")) {
            encodePNG(image.toImage(), it, 1, true)
        }
        return textures.size
    }

    fun engine(): ScapesEngine {
        return engine
    }

    fun texture(): Texture {
        return texture ?: throw IllegalStateException("Atlas not finished yet")
    }

    companion object : KLogging()
}
