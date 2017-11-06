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

import org.tobi29.scapes.engine.utils.AtomicReference
import org.tobi29.scapes.engine.math.vector.Vector2d

class GuiTooltip(style: GuiStyle) : Gui(style) {
    private val currentTooltip = AtomicReference<Pair<GuiComponent, GuiCursor>?>(
            null)
    private var lastTooltip: Pair<GuiComponent, GuiCursor>? = null
    private var currentPane: Pair<GuiComponent, () -> Unit>? = null

    override fun update(delta: Double) {
        super.update(delta)
        val tooltip = currentTooltip.get()
        if (tooltip != lastTooltip) {
            lastTooltip = tooltip
            currentPane?.first?.remove()
            currentPane = null
            if (tooltip != null) {
                val cursor = tooltip.second
                val layoutData = GuiLayoutDataAbsolute(this,
                        cursor.currentPos(), Vector2d(-1.0, -1.0), 0, true)
                val pane = GuiComponentPaneHeavy(layoutData)
                tooltip.first.tooltip(pane)?.let { update ->
                    currentPane = Pair(pane, {
                        val parent = pane.parent
                        if (parent is GuiLayoutDataAbsolute) {
                            val pos = parent.posMutable()
                            pos.set(cursor.currentPos())
                        }
                        update()
                    })
                    append(pane)
                }
            }
        }
        currentPane?.second?.invoke()
    }

    fun setTooltip(component: Pair<GuiComponent, GuiCursor>?) {
        currentTooltip.set(component)
    }

    override val isValid: Boolean
        get() = true
}
