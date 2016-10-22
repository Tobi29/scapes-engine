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

import java8.util.concurrent.ConcurrentMaps
import org.tobi29.scapes.engine.utils.collect
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.math.min
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class Gui protected constructor(val style: GuiStyle) : GuiComponentSlabHeavy(
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
                    return@on
                }
                val entry = selections[selection]
                sendNewEvent(GuiEvent.CLICK_LEFT, GuiComponentEvent(),
                        entry.components[min(selectionColumn,
                                entry.components.size - 1)])
            }
        })
        on(GuiAction.UP, {
            synchronized(selections) {
                selection = max(selection - 1,
                        min(0, selections.size - 1))
            }
        })
        on(GuiAction.DOWN, {
            synchronized(selections) {
                selection = min(selection + 1, selections.size - 1)
            }
        })
        on(GuiAction.LEFT, {
            synchronized(selections) {
                if (selection < 0) {
                    return@on
                }
                val entry = selections[selection]
                if (selectionColumn > 0) {
                    selectionColumn = min(selectionColumn,
                            entry.components.size - 1)
                    selectionColumn = max(selectionColumn - 1, 0)
                }
                sendNewEvent(GuiEvent.SCROLL,
                        GuiComponentEvent(Double.NaN,
                                Double.NaN, 1.0, 0.0,
                                false), entry.components[min(selectionColumn,
                        entry.components.size - 1)])
            }
        })
        on(GuiAction.RIGHT, {
            synchronized(selections) {
                if (selection < 0) {
                    return@on
                }
                val entry = selections[selection]
                if (selectionColumn < entry.components.size - 1) {
                    selectionColumn = min(selectionColumn + 1,
                            entry.components.size - 1)
                }
                sendNewEvent(GuiEvent.SCROLL,
                        GuiComponentEvent(Double.NaN,
                                Double.NaN, -1.0, 0.0,
                                false), entry.components[min(selectionColumn,
                        entry.components.size - 1)])
            }
        })
    }

    protected fun selection(component: GuiComponent) {
        selection(component.parent.priority, component)
    }

    protected fun selection(priority: Long,
                            component: GuiComponent) {
        addSelection(priority, collect(component))
    }

    protected fun selection(vararg components: GuiComponent) {
        if (components.size == 0) {
            return
        }
        selection(components[0].parent.priority, *components)
    }

    protected fun selection(priority: Long,
                            vararg components: GuiComponent) {
        if (components.size == 0) {
            return
        }
        addSelection(priority, collect(*components))
    }

    protected fun selection(components: List<GuiComponent>) {
        if (components.isEmpty()) {
            return
        }
        selection(components[0].parent.priority, components)
    }

    protected fun selection(priority: Long,
                            components: List<GuiComponent>) {
        if (components.isEmpty()) {
            return
        }
        val list = ArrayList<GuiComponent>()
        list.addAll(components)
        addSelection(priority, list)
    }

    private fun addSelection(priority: Long,
                             components: MutableList<GuiComponent>) {
        val entry = SelectionEntry(priority, components)
        synchronized(selections) {
            for (i in selections.indices.reversed()) {
                if (selections[i].priority >= priority) {
                    selections.add(i + 1, entry)
                    return
                }
            }
            selections.add(0, entry)
        }
    }

    fun on(action: GuiAction,
           listener: () -> Unit) {
        val listeners = ConcurrentMaps.computeIfAbsent(actions, action) { key ->
            Collections.newSetFromMap(ConcurrentHashMap())
        }
        listeners.add(listener)
    }

    fun fireNewEvent(type: GuiEvent,
                     event: GuiComponentEvent): GuiComponent? {
        return fireNewEvent(event, GuiComponent.sink(type))
    }

    fun fireNewEvent(event: GuiComponentEvent,
                     listener: Function2<GuiComponent, GuiComponentEvent, Boolean>): GuiComponent? {
        return fireEvent(GuiComponentEvent(event, baseSize()), listener)
    }

    fun fireNewRecursiveEvent(type: GuiEvent,
                              event: GuiComponentEvent): Set<GuiComponent> {
        return fireNewRecursiveEvent(event, GuiComponent.sink(type))
    }

    fun fireNewRecursiveEvent(event: GuiComponentEvent,
                              listener: Function2<GuiComponent, GuiComponentEvent, Boolean>): Set<GuiComponent> {
        return fireRecursiveEvent(GuiComponentEvent(event, baseSize()),
                listener)
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
        return sendEvent(GuiComponentEvent(event, baseSize()), destination,
                listener)
    }

    fun fireAction(action: GuiAction): Boolean {
        val listeners = actions[action]
        if (listeners == null || listeners.isEmpty()) {
            return false
        }
        listeners.forEach { it() }
        return true
    }

    abstract val isValid: Boolean

    public override fun update(delta: Double) {
        super.update(delta)
        if (isVisible) {
            synchronized(selections) {
                val iterator = selections.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    val componentIterator = entry.components.iterator()
                    while (componentIterator.hasNext()) {
                        val component = componentIterator.next()
                        if (component.removed) {
                            componentIterator.remove()
                        }
                    }
                    if (entry.components.isEmpty()) {
                        iterator.remove()
                        selection = min(selection, selections.size - 1)
                    }
                }
                if (selection < 0) {
                    return
                }
                val entry = selections[selection]
                val component = entry.components[min(selectionColumn,
                        entry.components.size - 1)]
                sendNewEvent(GuiComponentEvent(), component,
                        { component.hover(it) })
            }
        }
    }

    override fun ignoresEvents(): Boolean {
        return true
    }

    private class SelectionEntry(val priority: Long, val components: MutableList<GuiComponent>)
}
