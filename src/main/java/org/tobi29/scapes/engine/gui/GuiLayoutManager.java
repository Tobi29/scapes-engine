package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

public class GuiLayoutManager {
    private final MutableVector2 offset = new MutableVector2d();
    private final MutableVector2 size = new MutableVector2d();
    private final Vector2 start;
    private double heightAdd;

    public GuiLayoutManager(Vector2 start) {
        this.start = start;
        offset.set(start);
    }

    public Vector2 layout(GuiComponent component) {
        GuiLayoutData data = component.parent;
        Vector2 pos = Vector2d.ZERO;
        if (data instanceof GuiLayoutDataAbsolute) {
            pos = ((GuiLayoutDataAbsolute) data).pos().plus(start);
            setSize(pos.plus(new Vector2d(component.width, component.height)));
        } else if (data instanceof GuiLayoutDataHorizontal) {
            GuiLayoutDataHorizontal dataHorizontal =
                    (GuiLayoutDataHorizontal) data;
            Vector2 marginStart = dataHorizontal.marginStart();
            Vector2 marginEnd = dataHorizontal.marginEnd();
            pos = offset.now().plus(marginStart);
            offset.plusX(component.width + marginStart.doubleX() +
                    marginEnd.doubleX());
            heightAdd = FastMath.max(heightAdd,
                    component.height + marginStart.doubleY() +
                            marginEnd.doubleY());
            setSize(pos.plus(new Vector2d(component.width, component.height))
                    .plus(marginEnd));
        } else if (data instanceof GuiLayoutDataVertical) {
            GuiLayoutDataVertical dataVertical = (GuiLayoutDataVertical) data;
            Vector2 marginStart = dataVertical.marginStart();
            Vector2 marginEnd = dataVertical.marginEnd();
            offset.setX(0.0);
            offset.plusY(heightAdd);
            heightAdd = 0.0;
            pos = offset.now().plus(marginStart);
            offset.plusY(component.height + marginStart.doubleY() +
                    marginEnd.doubleY());
            setSize(pos.plus(new Vector2d(component.width, component.height))
                    .plus(marginEnd));
        }
        return pos;
    }

    private void setSize(Vector2 size) {
        this.size.setX(size.doubleX());
        this.size.setY(size.doubleY());
    }

    public Vector2 size() {
        return size.now().minus(start);
    }
}
