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

import org.tobi29.scapes.engine.Container;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.fbo.FBOTracker;
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.opengl.shader.ShaderManager;
import org.tobi29.scapes.engine.opengl.texture.TextureManager;
import org.tobi29.scapes.engine.opengl.texture.TextureTracker;
import org.tobi29.scapes.engine.opengl.vao.VAOTracker;
import org.tobi29.scapes.engine.utils.graphics.Cam;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.matrix.Matrix4f;

public abstract class GL implements OpenGL {
    protected final ScapesEngine engine;
    private final TextureManager textureManager;
    private final ShaderManager shaderManager;
    private final MatrixStack matrixStack;
    private final Matrix4f projectionMatrix = new Matrix4f(),
            modelViewProjectionMatrix = new Matrix4f();
    private final VAOTracker vaoTracker;
    private final TextureTracker textureTracker;
    private final FBOTracker fboTracker;
    private double resolutionMultiplier = 1.0;
    private int containerWidth = 1, containerHeight = 1, contentWidth = 1,
            contentHeight = 1;

    protected GL(ScapesEngine engine, Container container) {
        this.engine = engine;
        matrixStack = new MatrixStack(64);
        textureManager = new TextureManager(engine);
        shaderManager = new ShaderManager(engine);
        vaoTracker = new VAOTracker();
        textureTracker = new TextureTracker();
        fboTracker = new FBOTracker();
        resolutionMultiplier = engine.config().resolutionMultiplier();
        container.loadFont("Engine:font/QuicksandPro-Regular");
    }

    public void reshape(int contentWidth, int contentHeight, int containerWidth,
            int containerHeight, double resolutionMultiplier) {
        this.contentWidth = contentWidth;
        this.contentHeight = contentHeight;
        this.containerWidth = containerWidth;
        this.containerHeight = containerHeight;
        this.resolutionMultiplier = resolutionMultiplier;
        shaderManager.disposeAll(this);
    }

    public ScapesEngine engine() {
        return engine;
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

    public VAOTracker vaoTracker() {
        return vaoTracker;
    }

    public TextureTracker textureTracker() {
        return textureTracker;
    }

    public FBOTracker fboTracker() {
        return fboTracker;
    }

    @OpenGLFunction
    public void dispose() {
        vaoTracker.disposeAll(this);
        textureTracker.disposeAll(this);
        fboTracker.disposeAll(this);
        shaderManager.resetAll();
    }

    @OpenGLFunction
    public void reset() {
        vaoTracker.resetAll();
        textureTracker.resetAll();
        fboTracker.resetAll();
        shaderManager.resetAll();
    }
}
