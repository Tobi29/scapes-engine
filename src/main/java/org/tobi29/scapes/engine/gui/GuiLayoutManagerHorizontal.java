package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.utils.Triple;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

import java.util.List;
import java.util.Set;

public class GuiLayoutManagerHorizontal extends GuiLayoutManager {
    public GuiLayoutManagerHorizontal(Vector2 start, Vector2 maxSize,
            Set<GuiComponent> components) {
        super(start, maxSize, components);
    }

    @Override
    protected void layout(List<Triple<GuiComponent, Vector2, Vector2>> output) {
        int unsized = 0;
        double usedWidth = 0.0;
        for (GuiComponent component : components) {
            GuiLayoutData data = component.parent;
            if (data instanceof GuiLayoutDataHorizontal) {
                GuiLayoutDataHorizontal dataHorizontal =
                        (GuiLayoutDataHorizontal) data;
                if (dataHorizontal.width() < 0.0) {
                    unsized++;
                } else {
                    Vector2 marginStart = dataHorizontal.marginStart();
                    Vector2 marginEnd = dataHorizontal.marginEnd();
                    usedWidth += dataHorizontal.width() + marginStart.doubleX() +
                            marginEnd.doubleX();
                }
            }
        }
        MutableVector2 pos = new MutableVector2d();
        MutableVector2 size = new MutableVector2d();
        MutableVector2 offset = new MutableVector2d(start);
        MutableVector2 outSize = new MutableVector2d();
        Vector2 maxSize =
                new Vector2d((this.maxSize.doubleX() - usedWidth) / unsized,
                        this.maxSize.doubleY());
        for (GuiComponent component : components) {
            GuiLayoutData data = component.parent;
            pos.set(offset.now());
            size.set(data.width(), data.height());
            if (data instanceof GuiLayoutDataHorizontal) {
                GuiLayoutDataHorizontal dataHorizontal =
                        (GuiLayoutDataHorizontal) data;
                Vector2 marginStart = dataHorizontal.marginStart();
                Vector2 marginEnd = dataHorizontal.marginEnd();
                if (size.doubleY() >= 0.0) {
                    pos.plusY((maxSize.doubleY() - size.doubleY() -
                            marginStart.doubleY() - marginEnd.doubleY()) * 0.5);
                }
                size(size, maxSize.minus(marginStart).minus(marginEnd));
                pos.plus(marginStart);
                offset.plusX(size.doubleX() + marginStart.doubleX() +
                        marginEnd.doubleX());
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
