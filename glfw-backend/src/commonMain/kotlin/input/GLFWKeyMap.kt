/*
 * Copyright 2012-2019 Tobi29
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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.backends.glfw.input

import net.gitout.ktbindings.glfw.*
import org.tobi29.scapes.engine.input.ControllerAxis
import org.tobi29.scapes.engine.input.ControllerKey

private val KEYS =
    arrayOfNulls<ControllerKey>(GLFW_KEY_LAST + 1).apply {
        this[GLFW_KEY_SPACE] = ControllerKey.KEY_SPACE
        this[GLFW_KEY_APOSTROPHE] = ControllerKey.KEY_APOSTROPHE
        this[GLFW_KEY_COMMA] = ControllerKey.KEY_COMMA
        this[GLFW_KEY_MINUS] = ControllerKey.KEY_MINUS
        this[GLFW_KEY_PERIOD] = ControllerKey.KEY_PERIOD
        this[GLFW_KEY_SLASH] = ControllerKey.KEY_SLASH
        this[GLFW_KEY_0] = ControllerKey.KEY_0
        this[GLFW_KEY_1] = ControllerKey.KEY_1
        this[GLFW_KEY_2] = ControllerKey.KEY_2
        this[GLFW_KEY_3] = ControllerKey.KEY_3
        this[GLFW_KEY_4] = ControllerKey.KEY_4
        this[GLFW_KEY_5] = ControllerKey.KEY_5
        this[GLFW_KEY_6] = ControllerKey.KEY_6
        this[GLFW_KEY_7] = ControllerKey.KEY_7
        this[GLFW_KEY_8] = ControllerKey.KEY_8
        this[GLFW_KEY_9] = ControllerKey.KEY_9
        this[GLFW_KEY_SEMICOLON] = ControllerKey.KEY_SEMICOLON
        this[GLFW_KEY_EQUAL] = ControllerKey.KEY_EQUAL
        this[GLFW_KEY_A] = ControllerKey.KEY_A
        this[GLFW_KEY_B] = ControllerKey.KEY_B
        this[GLFW_KEY_C] = ControllerKey.KEY_C
        this[GLFW_KEY_D] = ControllerKey.KEY_D
        this[GLFW_KEY_E] = ControllerKey.KEY_E
        this[GLFW_KEY_F] = ControllerKey.KEY_F
        this[GLFW_KEY_G] = ControllerKey.KEY_G
        this[GLFW_KEY_H] = ControllerKey.KEY_H
        this[GLFW_KEY_I] = ControllerKey.KEY_I
        this[GLFW_KEY_J] = ControllerKey.KEY_J
        this[GLFW_KEY_K] = ControllerKey.KEY_K
        this[GLFW_KEY_L] = ControllerKey.KEY_L
        this[GLFW_KEY_M] = ControllerKey.KEY_M
        this[GLFW_KEY_N] = ControllerKey.KEY_N
        this[GLFW_KEY_O] = ControllerKey.KEY_O
        this[GLFW_KEY_P] = ControllerKey.KEY_P
        this[GLFW_KEY_Q] = ControllerKey.KEY_Q
        this[GLFW_KEY_R] = ControllerKey.KEY_R
        this[GLFW_KEY_S] = ControllerKey.KEY_S
        this[GLFW_KEY_T] = ControllerKey.KEY_T
        this[GLFW_KEY_U] = ControllerKey.KEY_U
        this[GLFW_KEY_V] = ControllerKey.KEY_V
        this[GLFW_KEY_W] = ControllerKey.KEY_W
        this[GLFW_KEY_X] = ControllerKey.KEY_X
        this[GLFW_KEY_Y] = ControllerKey.KEY_Y
        this[GLFW_KEY_Z] = ControllerKey.KEY_Z
        this[GLFW_KEY_LEFT_BRACKET] = ControllerKey.KEY_BRACKET_LEFT
        this[GLFW_KEY_BACKSLASH] = ControllerKey.KEY_BACKSLASH
        this[GLFW_KEY_RIGHT_BRACKET] = ControllerKey.KEY_BRACKET_RIGHT
        this[GLFW_KEY_GRAVE_ACCENT] = ControllerKey.KEY_GRAVE_ACCENT
        this[GLFW_KEY_WORLD_1] = c("KEY_WORLD_1", "World 1")
        this[GLFW_KEY_WORLD_2] = c("KEY_WORLD_2", "World 2")
        this[GLFW_KEY_ESCAPE] = ControllerKey.KEY_ESCAPE
        this[GLFW_KEY_ENTER] = ControllerKey.KEY_ENTER
        this[GLFW_KEY_TAB] = ControllerKey.KEY_TAB
        this[GLFW_KEY_BACKSPACE] = ControllerKey.KEY_BACKSPACE
        this[GLFW_KEY_INSERT] = ControllerKey.KEY_INSERT
        this[GLFW_KEY_DELETE] = ControllerKey.KEY_DELETE
        this[GLFW_KEY_RIGHT] = ControllerKey.KEY_RIGHT
        this[GLFW_KEY_LEFT] = ControllerKey.KEY_LEFT
        this[GLFW_KEY_DOWN] = ControllerKey.KEY_DOWN
        this[GLFW_KEY_UP] = ControllerKey.KEY_UP
        this[GLFW_KEY_PAGE_UP] = ControllerKey.KEY_PAGE_UP
        this[GLFW_KEY_PAGE_DOWN] = ControllerKey.KEY_PAGE_DOWN
        this[GLFW_KEY_HOME] = ControllerKey.KEY_HOME
        this[GLFW_KEY_END] = ControllerKey.KEY_END
        this[GLFW_KEY_CAPS_LOCK] = ControllerKey.KEY_CAPS_LOCK
        this[GLFW_KEY_SCROLL_LOCK] = ControllerKey.KEY_SCROLL_LOCK
        this[GLFW_KEY_NUM_LOCK] = ControllerKey.KEY_NUM_LOCK
        this[GLFW_KEY_PRINT_SCREEN] = ControllerKey.KEY_PRINT_SCREEN
        this[GLFW_KEY_PAUSE] = ControllerKey.KEY_PAUSE
        this[GLFW_KEY_F1] = ControllerKey.KEY_F1
        this[GLFW_KEY_F2] = ControllerKey.KEY_F2
        this[GLFW_KEY_F3] = ControllerKey.KEY_F3
        this[GLFW_KEY_F4] = ControllerKey.KEY_F4
        this[GLFW_KEY_F5] = ControllerKey.KEY_F5
        this[GLFW_KEY_F6] = ControllerKey.KEY_F6
        this[GLFW_KEY_F7] = ControllerKey.KEY_F7
        this[GLFW_KEY_F8] = ControllerKey.KEY_F8
        this[GLFW_KEY_F9] = ControllerKey.KEY_F9
        this[GLFW_KEY_F10] = ControllerKey.KEY_F10
        this[GLFW_KEY_F11] = ControllerKey.KEY_F11
        this[GLFW_KEY_F12] = ControllerKey.KEY_F12
        this[GLFW_KEY_F13] = ControllerKey.KEY_F13
        this[GLFW_KEY_F14] = ControllerKey.KEY_F14
        this[GLFW_KEY_F15] = ControllerKey.KEY_F15
        this[GLFW_KEY_F16] = ControllerKey.KEY_F16
        this[GLFW_KEY_F17] = ControllerKey.KEY_F17
        this[GLFW_KEY_F18] = ControllerKey.KEY_F18
        this[GLFW_KEY_F19] = ControllerKey.KEY_F19
        this[GLFW_KEY_F20] = ControllerKey.KEY_F20
        this[GLFW_KEY_F21] = ControllerKey.KEY_F21
        this[GLFW_KEY_F22] = ControllerKey.KEY_F22
        this[GLFW_KEY_F23] = ControllerKey.KEY_F23
        this[GLFW_KEY_F24] = ControllerKey.KEY_F24
        this[GLFW_KEY_F25] = ControllerKey.KEY_F25
        this[GLFW_KEY_KP_0] = ControllerKey.KEY_KP_0
        this[GLFW_KEY_KP_1] = ControllerKey.KEY_KP_1
        this[GLFW_KEY_KP_2] = ControllerKey.KEY_KP_2
        this[GLFW_KEY_KP_3] = ControllerKey.KEY_KP_3
        this[GLFW_KEY_KP_4] = ControllerKey.KEY_KP_4
        this[GLFW_KEY_KP_5] = ControllerKey.KEY_KP_5
        this[GLFW_KEY_KP_6] = ControllerKey.KEY_KP_6
        this[GLFW_KEY_KP_7] = ControllerKey.KEY_KP_7
        this[GLFW_KEY_KP_8] = ControllerKey.KEY_KP_8
        this[GLFW_KEY_KP_9] = ControllerKey.KEY_KP_9
        this[GLFW_KEY_KP_DECIMAL] = ControllerKey.KEY_KP_DECIMAL
        this[GLFW_KEY_KP_DIVIDE] = ControllerKey.KEY_KP_DIVIDE
        this[GLFW_KEY_KP_MULTIPLY] = ControllerKey.KEY_KP_MULTIPLY
        this[GLFW_KEY_KP_SUBTRACT] = ControllerKey.KEY_KP_SUBTRACT
        this[GLFW_KEY_KP_ADD] = ControllerKey.KEY_KP_ADD
        this[GLFW_KEY_KP_ENTER] = ControllerKey.KEY_KP_ENTER
        this[GLFW_KEY_KP_EQUAL] = ControllerKey.KEY_KP_EQUAL
        this[GLFW_KEY_LEFT_SHIFT] = ControllerKey.KEY_SHIFT_LEFT
        this[GLFW_KEY_LEFT_CONTROL] = ControllerKey.KEY_CONTROL_LEFT
        this[GLFW_KEY_LEFT_ALT] = ControllerKey.KEY_ALT_LEFT
        this[GLFW_KEY_LEFT_SUPER] = ControllerKey.KEY_SUPER_LEFT
        this[GLFW_KEY_RIGHT_SHIFT] = ControllerKey.KEY_SHIFT_RIGHT
        this[GLFW_KEY_RIGHT_CONTROL] = ControllerKey.KEY_CONTROL_RIGHT
        this[GLFW_KEY_RIGHT_ALT] = ControllerKey.KEY_ALT_RIGHT
        this[GLFW_KEY_RIGHT_SUPER] = ControllerKey.KEY_SUPER_RIGHT
        this[GLFW_KEY_MENU] = ControllerKey.KEY_MENU
    }
private val GAMEPAD_AXES =
    IntArray(GLFW_GAMEPAD_AXIS_LAST + 1) { -1 }.apply {
        this[GLFW_GAMEPAD_AXIS_LEFT_X] = ControllerAxis.X_LEFT
        this[GLFW_GAMEPAD_AXIS_LEFT_Y] = ControllerAxis.Y_LEFT
        this[GLFW_GAMEPAD_AXIS_RIGHT_X] = ControllerAxis.X_RIGHT
        this[GLFW_GAMEPAD_AXIS_RIGHT_Y] = ControllerAxis.Y_RIGHT
        this[GLFW_GAMEPAD_AXIS_LEFT_TRIGGER] = ControllerAxis.TRIGGER_LEFT
        this[GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER] = ControllerAxis.TRIGGER_RIGHT
    }
private val GAMEPAD_BUTTONS =
    arrayOfNulls<ControllerKey>(GLFW_GAMEPAD_BUTTON_LAST + 1).apply {
        this[GLFW_GAMEPAD_BUTTON_A] = ControllerKey.BUTTON_A
        this[GLFW_GAMEPAD_BUTTON_B] = ControllerKey.BUTTON_B
        this[GLFW_GAMEPAD_BUTTON_X] = ControllerKey.BUTTON_X
        this[GLFW_GAMEPAD_BUTTON_Y] = ControllerKey.BUTTON_Y
        this[GLFW_GAMEPAD_BUTTON_LEFT_BUMPER] = ControllerKey.BUTTON_BUMPER_LEFT
        this[GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER] =
                ControllerKey.BUTTON_BUMPER_RIGHT
        this[GLFW_GAMEPAD_BUTTON_BACK] = ControllerKey.BUTTON_BACK
        this[GLFW_GAMEPAD_BUTTON_START] = ControllerKey.BUTTON_START
        this[GLFW_GAMEPAD_BUTTON_GUIDE] = ControllerKey.BUTTON_GUIDE
        this[GLFW_GAMEPAD_BUTTON_LEFT_THUMB] = ControllerKey.BUTTON_THUMB_LEFT
        this[GLFW_GAMEPAD_BUTTON_RIGHT_THUMB] = ControllerKey.BUTTON_THUMB_RIGHT
        this[GLFW_GAMEPAD_BUTTON_DPAD_UP] = ControllerKey.BUTTON_DPAD_UP
        this[GLFW_GAMEPAD_BUTTON_DPAD_RIGHT] = ControllerKey.BUTTON_DPAD_RIGHT
        this[GLFW_GAMEPAD_BUTTON_DPAD_DOWN] = ControllerKey.BUTTON_DPAD_DOWN
        this[GLFW_GAMEPAD_BUTTON_DPAD_LEFT] = ControllerKey.BUTTON_DPAD_LEFT
    }

fun glfwKey(id: Int) = KEYS.getOrNull(id)

fun glfwGamepadAxis(id: Int) = GAMEPAD_AXES.getOrElse(id) { -1 }

fun glfwGamepadButton(id: Int) = GAMEPAD_BUTTONS.getOrNull(id)

private inline fun c(name: String, humanName: String) =
    ControllerKey.of("GLFW_$name", "$humanName (GLFW)")