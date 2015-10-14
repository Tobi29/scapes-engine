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
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;

import java.util.Optional;

public class GuiComponentScrollPaneViewport extends GuiComponent {
    protected final int scrollStep;
    protected final Optional<GuiComponentSliderVert> slider;
    private double maxY, scroll;

    public GuiComponentScrollPaneViewport(GuiComponent parent, int x, int y,
            int width, int height, int scrollStep) {
        this(parent, Optional.empty(), x, y, width, height, scrollStep);
    }

    public GuiComponentScrollPaneViewport(GuiComponent parent,
            GuiComponentSliderVert slider, int x, int y, int width, int height,
            int scrollStep) {
        this(parent, Optional.of(slider), x, y, width, height, scrollStep);
    }

    private GuiComponentScrollPaneViewport(GuiComponent parent,
            Optional<GuiComponentSliderVert> slider, int x, int y, int width,
            int height, int scrollStep) {
        super(parent, x, y, width, height);
        this.slider = slider;
        this.scrollStep = scrollStep;
    }

    @Override
    public void render(GL gl, Shader shader, double delta) {
        if (visible) {
            int yy = 0;
            GuiComponent child = this;
            while (true) {
                if (!child.parent.isPresent()) {
                    break;
                }
                GuiComponent parent = child.parent.get();
                MutableVector2 pos = parent.components.get(child);
                if (pos == null) {
                    return;
                }
                yy += pos.floatY();
                child = parent;
            }
            MatrixStack matrixStack = gl.matrixStack();
            Matrix matrix = matrixStack.push();
            transform(matrix);
            renderComponent(gl, shader, delta);
            matrix = matrixStack.push();
            matrix.translate(0.0f, (float) -scroll, 0.0f);
            gl.enableScissor(0, yy, 800, height);
            components.forEach((component, pos) -> {
                if (!inside(pos)) {
                    return;
                }
                Matrix childMatrix = matrixStack.push();
                childMatrix.translate(pos.floatX(), pos.floatY(), 0.0f);
                component.render(gl, shader, delta);
                matrixStack.pop();
            });
            gl.disableScissor();
            matrixStack.pop();
            renderOverlay(gl, shader);
            matrixStack.pop();
        }
    }

    @Override
    public void update(double mouseX, double mouseY, boolean mouseInside,
            ScapesEngine engine) {
        super.update(mouseX, mouseY, mouseInside, engine);
        double lastScroll = scroll;
        boolean inside = mouseInside && checkInside(mouseX, mouseY);
        if (inside) {
            GuiController guiController = engine.guiController();
            scroll -= guiController.scroll() * scrollStep;
        }
        scroll = FastMath.clamp(scroll, 0,
                Math.max(0, maxY + scrollStep - height));
        if (slider.isPresent()) {
            if (scroll != lastScroll) {
                slider.get().setValue(
                        scroll / Math.max(0, maxY + scrollStep - height));
            } else {
                scroll = (int) (slider.get().value() *
                        Math.max(0, maxY + scrollStep - height));
            }
        }
    }

    @Override
    protected void updateChild(GuiComponent component, double mouseX,
            double mouseY, boolean inside, ScapesEngine engine) {
        boolean childInside = inside && inside(component);
        component.update(mouseX, mouseY + scroll, childInside, engine);
    }

    protected boolean inside(GuiComponent component) {
        MutableVector2 pos = components.get(component);
        return pos != null && inside(pos);
    }

    protected boolean inside(MutableVector2 pos) {
        double y = pos.doubleY() - scroll;
        return y > -scrollStep && y < height;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
        maxY += scrollStep;
        if (slider.isPresent()) {
            if (maxY <= 0) {
                slider.get().setSliderHeight(height);
            } else {
                slider.get().setSliderHeight(
                        (int) FastMath.min(height * height / maxY, height));
            }
        }
    }
}
