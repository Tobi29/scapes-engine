/*
 * Copyright 2012-2016 Tobi29
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
import org.tobi29.scapes.engine.utils.math.vector.Vector2d

class GuiBasicStyle(override val engine: ScapesEngine, override val font: FontRenderer) : GuiStyle {

    override fun pane(renderer: GuiRenderer,
                      size: Vector2d) {
        renderer.texture(engine.graphics.textureEmpty(), 0)
        GuiUtils.shadow(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, 0.2f)
        GuiUtils.rectangle(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, 0.3f)
    }

    override fun button(renderer: GuiRenderer,
                        size: Vector2d,
                        hover: Boolean) {
        renderer.texture(engine.graphics.textureEmpty(), 0)
        val a: Float
        if (hover) {
            a = 1.0f
        } else {
            a = 0.5f
        }
        GuiUtils.rectangle(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, a)
    }

    override fun border(renderer: GuiRenderer,
                        size: Vector2d) {
        renderer.texture(engine.graphics.textureEmpty(), 0)
        GuiUtils.shadow(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, 0.2f)
    }

    override fun slider(renderer: GuiRenderer,
                        size: Vector2d,
                        horizontal: Boolean,
                        value: Double,
                        sliderSize: Double,
                        hover: Boolean) {
        var value = value
        renderer.texture(engine.graphics.textureEmpty(), 0)
        val v: Float
        val a: Float
        val ab: Float
        if (hover) {
            if (horizontal) {
                v = 0.3f
                a = 1.0f
                ab = 1.0f
            } else {
                v = 0.0f
                a = 1.0f
                ab = 0.3f
            }
        } else {
            v = 0.0f
            a = 0.5f
            ab = 0.3f
        }
        GuiUtils.rectangle(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, ab)
        if (horizontal) {
            value *= (size.x - sliderSize)
            GuiUtils.rectangle(renderer, value.toFloat(), 0.0f,
                    (value + sliderSize).toFloat(), size.floatY(), v, v, v, a)
        } else {
            value *= (size.y - sliderSize)
            GuiUtils.rectangle(renderer, 0.0f, value.toFloat(), size.floatX(),
                    (value + sliderSize).toFloat(), v, v, v, a)
        }
    }

    override fun separator(renderer: GuiRenderer,
                           size: Vector2d) {
        renderer.texture(engine.graphics.textureEmpty(), 0)
        GuiUtils.rectangle(renderer, 0.0f, 0.0f, size.floatX(),
                size.floatY() * 0.5f, 0.0f, 0.0f, 0.0f, 0.3f)
        GuiUtils.rectangle(renderer, 0.0f, size.floatY() * 0.5f, size.floatX(),
                size.floatY(), 0.2f, 0.2f, 0.2f, 0.3f)
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
