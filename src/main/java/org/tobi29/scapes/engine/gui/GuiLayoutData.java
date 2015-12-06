package org.tobi29.scapes.engine.gui;

import java8.util.Optional;

public class GuiLayoutData {
    private final Optional<GuiComponent> parent;
    private final boolean blocksEvents;

    public GuiLayoutData(Optional<GuiComponent> parent, boolean blocksEvents) {
        this.parent = parent;
        this.blocksEvents = blocksEvents;
    }

    public Optional<GuiComponent> parent() {
        return parent;
    }

    public boolean blocksEvents() {
        return blocksEvents;
    }
}
