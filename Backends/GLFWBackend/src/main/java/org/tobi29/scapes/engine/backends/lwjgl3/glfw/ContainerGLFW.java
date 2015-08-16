/*
 * Copyright 2012-2015 Tobi29
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

package org.tobi29.scapes.engine.backends.lwjgl3.glfw;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.ScapesEngineException;
import org.tobi29.scapes.engine.backends.lwjgl3.ContainerLWJGL3;
import org.tobi29.scapes.engine.backends.lwjgl3.GLFWControllers;
import org.tobi29.scapes.engine.backends.lwjgl3.GLFWKeyMap;
import org.tobi29.scapes.engine.backends.lwjgl3.STBGlyphRenderer;
import org.tobi29.scapes.engine.backends.lwjgl3.glfw.spi.GLFWDialogsProvider;
import org.tobi29.scapes.engine.gui.GlyphRenderer;
import org.tobi29.scapes.engine.input.ControllerJoystick;
import org.tobi29.scapes.engine.input.ControllerKey;
import org.tobi29.scapes.engine.opengl.GraphicsCheckException;
import org.tobi29.scapes.engine.opengl.GraphicsException;
import org.tobi29.scapes.engine.utils.BufferCreatorNative;
import org.tobi29.scapes.engine.utils.DesktopException;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.Sync;
import org.tobi29.scapes.engine.utils.io.tag.TagStructure;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerGLFW extends ContainerLWJGL3 {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ContainerGLFW.class);
    private static final GLFWDialogsProvider DIALOGS_PROVIDER = loadDialogs();
    private final PlatformDialogs dialogs;
    private final Sync sync;
    private final GLFWControllers controllers;
    private final Map<Integer, ControllerJoystick> virtualJoysticks =
            new ConcurrentHashMap<>();
    @SuppressWarnings("FieldCanBeLocal")
    private final GLFWErrorCallback errorFun;
    private final GLFWWindowSizeCallback windowSizeFun;
    private final GLFWWindowCloseCallback windowCloseFun;
    private final GLFWWindowFocusCallback windowFocusFun;
    private final GLFWFramebufferSizeCallback frameBufferSizeFun;
    private final GLFWKeyCallback keyFun;
    private final GLFWCharCallback charFun;
    private final GLFWMouseButtonCallback mouseButtonFun;
    private final GLFWCursorPosCallback cursorPosFun;
    private final GLFWScrollCallback scrollFun;
    private long window;
    private boolean running = true, skipMouseCallback, mouseGrabbed;

    public ContainerGLFW(ScapesEngine engine) {
        super(engine);
        dialogs = DIALOGS_PROVIDER.createDialogs(engine);
        errorFun = Callbacks.errorCallbackPrint();
        GLFW.glfwSetErrorCallback(errorFun);
        if (GLFW.glfwInit() != GL11.GL_TRUE) {
            throw new GraphicsException("Unable to initialize GLFW");
        }
        LOGGER.info("GLFW version: {}", GLFW.glfwGetVersionString());
        sync = new Sync(engine.config().fps(), 5000000000L, false, "Rendering");
        controllers = new GLFWControllers(virtualJoysticks);
        windowSizeFun = GLFW.GLFWWindowSizeCallback((window, width, height) -> {
            containerWidth = width;
            containerHeight = height;
            containerResized = true;
        });
        windowCloseFun = GLFW.GLFWWindowCloseCallback(window -> engine.stop());
        windowFocusFun = GLFW.GLFWWindowFocusCallback(
                (window, focused) -> focus = focused == GL11.GL_TRUE);
        frameBufferSizeFun =
                GLFW.GLFWFramebufferSizeCallback((window, width, height) -> {
                    contentWidth = width;
                    contentHeight = height;
                    containerResized = true;
                });
        keyFun = GLFW.GLFWKeyCallback((window, key, scancode, action, mods) -> {
            ControllerKey virtualKey = GLFWKeyMap.key(key);
            if (virtualKey != null) {
                if (virtualKey == ControllerKey.KEY_BACKSPACE &&
                        action != GLFW.GLFW_RELEASE) {
                    addTypeEvent((char) 127);
                }
                switch (action) {
                    case GLFW.GLFW_PRESS:
                        addPressEvent(virtualKey, PressState.PRESS);
                        break;
                    case GLFW.GLFW_REPEAT:
                        addPressEvent(virtualKey, PressState.REPEAT);
                        break;
                    case GLFW.GLFW_RELEASE:
                        addPressEvent(virtualKey, PressState.RELEASE);
                        break;
                }
            }
        });
        charFun = GLFW.GLFWCharCallback(
                (window, codepoint) -> addTypeEvent((char) codepoint));
        mouseButtonFun =
                GLFW.GLFWMouseButtonCallback((window, button, action, mods) -> {
                    ControllerKey virtualKey = ControllerKey.button(button);
                    if (virtualKey != ControllerKey.UNKNOWN) {
                        switch (action) {
                            case GLFW.GLFW_PRESS:
                                addPressEvent(virtualKey, PressState.PRESS);
                                break;
                            case GLFW.GLFW_RELEASE:
                                addPressEvent(virtualKey, PressState.RELEASE);
                                break;
                        }
                    }
                });
        cursorPosFun = GLFW.GLFWCursorPosCallback((window, xpos, ypos) -> {
            if (skipMouseCallback) {
                skipMouseCallback = false;
                if (mouseGrabbed) {
                    GLFW.glfwSetCursorPos(window, 0.0, 0.0);
                }
            } else {
                double dx, dy;
                if (mouseGrabbed) {
                    dx = xpos;
                    dy = ypos;
                    GLFW.glfwSetCursorPos(window, 0.0, 0.0);
                } else {
                    dx = xpos - mouseX;
                    dy = ypos - mouseY;
                    mouseX = (int) xpos;
                    mouseY = (int) ypos;
                }
                if (dx != 0.0 || dy != 0.0) {
                    set(xpos, ypos);
                    addDelta(dx, dy);
                }
            }
        });
        scrollFun = GLFW.GLFWScrollCallback((window, xoffset, yoffset) -> {
            if (xoffset != 0.0 || yoffset != 0.0) {
                addScroll(xoffset, yoffset);
            }
        });
    }

    private static GLFWDialogsProvider loadDialogs() {
        for (GLFWDialogsProvider dialogs : ServiceLoader
                .load(GLFWDialogsProvider.class)) {
            try {
                if (dialogs.available()) {
                    LOGGER.debug("Loaded dialogs: {}",
                            dialogs.getClass().getName());
                    return dialogs;
                }
            } catch (ServiceConfigurationError e) {
                LOGGER.warn("Unable to load dialogs provider: {}",
                        e.toString());
            }
        }
        throw new ScapesEngineException("No dialogs found!");
    }

    @Override
    public void setMouseGrabbed(boolean value) {
        if (value) {
            mouseGrabbed = true;
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR,
                    GLFW.GLFW_CURSOR_DISABLED);
            GLFW.glfwSetCursorPos(window, 0.0, 0.0);
            mouseX = 0.0;
            mouseY = 0.0;
            skipMouseCallback = true;
        } else {
            mouseGrabbed = false;
            mouseX = containerWidth * 0.5;
            mouseY = containerHeight * 0.5;
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR,
                    GLFW.GLFW_CURSOR_NORMAL);
            GLFW.glfwSetCursorPos(window, mouseX, mouseY);
        }
    }

    @Override
    public Collection<ControllerJoystick> joysticks() {
        joysticksChanged = false;
        Collection<ControllerJoystick> collection =
                new ArrayList<>(virtualJoysticks.size());
        collection.addAll(virtualJoysticks.values());
        return collection;
    }

    @Override
    public boolean loadFont(String asset) {
        return STBGlyphRenderer.loadFont(engine.files().get(asset + ".ttf"));
    }

    @Override
    public GlyphRenderer createGlyphRenderer(String fontName, int size) {
        return STBGlyphRenderer.fromFont(fontName, size);
    }

    @Override
    public void run() throws DesktopException {
        sync.init();
        while (running) {
            while (!tasks.isEmpty()) {
                tasks.poll().run();
            }
            if (!valid) {
                if (context != null) {
                    engine.graphics().reset();
                    cleanWindow();
                }
                initWindow(engine.config().isFullscreen(),
                        engine.config().vSync());
                Optional<String> check = initContext();
                if (check.isPresent()) {
                    throw new GraphicsCheckException(check.get());
                }
                valid = true;
                containerResized = true;
            }
            GLFW.glfwPollEvents();
            joysticksChanged = controllers.poll();
            engine.render(sync.delta());
            containerResized = false;
            sync.cap();
            GLFW.glfwSwapBuffers(window);
            if (!visible) {
                GLFW.glfwShowWindow(window);
                visible = true;
            }
        }
        engine.dispose();
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
        windowSizeFun.release();
        windowCloseFun.release();
        windowFocusFun.release();
        frameBufferSizeFun.release();
        keyFun.release();
        charFun.release();
        mouseButtonFun.release();
        cursorPosFun.release();
        scrollFun.release();
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public Path[] openFileDialog(Pair<String, String>[] extensions,
            String title, boolean multiple) {
        return exec(() -> dialogs.openFileDialog(extensions, title, multiple));
    }

    @Override
    public Optional<Path> saveFileDialog(Pair<String, String>[] extensions,
            String title) {
        return exec(() -> dialogs.saveFileDialog(extensions, title));
    }

    @Override
    public void message(MessageType messageType, String title, String message) {
        exec(() -> dialogs.message(messageType, title, message));
    }

    @Override
    public void openFile(Path path) {
        exec(() -> dialogs.openFile(path));
    }

    protected void initWindow(boolean fullscreen, boolean vSync) {
        LOGGER.info("Creating GLFW window...");
        String title = engine.game().name();
        long monitor = GLFW.glfwGetPrimaryMonitor();
        IntBuffer xBuffer = BufferCreatorNative.intsD(1);
        IntBuffer yBuffer = BufferCreatorNative.intsD(1);
        GLFW.glfwGetMonitorPos(monitor, xBuffer, yBuffer);
        ByteBuffer videoMode = GLFW.glfwGetVideoMode(monitor);
        int monitorX = xBuffer.get(0);
        int monitorY = yBuffer.get(0);
        int monitorWidth = GLFWvidmode.width(videoMode);
        int monitorHeight = GLFWvidmode.height(videoMode);
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
        // >:V Seriously, stop with this crap!
        GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY, GL11.GL_FALSE);
        TagStructure tagStructure = engine.tagStructure();
        if (!tagStructure.has("Compatibility") ||
                !engine.tagStructure().getStructure("Compatibility")
                        .getBoolean("ForceLegacyGL")) {
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE,
                    GLFW.GLFW_OPENGL_CORE_PROFILE);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);
        }
        if (fullscreen) {
            window = GLFW.glfwCreateWindow(monitorWidth, monitorHeight, title,
                    monitor, 0L);
        } else {
            int width, height;
            if (monitorWidth > 1280 && monitorHeight > 720) {
                width = 1280;
                height = 720;
            } else {
                width = 960;
                height = 540;
            }
            window = GLFW.glfwCreateWindow(width, height, title, 0L, 0L);
            GLFW.glfwSetWindowPos(window, monitorX + (monitorWidth - width) / 2,
                    monitorY + (monitorHeight - height) / 2);
        }
        IntBuffer widthBuffer = BufferCreatorNative.intsD(1);
        IntBuffer heightBuffer = BufferCreatorNative.intsD(1);
        GLFW.glfwGetWindowSize(window, widthBuffer, heightBuffer);
        containerWidth = widthBuffer.get(0);
        containerHeight = heightBuffer.get(0);
        GLFW.glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
        contentWidth = widthBuffer.get(0);
        contentHeight = heightBuffer.get(0);
        GLFW.glfwSetWindowSizeCallback(window, windowSizeFun);
        GLFW.glfwSetWindowCloseCallback(window, windowCloseFun);
        GLFW.glfwSetWindowFocusCallback(window, windowFocusFun);
        GLFW.glfwSetFramebufferSizeCallback(window, frameBufferSizeFun);
        GLFW.glfwSetKeyCallback(window, keyFun);
        GLFW.glfwSetCharCallback(window, charFun);
        GLFW.glfwSetMouseButtonCallback(window, mouseButtonFun);
        GLFW.glfwSetCursorPosCallback(window, cursorPosFun);
        GLFW.glfwSetScrollCallback(window, scrollFun);
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(vSync ? 1 : 0);
    }

    protected void cleanWindow() {
        clearStates();
        GLFW.glfwDestroyWindow(window);
        visible = false;
    }

    @Override
    public void clipboardCopy(String value) {
        GLFW.glfwSetClipboardString(window, value);
    }

    @Override
    public String clipboardPaste() {
        return GLFW.glfwGetClipboardString(window);
    }
}
