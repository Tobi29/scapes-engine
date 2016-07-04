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

import java8.util.function.Function;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public class GuiComponentSlider extends GuiComponentSlab {
    protected final GuiComponentText text;
    private final Function<Double, String> textFilter;
    private double value;
    private boolean hover;

    public GuiComponentSlider(GuiLayoutData parent, int textSize, String text,
            double value) {
        this(parent, textSize, text, value, (text1, value1) -> text1 + ": " +
                (int) (value1 * 100) +
                '%');
    }

    public GuiComponentSlider(GuiLayoutData parent, int textSize, String text,
            double value, TextFilter textFilter) {
        super(parent);
        this.value = value;
        this.textFilter = v -> textFilter.filter(text, v);
        this.text = addSubHori(4, 0, -1, textSize,
                p -> new GuiComponentText(p, this.textFilter.apply(value)));
        on(GuiEvent.DRAG_LEFT, event -> setValue(
                (event.x() - 8) / (event.size().doubleX() - 16.0)));
        on(GuiEvent.SCROLL, event -> {
            if (!event.screen()) {
                double delta = event.relativeX() * 0.05;
                setValue(this.value - delta);
            }
        });
        on(GuiEvent.CLICK_LEFT, (event, engine) -> engine.sounds()
                .playSound("Engine:sound/Click.ogg", "sound.GUI", 1.0f, 1.0f));
        on(GuiEvent.HOVER_ENTER, event -> {
            hover = true;
            dirty();
        });
        on(GuiEvent.HOVER_LEAVE, event -> {
            hover = false;
            dirty();
        });
    }

    @Override
    protected void updateMesh(GuiRenderer renderer, Vector2 size) {
        gui.style().slider(renderer, size, true, (float) value, 16.0f, hover);
    }

    public double value() {
        return value;
    }

    public void setValue(double value) {
        this.value = FastMath.clamp(value, 0.0, 1.0);
        dirty();
        text.setText(textFilter.apply(this.value));
        gui.sendNewEvent(GuiEvent.CHANGE, new GuiComponentEvent(), this,
                gui.style().engine());
    }

    public interface TextFilter {
        String filter(String text, double value);
    }
}
