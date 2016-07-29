package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.graphics.FontRenderer;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public interface GuiStyle {
    ScapesEngine engine();

    FontRenderer font();

    void pane(GuiRenderer renderer, Vector2 size);

    void button(GuiRenderer renderer, Vector2 size, boolean hover);

    void border(GuiRenderer renderer, Vector2 size);

    void slider(GuiRenderer renderer, Vector2 size, boolean horizontal,
            double value, double sliderSize, boolean hover);

    void separator(GuiRenderer renderer, Vector2 size);

    void widget(GuiRenderer renderer, Vector2 size);

    void widgetTitle(GuiRenderer renderer, Vector2 size);
}
