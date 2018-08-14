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

package org.tobi29.scapes.engine.backends.opengles

import org.tobi29.arrays.BytesRO
import org.tobi29.scapes.engine.Container
import org.tobi29.scapes.engine.graphics.*
import org.tobi29.scapes.engine.shader.CompiledShader
import org.tobi29.scapes.engine.shader.Expression

abstract class GOSGLES(override val container: Container) :
    GraphicsObjectSupplier {
    protected abstract val glh: GLESHandle
    override val vaoTracker = GraphicsObjectTracker<Model>()
    override val textureTracker = GraphicsObjectTracker<Texture>()
    override val fboTracker = GraphicsObjectTracker<Framebuffer>()
    override val shaderTracker = GraphicsObjectTracker<Shader>()
    private val currentFBO = CurrentFBO()

    override fun createTexture(
        width: Int,
        height: Int,
        buffer: BytesRO,
        mipmaps: Int,
        minFilter: TextureFilter,
        magFilter: TextureFilter,
        wrapS: TextureWrap,
        wrapT: TextureWrap
    ): Texture =
        TextureGL(
            glh, width, height, buffer, mipmaps, minFilter,
            magFilter, wrapS, wrapT
        )

    override fun createFramebuffer(
        width: Int,
        height: Int,
        colorAttachments: Int,
        depth: Boolean,
        hdr: Boolean,
        alpha: Boolean,
        minFilter: TextureFilter,
        magFilter: TextureFilter
    ): Framebuffer {
        return FBO(
            glh, currentFBO, width, height, colorAttachments, depth,
            hdr, alpha, minFilter, magFilter
        )
    }

    override fun createModelFast(
        attributes: List<ModelAttribute>,
        length: Int,
        renderType: RenderType
    ): Model {
        val vbo = VBO(
            glh,
            attributes,
            length
        )
        return VAOFast(
            vbo,
            length,
            renderType
        )
    }

    override fun createModelStatic(
        attributes: List<ModelAttribute>,
        length: Int,
        index: IntArray,
        indexLength: Int,
        renderType: RenderType
    ): Model {
        val vbo = VBO(
            glh,
            attributes,
            length
        )
        return VAOStatic(
            vbo,
            index,
            indexLength,
            renderType
        )
    }

    override fun createModelHybrid(
        attributes: List<ModelAttribute>,
        length: Int,
        attributesStream: List<ModelAttribute>,
        lengthStream: Int,
        renderType: RenderType
    ): ModelHybrid {
        val vbo = VBO(
            glh,
            attributes,
            length
        )
        val vboStream = VBO(
            glh,
            attributesStream,
            lengthStream
        )
        return VAOHybrid(
            vbo,
            vboStream,
            renderType
        )
    }

    override fun createShader(
        shader: CompiledShader,
        properties: Map<String, Expression>
    ): Shader {
        return ShaderGL(
            glh,
            shader,
            properties
        )
    }
}
