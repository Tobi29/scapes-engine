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

import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Runnable
import org.tobi29.scapes.engine.GameState
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues
import org.tobi29.scapes.engine.utils.graphics.Image
import org.tobi29.scapes.engine.utils.io.view
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.profiler.profilerSection
import org.tobi29.scapes.engine.utils.task.TaskChannel
import org.tobi29.scapes.engine.utils.task.offer
import org.tobi29.scapes.engine.utils.task.processCurrent
import kotlin.coroutines.experimental.CoroutineContext

class GraphicsSystem(
        val engine: ScapesEngine,
        private val gos: GraphicsObjectSupplier
) : CoroutineDispatcher(), GraphicsObjectSupplier by gos {
    private val fpsDebug: GuiWidgetDebugValues.Element
    private val widthDebug: GuiWidgetDebugValues.Element
    private val heightDebug: GuiWidgetDebugValues.Element
    private val textureDebug: GuiWidgetDebugValues.Element
    private val vaoDebug: GuiWidgetDebugValues.Element
    private val fboDebug: GuiWidgetDebugValues.Element
    private val shaderDebug: GuiWidgetDebugValues.Element
    private val empty: Texture
    private val queue = TaskChannel<(GL) -> Unit>()
    private var renderState: GameState? = null
    private var lastContentWidth = 0
    private var lastContentHeight = 0

    val textures = TextureManager(engine)

    init {
        empty = createTexture(1, 1, byteArrayOf(-1, -1, -1, -1).view, 0)
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
        synchronized(this) {
            gos.vaoTracker.disposeAll(gl)
            gos.textureTracker.disposeAll(gl)
            gos.fboTracker.disposeAll(gl)
            gos.shaderTracker.disposeAll(gl)
        }
    }

    fun textureEmpty(): Texture {
        return empty
    }

    fun render(gl: GL,
               delta: Double,
               contentWidth: Int,
               contentHeight: Int): Boolean {
        return synchronized(this) {
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
                val state = engine.state
                val renderState = renderState
                if (renderState !== state) {
                    profilerSection("SwitchState") {
                        this.renderState = state
                    }
                }
                executeDispatched(gl)
                gl.setViewport(0, 0, gl.contentWidth, gl.contentHeight)
                profilerSection("State") {
                    if (!state.renderState(gl, delta,
                            fboSizeDirty)) return@synchronized false
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
            } catch (e: RenderCancelException) {
            } catch (e: GraphicsException) {
                logger.warn { "Graphics error during rendering: $e" }
            }
            true
        }
    }

    fun requestScreenshot(block: (Image) -> Unit) {
        queue.offer { gl ->
            block(gl.screenShot(0, 0, gl.contentWidth, gl.contentHeight))
        }
    }

    fun reset() {
        gos.vaoTracker.resetAll()
        gos.textureTracker.resetAll()
        gos.fboTracker.resetAll()
        gos.shaderTracker.resetAll()
    }

    fun executeDispatched(gl: GL) {
        queue.processCurrent { it(gl) }
    }

    override fun dispatch(context: CoroutineContext,
                          block: Runnable) {
        queue.offer {
            try {
                block.run()
            } catch (e: CancellationException) {
                logger.warn { "Job cancelled: ${e.message}" }
            }
        }
    }

    companion object : KLogging()
}
