package org.tobi29.scapes.engine.gui;

import java8.util.Optional;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

public class GuiLayoutDataRoot extends GuiLayoutData {
    public GuiLayoutDataRoot() {
        super(Optional.empty(), new Vector2d(-1, -1), 0, false);
    }
}
