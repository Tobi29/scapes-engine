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

import org.tobi29.scapes.engine.utils.AtomicReference
import org.tobi29.scapes.engine.utils.ConcurrentHashMap
import org.tobi29.scapes.engine.utils.ConcurrentHashSet
import org.tobi29.scapes.engine.utils.computeAbsent
import org.tobi29.scapes.engine.utils.math.Face
import org.tobi29.scapes.engine.utils.math.vector.Vector2d
import org.tobi29.scapes.engine.utils.math.vector.div

abstract class Gui(val style: GuiStyle) : GuiComponentSlabHeavy(style.engine,
        GuiLayoutDataRoot()) {
    private val actions = ConcurrentHashMap<GuiAction, MutableSet<() -> Unit>>()
    var lastClicked: GuiComponent? = null
    private var currentSelection = AtomicReference<GuiComponent?>(null)

    init {
        on(GuiAction.ACTIVATE, {
            currentSelection.get()?.let { selection ->
                sendNewEvent(GuiEvent.CLICK_LEFT, GuiComponentEvent(),
                        selection)
            }
        })
        on(GuiAction.UP, {
            moveSelection(Face.NORTH)
        })
        on(GuiAction.DOWN, {
            moveSelection(Face.SOUTH)
        })
        on(GuiAction.LEFT, {
            if (!moveSelection(Face.EAST)) {
                currentSelection.get()?.let { selection ->
                    sendNewEvent(GuiEvent.SCROLL,
                            GuiComponentEvent(Double.NaN, Double.NaN, 1.0, 0.0,
                                    false), selection)
                }
            }
        })
        on(GuiAction.RIGHT, {
            if (!moveSelection(Face.WEST)) {
                currentSelection.get()?.let { selection ->
                    sendNewEvent(GuiEvent.SCROLL,
                            GuiComponentEvent(Double.NaN, Double.NaN, -1.0, 0.0,
                                    false), selection)
                }
            }
        })
    }

    private fun moveSelection(face: Face): Boolean {
        var level = currentSelection.get() ?: findSelectable() ?: return false
        val next: GuiComponent
        while (true) {
            val container = level.parent.parent ?: return false
            val size = container.size() ?: return false
            val n = container.layoutManager(size).navigate(face, level)
            level = if (n != null) {
                var e: GuiComponent = n
                while (true) {
                    e = e.layoutManager(size).enter(face) ?: break
                }
                e
            } else container
            if (level.parent.selectable) {
                next = level
                break
            }
        }
        currentSelection.set(next)
        return true
    }

    fun selectDefault() {
        if (!removedMut) {
            if (currentSelection.get() === null) {
                currentSelection.compareAndSet(null, findSelectable())
            }
        }
    }

    fun deselect(component: GuiComponent) {
        if (currentSelection.get() === component) {
            currentSelection.compareAndSet(component, findSelectable())
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
        if (visible && !engine.guiController.activeCursor()) {
            currentSelection.get()?.let { selection ->
                sendNewEvent(GuiComponentEvent(),
                        selection) { selection.hover(it) }
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
}
