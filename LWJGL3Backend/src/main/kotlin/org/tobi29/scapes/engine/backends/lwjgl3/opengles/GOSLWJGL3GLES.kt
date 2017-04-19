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

package org.tobi29.scapes.engine.backends.lwjgl3.opengles

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.*
import org.tobi29.scapes.engine.utils.io.ByteBuffer
import org.tobi29.scapes.engine.utils.shader.CompiledShader

class GOSLWJGL3GLES(override val engine: ScapesEngine) : GraphicsObjectSupplier {
    override val textures = TextureManager(engine)
    override val vaoTracker = GraphicsObjectTracker<Model>()
    override val textureTracker = GraphicsObjectTracker<Texture>()
    override val fboTracker = GraphicsObjectTracker<Framebuffer>()
    override val shaderTracker = GraphicsObjectTracker<Shader>()

    override fun createTexture(width: Int,
                               height: Int,
                               buffer: ByteBuffer,
                               mipmaps: Int,
                               minFilter: TextureFilter,
                               magFilter: TextureFilter,
                               wrapS: TextureWrap,
                               wrapT: TextureWrap): Texture {
        return TextureGL(engine, width, height, buffer, mipmaps, minFilter,
                magFilter, wrapS, wrapT)
    }

    override fun createFramebuffer(width: Int,
                                   height: Int,
                                   colorAttachments: Int,
                                   depth: Boolean,
                                   hdr: Boolean,
                                   alpha: Boolean,
                                   minFilter: TextureFilter,
                                   magFilter: TextureFilter): Framebuffer {
        return FBO(engine, width, height, colorAttachments, depth, hdr,
                alpha, minFilter, magFilter)
    }

    override fun createModelFast(attributes: List<ModelAttribute>,
                                 length: Int,
                                 renderType: RenderType): Model {
        val vbo = VBO(engine, attributes, length)
        return VAOFast(vbo, length, renderType)
    }

    override fun createModelStatic(attributes: List<ModelAttribute>,
                                   length: Int,
                                   index: IntArray,
                                   indexLength: Int,
                                   renderType: RenderType): Model {
        val vbo = VBO(engine, attributes, length)
        return VAOStatic(vbo, index, indexLength, renderType)
    }

    override fun createModelHybrid(attributes: List<ModelAttribute>,
                                   length: Int,
                                   attributesStream: List<ModelAttribute>,
                                   lengthStream: Int,
                                   renderType: RenderType): ModelHybrid {
        val vbo = VBO(engine, attributes, length)
        val vboStream = VBO(engine, attributesStream, lengthStream)
        return VAOHybrid(vbo, vboStream, renderType)
    }

    override fun createShader(shader: CompiledShader,
                              information: ShaderCompileInformation): Shader {
        return ShaderGL(shader, information)
    }
}
