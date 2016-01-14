package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.utils.Triple;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

import java.util.List;
import java.util.Set;

public class GuiLayoutManagerVertical extends GuiLayoutManager {
    public GuiLayoutManagerVertical(Vector2 start, Vector2 maxSize,
            Set<GuiComponent> components) {
        super(start, maxSize, components);
    }

    @Override
    protected void layout(List<Triple<GuiComponent, Vector2, Vector2>> output) {
        int unsized = 0;
        double usedHeight = 0.0;
        for (GuiComponent component : components) {
            GuiLayoutData data = component.parent;
            if (data instanceof GuiLayoutDataVertical) {
                GuiLayoutDataVertical dataVertical =
                        (GuiLayoutDataVertical) data;
                if (dataVertical.height() < 0.0) {
                    unsized++;
                } else {
                    Vector2 marginStart = dataVertical.marginStart();
                    Vector2 marginEnd = dataVertical.marginEnd();
                    usedHeight +=
                            dataVertical.height() + marginStart.doubleY() +
                                    marginEnd.doubleY();
                }
            }
        }
        MutableVector2 pos = new MutableVector2d();
        MutableVector2 size = new MutableVector2d();
        MutableVector2 offset = new MutableVector2d(start);
        MutableVector2 outSize = new MutableVector2d();
        Vector2 maxSize = new Vector2d(this.maxSize.doubleX(),
                (this.maxSize.doubleY() - usedHeight) / unsized);
        for (GuiComponent component : components) {
            GuiLayoutData data = component.parent;
            pos.set(offset.now());
            size.set(data.width(), data.height());
            if (data instanceof GuiLayoutDataVertical) {
                GuiLayoutDataVertical dataVertical =
                        (GuiLayoutDataVertical) data;
                Vector2 marginStart = dataVertical.marginStart();
                Vector2 marginEnd = dataVertical.marginEnd();
                if (size.doubleX() >= 0.0) {
                    pos.plusX((maxSize.doubleX() - size.doubleX() -
                            marginStart.doubleX() - marginEnd.doubleX()) * 0.5);
                }
                size(size, maxSize.minus(marginStart).minus(marginEnd));
                pos.plus(marginStart);
                offset.plusY(size.doubleY() + marginStart.doubleY() +
                        marginEnd.doubleY());
                setSize(pos.now().plus(size.now()).plus(marginEnd), outSize);
            } else if (data instanceof GuiLayoutDataAbsolute) {
                GuiLayoutDataAbsolute dataAbsolute =
                        (GuiLayoutDataAbsolute) data;
                pos.set(dataAbsolute.pos());
                size(size, maxSize);
                setSize(pos.now().plus(size.now()), outSize);
            } else {
                throw new IllegalStateException(
                        "Invalid layout node: " + data.getClass());
            }
            output.add(new Triple<>(component, pos.now(), size.now()));
        }
        this.size = outSize.now();
    }
}
