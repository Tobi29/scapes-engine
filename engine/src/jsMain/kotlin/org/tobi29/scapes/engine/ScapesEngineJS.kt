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

package org.tobi29.scapes.engine

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import org.tobi29.coroutines.*
import org.tobi29.io.FileSystemContainer
import org.tobi29.io.tag.MutableTagMap
import org.tobi29.logging.KLogging
import org.tobi29.profiler.profilerSection
import org.tobi29.scapes.engine.graphics.GraphicsSystem
import org.tobi29.scapes.engine.gui.*
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues
import org.tobi29.scapes.engine.gui.debug.GuiWidgetPerformance
import org.tobi29.scapes.engine.gui.debug.GuiWidgetProfiler
import org.tobi29.scapes.engine.resource.ResourceLoader
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.stdex.atomic.AtomicDouble
import org.tobi29.stdex.atomic.AtomicReference
import org.tobi29.stdex.concurrent.withLock
import org.tobi29.stdex.readOnly
import org.tobi29.utils.*
import org.tobi29.utils.ComponentLifecycle
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.coroutines.experimental.CoroutineContext

actual class ScapesEngine actual constructor(
    actual val container: Container,
    defaultGuiStyle: (ScapesEngine) -> GuiStyle,
    actual val taskExecutor: CoroutineContext,
    configMap: MutableTagMap
) : CoroutineDispatcher(), CoroutineScope, ComponentHolder<Any> {
    private val mutex = Mutex()
    actual override val componentStorage = ComponentStorage<Any>()
    private val queue = TaskChannel<(Double) -> Unit>()
    private val job = Job()
    override val coroutineContext: CoroutineContext get() = this + job

    private val tpsDebug: GuiWidgetDebugValues.Element
    private val newState = AtomicReference<GameState?>(null)
    private val updateJob = JobHandle(this)
    private var _state: GameState? = null
    actual val state: GameState? get() = _state
    actual val files = FileSystemContainer()
    actual val events = EventDispatcher()
    actual val resources = ResourceLoader(CoroutineScope(taskExecutor + job))
    actual val graphics: GraphicsSystem
    actual val sounds: SoundSystem
    actual val guiStyle: GuiStyle
    actual val guiStack = GuiStack()
    actual var guiController: GuiController = GuiControllerDummy(this)
    actual val notifications: GuiNotifications
    actual val tooltip: GuiTooltip
    actual val debugValues: GuiWidgetDebugValues
    actual val profiler: GuiWidgetProfiler
    actual val performance: GuiWidgetPerformance

    init {
        registerComponent(CONFIG_MAP_COMPONENT, configMap)

        logger.info { "Starting Scapes-Engine: $this" }

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
        tpsDebug = debugValues["Engine-Tps"]

        logger.info { "Initializing engine" }
        registerComponent(
            DeltaProfilerComponent.COMPONENT,
            DeltaProfilerComponent(performance)
        )
        registerComponent(
            CursorCaptureComponent.COMPONENT,
            CursorCaptureComponent()
        )
        graphics.initDebug(debugValues)

        logger.info { "Engine created" }
    }

    actual override fun dispatch(
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

    actual fun switchState(state: GameState) {
        newState.set(state)
    }

    actual fun start() {
        val startTps = AtomicDouble(1.0)
        updateJob.launchLater(taskExecutor) {
            var tps = startTps.get()
            val timer = Timer()
            timer.init()
            try {
                while (true) {
                    val tickDiff =
                        timer.cap(Timer.toDiff(tps), { delayNanos(it) })
                    tpsDebug.setValue(Timer.toTps(tickDiff))
                    val delta =
                        Timer.toDelta(tickDiff).coerceIn(0.0001, 0.1)
                    tps = withContext(NonCancellable) { step(delta) }
                }
            } finally {
                components.asSequence()
                    .filterIsInstance<ComponentLifecycle<*>>()
                    .forEach { it.halt() }
            }
        }?.let { (_, launch) ->
            components.asSequence().filterIsInstance<ComponentLifecycle<*>>()
                .forEach { it.start() }
            startTps.set(step(0.0001))
            launch()
        }
    }

    actual suspend fun halt() {
        updateJob.job?.cancelAndJoin()
    }

    actual suspend fun dispose() {
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

    actual fun debugMap(): Map<String, String> {
        val debugValues = HashMap<String, String>()
        for ((key, value) in this.debugValues.elements()) {
            debugValues[key] = value.toString()
        }
        return debugValues.readOnly()
    }

    private fun step(delta: Double): Double {
        var currentState = state
        val newState = newState.getAndSet(null)
        if (newState != null) {
            graphics.lock.withLock {
                state?.disposeState()
                _state = newState
                newState.initState()
            }
            currentState = newState
        }
        profilerSection("Components") {
            components.asSequence().filterIsInstance<ComponentStep>()
                .forEach { it.step(delta) }
        }
        profilerSection("Gui-Controller") {
            guiController.update(delta)
        }
        val state = currentState
                ?: return this[ScapesEngineConfig.COMPONENT].fps
        profilerSection("Container") {
            container.update(delta)
        }
        profilerSection("State") {
            state.step(delta)
        }
        profilerSection("Tasks") {
            queue.processCurrent { it(delta) }
        }
        return state.tps
    }

    actual companion object : KLogging() {
        actual val CONFIG_MAP_COMPONENT =
            ComponentTypeRegistered<ScapesEngine, MutableTagMap, Any>()
    }
}
