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
package org.tobi29.scapes.engine.backends.lwjgl3;

import java8.util.Optional;
import java8.util.function.Supplier;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLES20;
import org.lwjgl.opengles.GLESCapabilities;
import org.lwjgl.system.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.Container;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.backends.lwjgl3.openal.LWJGL3OpenAL;
import org.tobi29.scapes.engine.backends.lwjgl3.opengl.GLLWJGL3GL;
import org.tobi29.scapes.engine.backends.lwjgl3.opengl.LWJGL3OpenGL;
import org.tobi29.scapes.engine.backends.lwjgl3.opengles.LWJGL3OpenGLES;
import org.tobi29.scapes.engine.backends.openal.openal.OpenALSoundSystem;
import org.tobi29.scapes.engine.backends.opengl.GLOpenGL;
import org.tobi29.scapes.engine.graphics.GL;
import org.tobi29.scapes.engine.gui.GlyphRenderer;
import org.tobi29.scapes.engine.input.ControllerDefault;
import org.tobi29.scapes.engine.input.ControllerKey;
import org.tobi29.scapes.engine.sound.SoundSystem;
import org.tobi29.scapes.engine.utils.MutablePair;
import org.tobi29.scapes.engine.utils.io.IORunnable;
import org.tobi29.scapes.engine.utils.io.IOSupplier;
import org.tobi29.scapes.engine.utils.task.Joiner;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class ContainerLWJGL3 extends ControllerDefault
        implements Container {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ContainerLWJGL3.class);
    private static final boolean USE_FAST_GL = true;
    protected final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    protected final ScapesEngine engine;
    protected final Thread mainThread;
    protected final GL gl;
    protected final SoundSystem soundSystem;
    protected final boolean superModifier, useGLES;
    protected final AtomicBoolean joysticksChanged = new AtomicBoolean(false);
    protected boolean focus = true, valid, visible, containerResized = true;
    protected int containerWidth, containerHeight, contentWidth, contentHeight;
    protected double mouseX, mouseY;

    protected ContainerLWJGL3(ScapesEngine engine) {
        this(engine, false);
    }

    protected ContainerLWJGL3(ScapesEngine engine, boolean useGLES) {
        this.engine = engine;
        this.useGLES = useGLES;
        mainThread = Thread.currentThread();
        LOGGER.info("LWJGL version: {}", Version.getVersion());
        if (useGLES) {
            gl = new GLOpenGL(engine, this, new LWJGL3OpenGLES());
        } else {
            if (USE_FAST_GL) {
                gl = new GLLWJGL3GL(engine, this);
            } else {
                gl = new GLOpenGL(engine, this, new LWJGL3OpenGL());
            }
        }
        soundSystem =
                new OpenALSoundSystem(engine, new LWJGL3OpenAL(), 64, 5.0);
        superModifier = Platform.get() == Platform.MACOSX;
    }

    public static Optional<String> checkContextGL() {
        LOGGER.info("OpenGL: {} (Vendor: {}, Renderer: {})",
                GL11.glGetString(GL11.GL_VERSION),
                GL11.glGetString(GL11.GL_VENDOR),
                GL11.glGetString(GL11.GL_RENDERER));
        GLCapabilities capabilities = org.lwjgl.opengl.GL.getCapabilities();
        if (!capabilities.OpenGL11) {
            return Optional.of("Your graphics card has no OpenGL 1.1 support!");
        }
        if (!capabilities.OpenGL12) {
            return Optional.of("Your graphics card has no OpenGL 1.2 support!");
        }
        if (!capabilities.OpenGL13) {
            return Optional.of("Your graphics card has no OpenGL 1.3 support!");
        }
        if (!capabilities.OpenGL14) {
            return Optional.of("Your graphics card has no OpenGL 1.4 support!");
        }
        if (!capabilities.OpenGL15) {
            return Optional.of("Your graphics card has no OpenGL 1.5 support!");
        }
        if (!capabilities.OpenGL20) {
            return Optional.of("Your graphics card has no OpenGL 2.0 support!");
        }
        if (!capabilities.OpenGL21) {
            return Optional.of("Your graphics card has no OpenGL 2.1 support!");
        }
        if (!capabilities.OpenGL30) {
            return Optional.of("Your graphics card has no OpenGL 3.0 support!");
        }
        if (!capabilities.OpenGL31) {
            return Optional.of("Your graphics card has no OpenGL 3.1 support!");
        }
        if (!capabilities.OpenGL32) {
            return Optional.of("Your graphics card has no OpenGL 3.2 support!");
        }
        if (!capabilities.OpenGL33) {
            return Optional.of("Your graphics card has no OpenGL 3.3 support!");
        }
        return Optional.empty();
    }

    public static Optional<String> checkContextGLES() {
        LOGGER.info("OpenGL ES: {} (Vendor: {}, Renderer: {})",
                GLES20.glGetString(GLES20.GL_VERSION),
                GLES20.glGetString(GLES20.GL_VENDOR),
                GLES20.glGetString(GLES20.GL_RENDERER));
        GLESCapabilities capabilities = GLES.getCapabilities();
        if (!capabilities.GLES20) {
            return Optional
                    .of("Your graphics card has no OpenGL ES 2.0 support!");
        }
        if (!capabilities.GLES30) {
            return Optional
                    .of("Your graphics card has no OpenGL ES 3.0 support!");
        }
        return Optional.empty();
    }

    public Optional<String> checkContext() {
        if (useGLES) {
            return checkContextGLES();
        } else {
            return checkContextGL();
        }
    }

    @Override
    public int containerWidth() {
        return containerWidth;
    }

    @Override
    public int containerHeight() {
        return containerHeight;
    }

    @Override
    public int contentWidth() {
        return contentWidth;
    }

    @Override
    public int contentHeight() {
        return contentHeight;
    }

    @Override
    public boolean contentResized() {
        return containerResized;
    }

    @Override
    public void updateContainer() {
        valid = false;
    }

    @Override
    public GL gl() {
        return gl;
    }

    @Override
    public SoundSystem sound() {
        return soundSystem;
    }

    @Override
    public Optional<ControllerDefault> controller() {
        return Optional.of(this);
    }

    @Override
    public boolean joysticksChanged() {
        return joysticksChanged.get();
    }

    @Override
    public Optional<String> loadFont(String asset) {
        return STBGlyphRenderer.loadFont(engine.files().get(asset + ".ttf"));
    }

    @Override
    public GlyphRenderer createGlyphRenderer(String fontName, int size) {
        return STBGlyphRenderer.fromFont(this, fontName, size);
    }

    @Override
    public ByteBuffer allocate(int capacity) {
        // TODO: Do more testing if the direct buffer leak is actually gone
        return BufferUtils.createByteBuffer(capacity);
        // Late 2015 OpenJDK 8 (did not test this on other JVMs) deleted direct
        // buffers would not get freed properly causing massive leaks pushing
        // up memory usage to 5+ GB, backend currently can transparently take
        // heap buffers for LWJGL calls (by copying into a shared direct one)
        // return ByteBuffer.allocate(capacity).order(ByteOrder.nativeOrder());
    }

    @Override
    public boolean isModifierDown() {
        if (superModifier) {
            return isDown(ControllerKey.KEY_LEFT_SUPER) ||
                    isDown(ControllerKey.KEY_RIGHT_SUPER);
        } else {
            return isDown(ControllerKey.KEY_LEFT_CONTROL) ||
                    isDown(ControllerKey.KEY_RIGHT_CONTROL);
        }
    }

    protected void exec(Runnable runnable) {
        Thread thread = Thread.currentThread();
        if (thread == mainThread) {
            runnable.run();
            return;
        }
        Joiner.Joinable joinable = new Joiner.Joinable();
        tasks.add(() -> {
            runnable.run();
            joinable.join();
        });
        joinable.joiner().join();
    }

    protected void execIO(IORunnable runnable) throws IOException {
        Thread thread = Thread.currentThread();
        if (thread == mainThread) {
            runnable.run();
            return;
        }
        Joiner.Joinable joinable = new Joiner.Joinable();
        AtomicReference<IOException> output = new AtomicReference<>();
        tasks.add(() -> {
            try {
                runnable.run();
            } catch (IOException e) {
                output.set(e);
            }
            joinable.join();
        });
        joinable.joiner().join();
        IOException e = output.get();
        if (e != null) {
            throw new IOException(e);
        }
    }

    protected <R> R exec(Supplier<R> runnable) {
        Thread thread = Thread.currentThread();
        if (thread == mainThread) {
            return runnable.get();
        }
        Joiner.Joinable joinable = new Joiner.Joinable();
        AtomicReference<R> output = new AtomicReference<>();
        tasks.add(() -> {
            output.set(runnable.get());
            joinable.join();
        });
        joinable.joiner().join();
        return output.get();
    }

    protected <R> R execIO(IOSupplier<R> runnable) throws IOException {
        Thread thread = Thread.currentThread();
        if (thread == mainThread) {
            return runnable.get();
        }
        Joiner.Joinable joinable = new Joiner.Joinable();
        MutablePair<IOException, R> output = new MutablePair<>();
        tasks.add(() -> {
            try {
                output.b = runnable.get();
            } catch (IOException e) {
                output.a = e;
            }
            joinable.join();
        });
        joinable.joiner().join();
        if (output.a != null) {
            throw new IOException(output.a);
        }
        return output.b;
    }
}
