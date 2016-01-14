package org.tobi29.scapes.engine.gui;

import java8.util.Optional;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public class GuiLayoutDataHorizontal extends GuiLayoutData {
    private final Vector2 marginStart, marginEnd;

    public GuiLayoutDataHorizontal(GuiComponent parent, Vector2 marginStart,
            Vector2 marginEnd, Vector2 size, long priority) {
        this(parent, marginStart, marginEnd, size, priority, false);
    }

    public GuiLayoutDataHorizontal(GuiComponent parent, Vector2 marginStart,
            Vector2 marginEnd, Vector2 size, long priority,
            boolean blocksEvents) {
        super(Optional.of(parent), size, priority, blocksEvents);
        this.marginStart = marginStart;
        this.marginEnd = marginEnd;
    }

    public Vector2 marginStart() {
        return marginStart;
    }

    public Vector2 marginEnd() {
        return marginEnd;
    }
}
