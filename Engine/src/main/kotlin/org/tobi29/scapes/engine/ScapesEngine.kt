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

import mu.KLogging
import org.tobi29.scapes.engine.graphics.FontRenderer
import org.tobi29.scapes.engine.graphics.GraphicsSystem
import org.tobi29.scapes.engine.gui.*
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues
import org.tobi29.scapes.engine.gui.debug.GuiWidgetPerformance
import org.tobi29.scapes.engine.gui.debug.GuiWidgetProfiler
import org.tobi29.scapes.engine.resource.ResourceLoader
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.utils.Crashable
import org.tobi29.scapes.engine.utils.EventDispatcher
import org.tobi29.scapes.engine.utils.Sync
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath
import org.tobi29.scapes.engine.utils.io.filesystem.FileSystemContainer
import org.tobi29.scapes.engine.utils.io.filesystem.classpath.ClasspathPath
import org.tobi29.scapes.engine.utils.io.filesystem.file
import org.tobi29.scapes.engine.utils.io.filesystem.writeCrashReport
import org.tobi29.scapes.engine.utils.profiler.profilerSection
import org.tobi29.scapes.engine.utils.readOnly
import org.tobi29.scapes.engine.utils.tag.MutableTagMap
import org.tobi29.scapes.engine.utils.tag.mapMut
import org.tobi29.scapes.engine.utils.tag.set
import org.tobi29.scapes.engine.utils.task.Joiner
import org.tobi29.scapes.engine.utils.task.TaskExecutor
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

