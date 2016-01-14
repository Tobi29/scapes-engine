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
import org.lwjgl.Version;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.Container;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.input.ControllerDefault;
import org.tobi29.scapes.engine.input.ControllerKey;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.sound.SoundSystem;
import org.tobi29.scapes.engine.sound.openal.OpenALSoundSystem;
import org.tobi29.scapes.engine.utils.MutablePair;
import org.tobi29.scapes.engine.utils.MutableSingle;
import org.tobi29.scapes.engine.utils.io.IORunnable;
import org.tobi29.scapes.engine.utils.io.IOSupplier;
import org.tobi29.scapes.engine.utils.task.Joiner;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class ContainerLWJGL3 extends ControllerDefault
        implements Container {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ContainerLWJGL3.class);
    protected final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    protected final ScapesEngine engine;
    protected final Thread mainThread;
    protected final LWJGL3OpenGL openGL;
    protected final SoundSystem soundSystem;
    protected final boolean superModifier;
    protected boolean focus = true, valid, visible, containerResized = true,
            joysticksChanged;
    protected int containerWidth, containerHeight, contentWidth, contentHeight;
    protected double mouseX, mouseY;

    protected ContainerLWJGL3(ScapesEngine engine) {
        this.engine = engine;
        mainThread = Thread.currentThread();
        LOGGER.info("LWJGL version: {}", Version.getVersion());
        openGL = new LWJGL3OpenGL(engine, this);
        soundSystem = new OpenALSoundSystem(engine, new LWJGL3OpenAL());
        superModifier = Platform.get() == Platform.MACOSX;
    }

    private static Optional<String> checkContext() {
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
        return openGL;
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
        return joysticksChanged;
    }

    protected Optional<String> initContext() {
        org.lwjgl.opengl.GL.createCapabilities();
        return checkContext();
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
        MutableSingle<IOException> output = new MutableSingle<>();
        tasks.add(() -> {
            try {
                runnable.run();
            } catch (IOException e) {
                output.a = e;
            }
            joinable.join();
        });
        joinable.joiner().join();
        if (output.a != null) {
            throw new IOException(output.a);
        }
    }

    protected <R> R exec(Supplier<R> runnable) {
        Thread thread = Thread.currentThread();
        if (thread == mainThread) {
            return runnable.get();
        }
        Joiner.Joinable joinable = new Joiner.Joinable();
        MutableSingle<R> output = new MutableSingle<>();
        tasks.add(() -> {
            output.a = runnable.get();
            joinable.join();
        });
        joinable.joiner().join();
        return output.a;
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
