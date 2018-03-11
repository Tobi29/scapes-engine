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

package org.tobi29.scapes.engine.backends.js

import org.tobi29.scapes.engine.input.ControllerAxis
import org.tobi29.scapes.engine.input.ControllerKey

object DOMKeyMap {
    private val KEYS = HashMap<String, ControllerKey>()
    private val STANDARD_GAMEPAD_BUTTONS: Array<ControllerKey?>
    private val STANDARD_GAMEPAD_ANALOG_BUTTONS: Array<Int?>
    private val STANDARD_GAMEPAD_AXES: Array<Int?>

    init {
        KEYS.put("AltLeft", ControllerKey.KEY_ALT_LEFT)
        KEYS.put("AltRight", ControllerKey.KEY_ALT_RIGHT)
        KEYS.put("AltGraph", ControllerKey.KEY_ALT_RIGHT)
        KEYS.put("CapsLock", ControllerKey.KEY_CAPS_LOCK)
        KEYS.put("ControlLeft", ControllerKey.KEY_CONTROL_LEFT)
        KEYS.put("ControlRight", ControllerKey.KEY_CONTROL_RIGHT)
        KEYS.put("Fn", c("FN", "Fn"))
        KEYS.put("FnLock", c("FN_LOCK", "Fn Lock"))
        KEYS.put("Hyper", c("HYPER", "Hyper"))
        KEYS.put("MetaLeft", ControllerKey.KEY_SUPER_LEFT)
        KEYS.put("MetaRight", ControllerKey.KEY_SUPER_RIGHT)
        KEYS.put("NumLock", ControllerKey.KEY_NUM_LOCK)
        KEYS.put("ScrollLock", ControllerKey.KEY_SCROLL_LOCK)
        KEYS.put("ShiftLeft", ControllerKey.KEY_SHIFT_LEFT)
        KEYS.put("ShiftRight", ControllerKey.KEY_SHIFT_RIGHT)
        KEYS.put("SuperLeft", ControllerKey.KEY_SUPER_LEFT)
        KEYS.put("SuperRight", ControllerKey.KEY_SUPER_RIGHT)
        KEYS.put("Symbol", c("SUPER", "Super"))
        KEYS.put("SymbolLock", c("SYMBOL_LOCK", "Symbol Lock"))
        KEYS.put("Enter", ControllerKey.KEY_ENTER)
        KEYS.put("Tab", ControllerKey.KEY_TAB)
        KEYS.put("Space", ControllerKey.KEY_SPACE)
        KEYS.put("ArrowDown", ControllerKey.KEY_DOWN)
        KEYS.put("ArrowLeft", ControllerKey.KEY_LEFT)
        KEYS.put("ArrowRight", ControllerKey.KEY_RIGHT)
        KEYS.put("ArrowUp", ControllerKey.KEY_UP)
        KEYS.put("End", ControllerKey.KEY_END)
        KEYS.put("Home", ControllerKey.KEY_HOME)
        KEYS.put("PageDown", ControllerKey.KEY_PAGE_DOWN)
        KEYS.put("PageUp", ControllerKey.KEY_PAGE_UP)
        KEYS.put("Backspace", ControllerKey.KEY_BACKSPACE)
        KEYS.put("Clear", c("CLEAR", "Clear"))
        KEYS.put("Copy", c("COPY", "Copy"))
        KEYS.put("CrSel", c("CR_SEL", "Cursor Select"))
        KEYS.put("Cut", c("CUT", "Cut"))
        KEYS.put("Delete", ControllerKey.KEY_DELETE)
        KEYS.put("EraseEof", c("ERASE_EOF", "Erase to End of Field"))
        KEYS.put("ExSel", c("EX_SEL", "Extend Selection"))
        KEYS.put("Insert", ControllerKey.KEY_INSERT)
        KEYS.put("Paste", c("PASTE", "Paste"))
        KEYS.put("Redo", c("REDO", "Redo"))
        KEYS.put("Undo", c("UNDO", "Undo"))

        KEYS.put("F1", ControllerKey.KEY_F1)
        KEYS.put("F2", ControllerKey.KEY_F2)
        KEYS.put("F3", ControllerKey.KEY_F3)
        KEYS.put("F4", ControllerKey.KEY_F4)
        KEYS.put("F5", ControllerKey.KEY_F5)
        KEYS.put("F6", ControllerKey.KEY_F6)
        KEYS.put("F7", ControllerKey.KEY_F7)
        KEYS.put("F8", ControllerKey.KEY_F8)
        KEYS.put("F9", ControllerKey.KEY_F9)
        KEYS.put("F10", ControllerKey.KEY_F10)
        KEYS.put("F11", ControllerKey.KEY_F11)
        KEYS.put("F12", ControllerKey.KEY_F12)
        KEYS.put("F13", ControllerKey.KEY_F13)
        KEYS.put("F14", ControllerKey.KEY_F14)
        KEYS.put("F15", ControllerKey.KEY_F15)
        KEYS.put("F16", ControllerKey.KEY_F16)
        KEYS.put("F17", ControllerKey.KEY_F17)
        KEYS.put("F18", ControllerKey.KEY_F18)
        KEYS.put("F19", ControllerKey.KEY_F19)

        KEYS.put("NumpadAdd", ControllerKey.KEY_KP_ADD)
        KEYS.put("NumpadSubtract", ControllerKey.KEY_KP_SUBTRACT)
        KEYS.put("NumpadMultiply", ControllerKey.KEY_KP_MULTIPLY)
        KEYS.put("NumpadDivide", ControllerKey.KEY_KP_DIVIDE)
        KEYS.put("NumpadDecimal", ControllerKey.KEY_KP_DECIMAL)
        KEYS.put("Numpad0", ControllerKey.KEY_KP_0)
        KEYS.put("Numpad1", ControllerKey.KEY_KP_1)
        KEYS.put("Numpad2", ControllerKey.KEY_KP_2)
        KEYS.put("Numpad3", ControllerKey.KEY_KP_3)
        KEYS.put("Numpad4", ControllerKey.KEY_KP_4)
        KEYS.put("Numpad5", ControllerKey.KEY_KP_5)
        KEYS.put("Numpad6", ControllerKey.KEY_KP_6)
        KEYS.put("Numpad7", ControllerKey.KEY_KP_7)
        KEYS.put("Numpad8", ControllerKey.KEY_KP_8)
        KEYS.put("Numpad9", ControllerKey.KEY_KP_9)
        KEYS.put("Digit0", ControllerKey.KEY_0)
        KEYS.put("Digit1", ControllerKey.KEY_1)
        KEYS.put("Digit2", ControllerKey.KEY_2)
        KEYS.put("Digit3", ControllerKey.KEY_3)
        KEYS.put("Digit4", ControllerKey.KEY_4)
        KEYS.put("Digit5", ControllerKey.KEY_5)
        KEYS.put("Digit6", ControllerKey.KEY_6)
        KEYS.put("Digit7", ControllerKey.KEY_7)
        KEYS.put("Digit8", ControllerKey.KEY_8)
        KEYS.put("Digit9", ControllerKey.KEY_9)

        KEYS.put("KeyA", ControllerKey.KEY_A)
        KEYS.put("KeyB", ControllerKey.KEY_B)
        KEYS.put("KeyC", ControllerKey.KEY_C)
        KEYS.put("KeyD", ControllerKey.KEY_D)
        KEYS.put("KeyE", ControllerKey.KEY_E)
        KEYS.put("KeyF", ControllerKey.KEY_F)
        KEYS.put("KeyG", ControllerKey.KEY_G)
        KEYS.put("KeyH", ControllerKey.KEY_H)
        KEYS.put("KeyI", ControllerKey.KEY_I)
        KEYS.put("KeyJ", ControllerKey.KEY_J)
        KEYS.put("KeyK", ControllerKey.KEY_K)
        KEYS.put("KeyL", ControllerKey.KEY_L)
        KEYS.put("KeyM", ControllerKey.KEY_M)
        KEYS.put("KeyN", ControllerKey.KEY_N)
        KEYS.put("KeyO", ControllerKey.KEY_O)
        KEYS.put("KeyP", ControllerKey.KEY_P)
        KEYS.put("KeyQ", ControllerKey.KEY_Q)
        KEYS.put("KeyR", ControllerKey.KEY_R)
        KEYS.put("KeyS", ControllerKey.KEY_S)
        KEYS.put("KeyT", ControllerKey.KEY_T)
        KEYS.put("KeyU", ControllerKey.KEY_U)
        KEYS.put("KeyV", ControllerKey.KEY_V)
        KEYS.put("KeyW", ControllerKey.KEY_W)
        KEYS.put("KeyX", ControllerKey.KEY_X)
        KEYS.put("KeyY", ControllerKey.KEY_Y)
        KEYS.put("KeyZ", ControllerKey.KEY_Z)

        STANDARD_GAMEPAD_BUTTONS = arrayOf(
            /* A  */ ControllerKey.BUTTON_A,
            /* B  */ ControllerKey.BUTTON_B,
            /* X  */ ControllerKey.BUTTON_X,
            /* Y  */ ControllerKey.BUTTON_Y,
            /* L1 */ ControllerKey.BUTTON_BUMPER_LEFT,
            /* R1 */ ControllerKey.BUTTON_BUMPER_RIGHT,
            /* L2 */ null,
            /* R2 */ null,
            /* SE */ ControllerKey.BUTTON_SELECT,
            /* ST */ ControllerKey.BUTTON_START,
            /* LS */ ControllerKey.BUTTON_STICK_LEFT,
            /* LR */ ControllerKey.BUTTON_STICK_RIGHT,
            /* UP */ ControllerKey.BUTTON_DPAD_UP,
            /* DO */ ControllerKey.BUTTON_DPAD_DOWN,
            /* LE */ ControllerKey.BUTTON_DPAD_LEFT,
            /* RI */ ControllerKey.BUTTON_DPAD_RIGHT,
            /* WA */ ControllerKey.BUTTON_WHATEVER
        )

        STANDARD_GAMEPAD_ANALOG_BUTTONS = arrayOf(
            /* A  */ null,
            /* B  */ null,
            /* X  */ null,
            /* Y  */ null,
            /* L1 */ null,
            /* R1 */ null,
            /* L2 */ ControllerAxis.TRIGGER_LEFT,
            /* R2 */ ControllerAxis.TRIGGER_RIGHT,
            /* SE */ null,
            /* ST */ null,
            /* LS */ null,
            /* LR */ null,
            /* UP */ null,
            /* DO */ null,
            /* LE */ null,
            /* RI */ null,
            /* WA */ null
        )

        STANDARD_GAMEPAD_AXES = arrayOf(
            /* LX */ ControllerAxis.X_LEFT,
            /* LY */ ControllerAxis.Y_LEFT,
            /* RX */ ControllerAxis.X_RIGHT,
            /* RY */ ControllerAxis.Y_RIGHT
        )
    }

    private fun c(
        name: String,
        humanName: String
    ) =
        ControllerKey.of("JAVASCRIPT_$name", "$humanName (JS)")

    fun key(id: String) = KEYS[id]

    fun button(id: Short) = ControllerKey.button(id.toInt())

    fun standardGamepad(id: Int) =
        STANDARD_GAMEPAD_BUTTONS.getOrNull(id)

    fun standardGamepadAnalog(id: Int) =
        STANDARD_GAMEPAD_ANALOG_BUTTONS.getOrNull(id)

    fun standardGamepadAxis(id: Int) =
        STANDARD_GAMEPAD_AXES.getOrNull(id)

    fun touch() {}
}
