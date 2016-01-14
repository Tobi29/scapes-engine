package org.tobi29.scapes.engine.gui;

import java8.util.Optional;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public class GuiLayoutData {
    private final Optional<GuiComponent> parent;
    private final long priority;
    private final boolean blocksEvents;
    private double width, height;

    public GuiLayoutData(Optional<GuiComponent> parent, Vector2 size,
            long priority, boolean blocksEvents) {
        this.parent = parent;
        width = size.doubleX();
        height = size.doubleY();
        this.priority = priority;
        this.blocksEvents = blocksEvents;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public void setWidth(double value) {
        width = value;
    }

    public void setHeight(double value) {
        height = value;
    }

    public Optional<GuiComponent> parent() {
        return parent;
    }

    public long priority() {
        return priority;
    }

    public boolean blocksEvents() {
        return blocksEvents;
    }
}
