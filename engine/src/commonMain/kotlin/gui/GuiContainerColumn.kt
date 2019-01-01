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

interface GuiContainerColumn {
    fun <T : GuiComponent> addHori(
        marginX: Double, marginY: Double,
        width: Double, height: Double,
        priority: Long = 0L,
        child: (GuiLayoutDataFlow) -> T
    ): T = addHori(
        marginX, marginY, marginX, marginY, width, height, priority, child
    )

    fun <T : GuiComponent> addHori(
        marginStartX: Double, marginStartY: Double,
        marginEndX: Double, marginEndY: Double,
        width: Double, height: Double,
        priority: Long = 0L,
        child: (GuiLayoutDataFlow) -> T
    ): T = addHori(
        Vector2d(marginStartX, marginStartY), Vector2d(marginEndX, marginEndY),
        Vector2d(width, height), priority, child
    )

    fun <T : GuiComponent> addHori(
        marginStart: Vector2d,
        marginEnd: Vector2d,
        size: Vector2d,
        priority: Long = 0L,
        child: (GuiLayoutDataFlow) -> T
    ): T

    fun spacer(priority: Long = 0): GuiComponentGroup =
        addHori(0.0, 0.0, 0.0, 0.0, -1.0, -1.0, priority, ::GuiComponentGroup)
}
