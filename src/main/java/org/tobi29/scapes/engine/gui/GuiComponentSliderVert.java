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

import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public class GuiComponentSliderVert extends GuiComponent {
    private double value, sliderHeight;
    private boolean hover;

    public GuiComponentSliderVert(GuiLayoutData parent, double value) {
        this(parent, 16, value);
    }

    public GuiComponentSliderVert(GuiLayoutData parent, int sliderHeight,
            double value) {
        super(parent);
        this.sliderHeight = sliderHeight;
        this.value = value;
        onDragLeft(event -> {
            this.value = FastMath.clamp((event.y() - this.sliderHeight * 0.5) /
                    (event.size().doubleY() - this.sliderHeight), 0, 1);
            dirty();
        });
        onClick((event, engine) -> engine.sounds()
                .playSound("Engine:sound/Click.ogg", "sound.GUI", 1.0f, 1.0f));
        onHover(event -> {
            switch (event.state()) {
                case ENTER:
                    hover = true;
                    dirty();
                    break;
                case LEAVE:
                    hover = false;
                    dirty();
                    break;
            }
        });
    }

    @Override
    protected void updateMesh(GuiRenderer renderer, Vector2 size) {
        gui.style().slider(renderer, size, false, (float) value, sliderHeight,
                hover);
    }

    public double value() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
        dirty();
    }

    public void setSliderHeight(double value) {
        sliderHeight = value;
        dirty();
    }
}
