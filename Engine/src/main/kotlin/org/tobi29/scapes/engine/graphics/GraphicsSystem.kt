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
import org.tobi29.scapes.engine.utils.io.asString
import org.tobi29.scapes.engine.utils.io.process
import org.tobi29.scapes.engine.utils.profiler.profilerSection
import org.tobi29.scapes.engine.utils.shader.CompiledShader
import org.tobi29.scapes.engine.utils.shader.ShaderCompiler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class GraphicsSystem(private val gos: GraphicsObjectSupplier) : GraphicsObjectSupplier by gos {
    private val fpsDebug: GuiWidgetDebugValues.Element
    private val widthDebug: GuiWidgetDebugValues.Element
    private val heightDebug: GuiWidgetDebugValues.Element
    private val textureDebug: GuiWidgetDebugValues.Element
    private val vaoDebug: GuiWidgetDebugValues.Element
    private val fboDebug: GuiWidgetDebugValues.Element
    private val shaderDebug: GuiWidgetDebugValues.Element
    private val empty: Texture
    private val shaderCache = ConcurrentHashMap<String, CompiledShader>()
    private val shaderFallback = createShader("Engine:shader/Textured")
    private val queue = ConcurrentLinkedQueue<(GL) -> Unit>()
    private var resolutionMultiplier = 1.0
    private var renderState: GameState? = null
    private var lastContentWidth = 0
    private var lastContentHeight = 0

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

    fun dispose(gl: GL) {
        engine.halt()
        synchronized(this) {
            val state = engine.getState()
            state.disposeState(gl)
            gos.vaoTracker.disposeAll(gl)
            gos.textureTracker.disposeAll(gl)
            gos.fboTracker.disposeAll(gl)
            gos.shaderTracker.disposeAll(gl)
        }
    }

    fun engine(): ScapesEngine {
        return engine
    }

    fun textures(): TextureManager {
        return textures
    }

    fun textureEmpty(): Texture {
        return empty
    }

    @Synchronized fun render(gl: GL,
                             delta: Double,
                             contentWidth: Int = 0,
                             contentHeight: Int = 0) {
        try {
            gl.checkError("Pre-Render")
            gl.step(delta)
            val fboSizeDirty: Boolean
            val resolutionMultiplier = engine.config.resolutionMultiplier
            if (lastContentWidth != contentWidth ||
                    lastContentHeight != contentHeight ||
                    this.resolutionMultiplier != resolutionMultiplier) {
                lastContentWidth = contentWidth
                lastContentHeight = contentHeight
                this.resolutionMultiplier = resolutionMultiplier
                fboSizeDirty = true
                widthDebug.setValue(contentWidth)
                heightDebug.setValue(contentHeight)
                profilerSection("Reshape") {
                    gl.reshape(contentWidth, contentHeight,
                            resolutionMultiplier)
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
            while (queue.isNotEmpty()) {
                queue.poll()(gl)
            }
            fpsDebug.setValue(1.0 / delta)
            textureDebug.setValue(gos.textureTracker.count())
            vaoDebug.setValue(gos.vaoTracker.count())
            fboDebug.setValue(gos.fboTracker.count())
            shaderDebug.setValue(gos.shaderTracker.count())
            engine.performance.renderTimestamp(delta)
            profilerSection("Cleanup") {
                gos.vaoTracker.disposeUnused(gl)
                gos.textureTracker.disposeUnused(gl)
                gos.fboTracker.disposeUnused(gl)
                gos.shaderTracker.disposeUnused(gl)
            }
        } catch (e: GraphicsException) {
            logger.warn { "Graphics error during rendering: $e" }
        }
    }

    fun requestScreenshot(block: (Image) -> Unit) {
        queue.add { gl ->
            block(gl.screenShot(0, 0, gl.contentWidth(),
                    gl.contentHeight()))
        }
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
        val program = engine.files[asset + ".program"].get()
        val source = program.read({ stream -> process(stream, asString()) })
        val shader = compiled(source)
        return createShader(shader, information)
    }

    private fun compiled(source: String): CompiledShader {
        return shaderCache[source] ?: run {
            val shader = ShaderCompiler.compile(source)
            shaderCache.put(source, shader)
            shader
        }
    }

    fun reset() {
        gos.vaoTracker.resetAll()
        gos.textureTracker.resetAll()
        gos.fboTracker.resetAll()
        gos.shaderTracker.resetAll()
    }

    companion object : KLogging()
}
