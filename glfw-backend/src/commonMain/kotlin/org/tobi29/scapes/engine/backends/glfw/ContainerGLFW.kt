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

package org.tobi29.scapes.engine.backends.glfw

import kotlinx.coroutines.experimental.*
import org.tobi29.coroutines.*
import org.tobi29.io.AutoCloseable
import org.tobi29.io.tag.ReadTagMutableMap
import org.tobi29.io.use
import org.tobi29.logging.KLogger
import org.tobi29.platform.PLATFORM
import org.tobi29.profiler.profilerSection
import org.tobi29.scapes.engine.*
import org.tobi29.scapes.engine.backends.glfw.input.GLFWControllerDesktop
import org.tobi29.scapes.engine.backends.glfw.input.GLFWControllerJoystick
import org.tobi29.scapes.engine.backends.glfw.input.GLFWControllers
import org.tobi29.scapes.engine.backends.glfw.input.glfwKey
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.GraphicsCheckException
import org.tobi29.scapes.engine.graphics.GraphicsException
import org.tobi29.scapes.engine.graphics.GraphicsObjectSupplier
import org.tobi29.scapes.engine.gui.GuiController
import org.tobi29.scapes.engine.input.*
import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.stdex.concurrent.ReentrantLock
import org.tobi29.stdex.concurrent.withLock
import org.tobi29.stdex.math.clamp
import org.tobi29.utils.InstantSteadyNanos
import org.tobi29.utils.steadyClock
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.math.max
import kotlin.math.roundToInt

