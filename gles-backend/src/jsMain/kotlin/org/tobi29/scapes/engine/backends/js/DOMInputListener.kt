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

package org.tobi29.scapes.engine.backends.js

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.yield
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.backends.js.input.DOMControllerDesktop
import org.tobi29.scapes.engine.backends.js.input.DOMControllerTouch
import org.tobi29.scapes.engine.backends.js.input.WebJoystick
import org.tobi29.scapes.engine.backends.js.input.createWrapper
import org.tobi29.scapes.engine.input.Controller
import org.tobi29.scapes.engine.input.ControllerButtons
import org.tobi29.scapes.engine.input.ControllerKey
import org.w3c.dom.Element
import org.w3c.dom.events.*
import org.w3c.gamepad.GamepadEvent
import org.w3c.gamepad.eventGamepadconnected
import org.w3c.gamepad.eventGamepaddisconnected
import org.w3c.gamepad.getGamepads
import kotlin.browser.window

class DOMInputListener(private val engine: ScapesEngine) {
    private val pressedKeys = HashSet<ControllerKey>()
    private val defaultController = DOMControllerDesktop()
    private val touchController = DOMControllerTouch()
    private val gamepads = HashMap<Int, WebJoystick>()
    private var pollJob: Job? = null

    private val keydownListener =
        listener { event: KeyboardEvent ->
            DOMKeyMap.key(event.code)
                ?.let { key ->
                    if (pressedKeys.add(key)) {
                        defaultController.addPressEvent(
                            key,
                            ControllerButtons.Action.PRESS, engine.events
                        )
                    } else {
                        defaultController.addPressEvent(
                            key,
                            ControllerButtons.Action.REPEAT, engine.events
                        )
                    }
                }
        }
    private val keyupListener =
        listener { event: KeyboardEvent ->
            DOMKeyMap.key(event.code)
                ?.let { key ->
                    if (pressedKeys.remove(key)) {
                        defaultController.addPressEvent(
                            key,
                            ControllerButtons.Action.RELEASE, engine.events
                        )
                    }
                }
        }
    private val mousedownListener =
        listener { event: MouseEvent ->
            DOMKeyMap.button(event.button)
                ?.let { key ->
                    if (pressedKeys.add(key)) {
                        defaultController.addPressEvent(
                            key,
                            ControllerButtons.Action.PRESS, engine.events
                        )
                    } else {
                        defaultController.addPressEvent(
                            key,
                            ControllerButtons.Action.REPEAT, engine.events
                        )
                    }
                }
        }
    private val mouseupListener =
        listener { event: MouseEvent ->
            DOMKeyMap.button(event.button)
                ?.let { key ->
                    if (pressedKeys.remove(key)) {
                        defaultController.addPressEvent(
                            key,
                            ControllerButtons.Action.RELEASE, engine.events
                        )
                    }
                }
        }
    private val mousemoveListener =
        listener { event: MouseEvent ->
            defaultController.set(event.offsetX, event.offsetY)
            defaultController.addDelta(
                event.movementX, event.movementY,
                engine.events
            )
        }
    private val wheelListener =
        listener { event: WheelEvent ->
            event.preventDefault()
            defaultController.addScroll(
                event.deltaX, event.deltaY, event.deltaMode,
                engine.events
            )
        }
    private val touchstartListener =
        listener { event: TouchEvent ->
            event.preventDefault()
            touchController.setFingers(event.targetTouches)
        }
    private val touchmoveListener =
        listener { event: TouchEvent ->
            event.preventDefault()
            touchController.setFingers(event.targetTouches)
        }
    private val touchendListener =
        listener { event: TouchEvent ->
            event.preventDefault()
            touchController.setFingers(event.targetTouches)
        }
    private val touchcancelListener =
        listener { event: TouchEvent ->
            event.preventDefault()
            touchController.setFingers(event.targetTouches)
        }
    private val gamepadconnectedListener =
        listener { event: GamepadEvent ->
            val gamepad = event.gamepad
            gamepads[gamepad.index] = gamepad.createWrapper().also {
                engine.events.fire(Controller.AddEvent(it))
            }
        }
    private val gamepaddisconnectedListener =
        listener { event: GamepadEvent ->
            val gamepad = event.gamepad
            gamepads.remove(gamepad.index)?.let {
                engine.events.fire(Controller.RemoveEvent(it))
            }
        }

