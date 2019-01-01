/*
 * Copyright 2012-2019 Tobi29
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
import org.tobi29.stdex.math.clamp

class GuiComponentSlider(
    parent: GuiLayoutData,
    textSize: Int,
    text: String,
    value: Double,
    textFilter: (String, Double) -> String = { text1, value1 -> text1 + ": " + (value1 * 100).toInt() + '%' }
) : GuiComponentSlab(parent) {
    private val text: GuiComponentText
    private val textFilter: (Double) -> String
    private var hovered = false
    var value: Double = value
        set(value) {
            val v = clamp(value, 0.0, 1.0)
            field = v
            dirty()
            text.text = textFilter(v)
            gui.sendNewEvent(GuiEvent.CHANGE, GuiComponentEvent(), this)
        }

    init {
        this.textFilter = { v -> textFilter(text, v) }
        this.text = addSubHori(
            4.0, 0.0, -1.0, textSize.toDouble()
        ) { GuiComponentText(it, this.textFilter(value)) }
        on(GuiEvent.DRAG_LEFT) { event ->
            this.value = (event.x - 8.0) / (event.size.x - 16.0)
        }
        on(GuiEvent.SCROLL) { event ->
            // TODO: Do we want to scroll on pixel delta?
            this.value = when (event.delta) {
                is ScrollDelta.Line ->
                    this.value - event.delta.delta.x * 0.05
                is ScrollDelta.Page -> when {
                    event.delta.delta.x > 0.0 -> 1.0
                    event.delta.delta.x < 0.0 -> 0.0
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

    override fun updateMesh(renderer: GuiRenderer, size: Vector2d) {
        gui.style.slider(renderer, size, true, value, 16.0, hovered)
    }
}
