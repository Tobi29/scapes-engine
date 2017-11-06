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

import org.tobi29.scapes.engine.math.vector.Vector2d

open class GuiComponentEvent(val x: Double = Double.NaN,
                             val y: Double = Double.NaN,
                             val relativeX: Double = Double.NaN,
                             val relativeY: Double = Double.NaN,
                             val screen: Boolean = true,
                             val size: Vector2d = Vector2d(
                                     Int.MAX_VALUE.toDouble(),
                                     Int.MAX_VALUE.toDouble())) {

    constructor(x: Double,
                y: Double,
                size: Vector2d) : this(x, y,
            Double.NaN, Double.NaN, size)

    constructor(x: Double,
                y: Double,
                relativeX: Double,
                relativeY: Double,
                size: Vector2d) : this(x, y, relativeX,
            relativeY, true, size)

    constructor(parent: GuiComponentEvent,
                size: Vector2d) : this(parent.x,
            parent.y, parent.relativeX, parent.relativeY,
            parent.screen, size)

    constructor(parent: GuiComponentEvent,
                size: Vector2d,
                scale: Vector2d) : this(
            parent.x * scale.x, parent.y * scale.y, parent.relativeX * scale.x,
            parent.relativeY * scale.y, parent.screen, size)

    constructor(parent: GuiComponentEvent,
                x: Double,
                y: Double,
                size: Vector2d) : this(x, y, parent.relativeX, parent.relativeY,
            parent.screen, size)
}
