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
import org.tobi29.math.vector.addX
import org.tobi29.math.vector.addY

open class GuiComponentWidget(
    parent: GuiLayoutData,
    name: String
) : GuiComponentPaneHeavy(parent) {
    init {
        val titleBar = addVert(0.0, 0.0, -1.0, 16.0) {
            GuiComponentWidgetTitle(it, 12, name)
        }
        if (parent is GuiLayoutDataAbsolute) {
            val pos = parent.posMutable()
            titleBar.on(GuiEvent.DRAG_LEFT) {
                pos.addX(it.relativeX * it.scaleX)
                pos.addY(it.relativeY * it.scaleY)
            }
        }
    }

    public override fun updateMesh(renderer: GuiRenderer, size: Vector2d) {
        gui.style.widget(renderer, size)
    }
}
