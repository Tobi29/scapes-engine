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

import kotlinx.coroutines.experimental.channels.LinkedListChannel
import kotlinx.coroutines.experimental.launch
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.Pipeline
import org.tobi29.scapes.engine.graphics.SHADER_TEXTURED
import org.tobi29.scapes.engine.utils.AtomicBoolean

abstract class GameState(val engine: ScapesEngine) {
    open val tps = 60.0
    private val newPipeline = LinkedListChannel<Pair<Boolean, (GL) -> suspend () -> (Double) -> Unit>>()
    private var newPipelineLoaded: (() -> (() -> Unit)?)? = null
    private val dirtyPipeline = AtomicBoolean(false)
    private var pipeline: Pipeline? = null

    open fun dispose() {}

    abstract fun init()

    abstract val isMouseGrabbed: Boolean

    abstract fun step(delta: Double)

    fun renderState(gl: GL,
                    delta: Double,
                    updateSize: Boolean): Boolean {
        while (!newPipeline.isEmpty) {
            newPipeline.poll()?.let { (_, newPipeline) ->
                finishPipeline(gl, Pipeline(gl, newPipeline))
            }
        }
        updateLoadedPipeline()
        var pipeline = pipeline ?: return false
        if (dirtyPipeline.getAndSet(false) || updateSize) {
            pipeline = pipeline.rebuild(gl)
            launch(engine.graphics) {
                pipeline.finish()
            }
            this.pipeline = pipeline
        }
        renderStep(delta)
        return pipeline.render(delta)
    }

    private fun finishPipeline(gl: GL,
                               pipeline: Pipeline) {
        var loaded: (() -> Unit)? = null
        newPipelineLoaded = { loaded }
        launch(engine.graphics) {
            pipeline.finish()
            loaded = { this@GameState.pipeline = pipeline }
        }
        updateLoadedPipeline()
    }

    private fun updateLoadedPipeline() {
        newPipelineLoaded?.invoke()?.let { newPipeline ->
            newPipelineLoaded = null
            newPipeline()
        }
    }

    open fun renderStep(delta: Double) {
    }

    fun dirtyPipeline() {
        dirtyPipeline.set(true)
    }

    fun switchPipeline(block: (GL) -> suspend () -> (Double) -> Unit) =
            switchPipeline(true, block)

    fun switchPipelineWhenLoaded(block: (GL) -> suspend () -> (Double) -> Unit) =
            switchPipeline(false, block)

    fun switchPipeline(sync: Boolean,
                       block: (GL) -> suspend () -> (Double) -> Unit) {
        switchPipelineBare(sync) { gl ->
            val blockFinish = block(gl)
            val guiFinish = renderGui(gl)
            ;{
            val guiRender = guiFinish()
            val blockRender = blockFinish()
            ;{ delta ->
            blockRender(delta)
            guiRender(delta)
        }
        }
        }
    }

    fun switchPipelineBare(sync: Boolean,
                           block: (GL) -> suspend () -> (Double) -> Unit) {
        newPipeline.offer(Pair(sync, block))
    }

    private fun renderGui(gl: GL): suspend () -> (Double) -> Unit {
        val shader = engine.graphics.loadShader(SHADER_TEXTURED)
        return render@ {
            val s = shader.getAsync()
            ;{ delta ->
            gl.clearDepth()
            engine.guiStack.render(gl, s, delta)
            gl.checkError("Gui-Rendering")
        }
        }
    }
}
