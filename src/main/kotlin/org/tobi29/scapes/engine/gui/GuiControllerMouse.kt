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

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.input.ControllerBasic
import org.tobi29.scapes.engine.input.ControllerDefault
import org.tobi29.scapes.engine.input.ControllerKey
import org.tobi29.scapes.engine.utils.math.vector.Vector2d

class GuiControllerMouse constructor(engine: ScapesEngine, controller: ControllerDefault,
                                     private val scrollSensitivity: Double = 1.0) : GuiControllerDefault(
        engine, controller) {
    private val cursor = GuiCursor()
    private var draggingLeft: GuiComponent? = null
    private var draggingRight: GuiComponent? = null
    private var dragLeftX = 0.0
    private var dragLeftY = 0.0
    private var dragRightX = 0.0
    private var dragRightY = 0.0

    override fun update(delta: Double) {
        val cursorX = controller.x()
        val cursorY = controller.y()
        cursor.set(Vector2d(cursorX, cursorY), Vector2d(cursorX, cursorY))
        draggingLeft?.let { component ->
            val guiPos = cursor.currentGuiPos()
            val relativeX = guiPos.x - dragLeftX
            val relativeY = guiPos.y - dragLeftY
            dragLeftX = guiPos.x
            dragLeftY = guiPos.y
            component.gui.sendNewEvent(GuiEvent.DRAG_LEFT,
                    GuiComponentEvent(cursorX, cursorY, relativeX,
                            relativeY), component)
        }
        draggingRight?.let { component ->
            val guiPos = cursor.currentGuiPos()
            val relativeX = guiPos.x - dragRightX
            val relativeY = guiPos.y - dragRightY
            dragRightX = guiPos.x
            dragRightY = guiPos.y
            component.gui.sendNewEvent(GuiEvent.DRAG_RIGHT,
                    GuiComponentEvent(cursorX, cursorY, relativeX,
                            relativeY), component)
        }
        val scrollX = controller.scrollX() * scrollSensitivity
        val scrollY = controller.scrollY() * scrollSensitivity
        if (scrollX != 0.0 || scrollY != 0.0) {
            engine.guiStack.fireRecursiveEvent(GuiEvent.SCROLL,
                    GuiComponentEvent(cursorX, cursorY, scrollX,
                            scrollY, false))
        }
        val componentEvent = GuiComponentEvent(cursorX, cursorY)
        engine.guiStack.fireEvent(componentEvent,
                GuiComponent::hover)
        controller.pressEvents().forEach { event ->
            when (event.state()) {
                ControllerBasic.PressState.PRESS, ControllerBasic.PressState.REPEAT -> handlePress(
                        event.key(), componentEvent)
                ControllerBasic.PressState.RELEASE -> handleRelease(event.key(),
                        componentEvent)
            }
        }
    }

    override fun cursors(): Sequence<GuiCursor> {
        return sequenceOf(cursor)
    }

    override fun clicks(): Sequence<Pair<GuiCursor, ControllerBasic.PressEvent>> {
        return controller.pressEvents().map { event -> Pair(cursor, event) }
    }

    private fun handlePress(key: ControllerKey,
                            event: GuiComponentEvent) {
        when (key) {
            ControllerKey.BUTTON_0 -> {
                val guiPos = cursor.currentGuiPos()
                draggingLeft = engine.guiStack.fireEvent(GuiEvent.PRESS_LEFT,
                        event)
                dragLeftX = guiPos.x
                dragLeftY = guiPos.y
                if (engine.guiStack.fireEvent(GuiEvent.CLICK_LEFT,
                        event) != null) {
                    return
                }
            }
            ControllerKey.BUTTON_1 -> {
                val guiPos = cursor.currentGuiPos()
                draggingRight = engine.guiStack.fireEvent(GuiEvent.PRESS_RIGHT,
                        event)
                dragRightX = guiPos.x
                dragRightY = guiPos.y
                if (engine.guiStack.fireEvent(GuiEvent.CLICK_RIGHT,
                        event) != null) {
                    return
                }
            }
            ControllerKey.KEY_ESCAPE -> if (engine.guiStack.fireAction(
                    GuiAction.BACK)) {
                return
            }
            ControllerKey.KEY_ENTER -> if (engine.guiStack.fireAction(
                    GuiAction.ACTIVATE)) {
                return
            }
            ControllerKey.KEY_UP -> if (engine.guiStack.fireAction(
                    GuiAction.UP)) {
                return
            }
            ControllerKey.KEY_DOWN -> if (engine.guiStack.fireAction(
                    GuiAction.DOWN)) {
                return
            }
            ControllerKey.KEY_LEFT -> if (engine.guiStack.fireAction(
                    GuiAction.LEFT)) {
                return
            }
            ControllerKey.KEY_RIGHT -> if (engine.guiStack.fireAction(
                    GuiAction.RIGHT)) {
                return
            }
        }
        firePress(key)
    }

    private fun handleRelease(key: ControllerKey,
                              event: GuiComponentEvent) {
        when (key) {
            ControllerKey.BUTTON_0 -> draggingLeft?.let { component ->
                component.gui.sendNewEvent(GuiEvent.DROP_LEFT, event, component)
                draggingLeft = null
            }
            ControllerKey.BUTTON_1 -> draggingRight?.let { component ->
                component.gui.sendNewEvent(GuiEvent.DROP_RIGHT, event,
                        component)
                draggingRight = null
            }
        }
    }
}
