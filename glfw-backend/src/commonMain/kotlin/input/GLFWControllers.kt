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

package org.tobi29.scapes.engine.backends.glfw.input

import net.gitout.ktbindings.glfw.*
import net.gitout.ktbindings.utils.ByteReadBuffer
import net.gitout.ktbindings.utils.FloatReadBuffer
import net.gitout.ktbindings.utils.get
import net.gitout.ktbindings.utils.size
import org.tobi29.logging.KLogger
import org.tobi29.scapes.engine.input.Controller
import org.tobi29.utils.EventDispatcher

internal class GLFWControllers(
    private val events: EventDispatcher,
    private val joysticks: MutableMap<Int, GLFWControllerJoystick>
) {
    private val joystickFun =
        GLFWJoystickCallback { jid, event ->
            when (event) {
                GLFW_CONNECTED -> connected(
                    jid
                )
                GLFW_DISCONNECTED -> disconnected(
                    jid
                )
            }
        }
    private var state: GLFWGamepadState? = null

    fun init() {
        for (jid in GLFW_JOYSTICK_1..GLFW_JOYSTICK_LAST) {
            if (glfwJoystickPresent(jid)) {
                connected(jid)
            }
        }
        glfwSetJoystickCallback(
            joystickFun
        )
        state = GLFWGamepadState()
    }

    fun poll() {
        val state = state!!
        for ((jid, joystick) in joysticks) {
            if (joystick is GLFWControllerGamepad) {
                glfwGetGamepadState(
                    jid,
                    state
                )
                updateState(joystick, state.axes, state.buttons)
            } else {
                val axes = glfwGetJoystickAxes(
                    jid
                )
                        ?: throw IllegalArgumentException("Failed getting axes of joystick")
                val buttons = glfwGetJoystickButtons(
                    jid
                )
                        ?: throw IllegalArgumentException("Failed getting buttons of joystick")
                updateState(joystick, axes, buttons)
            }
        }
    }

    fun dispose() {
        joystickFun.close()
        for (jid in joysticks.keys) {
            disconnected(jid)
        }
        state?.close()
        state = null
    }

    private fun connected(jid: Int) {
        val joystick = if (glfwJoystickIsGamepad(
                jid
            )) {
            val name = glfwGetGamepadName(
                jid
            )
                    ?: throw IllegalArgumentException("Failed getting name of gamepad")
            val gamepad =
                GLFWControllerGamepad(
                    name,
                    GLFW_GAMEPAD_AXIS_LAST + 1
                )
            logger.info { "Connected gamepad $jid \"${gamepad.name}\"" }
            gamepad
        } else {
            val name = glfwGetJoystickName(
                jid
            )
                    ?: throw IllegalArgumentException("Failed getting name of joystick")
            val axes = glfwGetJoystickAxes(
                jid
            )
                    ?: throw IllegalArgumentException("Failed getting axes of joystick")
            val joystick =
                GLFWControllerJoystick(
                    name,
                    axes.size
                )
            logger.info { "Connected joystick $jid \"${joystick.name}\" with ${joystick.axes.size} axes" }
            joystick
        }
        val oldJoystick = joysticks.put(jid, joystick)
        if (oldJoystick != null) {
            logger.warn { "Duplicate joystick connect on $jid" }
        }
        events.fire(Controller.AddEvent(joystick))
    }

    private fun disconnected(jid: Int) {
        val joystick = joysticks.remove(jid)
        if (joystick == null) {
            logger.warn { "Duplicate joystick disconnect on $jid" }
        } else {
            if (joystick is GLFWControllerGamepad) {
                logger.info { "Disconnected gamepad $jid \"${joystick.name}\"" }
            } else {
                logger.info { "Disconnected joystick $jid \"${joystick.name}\"" }
            }
            events.fire(Controller.RemoveEvent(joystick))
        }
    }

    private fun updateState(
        joystick: GLFWControllerJoystick,
        axes: FloatReadBuffer,
        buttons: ByteReadBuffer
    ) {
        for (i in 0 until axes.size) {
            joystick.setAxis(i, deadzones(axes[i].toDouble()), events)
        }
        for (i in 0 until buttons.size) {
            val value = (buttons[i].toInt() and 0xFF) == GLFW_PRESS
            joystick.setButton(i, value, events)
        }
    }

    companion object {
        private val logger = KLogger<GLFWControllers>()
    }
}

private const val DEADZONES = 0.05
private const val DEADZONES_SCALE = 0.95

private fun deadzones(value: Double): Double {
    if (value > DEADZONES) {
        return (value - DEADZONES) / DEADZONES_SCALE
    } else if (value < -DEADZONES) {
        return (value + DEADZONES) / DEADZONES_SCALE
    }
    return 0.0
}

