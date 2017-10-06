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

import org.tobi29.scapes.engine.Container
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.utils.io.ByteViewRO
import org.tobi29.scapes.engine.utils.graphics.Image
import org.tobi29.scapes.engine.utils.shader.CompiledShader
import org.tobi29.scapes.engine.utils.shader.Expression
import org.tobi29.scapes.engine.utils.io.view

interface GraphicsObjectSupplier {
    val engine: ScapesEngine get() = container.engine
    val container: Container
    val vaoTracker: GraphicsObjectTracker<Model>
    val textureTracker: GraphicsObjectTracker<Texture>
    val fboTracker: GraphicsObjectTracker<Framebuffer>
    val shaderTracker: GraphicsObjectTracker<Shader>

    fun createTexture(width: Int,
                      height: Int): Texture {
        return createTexture(width, height, ByteArray(width * height * 4).view,
                0)
    }

    fun createTexture(image: Image,
                      mipmaps: Int): Texture {
        return createTexture(image.width, image.height, image.view, mipmaps,
                TextureFilter.NEAREST, TextureFilter.NEAREST,
                TextureWrap.REPEAT, TextureWrap.REPEAT)
    }

    fun createTexture(width: Int,
                      height: Int,
                      mipmaps: Int): Texture {
        return createTexture(width, height, ByteArray(width * height * 4).view,
                mipmaps)
    }

    fun createTexture(width: Int,
                      height: Int,
                      mipmaps: Int,
                      minFilter: TextureFilter,
                      magFilter: TextureFilter,
                      wrapS: TextureWrap,
                      wrapT: TextureWrap): Texture {
        return createTexture(width, height, ByteArray(width * height * 4).view,
                mipmaps, minFilter, magFilter, wrapS, wrapT)
    }

    fun createTexture(image: Image,
                      mipmaps: Int = 0,
                      minFilter: TextureFilter = TextureFilter.NEAREST,
                      magFilter: TextureFilter = TextureFilter.NEAREST,
                      wrapS: TextureWrap = TextureWrap.REPEAT,
                      wrapT: TextureWrap = TextureWrap.REPEAT): Texture {
        return createTexture(image.width, image.height, image.view, mipmaps,
                minFilter, magFilter, wrapS, wrapT)
    }

    fun createTexture(width: Int,
                      height: Int,
                      buffer: ByteViewRO,
                      mipmaps: Int = 0,
                      minFilter: TextureFilter = TextureFilter.NEAREST,
                      magFilter: TextureFilter = TextureFilter.NEAREST,
                      wrapS: TextureWrap = TextureWrap.REPEAT,
                      wrapT: TextureWrap = TextureWrap.REPEAT): Texture

    fun createFramebuffer(width: Int,
                          height: Int,
                          colorAttachments: Int,
                          depth: Boolean,
                          hdr: Boolean,
                          alpha: Boolean,
                          minFilter: TextureFilter = TextureFilter.NEAREST,
                          magFilter: TextureFilter = minFilter): Framebuffer

    fun createModelFast(attributes: List<ModelAttribute>,
                        length: Int,
                        renderType: RenderType): Model

    fun createModelStatic(attributes: List<ModelAttribute>,
                          length: Int,
                          index: IntArray,
                          renderType: RenderType): Model {
        return createModelStatic(attributes, length, index, index.size,
                renderType)
    }

    fun createModelStatic(attributes: List<ModelAttribute>,
                          length: Int,
                          index: IntArray,
                          indexLength: Int,
                          renderType: RenderType): Model

    fun createModelHybrid(
            attributes: List<ModelAttribute>,
            length: Int,
            attributesStream: List<ModelAttribute>,
            lengthStream: Int,
            renderType: RenderType): ModelHybrid

    fun loadShader(shader: () -> CompiledShader,
                   properties: Map<String, Expression> = emptyMap()) =
            engine.resources.load { createShader(shader(), properties) }

    fun loadShader(shader: CompiledShader,
                   properties: Map<String, Expression> = emptyMap()) =
            engine.resources.load { createShader(shader, properties) }

    fun createShader(shader: CompiledShader,
                     properties: Map<String, Expression> = emptyMap()): Shader
}