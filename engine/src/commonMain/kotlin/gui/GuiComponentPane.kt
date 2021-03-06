/*
 * Copyright 2012-2019 Tobi29
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

open class GuiComponentPane(
    parent: GuiLayoutData
) : GuiComponent(parent), GuiContainerRow, GuiContainerAbsolute {
    override fun <T : GuiComponent> add(
        pos: Vector2d,
        size: Vector2d,
        priority: Long,
        child: (GuiLayoutDataAbsolute) -> T
    ): T {
        val layoutData = GuiLayoutDataAbsolute(this, pos, size, priority)
        val component = child(layoutData)
        append(component)
        return component
    }

    override fun <T : GuiComponent> addVert(
        marginStart: Vector2d,
        marginEnd: Vector2d,
        size: Vector2d,
        priority: Long,
        child: (GuiLayoutDataFlow) -> T
    ): T {
        val layoutData =
            GuiLayoutDataFlow(this, marginStart, marginEnd, size, priority)
        val component = child(layoutData)
        append(component)
        return component
    }

    protected fun <T : GuiComponent> addSubVert(
        marginX: Double, marginY: Double,
        width: Double, height: Double,
        child: (GuiLayoutDataFlow) -> T
    ): T = addSubVert(marginX, marginY, marginX, marginY, width, height, child)

    protected fun <T : GuiComponent> addSubVert(
        marginStartX: Double, marginStartY: Double,
        marginEndX: Double, marginEndY: Double,
        width: Double, height: Double,
        child: (GuiLayoutDataFlow) -> T
    ): T = addSubVert(
        marginStartX, marginStartY, marginEndX, marginEndY, width, height, 0,
        child
    )

    protected fun <T : GuiComponent> addSubVert(
        marginStartX: Double, marginStartY: Double,
        marginEndX: Double, marginEndY: Double,
        width: Double, height: Double,
        priority: Long,
        child: (GuiLayoutDataFlow) -> T
    ): T = addSubVert(
        Vector2d(marginStartX, marginStartY), Vector2d(marginEndX, marginEndY),
        Vector2d(width, height), priority, child
    )

    protected fun <T : GuiComponent> addSubVert(
        marginStart: Vector2d,
        marginEnd: Vector2d,
        size: Vector2d,
        priority: Long,
        child: (GuiLayoutDataFlow) -> T
    ): T {
        val layoutData = GuiLayoutDataFlow(
            this, marginStart, marginEnd,
            size,
            priority, true
        )
        val component = child(layoutData)
        append(component)
        return component
    }

    override fun newLayoutManager(
        components: Collection<GuiComponent>,
        size: Vector2d
    ): GuiLayoutManager =
        GuiLayoutManagerVertical(Vector2d.ZERO, size, components)
}
