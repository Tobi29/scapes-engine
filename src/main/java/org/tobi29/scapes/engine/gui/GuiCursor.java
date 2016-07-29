package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public class GuiCursor {
    private final MutableVector2 pos = new MutableVector2d(), guiPos =
            new MutableVector2d();

    public void set(Vector2 pos, Vector2 guiPos) {
        this.pos.set(pos);
        this.guiPos.set(guiPos);
    }

    public Vector2 pos() {
        return pos.now();
    }

    public double x() {
        return pos.doubleX();
    }

    public double y() {
        return pos.doubleY();
    }

    public Vector2 guiPos() {
        return guiPos.now();
    }

    public double guiX() {
        return guiPos.doubleX();
    }

    public double guiY() {
        return guiPos.doubleY();
    }
}
