/*
 * Copyright 2012-2016 Tobi29
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

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

abstract class ControllerDefault protected constructor() : ControllerBasic {
    private val states: ByteArray
    private val pressEventQueue = ConcurrentLinkedQueue<ControllerBasic.PressEvent>()
    private val typeEventQueue = ConcurrentLinkedQueue<KeyTypeEvent>()
    private var pressEvents: Collection<ControllerBasic.PressEvent> = emptyList()
    private var typeEvents: Collection<KeyTypeEvent> = emptyList()
    private var x = 0.0
    private var y = 0.0
    private var deltaX = 0.0
    private var deltaY = 0.0
    private var scrollX = 0.0
    private var scrollY = 0.0
    private var deltaXSet = 0.0
    private var deltaYSet = 0.0
    private var scrollXSet = 0.0
    private var scrollYSet = 0.0
    override var isActive = false
        protected set

    init {
        states = ByteArray(ControllerKey.values().size)
    }

    @Synchronized override fun poll() {
        for (i in states.indices) {
            when (states[i].toInt()) {
                2 -> states[i] = 1
                3 -> states[i] = 0
            }
        }
        val newPressEvents = ArrayList<ControllerBasic.PressEvent>()
        val newTypeEvents = ArrayList<KeyTypeEvent>()
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
        while (!typeEventQueue.isEmpty()) {
            newTypeEvents.add(typeEventQueue.poll())
        }
        pressEvents = newPressEvents
        typeEvents = newTypeEvents
        deltaX = deltaXSet
        deltaXSet = 0.0
        deltaY = deltaYSet
        deltaYSet = 0.0
        scrollX = scrollXSet
        scrollXSet = 0.0
        scrollY = scrollYSet
        scrollYSet = 0.0
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

    fun typeEvents(): Sequence<KeyTypeEvent> {
        return typeEvents.asSequence()
    }

    abstract val isModifierDown: Boolean

    fun x(): Double {
        return x
    }

    fun y(): Double {
        return y
    }

    fun deltaX(): Double {
        return deltaX
    }

    fun deltaY(): Double {
        return deltaY
    }

    fun scrollX(): Double {
        return scrollX
    }

    fun scrollY(): Double {
        return scrollY
    }

    fun addTypeEvent(character: Char) {
        typeEventQueue.add(KeyTypeEvent(character))
    }

    fun clearTypeEvents() {
        typeEvents = emptyList<KeyTypeEvent>()
    }

    fun set(x: Double,
            y: Double) {
        this.x = x
        this.y = y
    }

    fun addDelta(x: Double,
                 y: Double) {
        deltaXSet += x
        deltaYSet += y
    }

    fun addScroll(x: Double,
                  y: Double) {
        scrollXSet += x
        scrollYSet += y
        if (x < 0.0) {
            addPressEvent(ControllerKey.SCROLL_LEFT,
                    ControllerBasic.PressState.PRESS)
            addPressEvent(ControllerKey.SCROLL_LEFT,
                    ControllerBasic.PressState.RELEASE)
        } else if (x > 0.0) {
            addPressEvent(ControllerKey.SCROLL_RIGHT,
                    ControllerBasic.PressState.PRESS)
            addPressEvent(ControllerKey.SCROLL_RIGHT,
                    ControllerBasic.PressState.RELEASE)
        }
        if (y < 0.0) {
            addPressEvent(ControllerKey.SCROLL_DOWN,
                    ControllerBasic.PressState.PRESS)
            addPressEvent(ControllerKey.SCROLL_DOWN,
                    ControllerBasic.PressState.RELEASE)
        } else if (y > 0.0) {
            addPressEvent(ControllerKey.SCROLL_UP,
                    ControllerBasic.PressState.PRESS)
            addPressEvent(ControllerKey.SCROLL_UP,
                    ControllerBasic.PressState.RELEASE)
        }
    }

    @Synchronized fun clearStates() {
        Arrays.fill(states, 0.toByte())
    }

    class KeyTypeEvent(private val character: Char) {

        fun character(): Char {
            return character
        }
    }
}
