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
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.utils.Pair;

public class Gui extends GuiComponent {
    private final GuiAlignment alignment;
    private GuiComponent lastClicked;

    public Gui(GuiAlignment alignment) {
        this(0, 0, 800, 512, alignment);
    }

    public Gui(int x, int y, int width, int height, GuiAlignment alignment) {
        super(x, y, width, height);
        this.alignment = alignment;
    }

    public void add(Gui add) {
        changeComponents.add(new Pair<>(true, add));
    }

    @Override
    public void render(GL gl, Shader shader, FontRenderer font, double delta) {
        if (visible) {
            if (alignment == GuiAlignment.STRETCH) {
                super.render(gl, shader, font, delta);
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
                super.render(gl, shader, font, delta);
                matrixStack.pop();
            }
        }
    }

    @Override
    public void update(double mouseX, double mouseY, boolean mouseInside,
            ScapesEngine engine) {
        super.update(alignedX(mouseX, engine), mouseY, mouseInside, engine);
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
                x *= engine.container().containerWidth() * 512.0f /
                        engine.container().containerHeight() /
                        800.0f;
                return x;
            case CENTER: {
                float width = engine.container().containerWidth() * 512.0f /
                        engine.container().containerHeight();
                x *= width / 800.0f;
                x += (800.0f - width) * 0.5f;
                return x;
            }
            case RIGHT: {
                float width = engine.container().containerWidth() * 512.0f /
                        engine.container().containerHeight();
                x *= width / 800.0f;
                x += 800.0f - width;
                return x;
            }
        }
        return x;
    }
}
