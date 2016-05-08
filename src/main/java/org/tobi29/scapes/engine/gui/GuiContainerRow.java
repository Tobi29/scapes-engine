package org.tobi29.scapes.engine.gui;

import java8.util.function.Function;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

public interface GuiContainerRow {
    default <T extends GuiComponent> T addVert(double marginX, double marginY,
            double width, double height,
            Function<GuiLayoutDataVertical, T> child) {
        return addVert(marginX, marginY, marginX, marginY, width, height,
                child);
    }

    default <T extends GuiComponent> T addVert(double marginStartX,
            double marginStartY, double marginEndX, double marginEndY,
            double width, double height,
            Function<GuiLayoutDataVertical, T> child) {
        return addVert(marginStartX, marginStartY, marginEndX, marginEndY,
                width, height, 0, child);
    }

    default <T extends GuiComponent> T addVert(double marginStartX,
            double marginStartY, double marginEndX, double marginEndY,
            double width, double height, long priority,
            Function<GuiLayoutDataVertical, T> child) {
        return addVert(new Vector2d(marginStartX, marginStartY),
                new Vector2d(marginEndX, marginEndY),
                new Vector2d(width, height), priority, child);
    }

    <T extends GuiComponent> T addVert(Vector2 marginStart, Vector2 marginEnd,
            Vector2 size, long priority,
            Function<GuiLayoutDataVertical, T> child);
}
