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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.backends.glfw

import org.lwjgl.glfw.GLFW
import org.lwjgl.system.MemoryUtil
import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.FloatsRO
import org.tobi29.io.viewSliceE
import org.tobi29.scapes.engine.backends.lwjgl3.stackFrame

actual inline val GLFW_VERSION_MAJOR get() = GLFW.GLFW_VERSION_MAJOR
actual inline val GLFW_VERSION_MINOR get() = GLFW.GLFW_VERSION_MINOR
actual inline val GLFW_VERSION_REVISION get() = GLFW.GLFW_VERSION_REVISION
actual inline val GLFW_TRUE get() = GLFW.GLFW_TRUE
actual inline val GLFW_FALSE get() = GLFW.GLFW_FALSE
actual inline val GLFW_RELEASE get() = GLFW.GLFW_RELEASE
actual inline val GLFW_PRESS get() = GLFW.GLFW_PRESS
actual inline val GLFW_REPEAT get() = GLFW.GLFW_REPEAT
actual inline val GLFW_HAT_CENTERED get() = GLFW.GLFW_HAT_CENTERED
actual inline val GLFW_HAT_UP get() = GLFW.GLFW_HAT_UP
actual inline val GLFW_HAT_RIGHT get() = GLFW.GLFW_HAT_RIGHT
actual inline val GLFW_HAT_DOWN get() = GLFW.GLFW_HAT_DOWN
actual inline val GLFW_HAT_LEFT get() = GLFW.GLFW_HAT_LEFT
actual inline val GLFW_HAT_RIGHT_UP get() = GLFW.GLFW_HAT_RIGHT_UP
actual inline val GLFW_HAT_RIGHT_DOWN get() = GLFW.GLFW_HAT_RIGHT_DOWN
actual inline val GLFW_HAT_LEFT_UP get() = GLFW.GLFW_HAT_LEFT_UP
actual inline val GLFW_HAT_LEFT_DOWN get() = GLFW.GLFW_HAT_LEFT_DOWN
actual inline val GLFW_KEY_UNKNOWN get() = GLFW.GLFW_KEY_UNKNOWN
actual inline val GLFW_KEY_SPACE get() = GLFW.GLFW_KEY_SPACE
actual inline val GLFW_KEY_APOSTROPHE get() = GLFW.GLFW_KEY_APOSTROPHE
actual inline val GLFW_KEY_COMMA get() = GLFW.GLFW_KEY_COMMA
actual inline val GLFW_KEY_MINUS get() = GLFW.GLFW_KEY_MINUS
actual inline val GLFW_KEY_PERIOD get() = GLFW.GLFW_KEY_PERIOD
actual inline val GLFW_KEY_SLASH get() = GLFW.GLFW_KEY_SLASH
actual inline val GLFW_KEY_0 get() = GLFW.GLFW_KEY_0
actual inline val GLFW_KEY_1 get() = GLFW.GLFW_KEY_1
actual inline val GLFW_KEY_2 get() = GLFW.GLFW_KEY_2
actual inline val GLFW_KEY_3 get() = GLFW.GLFW_KEY_3
actual inline val GLFW_KEY_4 get() = GLFW.GLFW_KEY_4
actual inline val GLFW_KEY_5 get() = GLFW.GLFW_KEY_5
actual inline val GLFW_KEY_6 get() = GLFW.GLFW_KEY_6
actual inline val GLFW_KEY_7 get() = GLFW.GLFW_KEY_7
actual inline val GLFW_KEY_8 get() = GLFW.GLFW_KEY_8
actual inline val GLFW_KEY_9 get() = GLFW.GLFW_KEY_9
actual inline val GLFW_KEY_SEMICOLON get() = GLFW.GLFW_KEY_SEMICOLON
actual inline val GLFW_KEY_EQUAL get() = GLFW.GLFW_KEY_EQUAL
actual inline val GLFW_KEY_A get() = GLFW.GLFW_KEY_A
actual inline val GLFW_KEY_B get() = GLFW.GLFW_KEY_B
actual inline val GLFW_KEY_C get() = GLFW.GLFW_KEY_C
actual inline val GLFW_KEY_D get() = GLFW.GLFW_KEY_D
actual inline val GLFW_KEY_E get() = GLFW.GLFW_KEY_E
actual inline val GLFW_KEY_F get() = GLFW.GLFW_KEY_F
actual inline val GLFW_KEY_G get() = GLFW.GLFW_KEY_G
actual inline val GLFW_KEY_H get() = GLFW.GLFW_KEY_H
actual inline val GLFW_KEY_I get() = GLFW.GLFW_KEY_I
actual inline val GLFW_KEY_J get() = GLFW.GLFW_KEY_J
actual inline val GLFW_KEY_K get() = GLFW.GLFW_KEY_K
actual inline val GLFW_KEY_L get() = GLFW.GLFW_KEY_L
actual inline val GLFW_KEY_M get() = GLFW.GLFW_KEY_M
actual inline val GLFW_KEY_N get() = GLFW.GLFW_KEY_N
actual inline val GLFW_KEY_O get() = GLFW.GLFW_KEY_O
actual inline val GLFW_KEY_P get() = GLFW.GLFW_KEY_P
actual inline val GLFW_KEY_Q get() = GLFW.GLFW_KEY_Q
actual inline val GLFW_KEY_R get() = GLFW.GLFW_KEY_R
actual inline val GLFW_KEY_S get() = GLFW.GLFW_KEY_S
actual inline val GLFW_KEY_T get() = GLFW.GLFW_KEY_T
actual inline val GLFW_KEY_U get() = GLFW.GLFW_KEY_U
actual inline val GLFW_KEY_V get() = GLFW.GLFW_KEY_V
actual inline val GLFW_KEY_W get() = GLFW.GLFW_KEY_W
actual inline val GLFW_KEY_X get() = GLFW.GLFW_KEY_X
actual inline val GLFW_KEY_Y get() = GLFW.GLFW_KEY_Y
actual inline val GLFW_KEY_Z get() = GLFW.GLFW_KEY_Z
actual inline val GLFW_KEY_LEFT_BRACKET get() = GLFW.GLFW_KEY_LEFT_BRACKET
actual inline val GLFW_KEY_BACKSLASH get() = GLFW.GLFW_KEY_BACKSLASH
actual inline val GLFW_KEY_RIGHT_BRACKET get() = GLFW.GLFW_KEY_RIGHT_BRACKET
actual inline val GLFW_KEY_GRAVE_ACCENT get() = GLFW.GLFW_KEY_GRAVE_ACCENT
actual inline val GLFW_KEY_WORLD_1 get() = GLFW.GLFW_KEY_WORLD_1
actual inline val GLFW_KEY_WORLD_2 get() = GLFW.GLFW_KEY_WORLD_2
actual inline val GLFW_KEY_ESCAPE get() = GLFW.GLFW_KEY_ESCAPE
actual inline val GLFW_KEY_ENTER get() = GLFW.GLFW_KEY_ENTER
actual inline val GLFW_KEY_TAB get() = GLFW.GLFW_KEY_TAB
actual inline val GLFW_KEY_BACKSPACE get() = GLFW.GLFW_KEY_BACKSPACE
actual inline val GLFW_KEY_INSERT get() = GLFW.GLFW_KEY_INSERT
actual inline val GLFW_KEY_DELETE get() = GLFW.GLFW_KEY_DELETE
actual inline val GLFW_KEY_RIGHT get() = GLFW.GLFW_KEY_RIGHT
actual inline val GLFW_KEY_LEFT get() = GLFW.GLFW_KEY_LEFT
actual inline val GLFW_KEY_DOWN get() = GLFW.GLFW_KEY_DOWN
actual inline val GLFW_KEY_UP get() = GLFW.GLFW_KEY_UP
actual inline val GLFW_KEY_PAGE_UP get() = GLFW.GLFW_KEY_PAGE_UP
actual inline val GLFW_KEY_PAGE_DOWN get() = GLFW.GLFW_KEY_PAGE_DOWN
actual inline val GLFW_KEY_HOME get() = GLFW.GLFW_KEY_HOME
actual inline val GLFW_KEY_END get() = GLFW.GLFW_KEY_END
actual inline val GLFW_KEY_CAPS_LOCK get() = GLFW.GLFW_KEY_CAPS_LOCK
actual inline val GLFW_KEY_SCROLL_LOCK get() = GLFW.GLFW_KEY_SCROLL_LOCK
actual inline val GLFW_KEY_NUM_LOCK get() = GLFW.GLFW_KEY_NUM_LOCK
actual inline val GLFW_KEY_PRINT_SCREEN get() = GLFW.GLFW_KEY_PRINT_SCREEN
actual inline val GLFW_KEY_PAUSE get() = GLFW.GLFW_KEY_PAUSE
actual inline val GLFW_KEY_F1 get() = GLFW.GLFW_KEY_F1
actual inline val GLFW_KEY_F2 get() = GLFW.GLFW_KEY_F2
actual inline val GLFW_KEY_F3 get() = GLFW.GLFW_KEY_F3
actual inline val GLFW_KEY_F4 get() = GLFW.GLFW_KEY_F4
actual inline val GLFW_KEY_F5 get() = GLFW.GLFW_KEY_F5
actual inline val GLFW_KEY_F6 get() = GLFW.GLFW_KEY_F6
actual inline val GLFW_KEY_F7 get() = GLFW.GLFW_KEY_F7
actual inline val GLFW_KEY_F8 get() = GLFW.GLFW_KEY_F8
actual inline val GLFW_KEY_F9 get() = GLFW.GLFW_KEY_F9
actual inline val GLFW_KEY_F10 get() = GLFW.GLFW_KEY_F10
actual inline val GLFW_KEY_F11 get() = GLFW.GLFW_KEY_F11
actual inline val GLFW_KEY_F12 get() = GLFW.GLFW_KEY_F12
actual inline val GLFW_KEY_F13 get() = GLFW.GLFW_KEY_F13
actual inline val GLFW_KEY_F14 get() = GLFW.GLFW_KEY_F14
actual inline val GLFW_KEY_F15 get() = GLFW.GLFW_KEY_F15
actual inline val GLFW_KEY_F16 get() = GLFW.GLFW_KEY_F16
actual inline val GLFW_KEY_F17 get() = GLFW.GLFW_KEY_F17
actual inline val GLFW_KEY_F18 get() = GLFW.GLFW_KEY_F18
actual inline val GLFW_KEY_F19 get() = GLFW.GLFW_KEY_F19
actual inline val GLFW_KEY_F20 get() = GLFW.GLFW_KEY_F20
actual inline val GLFW_KEY_F21 get() = GLFW.GLFW_KEY_F21
actual inline val GLFW_KEY_F22 get() = GLFW.GLFW_KEY_F22
actual inline val GLFW_KEY_F23 get() = GLFW.GLFW_KEY_F23
actual inline val GLFW_KEY_F24 get() = GLFW.GLFW_KEY_F24
actual inline val GLFW_KEY_F25 get() = GLFW.GLFW_KEY_F25
actual inline val GLFW_KEY_KP_0 get() = GLFW.GLFW_KEY_KP_0
actual inline val GLFW_KEY_KP_1 get() = GLFW.GLFW_KEY_KP_1
actual inline val GLFW_KEY_KP_2 get() = GLFW.GLFW_KEY_KP_2
actual inline val GLFW_KEY_KP_3 get() = GLFW.GLFW_KEY_KP_3
actual inline val GLFW_KEY_KP_4 get() = GLFW.GLFW_KEY_KP_4
actual inline val GLFW_KEY_KP_5 get() = GLFW.GLFW_KEY_KP_5
actual inline val GLFW_KEY_KP_6 get() = GLFW.GLFW_KEY_KP_6
actual inline val GLFW_KEY_KP_7 get() = GLFW.GLFW_KEY_KP_7
actual inline val GLFW_KEY_KP_8 get() = GLFW.GLFW_KEY_KP_8
actual inline val GLFW_KEY_KP_9 get() = GLFW.GLFW_KEY_KP_9
actual inline val GLFW_KEY_KP_DECIMAL get() = GLFW.GLFW_KEY_KP_DECIMAL
actual inline val GLFW_KEY_KP_DIVIDE get() = GLFW.GLFW_KEY_KP_DIVIDE
actual inline val GLFW_KEY_KP_MULTIPLY get() = GLFW.GLFW_KEY_KP_MULTIPLY
actual inline val GLFW_KEY_KP_SUBTRACT get() = GLFW.GLFW_KEY_KP_SUBTRACT
actual inline val GLFW_KEY_KP_ADD get() = GLFW.GLFW_KEY_KP_ADD
actual inline val GLFW_KEY_KP_ENTER get() = GLFW.GLFW_KEY_KP_ENTER
actual inline val GLFW_KEY_KP_EQUAL get() = GLFW.GLFW_KEY_KP_EQUAL
actual inline val GLFW_KEY_LEFT_SHIFT get() = GLFW.GLFW_KEY_LEFT_SHIFT
actual inline val GLFW_KEY_LEFT_CONTROL get() = GLFW.GLFW_KEY_LEFT_CONTROL
actual inline val GLFW_KEY_LEFT_ALT get() = GLFW.GLFW_KEY_LEFT_ALT
actual inline val GLFW_KEY_LEFT_SUPER get() = GLFW.GLFW_KEY_LEFT_SUPER
actual inline val GLFW_KEY_RIGHT_SHIFT get() = GLFW.GLFW_KEY_RIGHT_SHIFT
actual inline val GLFW_KEY_RIGHT_CONTROL get() = GLFW.GLFW_KEY_RIGHT_CONTROL
actual inline val GLFW_KEY_RIGHT_ALT get() = GLFW.GLFW_KEY_RIGHT_ALT
actual inline val GLFW_KEY_RIGHT_SUPER get() = GLFW.GLFW_KEY_RIGHT_SUPER
actual inline val GLFW_KEY_MENU get() = GLFW.GLFW_KEY_MENU
actual inline val GLFW_KEY_LAST get() = GLFW.GLFW_KEY_LAST
actual inline val GLFW_MOD_SHIFT get() = GLFW.GLFW_MOD_SHIFT
actual inline val GLFW_MOD_CONTROL get() = GLFW.GLFW_MOD_CONTROL
actual inline val GLFW_MOD_ALT get() = GLFW.GLFW_MOD_ALT
actual inline val GLFW_MOD_SUPER get() = GLFW.GLFW_MOD_SUPER
actual inline val GLFW_MOD_CAPS_LOCK get() = GLFW.GLFW_MOD_CAPS_LOCK
actual inline val GLFW_MOD_NUM_LOCK get() = GLFW.GLFW_MOD_NUM_LOCK
actual inline val GLFW_MOUSE_BUTTON_1 get() = GLFW.GLFW_MOUSE_BUTTON_1
actual inline val GLFW_MOUSE_BUTTON_2 get() = GLFW.GLFW_MOUSE_BUTTON_2
actual inline val GLFW_MOUSE_BUTTON_3 get() = GLFW.GLFW_MOUSE_BUTTON_3
actual inline val GLFW_MOUSE_BUTTON_4 get() = GLFW.GLFW_MOUSE_BUTTON_4
actual inline val GLFW_MOUSE_BUTTON_5 get() = GLFW.GLFW_MOUSE_BUTTON_5
actual inline val GLFW_MOUSE_BUTTON_6 get() = GLFW.GLFW_MOUSE_BUTTON_6
actual inline val GLFW_MOUSE_BUTTON_7 get() = GLFW.GLFW_MOUSE_BUTTON_7
actual inline val GLFW_MOUSE_BUTTON_8 get() = GLFW.GLFW_MOUSE_BUTTON_8
actual inline val GLFW_MOUSE_BUTTON_LAST get() = GLFW.GLFW_MOUSE_BUTTON_LAST
actual inline val GLFW_MOUSE_BUTTON_LEFT get() = GLFW.GLFW_MOUSE_BUTTON_LEFT
actual inline val GLFW_MOUSE_BUTTON_RIGHT get() = GLFW.GLFW_MOUSE_BUTTON_RIGHT
actual inline val GLFW_MOUSE_BUTTON_MIDDLE get() = GLFW.GLFW_MOUSE_BUTTON_MIDDLE
actual inline val GLFW_JOYSTICK_1 get() = GLFW.GLFW_JOYSTICK_1
actual inline val GLFW_JOYSTICK_2 get() = GLFW.GLFW_JOYSTICK_2
actual inline val GLFW_JOYSTICK_3 get() = GLFW.GLFW_JOYSTICK_3
actual inline val GLFW_JOYSTICK_4 get() = GLFW.GLFW_JOYSTICK_4
actual inline val GLFW_JOYSTICK_5 get() = GLFW.GLFW_JOYSTICK_5
actual inline val GLFW_JOYSTICK_6 get() = GLFW.GLFW_JOYSTICK_6
actual inline val GLFW_JOYSTICK_7 get() = GLFW.GLFW_JOYSTICK_7
actual inline val GLFW_JOYSTICK_8 get() = GLFW.GLFW_JOYSTICK_8
actual inline val GLFW_JOYSTICK_9 get() = GLFW.GLFW_JOYSTICK_9
actual inline val GLFW_JOYSTICK_10 get() = GLFW.GLFW_JOYSTICK_10
actual inline val GLFW_JOYSTICK_11 get() = GLFW.GLFW_JOYSTICK_11
actual inline val GLFW_JOYSTICK_12 get() = GLFW.GLFW_JOYSTICK_12
actual inline val GLFW_JOYSTICK_13 get() = GLFW.GLFW_JOYSTICK_13
actual inline val GLFW_JOYSTICK_14 get() = GLFW.GLFW_JOYSTICK_14
actual inline val GLFW_JOYSTICK_15 get() = GLFW.GLFW_JOYSTICK_15
actual inline val GLFW_JOYSTICK_16 get() = GLFW.GLFW_JOYSTICK_16
actual inline val GLFW_JOYSTICK_LAST get() = GLFW.GLFW_JOYSTICK_LAST
actual inline val GLFW_GAMEPAD_BUTTON_A get() = GLFW.GLFW_GAMEPAD_BUTTON_A
actual inline val GLFW_GAMEPAD_BUTTON_B get() = GLFW.GLFW_GAMEPAD_BUTTON_B
actual inline val GLFW_GAMEPAD_BUTTON_X get() = GLFW.GLFW_GAMEPAD_BUTTON_X
actual inline val GLFW_GAMEPAD_BUTTON_Y get() = GLFW.GLFW_GAMEPAD_BUTTON_Y
actual inline val GLFW_GAMEPAD_BUTTON_LEFT_BUMPER get() = GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER
actual inline val GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER get() = GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER
actual inline val GLFW_GAMEPAD_BUTTON_BACK get() = GLFW.GLFW_GAMEPAD_BUTTON_BACK
actual inline val GLFW_GAMEPAD_BUTTON_START get() = GLFW.GLFW_GAMEPAD_BUTTON_START
actual inline val GLFW_GAMEPAD_BUTTON_GUIDE get() = GLFW.GLFW_GAMEPAD_BUTTON_GUIDE
actual inline val GLFW_GAMEPAD_BUTTON_LEFT_THUMB get() = GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB
actual inline val GLFW_GAMEPAD_BUTTON_RIGHT_THUMB get() = GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB
actual inline val GLFW_GAMEPAD_BUTTON_DPAD_UP get() = GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP
actual inline val GLFW_GAMEPAD_BUTTON_DPAD_RIGHT get() = GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT
actual inline val GLFW_GAMEPAD_BUTTON_DPAD_DOWN get() = GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN
actual inline val GLFW_GAMEPAD_BUTTON_DPAD_LEFT get() = GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT
actual inline val GLFW_GAMEPAD_BUTTON_LAST get() = GLFW.GLFW_GAMEPAD_BUTTON_LAST
actual inline val GLFW_GAMEPAD_BUTTON_CROSS get() = GLFW.GLFW_GAMEPAD_BUTTON_CROSS
actual inline val GLFW_GAMEPAD_BUTTON_CIRCLE get() = GLFW.GLFW_GAMEPAD_BUTTON_CIRCLE
actual inline val GLFW_GAMEPAD_BUTTON_SQUARE get() = GLFW.GLFW_GAMEPAD_BUTTON_SQUARE
actual inline val GLFW_GAMEPAD_BUTTON_TRIANGLE get() = GLFW.GLFW_GAMEPAD_BUTTON_TRIANGLE
actual inline val GLFW_GAMEPAD_AXIS_LEFT_X get() = GLFW.GLFW_GAMEPAD_AXIS_LEFT_X
actual inline val GLFW_GAMEPAD_AXIS_LEFT_Y get() = GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y
actual inline val GLFW_GAMEPAD_AXIS_RIGHT_X get() = GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X
actual inline val GLFW_GAMEPAD_AXIS_RIGHT_Y get() = GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y
actual inline val GLFW_GAMEPAD_AXIS_LEFT_TRIGGER get() = GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER
actual inline val GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER get() = GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER
actual inline val GLFW_GAMEPAD_AXIS_LAST get() = GLFW.GLFW_GAMEPAD_AXIS_LAST
actual inline val GLFW_NO_ERROR get() = GLFW.GLFW_NO_ERROR
actual inline val GLFW_NOT_INITIALIZED get() = GLFW.GLFW_NOT_INITIALIZED
actual inline val GLFW_NO_CURRENT_CONTEXT get() = GLFW.GLFW_NO_CURRENT_CONTEXT
actual inline val GLFW_INVALID_ENUM get() = GLFW.GLFW_INVALID_ENUM
actual inline val GLFW_INVALID_VALUE get() = GLFW.GLFW_INVALID_VALUE
actual inline val GLFW_OUT_OF_MEMORY get() = GLFW.GLFW_OUT_OF_MEMORY
actual inline val GLFW_API_UNAVAILABLE get() = GLFW.GLFW_API_UNAVAILABLE
actual inline val GLFW_VERSION_UNAVAILABLE get() = GLFW.GLFW_VERSION_UNAVAILABLE
actual inline val GLFW_PLATFORM_ERROR get() = GLFW.GLFW_PLATFORM_ERROR
actual inline val GLFW_FORMAT_UNAVAILABLE get() = GLFW.GLFW_FORMAT_UNAVAILABLE
actual inline val GLFW_NO_WINDOW_CONTEXT get() = GLFW.GLFW_NO_WINDOW_CONTEXT
actual inline val GLFW_FOCUSED get() = GLFW.GLFW_FOCUSED
actual inline val GLFW_ICONIFIED get() = GLFW.GLFW_ICONIFIED
actual inline val GLFW_RESIZABLE get() = GLFW.GLFW_RESIZABLE
actual inline val GLFW_VISIBLE get() = GLFW.GLFW_VISIBLE
actual inline val GLFW_DECORATED get() = GLFW.GLFW_DECORATED
actual inline val GLFW_AUTO_ICONIFY get() = GLFW.GLFW_AUTO_ICONIFY
actual inline val GLFW_FLOATING get() = GLFW.GLFW_FLOATING
actual inline val GLFW_MAXIMIZED get() = GLFW.GLFW_MAXIMIZED
actual inline val GLFW_CENTER_CURSOR get() = GLFW.GLFW_CENTER_CURSOR
actual inline val GLFW_TRANSPARENT_FRAMEBUFFER get() = GLFW.GLFW_TRANSPARENT_FRAMEBUFFER
actual inline val GLFW_HOVERED get() = GLFW.GLFW_HOVERED
actual inline val GLFW_CURSOR get() = GLFW.GLFW_CURSOR
actual inline val GLFW_STICKY_KEYS get() = GLFW.GLFW_STICKY_KEYS
actual inline val GLFW_STICKY_MOUSE_BUTTONS get() = GLFW.GLFW_STICKY_MOUSE_BUTTONS
actual inline val GLFW_LOCK_KEY_MODS get() = GLFW.GLFW_LOCK_KEY_MODS
actual inline val GLFW_CURSOR_NORMAL get() = GLFW.GLFW_CURSOR_NORMAL
actual inline val GLFW_CURSOR_HIDDEN get() = GLFW.GLFW_CURSOR_HIDDEN
actual inline val GLFW_CURSOR_DISABLED get() = GLFW.GLFW_CURSOR_DISABLED
actual inline val GLFW_ARROW_CURSOR get() = GLFW.GLFW_ARROW_CURSOR
actual inline val GLFW_IBEAM_CURSOR get() = GLFW.GLFW_IBEAM_CURSOR
actual inline val GLFW_CROSSHAIR_CURSOR get() = GLFW.GLFW_CROSSHAIR_CURSOR
actual inline val GLFW_HAND_CURSOR get() = GLFW.GLFW_HAND_CURSOR
actual inline val GLFW_HRESIZE_CURSOR get() = GLFW.GLFW_HRESIZE_CURSOR
actual inline val GLFW_VRESIZE_CURSOR get() = GLFW.GLFW_VRESIZE_CURSOR
actual inline val GLFW_CONNECTED get() = GLFW.GLFW_CONNECTED
actual inline val GLFW_DISCONNECTED get() = GLFW.GLFW_DISCONNECTED
actual inline val GLFW_JOYSTICK_HAT_BUTTONS get() = GLFW.GLFW_JOYSTICK_HAT_BUTTONS
actual inline val GLFW_COCOA_CHDIR_RESOURCES get() = GLFW.GLFW_COCOA_CHDIR_RESOURCES
actual inline val GLFW_COCOA_MENUBAR get() = GLFW.GLFW_COCOA_MENUBAR
actual inline val GLFW_DONT_CARE get() = GLFW.GLFW_DONT_CARE
actual inline val GLFW_RED_BITS get() = GLFW.GLFW_RED_BITS
actual inline val GLFW_GREEN_BITS get() = GLFW.GLFW_GREEN_BITS
actual inline val GLFW_BLUE_BITS get() = GLFW.GLFW_BLUE_BITS
actual inline val GLFW_ALPHA_BITS get() = GLFW.GLFW_ALPHA_BITS
actual inline val GLFW_DEPTH_BITS get() = GLFW.GLFW_DEPTH_BITS
actual inline val GLFW_STENCIL_BITS get() = GLFW.GLFW_STENCIL_BITS
actual inline val GLFW_ACCUM_RED_BITS get() = GLFW.GLFW_ACCUM_RED_BITS
actual inline val GLFW_ACCUM_GREEN_BITS get() = GLFW.GLFW_ACCUM_GREEN_BITS
actual inline val GLFW_ACCUM_BLUE_BITS get() = GLFW.GLFW_ACCUM_BLUE_BITS
actual inline val GLFW_ACCUM_ALPHA_BITS get() = GLFW.GLFW_ACCUM_ALPHA_BITS
actual inline val GLFW_AUX_BUFFERS get() = GLFW.GLFW_AUX_BUFFERS
actual inline val GLFW_STEREO get() = GLFW.GLFW_STEREO
actual inline val GLFW_SAMPLES get() = GLFW.GLFW_SAMPLES
actual inline val GLFW_SRGB_CAPABLE get() = GLFW.GLFW_SRGB_CAPABLE
actual inline val GLFW_REFRESH_RATE get() = GLFW.GLFW_REFRESH_RATE
actual inline val GLFW_DOUBLEBUFFER get() = GLFW.GLFW_DOUBLEBUFFER
actual inline val GLFW_CLIENT_API get() = GLFW.GLFW_CLIENT_API
actual inline val GLFW_CONTEXT_VERSION_MAJOR get() = GLFW.GLFW_CONTEXT_VERSION_MAJOR
actual inline val GLFW_CONTEXT_VERSION_MINOR get() = GLFW.GLFW_CONTEXT_VERSION_MINOR
actual inline val GLFW_CONTEXT_REVISION get() = GLFW.GLFW_CONTEXT_REVISION
actual inline val GLFW_CONTEXT_ROBUSTNESS get() = GLFW.GLFW_CONTEXT_ROBUSTNESS
actual inline val GLFW_OPENGL_FORWARD_COMPAT get() = GLFW.GLFW_OPENGL_FORWARD_COMPAT
actual inline val GLFW_OPENGL_DEBUG_CONTEXT get() = GLFW.GLFW_OPENGL_DEBUG_CONTEXT
actual inline val GLFW_OPENGL_PROFILE get() = GLFW.GLFW_OPENGL_PROFILE
actual inline val GLFW_CONTEXT_RELEASE_BEHAVIOR get() = GLFW.GLFW_CONTEXT_RELEASE_BEHAVIOR
actual inline val GLFW_CONTEXT_NO_ERROR get() = GLFW.GLFW_CONTEXT_NO_ERROR
actual inline val GLFW_CONTEXT_CREATION_API get() = GLFW.GLFW_CONTEXT_CREATION_API
actual inline val GLFW_COCOA_RETINA_FRAMEBUFFER get() = GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER
actual inline val GLFW_COCOA_FRAME_NAME get() = GLFW.GLFW_COCOA_FRAME_NAME
actual inline val GLFW_COCOA_GRAPHICS_SWITCHING get() = GLFW.GLFW_COCOA_GRAPHICS_SWITCHING
actual inline val GLFW_X11_CLASS_NAME get() = GLFW.GLFW_X11_CLASS_NAME
actual inline val GLFW_X11_INSTANCE_NAME get() = GLFW.GLFW_X11_INSTANCE_NAME
actual inline val GLFW_NO_API get() = GLFW.GLFW_NO_API
actual inline val GLFW_OPENGL_API get() = GLFW.GLFW_OPENGL_API
actual inline val GLFW_OPENGL_ES_API get() = GLFW.GLFW_OPENGL_ES_API
actual inline val GLFW_NO_ROBUSTNESS get() = GLFW.GLFW_NO_ROBUSTNESS
actual inline val GLFW_NO_RESET_NOTIFICATION get() = GLFW.GLFW_NO_RESET_NOTIFICATION
actual inline val GLFW_LOSE_CONTEXT_ON_RESET get() = GLFW.GLFW_LOSE_CONTEXT_ON_RESET
actual inline val GLFW_OPENGL_ANY_PROFILE get() = GLFW.GLFW_OPENGL_ANY_PROFILE
actual inline val GLFW_OPENGL_CORE_PROFILE get() = GLFW.GLFW_OPENGL_CORE_PROFILE
actual inline val GLFW_OPENGL_COMPAT_PROFILE get() = GLFW.GLFW_OPENGL_COMPAT_PROFILE
actual inline val GLFW_ANY_RELEASE_BEHAVIOR get() = GLFW.GLFW_ANY_RELEASE_BEHAVIOR
actual inline val GLFW_RELEASE_BEHAVIOR_FLUSH get() = GLFW.GLFW_RELEASE_BEHAVIOR_FLUSH
actual inline val GLFW_RELEASE_BEHAVIOR_NONE get() = GLFW.GLFW_RELEASE_BEHAVIOR_NONE
actual inline val GLFW_NATIVE_CONTEXT_API get() = GLFW.GLFW_NATIVE_CONTEXT_API
actual inline val GLFW_EGL_CONTEXT_API get() = GLFW.GLFW_EGL_CONTEXT_API
actual inline val GLFW_OSMESA_CONTEXT_API get() = GLFW.GLFW_OSMESA_CONTEXT_API