class ContainerGLFW(
    backend: ScapesEngineBackend,
    graphicsBackend: GraphicsBackend,
    private val title: String,
    private val emulateTouch: Boolean = false,
    private val density: Double = if (emulateTouch) 1.0 / 3.0 else 1.0
) : CoroutineDispatcher(), CoroutineScope, Container, AutoCloseable,
    ScapesEngineBackend by backend {
    @PublishedApi
    internal val tasks = TaskChannel<Runnable>()
    private val job = Job()
    override val coroutineContext: CoroutineContext get() = this + job
    override var containerWidth = 0
        private set
    override var containerHeight = 0
        private set
    private val controllerDesktop =
        GLFWControllerDesktop()
    private val joysticks =
        ConcurrentHashMap<Int, GLFWControllerJoystick>()
    private val errorFun =
        GLFWErrorCallback { error, _ ->
            // TODO: Improve logging
            logger.error { "Error $error occurred in GLFW" }
        }
    private val useGLES: Boolean
    override val gos: GraphicsObjectSupplier
    private val gl: GL
    private val initContext: () -> Unit
    private val requestLegacy: (ReadTagMutableMap) -> String?
    var window = GLFWWindow_EMPTY
        private set
    private var refreshRate = 60
    private var contentWidth = 0
    private var contentHeight = 0
    private var mouseX = 0.0
    private var mouseY = 0.0
    @PublishedApi
    internal var running = true
    @PublishedApi
    internal var valid = false
    private var visible = false
    private var focus = true
    private var cursorCaptured = false
    private var mouseDeltaSkip = true
    private var plebSyncEnable = true

    init {
        glfwSetErrorCallback(errorFun)
        if (!glfwInit()) {
            throw GraphicsException("Unable to initialize GLFW")
        }
        logger.info { "GLFW version: ${glfwGetVersionString()}" }
        when (graphicsBackend) {
            is GLBackend -> {
                useGLES = false
                val (a, b) = graphicsBackend.createGL(this)
                gos = a
                gl = b
                initContext = graphicsBackend::initContext
                requestLegacy = graphicsBackend::requestLegacy
            }
            is GLESBackend -> {
                useGLES = true
                val (a, b) = graphicsBackend.createGL(this)
                gos = a
                gl = b
                initContext = graphicsBackend::initContext
                requestLegacy = { null }
            }
            else -> error("Unsupported graphics backend: ${graphicsBackend::class}")
        }
    }

    override val formFactor = Container.FormFactor.DESKTOP

    override fun updateContainer() {
        valid = false
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        tasks.offer(block)
    }

    override fun stop() {
        running = false
    }

    override fun cursorCapture(value: Boolean) {
        launch {
            val cursorCapture = !emulateTouch && value
            if (cursorCapture != cursorCaptured) {
                cursorCaptured = cursorCapture
                mouseX = containerWidth / density * 0.5
                mouseY = containerHeight / density * 0.5
                if (cursorCapture) {
                    glfwSetInputMode(
                        window,
                        GLFW_CURSOR,
                        GLFW_CURSOR_DISABLED
                    )
                    mouseDeltaSkip = true
                } else {
                    mouseX = containerWidth / density * 0.5
                    mouseY = containerHeight / density * 0.5
                    glfwSetInputMode(
                        window,
                        GLFW_CURSOR,
                        GLFW_CURSOR_NORMAL
                    )
                    glfwSetCursorPos(
                        window,
                        mouseX,
                        mouseY
                    )
                }
                controllerDesktop.set(mouseX, mouseY)
            }
        }
    }

    override fun clipboardCopy(value: String) {
        launch {
            glfwSetClipboardString(
                window,
                value
            )
        }
    }

    override fun clipboardPaste(callback: (String) -> Unit) {
        launch {
            callback(
                glfwGetClipboardString(
                    window
                ) ?: ""
            )
        }
    }

    override fun message(
        messageType: Container.MessageType,
        title: String,
        message: String
    ) {
        launch {
            message(
                this@ContainerGLFW,
                messageType,
                title,
                message
            )
        }
    }

    override fun dialog(
        title: String,
        text: GuiController.TextFieldData,
        multiline: Boolean
    ) {
        launch {
            dialog(
                this@ContainerGLFW,
                title,
                text,
                multiline
            )
        }
    }

    override fun isRenderCall() = glfwGetCurrentContext() == window

    @PublishedApi
    internal fun createRunState(engine: ScapesEngine) = RunState(engine)

    override fun close() {
        job.cancel()
        glfwTerminate()
    }

    @PublishedApi
    internal inner class RunState(
        val engine: ScapesEngine
    ) : AutoCloseable {
        val engineConfig = engine[ScapesEngineConfig.COMPONENT]
        val controllers =
            GLFWControllers(
                engine.events,
                joysticks
            )
        val windowSizeFun =
            GLFWWindowSizeCallback { _, width, height ->
                containerWidth = (width * density).roundToInt()
                containerHeight = (height * density).roundToInt()
            }
        val windowCloseFun =
            GLFWWindowCloseCallback { stop() }
        val windowFocusFun =
            GLFWWindowFocusCallback { _, focused ->
                focus = focused
            }
        val frameBufferSizeFun =
            GLFWFramebufferSizeCallback { _, width, height ->
                contentWidth = width
                contentHeight = height
            }
        val keyFun =
            GLFWKeyCallback { _, key, _, action, _ ->
                val virtualKey =
                    glfwKey(key)
                if (virtualKey != null) {
                    if (virtualKey == ControllerKey.KEY_BACKSPACE && action != GLFW_RELEASE) {
                        controllerDesktop.addTypeEvent(
                            127.toChar(),
                            engine.events
                        )
                    }
                    when (action) {
                        GLFW_PRESS -> controllerDesktop.addPressEvent(
                            virtualKey, ControllerButtons.Action.PRESS,
                            engine.events
                        )
                        GLFW_REPEAT -> controllerDesktop.addPressEvent(
                            virtualKey, ControllerButtons.Action.REPEAT,
                            engine.events
                        )
                        GLFW_RELEASE -> controllerDesktop.addPressEvent(
                            virtualKey, ControllerButtons.Action.RELEASE,
                            engine.events
                        )
                    }
                }
                if (key == GLFW_KEY_GRAVE_ACCENT && action == GLFW_PRESS) {
                    plebSyncEnable = !plebSyncEnable
                }
            }
        val charFun =
            GLFWCharCallback { _, codepoint ->
                controllerDesktop.addTypeEvent(
                    codepoint.toChar(),
                    engine.events
                )
            }
        val mouseButtonFun =
            GLFWMouseButtonCallback { _, button, action, _ ->
                val virtualKey = ControllerKey.button(button)
                if (virtualKey != null) {
                    when (action) {
                        GLFW_PRESS -> controllerDesktop.addPressEvent(
                            virtualKey, ControllerButtons.Action.PRESS,
                            engine.events
                        )
                        GLFW_RELEASE -> controllerDesktop.addPressEvent(
                            virtualKey, ControllerButtons.Action.RELEASE,
                            engine.events
                        )
                    }
                }
            }
        val cursorPosFun =
            GLFWCursorPosCallback { window, xpos, ypos ->
                val dx = xpos - mouseX
                val dy = ypos - mouseY
                if (dx != 0.0 || dy != 0.0) {
                    if (cursorCaptured) {
                        glfwSetCursorPos(
                            window,
                            0.0,
                            0.0
                        )
                        mouseX = 0.0
                        mouseY = 0.0
                    } else {
                        controllerDesktop.set(xpos * density, ypos * density)
                        mouseX = xpos
                        mouseY = ypos
                    }
                    if (mouseDeltaSkip) {
                        mouseDeltaSkip = false
                    } else {
                        controllerDesktop.addDelta(dx, dy, engine.events)
                    }
                }
            }
        val scrollFun =
            GLFWScrollCallback { _, xoffset, yoffset ->
                if (xoffset != 0.0 || yoffset != 0.0) {
                    controllerDesktop.addScroll(xoffset, yoffset, engine.events)
                }
            }
        val monitorFun =
            GLFWMonitorCallback { _, _ ->
                refreshRate = refreshRate(
                    window
                ) ?: 60
            }
        val latencyDebug = engine.debugValues["Input-Latency"]
        val plebSyncDebug = engine.debugValues["PlebSync™-Sleep"]
        var plebSync = 0L
        var controllerEmulateTouch: ControllerTouch? = if (emulateTouch) {
            val controllerEmulateTouch = object : ControllerTouch() {
                override val lastActive get() = controllerDesktop.lastActive

                private val lock = ReentrantLock()
                private var tracker: ControllerTracker.Tracker? = null

                override val name = "Extra real touchscreen"

                override fun fingers(): Sequence<ControllerTracker.Tracker> {
                    lock.withLock {
                        val tracker = tracker
                        if (tracker != null) {
                            if (controllerDesktop.isDown(
                                    ControllerKey.BUTTON_0
                                )) {
                                tracker.pos.setXY(
                                    controllerDesktop.x,
                                    controllerDesktop.y
                                )
                            } else {
                                this.tracker = null
                            }
                        } else if (controllerDesktop.isDown(
                                ControllerKey.BUTTON_0
                            )) {
                            val newTracker = ControllerTracker.Tracker()
                            newTracker.pos.setXY(
                                controllerDesktop.x,
                                controllerDesktop.y
                            )
                            this.tracker = newTracker
                        }
                        return tracker?.let {
                            sequenceOf(it)
                        } ?: emptySequence()
                    }
                }
            }
            engine.events.fire(Controller.AddEvent(controllerEmulateTouch))
            controllerEmulateTouch
        } else {
            engine.events.fire(Controller.AddEvent(controllerDesktop))
            null
        }
        val timer = Timer()

        fun init() {
            glfwSetMonitorCallback(
                monitorFun
            )
            controllers.init()
            timer.init()
        }

        fun initWindow() {
            engine.graphics.reset()
            controllerDesktop.clearStates()
            visible = false
            if (window != GLFWWindow_EMPTY) {
                disposeWindow(window)
            }
            val requestLegacy: () -> String? = {
                requestLegacy(engineConfig.configMap)
            }
            window = initWindow(
                initContext, requestLegacy, title,
                engineConfig.fullscreen, engineConfig.vSync,
                useGLES, this
            )
            refreshRate = refreshRate(
                window
            ) ?: 60
            val widthBuffer = IntArray(1)
            val heightBuffer = IntArray(1)
            glfwGetWindowSize(
                window,
                widthBuffer,
                heightBuffer
            )
            containerWidth = (widthBuffer[0] * density).roundToInt()
            containerHeight = (heightBuffer[0] * density).roundToInt()
            glfwGetFramebufferSize(
                window, widthBuffer,
                heightBuffer
            )
            contentWidth = widthBuffer[0]
            contentHeight = heightBuffer[0]
            valid = true
            if (cursorCaptured) {
                mouseX = containerWidth / density * 0.5
                mouseY = containerHeight / density * 0.5
                controllerDesktop.set(mouseX, mouseY)
            }
        }

        fun poll() {
            glfwPollEvents()
            controllers.poll()
        }

        fun render(tickDiff: Long) = profilerSection("Render") {
            engine.graphics.render(
                gl,
                Timer.toDelta(tickDiff).coerceIn(0.0001, 0.1),
                contentWidth,
                contentHeight,
                containerWidth,
                containerHeight
            )
        }

        fun swap(
            swap: Boolean,
            vSync: Boolean,
            time: InstantSteadyNanos,
            start: InstantSteadyNanos
        ) {
            if (swap) {
                glfwSwapBuffers(window)
                if (!visible) {
                    glfwShowWindow(window)
                    visible = true
                }
            }
            if (vSync && plebSyncEnable) {
                val maxDiff = 1000000000L / refreshRate
                val latency = steadyClock.timeSteadyNanos() - time
                val delta = steadyClock.timeSteadyNanos() - start
                val targetDelta = max(maxDiff - latency - plebSyncGap, 0L)
                val diff = delta - targetDelta
                plebSync = clamp(plebSync + diff, 0L, targetDelta)
                latencyDebug.setValue(latency / 1000000L)
                plebSyncDebug.setValue(plebSync)
            } else {
                plebSync = 0L
            }
        }

        fun end() {
            controllerEmulateTouch?.let {
                engine.events.fire(Controller.RemoveEvent(it))
            }
            if (!emulateTouch) {
                engine.events.fire(Controller.RemoveEvent(controllerDesktop))
            }
            logger.info { "Disposing graphics system" }
            engine.graphics.dispose(gl)
        }

        override fun close() {
            disposeWindow(window)
            glfwSetMonitorCallback(null)
            controllers.dispose()
            windowSizeFun.close()
            windowCloseFun.close()
            windowFocusFun.close()
            frameBufferSizeFun.close()
            keyFun.close()
            charFun.close()
            mouseButtonFun.close()
            cursorPosFun.close()
            scrollFun.close()
            monitorFun.close()
        }
    }

    companion object {
        internal val logger = KLogger<ContainerGLFW>()
    }
}

