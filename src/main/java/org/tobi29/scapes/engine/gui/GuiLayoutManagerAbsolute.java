package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.utils.Triple;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

import java.util.List;
import java.util.Set;

public class GuiLayoutManagerAbsolute extends GuiLayoutManager {
    public GuiLayoutManagerAbsolute(Vector2 start, Vector2 maxSize,
            Set<GuiComponent> components) {
        super(start, maxSize, components);
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
                size(size, maxSize, maxSize);
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
