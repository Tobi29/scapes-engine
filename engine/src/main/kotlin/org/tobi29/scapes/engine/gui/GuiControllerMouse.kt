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
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.input.*
import org.tobi29.utils.EventDispatcher
import org.tobi29.utils.listenAlive

class GuiControllerMouse(
    engine: ScapesEngine,
    controller: ControllerDesktop
) : GuiControllerDefault(engine, controller) {
    private val cursor = GuiCursor()
    private var draggingLeft: GuiComponent? = null
    private var draggingRight: GuiComponent? = null
    private var dragLeftX = 0.0
    private var dragLeftY = 0.0
    private var dragRightX = 0.0
    private var dragRightY = 0.0
    private var activeCursor = true
    private var hover: GuiComponent? = null
    private val events = EventDispatcher(engine.events) {
        listenAlive<ControllerButtons.PressEvent> { event ->
            if (when (event.action) {
                    ControllerButtons.Action.PRESS, ControllerButtons.Action.REPEAT ->
                        handlePress(
                            event.key,
                            event.action == ControllerButtons.Action.REPEAT,
                            GuiComponentEvent(controller.x, controller.y)
                        )
                    ControllerButtons.Action.RELEASE ->
                        handleRelease(
                            event.key,
                            GuiComponentEvent(controller.x, controller.y)
                        )
                }) event.muted = true
        }
        listen<ControllerMouse.ScrollEvent> { event ->
            engine.guiStack.fireRecursiveEvent(
                GuiEvent.SCROLL,
                GuiComponentEventScroll(
                    event.state.x, event.state.y,
                    delta = event.delta
                )
            )
        }
    }

    override fun update(delta: Double) {
        val cursorPos = Vector2d(controller.x, controller.y)
        if (cursor.pos != cursorPos) {
            cursor.pos = cursorPos
            activeCursor = true
        }
        draggingLeft?.let { component ->
            val guiPos = cursor.pos
            val relativeX = guiPos.x - dragLeftX
            val relativeY = guiPos.y - dragLeftY
            dragLeftX = guiPos.x
            dragLeftY = guiPos.y
            component.gui.sendNewEvent(
                GuiEvent.DRAG_LEFT,
                GuiComponentEventDrag(
                    cursorPos.x, cursorPos.y,
                    relativeX = relativeX,
                    relativeY = relativeY
                ), component
            )
        }
        draggingRight?.let { component ->
            val guiPos = cursor.pos
            val relativeX = guiPos.x - dragRightX
            val relativeY = guiPos.y - dragRightY
            dragRightX = guiPos.x
            dragRightY = guiPos.y
            component.gui.sendNewEvent(
                GuiEvent.DRAG_RIGHT,
                GuiComponentEventDrag(
                    cursorPos.x, cursorPos.y,
                    relativeX = relativeX,
                    relativeY = relativeY
                ), component
            )
        }
        val componentEvent = GuiComponentEvent(cursorPos.x, cursorPos.y)
        if (activeCursor) {
            val new = engine.guiStack.fireEvent(
                componentEvent
            ) { _, _ -> true }
            val old = this.hover
            if (new != old) {
                old?.let { component ->
                    component.gui.sendNewEvent(componentEvent, component) {
                        component.hoverEnd(it)
                    }
                }
                new?.let { component ->
                    component.gui.sendNewEvent(componentEvent, component) {
                        component.hoverBegin(it)
                    }
                }
                this.hover = new
            } else {
                new?.let { component ->
                    component.gui.sendNewEvent(componentEvent, component) {
                        component.hover(it)
                    }
                }
            }
            engine.tooltip.setTooltip(new?.let { Pair(it, cursor) })
        }
    }

    override fun cursors(): Sequence<GuiCursor> {
        return sequenceOf(cursor)
    }

    override fun activeCursor(): Boolean = activeCursor

    private fun handlePress(
        key: ControllerKey,
        repeat: Boolean,
        event: GuiComponentEvent
    ): Boolean =
        when (key) {
            ControllerKey.BUTTON_0 -> {
                val guiPos = cursor.pos
                draggingLeft = engine.guiStack.fireEvent(
                    GuiEvent.PRESS_LEFT, event
                )
                dragLeftX = guiPos.x
                dragLeftY = guiPos.y
                engine.guiStack.fireEvent(
                    GuiEvent.CLICK_LEFT,
                    event
                ) != null
            }
            ControllerKey.BUTTON_1 -> {
                val guiPos = cursor.pos
                draggingRight = engine.guiStack.fireEvent(
                    GuiEvent.PRESS_RIGHT, event
                )
                dragRightX = guiPos.x
                dragRightY = guiPos.y
                engine.guiStack.fireEvent(
                    GuiEvent.CLICK_RIGHT,
                    event
                ) != null
            }
            ControllerKey.KEY_ESCAPE -> {
                if (!repeat) {
                    activeCursor = false
                    engine.guiStack.fireAction(GuiAction.BACK)
                } else false
            }
            ControllerKey.KEY_ENTER -> {
                if (!repeat) {
                    activeCursor = false
                    engine.guiStack.fireAction(GuiAction.ACTIVATE)
                } else false
            }
            ControllerKey.KEY_UP -> {
                activeCursor = false
                engine.guiStack.fireAction(GuiAction.UP)
            }
            ControllerKey.KEY_DOWN -> {
                activeCursor = false
                engine.guiStack.fireAction(GuiAction.DOWN)
            }
            ControllerKey.KEY_LEFT -> {
                activeCursor = false
                engine.guiStack.fireAction(GuiAction.LEFT)
            }
            ControllerKey.KEY_RIGHT -> {
                activeCursor = false
                engine.guiStack.fireAction(GuiAction.RIGHT)
            }
            else -> false
        }

    override fun enabled() {
        super.enabled()
        events.enable()
    }

    override fun disabled() {
        super.disabled()
        events.disable()
    }

    private fun handleRelease(
        key: ControllerKey,
        event: GuiComponentEvent
    ): Boolean {
        when (key) {
            ControllerKey.BUTTON_0 -> draggingLeft?.let { component ->
                component.gui.sendNewEvent(GuiEvent.DROP_LEFT, event, component)
                draggingLeft = null
            }
            ControllerKey.BUTTON_1 -> draggingRight?.let { component ->
                component.gui.sendNewEvent(
                    GuiEvent.DROP_RIGHT, event,
                    component
                )
                draggingRight = null
            }
            else -> return false
        }
        return true
    }

    // TODO: Remove after 0.0.13

    @Deprecated("scrollSensitivity is no longer used")
    constructor(
        engine: ScapesEngine,
        controller: ControllerDesktop,
        scrollSensitivity: Double
    ) : this(engine, controller)
}
