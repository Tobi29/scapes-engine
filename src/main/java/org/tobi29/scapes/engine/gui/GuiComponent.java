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

import java8.util.Optional;
import java8.util.function.BiConsumer;
import java8.util.function.Consumer;
import java8.util.function.Function;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

public abstract class GuiComponent implements Comparable<GuiComponent> {
    private static final AtomicLong UID_COUNTER =
            new AtomicLong(Long.MIN_VALUE);
    protected final Set<BiConsumer<GuiComponentEvent, ScapesEngine>> clickLeft =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<BiConsumer<GuiComponentEvent, ScapesEngine>> pressLeft =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<BiConsumer<GuiComponentEvent, ScapesEngine>> dragLeft =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<BiConsumer<GuiComponentEvent, ScapesEngine>> dropLeft =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<BiConsumer<GuiComponentEvent, ScapesEngine>>
            clickRight = Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<BiConsumer<GuiComponentEvent, ScapesEngine>>
            pressRight = Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<BiConsumer<GuiComponentEvent, ScapesEngine>> dragRight =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<BiConsumer<GuiComponentEvent, ScapesEngine>> dropRight =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<BiConsumer<GuiComponentEvent, ScapesEngine>> scroll =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<BiConsumer<GuiComponentHoverEvent, ScapesEngine>>
            hovers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final GuiLayoutData parent;
    protected final Gui gui;
    protected final Queue<Runnable> changeComponents =
            new ConcurrentLinkedQueue<>();
    protected final Set<GuiComponent> components =
            new ConcurrentSkipListSet<>();
    private final long uid = UID_COUNTER.getAndIncrement();
    protected int width, height;
    protected boolean visible = true, hover, hovering;

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
        GuiLayoutDataAbsolute layoutData =
                new GuiLayoutDataAbsolute(this, pos, true);
        T component = child.apply(layoutData);
        append(component);
        return component;
    }

    public void onClickLeft(Consumer<GuiComponentEvent> add) {
        onClickLeft((event, engine) -> add.accept(event));
    }

    public void onClickLeft(BiConsumer<GuiComponentEvent, ScapesEngine> add) {
        clickLeft.add(add);
    }

    public void onPressLeft(Consumer<GuiComponentEvent> add) {
        onPressLeft((event, engine) -> add.accept(event));
    }

    public void onPressLeft(BiConsumer<GuiComponentEvent, ScapesEngine> add) {
        pressLeft.add(add);
    }

    public void onDragLeft(Consumer<GuiComponentEvent> add) {
        onDragLeft((event, engine) -> add.accept(event));
    }

    public void onDragLeft(BiConsumer<GuiComponentEvent, ScapesEngine> add) {
        dragLeft.add(add);
    }

    public void onDropLeft(Consumer<GuiComponentEvent> add) {
        onDropLeft((event, engine) -> add.accept(event));
    }

    public void onDropLeft(BiConsumer<GuiComponentEvent, ScapesEngine> add) {
        dropLeft.add(add);
    }

    public void onClickRight(Consumer<GuiComponentEvent> add) {
        onClickRight((event, engine) -> add.accept(event));
    }

    public void onClickRight(BiConsumer<GuiComponentEvent, ScapesEngine> add) {
        clickRight.add(add);
    }

    public void onPressRight(Consumer<GuiComponentEvent> add) {
        onPressRight((event, engine) -> add.accept(event));
    }

    public void onPressRight(BiConsumer<GuiComponentEvent, ScapesEngine> add) {
        pressRight.add(add);
    }

    public void onDragRight(Consumer<GuiComponentEvent> add) {
        onDragRight((event, engine) -> add.accept(event));
    }

    public void onDragRight(BiConsumer<GuiComponentEvent, ScapesEngine> add) {
        dragRight.add(add);
    }

    public void onDropRight(Consumer<GuiComponentEvent> add) {
        onDropRight((event, engine) -> add.accept(event));
    }

    public void onDropRight(BiConsumer<GuiComponentEvent, ScapesEngine> add) {
        dropRight.add(add);
    }

    public void onClick(Consumer<GuiComponentEvent> add) {
        onClick((event, engine) -> add.accept(event));
    }

    public void onClick(BiConsumer<GuiComponentEvent, ScapesEngine> add) {
        clickLeft.add(add);
        clickRight.add(add);
    }

    public void onScroll(Consumer<GuiComponentEvent> add) {
        onScroll((event, engine) -> add.accept(event));
    }

    public void onScroll(BiConsumer<GuiComponentEvent, ScapesEngine> add) {
        scroll.add(add);
    }

    public void onHover(Consumer<GuiComponentHoverEvent> add) {
        onHover((event, engine) -> add.accept(event));
    }

    public void onHover(BiConsumer<GuiComponentHoverEvent, ScapesEngine> add) {
        hovers.add(add);
    }

    public boolean clickLeft(GuiComponentEvent event, ScapesEngine engine) {
        boolean success = false;
        for (BiConsumer<GuiComponentEvent, ScapesEngine> listener : clickLeft) {
            listener.accept(event, engine);
            success = true;
        }
        gui.setLastClicked(this);
        return success;
    }

    public boolean pressLeft(GuiComponentEvent event, ScapesEngine engine) {
        boolean success = false;
        for (BiConsumer<GuiComponentEvent, ScapesEngine> listener : pressLeft) {
            listener.accept(event, engine);
            success = true;
        }
        gui.setLastClicked(this);
        return success;
    }

    public boolean dragLeft(GuiComponentEvent event, ScapesEngine engine) {
        boolean success = false;
        for (BiConsumer<GuiComponentEvent, ScapesEngine> listener : dragLeft) {
            listener.accept(event, engine);
            success = true;
        }
        return success;
    }

    public boolean dropLeft(GuiComponentEvent event, ScapesEngine engine) {
        boolean success = false;
        for (BiConsumer<GuiComponentEvent, ScapesEngine> listener : dropLeft) {
            listener.accept(event, engine);
            success = true;
        }
        return success;
    }

    public boolean clickRight(GuiComponentEvent event, ScapesEngine engine) {
        boolean success = false;
        for (BiConsumer<GuiComponentEvent, ScapesEngine> listener : clickRight) {
            listener.accept(event, engine);
            success = true;
        }
        gui.setLastClicked(this);
        return success;
    }

    public boolean pressRight(GuiComponentEvent event, ScapesEngine engine) {
        boolean success = false;
        for (BiConsumer<GuiComponentEvent, ScapesEngine> listener : pressRight) {
            listener.accept(event, engine);
            success = true;
        }
        gui.setLastClicked(this);
        return success;
    }

    public boolean dragRight(GuiComponentEvent event, ScapesEngine engine) {
        boolean success = false;
        for (BiConsumer<GuiComponentEvent, ScapesEngine> listener : dragRight) {
            listener.accept(event, engine);
            success = true;
        }
        return success;
    }

    public boolean dropRight(GuiComponentEvent event, ScapesEngine engine) {
        boolean success = false;
        for (BiConsumer<GuiComponentEvent, ScapesEngine> listener : dropRight) {
            listener.accept(event, engine);
            success = true;
        }
        return success;
    }

    public boolean scroll(GuiComponentEvent event, ScapesEngine engine) {
        boolean success = false;
        for (BiConsumer<GuiComponentEvent, ScapesEngine> listener : scroll) {
            listener.accept(event, engine);
            success = true;
        }
        return success;
    }

    public boolean hover(GuiComponentEvent event, ScapesEngine engine) {
        boolean success = false;
        GuiComponentHoverEvent hoverEvent;
        if (hovering) {
            hoverEvent = new GuiComponentHoverEvent(event,
                    GuiComponentHoverEvent.State.ENTER);
        } else {
            hoverEvent = new GuiComponentHoverEvent(event,
                    GuiComponentHoverEvent.State.HOVER);
            hovering = true;
        }
        hover = true;
        for (BiConsumer<GuiComponentHoverEvent, ScapesEngine> listener : hovers) {
            listener.accept(hoverEvent, engine);
            success = true;
        }
        return success;
    }

    protected boolean checkInside(double x, double y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public Gui gui() {
        return gui;
    }

    public boolean ignoresEvents() {
        return false;
    }

    protected void render(GL gl, Shader shader, double delta) {
        if (visible) {
            MatrixStack matrixStack = gl.matrixStack();
            Matrix matrix = matrixStack.push();
            transform(matrix);
            renderComponent(gl, shader, delta);
            MatrixStack matrixStack1 = gl.matrixStack();
            GuiLayoutManager layout = layoutManager();
            Streams.of(components).forEach(component -> {
                Vector2 pos = layout.layout(component);
                Matrix childMatrix = matrixStack1.push();
                childMatrix.translate(pos.floatX(), pos.floatY(), 0.0f);
                component.render(gl, shader, delta);
                matrixStack1.pop();
            });
            matrixStack.pop();
        }
    }

    protected void renderOverlays(GL gl, Shader shader) {
        if (visible) {
            Streams.of(components)
                    .forEach(component -> component.renderOverlays(gl, shader));
            renderOverlay(gl, shader);
        }
    }

    protected void renderComponent(GL gl, Shader shader, double delta) {
    }

    protected void renderOverlay(GL gl, Shader shader) {
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    protected void update(ScapesEngine engine) {
        if (visible) {
            updateComponent(engine);
            if (hovering && !hover) {
                hovering = false;
                for (BiConsumer<GuiComponentHoverEvent, ScapesEngine> listener : hovers) {
                    GuiComponentHoverEvent event =
                            new GuiComponentHoverEvent(Double.NaN, Double.NaN,
                                    GuiComponentHoverEvent.State.LEAVE);
                    listener.accept(event, engine);
                }
            }
            if (hover) {
                hover = false;
            }
            updateChildren(engine);
        }
    }

    protected Optional<GuiComponent> fireEvent(GuiComponentEvent event,
            EventSink listener, ScapesEngine engine) {
        if (visible) {
            boolean inside = checkInside(event.x(), event.y());
            if (inside) {
                GuiLayoutManager layout = layoutManager();
                for (GuiComponent component : components) {
                    Vector2 pos = layout.layout(component);
                    if (!component.parent.blocksEvents()) {
                        Optional<GuiComponent> sink = component.fireEvent(
                                new GuiComponentEvent(event, pos.doubleX(),
                                        pos.doubleY()), listener, engine);
                        if (sink.isPresent()) {
                            return sink;
                        }
                    }
                }
                if (!ignoresEvents()) {
                    listener.accept(this, event, engine);
                    return Optional.of(this);
                }
            }
        }
        return Optional.empty();
    }

    protected Set<GuiComponent> fireRecursiveEvent(GuiComponentEvent event,
            EventSink listener, ScapesEngine engine) {
        if (visible) {
            boolean inside = checkInside(event.x(), event.y());
            if (inside) {
                GuiLayoutManager layout = layoutManager();
                Set<GuiComponent> sinks = new HashSet<>();
                for (GuiComponent component : components) {
                    Vector2 pos = layout.layout(component);
                    if (!component.parent.blocksEvents()) {
                        sinks.addAll(component.fireRecursiveEvent(
                                new GuiComponentEvent(event, pos.doubleX(),
                                        pos.doubleY()), listener, engine));
                    }
                }
                if (!ignoresEvents()) {
                    if (listener.accept(this, event, engine)) {
                        sinks.add(this);
                    }
                }
                return sinks;
            }
        }
        return Collections.emptySet();
    }

    protected boolean sendEvent(GuiComponentEvent event,
            GuiComponent destination, EventDestination listener,
            ScapesEngine engine) {
        if (visible) {
            GuiLayoutManager layout = layoutManager();
            for (GuiComponent component : components) {
                Vector2 pos = layout.layout(component);
                if (!component.parent.blocksEvents()) {
                    boolean success = component.sendEvent(
                            new GuiComponentEvent(event, pos.doubleX(),
                                    pos.doubleY()), destination, listener,
                            engine);
                    if (success) {
                        return true;
                    }
                }
            }
            if (destination == this) {
                listener.accept(event, engine);
                return true;
            }
        }
        return false;
    }

    protected void updateComponent(ScapesEngine engine) {
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
        Streams.of(components).forEach(this::remove);
    }

    public void removed() {
        Streams.of(components).forEach(GuiComponent::removed);
    }

    protected void updateChildren(ScapesEngine engine) {
        while (!changeComponents.isEmpty()) {
            changeComponents.poll().run();
        }
        Streams.of(components).forEach(component -> component.update(engine));
    }

    protected GuiLayoutManager layoutManager() {
        return new GuiLayoutManager(Vector2d.ZERO);
    }

    protected void append(GuiComponent component) {
        components.add(component);
    }

    protected void drop(GuiComponent component) {
        components.remove(component);
        component.removed();
    }

    public interface EventSink {
        boolean accept(GuiComponent component, GuiComponentEvent event,
                ScapesEngine engine);
    }

    public interface EventDestination {
        void accept(GuiComponentEvent event, ScapesEngine engine);
    }
}