actual inline fun glfwInit() =
    GLFW.glfwInit()

actual inline fun glfwTerminate() =
    GLFW.glfwTerminate()

actual inline fun glfwInitHint(hint: Int, value: Int) =
    GLFW.glfwInitHint(hint, value)

actual inline fun glfwGetVersion(
    major: IntArray?, minor: IntArray?, rev: IntArray?
) = GLFW.glfwGetVersion(major, minor, rev)

actual inline fun glfwGetVersionString() =
    GLFW.glfwGetVersionString()

actual inline fun glfwGetError(
    description: Array<String?>?
) = stackFrame { stack ->
    val descriptionBuffer =
        if (description == null) null else stack.mallocPointer(1)
    GLFW.glfwGetError(descriptionBuffer).also {
        if (description != null && descriptionBuffer != null) {
            description[0] = MemoryUtil.memUTF8Safe(descriptionBuffer.get(0))
        }
    }
}

actual inline fun glfwSetErrorCallback(cbfun: GLFWErrorCallback?) =
    GLFW.glfwSetErrorCallback(cbfun)

actual inline fun glfwGetMonitors() =
    GLFW.glfwGetMonitors()

actual inline fun glfwGetPrimaryMonitor() =
    GLFW.glfwGetPrimaryMonitor()

actual inline fun glfwGetMonitorPos(
    monitor: GLFWMonitor, xpos: IntArray?, ypos: IntArray?
) = GLFW.glfwGetMonitorPos(monitor, xpos, ypos)

