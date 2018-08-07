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

package org.tobi29.scapes.engine.backends.glfw

import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.FloatsRO

expect val GLFW_VERSION_MAJOR: Int
expect val GLFW_VERSION_MINOR: Int
expect val GLFW_VERSION_REVISION: Int
expect val GLFW_TRUE: Int
expect val GLFW_FALSE: Int
expect val GLFW_RELEASE: Int
expect val GLFW_PRESS: Int
expect val GLFW_REPEAT: Int
expect val GLFW_HAT_CENTERED: Int
expect val GLFW_HAT_UP: Int
expect val GLFW_HAT_RIGHT: Int
expect val GLFW_HAT_DOWN: Int
expect val GLFW_HAT_LEFT: Int
expect val GLFW_HAT_RIGHT_UP: Int
expect val GLFW_HAT_RIGHT_DOWN: Int
expect val GLFW_HAT_LEFT_UP: Int
expect val GLFW_HAT_LEFT_DOWN: Int
expect val GLFW_KEY_UNKNOWN: Int
expect val GLFW_KEY_SPACE: Int
expect val GLFW_KEY_APOSTROPHE: Int
expect val GLFW_KEY_COMMA: Int
expect val GLFW_KEY_MINUS: Int
expect val GLFW_KEY_PERIOD: Int
expect val GLFW_KEY_SLASH: Int
expect val GLFW_KEY_0: Int
expect val GLFW_KEY_1: Int
expect val GLFW_KEY_2: Int
expect val GLFW_KEY_3: Int
expect val GLFW_KEY_4: Int
expect val GLFW_KEY_5: Int
expect val GLFW_KEY_6: Int
expect val GLFW_KEY_7: Int
expect val GLFW_KEY_8: Int
expect val GLFW_KEY_9: Int
expect val GLFW_KEY_SEMICOLON: Int
expect val GLFW_KEY_EQUAL: Int
expect val GLFW_KEY_A: Int
expect val GLFW_KEY_B: Int
expect val GLFW_KEY_C: Int
expect val GLFW_KEY_D: Int
expect val GLFW_KEY_E: Int
expect val GLFW_KEY_F: Int
expect val GLFW_KEY_G: Int
expect val GLFW_KEY_H: Int
expect val GLFW_KEY_I: Int
expect val GLFW_KEY_J: Int
expect val GLFW_KEY_K: Int
expect val GLFW_KEY_L: Int
expect val GLFW_KEY_M: Int
expect val GLFW_KEY_N: Int
expect val GLFW_KEY_O: Int
expect val GLFW_KEY_P: Int
expect val GLFW_KEY_Q: Int
expect val GLFW_KEY_R: Int
expect val GLFW_KEY_S: Int
expect val GLFW_KEY_T: Int
expect val GLFW_KEY_U: Int
expect val GLFW_KEY_V: Int
expect val GLFW_KEY_W: Int
expect val GLFW_KEY_X: Int
expect val GLFW_KEY_Y: Int
expect val GLFW_KEY_Z: Int
expect val GLFW_KEY_LEFT_BRACKET: Int
expect val GLFW_KEY_BACKSLASH: Int
expect val GLFW_KEY_RIGHT_BRACKET: Int
expect val GLFW_KEY_GRAVE_ACCENT: Int
expect val GLFW_KEY_WORLD_1: Int
expect val GLFW_KEY_WORLD_2: Int
expect val GLFW_KEY_ESCAPE: Int
expect val GLFW_KEY_ENTER: Int
expect val GLFW_KEY_TAB: Int
expect val GLFW_KEY_BACKSPACE: Int
expect val GLFW_KEY_INSERT: Int
expect val GLFW_KEY_DELETE: Int
expect val GLFW_KEY_RIGHT: Int
expect val GLFW_KEY_LEFT: Int
expect val GLFW_KEY_DOWN: Int
expect val GLFW_KEY_UP: Int
expect val GLFW_KEY_PAGE_UP: Int
expect val GLFW_KEY_PAGE_DOWN: Int
expect val GLFW_KEY_HOME: Int
expect val GLFW_KEY_END: Int
expect val GLFW_KEY_CAPS_LOCK: Int
expect val GLFW_KEY_SCROLL_LOCK: Int
expect val GLFW_KEY_NUM_LOCK: Int
expect val GLFW_KEY_PRINT_SCREEN: Int
expect val GLFW_KEY_PAUSE: Int
expect val GLFW_KEY_F1: Int
expect val GLFW_KEY_F2: Int
expect val GLFW_KEY_F3: Int
expect val GLFW_KEY_F4: Int
expect val GLFW_KEY_F5: Int
expect val GLFW_KEY_F6: Int
expect val GLFW_KEY_F7: Int
expect val GLFW_KEY_F8: Int
expect val GLFW_KEY_F9: Int
expect val GLFW_KEY_F10: Int
expect val GLFW_KEY_F11: Int
expect val GLFW_KEY_F12: Int
expect val GLFW_KEY_F13: Int
expect val GLFW_KEY_F14: Int
expect val GLFW_KEY_F15: Int
expect val GLFW_KEY_F16: Int
expect val GLFW_KEY_F17: Int
expect val GLFW_KEY_F18: Int
expect val GLFW_KEY_F19: Int
expect val GLFW_KEY_F20: Int
expect val GLFW_KEY_F21: Int
expect val GLFW_KEY_F22: Int
expect val GLFW_KEY_F23: Int
expect val GLFW_KEY_F24: Int
expect val GLFW_KEY_F25: Int
expect val GLFW_KEY_KP_0: Int
expect val GLFW_KEY_KP_1: Int
expect val GLFW_KEY_KP_2: Int
expect val GLFW_KEY_KP_3: Int
expect val GLFW_KEY_KP_4: Int
expect val GLFW_KEY_KP_5: Int
expect val GLFW_KEY_KP_6: Int
expect val GLFW_KEY_KP_7: Int
expect val GLFW_KEY_KP_8: Int
expect val GLFW_KEY_KP_9: Int
expect val GLFW_KEY_KP_DECIMAL: Int
expect val GLFW_KEY_KP_DIVIDE: Int
expect val GLFW_KEY_KP_MULTIPLY: Int
expect val GLFW_KEY_KP_SUBTRACT: Int
expect val GLFW_KEY_KP_ADD: Int
expect val GLFW_KEY_KP_ENTER: Int
expect val GLFW_KEY_KP_EQUAL: Int
expect val GLFW_KEY_LEFT_SHIFT: Int
expect val GLFW_KEY_LEFT_CONTROL: Int
expect val GLFW_KEY_LEFT_ALT: Int
expect val GLFW_KEY_LEFT_SUPER: Int
expect val GLFW_KEY_RIGHT_SHIFT: Int
expect val GLFW_KEY_RIGHT_CONTROL: Int
expect val GLFW_KEY_RIGHT_ALT: Int
expect val GLFW_KEY_RIGHT_SUPER: Int
expect val GLFW_KEY_MENU: Int
expect val GLFW_KEY_LAST: Int
expect val GLFW_MOD_SHIFT: Int
expect val GLFW_MOD_CONTROL: Int
expect val GLFW_MOD_ALT: Int
expect val GLFW_MOD_SUPER: Int
expect val GLFW_MOD_CAPS_LOCK: Int
expect val GLFW_MOD_NUM_LOCK: Int
expect val GLFW_MOUSE_BUTTON_1: Int
expect val GLFW_MOUSE_BUTTON_2: Int
expect val GLFW_MOUSE_BUTTON_3: Int
expect val GLFW_MOUSE_BUTTON_4: Int
expect val GLFW_MOUSE_BUTTON_5: Int
expect val GLFW_MOUSE_BUTTON_6: Int
expect val GLFW_MOUSE_BUTTON_7: Int
expect val GLFW_MOUSE_BUTTON_8: Int
expect val GLFW_MOUSE_BUTTON_LAST: Int
expect val GLFW_MOUSE_BUTTON_LEFT: Int
expect val GLFW_MOUSE_BUTTON_RIGHT: Int
expect val GLFW_MOUSE_BUTTON_MIDDLE: Int
expect val GLFW_JOYSTICK_1: Int
expect val GLFW_JOYSTICK_2: Int
expect val GLFW_JOYSTICK_3: Int
expect val GLFW_JOYSTICK_4: Int
expect val GLFW_JOYSTICK_5: Int
expect val GLFW_JOYSTICK_6: Int
expect val GLFW_JOYSTICK_7: Int
expect val GLFW_JOYSTICK_8: Int
expect val GLFW_JOYSTICK_9: Int
expect val GLFW_JOYSTICK_10: Int
expect val GLFW_JOYSTICK_11: Int
expect val GLFW_JOYSTICK_12: Int
expect val GLFW_JOYSTICK_13: Int
expect val GLFW_JOYSTICK_14: Int
expect val GLFW_JOYSTICK_15: Int
expect val GLFW_JOYSTICK_16: Int
expect val GLFW_JOYSTICK_LAST: Int
expect val GLFW_GAMEPAD_BUTTON_A: Int
expect val GLFW_GAMEPAD_BUTTON_B: Int
expect val GLFW_GAMEPAD_BUTTON_X: Int
expect val GLFW_GAMEPAD_BUTTON_Y: Int
expect val GLFW_GAMEPAD_BUTTON_LEFT_BUMPER: Int
expect val GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER: Int
expect val GLFW_GAMEPAD_BUTTON_BACK: Int
expect val GLFW_GAMEPAD_BUTTON_START: Int
expect val GLFW_GAMEPAD_BUTTON_GUIDE: Int
expect val GLFW_GAMEPAD_BUTTON_LEFT_THUMB: Int
expect val GLFW_GAMEPAD_BUTTON_RIGHT_THUMB: Int
expect val GLFW_GAMEPAD_BUTTON_DPAD_UP: Int
expect val GLFW_GAMEPAD_BUTTON_DPAD_RIGHT: Int
expect val GLFW_GAMEPAD_BUTTON_DPAD_DOWN: Int
expect val GLFW_GAMEPAD_BUTTON_DPAD_LEFT: Int
expect val GLFW_GAMEPAD_BUTTON_LAST: Int
expect val GLFW_GAMEPAD_BUTTON_CROSS: Int
expect val GLFW_GAMEPAD_BUTTON_CIRCLE: Int
expect val GLFW_GAMEPAD_BUTTON_SQUARE: Int
expect val GLFW_GAMEPAD_BUTTON_TRIANGLE: Int
expect val GLFW_GAMEPAD_AXIS_LEFT_X: Int
expect val GLFW_GAMEPAD_AXIS_LEFT_Y: Int
expect val GLFW_GAMEPAD_AXIS_RIGHT_X: Int
expect val GLFW_GAMEPAD_AXIS_RIGHT_Y: Int
expect val GLFW_GAMEPAD_AXIS_LEFT_TRIGGER: Int
expect val GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER: Int
expect val GLFW_GAMEPAD_AXIS_LAST: Int
expect val GLFW_NO_ERROR: Int
expect val GLFW_NOT_INITIALIZED: Int
expect val GLFW_NO_CURRENT_CONTEXT: Int
expect val GLFW_INVALID_ENUM: Int
expect val GLFW_INVALID_VALUE: Int
expect val GLFW_OUT_OF_MEMORY: Int
expect val GLFW_API_UNAVAILABLE: Int
expect val GLFW_VERSION_UNAVAILABLE: Int
expect val GLFW_PLATFORM_ERROR: Int
expect val GLFW_FORMAT_UNAVAILABLE: Int
expect val GLFW_NO_WINDOW_CONTEXT: Int
expect val GLFW_FOCUSED: Int
expect val GLFW_ICONIFIED: Int
expect val GLFW_RESIZABLE: Int
expect val GLFW_VISIBLE: Int
expect val GLFW_DECORATED: Int
expect val GLFW_AUTO_ICONIFY: Int
expect val GLFW_FLOATING: Int
expect val GLFW_MAXIMIZED: Int
expect val GLFW_CENTER_CURSOR: Int
expect val GLFW_TRANSPARENT_FRAMEBUFFER: Int
expect val GLFW_HOVERED: Int
expect val GLFW_CURSOR: Int
expect val GLFW_STICKY_KEYS: Int
expect val GLFW_STICKY_MOUSE_BUTTONS: Int
expect val GLFW_LOCK_KEY_MODS: Int
expect val GLFW_CURSOR_NORMAL: Int
expect val GLFW_CURSOR_HIDDEN: Int
expect val GLFW_CURSOR_DISABLED: Int
expect val GLFW_ARROW_CURSOR: Int
expect val GLFW_IBEAM_CURSOR: Int
expect val GLFW_CROSSHAIR_CURSOR: Int
expect val GLFW_HAND_CURSOR: Int
expect val GLFW_HRESIZE_CURSOR: Int
expect val GLFW_VRESIZE_CURSOR: Int
expect val GLFW_CONNECTED: Int
expect val GLFW_DISCONNECTED: Int
expect val GLFW_JOYSTICK_HAT_BUTTONS: Int
expect val GLFW_COCOA_CHDIR_RESOURCES: Int
expect val GLFW_COCOA_MENUBAR: Int
expect val GLFW_DONT_CARE: Int
expect val GLFW_RED_BITS: Int
expect val GLFW_GREEN_BITS: Int
expect val GLFW_BLUE_BITS: Int
expect val GLFW_ALPHA_BITS: Int
expect val GLFW_DEPTH_BITS: Int
expect val GLFW_STENCIL_BITS: Int
expect val GLFW_ACCUM_RED_BITS: Int
expect val GLFW_ACCUM_GREEN_BITS: Int
expect val GLFW_ACCUM_BLUE_BITS: Int
expect val GLFW_ACCUM_ALPHA_BITS: Int
expect val GLFW_AUX_BUFFERS: Int
expect val GLFW_STEREO: Int
expect val GLFW_SAMPLES: Int
expect val GLFW_SRGB_CAPABLE: Int
expect val GLFW_REFRESH_RATE: Int
expect val GLFW_DOUBLEBUFFER: Int
expect val GLFW_CLIENT_API: Int
expect val GLFW_CONTEXT_VERSION_MAJOR: Int
expect val GLFW_CONTEXT_VERSION_MINOR: Int
expect val GLFW_CONTEXT_REVISION: Int
expect val GLFW_CONTEXT_ROBUSTNESS: Int
expect val GLFW_OPENGL_FORWARD_COMPAT: Int
expect val GLFW_OPENGL_DEBUG_CONTEXT: Int
expect val GLFW_OPENGL_PROFILE: Int
expect val GLFW_CONTEXT_RELEASE_BEHAVIOR: Int
expect val GLFW_CONTEXT_NO_ERROR: Int
expect val GLFW_CONTEXT_CREATION_API: Int
expect val GLFW_COCOA_RETINA_FRAMEBUFFER: Int
expect val GLFW_COCOA_FRAME_NAME: Int
expect val GLFW_COCOA_GRAPHICS_SWITCHING: Int
expect val GLFW_X11_CLASS_NAME: Int
expect val GLFW_X11_INSTANCE_NAME: Int
expect val GLFW_NO_API: Int
expect val GLFW_OPENGL_API: Int
expect val GLFW_OPENGL_ES_API: Int
expect val GLFW_NO_ROBUSTNESS: Int
expect val GLFW_NO_RESET_NOTIFICATION: Int
expect val GLFW_LOSE_CONTEXT_ON_RESET: Int
expect val GLFW_OPENGL_ANY_PROFILE: Int
expect val GLFW_OPENGL_CORE_PROFILE: Int
expect val GLFW_OPENGL_COMPAT_PROFILE: Int
expect val GLFW_ANY_RELEASE_BEHAVIOR: Int
expect val GLFW_RELEASE_BEHAVIOR_FLUSH: Int
expect val GLFW_RELEASE_BEHAVIOR_NONE: Int
expect val GLFW_NATIVE_CONTEXT_API: Int
expect val GLFW_EGL_CONTEXT_API: Int
expect val GLFW_OSMESA_CONTEXT_API: Int

