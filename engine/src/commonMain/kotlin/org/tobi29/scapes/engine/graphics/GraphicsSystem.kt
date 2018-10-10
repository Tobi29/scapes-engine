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

import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Runnable
import org.tobi29.coroutines.TaskChannel
import org.tobi29.coroutines.offer
import org.tobi29.coroutines.processCurrent
import org.tobi29.io.view
import org.tobi29.logging.KLogger
import org.tobi29.profiler.profilerSection
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues
import org.tobi29.stdex.concurrent.ReentrantLock
import org.tobi29.stdex.concurrent.withLock
import kotlin.coroutines.experimental.CoroutineContext

class GraphicsSystem(
    val engine: ScapesEngine,
    private val gos: GraphicsObjectSupplier
) : CoroutineDispatcher(), GraphicsObjectSupplier by gos {
    internal val lock = ReentrantLock()
    private lateinit var fpsDebug: GuiWidgetDebugValues.Element
    private lateinit var widthDebug: GuiWidgetDebugValues.Element
    private lateinit var heightDebug: GuiWidgetDebugValues.Element
    private lateinit var textureDebug: GuiWidgetDebugValues.Element
    private lateinit var vaoDebug: GuiWidgetDebugValues.Element
    private lateinit var fboDebug: GuiWidgetDebugValues.Element
    private lateinit var shaderDebug: GuiWidgetDebugValues.Element
    private val empty: Texture = createTexture(
        1, 1,
        byteArrayOf(-1, -1, -1, -1).view, 0
    )
    private val queue = TaskChannel<(GL) -> Unit>()
    private var lastContentWidth = 0
    private var lastContentHeight = 0
    private var lastContainerWidth = 0
    private var lastContainerHeight = 0

    val textures = TextureManager(engine)

    internal fun initDebug(debugValues: GuiWidgetDebugValues) {
        fpsDebug = debugValues["Graphics-Fps"]
        widthDebug = debugValues["Graphics-Width"]
        heightDebug = debugValues["Graphics-Height"]
        textureDebug = debugValues["Graphics-Textures"]
        vaoDebug = debugValues["Graphics-VAOs"]
        fboDebug = debugValues["Graphics-FBOs"]
        shaderDebug = debugValues["Graphics-Shaders"]
    }

    fun dispose(gl: GL) {
        lock.withLock {
            gos.vaoTracker.disposeAll(gl)
            gos.textureTracker.disposeAll(gl)
            gos.fboTracker.disposeAll(gl)
            gos.shaderTracker.disposeAll(gl)
        }
    }

    fun textureEmpty(): Texture {
        return empty
    }

    fun render(
        gl: GL,
        delta: Double,
        contentWidth: Int,
        contentHeight: Int,
        containerWidth: Int,
        containerHeight: Int
    ): Boolean {
        return lock.withLock {
            try {
                gl.checkError("Pre-Render")
                gl.step(delta)
                val fboSizeDirty: Boolean
                if (lastContentWidth != contentWidth
                    || lastContentHeight != contentHeight
                    || lastContainerWidth != containerWidth
                    || lastContainerHeight != containerHeight) {
                    lastContentWidth = contentWidth
                    lastContentHeight = contentHeight
                    lastContainerWidth = containerWidth
                    lastContainerHeight = containerHeight
                    fboSizeDirty = true
                    widthDebug.setValue(contentWidth)
                    heightDebug.setValue(contentHeight)
                    profilerSection("Reshape") {
                        gl.reshape(
                            contentWidth, contentHeight,
                            containerWidth, containerHeight
                        )
                    }
                } else {
                    fboSizeDirty = false
                }
                val state = engine.state
                executeDispatched(gl)
                gl.setViewport(0, 0, gl.contentWidth, gl.contentHeight)
                if (profilerSection("State") {
                        state?.renderState(gl, delta, fboSizeDirty)
                    } != true) return false
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

    fun reset() {
        gos.vaoTracker.resetAll()
        gos.textureTracker.resetAll()
        gos.fboTracker.resetAll()
        gos.shaderTracker.resetAll()
    }

    fun executeDispatched(gl: GL) {
        queue.processCurrent { it(gl) }
    }

    fun dispatch(block: (GL) -> Unit) {
        queue.offer(block)
    }

    override fun dispatch(
        context: CoroutineContext,
        block: Runnable
    ) {
        dispatch {
            try {
                block.run()
            } catch (e: CancellationException) {
                logger.warn { "Job cancelled: ${e.message}" }
            }
        }
    }

    companion object {
        private val logger = KLogger<GraphicsSystem>()
    }
}