actual inline fun glfwGetMonitorPhysicalSize(
    monitor: GLFWMonitor, widthMM: IntArray?, heightMM: IntArray?
) = GLFW.glfwGetMonitorPhysicalSize(monitor, widthMM, heightMM)

actual inline fun glfwGetMonitorContentScale(
    monitor: GLFWMonitor, xscale: FloatArray?, yscale: FloatArray?
) = GLFW.glfwGetMonitorContentScale(monitor, xscale, yscale)

actual inline fun glfwGetMonitorName(monitor: GLFWMonitor) =
    GLFW.glfwGetMonitorName(monitor)

actual inline fun glfwSetMonitorUserPointer(
    monitor: GLFWMonitor, pointer: Long
) = GLFW.glfwSetMonitorUserPointer(monitor, pointer)

actual inline fun glfwGetMonitorUserPointer(monitor: GLFWMonitor) =
    GLFW.glfwGetMonitorUserPointer(monitor)

actual inline fun glfwSetMonitorCallback(
    cbfun: GLFWMonitorCallback?
) = GLFW.glfwSetMonitorCallback(cbfun)

actual inline fun glfwGetVideoModes(monitor: GLFWMonitor) =
    GLFW.glfwGetVideoModes(monitor)

actual inline fun glfwGetVideoMode(monitor: GLFWMonitor) =
    GLFW.glfwGetVideoMode(monitor)

