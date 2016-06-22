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

import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.OpenGL;
import org.tobi29.scapes.engine.opengl.fbo.FBO;
import org.tobi29.scapes.engine.opengl.scenes.Scene;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.opengl.texture.TextureFBOColor;
import org.tobi29.scapes.engine.opengl.vao.RenderType;
import org.tobi29.scapes.engine.opengl.vao.VAO;
import org.tobi29.scapes.engine.opengl.vao.VAOUtility;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public abstract class GameState {
    protected final VAO vao;
    protected final ScapesEngine engine;
    protected final AtomicReference<Scene> newScene = new AtomicReference<>();
    protected Scene scene;
    protected FBO[] fbos;

    protected GameState(ScapesEngine engine, Scene scene) {
        this.engine = engine;
        this.scene = scene;
        newScene.set(scene);
        vao = VAOUtility.createVTI(engine,
                new float[]{0.0f, 540.0f, 0.0f, 960.0f, 540.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 960.0f, 0.0f, 0.0f},
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f},
                new int[]{0, 1, 2, 3, 2, 1}, RenderType.TRIANGLES);
    }

    public ScapesEngine engine() {
        return engine;
    }

    public void disposeState(GL gl) {
        scene.dispose(gl);
    }

    public void disposeState() {
        scene.dispose();
        dispose();
    }

    public void dispose() {
    }

    public abstract void init();

    public abstract boolean isMouseGrabbed();

    public Scene scene() {
        return scene;
    }

    public void setScene(Scene scene) {
        newScene.set(scene);
    }

    public abstract void step(double delta);

    public void render(GL gl, double delta, boolean updateSize) {
        Scene newScene = this.newScene.getAndSet(null);
        if (newScene != null) {
            if (scene != null) {
                scene.dispose(gl);
                scene.dispose();
            }
            fbos = null;
            newScene.setState(this);
            newScene.init(gl);
            scene = newScene;
        }
        int sceneWidth = scene.width(gl.sceneWidth());
        int sceneHeight = scene.height(gl.sceneHeight());
        if (fbos == null || updateSize) {
            fbos = new FBO[scene.renderPasses()];
            for (int i = 0; i < fbos.length; i++) {
                fbos[i] = new FBO(engine, sceneWidth, sceneHeight,
                        scene.colorAttachments(), true, true, false);
                scene.initFBO(i, fbos[i]);
            }
        }
        gl.checkError("Initializing-Scene-Rendering");
        fbos[0].activate(gl);
        gl.viewport(0, 0, sceneWidth, sceneHeight);
        gl.clearDepth();
        scene.renderScene(gl);
        fbos[0].deactivate(gl);
        gl.checkError("Scene-Rendering");
        gl.setProjectionOrthogonal(0.0f, 0.0f, 960.0f, 540.0f);
        for (int i = 0; i < fbos.length - 1; i++) {
            fbos[i + 1].activate(gl);
            //gl.viewport(0, 0, gl.sceneWidth(), gl.sceneHeight());
            renderPostProcess(gl, fbos[i], fbos[i], i);
            fbos[i + 1].deactivate(gl);
        }
        gl.viewport(0, 0, gl.contentWidth(), gl.contentHeight());
        renderPostProcess(gl, fbos[fbos.length - 1], fbos[0], fbos.length - 1);
        gl.checkError("Post-Processing");
        gl.setProjectionOrthogonal(0.0f, 0.0f,
                (float) engine.container().containerWidth() /
                        engine.container().containerHeight() * 540.0f, 540.0f);
        Shader shader = gl.shaders().get("Engine:shader/Gui", gl);
        engine.guiStack().render(gl, shader, engine, delta);
        gl.checkError("Gui-Rendering");
        scene.postRender(gl, delta);
        gl.checkError("Post-Render");
    }

    public void renderPostProcess(GL gl, FBO fbo, FBO depthFBO, int i) {
        gl.setAttribute4f(OpenGL.COLOR_ATTRIBUTE, 1.0f, 1.0f, 1.0f, 1.0f);
        Iterator<TextureFBOColor> texturesColor =
                fbo.texturesColor().iterator();
        TextureFBOColor textureColor = texturesColor.next();
        int j = 2;
        while (texturesColor.hasNext()) {
            gl.activeTexture(j);
            texturesColor.next().bind(gl);
            j++;
        }
        gl.activeTexture(1);
        depthFBO.textureDepth().bind(gl);
        gl.activeTexture(0);
        textureColor.bind(gl);
        vao.render(gl, scene.postProcessing(gl, i));
    }

    public FBO fbo(int i) {
        return fbos[i];
    }
}
