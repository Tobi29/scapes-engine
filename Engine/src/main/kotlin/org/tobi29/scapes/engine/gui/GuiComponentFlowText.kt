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

import org.tobi29.scapes.engine.graphics.FontRenderer
import org.tobi29.scapes.engine.utils.math.vector.Vector2d

class GuiComponentFlowText constructor(parent: GuiLayoutData,
                                       private val textWidth: Int,
                                       text: String,
                                       private val r: Float = 1.0f,
                                       private val g: Float = 1.0f,
                                       private val b: Float = 1.0f,
                                       private val a: Float = 1.0f) : GuiComponent(
        parent) {
    var textFilter: (String) -> String = { it }
        set(value) {
            field = value
            updateSize()
            dirty()
        }
    var text: String = ""
        set(value) {
            if (field != value) {
                field = value
                updateSize()
                dirty()
            }
        }

    constructor(parent: GuiLayoutData,
                text: String) : this(parent,
            Int.MAX_VALUE, text)

    constructor(parent: GuiLayoutData,
                text: String,
                r: Float,
                g: Float,
                b: Float,
                a: Float) : this(parent, Int.MAX_VALUE,
            text, r, g, b, a)

    init {
        this.text = text
        updateSize()
    }

    fun text(): String {
        return text
    }

    private fun updateSize() {
        val font = gui.style.font
        val textInfo = font.render(FontRenderer.to(), textFilter(text),
                parent.height().toFloat(), textWidth.toFloat())
        parent.setSize(textInfo.size)
    }

    override fun updateMesh(renderer: GuiRenderer,
                            size: Vector2d) {
        val font = gui.style.font
        font.render(FontRenderer.to(renderer, r, g, b, a),
                textFilter(text), size.floatY(), textWidth.toFloat())
    }
}
