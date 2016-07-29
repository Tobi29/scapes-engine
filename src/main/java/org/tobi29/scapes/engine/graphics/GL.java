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
package org.tobi29.scapes.engine.graphics;

import org.tobi29.scapes.engine.Container;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.utils.graphics.Cam;
import org.tobi29.scapes.engine.utils.graphics.Image;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.matrix.Matrix4f;
import org.tobi29.scapes.engine.utils.shader.CompiledShader;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

public abstract class GL {
    public static final int VERTEX_ATTRIBUTE = 0, COLOR_ATTRIBUTE = 1,
            TEXTURE_ATTRIBUTE = 2, NORMAL_ATTRIBUTE = 3;
    protected final ScapesEngine engine;
    protected final TextureManager textureManager;
    protected final MatrixStack matrixStack;
    protected final Matrix4f projectionMatrix = new Matrix4f(),
            modelViewProjectionMatrix = new Matrix4f();
    protected final GraphicsObjectTracker<Model> vaoTracker;
    protected final GraphicsObjectTracker<Texture> textureTracker;
    protected final GraphicsObjectTracker<Framebuffer> fboTracker;
    protected final GraphicsObjectTracker<Shader> shaderTracker;
    protected double resolutionMultiplier = 1.0;
    protected int containerWidth = 1, containerHeight = 1, contentWidth = 1,
            contentHeight = 1, currentFBO;
    private Thread mainThread;

    protected GL(ScapesEngine engine, Container container) {
        this.engine = engine;
        matrixStack = new MatrixStack(64);
        textureManager = new TextureManager(engine);
        vaoTracker = new GraphicsObjectTracker<>();
        textureTracker = new GraphicsObjectTracker<>();
        fboTracker = new GraphicsObjectTracker<>();
        shaderTracker = new GraphicsObjectTracker<>();
        resolutionMultiplier = engine.config().resolutionMultiplier();
        container.loadFont("Engine:font/QuicksandPro-Regular");
    }

    public void init() {
        mainThread = Thread.currentThread();
    }

    public void reshape(int contentWidth, int contentHeight, int containerWidth,
            int containerHeight, double resolutionMultiplier) {
        this.contentWidth = contentWidth;
        this.contentHeight = contentHeight;
        this.containerWidth = containerWidth;
        this.containerHeight = containerHeight;
        this.resolutionMultiplier = resolutionMultiplier;
        shaderTracker.disposeAll(this);
    }

    public ScapesEngine engine() {
        return engine;
    }

    public TextureManager textures() {
        return textureManager;
    }

    public MatrixStack matrixStack() {
        return matrixStack;
    }

    public Matrix4f projectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f modelViewProjectionMatrix() {
        projectionMatrix.multiply(matrixStack.current().modelView(),
                modelViewProjectionMatrix);
        return modelViewProjectionMatrix;
    }

    public void setProjectionPerspective(float width, float height, Cam cam) {
        projectionMatrix.identity();
        projectionMatrix
                .perspective(cam.fov, width / height, cam.near, cam.far);
        Matrix matrix = matrixStack.current();
        matrix.identity();
        Matrix4f viewMatrix = matrix.modelView();
        viewMatrix.rotate(-cam.tilt, 0.0f, 0.0f, 1.0f);
        viewMatrix.rotate(-cam.pitch - 90.0f, 1.0f, 0.0f, 0.0f);
        viewMatrix.rotate(-cam.yaw + 90.0f, 0.0f, 0.0f, 1.0f);
        enableCulling();
        enableDepthTest();
        setBlending(BlendingMode.NORMAL);
    }

    public void setProjectionOrthogonal(float x, float y, float width,
            float height) {
        projectionMatrix.identity();
        projectionMatrix
                .orthogonal(x, x + width, y + height, y, -1024.0f, 1024.0f);
        Matrix matrix = matrixStack.current();
        matrix.identity();
        disableCulling();
        disableDepthTest();
        setBlending(BlendingMode.NORMAL);
    }

