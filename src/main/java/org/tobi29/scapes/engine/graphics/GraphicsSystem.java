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

package org.tobi29.scapes.engine.graphics;

import java8.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.Container;
import org.tobi29.scapes.engine.GameState;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues;
import org.tobi29.scapes.engine.utils.graphics.Image;
import org.tobi29.scapes.engine.utils.graphics.PNG;
import org.tobi29.scapes.engine.utils.io.ProcessStream;
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath;
import org.tobi29.scapes.engine.utils.io.filesystem.FileUtil;
import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource;
import org.tobi29.scapes.engine.utils.profiler.Profiler;
import org.tobi29.scapes.engine.utils.shader.CompiledShader;
import org.tobi29.scapes.engine.utils.shader.ShaderCompileException;
import org.tobi29.scapes.engine.utils.shader.ShaderCompiler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GraphicsSystem {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(GraphicsSystem.class);
    private final ScapesEngine engine;
    private final GuiWidgetDebugValues.Element fpsDebug, widthDebug,
            heightDebug, textureDebug, vaoDebug, fboDebug, shaderDebug;
    private final GL gl;
    private final Texture empty;
    private final ShaderCompiler shaderCompiler = new ShaderCompiler();
    private final Map<String, CompiledShader> shaderCache =
            new ConcurrentHashMap<>();
    private boolean triggerScreenshot;
    private double resolutionMultiplier = 1.0;
    private GameState renderState;

    public GraphicsSystem(ScapesEngine engine, GL gl) {
        this.engine = engine;
        this.gl = gl;
        ByteBuffer buffer = engine.allocate(4);
        buffer.put((byte) -1);
        buffer.put((byte) -1);
        buffer.put((byte) -1);
        buffer.put((byte) -1);
        buffer.rewind();
        empty = createTexture(1, 1, buffer);
        resolutionMultiplier = engine.config().resolutionMultiplier();
        GuiWidgetDebugValues debugValues = engine.debugValues();
        fpsDebug = debugValues.get("Graphics-Fps");
        widthDebug = debugValues.get("Graphics-Width");
        heightDebug = debugValues.get("Graphics-Height");
        textureDebug = debugValues.get("Graphics-Textures");
        vaoDebug = debugValues.get("Graphics-VAOs");
        fboDebug = debugValues.get("Graphics-FBOs");
        shaderDebug = debugValues.get("Graphics-Shaders");
    }

    public void dispose() {
        engine.halt();
        synchronized (this) {
            GameState state = engine.state();
            state.disposeState(gl);
            gl.clear();
        }
    }

    public ScapesEngine engine() {
        return engine;
    }

    public TextureManager textures() {
        return gl.textures();
    }

    public Texture textureEmpty() {
        return empty;
    }

    @SuppressWarnings("CallToNativeMethodWhileLocked")
    public synchronized void render(double delta) {
        engine.unlockUpdate();
        try {
            gl.checkError("Pre-Render");
            Container container = engine.container();
            int containerWidth = container.containerWidth();
            int containerHeight = container.containerHeight();
            boolean fboSizeDirty;
            double resolutionMultiplier =
                    engine.config().resolutionMultiplier();
            if (container.contentResized() ||
                    this.resolutionMultiplier != resolutionMultiplier) {
                this.resolutionMultiplier = resolutionMultiplier;
                int contentWidth = container.contentWidth();
                int contentHeight = container.contentHeight();
                fboSizeDirty = true;
                widthDebug.setValue(contentWidth);
                heightDebug.setValue(contentHeight);
                try (Profiler.C ignored = Profiler.section("Reshape")) {
                    gl.reshape(contentWidth, contentHeight, containerWidth,
                            containerHeight, resolutionMultiplier);
                }
            } else {
                fboSizeDirty = false;
            }
            GameState state = engine.state();
            if (renderState != state) {
                try (Profiler.C ignored = Profiler.section("SwitchState")) {
                    if (renderState != null) {
                        renderState.disposeState(gl);
                        gl.textures().clearCache();
                        gl.clear();
                    }
                    renderState = state;
                }
            }
            try (Profiler.C ignored = Profiler.section("State")) {
                state.render(gl, delta, fboSizeDirty);
            }
            fpsDebug.setValue(1.0 / delta);
            textureDebug.setValue(gl.textureTracker().count());
            vaoDebug.setValue(gl.vaoTracker().count());
            fboDebug.setValue(gl.fboTracker().count());
            shaderDebug.setValue(gl.shaderTracker().count());
            if (triggerScreenshot) {
                try (Profiler.C ignored = Profiler.section("Screenshot")) {
                    triggerScreenshot = false;
                    int width = gl.contentWidth(), height = gl.contentHeight();
                    Image image = gl.screenShot(0, 0, width, height);
                    FilePath path = engine.home().resolve(
                            "screenshots/" + System.currentTimeMillis() +
                                    ".png");
                    engine.taskExecutor().runTask(() -> {
                        try {
                            FileUtil.write(path, stream -> PNG
                                    .encode(image, stream, 9, false));
                        } catch (IOException e) {
                            LOGGER.error("Error saving screenshot: {}",
                                    e.toString());
                        }
                    }, "Write-Screenshot");
                }
            }
            try (Profiler.C ignored = Profiler.section("Cleanup")) {
                gl.vaoTracker().disposeUnused(gl);
                gl.textureTracker().disposeUnused(gl);
                gl.fboTracker().disposeUnused(gl);
                gl.shaderTracker().disposeUnused(gl);
            }
        } catch (GraphicsException e) {
            LOGGER.warn("Graphics error during rendering: {}", e.toString());
        }
    }

    public void triggerScreenshot() {
        triggerScreenshot = true;
    }

    public Texture createTexture(int width, int height) {
        return createTexture(width, height, engine.allocate(width * height * 4),
                0);
    }

    public Texture createTexture(Image image, int mipmaps) {
        return createTexture(image.width(), image.height(), image.buffer(),
                mipmaps, TextureFilter.NEAREST, TextureFilter.NEAREST,
                TextureWrap.REPEAT, TextureWrap.REPEAT);
    }

    public Texture createTexture(int width, int height, ByteBuffer buffer,
            int mipmaps) {
        return createTexture(width, height, buffer, mipmaps,
                TextureFilter.NEAREST, TextureFilter.NEAREST,
                TextureWrap.REPEAT, TextureWrap.REPEAT);
    }

    public Texture createTexture(int width, int height, int mipmaps) {
        return createTexture(width, height, engine.allocate(width * height * 4),
                mipmaps);
    }

    public Texture createTexture(int width, int height, int mipmaps,
            TextureFilter minFilter, TextureFilter magFilter, TextureWrap wrapS,
            TextureWrap wrapT) {
        return createTexture(width, height, engine.allocate(width * height * 4),
                mipmaps, minFilter, magFilter, wrapS, wrapT);
    }

    public Texture createTexture(Image image) {
        return createTexture(image.width(), image.height(), image.buffer(), 4);
    }

    public Texture createTexture(int width, int height, ByteBuffer buffer) {
        return createTexture(width, height, buffer, 4);
    }

    public Texture createTexture(Image image, int mipmaps,
            TextureFilter minFilter, TextureFilter magFilter, TextureWrap wrapS,
            TextureWrap wrapT) {
        return createTexture(image.width(), image.height(), image.buffer(),
                mipmaps, minFilter, magFilter, wrapS, wrapT);
    }

    public Texture createTexture(int width, int height, ByteBuffer buffer,
            int mipmaps, TextureFilter minFilter, TextureFilter magFilter,
            TextureWrap wrapS, TextureWrap wrapT) {
        return gl.createTexture(width, height, buffer, mipmaps, minFilter,
                magFilter, wrapS, wrapT);
    }

    public Framebuffer createFramebuffer(int width, int height,
            int colorAttachments, boolean depth, boolean hdr, boolean alpha) {
        return gl.createFramebuffer(width, height, colorAttachments, depth, hdr,
                alpha);
    }

    public Model createModelFast(List<ModelAttribute> attributes, int length,
            RenderType renderType) {
        return gl.createModelFast(attributes, length, renderType);
    }

    public Model createModelStatic(List<ModelAttribute> attributes, int length,
            int[] index, RenderType renderType) {
        return createModelStatic(attributes, length, index, index.length,
                renderType);
    }

    public Model createModelStatic(List<ModelAttribute> attributes, int length,
            int[] index, int indexLength, RenderType renderType) {
        return gl.createModelStatic(attributes, length, index, indexLength,
                renderType);
    }

    public ModelHybrid createModelHybrid(List<ModelAttribute> attributes,
            int length, List<ModelAttribute> attributesStream, int lengthStream,
            RenderType renderType) {
        return gl.createModelHybrid(attributes, length, attributesStream,
                lengthStream, renderType);
    }

    public Shader createShader(String asset) {
        return createShader(asset, new ShaderCompileInformation());
    }

    public Shader createShader(String asset,
            Consumer<ShaderCompileInformation> consumer) {
        ShaderCompileInformation information = new ShaderCompileInformation();
        consumer.accept(information);
        return createShader(asset, information);
    }

    public Shader createShader(String asset,
            ShaderCompileInformation information) {
        try {
            ReadSource program = gl.engine().files().get(asset + ".program");
            String source = program.readReturn(stream -> ProcessStream
                    .process(stream, ProcessStream.asString()));
            CompiledShader shader = compiled(source);
            return createShader(shader, information);
        } catch (ShaderCompileException | IOException e) {
            engine.crash(e);
            throw new AssertionError();
        }
    }

    public Shader createShader(CompiledShader shader,
            ShaderCompileInformation information) {
        return gl.createShader(shader, information);
    }

    private CompiledShader compiled(String source)
            throws ShaderCompileException {
        CompiledShader shader = shaderCache.get(source);
        if (shader == null) {
            synchronized (shaderCompiler) {
                shader = shaderCompiler.compile(source);
            }
            shaderCache.put(source, shader);
        }
        return shader;
    }

    public void clear() {
        gl.clear();
    }

    public void reset() {
        gl.reset();
    }
}
