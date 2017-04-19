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

import org.tobi29.scapes.engine.utils.AtomicLong

class GuiNotifications(style: GuiStyle) : Gui(style) {
    private val pane: GuiComponentGroup
    private val id = AtomicLong(Long.MIN_VALUE)

    init {
        spacer()
        pane = addHori(0.0, 0.0, 310.0, -1.0, ::GuiComponentGroup)
    }

    fun <T : GuiComponent> add(
            child: (GuiLayoutDataFlow) -> T): T {
        return pane.addVert(10.0, 10.0, 10.0, 10.0, -1.0, 60.0, id.andIncrement,
                child)
    }

    override val isValid: Boolean
        get() = true
}
