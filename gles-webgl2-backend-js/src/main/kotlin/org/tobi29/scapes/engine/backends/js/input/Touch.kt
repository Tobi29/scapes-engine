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

import org.tobi29.scapes.engine.backends.js.TouchList
import org.tobi29.scapes.engine.backends.js.offsetLeft
import org.tobi29.scapes.engine.backends.js.offsetTop
import org.tobi29.scapes.engine.input.ControllerTouch
import org.tobi29.scapes.engine.input.ControllerTracker
import org.tobi29.stdex.atomic.AtomicLong
import org.tobi29.utils.steadyClock

internal class DOMControllerTouch : ControllerTouch() {
    override val name = "Touchscreen"
    private val lastActiveMut = AtomicLong(Long.MIN_VALUE)
    override val lastActive get() = lastActiveMut.get()
    private var fingersMut = emptyMap<dynamic, ControllerTracker.Tracker>()

    override fun fingers() = fingersMut.values.asSequence()

    internal fun setFingers(touches: TouchList) {
        if (touches.length > 0)
            lastActiveMut.set(steadyClock.timeSteadyNanos())
        val fingers = HashMap<dynamic, ControllerTracker.Tracker>()
        for (i in 0 until touches.length) {
            val touch = touches.item(i)
            val finger =
                fingersMut[touch.identifier] ?: ControllerTracker.Tracker()
            finger.pos.setXY(
                touch.clientX - touch.target.offsetLeft,
                touch.clientY - touch.target.offsetTop
            )
            fingers[touch.identifier] = finger
        }
        fingersMut = fingers
    }
}
