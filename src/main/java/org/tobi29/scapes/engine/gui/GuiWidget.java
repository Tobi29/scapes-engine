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
import org.tobi29.scapes.engine.opengl.*;
import org.tobi29.scapes.engine.opengl.shader.Shader;

public class GuiWidget extends Gui {
    private final VAO vao;
    private final GuiComponentVisiblePane titleBar;
    private boolean dragging;
    private double dragX, dragY;

    public GuiWidget(int x, int y, int width, int height, String name) {
        super(x, y, width, height, GuiAlignment.LEFT);
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, -16.0f, width, height, 0.2f);
        mesh.addVertex(0.0f, -16.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 0.0f);
        mesh.addVertex(0.0f, height, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 0.0f);
        mesh.addVertex(width, -16.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 0.0f);
        mesh.addVertex(width, -16.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 0.0f);
        mesh.addVertex(0.0f, height, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 0.0f);
        mesh.addVertex(width, height, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 0.0f);
        vao = mesh.finish();
        titleBar = new GuiComponentVisiblePane(this, 0, -16, width, 16);
        new GuiComponentText(titleBar, 2, 2, 12, name);
    }

    @Override
    public void update(double mouseX, double mouseY, boolean mouseInside,
            ScapesEngine engine) {
        super.update(mouseX, mouseY, mouseInside, engine);
        double mouseXX = alignedX(mouseX, engine);
        GuiController guiController = engine.guiController();
        if (guiController.leftClick() &&
                titleBar.checkInside(mouseXX - x, mouseY - y)) {
            dragX = x - mouseXX;
            dragY = y - mouseY;
            dragging = true;
        }
        if (guiController.leftDrag()) {
            if (dragging) {
                x = (int) (dragX + mouseXX);
                y = (int) (dragY + mouseY);
            }
        } else {
            dragging = false;
        }
    }

    @Override
    public void renderComponent(GL gl, Shader shader, FontRenderer font,
            double delta) {
        gl.textures().unbind(gl);
        gl.setAttribute4f(OpenGL.COLOR_ATTRIBUTE, 1.0f, 1.0f, 1.0f, 1.0f);
        vao.render(gl, shader);
    }
}
