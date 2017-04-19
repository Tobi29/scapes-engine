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
import org.tobi29.scapes.engine.utils.math.vector.Vector2d

open class GuiComponentSlabHeavy : GuiComponentHeavy, GuiContainerColumn {
    constructor(
            parent: GuiLayoutData
    ) : super(parent)

    internal constructor(
            engine: ScapesEngine,
            parent: GuiLayoutData
    ) : super(engine, parent)

    fun <T : GuiComponent> add(x: Double,
                               y: Double,
                               width: Double,
                               height: Double,
                               child: (GuiLayoutDataAbsolute) -> T): T {
        return add(x, y, width, height, 0, child)
    }

    fun <T : GuiComponent> add(x: Double,
                               y: Double,
                               width: Double,
                               height: Double,
                               priority: Long,
                               child: (GuiLayoutDataAbsolute) -> T): T {
        return add(Vector2d(x, y), Vector2d(width, height), priority,
                child)
    }

    fun <T : GuiComponent> add(pos: Vector2d,
                               size: Vector2d,
                               priority: Long,
                               child: (GuiLayoutDataAbsolute) -> T): T {
        val layoutData = GuiLayoutDataAbsolute(this, pos, size, priority)
        val component = child(layoutData)
        append(component)
        return component
    }

    override fun <T : GuiComponent> addHori(marginStart: Vector2d,
                                            marginEnd: Vector2d,
                                            size: Vector2d,
                                            priority: Long,
                                            child: (GuiLayoutDataFlow) -> T): T {
        val layoutData = GuiLayoutDataFlow(this, marginStart, marginEnd,
                size,
                priority)
        val component = child(layoutData)
        append(component)
        return component
    }

    protected fun <T : GuiComponent> addSubHori(marginX: Double,
                                                marginY: Double,
                                                width: Double,
                                                height: Double,
                                                child: (GuiLayoutDataFlow) -> T): T {
        return addSubHori(marginX, marginY, marginX, marginY, width, height,
                child)
    }

    fun <T : GuiComponent> addSubHori(marginStartX: Double,
                                      marginStartY: Double,
                                      marginEndX: Double,
                                      marginEndY: Double,
                                      width: Double,
                                      height: Double,
                                      child: (GuiLayoutDataFlow) -> T): T {
        return addSubHori(marginStartX, marginStartY, marginEndX, marginEndY,
                width, height, 0, child)
    }

    fun <T : GuiComponent> addSubHori(marginStartX: Double,
                                      marginStartY: Double,
                                      marginEndX: Double,
                                      marginEndY: Double,
                                      width: Double,
                                      height: Double,
                                      priority: Long,
                                      child: (GuiLayoutDataFlow) -> T): T {
        return addSubHori(Vector2d(marginStartX, marginStartY),
                Vector2d(marginEndX, marginEndY),
                Vector2d(width, height), priority, child)
    }

    fun <T : GuiComponent> addSubHori(marginStart: Vector2d,
                                      marginEnd: Vector2d,
                                      size: Vector2d,
                                      priority: Long,
                                      child: (GuiLayoutDataFlow) -> T): T {
        val layoutData = GuiLayoutDataFlow(this, marginStart, marginEnd,
                size,
                priority, true)
        val component = child(layoutData)
        append(component)
        return component
    }

    fun spacer(priority: Long = 0): GuiComponentGroup {
        return addHori(0.0, 0.0, 0.0, 0.0, -1.0, -1.0, priority,
                ::GuiComponentGroup)
    }

    override fun newLayoutManager(size: Vector2d): GuiLayoutManager {
        return GuiLayoutManagerHorizontal(Vector2d.ZERO, size, components)
    }
}
