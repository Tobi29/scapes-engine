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

package org.tobi29.scapes.engine

import org.tobi29.scapes.engine.input.ControllerDefault
import org.tobi29.scapes.engine.input.ControllerKey
import org.tobi29.scapes.engine.input.ControllerTouch
import org.tobi29.scapes.engine.utils.EventDispatcher

class ContainerEmulateTouch(private val container: Container,
                            private val controller: ControllerDefault = container.controller() ?: throw IllegalArgumentException(
                                    "Trying to create mouse to touch emulator without mouse input")) : Container by container, ControllerTouch {
    override val events = EventDispatcher()

    private var tracker: ControllerTouch.Tracker? = null

    override fun containerWidth(): Int {
        return container.containerWidth() / 3
    }

    override fun containerHeight(): Int {
        return container.containerHeight() / 3
    }

    override fun formFactor(): Container.FormFactor {
        return Container.FormFactor.PHONE
    }

    override fun setMouseGrabbed(value: Boolean) {
    }

    override fun controller(): ControllerDefault? {
        return null
    }

    override fun touch(): ControllerTouch? {
        return this
    }

    override fun fingers(): Sequence<ControllerTouch.Tracker> {
        return tracker?.let { sequenceOf(it) } ?: emptySequence()
    }

    override val isActive: Boolean
        get() = controller.isActive


    override fun poll() {
        controller.poll()
        val tracker = tracker
        if (tracker != null) {
            if (controller.isDown(ControllerKey.BUTTON_0)) {
                tracker.pos.set(controller.x() / 3.0, controller.y() / 3.0)
            } else {
                this.tracker = null
            }
        } else if (controller.isPressed(ControllerKey.BUTTON_0)) {
            val newTracker = ControllerTouch.Tracker()
            newTracker.pos.set(controller.x() / 3.0, controller.y() / 3.0)
            this.tracker = newTracker
        }
    }
}
