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
package org.tobi29.scapes.engine.gui

import kotlinx.coroutines.experimental.yield
import org.tobi29.coroutines.JobHandle
import org.tobi29.coroutines.launchOrStop
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.Vector2i
import org.tobi29.math.vector.Vector3d
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.Matrix
import org.tobi29.scapes.engine.graphics.Shader
import org.tobi29.scapes.engine.input.pixelDeltaFor
import org.tobi29.stdex.math.ceilToInt
import org.tobi29.stdex.math.clamp
import org.tobi29.stdex.math.floorToInt
import org.tobi29.stdex.math.sqr
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class GuiComponentScrollPaneViewport(
    parent: GuiLayoutData,
    scrollStep: Int,
    private val autoHide: Boolean = false
) : GuiComponentPaneHeavy(parent) {
    internal var sliderX: GuiComponentSliderVert? = null
        set(value) {
            value?.let {
                if (autoHide) {
                    it.visible = false
                }
            }
            field = value
        }
    internal var sliderY: GuiComponentSliderVert? = null
        set(value) {
            value?.let {
                if (autoHide) {
                    it.visible = false
                }
            }
            field = value
        }
    private var size = Vector2d.ZERO
    internal var max = Vector2d.ZERO
        private set
    private var updateJob = JobHandle(this)
    var scrollX = 0.0
    var scrollY = 0.0

    init {
        on(GuiEvent.SCROLL) { event ->
            val delta = event.delta.pixelDeltaFor(
                Vector2d(scrollStep.toDouble(), scrollStep.toDouble()),
                Vector2i(
                    (size.x / scrollStep).ceilToInt(),
                    (size.y / scrollStep).ceilToInt()
                )
            )
            scrollX -= delta.x
            scrollY -= delta.y
            scrollX = clamp(
                scrollX, 0.0,
                max(0.0, max.x - event.size.x)
            )
            scrollY = clamp(
                scrollY, 0.0,
                max(0.0, max.y - event.size.y)
            )
            sliderX?.let { slider ->
                val limit = max(0.0, max.x - event.size.y)
                if (limit > 0.0) {
                    slider.value = scrollX / limit
                } else {
                    slider.value = 0.0
                }
            }
            sliderY?.let { slider ->
                val limit = max(0.0, max.y - event.size.y)
                if (limit > 0.0) {
                    slider.value = scrollY / limit
                } else {
                    slider.value = 0.0
                }
            }
        }
    }

    override fun render(
        gl: GL,
        shader: Shader,
        size: Vector2d,
        pixelSize: Vector2d,
        delta: Double
    ) {
        if (visible) {
            val matrixStack = gl.matrixStack
            val matrix = matrixStack.current()
            val start = matrix.modelViewProjection().multiply(Vector3d.ZERO)
            val end = matrix.modelViewProjection().multiply(
                Vector3d(size.x, size.y, 0.0)
            )
            val xx = ((start.x * 0.5 + 0.5) * gl.contentWidth).floorToInt()
            val yy = ((0.5 - start.y * 0.5) * gl.contentHeight).floorToInt()
            val xx2 = ((end.x * 0.5 + 0.5) * gl.contentWidth).floorToInt()
            val yy2 = ((0.5 - end.y * 0.5) * gl.contentHeight).floorToInt()
            gl.enableScissor(
                min(xx, xx2), min(yy, yy2) + 1, abs(xx - xx2),
                abs(yy - yy2)
            )
            super.render(gl, shader, size, pixelSize, delta)
            gl.disableScissor()
        }
    }

    override fun updateVisible() {
        updateJob.launchOrStop(isVisible, renderExecutor) {
            while (true) {
                yield() // Wait for next frame
                size()?.let { size ->
                    val layout = layoutManager(size)
                    layout.layout()
                    setMax(layout.size(), size)
                }
            }
        }
    }

    override fun transform(matrix: Matrix, size: Vector2d) {
        matrix.translate((-scrollX).toFloat(), (-scrollY).toFloat(), 0.0f)
    }

    private fun setMax(max: Vector2d, size: Vector2d) {
        if (this.size != size || this.max != max) {
            this.size = size
            this.max = max
            scrollX = clamp(scrollX, 0.0, max(0.0, max.x - size.x))
            scrollY = clamp(scrollY, 0.0, max(0.0, max.y - size.y))
            sliderX?.let { slider ->
                slider.sliderHeight = if (max.y <= 0) size.x
                else min(sqr(size.x) / max.y, size.x)
                if (autoHide) slider.visible = max.y > size.y
            }
            sliderY?.let { slider ->
                slider.sliderHeight = if (max.y <= 0) size.y
                else min(sqr(size.y) / max.y, size.y)
                if (autoHide) slider.visible = max.y > size.y
            }
        }
    }
}
