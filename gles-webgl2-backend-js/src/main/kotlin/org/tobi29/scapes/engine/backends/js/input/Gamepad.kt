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

package org.tobi29.scapes.engine.backends.js.input

import org.tobi29.arrays.sliceOver
import org.tobi29.scapes.engine.backends.js.DOMKeyMap
import org.tobi29.scapes.engine.input.ControllerButtons
import org.tobi29.scapes.engine.input.ControllerJoystick
import org.tobi29.scapes.engine.input.ControllerKey
import org.tobi29.scapes.engine.input.now
import org.tobi29.stdex.readOnly
import org.tobi29.utils.EventDispatcher
import org.tobi29.utils.steadyClock
import org.w3c.gamepad.Gamepad

internal fun Gamepad.createWrapper(): WebJoystick =
    when (mapping) {
        "standard" -> WebStandardGamepad(this)
        else -> WebJoystick(this)
    }

internal class WebStandardGamepad(
    gamepad: Gamepad
) : WebJoystick(gamepad, 6) {
    override val type get() = Type.GAMEPAD

    override fun setButton(
        button: Int,
        value: Boolean,
        events: EventDispatcher
    ) {
        DOMKeyMap.standardGamepad(button)
            ?.let { setButton(it, value, events) }
    }

    override fun setAnalogButton(
        axis: Int,
        value: Double,
        events: EventDispatcher
    ) {
        DOMKeyMap.standardGamepadAnalog(axis)
            ?.let { setRawAxis(it, value, events) }
    }

    override fun setAxis(
        axis: Int,
        value: Double,
        events: EventDispatcher
    ) {
        DOMKeyMap.standardGamepadAxis(axis)
            ?.let { setRawAxis(it, value, events) }
    }
}

internal open class WebJoystick(
    gamepad: Gamepad,
    axisCount: Int = gamepad.axes.size
) : ControllerJoystick() {
    private val index = gamepad.index
    override val name = gamepad.id
    private val _pressed = HashSet<ControllerKey>()
    final override var lastActive = Long.MIN_VALUE
        private set

    override val axes = DoubleArray(axisCount).sliceOver()

    override val pressed get() = _pressed.readOnly()

    override fun isDown(key: ControllerKey) = key in _pressed

    internal fun poll(gamepads: Array<Gamepad?>, events: EventDispatcher) {
        val gamepad = gamepads[index] ?: return
        for ((i, button) in gamepad.buttons.withIndex()) {
            setButton(i, button.pressed, events)
            setAnalogButton(i, button.value, events)
        }
        for ((i, axis) in gamepad.axes.withIndex()) {
            setAxis(i, axis, events)
        }
    }

    internal open fun setButton(
        button: Int,
        value: Boolean,
        events: EventDispatcher
    ) {
        ControllerKey.button(button)?.let { setButton(it, value, events) }
    }

    internal fun setButton(
        key: ControllerKey,
        value: Boolean,
        events: EventDispatcher
    ) {
        if (value) {
            if (_pressed.add(key)) {
                lastActive = steadyClock.timeSteadyNanos()
                events.fire(
                    ControllerButtons.PressEvent(
                        now(), key,
                        ControllerButtons.Action.PRESS
                    )
                )
            }
        } else {
            if (_pressed.remove(key)) {
                lastActive = steadyClock.timeSteadyNanos()
                events.fire(
                    ControllerButtons.PressEvent(
                        now(), key,
                        ControllerButtons.Action.RELEASE
                    )
                )
            }
        }
    }

    internal open fun setAnalogButton(
        axis: Int,
        value: Double,
        events: EventDispatcher
    ) {
    }

    internal open fun setAxis(
        axis: Int,
        value: Double,
        events: EventDispatcher
    ) {
        setRawAxis(axis, value, events)
    }

    internal fun setRawAxis(
        axis: Int,
        value: Double,
        events: EventDispatcher
    ) {
        if (axis < 0) return
        when {
            value >= 0.5 -> ControllerKey.axis(axis)?.let {
                setButton(it, true, events)
            }
            value <= -0.5 -> ControllerKey.axisNegative(axis)?.let {
                setButton(it, true, events)
            }
            else -> {
                ControllerKey.axis(axis)?.let {
                    setButton(it, false, events)
                }
                ControllerKey.axisNegative(axis)?.let {
                    setButton(it, false, events)
                }
            }
        }
        axes[axis] = value
    }
}