inline fun ContainerGLFW.run(
    engine: ScapesEngine,
    delayNanos: (Long) -> Unit,
    yield: () -> Unit = {}
) {
    createRunState(engine).use { state ->
        state.init()
        var tickDiff = 0L
        while (running) {
            val start = steadyClock.timeSteadyNanos()
            if (!valid) state.initWindow()
            tasks.processCurrent { it.run() }
            if (state.plebSync > 0) delayNanos(state.plebSync)
            val time = steadyClock.timeSteadyNanos()
            state.poll()
            val swap = state.render(tickDiff)
            yield()
            val vSync = state.engineConfig.vSync
            if (vSync) {
                tickDiff = state.timer.tick()
            } else {
                tickDiff = state.timer.cap(
                    Timer.toDiff(state.engineConfig.fps),
                    { delayNanos(it) }
                )
            }
            state.swap(swap, vSync, time, start)
        }
        state.end()
    }
}

suspend fun ContainerGLFW.runSuspending(
    engine: ScapesEngine
) = run(engine, { delayNanos(it) }, { yield() })

private val plebSyncGap
    get() = when (PLATFORM) {
        // Causes severe lag on Windows, but obviously Windows is THE
        // best "gaming" OS
        // Platform.WINDOWS -> 40000L
        else -> 20000L
    }

