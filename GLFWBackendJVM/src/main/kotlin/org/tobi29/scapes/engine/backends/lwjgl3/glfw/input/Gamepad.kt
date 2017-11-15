package org.tobi29.scapes.engine.backends.lwjgl3.glfw.input

import org.tobi29.scapes.engine.input.ControllerButtons
import org.tobi29.scapes.engine.input.ControllerGamepad
import org.tobi29.scapes.engine.input.ControllerKey
import org.tobi29.scapes.engine.input.now
import org.tobi29.scapes.engine.utils.*

internal class GLFWControllerGamepad(override val name: String,
                                     axisCount: Int) : ControllerGamepad() {
    private val pressedMut = ConcurrentHashSet<ControllerKey>()
    override val pressed = pressedMut.readOnly()
    override val axes = DoubleArray(axisCount).sliceOver()
    private val lastActiveMut = AtomicLong(Long.MIN_VALUE)
    override val lastActive get() = lastActiveMut.get()

    override fun isDown(key: ControllerKey) = key in pressedMut

    internal fun addPressEvent(key: ControllerKey,
                               action: ControllerButtons.Action,
                               events: EventDispatcher) {
        lastActiveMut.set(steadyClock.timeSteadyNanos())
        synchronized(this) {
            when (action) {
                ControllerButtons.Action.PRESS -> pressedMut.add(key)
                ControllerButtons.Action.RELEASE -> pressedMut.remove(key)
            }
            events.fire(ControllerButtons.PressEvent(now(), key, action))
        }
    }

    internal fun setAxis(axis: Int,
                         value: Double,
                         events: EventDispatcher) {
        synchronized(this) {
            if (axes[axis] < 0.5 && value >= 0.5) {
                lastActiveMut.set(steadyClock.timeSteadyNanos())
                ControllerKey.axis(axis)?.let {
                    addPressEvent(it, ControllerButtons.Action.PRESS,
                            events)
                }
            } else if (axes[axis] >= 0.5 && value < 0.5) {
                lastActiveMut.set(steadyClock.timeSteadyNanos())
                ControllerKey.axis(axis)?.let {
                    addPressEvent(it, ControllerButtons.Action.RELEASE,
                            events)
                }
            }
            if (axes[axis] > -0.5 && value <= -0.5) {
                lastActiveMut.set(steadyClock.timeSteadyNanos())
                ControllerKey.axisNegative(axis)?.let {
                    addPressEvent(it, ControllerButtons.Action.PRESS,
                            events)
                }
            } else if (axes[axis] <= -0.5 && value > -0.5) {
                lastActiveMut.set(steadyClock.timeSteadyNanos())
                ControllerKey.axisNegative(axis)?.let {
                    addPressEvent(it, ControllerButtons.Action.RELEASE,
                            events)
                }
            }
            axes[axis] = value
        }
    }
}