expect fun glfwInit(): Boolean
expect fun glfwTerminate()
expect fun glfwInitHint(hint: Int, value: Int)

expect fun glfwGetVersion(major: IntArray?, minor: IntArray?, rev: IntArray?)

expect fun glfwGetVersionString(): String

expect fun glfwGetError(description: Array<String?>?): Int

expect fun glfwSetErrorCallback(cbfun: GLFWErrorCallback?): GLFWErrorCallback?
expect fun glfwGetMonitors(): GLFWMonitorBuffer?
expect fun glfwGetPrimaryMonitor(): GLFWMonitor
expect fun glfwGetMonitorPos(
    monitor: GLFWMonitor, xpos: IntArray?, ypos: IntArray?
)

expect fun glfwGetMonitorPhysicalSize(
    monitor: GLFWMonitor, widthMM: IntArray?, heightMM: IntArray?
)

expect fun glfwGetMonitorContentScale(
    monitor: GLFWMonitor, xscale: FloatArray?, yscale: FloatArray?
)

expect fun glfwGetMonitorName(monitor: GLFWMonitor): String?
expect fun glfwSetMonitorUserPointer(monitor: GLFWMonitor, pointer: Long)
expect fun glfwGetMonitorUserPointer(monitor: GLFWMonitor): Long

