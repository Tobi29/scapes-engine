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

class GuiBasicStyle(override val engine: ScapesEngine,
                    override val font: FontRenderer) : GuiStyle {

    override fun pane(renderer: GuiRenderer,
                      size: Vector2d) {
        renderer.texture(engine.graphics.textureEmpty(), 0)
        GuiUtils.shadow(renderer, 0.0, 0.0, size.x, size.y, 0.0, 0.0, 0.0, 0.2)
        GuiUtils.rectangle(renderer, 0.0, 0.0, size.x, size.y, 0.0, 0.0, 0.0,
                0.3)
    }

    override fun button(renderer: GuiRenderer,
                        size: Vector2d,
                        hover: Boolean) {
        renderer.texture(engine.graphics.textureEmpty(), 0)
        val a = if (hover) 1.0 else 0.5
        GuiUtils.rectangle(renderer, 0.0, 0.0, size.x, size.y, 0.0, 0.0, 0.0, a)
    }

    override fun border(renderer: GuiRenderer,
                        size: Vector2d) {
        renderer.texture(engine.graphics.textureEmpty(), 0)
        GuiUtils.shadow(renderer, 0.0, 0.0, size.x, size.y, 0.0, 0.0, 0.0, 0.2)
    }

    override fun slider(renderer: GuiRenderer,
                        size: Vector2d,
                        horizontal: Boolean,
                        value: Double,
                        sliderSize: Double,
                        hover: Boolean) {
        renderer.texture(engine.graphics.textureEmpty(), 0)
        val v: Double
        val a: Double
        val ab: Double
        if (hover) {
            if (horizontal) {
                v = 0.3
                a = 1.0
                ab = 1.0
            } else {
                v = 0.0
                a = 1.0
                ab = 0.3
            }
        } else {
            v = 0.0
            a = 0.5
            ab = 0.3
        }
        GuiUtils.rectangle(renderer, 0.0, 0.0, size.x, size.y,
                0.0, 0.0, 0.0, ab)
        if (horizontal) {
            val offset = value * (size.x - sliderSize)
            GuiUtils.rectangle(renderer, offset, 0.0, offset + sliderSize,
                    size.y, v, v, v, a)
        } else {
            val offset = value * (size.y - sliderSize)
            GuiUtils.rectangle(renderer, 0.0, offset, size.x,
                    offset + sliderSize, v, v, v, a)
        }
    }

    override fun separator(renderer: GuiRenderer,
                           size: Vector2d) {
        renderer.texture(engine.graphics.textureEmpty(), 0)
        GuiUtils.rectangle(renderer, 0.0, 0.0, size.x, size.y * 0.5, 0.0, 0.0,
                0.0, 0.3)
        GuiUtils.rectangle(renderer, 0.0, size.y * 0.5, size.x, size.y, 0.2,
                0.2, 0.2, 0.3)
    }

    override fun widget(renderer: GuiRenderer,
                        size: Vector2d) {
        pane(renderer, size)
    }

    override fun widgetTitle(renderer: GuiRenderer,
                             size: Vector2d) {
        pane(renderer, size)
    }
}
