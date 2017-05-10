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

import org.tobi29.scapes.engine.GameState
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues
import org.tobi29.scapes.engine.resource.Resource
import org.tobi29.scapes.engine.utils.ConcurrentHashMap
import org.tobi29.scapes.engine.utils.ConcurrentLinkedQueue
import org.tobi29.scapes.engine.utils.computeAbsent
import org.tobi29.scapes.engine.utils.graphics.Image
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.profiler.profilerSection
import org.tobi29.scapes.engine.utils.shader.CompiledShader
import org.tobi29.scapes.engine.utils.shader.ShaderCompiler

class GraphicsSystem(private val gos: GraphicsObjectSupplier) : GraphicsObjectSupplier by gos {
    private val fpsDebug: GuiWidgetDebugValues.Element
    private val widthDebug: GuiWidgetDebugValues.Element
    private val heightDebug: GuiWidgetDebugValues.Element
    private val textureDebug: GuiWidgetDebugValues.Element
    private val vaoDebug: GuiWidgetDebugValues.Element
    private val fboDebug: GuiWidgetDebugValues.Element
    private val shaderDebug: GuiWidgetDebugValues.Element
    private val empty: Texture
    private val shaderCache = ConcurrentHashMap<String, Resource<CompiledShader>>()
    private val queue = ConcurrentLinkedQueue<(GL) -> Unit>()
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
            if (lastContentWidth != contentWidth ||
                    lastContentHeight != contentHeight) {
                lastContentWidth = contentWidth
                lastContentHeight = contentHeight
                fboSizeDirty = true
                widthDebug.setValue(contentWidth)
                heightDebug.setValue(contentHeight)
                profilerSection("Reshape") {
                    gl.reshape(contentWidth, contentHeight)
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

    fun compileShader(source: String): Resource<CompiledShader> {
        return shaderCache.computeAbsent(source) {
            engine.resources.load {
                profilerSection("Shader parse") {
                    ShaderCompiler.compile(source)
                }
            }
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
