/*
 * Copyright 2012-2016 Tobi29
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

import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.Matrix
import org.tobi29.scapes.engine.graphics.Shader
import org.tobi29.scapes.engine.utils.math.clamp
import org.tobi29.scapes.engine.utils.math.min
import org.tobi29.scapes.engine.utils.math.sqr
import org.tobi29.scapes.engine.utils.math.vector.Vector2d
import org.tobi29.scapes.engine.utils.math.vector.Vector3d
import org.tobi29.scapes.engine.utils.math.vector.div

class GuiComponentScrollPaneViewport(parent: GuiLayoutData,
                                     scrollStep: Int,
                                     private val autoHide: Boolean = false) : GuiComponentPaneHeavy(
        parent) {
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
    var scrollX = 0.0
    var scrollY = 0.0

    init {
        on(GuiEvent.SCROLL) { event ->
            if (event.screen) {
                scrollX -= event.relativeX
                scrollY -= event.relativeY
            } else {
                scrollX -= event.relativeX * scrollStep
                scrollY -= event.relativeY * scrollStep
            }
            scrollX = clamp(scrollX, 0.0,
                    Math.max(0.0, max.x - event.size.x))
            scrollY = clamp(scrollY, 0.0,
                    Math.max(0.0, max.y - event.size.y))
            sliderX?.let { slider ->
                val limit = Math.max(0.0, max.x - event.size.y)
                if (limit > 0.0) {
                    slider.setValue(scrollX / limit)
                } else {
                    slider.setValue(0.0)
                }
            }
            sliderY?.let { slider ->
                val limit = Math.max(0.0, max.y - event.size.y)
                if (limit > 0.0) {
                    slider.setValue(scrollY / limit)
                } else {
                    slider.setValue(0.0)
                }
            }
        }
    }

    override fun render(gl: GL,
                        shader: Shader,
                        size: Vector2d,
                        pixelSize: Vector2d,
                        delta: Double) {
        if (visible) {
            val matrixStack = gl.matrixStack()
            val matrix = matrixStack.current()
            val start = matrix.modelView().multiply(Vector3d.ZERO) / pixelSize
            val end = matrix.modelView().multiply(
                    Vector3d(size.x, size.y, 0.0)) / pixelSize
            gl.enableScissor(start.intX(), start.intY(),
                    end.intX() - start.intX(), end.intY() - start.intY())
            super.render(gl, shader, size, pixelSize, delta)
            gl.disableScissor()
        }
    }

    public override fun updateComponent(delta: Double) {
        size()?.let { size ->
            val layout = layoutManager(size)
            layout.layout()
            setMax(layout.size(), size)
        }
    }

    override fun transform(matrix: Matrix,
                           size: Vector2d) {
        matrix.translate((-scrollX).toFloat(), (-scrollY).toFloat(), 0.0f)
    }

    private fun setMax(max: Vector2d,
                       size: Vector2d) {
        if (this.size != size || this.max != max) {
            this.size = size
            this.max = max
            scrollX = clamp(scrollX, 0.0, Math.max(0.0, max.x - size.x))
            scrollY = clamp(scrollY, 0.0, Math.max(0.0, max.y - size.y))
            sliderX?.let { slider ->
                if (max.y <= 0) {
                    slider.setSliderHeight(size.x)
                } else {
                    slider.setSliderHeight(
                            min(sqr(size.x) / max.y, size.x))
                }
                if (autoHide) {
                    slider.visible = max.y > size.y
                }
            }
            sliderY?.let { slider ->
                if (max.y <= 0) {
                    slider.setSliderHeight(size.y)
                } else {
                    slider.setSliderHeight(
                            min(sqr(size.y) / max.y, size.y))
                }
                if (autoHide) {
                    slider.visible = max.y > size.y
                }
            }
        }
    }
}
