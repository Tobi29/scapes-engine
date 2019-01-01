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

import kotlinx.coroutines.Deferred
import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.asBytesRO
import org.tobi29.arrays.sliceOver
import org.tobi29.graphics.*
import org.tobi29.io.IOException
import org.tobi29.io.ReadSource
import org.tobi29.io.tag.binary.readBinary
import org.tobi29.scapes.engine.shader.CompiledShader
import org.tobi29.scapes.engine.shader.Expression
import org.tobi29.scapes.engine.shader.toCompiledShader

interface GraphicsObjectSupplier {
    val vaoTracker: GraphicsObjectTracker<Model>
    val textureTracker: GraphicsObjectTracker<Texture>
    val fboTracker: GraphicsObjectTracker<Framebuffer>
    val shaderTracker: GraphicsObjectTracker<Shader>

    fun createTexture(
        width: Int,
        height: Int
    ): Texture = createTexture(
        width, height, ByteArray(width * height * 4).sliceOver(), 0
    )

    fun createTexture(
        image: Bitmap<*, *>,
        mipmaps: Int
    ): Texture = when (image.format) {
        RGBA -> image.cast(RGBA)!!.let {
            createTexture(
                it.width, it.height, it.data.asBytesRO(), mipmaps,
                TextureFilter.NEAREST, TextureFilter.NEAREST,
                TextureWrap.REPEAT, TextureWrap.REPEAT
            )
        }
    }

    fun createTexture(
        width: Int,
        height: Int,
        mipmaps: Int
    ): Texture = createTexture(
        width, height, ByteArray(width * height * 4).sliceOver(), mipmaps
    )

    fun createTexture(
        width: Int,
        height: Int,
        mipmaps: Int,
        minFilter: TextureFilter,
        magFilter: TextureFilter,
        wrapS: TextureWrap,
        wrapT: TextureWrap
    ): Texture = createTexture(
        width, height, ByteArray(width * height * 4).sliceOver(), mipmaps,
        minFilter, magFilter, wrapS, wrapT
    )

    fun createTexture(
        image: Bitmap<*, *>,
        mipmaps: Int = 0,
        minFilter: TextureFilter = TextureFilter.NEAREST,
        magFilter: TextureFilter = TextureFilter.NEAREST,
        wrapS: TextureWrap = TextureWrap.REPEAT,
        wrapT: TextureWrap = TextureWrap.REPEAT
    ): Texture = when (image.format) {
        RGBA -> image.cast(RGBA)!!.let {
            createTexture(
                it.width, it.height, it.data.asBytesRO(), mipmaps,
                minFilter, magFilter, wrapS, wrapT
            )
        }
    }

    fun createTexture(
        width: Int,
        height: Int,
        buffer: BytesRO,
        mipmaps: Int = 0,
        minFilter: TextureFilter = TextureFilter.NEAREST,
        magFilter: TextureFilter = TextureFilter.NEAREST,
        wrapS: TextureWrap = TextureWrap.REPEAT,
        wrapT: TextureWrap = TextureWrap.REPEAT
    ): Texture

    fun createFramebuffer(
        width: Int,
        height: Int,
        colorAttachments: Int,
        depth: Boolean,
        hdr: Boolean,
        alpha: Boolean,
        minFilter: TextureFilter = TextureFilter.NEAREST,
        magFilter: TextureFilter = minFilter
    ): Framebuffer

    fun createModelFast(
        attributes: List<ModelAttribute>,
        length: Int,
        renderType: RenderType
    ): Model

    fun createModelStatic(
        attributes: List<ModelAttribute>,
        length: Int,
        index: IntArray,
        renderType: RenderType
    ): Model = createModelStatic(
        attributes, length, index, index.size, renderType
    )

    fun createModelStatic(
        attributes: List<ModelAttribute>,
        length: Int,
        index: IntArray,
        indexLength: Int,
        renderType: RenderType
    ): Model

    fun createModelHybrid(
        attributes: List<ModelAttribute>,
        length: Int,
        attributesStream: List<ModelAttribute>,
        lengthStream: Int,
        renderType: RenderType
    ): ModelHybrid

    fun createShader(
        shader: CompiledShader,
        properties: Map<String, Expression> = emptyMap()
    ): Shader
}


fun GraphicsSystem.loadShader(
    asset: String,
    properties: Map<String, Expression> = emptyMap()
): Deferred<Shader> =
    loadShader(engine.files[asset], properties)

fun GraphicsSystem.loadShader(
    asset: ReadSource,
    properties: Map<String, Expression> = emptyMap()
): Deferred<Shader> =
    loadShader({
        asset.readAsync { readBinary(it) }.toCompiledShader()
                ?: throw IOException("Failed to deserialize shader")
    }, properties)

fun GraphicsSystem.loadShader(
    shader: suspend () -> CompiledShader,
    properties: Map<String, Expression> = emptyMap()
): Deferred<Shader> =
    engine.resources.load { createShader(shader(), properties) }

fun GraphicsSystem.loadShader(
    shader: CompiledShader,
    properties: Map<String, Expression> = emptyMap()
): Deferred<Shader> =
    engine.resources.load { createShader(shader, properties) }
