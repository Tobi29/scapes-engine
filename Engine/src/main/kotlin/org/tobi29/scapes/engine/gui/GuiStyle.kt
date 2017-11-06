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

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.FontRenderer
import org.tobi29.scapes.engine.math.vector.Vector2d

interface GuiStyle {
    val engine: ScapesEngine

    val font: FontRenderer

    fun pane(renderer: GuiRenderer,
             size: Vector2d)

    fun button(renderer: GuiRenderer,
               size: Vector2d,
               hover: Boolean)

    fun border(renderer: GuiRenderer,
               size: Vector2d)

    fun slider(renderer: GuiRenderer,
               size: Vector2d,
               horizontal: Boolean,
               value: Double,
               sliderSize: Double,
               hover: Boolean)

    fun separator(renderer: GuiRenderer,
                  size: Vector2d)

    fun widget(renderer: GuiRenderer,
               size: Vector2d)

    fun widgetTitle(renderer: GuiRenderer,
                    size: Vector2d)
}
