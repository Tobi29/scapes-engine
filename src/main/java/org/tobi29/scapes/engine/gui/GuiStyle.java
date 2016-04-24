package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.FontRenderer;
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public interface GuiStyle {
    ScapesEngine engine();

    FontRenderer font();

    Pair<VAO, Texture> pane(Vector2 size);

    Pair<VAO, Texture> button(Vector2 size, boolean hover);

    Pair<VAO, Texture> border(Vector2 size);

    Pair<VAO, Texture> slider(Vector2 size, boolean horizontal, double value,
            double sliderSize, boolean hover);

    Pair<VAO, Texture> separator(Vector2 size);

    Pair<VAO, Texture> widget(Vector2 size);

    Pair<VAO, Texture> widgetTitle(Vector2 size);
}
