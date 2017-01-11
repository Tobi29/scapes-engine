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
import org.tobi29.scapes.engine.utils.graphics.Image
import org.tobi29.scapes.engine.utils.graphics.TextureAtlas
import org.tobi29.scapes.engine.utils.graphics.decodePNG
import org.tobi29.scapes.engine.utils.math.min
import java.io.IOException
import java.nio.ByteBuffer

abstract class TextureAtlasEngine<T : TextureAtlasEngineEntry>(val engine: ScapesEngine,
                                                               minSize: Int = 1) : TextureAtlas<T>(
        minSize) {
    var textureMut: Texture? = null
        protected set
    val texture: Texture
        get() = textureMut ?: throw IllegalStateException(
                "Atlas not finished yet")

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

    fun initTexture(mipmaps: Int = 0) {
        textureMut = engine.graphics.createTexture(image, mipmaps)
    }

    companion object : KLogging()
}
