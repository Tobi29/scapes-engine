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

import mu.KLogging
import org.tobi29.scapes.engine.graphics.FontRenderer
import org.tobi29.scapes.engine.graphics.GraphicsCheckException
import org.tobi29.scapes.engine.graphics.GraphicsSystem
import org.tobi29.scapes.engine.gui.*
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues
import org.tobi29.scapes.engine.gui.debug.GuiWidgetProfiler
import org.tobi29.scapes.engine.input.ControllerDefault
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.spi.ScapesEngineBackendProvider
import org.tobi29.scapes.engine.utils.Crashable
import org.tobi29.scapes.engine.utils.EventDispatcher
import org.tobi29.scapes.engine.utils.Sync
import org.tobi29.scapes.engine.utils.io.filesystem.*
import org.tobi29.scapes.engine.utils.io.filesystem.classpath.ClasspathPath
import org.tobi29.scapes.engine.utils.io.tag.TagStructure
import org.tobi29.scapes.engine.utils.io.tag.json.TagStructureJSON
import org.tobi29.scapes.engine.utils.io.tag.setDouble
import org.tobi29.scapes.engine.utils.profiler.profilerSection
import org.tobi29.scapes.engine.utils.task.Joiner
import org.tobi29.scapes.engine.utils.task.TaskExecutor
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class ScapesEngine(game: (ScapesEngine) -> Game, backend: (ScapesEngine) -> Container,
                   val home: FilePath, cache: FilePath, private val debug: Boolean) : Crashable {
    val events = EventDispatcher()
    val game: Game
    val container: Container
    val graphics: GraphicsSystem
    val sounds: SoundSystem
    val guiStyle: GuiStyle
    val guiStack: GuiStack
    private val runtime: Runtime
    val tagStructure: TagStructure
    val config: ScapesEngineConfig
    val files: FileSystemContainer
    val fileCache: FileCache
    val taskExecutor = TaskExecutor(this, "Engine")
    val notifications: GuiNotifications
    val debugValues: GuiWidgetDebugValues
    private val usedMemoryDebug: GuiWidgetDebugValues.Element
    private val heapMemoryDebug: GuiWidgetDebugValues.Element
    private val maxMemoryDebug: GuiWidgetDebugValues.Element
    private val tpsDebug: GuiWidgetDebugValues.Element
    val profiler: GuiWidgetProfiler
    private val newState = AtomicReference<GameState>()
    private var joiner: Joiner? = null
    var guiController: GuiController
    private var mouseGrabbed = false
    private var state: GameState? = null

    constructor(game: (ScapesEngine) -> Game, backend: (ScapesEngine) -> Container,
                home: FilePath, debug: Boolean) : this(game, backend, home,
            home.resolve("cache"), debug) {
    }

    init {
        if (instance != null) {
            throw ScapesEngineException(
                    "You can only have one engine running at a time!")
        }
        instance = this
        runtime = Runtime.getRuntime()
        this.game = game(this)
        checkSystem()
        logger.info { "Starting Scapes-Engine: $this (Game: $game)" }
        logger.info { "Initializing asset system" }
        files = FileSystemContainer()
        files.registerFileSystem("Class",
                ClasspathPath(javaClass.classLoader, ""))
        files.registerFileSystem("Engine",
                ClasspathPath(javaClass.classLoader,
                        "assets/scapes/tobi29/engine/"))
        logger.info { "Initializing game" }
        this.game.initEarly()
        tagStructure = TagStructure()
        try {
            logger.info { "Reading config" }
            val configPath = this.home.resolve("ScapesEngine.json")
            if (exists(configPath)) {
                read(configPath) { stream ->
                    TagStructureJSON.read(stream, tagStructure)
                }
            }
        } catch (e: IOException) {
            logger.warn { "Failed to load config file: $e" }
        }

        if (tagStructure.has("Engine")) {
            config = ScapesEngineConfig(tagStructure.structure("Engine"))
        } else {
            logger.info { "Setting defaults to config" }
            val engineTag = tagStructure.structure("Engine")
            engineTag.setBoolean("VSync", true)
            engineTag.setDouble("Framerate", 60.0)
            engineTag.setDouble("ResolutionMultiplier", 1.0)
            engineTag.setDouble("MusicVolume", 1.0)
            engineTag.setDouble("SoundVolume", 1.0)
            engineTag.setBoolean("Fullscreen", false)
            config = ScapesEngineConfig(engineTag)
        }
        try {
            fileCache = FileCache(cache)
            fileCache.check()
            createDirectories(this.home.resolve("screenshots"))
        } catch (e: IOException) {
            throw ScapesEngineException(
                    "Failed to initialize file cache: " + e)
        }

        logger.info { "Creating container" }
        container = backend(this)
        logger.info { "Loading default font" }
        val font = FontRenderer(this, container.loadFont(
                "Engine:font/QuicksandPro-Regular") ?: throw IllegalStateException(
                "Failed to load default font"))
        logger.info { "Setting up GUI" }
        guiStack = GuiStack()
        guiStyle = GuiBasicStyle(this, font)
        notifications = GuiNotifications(guiStyle)
        guiStack.addUnfocused("90-Notifications", notifications)
        val debugGui = object : Gui(guiStyle) {
            override val isValid: Boolean
                get() = true
        }
        debugValues = debugGui.add(32.0, 32.0, 360.0, 256.0,
                ::GuiWidgetDebugValues)
        debugValues.isVisible = false
        profiler = debugGui.add(32.0, 32.0, 360.0, 256.0, ::GuiWidgetProfiler)
        profiler.isVisible = false
        guiStack.addUnfocused("99-Debug", debugGui)
        usedMemoryDebug = debugValues["Runtime-Memory-Used"]
        heapMemoryDebug = debugValues["Runtime-Memory-Heap"]
        maxMemoryDebug = debugValues["Runtime-Memory-Max"]
        tpsDebug = debugValues["Engine-Tps"]
        logger.info { "Creating graphics system" }
        graphics = GraphicsSystem(this, container.gl())
        logger.info { "Creating sound system" }
        sounds = container.sound()
        guiController = GuiControllerDummy(this)
        this.game.init()
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

    fun debug(): Boolean {
        return debug
    }

    val controller: ControllerDefault?
        get() = container.controller()

    fun getState(): GameState {
        return state ?: throw IllegalStateException("Engine not running")
    }

    fun switchState(state: GameState) {
        newState.set(state)
    }

    fun allocate(capacity: Int): ByteBuffer {
        return container.allocate(capacity)
    }

    fun run(): Int {
        start()
        try {
            container.run()
        } catch (e: GraphicsCheckException) {
            logger.error(e) { "Failed to initialize graphics" }
            container.message(Container.MessageType.ERROR, game.name,
                    "Unable to initialize graphics:\n" + e.message)
            halt()
            return 1
        } catch (e: Throwable) {
            logger.error(e) { "Scapes engine shutting down because of crash" }
            writeCrash(e)
            try {
                container.message(Container.MessageType.ERROR, game.name,
                        game.name + " crashed:\n" + e)
            } catch (e2: Exception) {
                logger.error(e2) { "Failed to show crash message" }
            }
            System.exit(1)
        }
        halt()
        return 0
    }

    fun start() {
        val wait = Joiner.BasicJoinable()
        joiner = taskExecutor.runThread({ joiner ->
            try {
                game.initLate()
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
            } catch (e: Throwable) {
                crash(e)
            }
        }, "State", TaskExecutor.Priority.HIGH)
        wait.joiner.join()
    }

    fun halt() {
        joiner?.join()
    }

    fun dispose() {
        halt()
        logger.info { "Disposing last state" }
        state?.disposeState()
        state = null
        logger.info { "Disposing sound system" }
        sounds.dispose()
        logger.info { "Disposing game" }
        game.dispose()
        try {
            write(home.resolve("ScapesEngine.json")) { streamOut ->
                TagStructureJSON.write(tagStructure, streamOut)
            }
        } catch (e: IOException) {
            logger.warn { "Failed to save config file!" }
        }

        logger.info { "Shutting down tasks" }
        taskExecutor.shutdown()
        logger.info { "Stopped Scapes-Engine" }
        instance = null
    }

    override fun crash(e: Throwable) {
        logger.error(e) { "Scapes engine shutting down because of crash" }
        writeCrash(e)
        System.exit(1)
    }

    fun writeCrash(e: Throwable) {
        val debugValues = ConcurrentHashMap<String, String>()
        for ((key, value) in this.debugValues.elements()) {
            debugValues.put(key, value.toString())
        }
        try {
            val crashReportFile = file(home)
            writeCrashReport(e, crashReportFile, "ScapesEngine",
                    debugValues)
            container.openFile(crashReportFile)
        } catch (e1: IOException) {
            logger.error { "Failed to write crash report: $e" }
        }

    }

    fun stop() {
        container.stop()
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
            game.step()
        }
        profilerSection("Gui") {
            guiStack.step(delta)
            guiController.update(delta)
        }
        val state = currentState ?: return config.fps
        val mouseGrabbed = state.isMouseGrabbed || guiController.captureCursor()
        if (this.mouseGrabbed != mouseGrabbed) {
            this.mouseGrabbed = mouseGrabbed
            container.setMouseGrabbed(mouseGrabbed)
        }
        profilerSection("Container") {
            container.update(delta)
        }
        profilerSection("State") {
            state.step(delta)
        }
        usedMemoryDebug.setValue(
                (runtime.totalMemory() - runtime.freeMemory()) / 1048576)
        heapMemoryDebug.setValue(runtime.totalMemory() / 1048576)
        maxMemoryDebug.setValue(runtime.maxMemory() / 1048576)
        return state.tps
    }

    companion object : KLogging() {
        private var instance: ScapesEngine? = null

        fun loadBackend(): (ScapesEngine) -> Container {
            for (backend in ServiceLoader.load(
                    ScapesEngineBackendProvider::class.java)) {
                try {
                    logger.debug { "Loaded backend: ${backend.javaClass.name}" }
                    return { backend.createContainer(it) }
                } catch (e: ServiceConfigurationError) {
                    logger.warn { "Unable to load backend provider: $e" }
                }
            }
            throw ScapesEngineException("No backend found!")
        }

        fun emulateTouch(
                backend: (ScapesEngine) -> Container): (ScapesEngine) -> Container {
            return { engine -> ContainerEmulateTouch(backend(engine)) }
        }
    }
}