expect fun glfwSetMonitorCallback(
    cbfun: GLFWMonitorCallback?
): GLFWMonitorCallback?

expect fun glfwGetVideoModes(monitor: GLFWMonitor): GLFWVidModeBuffer?
expect fun glfwGetVideoMode(monitor: GLFWMonitor): GLFWVidMode?
expect fun glfwSetGamma(monitor: GLFWMonitor, gamma: Float)
expect fun glfwGetGammaRamp(monitor: GLFWMonitor): GLFWGammaRamp?
expect fun glfwSetGammaRamp(monitor: GLFWMonitor, ramp: GLFWGammaRamp)
expect fun glfwDefaultWindowHints()
expect fun glfwWindowHint(hint: Int, value: Int)
expect fun glfwWindowHintString(hint: Int, value: String)
expect fun glfwCreateWindow(
    width: Int, height: Int, title: String, monitor: GLFWMonitor, share: Long
): GLFWWindow

expect fun glfwDestroyWindow(window: GLFWWindow)
expect fun glfwWindowShouldClose(window: GLFWWindow): Boolean
expect fun glfwSetWindowTitle(window: GLFWWindow, title: String)
expect fun glfwSetWindowIcon(window: GLFWWindow, images: GLFWImageBuffer)
expect fun glfwGetWindowPos(
    window: GLFWWindow, xpos: IntArray?, ypos: IntArray?
)

