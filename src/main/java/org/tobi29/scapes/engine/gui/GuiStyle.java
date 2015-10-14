package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.opengl.FontRenderer;
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.utils.Pair;

public interface GuiStyle {
    FontRenderer font();

    Pair<VAO, Texture> pane(float width, float height);

    Pair<VAO, Texture> button(float width, float height, boolean hover);

    Pair<VAO, Texture> border(float width, float height);

    Pair<VAO, Texture> slider(float width, float height, boolean horizontal,
            float value, float size, boolean hover);

    Pair<VAO, Texture> separator(float width, float height);

    Pair<VAO, Texture> widget(float width, float height);

    Pair<VAO, Texture> widgetTitle(float width, float height);
}
