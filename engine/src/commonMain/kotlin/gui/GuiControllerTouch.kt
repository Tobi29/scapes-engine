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

import org.tobi29.math.vector.MutableVector2d
import org.tobi29.math.vector.Vector2d
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.input.ControllerTouch
import org.tobi29.scapes.engine.input.ControllerTracker
import org.tobi29.scapes.engine.input.ScrollDelta
import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.utils.steadyClock

class GuiControllerTouch(
    engine: ScapesEngine,
    private val controller: ControllerTouch
) : GuiController(engine) {
    private val fingers = ConcurrentHashMap<ControllerTracker.Tracker, Finger>()

    override fun update(delta: Double) {
        fingers.values.forEach { it.alive = false }
        controller.fingers().forEach { tracker ->
            var fetch: Finger? = fingers[tracker]
            if (fetch == null) {
                fetch = Finger(tracker.pos)
                val finger = fetch
                handleFinger(finger)
                val guiPos = finger.cursor.pos
                finger.dragX = guiPos.x
                finger.dragY = guiPos.y
                engine.guiStack.fireEvent(GuiComponentEvent(guiPos.x, guiPos.y),
                    { _, _ -> true })?.let { component ->
                    if (!fingers.values.any { it.dragging == component }) {
                        finger.dragging = component
                        component.gui.sendNewEvent(
                            GuiEvent.PRESS_LEFT,
                            GuiComponentEvent(guiPos.x, guiPos.y),
                            component
                        )
                    }
                }
                fingers.put(tracker, finger)
            } else {
                handleFinger(fetch)
            }
        }
        fingers.asSequence()
            .filter { !it.value.alive }
            .forEach { (tracker, _) ->
                fingers.remove(tracker)?.let { finger ->
                    finger.dragging?.let { component ->
                        val guiPos = finger.cursor.pos
                        if (!finger.clicked) {
                            finger.clicked = true
                            component.gui.sendNewEvent(
                                GuiEvent.CLICK_LEFT,
                                GuiComponentEvent(guiPos.x, guiPos.y),
                                component
                            )
                        }
                        component.gui.sendNewEvent(
                            GuiEvent.DROP_LEFT,
                            GuiComponentEvent(guiPos.x, guiPos.y), component
                        )
                    }
                }
            }
    }

    override fun focusTextField(
        valid: () -> Boolean,
        data: TextFieldData,
        multiline: Boolean
    ) {
        engine.container.dialog("Input", data, multiline)
    }

    override fun cursors(): Sequence<GuiCursor> {
        return fingers.values.asSequence().map { it.cursor }
    }

    override fun activeCursor() = false

    private fun handleFinger(finger: Finger) {
        finger.alive = true
        finger.cursor.pos = finger.tracker.now()
        finger.dragging?.let { component ->
            val guiPos = finger.cursor.pos
            val relativeX = guiPos.x - finger.dragX
            val relativeY = guiPos.y - finger.dragY
            finger.dragX = guiPos.x
            finger.dragY = guiPos.y
            component.gui.sendNewEvent(
                GuiEvent.DRAG_LEFT,
                GuiComponentEventDrag(
                    guiPos.x, guiPos.y,
                    relativeX = relativeX, relativeY = relativeY
                ),
                component
            )
            val source = finger.source
            engine.guiStack.fireRecursiveEvent(
                GuiEvent.SCROLL,
                GuiComponentEventScroll(
                    source.x, source.y,
                    delta = ScrollDelta.Pixel(
                        Vector2d(relativeX, relativeY)
                    )
                )
            )
            if (steadyClock.timeSteadyNanos() - finger.start >= 250000000L && !finger.clicked) {
                finger.clicked = true
                finger.dragging?.let {
                    it.gui.sendNewEvent(
                        GuiEvent.CLICK_RIGHT,
                        GuiComponentEvent(guiPos.x, guiPos.y), it
                    )
                }
            }
        }
    }

    private class Finger(val tracker: MutableVector2d) {
        var source = tracker.now()
        var start = steadyClock.timeSteadyNanos()
        var cursor = GuiCursor()
        var dragging: GuiComponent? = null
        var dragX = 0.0
        var dragY = 0.0
        var clicked = false
        var alive = false
    }
}
