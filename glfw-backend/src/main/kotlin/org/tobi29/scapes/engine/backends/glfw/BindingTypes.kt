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

expect class GLFWMonitor

expect class GLFWMonitorBuffer

expect val GLFWMonitorBuffer.size: Int
expect operator fun GLFWMonitorBuffer.get(index: Int): GLFWMonitor

expect val GLFWMonitor_EMPTY: GLFWMonitor

expect class GLFWWindow

expect val GLFWWindow_EMPTY: GLFWWindow

expect class GLFWVidMode

expect val GLFWVidMode.width: Int
expect val GLFWVidMode.height: Int
expect val GLFWVidMode.refreshRate: Int

expect class GLFWVidModeBuffer
expect val GLFWVidModeBuffer.size: Int
expect operator fun GLFWVidModeBuffer.get(index: Int): GLFWVidMode

expect class GLFWGammaRamp

expect class GLFWGamepadState

expect fun GLFWGamepadState(): GLFWGamepadState
expect val GLFWGamepadState.axes: FloatsRO
expect val GLFWGamepadState.buttons: BytesRO
expect fun GLFWGamepadState.close()

expect class GLFWImage
expect class GLFWImageBuffer
expect val GLFWImageBuffer.size: Int
expect operator fun GLFWImageBuffer.get(index: Int): GLFWImage


expect class GLFWCursor

expect val GLFWCursor_EMPTY: GLFWCursor
