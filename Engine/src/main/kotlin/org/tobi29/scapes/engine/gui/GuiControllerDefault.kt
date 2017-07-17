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
import org.tobi29.scapes.engine.input.ControllerBasic
import org.tobi29.scapes.engine.input.ControllerDefault
import org.tobi29.scapes.engine.input.ControllerKey
import org.tobi29.scapes.engine.utils.AtomicBoolean

abstract class GuiControllerDefault protected constructor(engine: ScapesEngine,
                                                          protected val controller: ControllerDefault) : GuiController(
        engine) {

    override fun focusTextField(data: GuiController.TextFieldData,
                                multiline: Boolean) {
    }

    override fun processTextField(data: GuiController.TextFieldData,
                                  multiline: Boolean): Boolean {
        val changed = AtomicBoolean(false)
        val shift = controller.isDown(ControllerKey.KEY_SHIFT_LEFT) ||
                controller.isDown(ControllerKey.KEY_SHIFT_RIGHT)
        if (controller.isModifierDown) {
            val container = engine.container
            controller.pressEvents().filter { event -> event.state !== ControllerBasic.PressState.RELEASE }.forEach { event ->
                when (event.key) {
                    ControllerKey.KEY_A -> data.selectAll()
                    ControllerKey.KEY_C -> data.copy()?.let {
                        container.clipboardCopy(it)
                    }
                    ControllerKey.KEY_X -> data.cut()?.let {
                        container.clipboardCopy(it)
                    }
                    ControllerKey.KEY_V -> {
                        var paste = container.clipboardPaste()
                        if (!multiline) {
                            paste = paste.replace("\n", "")
                        }
                        data.paste(paste)
                    }
                }
                changed.set(true)
            }
        } else {
            controller.typeEvents().forEach { event ->
                val character = event.character()
                if (!character.isISOControl()) {
                    data.insert(character)
                    changed.set(true)
                }
            }
            controller.pressEvents().filter { event -> event.state !== ControllerBasic.PressState.RELEASE }.forEach { event ->
                when (event.key) {
                    ControllerKey.KEY_LEFT -> data.left(shift)
                    ControllerKey.KEY_RIGHT -> data.right(shift)
                    ControllerKey.KEY_HOME -> data.home(shift)
                    ControllerKey.KEY_END -> data.end(shift)
                    ControllerKey.KEY_ENTER -> if (multiline) {
                        data.insert('\n')
                    }
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
                }
                changed.set(true)
            }
        }
        if (changed.get()) {
            if (data.selectionStart == data.selectionEnd) {
                data.selectionStart = -1
            }
            return true
        }
        return false
    }

    override fun captureCursor(): Boolean {
        return false
    }

    companion object {
        fun Char.isISOControl() = toInt().isISOControl()
        fun Int.isISOControl() = this <= 0x9F && (this >= 0x7F || (this.ushr(
                5) == 0))
    }
}