    public int sceneWidth() {
        return (int) (contentWidth * resolutionMultiplier);
    }

    public int sceneHeight() {
        return (int) (contentHeight * resolutionMultiplier);
    }

    public double sceneSpace() {
        return FastMath.max(sceneWidth(), sceneHeight()) / 1920.0;
    }

    public int contentWidth() {
        return contentWidth;
    }

    public int contentHeight() {
        return contentHeight;
    }

    public int containerWidth() {
        return containerWidth;
    }

    public int containerHeight() {
        return containerHeight;
    }

    public GraphicsObjectTracker<Model> vaoTracker() {
        return vaoTracker;
    }

    public GraphicsObjectTracker<Texture> textureTracker() {
        return textureTracker;
    }

    public GraphicsObjectTracker<Framebuffer> fboTracker() {
        return fboTracker;
    }

    public GraphicsObjectTracker<Shader> shaderTracker() {
        return shaderTracker;
    }

    public int currentFBO() {
        return currentFBO;
    }

    public void setCurrentFBO(int currentFBO) {
        this.currentFBO = currentFBO;
    }

    public void clear() {
        vaoTracker.disposeAll(this);
        textureTracker.disposeAll(this);
        fboTracker.disposeAll(this);
        shaderTracker.disposeAll(this);
    }

    public void reset() {
        vaoTracker.resetAll();
        textureTracker.resetAll();
        fboTracker.resetAll();
        shaderTracker.resetAll();
    }

    public void check() {
        assert Thread.currentThread() == mainThread;
    }

    public abstract Texture createTexture(int width, int height,
            ByteBuffer buffer, int mipmaps, TextureFilter minFilter,
            TextureFilter magFilter, TextureWrap wrapS, TextureWrap wrapT);

    public abstract Framebuffer createFramebuffer(int width, int height,
            int colorAttachments, boolean depth, boolean hdr, boolean alpha);

    public abstract Model createModelFast(List<ModelAttribute> attributes,
            int length, RenderType renderType);

    public abstract Model createModelStatic(List<ModelAttribute> attributes,
            int length, int[] index, int indexLength, RenderType renderType);

    public abstract ModelHybrid createModelHybrid(
            List<ModelAttribute> attributes, int length,
            List<ModelAttribute> attributesStream, int lengthStream,
            RenderType renderType);

    public abstract Shader createShader(CompiledShader shader,
            ShaderCompileInformation information);

    public abstract void checkError(String message);

    public abstract void clear(float r, float g, float b, float a);

    public abstract void clearDepth();

    public abstract void disableCulling();

    public abstract void disableDepthTest();

    public abstract void disableDepthMask();

    public abstract void disableWireframe();

    public abstract void disableScissor();

    public abstract void enableCulling();

    public abstract void enableDepthTest();

    public abstract void enableDepthMask();

    public abstract void enableWireframe();

    public abstract void enableScissor(int x, int y, int width, int height);

    public abstract void setBlending(BlendingMode mode);

    public abstract void viewport(int x, int y, int width, int height);

    public abstract Image screenShot(int x, int y, int width, int height);

    public abstract Image screenShotFBO(Framebuffer fbo);

    public abstract void setAttribute1f(int id, float v0);

    public abstract void setAttribute2f(int id, float v0, float v1);

    public abstract void setAttribute3f(int id, float v0, float v1, float v2);

    public abstract void setAttribute4f(int id, float v0, float v1, float v2,
            float v3);

    public abstract void setAttribute2f(int uniform, FloatBuffer values);

    public abstract void setAttribute3f(int uniform, FloatBuffer values);

    public abstract void setAttribute4f(int uniform, FloatBuffer values);

    public abstract void replaceTexture(int x, int y, int width, int height,
            ByteBuffer buffer);

    public abstract void replaceTextureMipMap(int x, int y, int width,
            int height, ByteBuffer... buffers);

    public abstract void activeTexture(int i);
}
