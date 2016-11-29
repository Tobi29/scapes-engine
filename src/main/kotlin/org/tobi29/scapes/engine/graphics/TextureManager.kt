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

import java8.util.Maps
import mu.KLogging
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.resource.Resource
import org.tobi29.scapes.engine.utils.graphics.decodePNG
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import java.util.*

class TextureManager(private val engine: ScapesEngine) {
    private val cache = WeakHashMap<String, Resource<Texture>>()

    @Synchronized
    operator fun get(asset: String): Resource<Texture> {
        return Maps.computeIfAbsent(cache, asset) {
            load(asset)
        }
    }

    fun empty(): Texture {
        return engine.graphics.textureEmpty()
    }

    private fun load(asset: String): Resource<Texture> {
        return engine.resources.load({
            empty()
        }) res@ {
            val properties = Properties()
            val files = engine.files
            val imageResource = files[asset + ".png"]
            val propertiesResource = files[asset + ".properties"]
            if (propertiesResource.exists()) {
                propertiesResource.readIO().use { streamIn ->
                    properties.load(streamIn)
                }
            }
            imageResource.read { texture(it, properties) }
        }
    }

    private fun texture(stream: ReadableByteStream,
                        properties: Properties): Texture {
        return engine.graphics.createTexture(decodePNG(stream,
                { engine.allocate(it) }),
                properties.getProperty("Mipmaps", "4").toInt(),
                TextureFilter[properties.getProperty("MinFilter", "Nearest")],
                TextureFilter[properties.getProperty("MagFilter", "Nearest")],
                TextureWrap[properties.getProperty("WrapS", "Repeat")],
                TextureWrap[properties.getProperty("WrapT", "Repeat")])
    }

    fun unbind(gl: GL) {
        empty().bind(gl)
    }

    @Synchronized
    fun clearCache() {
        cache.clear()
    }

    companion object : KLogging()
}
