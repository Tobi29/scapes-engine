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

package org.tobi29.scapes.engine

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.tobi29.coroutines.TaskChannel
import org.tobi29.coroutines.offer
import org.tobi29.coroutines.processCurrent
import org.tobi29.io.FileSystemContainer
import org.tobi29.io.tag.MutableTagMap
import org.tobi29.logging.KLogger
import org.tobi29.profiler.profilerSection
import org.tobi29.scapes.engine.graphics.GraphicsSystem
import org.tobi29.scapes.engine.gui.*
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues
import org.tobi29.scapes.engine.gui.debug.GuiWidgetPerformance
import org.tobi29.scapes.engine.gui.debug.GuiWidgetProfiler
import org.tobi29.scapes.engine.resource.ResourceLoader
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.stdex.concurrent.ReentrantLock
import org.tobi29.stdex.concurrent.withLock
import org.tobi29.stdex.readOnly
import org.tobi29.utils.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext

class ScapesEngine(
    val container: Container,
    defaultGuiStyle: (ScapesEngine) -> GuiStyle,
    val taskExecutor: CoroutineContext,
    configMap: MutableTagMap
) : CoroutineDispatcher(), CoroutineScope, ComponentHolder<Any> {
    private val lock = ReentrantLock()
    private val mutex = Mutex()
    override val componentStorage = ComponentStorage<Any>()
    private val queue = TaskChannel<(Double) -> Unit>()
    private val job = Job()
    override val coroutineContext: CoroutineContext get() = this + job

    private var _state: GameState? = null
    val state: GameState? get() = _state
    val files = FileSystemContainer()
    val events = EventDispatcher()
    val resources = ResourceLoader(CoroutineScope(taskExecutor + job))
    val graphics: GraphicsSystem
    val sounds: SoundSystem
    val guiStyle: GuiStyle
    val guiStack = GuiStack()
    var guiController: GuiController = GuiControllerDummy(this)
    val notifications: GuiNotifications
    val tooltip: GuiTooltip
    val debugValues: GuiWidgetDebugValues
    val profiler: GuiWidgetProfiler
    val performance: GuiWidgetPerformance

    init {
        registerComponent(CONFIG_MAP_COMPONENT, configMap)

        logger.info { "Starting Scapes-Engine: $this" }
        initEngineEarly()

        logger.info { "Creating backend" }
        sounds = container.createSoundSystem(this)

        logger.info { "Creating graphics system" }
        graphics = GraphicsSystem(this, container.gos)

        logger.info { "Setting up GUI" }
        guiStyle = defaultGuiStyle(this)
        notifications = GuiNotifications(guiStyle)
        guiStack.addUnfocused("90-Notifications", notifications)
        tooltip = GuiTooltip(guiStyle)
        guiStack.addUnfocused("80-Tooltip", tooltip)
        val debugGui = Gui(guiStyle)
        debugValues = debugGui.add(32.0, 32.0, 360.0, 256.0) {
            GuiWidgetDebugValues(it)
        }
        debugValues.visible = false
        profiler = debugGui.add(32.0, 32.0, 360.0, 256.0) {
            GuiWidgetProfiler(it)
        }
        profiler.visible = false
        performance = debugGui.add(32.0, 32.0, 360.0, 256.0) {
            GuiWidgetPerformance(it)
        }
        performance.visible = false
        guiStack.addUnfocused("99-Debug", debugGui)

        logger.info { "Initializing engine" }
        registerComponent(
            CursorCaptureComponent.COMPONENT,
            CursorCaptureComponent()
        )
        initEngineLate()
        graphics.initDebug(debugValues)

        logger.info { "Engine created" }
    }

    override fun dispatch(
        context: CoroutineContext,
        block: Runnable
    ) {
        queue.offer {
            try {
                block.run()
            } catch (e: CancellationException) {
                logger.warn { "Job cancelled: ${e.message}" }
            }
        }
    }

    fun switchState(state: GameState) {
        lock.withLock {
            graphics.lock.withLock {
                _state?.disposeState()
                _state = state
                state.initState()
            }
        }
    }

    fun start() {
        lock.withLock {
            components.asSequence().filterIsInstance<ComponentLifecycle<*>>()
                .forEach { it.start() }
        }
    }

    fun halt() {
        lock.withLock {
            components.asSequence()
                .filterIsInstance<ComponentLifecycle<*>>()
                .forEach { it.halt() }
        }
    }

    suspend fun dispose() {
        halt()
        mutex.withLock {
            logger.info { "Disposing last state" }
            state?.disposeState()
            _state = null
            logger.info { "Disposing GUI" }
            guiStack.clear()
            logger.info { "Disposing sound system" }
            sounds.dispose()
            logger.info { "Disposing components" }
            clearComponents()
            logger.info { "Stopped Scapes-Engine" }
            job.cancel()
        }
    }

    fun debugMap(): Map<String, String> {
        val debugValues = HashMap<String, String>()
        for ((key, value) in this.debugValues.elements()) {
            debugValues[key] = value.toString()
        }
        return debugValues.readOnly()
    }

    fun update(delta: Double) {
        lock.withLock {
            profilerSection("Components") {
                components.asSequence().filterIsInstance<ComponentStep>()
                    .forEach { it.step(delta) }
            }
            profilerSection("Gui-Controller") {
                guiController.update(delta)
            }
            val state = state ?: return
            profilerSection("State") {
                state.step(delta)
            }
            profilerSection("Tasks") {
                queue.processCurrent { it(delta) }
            }
        }
    }

    companion object {
        internal val logger = KLogger<ScapesEngine>()

        val CONFIG_MAP_COMPONENT =
            ComponentTypeRegistered<ScapesEngine, MutableTagMap, Any>()
    }
}

internal expect fun ScapesEngine.initEngineEarly()

internal expect fun ScapesEngine.initEngineLate()

interface ComponentStep {
    fun step(delta: Double) {}
}