expect fun glfwSetWindowPos(window: GLFWWindow, xpos: Int, ypos: Int)

expect fun glfwGetWindowSize(
    window: GLFWWindow, width: IntArray?, height: IntArray?
)

expect fun glfwSetWindowSizeLimits(
    window: GLFWWindow, minwidth: Int,
    minheight: Int,
    maxwidth: Int,
    maxheight: Int
)

expect fun glfwSetWindowAspectRatio(window: GLFWWindow, numer: Int, denom: Int)
expect fun glfwSetWindowSize(window: GLFWWindow, width: Int, height: Int)
expect fun glfwGetFramebufferSize(
    window: GLFWWindow, width: IntArray?, height: IntArray?
)

expect fun glfwGetWindowFrameSize(
    window: GLFWWindow,
    left: IntArray?, top: IntArray?,
    right: IntArray?, bottom: IntArray?
)

expect fun glfwGetWindowContentScale(
    window: GLFWWindow, xscale: FloatArray?, yscale: FloatArray?
)

expect fun glfwGetWindowOpacity(window: GLFWWindow): Float
expect fun glfwSetWindowOpacity(window: GLFWWindow, opacity: Float)
expect fun glfwIconifyWindow(window: GLFWWindow)
expect fun glfwRestoreWindow(window: GLFWWindow)
expect fun glfwMaximizeWindow(window: GLFWWindow)
expect fun glfwShowWindow(window: GLFWWindow)
expect fun glfwHideWindow(window: GLFWWindow)
expect fun glfwFocusWindow(window: GLFWWindow)
expect fun glfwRequestWindowAttention(window: GLFWWindow)
expect fun glfwGetWindowMonitor(window: GLFWWindow): GLFWMonitor
expect fun glfwSetWindowMonitor(
    window: GLFWWindow, monitor: GLFWMonitor,
    xpos: Int, ypos: Int, width: Int, height: Int, refreshRate: Int
)

