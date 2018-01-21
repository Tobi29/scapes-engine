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
import org.tobi29.scapes.engine.input.*
import org.tobi29.utils.EventDispatcher
import org.tobi29.utils.listenAlive

class GuiControllerGamepad(
        engine: ScapesEngine,
        private val controller: ControllerGamepad,
        private val primaryButton: ControllerKeyReference?,
        private val secondaryButton: ControllerKeyReference?,
        private val upButton: ControllerKeyReference?,
        private val downButton: ControllerKeyReference?,
        private val leftButton: ControllerKeyReference?,
        private val rightButton: ControllerKeyReference?
) : GuiController(engine) {
    private val events = EventDispatcher(engine.events) {
        listenAlive<ControllerButtons.PressEvent> { event ->
            if (when (event.action) {
                ControllerButtons.Action.PRESS, ControllerButtons.Action.REPEAT ->
                    handlePress(event.key)
                ControllerButtons.Action.RELEASE -> false
            }) event.muted = true
        }
    }

    override fun focusTextField(valid: () -> Boolean,
                                data: TextFieldData,
                                multiline: Boolean) {
        // TODO: Implement keyboard for gamepads
    }

    override fun captureCursor() = true

    override fun enabled() {
        super.enabled()
        events.enable()
    }

    override fun disabled() {
        super.disabled()
        events.disable()
    }

    private fun handlePress(key: ControllerKey): Boolean {
        if (primaryButton.isPressed(key,
                controller) && engine.guiStack.fireAction(GuiAction.ACTIVATE)) {
            return true
        }
        if (secondaryButton.isPressed(key,
                controller) && engine.guiStack.fireAction(GuiAction.BACK)) {
            return true
        }
        if (upButton.isPressed(key, controller) && engine.guiStack.fireAction(
                GuiAction.UP)) {
            return true
        }
        if (downButton.isPressed(key, controller) && engine.guiStack.fireAction(
                GuiAction.DOWN)) {
            return true
        }
        if (leftButton.isPressed(key, controller) && engine.guiStack.fireAction(
                GuiAction.LEFT)) {
            return true
        }
        if (rightButton.isPressed(key,
                controller) && engine.guiStack.fireAction(GuiAction.RIGHT)) {
            return true
        }
        return false
    }
}
