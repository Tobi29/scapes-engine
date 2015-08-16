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
import org.tobi29.scapes.engine.opengl.FontRenderer;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.utils.Pair;

import java.util.Collections;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

public abstract class GuiComponent implements Comparable<GuiComponent> {
    private static final AtomicLong UID_COUNTER =
            new AtomicLong(Long.MIN_VALUE);
    protected final Queue<Pair<Boolean, GuiComponent>> changeComponents =
            new ConcurrentLinkedQueue<>();
    protected final Set<GuiComponent> components =
            new ConcurrentSkipListSet<>();
    protected final Set<GuiComponentEventListener> events =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<GuiComponentHoverListener> hovers =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<GuiComponentEventListener> rightEvents =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Optional<GuiComponent> parent;
    private final long uid = UID_COUNTER.getAndIncrement();
    protected boolean visible = true, hovering;
    protected int x, y, width, height;

    protected GuiComponent(int x, int y, int width, int height) {
        this(Optional.empty(), x, y, width, height);
    }

    protected GuiComponent(GuiComponent parent, int x, int y, int width,
            int height) {
        this(Optional.of(parent), x, y, width, height);
        parent.changeComponents.add(new Pair<>(true, this));
    }

    protected GuiComponent(Optional<GuiComponent> parent, int x, int y,
            int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.parent = parent;
    }

    public void remove(GuiComponent remove) {
        changeComponents.add(new Pair<>(false, remove));
    }

    public void removeAll() {
        components.stream().map(component -> new Pair<>(false, component))
                .forEach(changeComponents::add);
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
        gui().ifPresent(gui -> gui.setLastClicked(this));
    }

    public void clickRight(GuiComponentEvent event, ScapesEngine engine) {
        for (GuiComponentEventListener rightEvent : rightEvents) {
            rightEvent.click(event);
        }
        gui().ifPresent(gui -> gui.setLastClicked(this));
    }

    public void hover(GuiComponentHoverEvent event) {
        for (GuiComponentHoverListener hover : hovers) {
            hover.hover(event);
        }
    }

    public void removed() {
        components.forEach(GuiComponent::removed);
    }

    public boolean checkInside(double x, double y) {
        return x >= this.x && y >= this.y && x < this.x + width &&
                y < this.y + height;
    }

    public int x() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int y() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Optional<Gui> gui() {
        GuiComponent other = this;
        while (true) {
            if (other.parent.isPresent()) {
                other = other.parent.get();
                continue;
            }
            if (other instanceof Gui) {
                return Optional.of((Gui) other);
            }
            return Optional.empty();
        }
    }

    public void render(GL gl, Shader shader, FontRenderer font, double delta) {
        if (visible) {
            MatrixStack matrixStack = gl.matrixStack();
            Matrix matrix = matrixStack.push();
            transform(matrix);
            renderComponent(gl, shader, font, delta);
            components.stream().forEach(
                    component -> component.render(gl, shader, font, delta));
            renderOverlay(gl, shader, font);
            matrixStack.pop();
        }
    }

    public void renderComponent(GL gl, Shader shader, FontRenderer font,
            double delta) {
    }

    public void renderOverlay(GL gl, Shader shader, FontRenderer font) {
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
                Pair<Boolean, GuiComponent> component = changeComponents.poll();
                if (component.a) {
                    components.add(component.b);
                } else {
                    components.remove(component.b);
                    component.b.removed();
                }
            }
            double mouseXX = mouseX - x;
            double mouseYY = mouseY - y;
            components.forEach(
                    component -> updateChild(component, mouseXX, mouseYY,
                            inside, engine));
        }
    }

    protected void resetHover(ScapesEngine engine) {
        hovering = false;
        setHover(false, engine);
        for (GuiComponent component : components) {
            component.resetHover(engine);
        }
    }

    public void updateComponent() {
    }

    protected void updateChild(GuiComponent component, double mouseX,
            double mouseY, boolean inside, ScapesEngine engine) {
        component.update(mouseX, mouseY, inside, engine);
    }

    protected void transform(Matrix matrix) {
        matrix.translate(x, y, 0.0f);
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
