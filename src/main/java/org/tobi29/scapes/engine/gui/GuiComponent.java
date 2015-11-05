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
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public abstract class GuiComponent implements Comparable<GuiComponent> {
    private static final AtomicLong UID_COUNTER =
            new AtomicLong(Long.MIN_VALUE);
    protected final Set<GuiComponentEventListener> events =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<GuiComponentHoverListener> hovers =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<GuiComponentEventListener> rightEvents =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final GuiLayoutData parent;
    protected final Gui gui;
    protected final Queue<Runnable> changeComponents =
            new ConcurrentLinkedQueue<>();
    protected final Set<GuiComponent> components =
            new ConcurrentSkipListSet<>();
    private final long uid = UID_COUNTER.getAndIncrement();
    protected int width, height;
    protected boolean visible = true, hovering;

    protected GuiComponent(GuiLayoutData parent, int width, int height) {
        this.width = width;
        this.height = height;
        this.parent = parent;
        GuiComponent other = this;
        while (true) {
            if (other instanceof Gui) {
                gui = (Gui) other;
                break;
            }
            assert other.parent.parent().isPresent();
            other = other.parent.parent().get();
        }
    }

    public <T extends GuiComponent> T addSub(double x, double y,
            Function<GuiLayoutDataAbsolute, T> child) {
        return addSub(new Vector2d(x, y), child);
    }

    public <T extends GuiComponent> T addSub(Vector2 pos,
            Function<GuiLayoutDataAbsolute, T> child) {
        GuiLayoutDataAbsolute layoutData = new GuiLayoutDataAbsolute(this, pos);
        T component = child.apply(layoutData);
        append(component);
        return component;
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
            renderChildren(gl, shader, delta);
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
            updateChildren(mouseX, mouseY, inside, engine);
        }
    }

    public void updateComponent() {
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

    public void remove(GuiComponent remove) {
        changeComponents.add(() -> drop(remove));
    }

    public void removeAll() {
        components.forEach(this::remove);
    }

    public void removed() {
        components.forEach(GuiComponent::removed);
    }

    public void renderChildren(GL gl, Shader shader, double delta) {
        MatrixStack matrixStack = gl.matrixStack();
        GuiLayoutManager layout = new GuiLayoutManager();
        components.forEach(component -> {
            Vector2 pos = layout.layout(component);
            Matrix childMatrix = matrixStack.push();
            childMatrix.translate(pos.floatX(), pos.floatY(), 0.0f);
            component.render(gl, shader, delta);
            matrixStack.pop();
        });
    }

    public void updateChildren(double mouseX, double mouseY, boolean inside,
            ScapesEngine engine) {
        while (!changeComponents.isEmpty()) {
            changeComponents.poll().run();
        }
        GuiLayoutManager layout = new GuiLayoutManager();
        components.forEach(component -> {
            Vector2 pos = layout.layout(component);
            double mouseXX = mouseX - pos.doubleX();
            double mouseYY = mouseY - pos.doubleY();
            updateChild(component, mouseXX, mouseYY, inside, engine);
        });
    }

    protected void resetHover(ScapesEngine engine) {
        hovering = false;
        setHover(false, engine);
        for (GuiComponent component : components) {
            component.resetHover(engine);
        }
    }

    protected void updateChild(GuiComponent component, double mouseX,
            double mouseY, boolean inside, ScapesEngine engine) {
        component.update(mouseX, mouseY, inside, engine);
    }

    protected void append(GuiComponent component) {
        components.add(component);
    }

    protected void drop(GuiComponent component) {
        components.remove(component);
        component.removed();
    }
}
