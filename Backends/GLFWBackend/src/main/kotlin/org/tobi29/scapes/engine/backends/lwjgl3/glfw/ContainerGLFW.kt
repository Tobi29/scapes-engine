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

package org.tobi29.scapes.engine.backends.lwjgl3.glfw

import mu.KLogging
import org.lwjgl.glfw.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengles.GLES
import org.lwjgl.system.MemoryStack
import org.tobi29.scapes.engine.Container
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.backends.lwjgl3.ContainerLWJGL3
import org.tobi29.scapes.engine.backends.lwjgl3.GLFWControllers
import org.tobi29.scapes.engine.backends.lwjgl3.GLFWKeyMap
import org.tobi29.scapes.engine.backends.lwjgl3.push
import org.tobi29.scapes.engine.graphics.GraphicsCheckException
import org.tobi29.scapes.engine.graphics.GraphicsException
import org.tobi29.scapes.engine.gui.GuiController
import org.tobi29.scapes.engine.input.*
import org.tobi29.scapes.engine.utils.Sync
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath
import org.tobi29.scapes.engine.utils.profiler.profilerSection
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ContainerGLFW(engine: ScapesEngine) : ContainerLWJGL3(engine) {
    private val sync: Sync
    private val controllers: GLFWControllers
    private val virtualJoysticks = ConcurrentHashMap<Int, ControllerJoystick>()
    private val errorFun: GLFWErrorCallback
    private val windowSizeFun: GLFWWindowSizeCallback
    private val windowCloseFun: GLFWWindowCloseCallback
    private val windowFocusFun: GLFWWindowFocusCallback
    private val frameBufferSizeFun: GLFWFramebufferSizeCallback
    private val keyFun: GLFWKeyCallback
    private val charFun: GLFWCharCallback
    private val mouseButtonFun: GLFWMouseButtonCallback
    private val cursorPosFun: GLFWCursorPosCallback
    private val scrollFun: GLFWScrollCallback
    private var window: Long = 0
    private var running = true
    private var mouseGrabbed = false
    private var mouseGrabbedCurrent = false

    init {
        errorFun = GLFWErrorCallback.createPrint()
        GLFW.glfwSetErrorCallback(errorFun)
        if (!GLFW.glfwInit()) {
            throw GraphicsException("Unable to initialize GLFW")
        }
        logger.info { "GLFW version: ${GLFW.glfwGetVersionString()}" }
        sync = Sync(engine.config.fps, 5000000000L, false,
                "Rendering")
        controllers = GLFWControllers(virtualJoysticks)
        windowSizeFun = GLFWWindowSizeCallback.create { window, width, height ->
            containerWidth = width
            containerHeight = height
            containerResized = true
        }
        windowCloseFun = GLFWWindowCloseCallback.create { engine.stop() }
        windowFocusFun = GLFWWindowFocusCallback.create { window, focused -> focus = focused }
        frameBufferSizeFun = GLFWFramebufferSizeCallback.create { window, width, height ->
            contentWidth = width
            contentHeight = height
            containerResized = true
        }
        keyFun = GLFWKeyCallback.create { window, key, scancode, action, mods ->
            val virtualKey = GLFWKeyMap.key(key)
            if (virtualKey != null) {
                if (virtualKey === ControllerKey.KEY_BACKSPACE && action != GLFW.GLFW_RELEASE) {
                    addTypeEvent(127.toChar())
                }
                when (action) {
                    GLFW.GLFW_PRESS -> addPressEvent(virtualKey,
                            ControllerBasic.PressState.PRESS)
                    GLFW.GLFW_REPEAT -> addPressEvent(virtualKey,
                            ControllerBasic.PressState.REPEAT)
                    GLFW.GLFW_RELEASE -> addPressEvent(virtualKey,
                            ControllerBasic.PressState.RELEASE)
                }
            }
        }
        charFun = GLFWCharCallback.create { window, codepoint ->
            addTypeEvent(codepoint.toChar())
        }
        mouseButtonFun = GLFWMouseButtonCallback.create { window, button, action, mods ->
            val virtualKey = ControllerKey.button(button)
            if (virtualKey != null) {
                when (action) {
                    GLFW.GLFW_PRESS -> addPressEvent(virtualKey,
                            ControllerBasic.PressState.PRESS)
                    GLFW.GLFW_RELEASE -> addPressEvent(virtualKey,
                            ControllerBasic.PressState.RELEASE)
                }
            }
        }
        cursorPosFun = GLFWCursorPosCallback.create { window, xpos, ypos ->
            val dx = xpos - mouseX
            val dy = ypos - mouseY
            if (dx != 0.0 || dy != 0.0) {
                mouseX = xpos
                mouseY = ypos
                if (!mouseGrabbedCurrent) {
                    set(mouseX, mouseY)
                }
                addDelta(dx, dy)
            }
        }
        scrollFun = GLFWScrollCallback.create { window, xoffset, yoffset ->
            if (xoffset != 0.0 || yoffset != 0.0) {
                addScroll(xoffset, yoffset)
            }
        }
    }

    override fun formFactor(): Container.FormFactor {
        return Container.FormFactor.DESKTOP
    }

    override fun setMouseGrabbed(value: Boolean) {
        mouseGrabbed = value
    }

    override fun update(delta: Double) {
        if (isPressed(ControllerKey.KEY_F2)) {
            engine.graphics.triggerScreenshot()
        }
        if (isPressed(ControllerKey.KEY_F3)) {
            if (isDown(ControllerKey.KEY_LEFT_CONTROL)) {
                engine.writeCrash(Throwable("Debug report"))
            } else if (engine.debug() && isDown(ControllerKey.KEY_LEFT_SHIFT)) {
                val profiler = engine.profiler
                profiler.isVisible = !profiler.isVisible
            } else if (engine.debug()) {
                val debugValues = engine.debugValues
                debugValues.isVisible = !debugValues.isVisible
            }
        }
    }

    override fun joysticks(): Collection<ControllerJoystick> {
        joysticksChanged.set(false)
        val collection = ArrayList<ControllerJoystick>(virtualJoysticks.size)
        collection.addAll(virtualJoysticks.values)
        return collection
    }

    override fun touch(): ControllerTouch? {
        return null
    }

    override fun run() {
        sync.init()
        while (running) {
            while (!tasks.isEmpty()) {
                tasks.poll()()
            }
            if (!valid) {
                if (window != 0L) {
                    engine.graphics.reset()
                    cleanWindow()
                }
                initWindow(engine.config.fullscreen, engine.config.vSync)
                if (useGLES) {
                    GLES.createCapabilities()
                } else {
                    GL.createCapabilities()
                }
                checkContext()?.let { throw GraphicsCheckException(it) }
                gl.init()
                valid = true
                containerResized = true
                if (mouseGrabbedCurrent) {
                    mouseX = containerWidth * 0.5
                    mouseY = containerHeight * 0.5
                    set(mouseX, mouseY)
                }
            }
            profilerSection("Render") {
                engine.graphics.render(sync.delta())
            }
            containerResized = false
            val mouseGrabbed = this.mouseGrabbed
            if (mouseGrabbed != mouseGrabbedCurrent) {
                mouseGrabbedCurrent = mouseGrabbed
                mouseX = containerWidth * 0.5
                mouseY = containerHeight * 0.5
                if (mouseGrabbed) {
                    GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR,
                            GLFW.GLFW_CURSOR_DISABLED)
                } else {
                    mouseX = containerWidth * 0.5
                    mouseY = containerHeight * 0.5
                    GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR,
                            GLFW.GLFW_CURSOR_NORMAL)
                    GLFW.glfwSetCursorPos(window, mouseX, mouseY)
                }
                set(mouseX, mouseY)
            }
            GLFW.glfwPollEvents()
            if (controllers.poll()) {
                joysticksChanged.set(true)
            }
            sync.cap()
            GLFW.glfwSwapBuffers(window)
            if (!visible) {
                GLFW.glfwShowWindow(window)
                visible = true
            }
        }
        logger.info { "Disposing graphics system" }
        engine.graphics.dispose()
        engine.dispose()
        GLFW.glfwDestroyWindow(window)
        GLFW.glfwTerminate()
        windowSizeFun.close()
        windowCloseFun.close()
        windowFocusFun.close()
        frameBufferSizeFun.close()
        keyFun.close()
        charFun.close()
        mouseButtonFun.close()
        cursorPosFun.close()
        scrollFun.close()
    }

    override fun stop() {
        running = false
    }

    override fun clipboardCopy(value: String) {
        GLFW.glfwSetClipboardString(window, value)
    }

    override fun clipboardPaste(): String {
        return GLFW.glfwGetClipboardString(window)
    }

    override fun openFileDialog(type: FileType,
                                title: String,
                                multiple: Boolean,
                                result: Function2<String, ReadableByteStream, Unit>) {
        exec {
            PlatformDialogs.openFileDialog(window, type.extensions, multiple,
                    result)
        }
    }

    override fun saveFileDialog(extensions: Array<Pair<String, String>>,
                                title: String): FilePath? {
        return exec {
            PlatformDialogs.saveFileDialog(window, extensions)
        }
    }

    override fun message(messageType: Container.MessageType,
                         title: String,
                         message: String) {
        // FIXME: No implementation
    }

    override fun dialog(title: String,
                        text: GuiController.TextFieldData,
                        multiline: Boolean) {
        // FIXME: No implementation
    }

    override fun openFile(path: FilePath) {
        PlatformDialogs.openFile(path)
    }

    private fun initWindow(fullscreen: Boolean,
                           vSync: Boolean) {
        val stack = MemoryStack.stackGet()
        stack.push {
            logger.info { "Creating GLFW window..." }
            val title = engine.game.name
            val monitor = GLFW.glfwGetPrimaryMonitor()
            val videoMode = GLFW.glfwGetVideoMode(monitor)
            val monitorWidth = videoMode.width()
            val monitorHeight = videoMode.height()
            GLFW.glfwDefaultWindowHints()
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE)
            // >:V Seriously, stop with this crap!
            GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY, GL11.GL_FALSE)
            if (useGLES) {
                GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API,
                        GLFW.GLFW_OPENGL_ES_API)
                GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
                GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 0)
            } else if (!(engine.tagStructure.getStructure(
                    "Compatibility")?.getBoolean("ForceLegacyGL") ?: false)) {
                GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
                GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3)
                GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE,
                        GLFW.GLFW_OPENGL_CORE_PROFILE)
                GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT,
                        GL11.GL_TRUE)
            }
            if (fullscreen) {
                window = GLFW.glfwCreateWindow(monitorWidth, monitorHeight,
                        title, monitor, 0L)
                if (window == 0L) {
                    throw GraphicsCheckException(
                            "Failed to create fullscreen window")
                }
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
                window = GLFW.glfwCreateWindow(width, height, title, 0L, 0L)
                if (window == 0L) {
                    throw GraphicsCheckException("Failed to create window")
                }
            }
            val widthBuffer = stack.mallocInt(1)
            val heightBuffer = stack.mallocInt(1)
            GLFW.glfwGetWindowSize(window, widthBuffer, heightBuffer)
            containerWidth = widthBuffer.get(0)
            containerHeight = heightBuffer.get(0)
            GLFW.glfwGetFramebufferSize(window, widthBuffer, heightBuffer)
            contentWidth = widthBuffer.get(0)
            contentHeight = heightBuffer.get(0)
            GLFW.glfwSetWindowSizeCallback(window, windowSizeFun)
            GLFW.glfwSetWindowCloseCallback(window, windowCloseFun)
            GLFW.glfwSetWindowFocusCallback(window, windowFocusFun)
            GLFW.glfwSetFramebufferSizeCallback(window, frameBufferSizeFun)
            GLFW.glfwSetKeyCallback(window, keyFun)
            GLFW.glfwSetCharCallback(window, charFun)
            GLFW.glfwSetMouseButtonCallback(window, mouseButtonFun)
            GLFW.glfwSetCursorPosCallback(window, cursorPosFun)
            GLFW.glfwSetScrollCallback(window, scrollFun)
            GLFW.glfwMakeContextCurrent(window)
            GLFW.glfwSwapInterval(if (vSync) 1 else 0)
        }
    }

    private fun cleanWindow() {
        clearStates()
        GLFW.glfwDestroyWindow(window)
        window = 0
        visible = false
    }

    companion object : KLogging()
}
