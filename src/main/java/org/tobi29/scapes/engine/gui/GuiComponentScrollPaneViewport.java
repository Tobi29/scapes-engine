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
    protected Optional<GuiComponentSliderVert> slider = Optional.empty();
    private double maxY, scroll;

    public GuiComponentScrollPaneViewport(GuiLayoutData parent,
            int scrollStep) {
        super(parent);
        onScroll(event -> {
            if (event.screen()) {
                scroll -= event.relativeY();
            } else {
                scroll -= event.relativeY() * scrollStep;
            }
            scroll = FastMath.clamp(scroll, 0,
                    Math.max(0, maxY - event.size().doubleY()));
            Optional<GuiComponentSliderVert> slider = this.slider;
            if (slider.isPresent()) {
                double limit = Math.max(0, maxY - event.size().doubleY());
                if (limit > 0.0) {
                    slider.get().setValue(scroll / limit);
                } else {
                    slider.get().setValue(0.0);
                }
            }
        });
    }

    @Override
    public void render(GL gl, Shader shader, double delta, Vector2 size) {
        if (visible) {
            MatrixStack matrixStack = gl.matrixStack();
            Matrix matrix = matrixStack.push();
            Vector3 start = matrix.modelView().multiply(Vector3d.ZERO);
            Vector3 end = matrix.modelView().multiply(
                    new Vector3d(size.doubleX(), size.doubleY(), 0.0));
            matrixStack.pop();
            gl.enableScissor(start.intX(), start.intY(),
                    end.intX() - start.intX(), end.intY() - start.intY());
            super.render(gl, shader, delta, size);
            gl.disableScissor();
        }
    }

    @Override
    protected void updateComponent(ScapesEngine engine, Vector2 size) {
        Optional<GuiComponentSliderVert> slider = this.slider;
        if (slider.isPresent()) {
            scroll = slider.get().value() * Math.max(0, maxY - size.doubleY());
        }
    }

    @Override
    public void updateChildren(ScapesEngine engine, Vector2 size) {
        while (!changeComponents.isEmpty()) {
            changeComponents.poll().run();
        }
        GuiLayoutManager layout = layoutManager(size);
        Streams.of(layout.layout()).forEach(component -> {
            if (component.a.removing) {
                drop(component.a);
            } else {
                component.a.update(engine, component.c);
            }
        });
        setMaxY(layout.size().doubleY(), size.doubleY());
    }

    @Override
    protected GuiLayoutManager layoutManager(Vector2 size) {
        return new GuiLayoutManagerVertical(new Vector2d(0.0, -scroll), size,
                components);
    }

    public void setMaxY(double maxY, double height) {
        this.maxY = maxY;
        Optional<GuiComponentSliderVert> slider = this.slider;
        if (slider.isPresent()) {
            if (maxY <= 0) {
                slider.get().setSliderHeight(height);
            } else {
                slider.get().setSliderHeight(
                        (int) FastMath.min(height * height / maxY, height));
            }
        }
    }

    public void setSlider(GuiComponentSliderVert slider) {
        this.slider = Optional.of(slider);
    }

    public void removeSlider() {
        slider = Optional.empty();
    }
}
