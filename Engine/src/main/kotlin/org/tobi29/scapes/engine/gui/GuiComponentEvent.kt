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

@file:Suppress("UNCHECKED_CAST")

package org.tobi29.scapes.engine.gui

import org.tobi29.scapes.engine.input.ScrollDelta
import org.tobi29.scapes.engine.math.vector.Vector2d

open class GuiComponentEvent(val x: Double = Double.NaN,
                             val y: Double = Double.NaN,
                             val scaleX: Double = 1.0,
                             val scaleY: Double = 1.0,
                             val size: Vector2d = Vector2d(
                                     Int.MAX_VALUE.toDouble(),
                                     Int.MAX_VALUE.toDouble())) {
    open fun copyImpl(
            x: Double,
            y: Double,
            scaleX: Double,
            scaleY: Double,
            size: Vector2d
    ) = GuiComponentEvent(x, y, scaleX, scaleY, size)
}

class GuiComponentEventDrag(
        x: Double = Double.NaN,
        y: Double = Double.NaN,
        scaleX: Double = 1.0,
        scaleY: Double = 1.0,
        val relativeX: Double = Double.NaN,
        val relativeY: Double = Double.NaN,
        size: Vector2d = Vector2d(
                Int.MAX_VALUE.toDouble(),
                Int.MAX_VALUE.toDouble())
) : GuiComponentEvent(x, y, scaleX, scaleY, size) {
    override fun copyImpl(
            x: Double,
            y: Double,
            scaleX: Double,
            scaleY: Double,
            size: Vector2d
    ) = GuiComponentEventDrag(x, y, scaleX, scaleY, relativeX, relativeY, size)
}

class GuiComponentEventScroll(
        x: Double = Double.NaN,
        y: Double = Double.NaN,
        scaleX: Double = 1.0,
        scaleY: Double = 1.0,
        val delta: ScrollDelta,
        size: Vector2d = Vector2d(
                Int.MAX_VALUE.toDouble(),
                Int.MAX_VALUE.toDouble())
) : GuiComponentEvent(x, y, scaleX, scaleY, size) {
    override fun copyImpl(
            x: Double,
            y: Double,
            scaleX: Double,
            scaleY: Double,
            size: Vector2d
    ) = GuiComponentEventScroll(x, y, scaleX, scaleY, delta, size)
}

@Suppress("UNCHECKED_CAST")
fun <T : GuiComponentEvent> T.copy(
        x: Double = this.x,
        y: Double = this.y,
        scaleX: Double = this.scaleX,
        scaleY: Double = this.scaleY,
        size: Vector2d = this.size
) = copyImpl(x, y, scaleX, scaleY, size) as T

@Suppress("UNCHECKED_CAST")
fun <T : GuiComponentEvent> T.scale(
        scale: Vector2d,
        size: Vector2d = this.size
) = copy(x * scale.x, y * scale.y, scaleX * scale.x, scaleY * scale.y, size)
