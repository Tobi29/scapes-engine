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

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.gui.GuiController
import org.tobi29.scapes.engine.gui.GuiControllerDummy
import org.tobi29.scapes.engine.utils.*
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.tag.MutableTagMap
import org.tobi29.scapes.engine.utils.tag.mapMut

abstract class InputManager<M : InputMode>(private val engine: ScapesEngine,
                                           private val configMap: MutableTagMap) {
    private val inputModesMut =
            ConcurrentHashMap<Controller, (MutableTagMap) -> M>()
    var inputModes = emptyList<M>()
        private set
    private var inputModeMut = AtomicReference<M?>(null)
    val inputMode get() = inputModeMut.get() ?: throw IllegalStateException(
            "No input mode is set")
    // TODO: Implement and use atomic delegated properties
    private val freezeInputModeMut = AtomicBoolean()
    var freezeInputMode get() = freezeInputModeMut.get()
        set(value) = freezeInputModeMut.set(value)

    val events = EventDispatcher(engine.events) {
        listen<ControllerAddEvent> { event ->
            inputMode(event.controller)?.let {
                inputModesMut[event.controller] = it
                reloadInput()
            }
        }
        listen<ControllerRemoveEvent> { event ->
            inputModesMut.remove(event.controller)
            reloadInput()
        }
    }.apply { enable() }

    fun step(delta: Double) {
        var newInputMode: M? = null
        for (inputMode in inputModes) {
            if (inputMode.poll(delta)) {
                newInputMode = inputMode
            }
        }
        if (newInputMode != null && inputMode !== newInputMode
                && !freezeInputMode) {
            changeInput(newInputMode)
        }
    }

    fun reloadInput() {
        val configMap = configMap.mapMut("Input")
        inputModes = inputModesMut.values.asSequence().map {
            it(configMap)
        }.toList().readOnly()
        changeInput(inputModes.firstOrNull())
    }

    fun enable() {
        events.enable()
        reloadInput()
    }

    fun disable() {
        events.disable()
        changeInput(null)
    }

    abstract protected fun inputMode(controller: Controller): ((MutableTagMap) -> M)?

    abstract protected fun inputModeChanged(inputMode: M)

    private fun changeInput(inputMode: M?) {
        logger.info {
            inputMode?.let { "Setting input mode to $it" }
                    ?: "Disabling input mode"
        }
        synchronized(inputModesMut) {
            inputModeMut.get()?.disabled()
            inputModeMut.set(inputMode)
            engine.guiController = inputMode?.guiController()
                    ?: GuiControllerDummy(engine)
            inputMode?.let { inputModeChanged(it) }
            inputMode?.enabled()
        }
    }

    companion object : KLogging()
}

interface InputMode {
    fun enabled() {}

    fun disabled() {}

    fun poll(delta: Double): Boolean

    fun guiController(): GuiController
}
