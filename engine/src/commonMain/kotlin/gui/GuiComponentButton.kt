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
import org.tobi29.scapes.engine.sound.CLICK

open class GuiComponentButton(
    parent: GuiLayoutData
) : GuiComponentSlab(parent) {
    private var hovered = false

    init {
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
        gui.style.button(renderer, size, hovered)
    }
}

fun GuiComponent.playClickSound() {
    engine.sounds.playSound(CLICK, "sound.GUI", 1.0, 1.0)
}
