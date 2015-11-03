package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

import java.util.Optional;

public class GuiLayoutDataAbsolute extends GuiLayoutData {
    private final MutableVector2 pos;

    public GuiLayoutDataAbsolute(GuiComponent parent, Vector2 pos) {
        super(Optional.of(parent));
        this.pos = new MutableVector2d(pos);
    }

    public Vector2 pos() {
        return pos.now();
    }

    public MutableVector2 posMutable() {
        return pos;
    }
}
