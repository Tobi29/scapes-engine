package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.utils.Triple;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class GuiLayoutManager {
    protected final List<GuiComponent> components;
    protected final Vector2 start, maxSize;
    protected Vector2 size;

    protected GuiLayoutManager(Vector2 start, Vector2 maxSize,
            Set<GuiComponent> components) {
        this.start = start;
        this.maxSize = maxSize;
        this.components = new ArrayList<>(components.size());
        this.components.addAll(components);
    }

    public List<Triple<GuiComponent, Vector2, Vector2>> layout() {
        List<Triple<GuiComponent, Vector2, Vector2>> output =
                new ArrayList<>(components.size());
        layout(output);
        return output;
    }

    protected abstract void layout(
            List<Triple<GuiComponent, Vector2, Vector2>> output);

    protected void size(MutableVector2 size, Vector2 preferredSize,
            Vector2 maxSize) {
        if (size.doubleX() < 0.0) {
            size.setX(preferredSize.doubleX() * -size.doubleX());
        }
        size.setX(FastMath.min(size.doubleX(), maxSize.doubleX()));
        if (size.doubleY() < 0.0) {
            size.setY(preferredSize.doubleY() * -size.doubleY());
        }
        size.setY(FastMath.min(size.doubleY(), maxSize.doubleY()));
    }

    protected void setSize(Vector2 size, MutableVector2 outSize) {
        outSize.set(FastMath.max(size, outSize.now()));
    }

    public Vector2 size() {
        return size.minus(start);
    }
}