actual inline fun glfwSetGamma(monitor: GLFWMonitor, gamma: Float) =
    GLFW.glfwSetGamma(monitor, gamma)

actual inline fun glfwGetGammaRamp(monitor: GLFWMonitor) =
    GLFW.glfwGetGammaRamp(monitor)

actual inline fun glfwSetGammaRamp(monitor: GLFWMonitor, ramp: GLFWGammaRamp) =
    GLFW.glfwSetGammaRamp(monitor, ramp)

actual inline fun glfwDefaultWindowHints() =
    GLFW.glfwDefaultWindowHints()

actual inline fun glfwWindowHint(hint: Int, value: Int) =
    GLFW.glfwWindowHint(hint, value)

actual inline fun glfwWindowHintString(hint: Int, value: String) =
    GLFW.glfwWindowHintString(hint, value)

actual inline fun glfwCreateWindow(
    width: Int, height: Int, title: String, monitor: GLFWMonitor, share: Long
) = GLFW.glfwCreateWindow(width, height, title, monitor, share)

actual inline fun glfwDestroyWindow(window: GLFWWindow) =
    GLFW.glfwDestroyWindow(window)

actual inline fun glfwWindowShouldClose(window: GLFWWindow) =
    GLFW.glfwWindowShouldClose(window)

