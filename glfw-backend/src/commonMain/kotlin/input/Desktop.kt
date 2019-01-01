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

import org.tobi29.math.vector.Vector2d
import org.tobi29.platform.PLATFORM
import org.tobi29.platform.Platform
import org.tobi29.scapes.engine.input.*
import org.tobi29.stdex.ConcurrentHashSet
import org.tobi29.stdex.atomic.AtomicLong
import org.tobi29.stdex.concurrent.ReentrantLock
import org.tobi29.stdex.concurrent.withLock
import org.tobi29.stdex.readOnly
import org.tobi29.utils.EventDispatcher
import org.tobi29.utils.steadyClock

internal class GLFWControllerDesktop : ControllerDesktop() {
    private val lock = ReentrantLock()
    private val pressedMut = ConcurrentHashSet<ControllerKey>()
    override val pressed = pressedMut.readOnly()
    override var x = 0.0
        private set
    override var y = 0.0
        private set
    private val lastActiveMut = AtomicLong(Long.MIN_VALUE)
    override val lastActive get() = lastActiveMut.get()

    override val name = "Default"

    private val superModifier = PLATFORM == Platform.MACOS

    override val isModifierDown
        get() = run {
            if (superModifier) {
                isDown(ControllerKey.KEY_SUPER_LEFT) || isDown(
                    ControllerKey.KEY_SUPER_RIGHT
                )
            } else {
                isDown(ControllerKey.KEY_CONTROL_LEFT) || isDown(
                    ControllerKey.KEY_CONTROL_RIGHT
                )
            }
        }

    override fun isDown(key: ControllerKey) = key in pressedMut

    internal fun addPressEvent(
        key: ControllerKey,
        action: ControllerButtons.Action,
        events: EventDispatcher
    ) {
        lastActiveMut.set(steadyClock.timeSteadyNanos())
        lock.withLock {
            when (action) {
                ControllerButtons.Action.PRESS -> pressedMut.add(key)
                ControllerButtons.Action.RELEASE -> pressedMut.remove(key)
            }
            events.fire(ControllerButtons.PressEvent(now(), key, action))
        }
    }

    internal fun addTypeEvent(
        character: Char,
        events: EventDispatcher
    ) {
        lastActiveMut.set(steadyClock.timeSteadyNanos())
        events.fire(ControllerKeyboard.TypeEvent(now(), character))
    }

    internal fun set(
        x: Double,
        y: Double
    ) {
        this.x = x
        this.y = y
    }

    internal fun addDelta(
        x: Double,
        y: Double,
        events: EventDispatcher
    ) {
        if (x != 0.0 && y != 0.0)
            lastActiveMut.set(steadyClock.timeSteadyNanos())
        events.fire(ControllerMouse.DeltaEvent(now(), Vector2d(x, y)))
    }

    internal fun addScroll(
        x: Double,
        y: Double,
        events: EventDispatcher
    ) {
        if (x != 0.0 && y != 0.0)
            lastActiveMut.set(steadyClock.timeSteadyNanos())
        val delta = ScrollDelta.Line(Vector2d(x, y))
        keyPressesForScroll(delta) { key, action ->
            addPressEvent(key, action, events)
        }
        events.fire(
            ControllerMouse.ScrollEvent(
                now(),
                ScrollDelta.Line(Vector2d(x, y))
            )
        )
    }

    internal fun clearStates() {
        lock.withLock {
            pressedMut.clear()
        }
    }
}
