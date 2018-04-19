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

import org.tobi29.math.vector.Vector2d
import org.tobi29.scapes.engine.input.ScrollDelta
import org.tobi29.stdex.JsName
import org.tobi29.stdex.JvmName
import org.tobi29.stdex.math.clamp

class GuiComponentSliderVert(
    parent: GuiLayoutData,
    sliderHeight: Int,
    value: Double
) : GuiComponent(parent) {
    var sliderHeight = 0.0
        set(value) {
            field = value
            dirty()
        }
    private var hovered = false
    var value: Double = value
        set(value) {
            val v = clamp(value, 0.0, 1.0)
            field = v
            dirty()
            gui.sendNewEvent(GuiEvent.CHANGE, GuiComponentEvent(), this)
        }

    init {
        this.sliderHeight = sliderHeight.toDouble()
        on(GuiEvent.DRAG_LEFT) { event ->
            this.value = (event.y - this.sliderHeight * 0.5) /
                    (event.size.y - this.sliderHeight)
        }
        on(GuiEvent.SCROLL) { event ->
            // TODO: Do we want to scroll on pixel delta?
            this.value = when (event.delta) {
                is ScrollDelta.Line ->
                    this.value - event.delta.delta.y * 0.05
                is ScrollDelta.Page -> when {
                    event.delta.delta.y > 0.0 -> 1.0
                    event.delta.delta.y < 0.0 -> 0.0
                    else -> this.value
                }
                else -> this.value
            }
        }
        on(GuiEvent.CLICK_LEFT) {
            playClickSound()
        }
        on(GuiEvent.HOVER_ENTER) {
            hovered = true
            dirty()
        }
        on(GuiEvent.HOVER_LEAVE) {
            hovered = false
            dirty()
        }
    }

    constructor(parent: GuiLayoutData, value: Double) : this(parent, 16, value)

    override fun updateMesh(renderer: GuiRenderer, size: Vector2d) {
        gui.style.slider(renderer, size, false, value, sliderHeight, hovered)
    }

    // TODO: Remove after 0.0.13

    @Deprecated("Use property")
    @JsName("getValue")
    fun value(): Double {
        return value
    }

    @Deprecated("Use property")
    @JvmName("setValueFun")
    fun setValue(value: Double) {
        this.value = value
    }

    @Deprecated("Use property")
    @JvmName("setSliderHeightFun")
    fun setSliderHeight(value: Double) {
        sliderHeight = value
        dirty()
    }
}