actual inline fun glfwSetWindowTitle(window: GLFWWindow, title: String) =
    GLFW.glfwSetWindowTitle(window, title)

actual inline fun glfwSetWindowIcon(
    window: GLFWWindow, images: GLFWImageBuffer
) = GLFW.glfwSetWindowIcon(window, images)

actual inline fun glfwGetWindowPos(
    window: GLFWWindow, xpos: IntArray?, ypos: IntArray?
) = GLFW.glfwGetWindowPos(window, xpos, ypos)

actual inline fun glfwSetWindowPos(window: GLFWWindow, xpos: Int, ypos: Int) =
    GLFW.glfwSetWindowPos(window, xpos, ypos)

actual inline fun glfwGetWindowSize(
    window: GLFWWindow, width: IntArray?, height: IntArray?
) = GLFW.glfwGetWindowSize(window, width, height)

actual inline fun glfwSetWindowSizeLimits(
    window: GLFWWindow,
    minwidth: Int, minheight: Int,
    maxwidth: Int, maxheight: Int
) = GLFW.glfwSetWindowSizeLimits(
    window, minwidth, minheight, maxwidth, maxheight
)

actual inline fun glfwSetWindowAspectRatio(
    window: GLFWWindow, numer: Int, denom: Int
) = GLFW.glfwSetWindowAspectRatio(window, numer, denom)

