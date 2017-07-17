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

import org.tobi29.scapes.engine.utils.ConcurrentHashMap
import org.tobi29.scapes.engine.utils.ConcurrentHashSet
import org.tobi29.scapes.engine.utils.computeAbsent
import org.tobi29.scapes.engine.utils.math.Face
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.math.min
import org.tobi29.scapes.engine.utils.math.vector.Vector2d
import org.tobi29.scapes.engine.utils.math.vector.div

abstract class Gui(val style: GuiStyle) : GuiComponentSlabHeavy(style.engine,
        GuiLayoutDataRoot()) {
    private val selections = ArrayList<SelectionEntry>()
    private val actions = ConcurrentHashMap<GuiAction, MutableSet<() -> Unit>>()
    var lastClicked: GuiComponent? = null
    private var selection = -1
    private var selectionColumn = 0

    init {
        on(GuiAction.ACTIVATE, {
            synchronized(selections) {
                if (selection < 0) {
                    return@synchronized
                }
                val entry = selections[selection]
                sendNewEvent(GuiEvent.CLICK_LEFT, GuiComponentEvent(),
                        entry.components[min(selectionColumn,
                                entry.components.size - 1)])
            }
        })
        on(GuiAction.UP, { moveSelectionV(true) })
        on(GuiAction.DOWN, { moveSelectionV(false) })
        on(GuiAction.LEFT, {
            moveSelectionH(true)?.let { entry ->
                sendNewEvent(GuiEvent.SCROLL,
                        GuiComponentEvent(Double.NaN,
                                Double.NaN, 1.0, 0.0,
                                false), entry.components[min(selectionColumn,
                        entry.components.size - 1)])
            }
        })
        on(GuiAction.RIGHT, {
            synchronized(selections) {
                moveSelectionH(false)?.let { entry ->
                    sendNewEvent(GuiEvent.SCROLL,
                            GuiComponentEvent(Double.NaN,
                                    Double.NaN, -1.0, 0.0,
                                    false),
                            entry.components[min(selectionColumn,
                                    entry.components.size - 1)])
                }
            }
        })
    }

    private fun fixSelection(): SelectionEntry? {
        selections.getOrNull(selection)?.let { entry ->
            val moveOnwards = entry.visible()
            if (moveOnwards == Face.NONE) {
                return entry
            }
            if (moveOnwards != Face.NORTH && moveOnwards != Face.SOUTH) {
                throw IllegalArgumentException("Invalid move: $moveOnwards")
            }
            return moveSelectionV(moveOnwards == Face.NORTH)
        }
        return null
    }

    private fun moveSelectionH(left: Boolean): SelectionEntry? {
        if (left) {
            selections.getOrNull(selection)?.let { entry ->
                if (selectionColumn > 0) {
                    selectionColumn = min(selectionColumn,
                            entry.components.size - 1)
                    selectionColumn = max(selectionColumn - 1, 0)
                }
            }
        } else {
            selections.getOrNull(selection)?.let { entry ->
                if (selectionColumn < entry.components.size - 1) {
                    selectionColumn = min(selectionColumn + 1,
                            entry.components.size - 1)
                }
            }
        }
        return fixSelection()
    }

    private fun moveSelectionV(up: Boolean): SelectionEntry? {
        var dir = up
        while (true) {
            if (dir) {
                selection = max(selection - 1, min(0, selections.lastIndex))
            } else {
                selection = min(selection + 1, selections.lastIndex)
            }
            (selections.getOrNull(selection) ?: return null).let { entry ->
                val moveOnwards = entry.visible()
                if (moveOnwards == Face.NONE) {
                    return entry
                }
                if (moveOnwards != Face.NORTH && moveOnwards != Face.SOUTH) {
                    throw IllegalArgumentException("Invalid move: $moveOnwards")
                }
                if (dir && selection <= 0) {
                    dir = false
                } else if (selection >= selections.lastIndex) {
                    dir = true
                }
            }
        }
    }

    protected fun selection(vararg components: GuiComponent) {
        if (components.isEmpty()) {
            return
        }
        selection(components[0].parent.priority, *components)
    }

    protected fun selection(priority: Long,
                            vararg components: GuiComponent) {
        if (components.isEmpty()) {
            return
        }
        addSelection(priority, arrayListOf(*components))
    }

    protected fun selection(visible: () -> Face,
                            vararg components: GuiComponent) {
        if (components.isEmpty()) {
            return
        }
        selection(components[0].parent.priority, visible, *components)
    }

    protected fun selection(priority: Long,
                            visible: () -> Face,
                            vararg components: GuiComponent) {
        if (components.isEmpty()) {
            return
        }
        addSelection(priority, arrayListOf(*components), visible)
    }

    protected fun selection(components: List<GuiComponent>,
                            visible: () -> Face = { Face.NONE }) {
        if (components.isEmpty()) {
            return
        }
        selection(components[0].parent.priority, components, visible)
    }

    protected fun selection(priority: Long,
                            components: List<GuiComponent>,
                            visible: () -> Face = { Face.NONE }) {
        if (components.isEmpty()) {
            return
        }
        val list = ArrayList<GuiComponent>()
        list.addAll(components)
        addSelection(priority, list, visible)
    }

    private fun addSelection(priority: Long,
                             components: MutableList<GuiComponent>,
                             visible: () -> Face = { Face.NONE }) {
        val entry = SelectionEntry(priority, components, visible)
        synchronized(selections) {
            for (i in selections.indices.reversed()) {
                if (selections[i].priority >= priority) {
                    selections.add(i + 1, entry)
                    return@synchronized
                }
            }
            selections.add(0, entry)
        }
    }

    fun on(action: GuiAction,
           listener: () -> Unit) {
        val listeners = actions.computeAbsent(action) { ConcurrentHashSet() }
        listeners.add(listener)
    }

    fun fireNewEvent(type: GuiEvent,
                     event: GuiComponentEvent): GuiComponent? {
        return fireNewEvent(event, GuiComponent.sink(type))
    }

    fun fireNewEvent(event: GuiComponentEvent,
                     listener: (GuiComponent, GuiComponentEvent) -> Boolean): GuiComponent? {
        return fireEvent(scaleEvent(event), listener)
    }

    fun fireNewRecursiveEvent(type: GuiEvent,
                              event: GuiComponentEvent): Set<GuiComponent> {
        return fireNewRecursiveEvent(event, GuiComponent.sink(type))
    }

    fun fireNewRecursiveEvent(event: GuiComponentEvent,
                              listener: (GuiComponent, GuiComponentEvent) -> Boolean): Set<GuiComponent> {
        return fireRecursiveEvent(scaleEvent(event), listener)
    }

    fun sendNewEvent(type: GuiEvent,
                     event: GuiComponentEvent,
                     destination: GuiComponent): Boolean {
        return sendNewEvent(event, destination,
                GuiComponent.sink(type, destination))
    }

    fun sendNewEvent(event: GuiComponentEvent,
                     destination: GuiComponent,
                     listener: (GuiComponentEvent) -> Unit): Boolean {
        return sendEvent(scaleEvent(event), destination, listener)
    }

    fun fireAction(action: GuiAction): Boolean {
        val listeners = actions[action]
        if (listeners == null || listeners.isEmpty()) {
            return false
        }
        listeners.forEach { it() }
        return true
    }

    open fun baseSize(): Vector2d {
        val container = engine.container
        return Vector2d(container.containerWidth.toDouble(),
                container.containerHeight.toDouble())
    }

    abstract val isValid: Boolean

    public override fun update(delta: Double) {
        super.update(delta)
        if (visible) {
            synchronized(selections) {
                fixSelection()
                val iterator = selections.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    val componentIterator = entry.components.iterator()
                    while (componentIterator.hasNext()) {
                        val component = componentIterator.next()
                        if (component.removedMut) {
                            componentIterator.remove()
                        }
                    }
                    if (entry.components.isEmpty()) {
                        iterator.remove()
                        selection = min(selection, selections.size - 1)
                    }
                }
                if (selection < 0) {
                    return@synchronized
                }
                val entry = selections[selection]
                val component = entry.components[min(selectionColumn,
                        entry.components.lastIndex)]
                sendNewEvent(GuiComponentEvent(), component,
                        { component.hover(it) })
            }
        }
    }

    override fun ignoresEvents(): Boolean {
        return true
    }

    private fun scaleEvent(event: GuiComponentEvent): GuiComponentEvent {
        val size = baseSize()
        val container = engine.container
        val containerSize = Vector2d(container.containerWidth.toDouble(),
                container.containerHeight.toDouble())
        return GuiComponentEvent(event, size, size / containerSize)
    }

    private class SelectionEntry(val priority: Long,
                                 val components: MutableList<GuiComponent>,
                                 val visible: () -> Face)
}
