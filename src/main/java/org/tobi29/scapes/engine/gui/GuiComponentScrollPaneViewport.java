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
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3d;

public class GuiComponentScrollPaneViewport extends GuiComponentPaneHeavy {
    protected Optional<GuiComponentSliderVert> sliderX = Optional.empty(),
            sliderY = Optional.empty();
    protected double maxX, maxY, scrollX, scrollY;

    public GuiComponentScrollPaneViewport(GuiLayoutData parent,
            int scrollStep) {
        super(parent);
        onScroll(event -> {
            if (event.screen()) {
                scrollX -= event.relativeX();
                scrollY -= event.relativeY();
            } else {
                scrollX -= event.relativeX() * scrollStep;
                scrollY -= event.relativeY() * scrollStep;
            }
            scrollX = FastMath.clamp(scrollX, 0,
                    Math.max(0, maxX - event.size().doubleX()));
            scrollY = FastMath.clamp(scrollY, 0,
                    Math.max(0, maxY - event.size().doubleY()));
            Optional<GuiComponentSliderVert> slider = sliderX;
            if (slider.isPresent()) {
                double limit = Math.max(0, maxX - event.size().doubleY());
                if (limit > 0.0) {
                    slider.get().setValue(scrollX / limit);
                } else {
                    slider.get().setValue(0.0);
                }
            }
            slider = sliderY;
            if (slider.isPresent()) {
                double limit = Math.max(0, maxY - event.size().doubleY());
                if (limit > 0.0) {
                    slider.get().setValue(scrollY / limit);
                } else {
                    slider.get().setValue(0.0);
                }
            }
        });
    }

    @Override
    public void render(GL gl, Shader shader, Vector2 size) {
        if (visible) {
            MatrixStack matrixStack = gl.matrixStack();
            Matrix matrix = matrixStack.current();
            Vector3 start = matrix.modelView().multiply(Vector3d.ZERO);
            Vector3 end = matrix.modelView().multiply(
                    new Vector3d(size.doubleX(), size.doubleY(), 0.0));
            gl.enableScissor(start.intX(), start.intY(),
                    end.intX() - start.intX(), end.intY() - start.intY());
            super.render(gl, shader, size);
            gl.disableScissor();
        }
    }

    @Override
    public void updateComponent(ScapesEngine engine, double delta) {
        size(engine).ifPresent(size -> {
            GuiLayoutManager layout = layoutManager(size);
            layout.layout();
            setMax(layout.size(), size);
        });
    }

    @Override
    protected void transform(Matrix matrix, Vector2 size) {
        matrix.translate((float) -scrollX, (float) -scrollY, 0.0f);
    }

    private void setMax(Vector2 max, Vector2 size) {
        if (maxX != max.doubleX() || maxY != max.doubleY()) {
            maxX = max.doubleX();
            maxY = max.doubleY();
            scrollX = FastMath.clamp(scrollX, 0,
                    Math.max(0, maxX - size.doubleX()));
            scrollY = FastMath.clamp(scrollY, 0,
                    Math.max(0, maxY - size.doubleY()));
            Optional<GuiComponentSliderVert> slider = sliderX;
            if (slider.isPresent()) {
                if (maxY <= 0) {
                    slider.get().setSliderHeight(size.doubleX());
                } else {
                    slider.get().setSliderHeight((int) FastMath
                            .min(FastMath.sqr(size.doubleX()) / maxY,
                                    size.doubleX()));
                }
            }
            slider = sliderY;
            if (slider.isPresent()) {
                if (maxY <= 0) {
                    slider.get().setSliderHeight(size.doubleY());
                } else {
                    slider.get().setSliderHeight((int) FastMath
                            .min(FastMath.sqr(size.doubleY()) / maxY,
                                    size.doubleY()));
                }
            }
        }
    }

    public double maxX() {
        return maxX;
    }

    public double maxY() {
        return maxY;
    }

    public void setScrollX(double scroll) {
        scrollX = scroll;
    }

    public void setScrollY(double scroll) {
        scrollY = scroll;
    }

    public void setSliderX(GuiComponentSliderVert slider) {
        sliderX = Optional.of(slider);
    }

    public void removeSliderX() {
        sliderX = Optional.empty();
    }

    public void setSliderY(GuiComponentSliderVert slider) {
        sliderY = Optional.of(slider);
    }

    public void removeSliderY() {
        sliderY = Optional.empty();
    }
}