actual inline fun glfwSetWindowSize(
    window: GLFWWindow, width: Int, height: Int
) = GLFW.glfwSetWindowSize(window, width, height)

actual inline fun glfwGetFramebufferSize(
    window: GLFWWindow, width: IntArray?, height: IntArray?
) = GLFW.glfwGetFramebufferSize(window, width, height)

actual inline fun glfwGetWindowFrameSize(
    window: GLFWWindow,
    left: IntArray?, top: IntArray?,
    right: IntArray?, bottom: IntArray?
) = GLFW.glfwGetWindowFrameSize(window, left, top, right, bottom)

actual inline fun glfwGetWindowContentScale(
    window: GLFWWindow, xscale: FloatArray?, yscale: FloatArray?
) = GLFW.glfwGetWindowContentScale(window, xscale, yscale)

actual inline fun glfwGetWindowOpacity(window: GLFWWindow) =
    GLFW.glfwGetWindowOpacity(window)

actual inline fun glfwSetWindowOpacity(window: GLFWWindow, opacity: Float) =
    GLFW.glfwSetWindowOpacity(window, opacity)

actual inline fun glfwIconifyWindow(window: GLFWWindow) =
    GLFW.glfwIconifyWindow(window)

actual inline fun glfwRestoreWindow(window: GLFWWindow) =
    GLFW.glfwRestoreWindow(window)

