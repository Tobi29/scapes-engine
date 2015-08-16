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

package org.tobi29.scapes.engine.opengl;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.opengl.shader.ShaderManager;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.opengl.texture.TextureManager;
import org.tobi29.scapes.engine.utils.BufferCreatorNative;
import org.tobi29.scapes.engine.utils.graphics.Cam;
import org.tobi29.scapes.engine.utils.math.matrix.Matrix4f;

public abstract class GL implements OpenGL {
    protected final ScapesEngine engine;
    private final FontRenderer defaultFont;
    private final TextureManager textureManager;
    private final ShaderManager shaderManager;
    private final MatrixStack matrixStack;
    private final Matrix4f projectionMatrix, modelViewProjectionMatrix;
    private double resolutionMultiplier = 1.0;
    private int containerWidth = 1, containerHeight = 1, contentWidth = 1,
            contentHeight = 1;

    protected GL(ScapesEngine engine, Container container) {
        this.engine = engine;
        matrixStack = new MatrixStack(64);
        projectionMatrix = new Matrix4f(BufferCreatorNative.floatsD(16));
        modelViewProjectionMatrix =
                new Matrix4f(BufferCreatorNative.floatsD(16));
        textureManager = new TextureManager(engine);
        shaderManager = new ShaderManager(engine);
        resolutionMultiplier = engine.config().resolutionMultiplier();
        container.loadFont("Engine:font/QuicksandPro-Regular");
        defaultFont = new FontRenderer(
                container.createGlyphRenderer("Quicksand Pro", 64));
    }

    public FontRenderer defaultFont() {
        return defaultFont;
    }

    public void reshape(int contentWidth, int contentHeight, int containerWidth,
            int containerHeight, double resolutionMultiplier) {
        this.contentWidth = contentWidth;
        this.contentHeight = contentHeight;
        this.containerWidth = containerWidth;
        this.containerHeight = containerHeight;
        this.resolutionMultiplier = resolutionMultiplier;
    }

    public void dispose() {
        textureManager.clearCache(this);
        defaultFont.dispose(this);
        VAO.disposeAll(this);
    }

    public TextureManager textures() {
        return textureManager;
    }

    public ShaderManager shaders() {
        return shaderManager;
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

    @OpenGLFunction
    public void reset() {
        Texture.disposeAll(this);
        VAO.disposeAll(this);
        FBO.disposeAll(this);
        shaderManager.clearCache(this);
    }
}
