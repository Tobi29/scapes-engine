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

package org.tobi29.scapes.engine.backends.lwjgl3.glfw.input

import org.lwjgl.glfw.GLFW
import org.tobi29.scapes.engine.input.Controller
import org.tobi29.scapes.engine.input.ControllerButtons
import org.tobi29.scapes.engine.input.ControllerKey
import org.tobi29.scapes.engine.utils.ConcurrentHashMap
import org.tobi29.scapes.engine.utils.EventDispatcher
import java.nio.ByteBuffer
import java.nio.FloatBuffer

internal class GLFWControllers(
        private val events: EventDispatcher,
        private val virtualJoysticks: MutableMap<Int, GLFWControllerGamepad>
) {
    private val handlers = ConcurrentHashMap<Int, (String, FloatBuffer, ByteBuffer) -> Unit>()

    fun poll(): Boolean {
        var joysticksChanged = false
        for (joystick in GLFW.GLFW_JOYSTICK_1..GLFW.GLFW_JOYSTICK_LAST) {
            if (GLFW.glfwJoystickPresent(joystick)) {
                val name = GLFW.glfwGetJoystickName(joystick)
                val axes = GLFW.glfwGetJoystickAxes(joystick)
                val buttons = GLFW.glfwGetJoystickButtons(joystick)
                val handler = handlers[joystick] ?: run {
                    val virtualJoystick = GLFWControllerGamepad(name,
                            axes.capacity())
                    val states = BooleanArray(buttons.remaining())
                    val handler = { name: String, axes: FloatBuffer, buttons: ByteBuffer ->
                        if (name != virtualJoystick.name ||
                                buttons.remaining() != states.size ||
                                axes.remaining() != virtualJoystick.axes.size) {
                            handlers.remove(joystick)
                        }
                        var i = 0
                        while (axes.hasRemaining()) {
                            virtualJoystick.setAxis(i,
                                    deadzones(
                                            axes.get().toDouble()), events)
                            i++
                        }
                        i = 0
                        while (buttons.hasRemaining()) {
                            val value = buttons.get().toInt() == 1
                            if (states[i] != value) {
                                states[i] = value
                                val button = ControllerKey.button(i)
                                if (button != null) {
                                    virtualJoystick.addPressEvent(button,
                                            if (value)
                                                ControllerButtons.Action.PRESS
                                            else
                                                ControllerButtons.Action.RELEASE,
                                            events)
                                }
                            }
                            i++
                        }
                    }
                    handlers.put(joystick, handler)
                    virtualJoysticks.put(joystick, virtualJoystick)
                    events.fire(Controller.AddEvent(virtualJoystick))
                    joysticksChanged = true
                    handler
                }
                handler(name, axes, buttons)
            } else if (handlers.containsKey(joystick)) {
                handlers.remove(joystick)
                virtualJoysticks.remove(joystick)?.let { virtualJoystick ->
                    events.fire(Controller.RemoveEvent(virtualJoystick))
                }
                joysticksChanged = true
            }
        }
        return joysticksChanged
    }

    companion object {
        private val DEADZONES = 0.05
        private val DEADZONES_SCALE = 0.95

        private fun deadzones(value: Double): Double {
            if (value > DEADZONES) {
                return (value - DEADZONES) / DEADZONES_SCALE
            } else if (value < -DEADZONES) {
                return (value + DEADZONES) / DEADZONES_SCALE
            }
            return 0.0
        }
    }
}