actual inline fun glfwMaximizeWindow(window: GLFWWindow) =
    GLFW.glfwMaximizeWindow(window)

actual inline fun glfwShowWindow(window: GLFWWindow) =
    GLFW.glfwShowWindow(window)

actual inline fun glfwHideWindow(window: GLFWWindow) =
    GLFW.glfwHideWindow(window)

actual inline fun glfwFocusWindow(window: GLFWWindow) =
    GLFW.glfwFocusWindow(window)

actual inline fun glfwRequestWindowAttention(window: GLFWWindow) =
    GLFW.glfwRequestWindowAttention(window)

actual inline fun glfwGetWindowMonitor(window: GLFWWindow) =
    GLFW.glfwGetWindowMonitor(window)

actual inline fun glfwSetWindowMonitor(
    window: GLFWWindow, monitor: GLFWMonitor,
    xpos: Int, ypos: Int, width: Int, height: Int, refreshRate: Int
) = GLFW.glfwSetWindowMonitor(
    window, monitor, xpos, ypos, width, height, refreshRate
)

actual inline fun glfwGetWindowAttrib(window: GLFWWindow, attrib: Int) =
    GLFW.glfwGetWindowAttrib(window, attrib)

actual inline fun glfwSetWindowAttrib(
    window: GLFWWindow, attrib: Int, value: Int
) = GLFW.glfwSetWindowAttrib(window, attrib, value)

actual inline fun glfwSetWindowUserPointer(window: GLFWWindow, pointer: Long) =
    GLFW.glfwSetWindowUserPointer(window, pointer)

actual inline fun glfwGetWindowUserPointer(window: GLFWWindow) =
    GLFW.glfwGetWindowUserPointer(window)

actual inline fun glfwSetWindowPosCallback(
    window: GLFWWindow, cbfun: GLFWWindowPosCallback?
) = GLFW.glfwSetWindowPosCallback(window, cbfun)

actual inline fun glfwSetWindowSizeCallback(
    window: GLFWWindow, cbfun: GLFWWindowSizeCallback?
) = GLFW.glfwSetWindowSizeCallback(window, cbfun)

actual inline fun glfwSetWindowCloseCallback(
    window: GLFWWindow, cbfun: GLFWWindowCloseCallback?
) = GLFW.glfwSetWindowCloseCallback(window, cbfun)

actual inline fun glfwSetWindowRefreshCallback(
    window: GLFWWindow, cbfun: GLFWWindowRefreshCallback?
) = GLFW.glfwSetWindowRefreshCallback(window, cbfun)

actual inline fun glfwSetWindowFocusCallback(
    window: GLFWWindow, cbfun: GLFWWindowFocusCallback?
) = GLFW.glfwSetWindowFocusCallback(window, cbfun)

actual inline fun glfwSetWindowIconifyCallback(
    window: GLFWWindow, cbfun: GLFWWindowIconifyCallback?
) = GLFW.glfwSetWindowIconifyCallback(window, cbfun)

actual inline fun glfwSetWindowMaximizeCallback(
    window: GLFWWindow, cbfun: GLFWWindowMaximizeCallback?
) = GLFW.glfwSetWindowMaximizeCallback(window, cbfun)

actual inline fun glfwSetFramebufferSizeCallback(
    window: GLFWWindow, cbfun: GLFWFramebufferSizeCallback?
) = GLFW.glfwSetFramebufferSizeCallback(window, cbfun)

actual inline fun glfwSetWindowContentScaleCallback(
    window: GLFWWindow, cbfun: GLFWWindowContentScaleCallback?
) = GLFW.glfwSetWindowContentScaleCallback(window, cbfun)

actual inline fun glfwPollEvents() = GLFW.glfwPollEvents()
actual inline fun glfwWaitEvents() = GLFW.glfwWaitEvents()
actual inline fun glfwWaitEventsTimeout(timeout: Double) =
    GLFW.glfwWaitEventsTimeout(timeout)

actual inline fun glfwPostEmptyEvent() = GLFW.glfwPostEmptyEvent()
actual inline fun glfwGetInputMode(window: GLFWWindow, mode: Int) =
    GLFW.glfwGetInputMode(window, mode)

