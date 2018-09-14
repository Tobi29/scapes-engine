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

package org.tobi29.scapes.engine.backends.glfw.input

import org.tobi29.arrays.sliceOver
import org.tobi29.scapes.engine.input.ControllerButtons
import org.tobi29.scapes.engine.input.ControllerJoystick
import org.tobi29.scapes.engine.input.ControllerKey
import org.tobi29.scapes.engine.input.now
import org.tobi29.stdex.ConcurrentHashSet
import org.tobi29.stdex.atomic.AtomicLong
import org.tobi29.stdex.readOnly
import org.tobi29.utils.EventDispatcher
import org.tobi29.utils.steadyClock

internal open class GLFWControllerJoystick(
    final override val name: String,
    axisCount: Int
) : ControllerJoystick() {
    private val pressedMut = ConcurrentHashSet<ControllerKey>()
    final override val pressed = pressedMut.readOnly()
    final override val axes = DoubleArray(axisCount).sliceOver()
    private val lastActiveMut = AtomicLong(Long.MIN_VALUE)
    final override val lastActive get() = lastActiveMut.get()

    final override fun isDown(key: ControllerKey) = key in pressedMut

    internal open fun setAxis(
        axis: Int,
        value: Double,
        events: EventDispatcher
    ) {
        if (axis < 0) return
        synchronized(this) {
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
        synchronized(this) {
            if (value) {
                if (pressedMut.add(key)) {
                    lastActiveMut.set(steadyClock.timeSteadyNanos())
                    events.fire(
                        ControllerButtons.PressEvent(
                            now(), key,
                            ControllerButtons.Action.PRESS
                        )
                    )
                }
            } else {
                if (pressedMut.remove(key)) {
                    lastActiveMut.set(steadyClock.timeSteadyNanos())
                    events.fire(
                        ControllerButtons.PressEvent(
                            now(), key,
                            ControllerButtons.Action.RELEASE
                        )
                    )
                }
            }
        }
    }
}

internal class GLFWControllerGamepad(
    name: String,
    axisCount: Int
) : GLFWControllerJoystick(name, axisCount) {
    override val type: Type get() = Type.GAMEPAD

    override fun setAxis(
        axis: Int,
        value: Double,
        events: EventDispatcher
    ) {
        glfwGamepadAxis(axis).let {
            if (it >= 0) super.setAxis(it, value, events)
        }
    }

    override fun setButton(
        button: Int,
        value: Boolean,
        events: EventDispatcher
    ) {
        glfwGamepadButton(button)?.let {
            setButton(it, value, events)
        }
    }
}