expect fun glfwGetWindowAttrib(window: GLFWWindow, attrib: Int): Int
expect fun glfwSetWindowAttrib(window: GLFWWindow, attrib: Int, value: Int)
expect fun glfwSetWindowUserPointer(window: GLFWWindow, pointer: Long)
expect fun glfwGetWindowUserPointer(window: GLFWWindow): Long
expect fun glfwSetWindowPosCallback(
    window: GLFWWindow, cbfun: GLFWWindowPosCallback?
): GLFWWindowPosCallback?

expect fun glfwSetWindowSizeCallback(
    window: GLFWWindow, cbfun: GLFWWindowSizeCallback?
): GLFWWindowSizeCallback?

expect fun glfwSetWindowCloseCallback(
    window: GLFWWindow, cbfun: GLFWWindowCloseCallback?
): GLFWWindowCloseCallback?

expect fun glfwSetWindowRefreshCallback(
    window: GLFWWindow, cbfun: GLFWWindowRefreshCallback?
): GLFWWindowRefreshCallback?

expect fun glfwSetWindowFocusCallback(
    window: GLFWWindow, cbfun: GLFWWindowFocusCallback?
): GLFWWindowFocusCallback?

expect fun glfwSetWindowIconifyCallback(
    window: GLFWWindow, cbfun: GLFWWindowIconifyCallback?
): GLFWWindowIconifyCallback?

