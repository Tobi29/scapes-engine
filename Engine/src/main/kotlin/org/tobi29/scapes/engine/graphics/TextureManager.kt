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

package org.tobi29.scapes.engine.graphics

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.resource.Resource
import org.tobi29.scapes.engine.utils.computeAbsent
import org.tobi29.scapes.engine.utils.graphics.decodePNG
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.io.readProperties
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.tag.TagMap
import org.tobi29.scapes.engine.utils.tag.toInt
import java.util.*

class TextureManager(private val engine: ScapesEngine) {
    private val cache = WeakHashMap<String, Resource<Texture>>()

    @Synchronized
    operator fun get(asset: String): Resource<Texture> {
        return cache.computeAbsent(asset) { load(asset) }
    }

    fun empty(): Texture {
        return engine.graphics.textureEmpty()
    }

    private fun load(asset: String): Resource<Texture> {
        return engine.resources.load res@ {
            val files = engine.files
            val imageResource = files[asset + ".png"]
            val propertiesResource = files[asset + ".properties"]
            val properties = if (propertiesResource.exists()) {
                propertiesResource.read(::readProperties)
            } else {
                TagMap()
            }
            imageResource.read { texture(it, properties) }
        }
    }

    private fun texture(stream: ReadableByteStream,
                        properties: TagMap): Texture {
        return engine.graphics.createTexture(decodePNG(stream,
                { engine.allocate(it) }),
                properties["Mipmaps"]?.toInt() ?: 4,
                properties["MinFilter"]?.toString()?.let { TextureFilter[it] } ?: TextureFilter.NEAREST,
                properties["MagFilter"]?.toString()?.let { TextureFilter[it] } ?: TextureFilter.NEAREST,
                properties["WrapS"]?.toString()?.let { TextureWrap[it] } ?: TextureWrap.REPEAT,
                properties["WrapT"]?.toString()?.let { TextureWrap[it] } ?: TextureWrap.REPEAT)
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
