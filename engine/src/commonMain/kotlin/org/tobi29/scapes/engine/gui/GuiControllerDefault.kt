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

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.input.*
import org.tobi29.stdex.atomic.AtomicReference
import org.tobi29.stdex.isISOControl
import org.tobi29.utils.EventDispatcher
import org.tobi29.utils.listenAlive

abstract class GuiControllerDefault(
    engine: ScapesEngine,
    protected val controller: ControllerDesktop
) : GuiController(engine) {
    private val currentTextField =
        AtomicReference<Triple<() -> Boolean, TextFieldData, Boolean>?>(
            null
        )

    private val events = EventDispatcher(engine.events) {
        listenAlive<ControllerButtons.PressEvent>(100, {
            it.state.controller == controller
                    && it.action != ControllerButtons.Action.RELEASE
        }) { event ->
            val current = currentTextField.get() ?: return@listenAlive
            val (valid, data, multiline) = current
            if (!valid()) {
                currentTextField.compareAndSet(current, null)
                return@listenAlive
            }
            val container = engine.container
            val shift = event.state.isDown(ControllerKey.KEY_SHIFT_LEFT) ||
                    event.state.isDown(ControllerKey.KEY_SHIFT_RIGHT)
            synchronized(data) {
                if (event.state is ControllerDesktopState
                    && event.state.isModifierDown) {
                    when (event.key) {
                        ControllerKey.KEY_A -> data.selectAll()
                        ControllerKey.KEY_C -> data.copy()?.let {
                            container.clipboardCopy(it)
                        }
                        ControllerKey.KEY_X -> data.cut()?.let {
                            container.clipboardCopy(it)
                        }
                        ControllerKey.KEY_V -> {
                            container.clipboardPaste { paste ->
                                synchronized(data) {
                                    data.paste(
                                        if (multiline) paste
                                        else paste.replace("\n", "")
                                    )
                                    data.dirty.set(true)
                                }
                            }
                        }
                        else -> return@listenAlive
                    }
                } else {
                    when (event.key) {
                        ControllerKey.KEY_LEFT -> data.left(shift)
                        ControllerKey.KEY_RIGHT -> data.right(shift)
                        ControllerKey.KEY_HOME -> data.home(shift)
                        ControllerKey.KEY_END -> data.end(shift)
                        ControllerKey.KEY_ENTER -> if (multiline) {
                            data.insert('\n')
                        } else return@listenAlive
                        ControllerKey.KEY_BACKSPACE -> if (data.selectionStart >= 0) {
                            data.deleteSelection()
                        } else {
                            if (data.cursor > 0) {
                                data.text.delete(data.cursor - 1)
                                data.cursor--
                            }
                        }
                        ControllerKey.KEY_DELETE -> if (data.selectionStart >= 0) {
                            data.deleteSelection()
                        } else {
                            if (data.cursor < data.text.length) {
                                data.text.delete(data.cursor)
                            }
                        }
                        else -> return@listenAlive
                    }
                }
                data.dirty.set(true)
            }
            event.muted = true
        }
        listenAlive<ControllerKeyboard.TypeEvent>(100) { event ->
            if (event.controller.isModifierDown) return@listenAlive
            val current = currentTextField.get() ?: return@listenAlive
            val (valid, data, multiline) = current
            if (!valid()) {
                currentTextField.compareAndSet(current, null)
                return@listenAlive
            }
            synchronized(data) {
                val character = event.character
                if (!character.isISOControl()) {
                    data.insert(character)
                    data.dirty.set(true)
                } else return@listenAlive
            }
            event.muted = true
        }
    }

    override fun focusTextField(
        valid: () -> Boolean,
        data: TextFieldData,
        multiline: Boolean
    ) {
        currentTextField.set(Triple(valid, data, multiline))
    }

    override fun enabled() {
        super.enabled()
        events.enable()
    }

    override fun disabled() {
        super.disabled()
        events.disable()
    }
}
