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
import org.tobi29.scapes.engine.gui.debug.GuiWidgetProfiler;
import org.tobi29.scapes.engine.input.ControllerDefault;
import org.tobi29.scapes.engine.opengl.FontRenderer;
import org.tobi29.scapes.engine.opengl.GraphicsCheckException;
import org.tobi29.scapes.engine.opengl.GraphicsSystem;
import org.tobi29.scapes.engine.sound.SoundSystem;
import org.tobi29.scapes.engine.spi.ScapesEngineBackendProvider;
import org.tobi29.scapes.engine.utils.Crashable;
import org.tobi29.scapes.engine.utils.Sync;
import org.tobi29.scapes.engine.utils.io.filesystem.*;
import org.tobi29.scapes.engine.utils.io.filesystem.classpath.ClasspathPath;
import org.tobi29.scapes.engine.utils.io.tag.TagStructure;
import org.tobi29.scapes.engine.utils.io.tag.json.TagStructureJSON;
import org.tobi29.scapes.engine.utils.task.Joiner;
import org.tobi29.scapes.engine.utils.task.TaskExecutor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ScapesEngine implements Crashable {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ScapesEngine.class);
    private static ScapesEngine instance;
    private final AtomicBoolean sync = new AtomicBoolean(true);
    private final Container container;
    private final GraphicsSystem graphics;
    private final SoundSystem sounds;
    private final Game game;
    private final GuiStyle guiStyle;
    private final GuiStack guiStack;
    private final Runtime runtime;
    private final TagStructure tagStructure;
    private final ScapesEngineConfig config;
    private final FilePath home;
    private final FileSystemContainer assets;
    private final FileCache fileCache;
    private final TaskExecutor taskExecutor;
    private final boolean debug;
    private final GuiNotifications notifications;
    private final GuiWidgetDebugValues debugValues;
    private final GuiWidgetDebugValues.Element usedMemoryDebug, maxMemoryDebug;
    private final GuiWidgetProfiler profiler;
    private final AtomicReference<GameState> newState = new AtomicReference<>();
    private Joiner joiner;
    private GuiController guiController;
    private boolean mouseGrabbed;
    private GameState state;

    public ScapesEngine(Game game, Function<ScapesEngine, Container> backend,
            FilePath home, boolean debug) {
        this(game, backend, home, home.resolve("cache"), debug);
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
        checkSystem();
        LOGGER.info("Starting Scapes-Engine: {} (Game: {})", this, game);
        LOGGER.info("Creating task executor");
        taskExecutor = new TaskExecutor(this, "Engine");
        LOGGER.info("Initializing asset system");
        assets = new FileSystemContainer();
        assets.registerFileSystem("Class",
                new ClasspathPath(getClass().getClassLoader(), ""));
        assets.registerFileSystem("Engine",
                new ClasspathPath(getClass().getClassLoader(),
                        "assets/scapes/tobi29/engine/"));
        LOGGER.info("Initializing game");
        game.initEarly();
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
        try {
            fileCache = new FileCache(cache);
            fileCache.check();
            FileUtil.createDirectories(this.home.resolve("screenshots"));
        } catch (IOException e) {
            throw new ScapesEngineException(
                    "Failed to initialize file cache: " + e);
        }
        LOGGER.info("Creating container");
        container = backend.apply(this);
        LOGGER.info("Loading default font");
        String fontName = container.loadFont("Engine:font/QuicksandPro-Regular")
                .orElse("Quicksand Pro");
        FontRenderer font = new FontRenderer(this,
                container.createGlyphRenderer(fontName, 64));
        LOGGER.info("Setting up GUI");
        guiStack = new GuiStack(this);
        guiStyle = new GuiBasicStyle(this, font, container.gl().textures());
        notifications = new GuiNotifications(guiStyle);
        guiStack.addUnfocused("90-Notifications", notifications);
        Gui debugGui = new Gui(guiStyle) {
            @Override
            public boolean valid() {
                return true;
            }
        };
        debugValues = debugGui.add(32, 32, 360, 256, GuiWidgetDebugValues::new);
        debugValues.setVisible(false);
        profiler = debugGui.add(32, 32, 360, 256, GuiWidgetProfiler::new);
        profiler.setVisible(false);
        guiStack.addUnfocused("99-Debug", debugGui);
        usedMemoryDebug = debugValues.get("Runtime-Memory-Used");
        maxMemoryDebug = debugValues.get("Runtime-Memory-Max");
        LOGGER.info("Creating graphics system");
        graphics = new GraphicsSystem(this, container.gl());
        LOGGER.info("Creating sound system");
        sounds = container.sound();
        guiController = new GuiControllerDummy(this);
        game.init();
    }

    public static Function<ScapesEngine, Container> loadBackend() {
        for (ScapesEngineBackendProvider backend : ServiceLoader
                .load(ScapesEngineBackendProvider.class)) {
            try {
                LOGGER.debug("Loaded backend: {}",
                        backend.getClass().getName());
                return backend::createContainer;
            } catch (ServiceConfigurationError e) {
                LOGGER.warn("Unable to load backend provider: {}",
                        e.toString());
            }
        }
        throw new ScapesEngineException("No backend found!");
    }

    public static Function<ScapesEngine, Container> emulateTouch(
            Function<ScapesEngine, Container> backend) {
        return engine -> new ContainerEmulateTouch(backend.apply(engine));
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

    public GuiNotifications notifications() {
        return notifications;
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

    public GuiWidgetProfiler profiler() {
        return profiler;
    }

    public GameState state() {
        if (state == null) {
            throw new IllegalStateException("Engine not running");
        }
        return state;
    }

    public void setState(GameState state) {
        newState.set(state);
    }

    public ByteBuffer allocate(int capacity) {
        return container.allocate(capacity);
    }

    public void unlockUpdate() {
        synchronized (sync) {
            sync.set(true);
            sync.notifyAll();
        }
    }

    @SuppressWarnings({"OverlyBroadCatchBlock", "CallToSystemExit"})
    public int run() {
        start();
        try {
            container.run();
        } catch (GraphicsCheckException e) {
            LOGGER.error("Failed to initialize graphics:", e);
            container.message(Container.MessageType.ERROR, game.name(),
                    "Unable to initialize graphics:\n" + e.getMessage());
            halt();
            return 1;
        } catch (Throwable e) {
            LOGGER.error("Scapes engine shutting down because of crash", e);
            writeCrash(e);
            try {
                container.message(Container.MessageType.ERROR, game.name(),
                        game.name() + " crashed:\n" + e);
            } catch (Exception e2) {
                LOGGER.error("Failed to show crash message", e2);
            }
            System.exit(1);
        }
        halt();
        return 0;
    }

    public void start() {
        Joiner.Joinable wait = new Joiner.Joinable();
        joiner = taskExecutor.runTask(joiner -> {
            try {
                Sync sync = new Sync(config.fps(), 0L, false, "Engine-Update");
                game.initLate();
                sync.init();
                step(sync.delta());
                wait.join();
                sync.cap();
                int maxWait = sync.maxDiff() / 750000;
                while (!joiner.marked()) {
                    step(sync.delta());
                    if (this.sync.getAndSet(false)) {
                        synchronized (this.sync) {
                            if (!joiner.marked()) {
                                try {
                                    this.sync.wait(maxWait);
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                        sync.tick();
                    } else {
                        sync.cap();
                    }
                }
            } catch (Throwable e) {
                crash(e);
            }
        }, "State", TaskExecutor.Priority.HIGH);
        wait.joiner().join();
    }

    public void halt() {
        synchronized (sync) {
            sync.set(false);
            sync.notifyAll();
        }
        joiner.join();
    }

    public void dispose() {
        halt();
        LOGGER.info("Disposing last state");
        state.disposeState();
        state = null;
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
        LOGGER.error("Scapes engine shutting down because of crash", e);
        writeCrash(e);
        System.exit(1);
    }

    public void writeCrash(Throwable e) {
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

    private void step(double delta) {
        GameState state = newState.getAndSet(null);
        if (state == null) {
            state = this.state;
        } else {
            synchronized (graphics) {
                if (this.state != null) {
                    this.state.disposeState();
                }
                this.state = state;
                state.init();
            }
        }
        taskExecutor.tick();
        boolean mouseGrabbed =
                state.isMouseGrabbed() || guiController.captureCursor();
        if (this.mouseGrabbed != mouseGrabbed) {
            this.mouseGrabbed = mouseGrabbed;
            container.setMouseGrabbed(mouseGrabbed);
        }
        container.update(delta);
        usedMemoryDebug.setValue(
                (runtime.totalMemory() - runtime.freeMemory()) / 1048576);
        maxMemoryDebug.setValue(runtime.maxMemory() / 1048576);
        state.step(delta);
        guiStack.step(this, delta);
        game.step();
        guiController.update(delta);
    }
}
