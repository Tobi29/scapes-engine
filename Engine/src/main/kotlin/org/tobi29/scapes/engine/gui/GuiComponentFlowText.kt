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
import org.tobi29.scapes.engine.graphics.FontRenderer

class GuiComponentFlowText(
    parent: GuiLayoutData,
    private val textWidth: Double,
    text: String,
    private val r: Double = 1.0,
    private val g: Double = 1.0,
    private val b: Double = 1.0,
    private val a: Double = 1.0
) : GuiComponent(parent) {
    var textFilter: (String) -> String = { it }
        set(value) {
            field = value
            dirty()
        }
    var text: String = ""
        set(value) {
            if (field != value) {
                field = value
                dirty()
            }
        }

    init {
        this.text = text
        parent.preferredSize = { size, maxSize ->
            val layoutSize = mangleSize(size, maxSize)
            val font = gui.style.font
            val textInfo = font.render(
                FontRenderer.to(), textFilter(text),
                layoutSize.y, textWidth
            )
            textInfo.size
        }
    }

    constructor(
        parent: GuiLayoutData,
        text: String
    ) : this(parent, Double.MAX_VALUE, text)

    constructor(
        parent: GuiLayoutData,
        text: String,
        r: Double,
        g: Double,
        b: Double,
        a: Double
    ) : this(parent, Double.MAX_VALUE, text, r, g, b, a)

    override fun updateMesh(
        renderer: GuiRenderer,
        size: Vector2d
    ) {
        val font = gui.style.font
        font.render(
            FontRenderer.to(renderer, r, g, b, a), textFilter(text),
            size.y, textWidth
        )
    }
}
