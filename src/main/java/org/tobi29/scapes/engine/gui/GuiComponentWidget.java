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
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;

public class GuiComponentWidget extends GuiComponent {
    private final Pair<VAO, Texture> vao;
    private final GuiComponentWidgetTitle titleBar;
    private double dragX, dragY;
    private boolean dragging;

    public GuiComponentWidget(GuiComponent parent, int x, int y, int width,
            int height, String name) {
        super(parent, x, y, width, height);
        vao = gui.style().widget(width, height);
        titleBar =
                new GuiComponentWidgetTitle(this, 0, -16, width, 16, 12, name);
    }

    @Override
    public void renderComponent(GL gl, Shader shader, double delta) {
        vao.b.bind(gl);
        vao.a.render(gl, shader);
    }

    @Override
    public void update(double mouseX, double mouseY, boolean mouseInside,
            ScapesEngine engine) {
        super.update(mouseX, mouseY, mouseInside, engine);
        if (!parent.isPresent()) {
            return;
        }
        GuiComponent parent = this.parent.get();
        MutableVector2 pos = parent.components.get(this);
        if (pos == null) {
            return;
        }
        double mouseXX = mouseX + pos.doubleX();
        double mouseYY = mouseY + pos.doubleY();
        GuiController guiController = engine.guiController();
        if (guiController.leftClick() &&
                titleBar.checkInside(mouseX, mouseY + 16)) {
            dragX = pos.doubleX() - mouseXX;
            dragY = pos.doubleY() - mouseYY;
            dragging = true;
        }
        if (guiController.leftDrag()) {
            if (dragging) {
                pos.setX(dragX + mouseXX);
                pos.setY(dragY + mouseYY);
            }
        } else {
            dragging = false;
        }
    }
}
