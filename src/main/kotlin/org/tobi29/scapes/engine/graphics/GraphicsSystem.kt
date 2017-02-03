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

import mu.KLogging
import org.tobi29.scapes.engine.GameState
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues
import org.tobi29.scapes.engine.resource.Resource
import org.tobi29.scapes.engine.utils.graphics.Image
import org.tobi29.scapes.engine.utils.graphics.encodePNG
import org.tobi29.scapes.engine.utils.io.asString
import org.tobi29.scapes.engine.utils.io.filesystem.write
import org.tobi29.scapes.engine.utils.io.process
import org.tobi29.scapes.engine.utils.profiler.profilerSection
import org.tobi29.scapes.engine.utils.shader.CompiledShader
import org.tobi29.scapes.engine.utils.shader.ShaderCompileException
import org.tobi29.scapes.engine.utils.shader.ShaderCompiler
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

class GraphicsSystem(val engine: ScapesEngine,
                     private val gl: GL) {
    val textures: TextureManager
        get() = gl.textures
    private val fpsDebug: GuiWidgetDebugValues.Element
    private val widthDebug: GuiWidgetDebugValues.Element
    private val heightDebug: GuiWidgetDebugValues.Element
    private val textureDebug: GuiWidgetDebugValues.Element
    private val vaoDebug: GuiWidgetDebugValues.Element
    private val fboDebug: GuiWidgetDebugValues.Element
    private val shaderDebug: GuiWidgetDebugValues.Element
    private val empty: Texture
    private val shaderCompiler = ShaderCompiler()
    private val shaderCache = ConcurrentHashMap<String, CompiledShader>()
    private val shaderFallback = createShader("Engine:shader/Textured")
    private var triggerScreenshot = false
    private var resolutionMultiplier = 1.0
    private var renderState: GameState? = null

    init {
        val buffer = engine.allocate(4)
        buffer.put((-1).toByte())
        buffer.put((-1).toByte())
        buffer.put((-1).toByte())
        buffer.put((-1).toByte())
        buffer.rewind()
        empty = createTexture(1, 1, buffer)
        resolutionMultiplier = engine.config.resolutionMultiplier
        val debugValues = engine.debugValues
        fpsDebug = debugValues["Graphics-Fps"]
        widthDebug = debugValues["Graphics-Width"]
        heightDebug = debugValues["Graphics-Height"]
        textureDebug = debugValues["Graphics-Textures"]
        vaoDebug = debugValues["Graphics-VAOs"]
        fboDebug = debugValues["Graphics-FBOs"]
        shaderDebug = debugValues["Graphics-Shaders"]
    }

    fun dispose() {
        engine.halt()
        synchronized(this) {
            val state = engine.getState()
            state.disposeState(gl)
            gl.clear()
        }
    }

    fun engine(): ScapesEngine {
        return engine
    }

    fun textures(): TextureManager {
        return gl.textures()
    }

    fun textureEmpty(): Texture {
        return empty
    }

    @Synchronized fun render(delta: Double) {
        try {
            gl.checkError("Pre-Render")
            gl.step(delta)
            val container = engine.container
            val containerWidth = container.containerWidth()
            val containerHeight = container.containerHeight()
            val fboSizeDirty: Boolean
            val resolutionMultiplier = engine.config.resolutionMultiplier
            if (container.contentResized() || this.resolutionMultiplier != resolutionMultiplier) {
                this.resolutionMultiplier = resolutionMultiplier
                val contentWidth = container.contentWidth()
                val contentHeight = container.contentHeight()
                fboSizeDirty = true
                widthDebug.setValue(contentWidth)
                heightDebug.setValue(contentHeight)
                profilerSection("Reshape") {
                    gl.reshape(contentWidth, contentHeight, containerWidth,
                            containerHeight, resolutionMultiplier)
                }
            } else {
                fboSizeDirty = false
            }
            val state = engine.getState()
            val renderState = renderState
            if (renderState !== state) {
                profilerSection("SwitchState") {
                    renderState?.disposeState(gl)
                    this.renderState = state
                }
            }
            gl.setViewport(0, 0, gl.contentWidth(), gl.contentHeight())
            profilerSection("State") {
                state.renderState(gl, delta, fboSizeDirty)
            }
            fpsDebug.setValue(1.0 / delta)
            textureDebug.setValue(gl.textureTracker().count())
            vaoDebug.setValue(gl.vaoTracker().count())
            fboDebug.setValue(gl.fboTracker().count())
            shaderDebug.setValue(gl.shaderTracker().count())
            engine.performance.renderTimestamp(delta)
            if (triggerScreenshot) {
                profilerSection("Screenshot") {
                    triggerScreenshot = false
                    val width = gl.contentWidth()
                    val height = gl.contentHeight()
                    val image = gl.screenShot(0, 0, width, height)
                    val path = engine.home.resolve(
                            "screenshots/" + System.currentTimeMillis() +
                                    ".png")
                    engine.taskExecutor.runTask({
                        try {
                            write(path) { encodePNG(image, it, 9, false) }
                        } catch (e: IOException) {
                            logger.error { "Error saving screenshot: $e" }
                        }
                    }, "Write-Screenshot")
                }
            }
            profilerSection("Cleanup") {
                gl.vaoTracker().disposeUnused(gl)
                gl.textureTracker().disposeUnused(gl)
                gl.fboTracker().disposeUnused(gl)
                gl.shaderTracker().disposeUnused(gl)
            }
        } catch (e: GraphicsException) {
            logger.warn { "Graphics error during rendering: $e" }
        }
    }

    fun triggerScreenshot() {
        triggerScreenshot = true
    }

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
                      wrapT: TextureWrap = TextureWrap.REPEAT): Texture {
        return gl.createTexture(width, height, buffer, mipmaps, minFilter,
                magFilter, wrapS, wrapT)
    }

    fun createFramebuffer(width: Int,
                          height: Int,
                          colorAttachments: Int,
                          depth: Boolean,
                          hdr: Boolean,
                          alpha: Boolean,
                          minFilter: TextureFilter = TextureFilter.NEAREST,
                          magFilter: TextureFilter = minFilter): Framebuffer {
        return gl.createFramebuffer(width, height, colorAttachments, depth, hdr,
                alpha, minFilter, magFilter)
    }

    fun createModelFast(attributes: List<ModelAttribute>,
                        length: Int,
                        renderType: RenderType): Model {
        return gl.createModelFast(attributes, length, renderType)
    }

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
                          renderType: RenderType): Model {
        return gl.createModelStatic(attributes, length, index, indexLength,
                renderType)
    }

    fun createModelHybrid(attributes: List<ModelAttribute>,
                          length: Int,
                          attributesStream: List<ModelAttribute>,
                          lengthStream: Int,
                          renderType: RenderType): ModelHybrid {
        return gl.createModelHybrid(attributes, length, attributesStream,
                lengthStream, renderType)
    }

    fun loadShader(asset: String,
                   consumer: ShaderCompileInformation.() -> Unit): Resource<Shader> {
        return engine.resources.load({ shaderFallback }) {
            createShader(asset, consumer)
        }
    }

    fun loadShader(asset: String,
                   information: ShaderCompileInformation = ShaderCompileInformation()): Resource<Shader> {
        return engine.resources.load({ shaderFallback }) {
            createShader(asset, information)
        }
    }

    fun createShader(asset: String,
                     consumer: ShaderCompileInformation.() -> Unit): Shader {
        val information = ShaderCompileInformation()
        consumer(information)
        return createShader(asset, information)
    }

    fun createShader(asset: String,
                     information: ShaderCompileInformation = ShaderCompileInformation()): Shader {
        try {
            val program = gl.engine.files[asset + ".program"].get()
            val source = program.read({ stream -> process(stream, asString()) })
            val shader = compiled(source)
            return createShader(shader, information)
        } catch (e: ShaderCompileException) {
            engine.crash(e)
            throw AssertionError()
        } catch (e: IOException) {
            engine.crash(e)
            throw AssertionError()
        }
    }

    fun createShader(shader: CompiledShader,
                     information: ShaderCompileInformation): Shader {
        return gl.createShader(shader, information)
    }

    private fun compiled(source: String): CompiledShader {
        return shaderCache[source] ?: run {
            val shader = synchronized(shaderCompiler) {
                shaderCompiler.compile(source)
            }
            shaderCache.put(source, shader)
            shader
        }
    }

    fun clear() {
        gl.clear()
    }

    fun reset() {
        gl.reset()
    }

    companion object : KLogging()
}
