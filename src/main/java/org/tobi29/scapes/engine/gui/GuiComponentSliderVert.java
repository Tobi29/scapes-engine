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
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.utils.math.FastMath;

public class GuiComponentSliderVert extends GuiComponent {
    private final VAO vaoShadow;
    public double value;
    private int sliderHeight;
    private boolean hover, dragging;
    private VAO vaoSlider;

    public GuiComponentSliderVert(GuiComponent parent, int x, int y, int width,
            int height, double value) {
        this(parent, x, y, width, height, 16, value);
    }

    public GuiComponentSliderVert(GuiComponent parent, int x, int y, int width,
            int height, int sliderHeight, double value) {
        super(parent, x, y, width, height);
        this.sliderHeight = sliderHeight;
        this.value = value;
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, width, height, 0.2f);
        mesh.addVertex(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.2f, 0.0f, 0.0f);
        mesh.addVertex(0.0f, height, 0.0f, 0.0f, 0.0f, 0.0f, 0.2f, 0.0f, 0.0f);
        mesh.addVertex(width, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.2f, 0.0f, 0.0f);
        mesh.addVertex(width, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.2f, 0.0f, 0.0f);
        mesh.addVertex(0.0f, height, 0.0f, 0.0f, 0.0f, 0.0f, 0.2f, 0.0f, 0.0f);
        mesh.addVertex(width, height, 0.0f, 0.0f, 0.0f, 0.0f, 0.2f, 0.0f, 0.0f);
        vaoShadow = mesh.finish();
        updateMesh();
    }

    @Override
    public void clickLeft(GuiComponentEvent event, ScapesEngine engine) {
        super.clickLeft(event, engine);
        dragging = true;
    }

    @Override
    public void renderComponent(GL gl, Shader shader, FontRenderer font,
            double delta) {
        float slider = (float) value * (height - sliderHeight) + y +
                sliderHeight * 0.5f;
        gl.textures().unbind(gl);
        MatrixStack matrixStack = gl.matrixStack();
        vaoShadow.render(gl, shader);
        Matrix matrix = matrixStack.push();
        matrix.translate(0.0f, slider, 0.0f);
        vaoSlider.render(gl, shader);
        matrixStack.pop();
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
                value = FastMath.clamp((mouseY - y - sliderHeight * 0.5) /
                        (height - sliderHeight), 0, 1);
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
                engine.sounds().playSound("Engine:sound/Click.ogg", 1.0f, 1.0f);
            }
        }
    }

    public void setSliderHeight(int value) {
        sliderHeight = value;
        updateMesh();
    }

    private void updateMesh() {
        float a;
        if (hover) {
            a = 0.8f;
        } else {
            a = 0.6f;
        }
        float sliderHalf = sliderHeight * 0.5f;
        vaoSlider = VAOUtility.createVCTI(
                new float[]{0.0f, sliderHalf, 0.0f, width, sliderHalf, 0.0f,
                        0.0f, -sliderHalf, 0.0f, width, -sliderHalf, 0.0f},
                new float[]{0.0f, 0.0f, 0.0f, a, 0.0f, 0.0f, 0.0f, a, 0.0f,
                        0.0f, 0.0f, a, 0.0f, 0.0f, 0.0f, a},
                new float[]{0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f},
                new int[]{0, 1, 2, 3, 2, 1}, RenderType.TRIANGLES);
    }
}
