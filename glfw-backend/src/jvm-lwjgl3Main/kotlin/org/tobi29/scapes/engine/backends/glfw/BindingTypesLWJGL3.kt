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

@file:Suppress("NOTHING_TO_INLINE", "EXTENSION_SHADOWED_BY_MEMBER")

package org.tobi29.scapes.engine.backends.glfw

import org.lwjgl.PointerBuffer
import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.FloatsRO
import org.tobi29.io.viewSliceE

actual typealias GLFWMonitor = Long
actual typealias GLFWMonitorBuffer = PointerBuffer

actual inline val GLFWMonitorBuffer.size get() = remaining()
actual inline operator fun GLFWMonitorBuffer.get(index: Int) =
    get(position() + index)

actual inline val GLFWMonitor_EMPTY: GLFWMonitor get() = 0L

actual typealias GLFWWindow = Long

actual inline val GLFWWindow_EMPTY: GLFWWindow get() = 0L

actual typealias GLFWVidMode = org.lwjgl.glfw.GLFWVidMode

actual inline val GLFWVidMode.width get() = width()
actual inline val GLFWVidMode.height get() = height()
actual inline val GLFWVidMode.refreshRate get() = refreshRate()

actual typealias GLFWVidModeBuffer = org.lwjgl.glfw.GLFWVidMode.Buffer

actual inline val GLFWVidModeBuffer.size get() = remaining()
actual inline operator fun GLFWVidModeBuffer.get(index: Int) =
    get(position() + index)

actual typealias GLFWGammaRamp = org.lwjgl.glfw.GLFWGammaRamp

actual typealias GLFWGamepadState = org.lwjgl.glfw.GLFWGamepadState

actual inline fun GLFWGamepadState() = GLFWGamepadState.create()
actual inline val GLFWGamepadState.axes: FloatsRO get() = axes().asFloats()
actual inline val GLFWGamepadState.buttons: BytesRO get() = buttons().viewSliceE
actual inline fun GLFWGamepadState.close() = close()

actual typealias GLFWImage = org.lwjgl.glfw.GLFWImage
actual typealias GLFWImageBuffer = org.lwjgl.glfw.GLFWImage.Buffer

actual inline val GLFWImageBuffer.size get() = remaining()
actual inline operator fun GLFWImageBuffer.get(index: Int) =
    get(position() + index)

actual typealias GLFWCursor = Long

actual inline val GLFWCursor_EMPTY: GLFWCursor get() = 0L