actual inline fun glfwSetInputMode(window: GLFWWindow, mode: Int, value: Int) =
    GLFW.glfwSetInputMode(window, mode, value)

actual inline fun glfwGetKeyName(key: Int, scancode: Int) =
    GLFW.glfwGetKeyName(key, scancode)

actual inline fun glfwGetKeyScancode(key: Int) = GLFW.glfwGetKeyScancode(key)
actual inline fun glfwGetKey(window: GLFWWindow, key: Int) =
    GLFW.glfwGetKey(window, key)

actual inline fun glfwGetMouseButton(window: GLFWWindow, button: Int) =
    GLFW.glfwGetMouseButton(window, button)

actual inline fun glfwGetCursorPos(
    window: GLFWWindow, xpos: DoubleArray?, ypos: DoubleArray?
) = GLFW.glfwGetCursorPos(window, xpos, ypos)

actual inline fun glfwSetCursorPos(
    window: GLFWWindow, xpos: Double, ypos: Double
) = GLFW.glfwSetCursorPos(window, xpos, ypos)

actual inline fun glfwCreateCursor(image: GLFWImage, xhot: Int, yhot: Int) =
    GLFW.glfwCreateCursor(image, xhot, yhot)

actual inline fun glfwCreateStandardCursor(shape: Int) =
    GLFW.glfwCreateStandardCursor(shape)

actual inline fun glfwDestroyCursor(cursor: GLFWCursor) =
    GLFW.glfwDestroyCursor(cursor)

actual inline fun glfwSetCursor(window: GLFWWindow, cursor: GLFWCursor) =
    GLFW.glfwSetCursor(window, cursor)

actual inline fun glfwSetKeyCallback(
    window: GLFWWindow, cbfun: GLFWKeyCallback?
) = GLFW.glfwSetKeyCallback(window, cbfun)

actual inline fun glfwSetCharCallback(
    window: GLFWWindow, cbfun: GLFWCharCallback?
) = GLFW.glfwSetCharCallback(window, cbfun)

actual inline fun glfwSetCharModsCallback(
    window: GLFWWindow, cbfun: GLFWCharModsCallback?
) = GLFW.glfwSetCharModsCallback(window, cbfun)

actual inline fun glfwSetMouseButtonCallback(
    window: GLFWWindow, cbfun: GLFWMouseButtonCallback?
) = GLFW.glfwSetMouseButtonCallback(window, cbfun)

actual inline fun glfwSetCursorPosCallback(
    window: GLFWWindow, cbfun: GLFWCursorPosCallback?
) = GLFW.glfwSetCursorPosCallback(window, cbfun)

actual inline fun glfwSetCursorEnterCallback(
    window: GLFWWindow, cbfun: GLFWCursorEnterCallback?
) = GLFW.glfwSetCursorEnterCallback(window, cbfun)

actual inline fun glfwSetScrollCallback(
    window: GLFWWindow, cbfun: GLFWScrollCallback?
) = GLFW.glfwSetScrollCallback(window, cbfun)

actual inline fun glfwSetDropCallback(
    window: GLFWWindow, cbfun: GLFWDropCallback?
) = GLFW.glfwSetDropCallback(window, cbfun)

actual inline fun glfwJoystickPresent(jid: Int) =
    GLFW.glfwJoystickPresent(jid)

actual inline fun glfwGetJoystickAxes(jid: Int): FloatsRO? =
    GLFW.glfwGetJoystickAxes(jid)?.asFloats()

actual inline fun glfwGetJoystickButtons(jid: Int): BytesRO? =
    GLFW.glfwGetJoystickButtons(jid)?.viewSliceE

actual inline fun glfwGetJoystickHats(jid: Int): BytesRO? =
    GLFW.glfwGetJoystickHats(jid)?.viewSliceE

actual inline fun glfwGetJoystickName(jid: Int) =
    GLFW.glfwGetJoystickName(jid)

actual inline fun glfwGetJoystickGUID(jid: Int) =
    GLFW.glfwGetJoystickGUID(jid)

actual inline fun glfwSetJoystickUserPointer(jid: Int, pointer: Long) =
    GLFW.glfwSetJoystickUserPointer(jid, pointer)

actual inline fun glfwGetJoystickUserPointer(jid: Int) =
    GLFW.glfwGetJoystickUserPointer(jid)

actual inline fun glfwJoystickIsGamepad(jid: Int) =
    GLFW.glfwJoystickIsGamepad(jid)

actual inline fun glfwSetJoystickCallback(cbfun: GLFWJoystickCallback?) =
    GLFW.glfwSetJoystickCallback(cbfun)

actual inline fun glfwUpdateGamepadMappings(string: String) =
    GLFW.glfwUpdateGamepadMappings(string)

actual inline fun glfwGetGamepadName(jid: Int) =
    GLFW.glfwGetGamepadName(jid)

actual inline fun glfwGetGamepadState(jid: Int, state: GLFWGamepadState) =
    GLFW.glfwGetGamepadState(jid, state)

actual inline fun glfwSetClipboardString(window: GLFWWindow, string: String) =
    GLFW.glfwSetClipboardString(window, string)

actual inline fun glfwGetClipboardString(window: GLFWWindow) =
    GLFW.glfwGetClipboardString(window)

actual inline fun glfwGetTime() =
    GLFW.glfwGetTime()

actual inline fun glfwSetTime(time: Double) =
    GLFW.glfwSetTime(time)

actual inline fun glfwGetTimerValue() =
    GLFW.glfwGetTimerValue()

actual inline fun glfwGetTimerFrequency() =
    GLFW.glfwGetTimerFrequency()

actual inline fun glfwMakeContextCurrent(window: GLFWWindow) =
    GLFW.glfwMakeContextCurrent(window)

actual inline fun glfwGetCurrentContext() =
    GLFW.glfwGetCurrentContext()

actual inline fun glfwSwapBuffers(window: GLFWWindow) =
    GLFW.glfwSwapBuffers(window)

actual inline fun glfwSwapInterval(interval: Int) =
    GLFW.glfwSwapInterval(interval)