private fun initWindow(
    initContext: () -> Unit,
    requestLegacy: () -> String?,
    title: String,
    fullscreen: Boolean,
    vSync: Boolean,
    useGLES: Boolean,
    state: ContainerGLFW.RunState
): GLFWWindow {
    ContainerGLFW.logger.info { "Creating GLFW window..." }
    val monitor = glfwGetPrimaryMonitor()
    val videoMode =
        if (monitor == GLFWMonitor_EMPTY) null else glfwGetVideoMode(monitor)
    val monitorWidth = videoMode?.width ?: 1280
    val monitorHeight = videoMode?.height ?: 720
    glfwDefaultWindowHints()
    val window = if (useGLES) {
        initContextGLES()
        val window = initWindow(
            title, fullscreen, monitor,
            monitorWidth, monitorHeight
        )
        glfwMakeContextCurrent(window)
        window
    } else {
        initContextGL()
        var window = initWindow(
            title, fullscreen, monitor,
            monitorWidth, monitorHeight
        )
        glfwMakeContextCurrent(window)
        val legacy = requestLegacy()
        if (legacy != null) {
            ContainerGLFW.logger.warn { "Detected problem with using a core profile on this driver: $legacy" }
            ContainerGLFW.logger.warn { "Recreating window with legacy context..." }
            glfwDestroyWindow(window)
            glfwDefaultWindowHints()
            initContextGL(true)
            window = initWindow(
                title, fullscreen, monitor,
                monitorWidth, monitorHeight
            )
            glfwMakeContextCurrent(window)
        }
        window
    }
    initContext()
    glfwSetWindowSizeCallback(
        window,
        state.windowSizeFun
    )
    glfwSetWindowCloseCallback(
        window,
        state.windowCloseFun
    )
    glfwSetWindowFocusCallback(
        window,
        state.windowFocusFun
    )
    glfwSetFramebufferSizeCallback(
        window,
        state.frameBufferSizeFun
    )
    glfwSetKeyCallback(
        window,
        state.keyFun
    )
    glfwSetCharCallback(
        window,
        state.charFun
    )
    glfwSetMouseButtonCallback(
        window,
        state.mouseButtonFun
    )
    glfwSetCursorPosCallback(
        window,
        state.cursorPosFun
    )
    glfwSetScrollCallback(
        window,
        state.scrollFun
    )
    glfwSwapInterval(if (vSync) 1 else 0)
    return window
}

