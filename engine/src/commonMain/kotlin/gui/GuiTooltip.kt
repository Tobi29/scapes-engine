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

import kotlinx.coroutines.yield
import org.tobi29.coroutines.JobHandle
import org.tobi29.coroutines.launchOrStop
import org.tobi29.math.vector.Vector2d
import org.tobi29.stdex.atomic.AtomicReference

class GuiTooltip(style: GuiStyle) : Gui(style) {
    private val currentTooltip =
        AtomicReference<Pair<GuiComponent, GuiCursor>?>(null)
    private var lastTooltip: Pair<GuiComponent, GuiCursor>? = null
    private var currentPane: Pair<GuiComponent, () -> Unit>? = null
    private var updateJob = JobHandle(this)

    fun setTooltip(component: Pair<GuiComponent, GuiCursor>?) {
        currentTooltip.set(component)
    }

    override fun updateVisible() {
        updateJob.launchOrStop(isVisible, renderExecutor) {
            while (true) {
                yield() // Wait for next frame
                update()
            }
        }
    }

    private fun update() {
        val tooltip = currentTooltip.get()
        if (tooltip != lastTooltip) {
            lastTooltip = tooltip
            currentPane?.first?.remove()
            currentPane = null
            if (tooltip != null) {
                val cursor = tooltip.second
                val layoutData = GuiLayoutDataAbsolute(
                    this, cursor.pos, Vector2d(-1.0, -1.0), 0, true
                )
                val pane = GuiComponentPaneHeavy(layoutData)
                tooltip.first.tooltip(pane)?.let { update ->
                    currentPane = Pair(pane, {
                        val parent = pane.parent
                        if (parent is GuiLayoutDataAbsolute) {
                            val pos = parent.posMutable()
                            pos.set(cursor.pos)
                        }
                        update()
                    })
                    append(pane)
                }
            }
        }
        currentPane?.second?.invoke()
    }
}