class ScapesEngine(game: (ScapesEngine) -> Game,
                   backend: (ScapesEngine) -> Container,
                   val taskExecutor: TaskExecutor,
                   val configMap: MutableTagMap) {
    private val runtime = Runtime.getRuntime()
    val files = FileSystemContainer()
    val events = EventDispatcher()
    val resources = ResourceLoader(taskExecutor)
    val config: ScapesEngineConfig
    val game: Game
    val container: Container
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
    private val usedMemoryDebug: GuiWidgetDebugValues.Element
    private val heapMemoryDebug: GuiWidgetDebugValues.Element
    private val maxMemoryDebug: GuiWidgetDebugValues.Element
    private val tpsDebug: GuiWidgetDebugValues.Element
    private val newState = AtomicReference<GameState>()
    private var joiner: Joiner? = null
    private var state: GameState? = null

    init {
        this.game = game(this)
        checkSystem()

        logger.info { "Starting Scapes-Engine: $this (Game: $game)" }

        logger.info { "Initializing asset system" }
        files.registerFileSystem("Class",
                ClasspathPath(this::class.java.classLoader, ""))
        files.registerFileSystem("Engine",
                ClasspathPath(this::class.java.classLoader,
                        "assets/scapes/tobi29/engine"))

        logger.info { "Initializing game" }
        this.game.initEarly()

        logger.info { "Initializing engine config" }
        if (configMap.containsKey("Engine")) {
            config = ScapesEngineConfig(configMap.mapMut("Engine"))
        } else {
            logger.info { "Setting defaults to config" }
            val engineTag = configMap.mapMut("Engine")
            engineTag["VSync"] = true
            engineTag["Framerate"] = 60.0
            engineTag["ResolutionMultiplier"] = 1.0
            engineTag["Fullscreen"] = false
            config = ScapesEngineConfig(engineTag)
        }

        logger.info { "Creating backend" }
        container = backend(this)
        sounds = container.sounds

        logger.info { "Loading default font" }
        val font = FontRenderer(this, container.loadFont(
                "Engine:font/QuicksandPro-Regular") ?: throw IllegalStateException(
                "Failed to load default font"))

        logger.info { "Setting up GUI" }
        guiStyle = GuiBasicStyle(this, font)
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
        usedMemoryDebug = debugValues["Runtime-Memory-Used"]
        heapMemoryDebug = debugValues["Runtime-Memory-Heap"]
        maxMemoryDebug = debugValues["Runtime-Memory-Max"]
        tpsDebug = debugValues["Engine-Tps"]
        logger.info { "Creating graphics system" }
        graphics = GraphicsSystem(container.gos)
        logger.info { "Initializing game" }
        this.game.init()
        logger.info { "Engine created" }
    }

    private fun checkSystem() {
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

    fun getState(): GameState {
        return state ?: throw IllegalStateException("Engine not running")
    }

    fun switchState(state: GameState) {
        newState.set(state)
    }

    fun allocate(capacity: Int): ByteBuffer {
        return container.allocate(capacity)
    }

    @Synchronized
    fun start() {
        if (joiner != null) {
            return
        }
        val wait = Joiner.BasicJoinable()
        joiner = taskExecutor.runThread({ joiner ->
            game.start()
            var tps = step(0.0001)
            var sync = Sync(tps, 0L, false, "Engine-Update")
            sync.init()
            wait.join()
            sync.cap()
            while (!joiner.marked) {
                tpsDebug.setValue(sync.tps())
                val newTPS = step(sync.delta())
                if (tps != newTPS) {
                    tps = newTPS
                    sync = Sync(tps, 0L, false, "Engine-Update")
                }
                sync.cap()
            }
            game.halt()
        }, "State", TaskExecutor.Priority.HIGH)
        wait.joiner.join()
    }

    @Synchronized
    fun halt() {
        joiner?.let {
            it.join()
            joiner = null
        }
    }

    @Synchronized
    fun dispose() {
        halt()
        logger.info { "Disposing last state" }
        state?.disposeState()
        state = null
        logger.info { "Disposing sound system" }
        sounds.dispose()
        logger.info { "Disposing game" }
        game.dispose()
        logger.info { "Shutting down tasks" }
        taskExecutor.shutdown()
        logger.info { "Stopped Scapes-Engine" }
    }

    fun debugMap(): Map<String, String> {
        val debugValues = HashMap<String, String>()
        for ((key, value) in this.debugValues.elements()) {
            debugValues.put(key, value.toString())
        }
        return debugValues.readOnly()
    }

    fun isMouseGrabbed(): Boolean {
        return state?.isMouseGrabbed ?: false || guiController.captureCursor()
    }

    private fun step(delta: Double): Double {
        var currentState = this.state
        val newState = newState.getAndSet(null)
        if (newState != null) {
            synchronized(graphics) {
                this.state?.disposeState()
                this.state = newState
                newState.init()
            }
            currentState = newState
        }
        profilerSection("Tasks") {
            taskExecutor.tick()
        }
        profilerSection("Game") {
            game.step(delta)
        }
        profilerSection("Gui") {
            guiStack.step(delta)
            guiController.update(delta)
        }
        val state = currentState ?: return config.fps
        profilerSection("Container") {
            container.update(delta)
        }
        profilerSection("State") {
            state.step(delta)
        }
        performance.updateTimestamp(delta)
        usedMemoryDebug.setValue(
                (runtime.totalMemory() - runtime.freeMemory()) / 1048576)
        heapMemoryDebug.setValue(runtime.totalMemory() / 1048576)
        maxMemoryDebug.setValue(runtime.maxMemory() / 1048576)
        return state.tps
    }

    companion object : KLogging() {
        fun crashReport(path: FilePath,
                        engine: () -> ScapesEngine?,
                        e: Throwable) {
            val crashReportFile = file(path)
            val debug = try {
                engine()?.debugMap()
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            } ?: emptyMap<String, String>()
            writeCrashReport(e, crashReportFile, "ScapesEngine",
                    debug)
            engine()?.container?.openFile(crashReportFile)
        }

        fun crashHandler(path: FilePath,
                         engine: () -> ScapesEngine?) = object : Crashable {
            override fun crash(e: Throwable): Nothing {
                try {
                    System.err.println("Engine crashed: $e")
                    e.printStackTrace()
                    crashReport(path, engine, e)
                } finally {
                    exitProcess(1)
                }
            }
        }
    }
}
