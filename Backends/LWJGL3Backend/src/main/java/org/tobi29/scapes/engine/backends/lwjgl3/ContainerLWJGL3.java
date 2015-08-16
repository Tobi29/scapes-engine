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

import org.lwjgl.LWJGLUtil;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.input.ControllerDefault;
import org.tobi29.scapes.engine.input.ControllerKey;
import org.tobi29.scapes.engine.openal.OpenAL;
import org.tobi29.scapes.engine.opengl.Container;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.utils.MutableSingle;
import org.tobi29.scapes.engine.utils.task.Joiner;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public abstract class ContainerLWJGL3 extends ControllerDefault
        implements Container {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ContainerLWJGL3.class);
    protected final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    protected final ScapesEngine engine;
    protected final Thread mainThread;
    protected final LWJGL3OpenGL openGL;
    protected final LWJGL3OpenAL openAL;
    protected final boolean superModifier;
    protected GLContext context;
    protected boolean focus = true, valid, visible, containerResized = true,
            joysticksChanged;
    protected int containerWidth, containerHeight, contentWidth, contentHeight;
    protected double mouseX, mouseY;

    protected ContainerLWJGL3(ScapesEngine engine) {
        this.engine = engine;
        mainThread = Thread.currentThread();
        LOGGER.info("LWJGL version: {}", Sys.getVersion());
        openGL = new LWJGL3OpenGL(engine, this);
        openAL = new LWJGL3OpenAL();
        superModifier = LWJGLUtil.getPlatform() == LWJGLUtil.Platform.MACOSX;
    }

    private static Optional<String> checkContext(GLContext context) {
        LOGGER.info("OpenGL: {} (Vendor: {}, Renderer: {})",
                GL11.glGetString(GL11.GL_VERSION),
                GL11.glGetString(GL11.GL_VENDOR),
                GL11.glGetString(GL11.GL_RENDERER));
        if (!context.getCapabilities().OpenGL11) {
            return Optional.of("Your graphics card has no OpenGL 1.1 support!");
        }
        if (!context.getCapabilities().OpenGL12) {
            return Optional.of("Your graphics card has no OpenGL 1.2 support!");
        }
        if (!context.getCapabilities().OpenGL13) {
            return Optional.of("Your graphics card has no OpenGL 1.3 support!");
        }
        if (!context.getCapabilities().OpenGL14) {
            return Optional.of("Your graphics card has no OpenGL 1.4 support!");
        }
        if (!context.getCapabilities().OpenGL15) {
            return Optional.of("Your graphics card has no OpenGL 1.5 support!");
        }
        if (!context.getCapabilities().OpenGL20) {
            return Optional.of("Your graphics card has no OpenGL 2.0 support!");
        }
        if (!context.getCapabilities().OpenGL21) {
            return Optional.of("Your graphics card has no OpenGL 2.1 support!");
        }
        if (!context.getCapabilities().OpenGL30) {
            return Optional.of("Your graphics card has no OpenGL 3.0 support!");
        }
        if (!context.getCapabilities().OpenGL31) {
            return Optional.of("Your graphics card has no OpenGL 3.1 support!");
        }
        if (!context.getCapabilities().OpenGL32) {
            return Optional.of("Your graphics card has no OpenGL 3.2 support!");
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
    public OpenAL al() {
        return openAL;
    }

    @Override
    public ControllerDefault controller() {
        return this;
    }

    @Override
    public boolean joysticksChanged() {
        return joysticksChanged;
    }

    protected Optional<String> initContext() {
        context = GLContext.createFromCurrent();
        return checkContext(context);
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

    protected <R> R exec(Supplier<R> runnable) {
        Thread thread = Thread.currentThread();
        if (thread == mainThread) {
            return runnable.get();
        }
        Joiner.Joinable joinable = new Joiner.Joinable();
        MutableSingle<R> output = new MutableSingle<>(null);
        tasks.add(() -> {
            output.a = runnable.get();
            joinable.join();
        });
        joinable.joiner().join();
        return output.a;
    }
}
