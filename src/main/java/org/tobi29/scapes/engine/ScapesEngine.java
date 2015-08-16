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

package org.tobi29.scapes.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.gui.Gui;
import org.tobi29.scapes.engine.gui.GuiAlignment;
import org.tobi29.scapes.engine.gui.GuiController;
import org.tobi29.scapes.engine.gui.GuiControllerDefault;
import org.tobi29.scapes.engine.gui.debug.GuiDebugLayer;
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues;
import org.tobi29.scapes.engine.input.ControllerDefault;
import org.tobi29.scapes.engine.input.ControllerKey;
import org.tobi29.scapes.engine.openal.SoundSystem;
import org.tobi29.scapes.engine.opengl.Container;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.GraphicsCheckException;
import org.tobi29.scapes.engine.opengl.GraphicsSystem;
import org.tobi29.scapes.engine.spi.ScapesEngineBackendProvider;
import org.tobi29.scapes.engine.utils.Crashable;
import org.tobi29.scapes.engine.utils.Sync;
import org.tobi29.scapes.engine.utils.io.filesystem.CrashReportFile;
import org.tobi29.scapes.engine.utils.io.filesystem.FileCache;
import org.tobi29.scapes.engine.utils.io.filesystem.FileSystemContainer;
import org.tobi29.scapes.engine.utils.io.filesystem.FileUtil;
import org.tobi29.scapes.engine.utils.io.filesystem.classpath.ClasspathPath;
import org.tobi29.scapes.engine.utils.io.tag.TagStructure;
import org.tobi29.scapes.engine.utils.io.tag.TagStructureJSON;
import org.tobi29.scapes.engine.utils.task.Joiner;
import org.tobi29.scapes.engine.utils.task.TaskExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class ScapesEngine implements Crashable {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ScapesEngine.class);
    private static ScapesEngine instance;
    private final Container container;
    private final GraphicsSystem graphics;
    private final SoundSystem sounds;
    private final ControllerDefault controller;
    private final Game game;
    private final Gui globalGui;
    private final Runtime runtime;
    private final TagStructure tagStructure;
    private final ScapesEngineConfig config;
    private final Path home, temp;
    private final FileSystemContainer assets;
    private final FileCache fileCache;
    private final TaskExecutor taskExecutor;
    private final boolean debug;
    private final GuiDebugLayer debugGui;
    private final GuiWidgetDebugValues.Element usedMemoryDebug, maxMemoryDebug;
    private GuiController guiController;
    private boolean mouseGrabbed;
    private GameState state, newState;
    private StateThread stateThread;

    public ScapesEngine(Game game, Path home, boolean debug) {
        this(game, loadBackend(), home, debug);
    }

    public ScapesEngine(Game game, ScapesEngineBackendProvider backend,
            Path home, boolean debug) {
        if (instance != null) {
            throw new ScapesEngineException(
                    "You can only have one engine running at a time!");
        }
        instance = this;
        this.debug = debug;
        this.game = game;
        this.home = home;
        runtime = Runtime.getRuntime();
        game.engine = this;
        Thread.currentThread().setName("Engine-Rendering-Thread");
        LOGGER.info("Starting Scapes-Engine: {} (Game: {})", this, game);
        try {
            assets = new FileSystemContainer();
            temp = Files.createTempDirectory("ScapesEngine");
            runtime.addShutdownHook(new Thread(() -> {
                try {
                    FileUtil.deleteDir(temp);
                } catch (IOException e) {
                    LOGGER.warn("Failed to delete temporary directory: {}",
                            e.toString());
                }
            }));
            assets.registerFileSystem("Class",
                    new ClasspathPath(getClass().getClassLoader(), ""));
            assets.registerFileSystem("Engine",
                    new ClasspathPath(getClass().getClassLoader(),
                            "assets/scapes/tobi29/engine/"));
            fileCache = new FileCache(this.home.resolve("cache"),
                    temp.resolve("cache"));
            fileCache.check();
            Files.createDirectories(this.home.resolve("screenshots"));
        } catch (IOException e) {
            throw new ScapesEngineException(
                    "Failed to create virtual file system: " + e.toString());
        }
        checkSystem();
        taskExecutor = new TaskExecutor(this, "Engine");
        tagStructure = new TagStructure();
        try {
            Path configPath = this.home.resolve("ScapesEngine.json");
            if (Files.exists(configPath)) {
                FileUtil.read(configPath,
                        stream -> TagStructureJSON.read(tagStructure, stream));
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load config file: {}", e.toString());
        }
        if (tagStructure.has("Engine")) {
            config =
                    new ScapesEngineConfig(tagStructure.getStructure("Engine"));
        } else {
            TagStructure engineTag = tagStructure.getStructure("Engine");
            engineTag.setBoolean("VSync", true);
            engineTag.setDouble("Framerate", 60.0);
            engineTag.setDouble("ResolutionMultiplier", 1.0);
            engineTag.setDouble("MusicVolume", 1.0);
            engineTag.setDouble("SoundVolume", 1.0);
            engineTag.setBoolean("Fullscreen", false);
            config = new ScapesEngineConfig(engineTag);
        }
        globalGui = new Gui(GuiAlignment.STRETCH);
        debugGui = new GuiDebugLayer();
        globalGui.add(debugGui);
        GuiWidgetDebugValues debugValues = debugGui.values();
        usedMemoryDebug = debugValues.get("Runtime-Memory-Used");
        maxMemoryDebug = debugValues.get("Runtime-Memory-Max");
        game.init();
        container = backend.createContainer(this);
        graphics = new GraphicsSystem(this, container.gl());
        sounds = new SoundSystem(this, container.al());
        controller = container.controller();
        guiController = new GuiControllerDefault(this, controller);
    }

    private static ScapesEngineBackendProvider loadBackend() {
        for (ScapesEngineBackendProvider backend : ServiceLoader
                .load(ScapesEngineBackendProvider.class)) {
            try {
                if (backend.available()) {
                    LOGGER.debug("Loaded backend: {}",
                            backend.getClass().getName());
                    return backend;
                }
            } catch (ServiceConfigurationError e) {
                LOGGER.warn("Unable to load backend provider: {}",
                        e.toString());
            }
        }
        throw new ScapesEngineException("No backend found!");
    }

    private void checkSystem() {
        LOGGER.info("Operating system: {} {} {}", System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"));
        LOGGER.info("Java: {} (MaxMemory: {}, Processors: {})",
                System.getProperty("java.version"),
                runtime.maxMemory() / 1048576, runtime.availableProcessors());
    }

    public Container container() {
        return container;
    }

    public GraphicsSystem graphics() {
        return graphics;
    }

    public SoundSystem sounds() {
        return sounds;
    }

    public Game game() {
        return game;
    }

    public Gui globalGUI() {
        return globalGui;
    }

    public ControllerDefault controller() {
        return controller;
    }

    public GuiController guiController() {
        return guiController;
    }

    public void setGUIController(GuiController guiController) {
        this.guiController = guiController;
    }

    public Path home() {
        return home;
    }

    public Path temp() {
        return temp;
    }

    public FileSystemContainer files() {
        return assets;
    }

    public FileCache fileCache() {
        return fileCache;
    }

    public ScapesEngineConfig config() {
        return config;
    }

    public TagStructure tagStructure() {
        return tagStructure;
    }

    public TaskExecutor taskExecutor() {
        return taskExecutor;
    }

    public GuiWidgetDebugValues debugValues() {
        return debugGui.values();
    }

    public GameState state() {
        return state;
    }

    public void setState(GameState state) {
        newState = state;
    }

    @SuppressWarnings({"OverlyBroadCatchBlock", "CallToSystemExit"})
    public int run() {
        try {
            container.run();
        } catch (GraphicsCheckException e) {
            LOGGER.error("Failed to initialize graphics:", e);
            container.message(Container.MessageType.ERROR, game.name(),
                    "Unable to initialize graphics:\n" + e.getMessage());
            return 1;
        } catch (Throwable e) {
            writeCrash(e);
            try {
                container.message(Container.MessageType.ERROR, game.name(),
                        game.name() + " crashed\n:" + toString());
            } catch (Exception e2) {
                LOGGER.error("Failed to show crash message", e2);
            }
            System.exit(1);
        }
        return 0;
    }

    public void render(double delta) {
        sounds.poll(delta);
        graphics.render(delta);
    }

    public void dispose() {
        if (stateThread != null) {
            stateThread.joiner.join();
            stateThread = null;
        }
        graphics.dispose();
        sounds.dispose();
        game.dispose();
        try {
            FileUtil.write(home.resolve("ScapesEngine.json"),
                    streamOut -> TagStructureJSON
                            .write(tagStructure, streamOut));
        } catch (IOException e) {
            LOGGER.warn("Failed to save config file!");
        }
        taskExecutor.shutdown();
    }

    @Override
    @SuppressWarnings("CallToSystemExit")
    public void crash(Throwable e) {
        writeCrash(e);
        System.exit(1);
    }

    private void writeCrash(Throwable e) {
        LOGGER.error("Scapes engine shutting down because of crash", e);
        Map<String, String> debugValues = new ConcurrentHashMap<>();
        for (Map.Entry<String, GuiWidgetDebugValues.Element> entry : debugGui
                .values().elements()) {
            debugValues.put(entry.getKey(), entry.getValue().toString());
        }
        try {
            Path crashReportFile = CrashReportFile.file(home);
            CrashReportFile.writeCrashReport(e, crashReportFile, "ScapesEngine",
                    debugValues);
            container.openFile(crashReportFile);
        } catch (IOException e1) {
            LOGGER.error("Failed to write crash report: {}", e.toString());
        }
    }

    public void stop() {
        container.stop();
    }

    public void step(GL gl, double delta) {
        if (newState != null) {
            if (stateThread != null) {
                stateThread.joiner.join();
                stateThread = null;
            }
            if (state == null) {
                state = newState;
                game.initLate(gl);
            } else {
                state.scene().dispose(gl);
                state.disposeState(gl);
                state = newState;
            }
            newState = null;
            state.init(gl);
        }
        if (state.isThreaded()) {
            if (stateThread == null) {
                stateThread = new StateThread(state);
                stateThread.joiner = taskExecutor.runTask(stateThread, "State",
                        TaskExecutor.Priority.MEDIUM);
            }
        } else {
            if (stateThread != null) {
                stateThread.joiner.join();
                stateThread = null;
            }
            step(delta, state);
        }
    }

    private void step(double delta, GameState state) {
        taskExecutor.tick();
        boolean mouseGrabbed =
                this.state.isMouseGrabbed() || guiController.isSoftwareMouse();
        if (this.mouseGrabbed != mouseGrabbed) {
            this.mouseGrabbed = mouseGrabbed;
            container.setMouseGrabbed(mouseGrabbed);
        }
        controller.poll();
        usedMemoryDebug.setValue(
                (runtime.totalMemory() - runtime.freeMemory()) / 1048576);
        maxMemoryDebug.setValue(runtime.maxMemory() / 1048576);
        if (controller.isPressed(ControllerKey.KEY_F2)) {
            graphics.triggerScreenshot();
        }
        if (debug && controller.isPressed(ControllerKey.KEY_F3)) {
            debugGui.toggleDebugValues();
        }
        state.step(delta);
        globalGui.update(this);
        game.step();
        guiController.update(delta);
    }

    private class StateThread implements TaskExecutor.ASyncTask {
        private final GameState state;
        private Joiner joiner;

        private StateThread(GameState state) {
            this.state = state;
        }

        @Override
        public void run(Joiner joiner) {
            try {
                Sync sync = new Sync(config.fps(), 5000000000L, true,
                        "Engine-Update");
                sync.init();
                while (!joiner.marked()) {
                    step(sync.delta(), state);
                    sync.cap();
                }
            } catch (Throwable e) {
                crash(e);
            }
        }
    }
}
