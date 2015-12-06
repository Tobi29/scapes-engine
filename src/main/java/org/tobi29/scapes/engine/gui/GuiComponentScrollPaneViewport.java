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

import java8.util.Optional;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3d;

public class GuiComponentScrollPaneViewport extends GuiComponentPane {
    protected final Optional<GuiComponentSliderVert> slider;
    private double maxY, scroll;

    public GuiComponentScrollPaneViewport(GuiLayoutData parent, int width,
            int height, int scrollStep) {
        this(parent, Optional.empty(), width, height, scrollStep);
    }

    public GuiComponentScrollPaneViewport(GuiLayoutData parent,
            GuiComponentSliderVert slider, int width, int height,
            int scrollStep) {
        this(parent, Optional.of(slider), width, height, scrollStep);
    }

    private GuiComponentScrollPaneViewport(GuiLayoutData parent,
            Optional<GuiComponentSliderVert> slider, int width, int height,
            int scrollStep) {
        super(parent, width, height);
        this.slider = slider;
        onScroll(event -> {
            if (event.screen()) {
                scroll -= event.relativeY();
            } else {
                scroll -= event.relativeY() * scrollStep;
            }
            scroll = FastMath.clamp(scroll, 0, Math.max(0, maxY - height));
            Optional<GuiComponentSliderVert> currentSlider = this.slider;
            if (currentSlider.isPresent()) {
                double limit = Math.max(0, maxY - height);
                if (limit > 0.0) {
                    currentSlider.get().setValue(scroll / limit);
                } else {
                    currentSlider.get().setValue(0.0);
                }
            }
        });
    }

    @Override
    public void render(GL gl, Shader shader, double delta) {
        if (visible) {
            MatrixStack matrixStack = gl.matrixStack();
            Matrix matrix = matrixStack.push();
            transform(matrix);
            renderComponent(gl, shader, delta);
            Vector3 start = matrix.modelView().multiply(Vector3d.ZERO);
            Vector3 end = matrix.modelView()
                    .multiply(new Vector3d(width, height, 0.0));
            gl.enableScissor(start.intX(), start.intY(),
                    end.intX() - start.intX(), end.intY() - start.intY());
            GuiLayoutManager layout = layoutManager();
            Streams.of(components).forEach(component -> {
                Vector2 pos = layout.layout(component);
                if (!inside(pos, component)) {
                    return;
                }
                Matrix childMatrix = matrixStack.push();
                childMatrix.translate(pos.floatX(), pos.floatY(), 0.0f);
                component.render(gl, shader, delta);
                matrixStack.pop();
            });
            gl.disableScissor();
            renderOverlay(gl, shader);
            matrixStack.pop();
        }
    }

    @Override
    protected void updateComponent(ScapesEngine engine) {
        if (slider.isPresent()) {
            scroll = slider.get().value() * Math.max(0, maxY - height);
        }
    }

    @Override
    public void updateChildren(ScapesEngine engine) {
        while (!changeComponents.isEmpty()) {
            changeComponents.poll().run();
        }
        GuiLayoutManager layout = layoutManager();
        Streams.of(components).forEach(component -> {
            layout.layout(component);
            component.update(engine);
        });
        setMaxY(layout.size().doubleY());
    }

    @Override
    protected GuiLayoutManager layoutManager() {
        return new GuiLayoutManager(new Vector2d(0.0, -scroll));
    }

    protected boolean inside(Vector2 pos, GuiComponent component) {
        double y = pos.doubleY();
        return y > -component.height && y < height;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
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
