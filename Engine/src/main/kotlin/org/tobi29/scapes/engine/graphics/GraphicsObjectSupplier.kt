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
import org.tobi29.scapes.engine.resource.loadString
import org.tobi29.scapes.engine.utils.graphics.Image
import org.tobi29.scapes.engine.utils.io.ByteBuffer
import org.tobi29.scapes.engine.utils.shader.CompiledShader

interface GraphicsObjectSupplier {
    val engine: ScapesEngine

    val vaoTracker: GraphicsObjectTracker<Model>
    val textureTracker: GraphicsObjectTracker<Texture>
    val fboTracker: GraphicsObjectTracker<Framebuffer>
    val shaderTracker: GraphicsObjectTracker<Shader>

    val textures: TextureManager

    fun createTexture(width: Int,
                      height: Int): Texture {
        return createTexture(width, height, engine.allocate(width * height * 4),
                0)
    }

    fun createTexture(image: Image,
                      mipmaps: Int): Texture {
        return createTexture(image.width, image.height, image.buffer,
                mipmaps, TextureFilter.NEAREST, TextureFilter.NEAREST,
                TextureWrap.REPEAT, TextureWrap.REPEAT)
    }

    fun createTexture(width: Int,
                      height: Int,
                      mipmaps: Int): Texture {
        return createTexture(width, height, engine.allocate(width * height * 4),
                mipmaps)
    }

    fun createTexture(width: Int,
                      height: Int,
                      mipmaps: Int,
                      minFilter: TextureFilter,
                      magFilter: TextureFilter,
                      wrapS: TextureWrap,
                      wrapT: TextureWrap): Texture {
        return createTexture(width, height, engine.allocate(width * height * 4),
                mipmaps, minFilter, magFilter, wrapS, wrapT)
    }

    fun createTexture(image: Image): Texture {
        return createTexture(image.width, image.height, image.buffer, 4)
    }

    fun createTexture(image: Image,
                      mipmaps: Int,
                      minFilter: TextureFilter,
                      magFilter: TextureFilter,
                      wrapS: TextureWrap,
                      wrapT: TextureWrap): Texture {
        return createTexture(image.width, image.height, image.buffer,
                mipmaps, minFilter, magFilter, wrapS, wrapT)
    }

    fun createTexture(width: Int,
                      height: Int,
                      buffer: ByteBuffer,
                      mipmaps: Int = 4,
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

    fun loadShader(asset: String,
                   consumer: ShaderCompileInformation.() -> Unit): Resource<Shader> {
        return loadShader(engine.resources.loadString(
                engine.files["$asset.program"].get()), consumer)
    }

    fun loadShader(asset: String,
                   information: ShaderCompileInformation = ShaderCompileInformation()): Resource<Shader> {
        return loadShader(engine.resources.loadString(
                engine.files["$asset.program"].get()), information)
    }

    fun loadShader(source: Resource<String>,
                   consumer: ShaderCompileInformation.() -> Unit): Resource<Shader> {
        return engine.resources.load {
            createShader(source.getAsync(), consumer)
        }
    }

    fun loadShader(source: Resource<String>,
                   information: ShaderCompileInformation = ShaderCompileInformation()): Resource<Shader> {
        return engine.resources.load {
            createShader(source.getAsync(), information)
        }
    }

    fun createShader(source: String,
                     consumer: ShaderCompileInformation.() -> Unit): Shader {
        val information = ShaderCompileInformation()
        consumer(information)
        return createShader(source, information)
    }

    fun createShader(source: String,
                     information: ShaderCompileInformation = ShaderCompileInformation()): Shader {
        val shader = engine.graphics.compileShader(source)
        return createShader(shader, information)
    }

    fun createShader(shader: CompiledShader,
                     information: ShaderCompileInformation): Shader
}