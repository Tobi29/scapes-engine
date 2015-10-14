/*
 * Copyright 2012-2015 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class GuiComponent implements Comparable<GuiComponent> {
    private static final AtomicLong UID_COUNTER =
            new AtomicLong(Long.MIN_VALUE);
    protected final Queue<Runnable> changeComponents =
            new ConcurrentLinkedQueue<>();
    protected final Map<GuiComponent, MutableVector2> components =
            new ConcurrentSkipListMap<>();
    protected final Set<GuiComponentEventListener> events =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<GuiComponentHoverListener> hovers =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<GuiComponentEventListener> rightEvents =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Optional<GuiComponent> parent;
    protected final Gui gui;
    protected final int width, height;
    private final long uid = UID_COUNTER.getAndIncrement();
    protected boolean visible = true, hovering;
    protected long lastClick;

    protected GuiComponent(int width, int height) {
        this(Optional.empty(), width, height);
    }

    protected GuiComponent(GuiComponent parent, int x, int y, int width,
            int height) {
        this(Optional.of(parent), width, height);
        parent.changeComponents.add(() -> parent.append(this, x, y));
    }

    protected GuiComponent(Optional<GuiComponent> parent, int width,
            int height) {
        this.width = width;
        this.height = height;
        this.parent = parent;
        lastClick = System.currentTimeMillis();
        GuiComponent other = this;
        while (true) {
            if (other instanceof Gui) {
                gui = (Gui) other;
                break;
            }
            assert other.parent.isPresent();
            other = other.parent.get();
        }
    }

    public void remove(GuiComponent remove) {
        changeComponents.add(() -> drop(remove));
    }

    public void removeAll() {
        components.keySet().forEach(this::remove);
    }

    public void addLeftClick(GuiComponentEventListener add) {
        events.add(add);
    }

    public void addRightClick(GuiComponentEventListener add) {
        rightEvents.add(add);
    }

    public void addHover(GuiComponentHoverListener add) {
        hovers.add(add);
    }

    public void removeLeftClick(GuiComponentEventListener remove) {
        events.remove(remove);
    }

    public void removeRightClock(GuiComponentEventListener remove) {
        rightEvents.remove(remove);
    }

    public void removeHover(GuiComponentHoverListener remove) {
        hovers.remove(remove);
    }

    public void clickLeft(GuiComponentEvent event, ScapesEngine engine) {
        for (GuiComponentEventListener event1 : events) {
            event1.click(event);
        }
        gui.setLastClicked(this);
    }

    public void clickRight(GuiComponentEvent event, ScapesEngine engine) {
        for (GuiComponentEventListener rightEvent : rightEvents) {
            rightEvent.click(event);
        }
        gui.setLastClicked(this);
    }

    public void hover(GuiComponentHoverEvent event) {
        for (GuiComponentHoverListener hover : hovers) {
            hover.hover(event);
        }
    }

    public void removed() {
        components.keySet().forEach(GuiComponent::removed);
    }

    public boolean checkInside(double x, double y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public Gui gui() {
        return gui;
    }

    public void render(GL gl, Shader shader, double delta) {
        if (visible) {
            MatrixStack matrixStack = gl.matrixStack();
            Matrix matrix = matrixStack.push();
            transform(matrix);
            renderComponent(gl, shader, delta);
            components.forEach((component, pos) -> {
                Matrix childMatrix = matrixStack.push();
                childMatrix.translate(pos.floatX(), pos.floatY(), 0.0f);
                component.render(gl, shader, delta);
                matrixStack.pop();
            });
            renderOverlay(gl, shader);
            matrixStack.pop();
        }
    }

    public void renderComponent(GL gl, Shader shader, double delta) {
    }

    public void renderOverlay(GL gl, Shader shader) {
    }

    public void setHover(boolean hover, ScapesEngine engine) {
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void update(ScapesEngine engine) {
        GuiController guiController = engine.guiController();
        update(guiController.guiCursorX(), guiController.guiCursorY(), true,
                engine);
    }

    public void update(double mouseX, double mouseY, boolean mouseInside,
            ScapesEngine engine) {
        if (visible) {
            updateComponent();
            boolean inside = mouseInside && checkInside(mouseX, mouseY);
            if (inside) {
                GuiController guiController = engine.guiController();
                if (guiController.leftClick()) {
                    clickLeft(new GuiComponentEvent(mouseX, mouseY), engine);
                }
                if (guiController.rightClick()) {
                    clickRight(new GuiComponentEvent(mouseX, mouseY), engine);
                }
                setHover(true, engine);
                if (hovering) {
                    hover(new GuiComponentHoverEvent(mouseX, mouseY,
                            GuiComponentHoverEvent.State.HOVER));
                } else {
                    hover(new GuiComponentHoverEvent(mouseX, mouseY,
                            GuiComponentHoverEvent.State.ENTER));
                    hovering = true;
                }
            } else {
                resetHover(engine);
            }
            while (!changeComponents.isEmpty()) {
                changeComponents.poll().run();
            }
            components.forEach((component, pos) -> {
                double mouseXX = mouseX - pos.doubleX();
                double mouseYY = mouseY - pos.doubleY();
                updateChild(component, mouseXX, mouseYY, inside, engine);
            });
        }
    }

    protected void resetHover(ScapesEngine engine) {
        hovering = false;
        setHover(false, engine);
        for (GuiComponent component : components.keySet()) {
            component.resetHover(engine);
        }
    }

    public void updateComponent() {
    }

    protected void updateChild(GuiComponent component, double mouseX,
            double mouseY, boolean inside, ScapesEngine engine) {
        component.update(mouseX, mouseY, inside, engine);
    }

    protected void append(GuiComponent component, double x, double y) {
        components.put(component, new MutableVector2d(x, y));
    }

    protected void drop(GuiComponent component) {
        components.remove(component);
        component.removed();
    }

    protected void transform(Matrix matrix) {
    }

    @Override
    public int hashCode() {
        return (int) uid;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GuiComponent && uid == ((GuiComponent) obj).uid;
    }

    @Override
    public int compareTo(GuiComponent o) {
        long id = uid - o.uid;
        if (id > 0) {
            return 1;
        } else if (id < 0) {
            return -1;
        }
        return 0;
    }
}
