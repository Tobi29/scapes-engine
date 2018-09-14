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

package org.tobi29.scapes.engine.input

import org.tobi29.arrays.DoublesRO
import org.tobi29.arrays.sliceOver
import org.tobi29.math.vector.MutableVector2d
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.Vector2i
import org.tobi29.math.vector.times
import org.tobi29.stdex.readOnly
import org.tobi29.utils.EventMuteable
import org.tobi29.utils.steadyClock

interface Controller {
    val id: String get() = name.replace(idRemove, "")
    val name: String

    val lastActive: Long
    val isActive get() = steadyClock.timeSteadyNanos() < lastActive + 100000000L

    class AddEvent(val controller: Controller)

    class RemoveEvent(val controller: Controller)
}

interface ControllerState {
    val controller: Controller
}

interface ControllerButtons : Controller {
    val pressed: Set<ControllerKey>

    fun isDown(key: ControllerKey): Boolean

    enum class Action {
        PRESS,
        REPEAT,
        RELEASE
    }

    class PressEvent(val state: ControllerButtonsState,
                     val key: ControllerKey,
                     val action: Action) : EventMuteable {
        override var muted = false
    }
}

interface ControllerButtonsState : ControllerState {
    override val controller: ControllerButtons

    val pressed: Set<ControllerKey>
}

inline fun ControllerButtonsState.isDown(key: ControllerKey): Boolean =
        key in pressed

interface ControllerMouse : Controller {
    val x: Double
    val y: Double

    val position get() = Vector2d(x, y)

    class DeltaEvent(val state: ControllerDesktopState,
                     val delta: Vector2d)

    class ScrollEvent(val state: ControllerDesktopState,
                      val delta: ScrollDelta)
}

sealed class ScrollDelta {
    class Pixel(val delta: Vector2d) : ScrollDelta()

    class Line(val delta: Vector2d) : ScrollDelta()

    class Page(val delta: Vector2d) : ScrollDelta()
}

fun ScrollDelta.pixelDeltaFor(lineSize: Vector2d,
                              lines: Vector2i): Vector2d =
        when (this) {
            is ScrollDelta.Pixel -> delta
            is ScrollDelta.Line -> delta * lineSize
            is ScrollDelta.Page -> Vector2d(
                    delta.x * lineSize.x * lines.x,
                    delta.y * lineSize.y * lines.y)
        }

interface ControllerMouseState : ControllerState {
    override val controller: ControllerMouse

    val position: Vector2d
}

inline val ControllerMouseState.x get() = position.x
inline val ControllerMouseState.y get() = position.y

interface ControllerKeyboard : ControllerButtons {
    val isModifierDown
        get() = isDown(ControllerKey.KEY_CONTROL_LEFT) ||
                isDown(ControllerKey.KEY_CONTROL_RIGHT)

    class TypeEvent(val controller: ControllerDesktopState,
                    val character: Char) : EventMuteable {
        override var muted = false
    }
}

interface ControllerKeyboardState : ControllerButtonsState {
    override val controller: ControllerKeyboard

    val isModifierDown: Boolean
}

interface ControllerAxes : Controller {
    val axes: DoublesRO
}

interface ControllerAxesState : ControllerState {
    override val controller: ControllerAxes

    val axes: DoublesRO
}

interface ControllerTracker : Controller {
    fun fingers(): Sequence<Tracker>

    class Tracker {
        val pos = MutableVector2d()
    }
}

interface ControllerTrackerState : ControllerState {
    override val controller: ControllerTracker

    val fingers: Set<ControllerTracker.Tracker>
}

abstract class ControllerDesktop : ControllerKeyboard,
        ControllerMouse

inline fun ControllerDesktop.now() = ControllerDesktopState(this)

class ControllerDesktopState(
        override val controller: ControllerDesktop
) : ControllerKeyboardState,
        ControllerMouseState {
    override val pressed = controller.pressed.toSet().readOnly()
    override val position = controller.position
    override val isModifierDown = controller.isModifierDown
}

abstract class ControllerJoystick : ControllerAxes,
        ControllerButtons {
    open val type: Type get() = Type.UNKNOWN

    enum class Type {
        UNKNOWN,
        GAMEPAD,
        STEERING_WHEEL,
        JOYSTICK
    }
}

inline fun ControllerJoystick.now() = ControllerJoystickState(this)

class ControllerJoystickState(
        override val controller: ControllerJoystick
) : ControllerAxesState,
        ControllerButtonsState {
    override val pressed = controller.pressed.toSet().readOnly()

    // TODO: Make secure?
    override val axes: DoublesRO = controller.axes.let { axes ->
        DoubleArray(axes.size).sliceOver().also {
            axes.getDoubles(0, it)
        }
    }
}

abstract class ControllerTouch : ControllerTracker

inline fun ControllerTouch.now() = ControllerTouchState(this)

class ControllerTouchState(
        override val controller: ControllerTouch
) : ControllerTrackerState {
    override val fingers = controller.fingers().toSet().readOnly()
}

private val idRemove = "[ /-]".toRegex()
