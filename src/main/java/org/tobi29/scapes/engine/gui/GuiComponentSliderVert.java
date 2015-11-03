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
import org.tobi29.scapes.engine.utils.math.FastMath;

public class GuiComponentSliderVert extends GuiComponent {
    private double value;
    private int sliderHeight;
    private boolean hover, dragging;
    private Pair<VAO, Texture> vao;

    public GuiComponentSliderVert(GuiLayoutData parent, int width, int height,
            double value) {
        this(parent, width, height, 16, value);
    }

    public GuiComponentSliderVert(GuiLayoutData parent, int width, int height,
            int sliderHeight, double value) {
        super(parent, width, height);
        this.sliderHeight = sliderHeight;
        this.value = value;
        updateMesh();
    }

    @Override
    public void clickLeft(GuiComponentEvent event, ScapesEngine engine) {
        super.clickLeft(event, engine);
        dragging = true;
    }

    @Override
    public void renderComponent(GL gl, Shader shader, double delta) {
        vao.b.bind(gl);
        vao.a.render(gl, shader);
    }

    @Override
    public void setHover(boolean hover, ScapesEngine engine) {
        if (this.hover != hover) {
            this.hover = hover;
            updateMesh();
        }
    }

    @Override
    public void update(double mouseX, double mouseY, boolean mouseInside,
            ScapesEngine engine) {
        super.update(mouseX, mouseY, mouseInside, engine);
        if (dragging) {
            if (engine.guiController().leftDrag()) {
                value = FastMath.clamp(
                        (mouseY - sliderHeight * 0.5) / (height - sliderHeight),
                        0, 1);
                updateMesh();
                if (hovering) {
                    hover(new GuiComponentHoverEvent(mouseX, mouseY,
                            GuiComponentHoverEvent.State.HOVER));
                } else {
                    hover(new GuiComponentHoverEvent(mouseX, mouseY,
                            GuiComponentHoverEvent.State.ENTER));
                    hovering = true;
                }
            } else {
                dragging = false;
            }
        }
        if (checkInside(mouseX, mouseY)) {
            if (engine.guiController().leftClick()) {
                engine.sounds()
                        .playSound("Engine:sound/Click.ogg", "sound.GUI", 1.0f,
                                1.0f);
            }
        }
    }

    public double value() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
        updateMesh();
    }

    public void setSliderHeight(int value) {
        sliderHeight = value;
        updateMesh();
    }

    private void updateMesh() {
        vao = gui.style()
                .slider(width, height, false, (float) value, sliderHeight,
                        hover);
    }
}
