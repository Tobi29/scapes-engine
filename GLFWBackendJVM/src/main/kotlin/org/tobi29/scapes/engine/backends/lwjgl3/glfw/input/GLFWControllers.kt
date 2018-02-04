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
import org.lwjgl.glfw.GLFWGamepadState
import org.lwjgl.glfw.GLFWJoystickCallback
import org.tobi29.logging.KLogging
import org.tobi29.scapes.engine.backends.lwjgl3.stackFrame
import org.tobi29.scapes.engine.input.Controller
import org.tobi29.utils.EventDispatcher
import java.nio.ByteBuffer
import java.nio.FloatBuffer

internal class GLFWControllers(
    private val events: EventDispatcher,
    private val joysticks: MutableMap<Int, GLFWControllerJoystick>
) {
    private val joystickFun = GLFWJoystickCallback.create { jid, event ->
        when (event) {
            GLFW.GLFW_CONNECTED -> connected(jid)
            GLFW.GLFW_DISCONNECTED -> disconnected(jid)
        }
    }

    fun init() {
        for (jid in GLFW.GLFW_JOYSTICK_1..GLFW.GLFW_JOYSTICK_LAST) {
            if (GLFW.glfwJoystickPresent(jid)) {
                connected(jid)
            }
        }
        GLFW.glfwSetJoystickCallback(joystickFun)
    }

    fun poll() {
        stackFrame { stack ->
            for ((jid, joystick) in joysticks) {
                if (joystick is GLFWControllerGamepad) {
                    val state = GLFWGamepadState.mallocStack(stack)
                    GLFW.glfwGetGamepadState(jid, state)
                    updateState(joystick, state.axes(), state.buttons())
                } else {
                    val axes = GLFW.glfwGetJoystickAxes(jid)
                            ?: throw IllegalArgumentException("Failed getting axes of joystick")
                    val buttons = GLFW.glfwGetJoystickButtons(jid)
                            ?: throw IllegalArgumentException("Failed getting buttons of joystick")
                    updateState(joystick, axes, buttons)
                }
            }
        }
    }

    fun dispose() {
        joystickFun.close()
        for (jid in joysticks.keys) {
            disconnected(jid)
        }
    }

    private fun connected(jid: Int) {
        val joystick = if (GLFW.glfwJoystickIsGamepad(jid)) {
            val name = GLFW.glfwGetGamepadName(jid)
                    ?: throw IllegalArgumentException("Failed getting name of gamepad")
            val gamepad =
                GLFWControllerGamepad(name, GLFW.GLFW_GAMEPAD_AXIS_LAST + 1)
            logger.info { "Connected gamepad $jid \"${gamepad.name}\"" }
            gamepad
        } else {
            val name = GLFW.glfwGetJoystickName(jid)
                    ?: throw IllegalArgumentException("Failed getting name of joystick")
            val axes = GLFW.glfwGetJoystickAxes(jid)
                    ?: throw IllegalArgumentException("Failed getting axes of joystick")
            val joystick = GLFWControllerJoystick(name, axes.capacity())
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
        axes: FloatBuffer,
        buttons: ByteBuffer
    ) {
        var i = 0
        while (axes.hasRemaining()) {
            joystick.setAxis(
                i, deadzones(axes.get().toDouble()), events
            )
            i++
        }
        i = 0
        while (buttons.hasRemaining()) {
            val value = buttons.get().toInt() == GLFW.GLFW_PRESS
            joystick.setButton(i, value, events)
            i++
        }
    }

    companion object : KLogging()
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

