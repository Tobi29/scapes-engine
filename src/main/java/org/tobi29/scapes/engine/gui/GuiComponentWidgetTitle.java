package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

public class GuiComponentWidgetTitle extends GuiComponentSlab {
    public GuiComponentWidgetTitle(GuiLayoutData parent, int textSize,
            String text) {
        this(parent, 4, textSize, text);
    }

    public GuiComponentWidgetTitle(GuiLayoutData parent, int textX,
            int textSize, String text) {
        super(parent);
        addSubHori(textX, 0, -1, textSize, p -> new GuiComponentText(p, text));
    }

    @Override
    public void updateMesh(GuiRenderer renderer, Vector2 size) {
        gui.style().widgetTitle(renderer, size);
    }

    @Override
    protected GuiLayoutManager newLayoutManager(Vector2 size) {
        return new GuiLayoutManagerHorizontal(Vector2d.ZERO, size, components);
    }
}