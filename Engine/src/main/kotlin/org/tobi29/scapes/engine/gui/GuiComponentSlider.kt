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
package org.tobi29.scapes.engine.gui

import org.tobi29.scapes.engine.utils.math.clamp
import org.tobi29.scapes.engine.utils.math.vector.Vector2d

class GuiComponentSlider constructor(parent: GuiLayoutData,
                                     textSize: Int,
                                     text: String,
                                     private var value: Double,
                                     textFilter: (String, Double) -> String = { text1, value1 -> text1 + ": " + (value1 * 100).toInt() + '%' }) : GuiComponentSlab(
        parent) {
    private val text: GuiComponentText
    private val textFilter: (Double) -> String
    private var hovered = false

    init {
        this.textFilter = { v -> textFilter(text, v) }
        this.text = addSubHori(4.0, 0.0, -1.0, textSize.toDouble()
        ) { GuiComponentText(it, this.textFilter(value)) }
        on(GuiEvent.DRAG_LEFT) { event ->
            setValue((event.x - 8.0) / (event.size.x - 16.0))
        }
        on(GuiEvent.SCROLL) { event ->
            if (!event.screen) {
                val delta = event.relativeX * 0.05
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
        gui.style.slider(renderer, size, true, value.toFloat().toDouble(), 16.0,
                hovered)
    }

    fun value(): Double {
        return value
    }

    fun setValue(value: Double) {
        this.value = clamp(value, 0.0, 1.0)
        dirty()
        text.text = textFilter(this.value)
        gui.sendNewEvent(GuiEvent.CHANGE, GuiComponentEvent(), this)
    }
}
