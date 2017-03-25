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

package org.tobi29.scapes.engine

import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.Pipeline
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

abstract class GameState(val engine: ScapesEngine) {
    open val tps = 60.0
    private val newPipeline = AtomicReference<((GL) -> () -> Unit)?>()
    private val newPipelineLoaded = AtomicReference<((GL) -> () -> Unit)?>()
    private val dirtyPipeline = AtomicBoolean()
    private var pipeline: Pipeline? = null
    private var pipelineLoaded: Pipeline? = null
    private var guiRenderer: (Double) -> Unit = {}

    fun engine(): ScapesEngine {
        return engine
    }

    fun disposeState(gl: GL) {
    }

    fun disposeState() {
        dispose()
    }

    open fun dispose() {
    }

    abstract fun init()

    abstract val isMouseGrabbed: Boolean

    abstract fun step(delta: Double)

    fun renderState(gl: GL,
                    delta: Double,
                    updateSize: Boolean) {
        newPipelineLoaded.getAndSet(null)?.let { newPipeline ->
            pipelineLoaded = Pipeline(gl, newPipeline)
        }
        pipelineLoaded?.let { pipeline ->
            if (engine.resources.isDone()) {
                this.pipeline = pipeline
                guiRenderer = renderGui(gl)
                pipelineLoaded = null
            }
        }
        newPipeline.getAndSet(null)?.let { newPipeline ->
            pipeline = Pipeline(gl, newPipeline)
            guiRenderer = renderGui(gl)
        }
        val pipeline = pipeline
        if (pipeline == null) {
            gl.clear(1.0f, 0.0f, 0.0f, 1.0f)
        } else {
            if (dirtyPipeline.getAndSet(false) || updateSize) {
                this.pipeline = pipeline.rebuild(gl)
                guiRenderer = renderGui(gl)
            }
            renderStep(delta)
            pipeline.render()
            guiRenderer(delta)
        }
    }

    open fun renderStep(delta: Double) {
    }

    fun dirtyPipeline() {
        dirtyPipeline.set(true)
    }

    fun switchPipeline(block: (GL) -> () -> Unit) {
        newPipeline.set(block)
    }

    fun switchPipelineWhenLoaded(block: (GL) -> () -> Unit) {
        newPipelineLoaded.set(block)
    }

    private fun renderGui(gl: GL): (Double) -> Unit {
        val shader = engine.graphics.loadShader("Engine:shader/Gui")
        return { delta ->
            gl.clearDepth()
            engine.guiStack.render(gl, shader.get(), delta)
            gl.checkError("Gui-Rendering")
        }
    }
}
