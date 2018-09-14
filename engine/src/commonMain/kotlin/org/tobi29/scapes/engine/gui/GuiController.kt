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
import org.tobi29.stdex.atomic.AtomicBoolean
import org.tobi29.utils.MutableString

abstract class GuiController(protected val engine: ScapesEngine) {
    open fun update(delta: Double) {}

    abstract fun focusTextField(valid: () -> Boolean,
                                data: TextFieldData,
                                multiline: Boolean)

    open fun cursors(): Sequence<GuiCursor> = emptySequence()

    open fun captureCursor(): Boolean = false

    open fun activeCursor(): Boolean = false

    open fun enabled() {}

    open fun disabled() {}

    class TextFieldData {
        val dirty = AtomicBoolean(false)
        var text = MutableString(100)
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
