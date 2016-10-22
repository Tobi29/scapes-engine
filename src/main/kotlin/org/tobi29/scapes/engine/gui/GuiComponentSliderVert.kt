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

import org.tobi29.scapes.engine.utils.math.clamp
import org.tobi29.scapes.engine.utils.math.vector.Vector2d

class GuiComponentSliderVert(parent: GuiLayoutData, sliderHeight: Int,
                             private var value: Double) : GuiComponent(parent) {
    private var sliderHeight = 0.0
    private var hovered = false

    constructor(parent: GuiLayoutData, value: Double) : this(parent, 16,
            value) {
    }

    init {
        this.sliderHeight = sliderHeight.toDouble()
        on(GuiEvent.DRAG_LEFT) { event ->
            setValue(
                    (event.y - this.sliderHeight * 0.5) / (event.size.y - this.sliderHeight))
        }
        on(GuiEvent.SCROLL) { event ->
            if (!event.screen) {
                val delta = event.relativeY * 0.05
                setValue(this.value - delta)
            }
        }
        on(GuiEvent.CLICK_LEFT) { event ->
            engine.sounds.playSound("Engine:sound/Click.ogg", "sound.GUI",
                    1.0f, 1.0f)
        }
        on(GuiEvent.HOVER_ENTER) { event ->
            hovered = true
            dirty()
        }
        on(GuiEvent.HOVER_LEAVE) { event ->
            hovered = false
            dirty()
        }
    }

    override fun updateMesh(renderer: GuiRenderer,
                            size: Vector2d) {
        gui.style.slider(renderer, size, false, value.toFloat().toDouble(),
                sliderHeight,
                hovered)
    }

    fun value(): Double {
        return value
    }

    fun setValue(value: Double) {
        this.value = clamp(value, 0.0, 1.0)
        dirty()
        gui.sendNewEvent(GuiEvent.CHANGE, GuiComponentEvent(), this)
    }

    fun setSliderHeight(value: Double) {
        sliderHeight = value
        dirty()
    }
}
