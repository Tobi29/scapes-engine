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

import org.tobi29.scapes.engine.input.ScrollDelta
import org.tobi29.scapes.engine.math.Face
import org.tobi29.scapes.engine.math.vector.Vector2d
import org.tobi29.scapes.engine.math.vector.div
import org.tobi29.scapes.engine.utils.AtomicReference
import org.tobi29.scapes.engine.utils.ConcurrentHashMap
import org.tobi29.scapes.engine.utils.ConcurrentHashSet
import org.tobi29.scapes.engine.utils.computeAbsent

abstract class Gui(val style: GuiStyle) : GuiComponentSlabHeavy(style.engine,
        GuiLayoutDataRoot()) {
    private val actions = ConcurrentHashMap<GuiAction, MutableSet<() -> Unit>>()
    private var currentSelectionMut = AtomicReference<GuiComponent?>(null)
    var currentSelection: GuiComponent?
        get() = currentSelectionMut.get()
        set(value) = currentSelectionMut.set(value)

    init {
        on(GuiAction.ACTIVATE, {
            currentSelectionMut.get()?.let { selection ->
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
                currentSelectionMut.get()?.let { selection ->
                    sendNewEvent(GuiEvent.SCROLL,
                            GuiComponentEventScroll(Double.NaN, Double.NaN,
                                    delta = ScrollDelta.Line(
                                            Vector2d(1.0, 0.0))), selection)
                }
            }
        })
        on(GuiAction.RIGHT, {
            if (!moveSelection(Face.WEST)) {
                currentSelectionMut.get()?.let { selection ->
                    sendNewEvent(GuiEvent.SCROLL,
                            GuiComponentEventScroll(Double.NaN, Double.NaN,
                                    delta = ScrollDelta.Line(
                                            Vector2d(-1.0, 0.0))), selection)
                }
            }
        })
    }

    private fun moveSelection(face: Face): Boolean {
        var level = currentSelectionMut.get() ?: findSelectable() ?: return false
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
        currentSelectionMut.set(next)
        return true
    }

    fun selectDefault() {
        if (!removedMut) {
            if (currentSelectionMut.get() === null) {
                currentSelectionMut.compareAndSet(null, findSelectable())
            }
        }
    }

    fun deselect(component: GuiComponent) {
        if (currentSelectionMut.get() === component) {
            currentSelectionMut.compareAndSet(component, findSelectable())
        }
    }

    fun on(action: GuiAction,
           listener: () -> Unit) {
        val listeners = actions.computeAbsent(action) { ConcurrentHashSet() }
        listeners.add(listener)
    }

    fun <T : GuiComponentEvent> fireNewEvent(
            type: GuiEvent<T>,
            event: T
    ): GuiComponent? {
        return fireNewEvent(event, GuiComponent.sink(type))
    }

    fun <T : GuiComponentEvent> fireNewEvent(
            event: T,
            listener: (GuiComponent, T) -> Boolean
    ): GuiComponent? {
        return fireEvent(scaleEvent(event), listener)
    }

    fun <T : GuiComponentEvent> fireNewRecursiveEvent(
            type: GuiEvent<T>,
            event: T
    ): Set<GuiComponent> {
        return fireNewRecursiveEvent(event, GuiComponent.sink(type))
    }

    fun <T : GuiComponentEvent> fireNewRecursiveEvent(
            event: T,
            listener: (GuiComponent, T) -> Boolean
    ): Set<GuiComponent> {
        return fireRecursiveEvent(scaleEvent(event), listener)
    }

    fun <T : GuiComponentEvent> sendNewEvent(
            type: GuiEvent<T>,
            event: T,
            destination: GuiComponent
    ): Boolean {
        return sendNewEvent(event, destination,
                GuiComponent.sink(type, destination))
    }

    fun <T : GuiComponentEvent> sendNewEvent(
            event: T,
            destination: GuiComponent,
            listener: (T) -> Unit
    ): Boolean {
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
            currentSelectionMut.get()?.let { selection ->
                sendNewEvent(GuiComponentEvent(),
                        selection) { selection.hover(it) }
            }
        }
    }

    override fun ignoresEvents(): Boolean {
        return true
    }

    private fun <T : GuiComponentEvent> scaleEvent(event: T): T {
        val size = baseSize()
        val container = engine.container
        val containerSize = Vector2d(container.containerWidth.toDouble(),
                container.containerHeight.toDouble())
        return event.scale(size / containerSize, size)
    }
}
