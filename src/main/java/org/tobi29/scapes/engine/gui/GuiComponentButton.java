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

package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.FontRenderer;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.Mesh;
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.shader.Shader;

public class GuiComponentButton extends GuiComponent {
    private boolean hover;
    private VAO vao;

    public GuiComponentButton(GuiComponent parent, int x, int y, int width,
            int height) {
        super(parent, x, y, width, height);
        updateMesh();
    }

    @Override
    public void clickLeft(GuiComponentEvent event, ScapesEngine engine) {
        super.clickLeft(event, engine);
        engine.sounds().playSound("Engine:sound/Click.ogg", 1.0f, 1.0f);
    }

    @Override
    public void clickRight(GuiComponentEvent event, ScapesEngine engine) {
        super.clickRight(event, engine);
        engine.sounds().playSound("Engine:sound/Click.ogg", 1.0f, 1.0f);
    }

    @Override
    public void renderComponent(GL gl, Shader shader, FontRenderer font,
            double delta) {
        gl.textures().unbind(gl);
        vao.render(gl, shader);
    }

    @Override
    public void setHover(boolean hover, ScapesEngine engine) {
        if (this.hover != hover) {
            this.hover = hover;
            updateMesh();
        }
    }

    private void updateMesh() {
        float a;
        if (hover) {
            a = 0.8f;
        } else {
            a = 0.6f;
        }
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, width, height, 0.2f);
        mesh.addVertex(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, a, 0.0f, 0.0f);
        mesh.addVertex(0.0f, height, 0.0f, 0.0f, 0.0f, 0.0f, a, 0.0f, 0.0f);
        mesh.addVertex(width, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, a, 0.0f, 0.0f);
        mesh.addVertex(width, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, a, 0.0f, 0.0f);
        mesh.addVertex(0.0f, height, 0.0f, 0.0f, 0.0f, 0.0f, a, 0.0f, 0.0f);
        mesh.addVertex(width, height, 0.0f, 0.0f, 0.0f, 0.0f, a, 0.0f, 0.0f);
        vao = mesh.finish();
    }
}
