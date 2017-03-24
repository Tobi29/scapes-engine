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

import org.tobi29.scapes.engine.utils.EventDispatcher
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class ControllerJoystick(private val name: String,
                         axisCount: Int) : ControllerBasic {
    override val events = EventDispatcher()
    private val id: String
    private val states: ByteArray
    private val axes: DoubleArray
    private val pressEventQueue = ConcurrentLinkedQueue<ControllerBasic.PressEvent>()
    private var pressEvents: Collection<ControllerBasic.PressEvent> = emptyList()
    override var isActive = false
        private set

    init {
        id = name.replace(REPLACE, "")
        states = ByteArray(ControllerKey.values().size)
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

    @Synchronized override fun poll() {
        for (i in states.indices) {
            when (states[i].toInt()) {
                2 -> states[i] = 1
                3 -> states[i] = 0
            }
        }
        val newPressEvents = ArrayList<ControllerBasic.PressEvent>()
        isActive = !pressEventQueue.isEmpty()
        while (!pressEventQueue.isEmpty()) {
            val event = pressEventQueue.poll()
            val keyID = event.key.id
            when (event.state) {
                ControllerBasic.PressState.PRESS -> states[keyID] = 2
                ControllerBasic.PressState.RELEASE -> if (states[keyID].toInt() == 2) {
                    states[keyID] = 3
                } else {
                    states[keyID] = 0
                }
            }
            newPressEvents.add(event)
        }
        pressEvents = newPressEvents
    }

    override fun isDown(key: ControllerKey): Boolean {
        return states[key.id] >= 1
    }

    override fun isPressed(key: ControllerKey): Boolean {
        return states[key.id] >= 2
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

    @Synchronized fun setAxis(axis: Int,
                              value: Double) {
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

    companion object {
        private val REPLACE = "[ /-]".toRegex()
    }
}