private fun disposeWindow(
    window: GLFWWindow
) {
    glfwSetWindowSizeCallback(
        window,
        null
    )
    glfwSetWindowCloseCallback(
        window,
        null
    )
    glfwSetWindowFocusCallback(
        window,
        null
    )
    glfwSetFramebufferSizeCallback(
        window,
        null
    )
    glfwSetKeyCallback(window, null)
    glfwSetCharCallback(window, null)
    glfwSetMouseButtonCallback(
        window,
        null
    )
    glfwSetCursorPosCallback(
        window,
        null
    )
    glfwSetScrollCallback(window, null)
    glfwDestroyWindow(window)
}

private fun initContextGL(contextLegacy: Boolean = false) {
    if (!contextLegacy) {
        glfwWindowHint(
            GLFW_CONTEXT_VERSION_MAJOR,
            3
        )
        glfwWindowHint(
            GLFW_CONTEXT_VERSION_MINOR,
            3
        )
        glfwWindowHint(
            GLFW_OPENGL_PROFILE,
            GLFW_OPENGL_CORE_PROFILE
        )
        glfwWindowHint(
            GLFW_OPENGL_FORWARD_COMPAT,
            GLFW_TRUE
        )
    }
}

private fun initContextGLES() {
    glfwWindowHint(
        GLFW_CLIENT_API,
        GLFW_OPENGL_ES_API
    )
    glfwWindowHint(
        GLFW_CONTEXT_VERSION_MAJOR,
        3
    )
    glfwWindowHint(
        GLFW_CONTEXT_VERSION_MINOR,
        0
    )
}

private fun initWindow(
    title: String,
    fullscreen: Boolean,
    monitor: GLFWMonitor,
    monitorWidth: Int,
    monitorHeight: Int
): GLFWWindow {
    glfwWindowHint(
        GLFW_VISIBLE,
        GLFW_FALSE
    )
    // >:V Seriously, stop with this crap!
    glfwWindowHint(
        GLFW_AUTO_ICONIFY,
        GLFW_FALSE
    )
    return if (fullscreen) {
        val window = glfwCreateWindow(
            monitorWidth, monitorHeight, title, monitor, 0L
        )
        if (window == GLFWWindow_EMPTY) {
            throw GraphicsCheckException(
                "Failed to create fullscreen window"
            )
        }
        window
    } else {
        val width: Int
        val height: Int
        if (monitorWidth > 1280 && monitorHeight > 720) {
            width = 1280
            height = 720
        } else {
            width = 960
            height = 540
        }
        val window = glfwCreateWindow(
            width,
            height,
            title,
            GLFWMonitor_EMPTY,
            0L
        )
        if (window == GLFWWindow_EMPTY) {
            throw GraphicsCheckException("Failed to create window")
        }
        window
    }
}

private fun refreshRate(window: GLFWWindow): Int? {
    val monitor =
        glfwGetWindowMonitor(window)
    if (monitor != GLFWMonitor_EMPTY) {
        return glfwGetVideoMode(monitor)?.refreshRate ?: 60
    }
    // We use the maximum refresh rate to avoid slowing rendering in
    // case of different rates
    // GLFW sadly does not seem to support fetching the current monitor
    // of non-fullscreen windows
    val monitors = glfwGetMonitors() ?: return null
    var max = 0
    for (i in 0 until monitors.size) {
        glfwGetVideoMode(monitors[i])?.let {
            max = max.coerceAtLeast(it.refreshRate)
        }
    }
    return max
}
