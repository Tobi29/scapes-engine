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

public class GuiComponentSlider extends GuiComponent {
    private final String text;
    private final float textSize;
    private final int textX, textY;
    private final VAO vaoShadow;
    private final TextFilter textFilter;
    public double value;
    private boolean hover, dragging;
    private FontRenderer.Text vaoText;
    private VAO vaoSlider;
    private FontRenderer font;

    public GuiComponentSlider(GuiComponent parent, int x, int y, int width,
            int height, int textSize, String text, double value) {
        this(parent, x, y, width, height, textSize, text, value,
                (text1, value1) -> text1 + ": " +
                        (int) (value1 * 100) +
                        '%');
    }

    public GuiComponentSlider(GuiComponent parent, int x, int y, int width,
            int height, int textSize, String text, double value,
            TextFilter textFilter) {
        super(parent, x, y, width, height);
        this.width = width;
        this.height = height;
        this.text = text;
        this.textSize = textSize;
        this.value = value;
        this.textFilter = textFilter;
        textX = 4;
        textY = (height - textSize) / 2;
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
        float slider = (float) value * (width - 16) + 8;
        gl.textures().unbind(gl);
        vaoShadow.render(gl, shader);
        if (this.font != font) {
            this.font = font;
            updateText();
        }
        MatrixStack matrixStack = gl.matrixStack();
        Matrix matrix = matrixStack.push();
        matrix.translate(slider, 0.0f, 0.0f);
        vaoSlider.render(gl, shader);
        matrixStack.pop();
        vaoText.render(gl, shader);
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
                value = FastMath.clamp((mouseX - x - 8) / (width - 16.0), 0, 1);
                updateText();
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

    private void updateText() {
        if (font != null) {
            vaoText = font.render(textFilter.filter(text, value), textX, textY,
                    textSize, width, 1.0f, 1.0f, 1.0f, 1);
        }
    }

    private void updateMesh() {
        float a;
        if (hover) {
            a = 0.8f;
        } else {
            a = 0.6f;
        }
        vaoSlider = VAOUtility.createVCTI(
                new float[]{-8.0f, height, 0.0f, 8.0f, height, 0.0f, -8.0f,
                        0.0f, 0.0f, 8.0f, 0.0f, 0.0f},
                new float[]{0.0f, 0.0f, 0.0f, a, 0.0f, 0.0f, 0.0f, a, 0.0f,
                        0.0f, 0.0f, a, 0.0f, 0.0f, 0.0f, a},
                new float[]{0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f},
                new int[]{0, 1, 2, 3, 2, 1}, RenderType.TRIANGLES);
    }

    public interface TextFilter {
        String filter(String text, double value);
    }
}
