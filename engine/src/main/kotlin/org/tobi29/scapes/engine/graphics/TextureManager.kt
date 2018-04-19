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

package org.tobi29.scapes.engine.graphics

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.resource.Resource
import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.stdex.computeAbsent
import org.tobi29.graphics.decodePNG
import org.tobi29.io.IOException
import org.tobi29.io.tag.json.readJSON
import org.tobi29.logging.KLogging
import org.tobi29.io.tag.TagMap
import org.tobi29.io.tag.toInt

class TextureManager(private val engine: ScapesEngine) {
    private val cache = ConcurrentHashMap<String, Resource<Texture>>()

    operator fun get(asset: String): Resource<Texture> {
        return cache.computeAbsent(asset) { load(asset) }
    }

    private fun load(asset: String): Resource<Texture> {
        return engine.resources.load {
            val files = engine.files
            val imageResource = files["$asset.png"]
            val propertiesResource = files["$asset.json"]
            val properties = try {
                propertiesResource.readAsync { readJSON(it) }
            } catch (e: IOException) {
                TagMap()
            }
            engine.graphics.createTexture(decodePNG(imageResource),
                    properties["Mipmaps"]?.toInt() ?: 0,
                    properties["MinFilter"]?.toString()?.let { TextureFilter[it] } ?: TextureFilter.NEAREST,
                    properties["MagFilter"]?.toString()?.let { TextureFilter[it] } ?: TextureFilter.NEAREST,
                    properties["WrapS"]?.toString()?.let { TextureWrap[it] } ?: TextureWrap.REPEAT,
                    properties["WrapT"]?.toString()?.let { TextureWrap[it] } ?: TextureWrap.REPEAT)
        }
    }

    fun clearCache() {
        cache.clear()
    }

    companion object : KLogging()
}
