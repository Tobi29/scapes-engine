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

package org.tobi29.scapes.engine.input

import org.tobi29.io.tag.MutableTagMap
import org.tobi29.io.tag.mapMut
import org.tobi29.logging.KLogging
import org.tobi29.scapes.engine.ComponentStep
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.gui.GuiController
import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.stdex.atomic.AtomicBoolean
import org.tobi29.stdex.atomic.AtomicReference
import org.tobi29.stdex.concurrent.ReentrantLock
import org.tobi29.stdex.concurrent.withLock
import org.tobi29.stdex.readOnly
import org.tobi29.utils.ComponentHolder
import org.tobi29.utils.ComponentRegistered
import org.tobi29.utils.EventDispatcher
import kotlin.collections.set

abstract class InputManager<M : InputMode>(
    val engine: ScapesEngine,
    private val configMap: MutableTagMap,
    private val inputModeDummy: M
) : ComponentRegistered, ComponentStep {
    private val lock = ReentrantLock()
    private val inputModesMut =
        ConcurrentHashMap<Controller, (MutableTagMap) -> M>()
    var inputModes = emptyList<M>()
        private set
    private var inputModeMut = AtomicReference(inputModeDummy)
    val inputMode get() = inputModeMut.get()
    private val freezeInputModeMut = AtomicBoolean(false)
    var freezeInputMode
        get() = freezeInputModeMut.get()
        set(value) = freezeInputModeMut.set(value)

    val events = EventDispatcher(engine.events) {
        listen<Controller.AddEvent> { event ->
            inputMode(event.controller)?.let {
                inputModesMut[event.controller] = it
                reloadInput()
            }
        }
        listen<Controller.RemoveEvent> { event ->
            inputModesMut.remove(event.controller)
            reloadInput()
        }
    }.apply { enable() }

    override fun init(holder: ComponentHolder<out Any>) {
        events.enable()
        reloadInput()
    }

    override fun dispose(holder: ComponentHolder<out Any>) {
        events.disable()
        changeInput(null)
    }

    override fun step(delta: Double) {
        val newInputMode: M? = inputModes.maxBy { it.poll(delta) }
        if (newInputMode !== null && inputMode !== newInputMode
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

    protected abstract fun inputMode(controller: Controller): ((MutableTagMap) -> M)?

    protected abstract fun inputModeChanged(inputMode: M)

    private fun changeInput(inputMode: M?) = lock.withLock {
        logger.info {
            inputMode?.let { "Setting input mode to $it" }
                    ?: "Disabling input mode"
        }
        val newInputMode = inputMode ?: inputModeDummy
        inputModeMut.get().disabled()
        inputModeMut.set(newInputMode)
        engine.guiController = newInputMode.guiController()
        newInputMode.let { inputModeChanged(it) }
        newInputMode.enabled()
    }

    companion object : KLogging()
}

interface InputMode {
    fun enabled() {}

    fun disabled() {}

    fun poll(delta: Double): Long

    fun guiController(): GuiController
}
