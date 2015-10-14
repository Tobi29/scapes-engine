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
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.opengl.shader.Shader;

public class Gui extends GuiComponent {
    protected final GuiStyle style;
    private final GuiAlignment alignment;
    private GuiComponent lastClicked;

    public Gui(GuiStyle style, GuiAlignment alignment) {
        this(800, 512, style, alignment);
    }

    public Gui(int width, int height, GuiStyle style, GuiAlignment alignment) {
        super(width, height);
        this.style = style;
        this.alignment = alignment;
    }

    public void add(Gui add) {
        add(add, 0.0f, 0.0f);
    }

    public void add(Gui add, float x, float y) {
        changeComponents.add(() -> append(add, x, y));
    }

    @Override
    public void render(GL gl, Shader shader, double delta) {
        if (visible) {
            if (alignment == GuiAlignment.STRETCH) {
                super.render(gl, shader, delta);
            } else {
                MatrixStack matrixStack = gl.matrixStack();
                Matrix matrix = matrixStack.push();
                float ratio =
                        (float) gl.sceneHeight() / gl.sceneWidth() * 1.5625f;
                matrix.scale(ratio, 1.0f, 1.0f);
                switch (alignment) {
                    case CENTER:
                        matrix.translate(-400.0f + 400.0f / ratio, 0.0f, 0.0f);
                        break;
                    case RIGHT:
                        matrix.translate(-800.0f + 800.0f / ratio, 0.0f, 0.0f);
                        break;
                }
                super.render(gl, shader, delta);
                matrixStack.pop();
            }
        }
    }

    @Override
    public void update(double mouseX, double mouseY, boolean mouseInside,
            ScapesEngine engine) {
        super.update(alignedX(mouseX, engine), mouseY, mouseInside, engine);
    }

    public GuiStyle style() {
        return style;
    }

    public GuiComponent lastClicked() {
        return lastClicked;
    }

    protected void setLastClicked(GuiComponent component) {
        lastClicked = component;
    }

    protected double alignedX(double x, ScapesEngine engine) {
        switch (alignment) {
            case LEFT:
                x *= engine.container().containerWidth() * 512.0 /
                        engine.container().containerHeight() /
                        800.0;
                return x;
            case CENTER: {
                double width = engine.container().containerWidth() * 512.0 /
                        engine.container().containerHeight();
                x *= width / 800.0;
                x += (800.0 - width) * 0.5;
                return x;
            }
            case RIGHT: {
                double width = engine.container().containerWidth() * 512.0 /
                        engine.container().containerHeight();
                x *= width / 800.0;
                x += 800.0 - width;
                return x;
            }
        }
        return x;
    }
}
