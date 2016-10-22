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

import java8.util.stream.Stream
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.input.ControllerBasic
import org.tobi29.scapes.engine.input.ControllerKey
import org.tobi29.scapes.engine.utils.EventDispatcher

abstract class GuiController(protected val engine: ScapesEngine) {
    val events = EventDispatcher(engine.events)

    abstract fun update(delta: Double)

    abstract fun focusTextField(data: TextFieldData,
                                multiline: Boolean)

    abstract fun processTextField(data: TextFieldData,
                                  multiline: Boolean): Boolean

    abstract fun cursors(): Stream<GuiCursor>

    abstract fun clicks(): Stream<Pair<GuiCursor, ControllerBasic.PressEvent>>

    abstract fun captureCursor(): Boolean

    protected fun firePress(key: ControllerKey): Boolean {
        val event = PressEvent(this, key)
        events.fire(event)
        return !event.muted
    }

    class TextFieldData {
        var text = StringBuilder(100)
        var cursor = 0
        var selectionStart = -1
        var selectionEnd = 0

        fun selectAll() {
            cursor = text.length
            selectionStart = 0
            selectionEnd = cursor
        }

        fun copy(): String? {
            if (selectionStart >= 0) {
                return text.substring(selectionStart, selectionEnd)
            }
            return null
        }

        fun cut(): String? {
            if (selectionStart >= 0) {
                val cut = text.substring(selectionStart, selectionEnd)
                text.delete(selectionStart, selectionEnd)
                cursor = selectionStart
                selectionStart = -1
                return cut
            }
            return null
        }

        fun paste(paste: String) {
            deleteSelection()
            text.insert(cursor, paste)
            cursor += paste.length
        }

        fun deleteSelection() {
            if (selectionStart >= 0) {
                text.delete(selectionStart, selectionEnd)
                cursor = selectionStart
                selectionStart = -1
            }
        }

        fun left(shift: Boolean) {
            if (cursor > 0) {
                if (shift) {
                    if (selectionStart == -1) {
                        selectionStart = cursor - 1
                        selectionEnd = cursor
                    } else if (cursor > selectionStart) {
                        selectionEnd = cursor - 1
                    } else {
                        selectionStart = cursor - 1
                    }
                }
                cursor--
            }
            if (!shift && selectionStart >= 0 || selectionStart == selectionEnd) {
                selectionStart = -1
            }
        }

        fun right(shift: Boolean) {
            if (cursor < text.length) {
                if (shift) {
                    if (selectionStart == -1) {
                        selectionStart = cursor
                        selectionEnd = cursor + 1
                    } else if (cursor < selectionEnd) {
                        selectionStart = cursor + 1
                    } else {
                        selectionEnd = cursor + 1
                    }
                }
                cursor++
            }
            if (!shift && selectionStart >= 0 || selectionStart == selectionEnd) {
                selectionStart = -1
            }
        }

        fun home(shift: Boolean) {
            if (shift) {
                if (selectionStart == -1) {
                    selectionEnd = cursor
                } else if (cursor > selectionStart) {
                    selectionEnd = selectionStart
                }
                selectionStart = 0
            }
            cursor = 0
            if (!shift && selectionStart >= 0 || selectionStart == selectionEnd) {
                selectionStart = -1
            }
        }

        fun end(shift: Boolean) {
            if (shift) {
                if (selectionStart == -1) {
                    selectionStart = cursor
                } else if (cursor < selectionEnd) {
                    selectionStart = selectionEnd
                }
                selectionEnd = text.length
            }
            cursor = text.length
            if (!shift && selectionStart >= 0 || selectionStart == selectionEnd) {
                selectionStart = -1
            }
        }

        fun insert(character: Char) {
            deleteSelection()
            text.insert(cursor++, character)
        }
    }
}

class PressEvent(val controller: GuiController,
                 val key: ControllerKey) {
    var muted = false
}
