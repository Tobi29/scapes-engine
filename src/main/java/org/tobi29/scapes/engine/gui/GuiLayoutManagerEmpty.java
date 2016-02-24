package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.utils.Triple;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

import java.util.Collections;
import java.util.List;

public class GuiLayoutManagerEmpty extends GuiLayoutManager {
    public static final GuiLayoutManager INSTANCE = new GuiLayoutManagerEmpty();

    public GuiLayoutManagerEmpty() {
        super(Vector2d.ZERO, Vector2d.ZERO, Collections.emptySet());
    }

    @Override
    protected void layout(List<Triple<GuiComponent, Vector2, Vector2>> output) {
        MutableVector2 size = new MutableVector2d();
        MutableVector2 outSize = new MutableVector2d();
        for (GuiComponent component : components) {
            GuiLayoutData data = component.parent;
            if (data instanceof GuiLayoutDataAbsolute) {
                GuiLayoutDataAbsolute dataAbsolute =
                        (GuiLayoutDataAbsolute) data;
                size.set(data.width(), data.height());
                size(size, maxSize);
                setSize(dataAbsolute.pos().plus(size.now()), outSize);
                output.add(new Triple<>(component, dataAbsolute.pos(),
                        size.now()));
            } else {
                throw new IllegalStateException(
                        "Invalid layout node: " + data.getClass());
            }
        }
        this.size = outSize.now();
    }
}