    init {
        engine.events.fire(Controller.AddEvent(defaultController))
        engine.events.fire(Controller.AddEvent(touchController))
        window.navigator.getGamepads()?.forEach { gamepad ->
            if (gamepad != null)
                gamepads[gamepad.index] = gamepad.createWrapper().also {
                    engine.events.fire(Controller.AddEvent(it))
                }
        }
    }

    fun attach(target: EventTarget) {
        target.addEventListener("keydown", keydownListener)
        target.addEventListener("keyup", keyupListener)
        target.addEventListener("mousedown", mousedownListener)
        target.addEventListener("mouseup", mouseupListener)
        target.addEventListener("mousemove", mousemoveListener)
        target.addEventListener("wheel", wheelListener)
        target.addEventListener("touchstart", touchstartListener)
        target.addEventListener("touchmove", touchmoveListener)
        target.addEventListener("touchend", touchendListener)
        target.addEventListener("touchcancel", touchcancelListener)
        eventGamepadconnected?.let {
            window.addEventListener(it, gamepadconnectedListener)
        }
        eventGamepaddisconnected?.let {
            window.addEventListener(it, gamepaddisconnectedListener)
        }
        pollJob?.cancel()
        pollJob = engine.launch(engine.graphics) {
            while (true) {
                val gamepadArray = window.navigator.getGamepads()
                        ?: break // Not supported
                gamepads.values.forEach { it.poll(gamepadArray, engine.events) }
                yield() // Wait for next frame
            }
        }
    }

    fun dettach(target: EventTarget) {
        target.removeEventListener("keydown", keydownListener)
        target.removeEventListener("keyup", keyupListener)
        target.removeEventListener("mousedown", mousedownListener)
        target.removeEventListener("mouseup", mouseupListener)
        target.removeEventListener("mousemove", mousemoveListener)
        target.removeEventListener("wheel", wheelListener)
        target.removeEventListener("touchstart", touchstartListener)
        target.removeEventListener("touchmove", touchmoveListener)
        target.removeEventListener("touchend", touchendListener)
        target.removeEventListener("touchcancel", touchcancelListener)
        eventGamepadconnected?.let {
            window.removeEventListener(it, gamepadconnectedListener)
        }
        eventGamepaddisconnected?.let {
            window.removeEventListener(it, gamepaddisconnectedListener)
        }
        pollJob?.cancel()
        pollJob = null
    }

    companion object {
        private inline fun <reified E : Event> listener(
            crossinline block: (E) -> Unit
        ): (Event) -> Unit = { event ->
            @Suppress("UnsafeCastFromDynamic")
            block(event.asDynamic())
        }
    }
}

internal external open class TouchEvent : Event {
    val touches: TouchList
    val targetTouches: TouchList
}

internal external class Touch {
    val identifier: dynamic
    val screenX: Double
    val screenY: Double
    val clientX: Double
    val clientY: Double
    val pageX: Double
    val pageY: Double
    val target: Element
}

internal external class TouchList {
    val length: Int
    fun item(index: Int): Touch
}

internal inline val MouseEvent.offsetX: Double
    get() {
        val value = asDynamic().offsetX
        if (value == undefined) {
            throw UnsupportedOperationException(
                "Mouse offset property unavailable"
            )
        }
        return value
    }

internal inline val MouseEvent.offsetY: Double
    get() {
        val value = asDynamic().offsetY
        if (value == undefined) {
            throw UnsupportedOperationException(
                "Mouse offset property unavailable"
            )
        }
        return value
    }

internal inline val MouseEvent.movementX: Double
    get() {
        var value = asDynamic().movementX
        if (value === undefined) {
            value = asDynamic().mozMovementX
        }
        if (value == undefined) {
            throw UnsupportedOperationException(
                "Mouse movement property unavailable"
            )
        }
        return value
    }

internal inline val MouseEvent.movementY: Double
    get() {
        var value = asDynamic().movementY
        if (value === undefined) {
            value = asDynamic().mozMovementY
        }
        if (value == undefined) {
            throw UnsupportedOperationException(
                "Mouse movement property unavailable"
            )
        }
        return value
    }

internal inline val Element.offsetLeft: Double
    get() {
        val value = asDynamic().offsetLeft
        if (value == undefined) {
            throw UnsupportedOperationException(
                "Element offset property unavailable"
            )
        }
        return value
    }

internal inline val Element.offsetTop: Double
    get() {
        val value = asDynamic().offsetTop
        if (value == undefined) {
            throw UnsupportedOperationException(
                "Element offset property unavailable"
            )
        }
        return value
    }
