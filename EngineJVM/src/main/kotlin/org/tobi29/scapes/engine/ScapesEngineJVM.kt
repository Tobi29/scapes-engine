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

package org.tobi29.scapes.engine

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.sync.Mutex
import org.tobi29.scapes.engine.graphics.GraphicsSystem
import org.tobi29.scapes.engine.gui.*
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues
import org.tobi29.scapes.engine.gui.debug.GuiWidgetPerformance
import org.tobi29.scapes.engine.gui.debug.GuiWidgetProfiler
import org.tobi29.scapes.engine.resource.ResourceLoader
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.utils.*
import org.tobi29.scapes.engine.utils.io.FileSystemContainer
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.profiler.profilerSection
import org.tobi29.scapes.engine.utils.tag.MutableTagMap
import org.tobi29.scapes.engine.utils.task.*
import kotlin.coroutines.experimental.CoroutineContext

actual class ScapesEngine actual constructor(
        actual val container: Container,
        defaultGuiStyle: (ScapesEngine) -> GuiStyle,
        actual val taskExecutor: CoroutineContext,
        configMap: MutableTagMap
) : CoroutineDispatcher(),
        ComponentHolder<Any> {
    actual override val componentStorage = ComponentStorage<Any>()
    private val queue = TaskChannel<(Double) -> Unit>()
    private val tpsDebug: GuiWidgetDebugValues.Element
    private val newState = AtomicReference<GameState>()
    private val updateJob = AtomicReference<Pair<Job, AtomicBoolean>?>(null)
    private var stateMut: GameState? = null
    actual val files = FileSystemContainer()
    actual val events = EventDispatcher()
    actual val resources = ResourceLoader(taskExecutor)
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

        checkSystem()
        logger.info { "Starting Scapes-Engine: $this" }

        logger.info { "Creating backend" }
        sounds = container.createSoundSystem(this)

        logger.info { "Setting up GUI" }
        guiStyle = defaultGuiStyle(this)
        notifications = GuiNotifications(guiStyle)
        guiStack.addUnfocused("90-Notifications", notifications)
        tooltip = GuiTooltip(guiStyle)
        guiStack.addUnfocused("80-Tooltip", tooltip)
        val debugGui = object : Gui(guiStyle) {
            override val isValid = true
        }
        debugValues = debugGui.add(32.0, 32.0, 360.0, 256.0,
                ::GuiWidgetDebugValues)
        debugValues.visible = false
        profiler = debugGui.add(32.0, 32.0, 360.0, 256.0, ::GuiWidgetProfiler)
        profiler.visible = false
        performance = debugGui.add(32.0, 32.0, 360.0, 256.0,
                ::GuiWidgetPerformance)
        performance.visible = false
        guiStack.addUnfocused("99-Debug", debugGui)
        tpsDebug = debugValues["Engine-Tps"]
        logger.info { "Creating graphics system" }
        graphics = GraphicsSystem(this, container.gos)
        logger.info { "Initializing engine" }
        registerComponent(DeltaProfilerComponent.COMPONENT,
                DeltaProfilerComponent(performance))
        registerComponent(MemoryProfilerComponent.COMPONENT,
                MemoryProfilerComponent(debugValues))
        logger.info { "Engine created" }
    }

    private fun checkSystem() {
        val runtime = Runtime.getRuntime()
        logger.info {
            "Operating system: ${System.getProperty(
                    "os.name")} ${System.getProperty(
                    "os.version")} ${System.getProperty("os.arch")}"
        }
        logger.info {
            "Java: ${System.getProperty(
                    "java.version")} (MaxMemory: ${runtime.maxMemory() / 1048576}, Processors: ${runtime.availableProcessors()})"
        }
    }

    actual val state
        get() = stateMut ?: throw IllegalStateException("Engine not running")

    actual override fun dispatch(context: CoroutineContext,
                                 block: Runnable) {
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
        val stop = AtomicBoolean(false)
        val mutex = Mutex(true)
        var startTps = 1.0
        val job = launch(taskExecutor + CoroutineName("Engine-State")) {
            mutex.lock()
            launchThread("Engine-State") {
                var tps = startTps
                val timer = Timer()
                timer.init()
                while (!stop.get()) {
                    val tickDiff = timer.cap(Timer.toDiff(tps), ::sleepNanos)
                    tpsDebug.setValue(Timer.toTps(tickDiff))
                    val delta = Timer.toDelta(tickDiff).coerceIn(0.0001, 0.1)
                    tps = step(delta)
                }
                components.asSequence().filterIsInstance<ComponentLifecycle>()
                        .forEach { it.halt() }
            }.join()
        } to stop
        if (updateJob.compareAndSet(null, job)) {
            components.asSequence().filterIsInstance<ComponentLifecycle>()
                    .forEach { it.start() }
            startTps = step(0.0001)
            mutex.unlock()
        } else job.first.cancel()
    }

    actual suspend fun halt() {
        updateJob.get()?.let { job ->
            job.second.set(true)
            job.first.join()
            updateJob.compareAndSet(job, null)
        }
    }

    actual suspend fun dispose() {
        halt()
        synchronized(this) {
            logger.info { "Disposing last state" }
            stateMut?.dispose()
            stateMut = null
            logger.info { "Disposing sound system" }
            sounds.dispose()
            logger.info { "Disposing game" }
            clearComponents()
            logger.info { "Shutting down tasks" }
            taskExecutor[Job]?.cancel()
            logger.info { "Stopped Scapes-Engine" }
        }
    }

    actual fun debugMap(): Map<String, String> {
        val debugValues = HashMap<String, String>()
        for ((key, value) in this.debugValues.elements()) {
            debugValues.put(key, value.toString())
        }
        return debugValues.readOnly()
    }

    actual fun isMouseGrabbed(): Boolean {
        return stateMut?.isMouseGrabbed ?: false || guiController.captureCursor()
    }

    private fun step(delta: Double): Double {
        var currentState = this.stateMut
        val newState = newState.getAndSet(null)
        if (newState != null) {
            synchronized(graphics) {
                this.stateMut?.dispose()
                this.stateMut = newState
                newState.init()
            }
            currentState = newState
        }
        profilerSection("Components") {
            components.asSequence().filterIsInstance<ComponentStep>()
                    .forEach { it.step(delta) }
        }
        profilerSection("Gui") {
            guiStack.step(delta)
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
        actual val CONFIG_MAP_COMPONENT = ComponentTypeRegistered<ScapesEngine, MutableTagMap, Any>()
    }
}