expect fun glfwSetWindowMaximizeCallback(
    window: GLFWWindow, cbfun: GLFWWindowMaximizeCallback?
): GLFWWindowMaximizeCallback?

expect fun glfwSetFramebufferSizeCallback(
    window: GLFWWindow, cbfun: GLFWFramebufferSizeCallback?
): GLFWFramebufferSizeCallback?

expect fun glfwSetWindowContentScaleCallback(
    window: GLFWWindow, cbfun: GLFWWindowContentScaleCallback?
): GLFWWindowContentScaleCallback?

expect fun glfwPollEvents()
expect fun glfwWaitEvents()
expect fun glfwWaitEventsTimeout(timeout: Double)
expect fun glfwPostEmptyEvent()
expect fun glfwGetInputMode(window: GLFWWindow, mode: Int): Int
expect fun glfwSetInputMode(window: GLFWWindow, mode: Int, value: Int)
expect fun glfwGetKeyName(key: Int, scancode: Int): String?
expect fun glfwGetKeyScancode(key: Int): Int
expect fun glfwGetKey(window: GLFWWindow, key: Int): Int
expect fun glfwGetMouseButton(window: GLFWWindow, button: Int): Int
expect fun glfwGetCursorPos(
    window: GLFWWindow, xpos: DoubleArray?, ypos: DoubleArray?
)

expect fun glfwSetCursorPos(window: GLFWWindow, xpos: Double, ypos: Double)
expect fun glfwCreateCursor(image: GLFWImage, xhot: Int, yhot: Int): GLFWCursor
expect fun glfwCreateStandardCursor(shape: Int): GLFWCursor
expect fun glfwDestroyCursor(cursor: GLFWCursor)
expect fun glfwSetCursor(window: GLFWWindow, cursor: GLFWCursor)
expect fun glfwSetKeyCallback(
    window: GLFWWindow, cbfun: GLFWKeyCallback?
): GLFWKeyCallback?

expect fun glfwSetCharCallback(
    window: GLFWWindow, cbfun: GLFWCharCallback?
): GLFWCharCallback?

expect fun glfwSetCharModsCallback(
    window: GLFWWindow, cbfun: GLFWCharModsCallback?
): GLFWCharModsCallback?

expect fun glfwSetMouseButtonCallback(
    window: GLFWWindow, cbfun: GLFWMouseButtonCallback?
): GLFWMouseButtonCallback?

expect fun glfwSetCursorPosCallback(
    window: GLFWWindow, cbfun: GLFWCursorPosCallback?
): GLFWCursorPosCallback?

expect fun glfwSetCursorEnterCallback(
    window: GLFWWindow, cbfun: GLFWCursorEnterCallback?
): GLFWCursorEnterCallback?

expect fun glfwSetScrollCallback(
    window: GLFWWindow, cbfun: GLFWScrollCallback?
): GLFWScrollCallback?

expect fun glfwSetDropCallback(
    window: GLFWWindow, cbfun: GLFWDropCallback?
): GLFWDropCallback?

expect fun glfwJoystickPresent(jid: Int): Boolean
expect fun glfwGetJoystickAxes(jid: Int): FloatsRO?
expect fun glfwGetJoystickButtons(jid: Int): BytesRO?
expect fun glfwGetJoystickHats(jid: Int): BytesRO?
expect fun glfwGetJoystickName(jid: Int): String?
expect fun glfwGetJoystickGUID(jid: Int): String?
expect fun glfwSetJoystickUserPointer(jid: Int, pointer: Long)
expect fun glfwGetJoystickUserPointer(jid: Int): Long
expect fun glfwJoystickIsGamepad(jid: Int): Boolean
expect fun glfwSetJoystickCallback(cbfun: GLFWJoystickCallback?): GLFWJoystickCallback?
expect fun glfwUpdateGamepadMappings(string: String): Boolean
expect fun glfwGetGamepadName(jid: Int): String?
expect fun glfwGetGamepadState(jid: Int, state: GLFWGamepadState): Boolean
expect fun glfwSetClipboardString(window: GLFWWindow, string: String)
expect fun glfwGetClipboardString(window: GLFWWindow): String?
expect fun glfwGetTime(): Double
expect fun glfwSetTime(time: Double)
expect fun glfwGetTimerValue(): Long
expect fun glfwGetTimerFrequency(): Long
expect fun glfwMakeContextCurrent(window: GLFWWindow)
expect fun glfwGetCurrentContext(): GLFWWindow
expect fun glfwSwapBuffers(window: GLFWWindow)
expect fun glfwSwapInterval(interval: Int)
