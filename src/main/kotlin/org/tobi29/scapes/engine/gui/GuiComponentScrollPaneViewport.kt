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

class GuiComponentScrollPaneViewport(parent: GuiLayoutData,
                                     scrollStep: Int) : GuiComponentPaneHeavy(
        parent) {
    internal var sliderX: GuiComponentSliderVert? = null
    internal var sliderY: GuiComponentSliderVert? = null
    private var maxX = 0.0
    private var maxY = 0.0
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
                    Math.max(0.0, maxX - event.size.x))
            scrollY = clamp(scrollY, 0.0,
                    Math.max(0.0, maxY - event.size.y))
            sliderX?.let { slider ->
                val limit = Math.max(0.0, maxX - event.size.y)
                if (limit > 0.0) {
                    slider.setValue(scrollX / limit)
                } else {
                    slider.setValue(0.0)
                }
            }
            sliderY?.let { slider ->
                val limit = Math.max(0.0, maxY - event.size.y)
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
        if (isVisible) {
            val matrixStack = gl.matrixStack()
            val matrix = matrixStack.current()
            val start = matrix.modelView().multiply(Vector3d.ZERO)
            val end = matrix.modelView().multiply(
                    Vector3d(size.x, size.y, 0.0))
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
        if (maxX != max.x || maxY != max.y) {
            maxX = max.x
            maxY = max.y
            scrollX = clamp(scrollX, 0.0, Math.max(0.0, maxX - size.x))
            scrollY = clamp(scrollY, 0.0, Math.max(0.0, maxY - size.y))
            sliderX?.let { slider ->
                if (maxY <= 0) {
                    slider.setSliderHeight(size.x)
                } else {
                    slider.setSliderHeight(
                            min(sqr(size.x) / maxY, size.x))
                }
            }
            sliderY?.let { slider ->
                if (maxY <= 0) {
                    slider.setSliderHeight(size.y)
                } else {
                    slider.setSliderHeight(
                            min(sqr(size.y) / maxY, size.y))
                }
            }
        }
    }

    fun maxX(): Double {
        return maxX
    }

    fun maxY(): Double {
        return maxY
    }
}
