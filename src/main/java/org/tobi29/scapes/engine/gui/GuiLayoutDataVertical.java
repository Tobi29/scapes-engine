package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.utils.math.vector.Vector2;

import java.util.Optional;

public class GuiLayoutDataVertical extends GuiLayoutData {
    private final Vector2 marginStart, marginEnd;

    public GuiLayoutDataVertical(GuiComponent parent, Vector2 marginStart,
            Vector2 marginEnd) {
        super(Optional.of(parent));
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
