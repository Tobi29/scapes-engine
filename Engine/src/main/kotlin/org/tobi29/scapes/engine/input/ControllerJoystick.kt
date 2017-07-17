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

package org.tobi29.scapes.engine.input

import org.tobi29.scapes.engine.utils.ConcurrentHashMap
import org.tobi29.scapes.engine.utils.ConcurrentLinkedQueue
import org.tobi29.scapes.engine.utils.EventDispatcher
import org.tobi29.scapes.engine.utils.remove

class ControllerJoystick(private val name: String,
                         axisCount: Int) : ControllerBasic {
    private val id: String
    private val states = ConcurrentHashMap<ControllerKey, KeyState>()
    private val axes: DoubleArray
    private val pressEventQueue = ConcurrentLinkedQueue<ControllerBasic.PressEvent>()
    private var pressEvents: Collection<ControllerBasic.PressEvent> = emptyList()
    override var isActive = false
        private set

    init {
        id = name.replace(REPLACE, "")
        axes = DoubleArray(axisCount)
    }

    fun name(): String {
        return name
    }

    fun id(): String {
        return id
    }

    fun axes(): Int {
        return axes.size
    }

    override fun poll(events: EventDispatcher) {
        synchronized(this) {
            states.forEach {
                when (it.value) {
                    KeyState.PRESSED ->
                        states.replace(it.key, KeyState.PRESSED, KeyState.DOWN)
                    KeyState.RELEASED ->
                        states.remove(it.key, KeyState.RELEASED)
                }
            }
            val newPressEvents = ArrayList<ControllerBasic.PressEvent>()
            isActive = !pressEventQueue.isEmpty()
            while (!pressEventQueue.isEmpty()) {
                val event = pressEventQueue.poll()!!
                val key = event.key
                when (event.state) {
                    ControllerBasic.PressState.PRESS ->
                        states[key] = KeyState.PRESSED
                    ControllerBasic.PressState.RELEASE -> {
                        states.replace(key, KeyState.PRESSED, KeyState.RELEASED)
                        states.remove(key, KeyState.DOWN)
                    }
                }
                newPressEvents.add(event)
            }
            pressEvents = newPressEvents
        }
    }

    override fun isDown(key: ControllerKey): Boolean {
        return states[key] != null
    }

    override fun isPressed(key: ControllerKey): Boolean {
        return (states[key] ?: return false) != KeyState.DOWN
    }

    override fun pressEvents(): Sequence<ControllerBasic.PressEvent> {
        return pressEvents.asSequence()
    }

    override fun addPressEvent(key: ControllerKey,
                               state: ControllerBasic.PressState) {
        pressEventQueue.add(ControllerBasic.PressEvent(key, state))
    }

    fun axis(axis: Int): Double {
        if (axis < 0 || axis >= axes.size) {
            return 0.0
        }
        return axes[axis]
    }

    fun setAxis(axis: Int,
                value: Double) {
        synchronized(this) {
            if (axes[axis] < 0.5 && value >= 0.5) {
                ControllerKey.axis(axis)?.let {
                    addPressEvent(it, ControllerBasic.PressState.PRESS)
                }
            } else if (axes[axis] >= 0.5 && value < 0.5) {
                ControllerKey.axis(axis)?.let {
                    addPressEvent(it, ControllerBasic.PressState.RELEASE)
                }
            }
            if (axes[axis] > -0.5 && value <= -0.5) {
                ControllerKey.axisNegative(axis)?.let {
                    addPressEvent(it, ControllerBasic.PressState.PRESS)
                }
            } else if (axes[axis] <= -0.5 && value > -0.5) {
                ControllerKey.axisNegative(axis)?.let {
                    addPressEvent(it, ControllerBasic.PressState.RELEASE)
                }
            }
            axes[axis] = value
        }
    }

    companion object {
        private val REPLACE = "[ /-]".toRegex()
    }

    private enum class KeyState {
        DOWN,
        PRESSED,
        RELEASED
    }
}
