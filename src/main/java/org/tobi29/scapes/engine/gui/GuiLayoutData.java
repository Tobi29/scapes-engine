package org.tobi29.scapes.engine.gui;

import java.util.Optional;

public class GuiLayoutData {
    private final Optional<GuiComponent> parent;

    public GuiLayoutData(Optional<GuiComponent> parent) {
        this.parent = parent;
    }

    public Optional<GuiComponent> parent() {
        return parent;
    }
}
