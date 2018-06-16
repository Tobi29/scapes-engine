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

import kotlinx.coroutines.experimental.Deferred
import org.tobi29.graphics.decodePng
import org.tobi29.logging.KLogging
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.stdex.computeAbsent

class TextureManager(private val engine: ScapesEngine) {
    private val cache = ConcurrentHashMap<TextureReference, Deferred<Texture>>()

    operator fun get(
        asset: String,
        mipmaps: Int = 0,
        minFilter: TextureFilter = TextureFilter.NEAREST,
        magFilter: TextureFilter = TextureFilter.NEAREST,
        wrapS: TextureWrap = TextureWrap.REPEAT,
        wrapT: TextureWrap = TextureWrap.REPEAT
    ): Deferred<Texture> = TextureReference(
        asset,
        mipmaps,
        minFilter, magFilter,
        wrapS, wrapT
    ).let { cache.computeAbsent(it) { load(it) } }

    private fun load(
        reference: TextureReference
    ): Deferred<Texture> = engine.resources.load {
        engine.graphics.createTexture(
            decodePng(engine.files[reference.asset]),
            reference.mipmaps,
            reference.minFilter, reference.magFilter,
            reference.wrapS, reference.wrapT
        )
    }

    fun clearCache() {
        cache.clear()
    }

    companion object : KLogging()
}

private data class TextureReference(
    val asset: String,
    val mipmaps: Int,
    val minFilter: TextureFilter,
    val magFilter: TextureFilter,
    val wrapS: TextureWrap,
    val wrapT: TextureWrap
)
