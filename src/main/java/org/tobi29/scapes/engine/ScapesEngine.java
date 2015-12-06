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

import java8.util.Optional;
import java8.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.gui.*;
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues;
import org.tobi29.scapes.engine.input.ControllerDefault;
import org.tobi29.scapes.engine.opengl.*;
import org.tobi29.scapes.engine.sound.SoundSystem;
import org.tobi29.scapes.engine.spi.ScapesEngineBackendProvider;
import org.tobi29.scapes.engine.utils.Crashable;
import org.tobi29.scapes.engine.utils.Sync;
import org.tobi29.scapes.engine.utils.io.filesystem.*;
import org.tobi29.scapes.engine.utils.io.filesystem.classpath.ClasspathPath;
import org.tobi29.scapes.engine.utils.io.tag.TagStructure;
import org.tobi29.scapes.engine.utils.io.tag.TagStructureJSON;
import org.tobi29.scapes.engine.utils.task.Joiner;
import org.tobi29.scapes.engine.utils.task.TaskExecutor;

import java.io.IOException;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ScapesEngine implements Crashable {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ScapesEngine.class);
    private static ScapesEngine instance;
    private final Container container;
    private final GraphicsSystem graphics;
    private final SoundSystem sounds;
    private final Game game;
    private final GuiStyle guiStyle;
    private final GuiStack guiStack = new GuiStack();
    private final Runtime runtime;
    private final TagStructure tagStructure;
    private final ScapesEngineConfig config;
    private final FilePath home;
    private final FileSystemContainer assets;
    private final FileCache fileCache;
    private final TaskExecutor taskExecutor;
    private final boolean debug;
    private final GuiWidgetDebugValues debugValues;
    private final GuiWidgetDebugValues.Element usedMemoryDebug, maxMemoryDebug;
    private final AtomicReference<GameState> newState = new AtomicReference<>();
    private GuiController guiController;
    private boolean mouseGrabbed;
    private GameState state;
    private StateThread stateThread;

    public ScapesEngine(Game game, FilePath home, boolean debug) {
        this(game, loadBackend(), home, home.resolve("cache"), debug);
    }

    public ScapesEngine(Game game, ScapesEngineBackendProvider backend,
            FilePath home, FilePath cache, boolean debug) {
        this(game, backend::createContainer, home, cache, debug);
    }

    public ScapesEngine(Game game, Function<ScapesEngine, Container> backend,
            FilePath home, FilePath cache, boolean debug) {
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
            assets.registerFileSystem("Class",
                    new ClasspathPath(getClass().getClassLoader(), ""));
            assets.registerFileSystem("Engine",
                    new ClasspathPath(getClass().getClassLoader(),
                            "assets/scapes/tobi29/engine/"));
            fileCache = new FileCache(cache);
            fileCache.check();
            FileUtil.createDirectories(this.home.resolve("screenshots"));
        } catch (IOException e) {
            throw new ScapesEngineException(
                    "Failed to create virtual file system: " + e);
        }
        checkSystem();
        LOGGER.info("Creating task executor");
        taskExecutor = new TaskExecutor(this, "Engine");
        tagStructure = new TagStructure();
        try {
            LOGGER.info("Reading config");
            FilePath configPath = this.home.resolve("ScapesEngine.json");
            if (FileUtil.exists(configPath)) {
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
            LOGGER.info("Setting defaults to config");
            TagStructure engineTag = tagStructure.getStructure("Engine");
            engineTag.setBoolean("VSync", true);
            engineTag.setDouble("Framerate", 60.0);
            engineTag.setDouble("ResolutionMultiplier", 1.0);
            engineTag.setDouble("MusicVolume", 1.0);
            engineTag.setDouble("SoundVolume", 1.0);
            engineTag.setBoolean("Fullscreen", false);
            config = new ScapesEngineConfig(engineTag);
        }
        LOGGER.info("Initializing game");
        game.init();
        LOGGER.info("Creating container");
        container = backend.apply(this);
        LOGGER.info("Loading default font");
        String fontName = container.loadFont("Engine:font/QuicksandPro-Regular")
                .orElse("Quicksand Pro");
        FontRenderer font =
                new FontRenderer(container.createGlyphRenderer(fontName, 64));
        LOGGER.info("Setting up GUI");
        guiStyle = new GuiBasicStyle(font, container.gl().textures());
        Gui debugGui = new Gui(guiStyle, GuiAlignment.LEFT) {
            @Override
            public boolean valid() {
                return true;
            }
        };
        debugValues = debugGui.add(32, 32, GuiWidgetDebugValues::new);
        debugValues.setVisible(false);
        guiStack.add("99-Debug", debugGui);
        usedMemoryDebug = debugValues.get("Runtime-Memory-Used");
        maxMemoryDebug = debugValues.get("Runtime-Memory-Max");
        LOGGER.info("Creating graphics system");
        graphics = new GraphicsSystem(this, container.gl());
        LOGGER.info("Creating sound system");
        sounds = container.sound();
        guiController = new GuiControllerDummy();
    }

    private static ScapesEngineBackendProvider loadBackend() {
        for (ScapesEngineBackendProvider backend : ServiceLoader
                .load(ScapesEngineBackendProvider.class)) {
            try {
                LOGGER.debug("Loaded backend: {}",
                        backend.getClass().getName());
                return backend;
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

    public boolean debug() {
        return debug;
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

    public GuiStack guiStack() {
        return guiStack;
    }

    public GuiStyle guiStyle() {
        return guiStyle;
    }

    public Optional<ControllerDefault> controller() {
        return container().controller();
    }

    public GuiController guiController() {
        return guiController;
    }

    public void setGUIController(GuiController guiController) {
        this.guiController = guiController;
    }

    public FilePath home() {
        return home;
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
        return debugValues;
    }

    public GameState state() {
        return state;
    }

    public void setState(GameState state) {
        newState.set(state);
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

    public void halt() {
        if (stateThread != null) {
            stateThread.joiner.join();
            stateThread = null;
        }
    }
    public void dispose() {
        halt();
        LOGGER.info("Disposing last state");
        state.disposeState();
        LOGGER.info("Disposing sound system");
        sounds.dispose();
        LOGGER.info("Disposing game");
        game.dispose();
        try {
            FileUtil.write(home.resolve("ScapesEngine.json"),
                    streamOut -> TagStructureJSON
                            .write(tagStructure, streamOut));
        } catch (IOException e) {
            LOGGER.warn("Failed to save config file!");
        }
        LOGGER.info("Shutting down tasks");
        taskExecutor.shutdown();
        LOGGER.info("Stopped Scapes-Engine");
        instance = null;
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
        for (Map.Entry<String, GuiWidgetDebugValues.Element> entry : this.debugValues
                .elements()) {
            debugValues.put(entry.getKey(), entry.getValue().toString());
        }
        try {
            FilePath crashReportFile = CrashReportFile.file(home);
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
        GameState newState = this.newState.getAndSet(null);
        if (newState != null) {
            if (stateThread != null) {
                stateThread.joiner.join();
                stateThread = null;
            }
            if (state == null) {
                state = newState;
                game.initLate(gl);
            } else {
                state.disposeState(gl);
                state = newState;
            }
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
                this.state.isMouseGrabbed() || guiController.captureCursor();
        if (this.mouseGrabbed != mouseGrabbed) {
            this.mouseGrabbed = mouseGrabbed;
            container.setMouseGrabbed(mouseGrabbed);
        }
        container.update(delta);
        usedMemoryDebug.setValue(
                (runtime.totalMemory() - runtime.freeMemory()) / 1048576);
        maxMemoryDebug.setValue(runtime.maxMemory() / 1048576);
        state.step(delta);
        guiStack.step(this);
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
