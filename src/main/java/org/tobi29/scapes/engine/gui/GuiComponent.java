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
import org.tobi29.scapes.engine.Container;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.Triple;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class GuiComponent implements Comparable<GuiComponent> {
    /*private static final VAO FRAME = VAOUtility.createVI(
            new float[]{0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f}, new int[]{0, 1, 1, 2, 2, 3, 3, 0},
            RenderType.LINES);*/
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
    protected final AtomicBoolean dirty = new AtomicBoolean(true);
    private final long uid = UID_COUNTER.getAndIncrement();
    protected boolean visible = true, hover, hovering, removing;
    private Vector2 lastSize = Vector2d.ZERO;

    protected GuiComponent(GuiLayoutData parent) {
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

    public <T extends GuiComponent> T addSub(double x, double y, double width,
            double height, Function<GuiLayoutDataAbsolute, T> child) {
        return addSub(x, y, width, height, 0, child);
    }

    public <T extends GuiComponent> T addSub(double x, double y, double width,
            double height, long priority,
            Function<GuiLayoutDataAbsolute, T> child) {
        return addSub(new Vector2d(x, y), new Vector2d(width, height), priority,
                child);
    }

    public <T extends GuiComponent> T addSub(Vector2 pos, Vector2 size,
            long priority, Function<GuiLayoutDataAbsolute, T> child) {
        GuiLayoutDataAbsolute layoutData =
                new GuiLayoutDataAbsolute(this, pos, size, priority, true);
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

    protected boolean checkInside(double x, double y, Vector2 size) {
        return x >= 0 && y >= 0 && x < size.doubleX() && y < size.doubleY();
    }

    public Gui gui() {
        return gui;
    }

    public boolean ignoresEvents() {
        return false;
    }

    protected void render(GL gl, Shader shader, Vector2 size) {
        if (visible) {
            MatrixStack matrixStack = gl.matrixStack();
            Matrix matrix = matrixStack.push();
            transform(matrix, size);
            if (dirty.getAndSet(false) || !lastSize.equals(size)) {
                updateMesh(size);
                lastSize = size;
            }
            renderComponent(gl, shader, size.doubleX(), size.doubleY());
            /*{
                gl.textures().unbind(gl);
                if (ignoresEvents()) {
                    gl.setAttribute4f(OpenGL.COLOR_ATTRIBUTE, 0.0f, 0.0f, 1.0f,
                            1.0f);
                } else {
                    gl.setAttribute4f(OpenGL.COLOR_ATTRIBUTE, 1.0f, 0.0f, 0.0f,
                            1.0f);
                }
                matrix = matrixStack.push();
                matrix.scale(size.floatX(), size.floatY(), 1.0f);
                FRAME.render(gl, shader);
                matrixStack.pop();
            }*/
            MatrixStack matrixStack1 = gl.matrixStack();
            GuiLayoutManager layout = layoutManager(size);
            Streams.of(layout.layout()).forEach(component -> {
                if (component.b.doubleX() >= -component.c.doubleX() &&
                        component.b.doubleY() >= -component.c.doubleY() &&
                        component.b.doubleX() <= size.doubleX() &&
                        component.b.doubleY() <= size.doubleY()) {
                    Matrix childMatrix = matrixStack1.push();
                    childMatrix.translate(component.b.floatX(),
                            component.b.floatY(), 0.0f);
                    component.a.render(gl, shader, component.c);
                    matrixStack1.pop();
                }
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

    protected void renderComponent(GL gl, Shader shader, double width,
            double height) {
    }

    protected void renderOverlay(GL gl, Shader shader) {
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    protected void update(ScapesEngine engine, double delta, Vector2 size) {
        if (visible) {
            updateComponent(engine, delta, size);
            if (hovering && !hover) {
                hovering = false;
                for (BiConsumer<GuiComponentHoverEvent, ScapesEngine> listener : hovers) {
                    GuiComponentHoverEvent event =
                            new GuiComponentHoverEvent(Double.NaN, Double.NaN,
                                    GuiComponentHoverEvent.State.LEAVE, size);
                    listener.accept(event, engine);
                }
            }
            if (hover) {
                hover = false;
            }
            updateChildren(engine, delta, size);
        }
    }

    protected void updateMesh(Vector2 size) {
    }

    protected Optional<GuiComponent> fireEvent(GuiComponentEvent event,
            EventSink listener, ScapesEngine engine) {
        if (visible) {
            boolean inside = checkInside(event.x(), event.y(), event.size());
            if (inside) {
                GuiLayoutManager layout = layoutManager(event.size());
                for (Triple<GuiComponent, Vector2, Vector2> component : layout
                        .layout()) {
                    if (!component.a.parent.blocksEvents()) {
                        Optional<GuiComponent> sink = component.a.fireEvent(
                                new GuiComponentEvent(event,
                                        component.b.doubleX(),
                                        component.b.doubleY(), component.c),
                                listener, engine);
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
            boolean inside = checkInside(event.x(), event.y(), event.size());
            if (inside) {
                GuiLayoutManager layout = layoutManager(event.size());
                Set<GuiComponent> sinks = new HashSet<>();
                Streams.of(layout.layout())
                        .filter(component -> !component.a.parent.blocksEvents())
                        .forEach(component -> sinks.addAll(component.a
                                .fireRecursiveEvent(new GuiComponentEvent(event,
                                                component.b.doubleX(),
                                                component.b.doubleY(), component.c),
                                        listener, engine)));
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
            GuiLayoutManager layout = layoutManager(event.size());
            for (Triple<GuiComponent, Vector2, Vector2> component : layout
                    .layout()) {
                if (!component.a.parent.blocksEvents()) {
                    boolean success = component.a.sendEvent(
                            new GuiComponentEvent(event, component.b.doubleX(),
                                    component.b.doubleY(), component.c),
                            destination, listener, engine);
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

    protected void updateComponent(ScapesEngine engine, double delta,
            Vector2 size) {
    }

    protected void transform(Matrix matrix, Vector2 size) {
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
        if (parent.priority() > o.parent.priority()) {
            return -1;
        }
        if (parent.priority() < o.parent.priority()) {
            return 1;
        }
        if (uid > o.uid) {
            return 1;
        }
        if (uid < o.uid) {
            return -1;
        }
        return 0;
    }

    public void remove() {
        removing = true;
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

    protected void updateChildren(ScapesEngine engine, double delta,
            Vector2 size) {
        while (!changeComponents.isEmpty()) {
            changeComponents.poll().run();
        }
        GuiLayoutManager layout = layoutManager(size);
        Streams.of(layout.layout()).forEach(component -> {
            if (component.a.removing) {
                drop(component.a);
            } else {
                component.a.update(engine, delta, component.c);
            }
        });
    }

    protected GuiLayoutManager layoutManager(Vector2 size) {
        return new GuiLayoutManagerAbsolute(Vector2d.ZERO, size, components);
    }

    protected void append(GuiComponent component) {
        components.add(component);
    }

    protected void drop(GuiComponent component) {
        components.remove(component);
        component.removed();
    }

    protected Vector2 baseSize(ScapesEngine engine) {
        return baseSize(engine.container());
    }

    protected Vector2 baseSize(Container container) {
        return new Vector2d((double) container.contentWidth() /
                container.containerHeight() * 540.0, 540.0);
    }

    protected Vector2 baseSize(GL gl) {
        return new Vector2d(
                (double) gl.contentWidth() / gl.containerHeight() * 540.0,
                540.0);
    }

    public interface EventSink {
        boolean accept(GuiComponent component, GuiComponentEvent event,
                ScapesEngine engine);
    }

    public interface EventDestination {
        void accept(GuiComponentEvent event, ScapesEngine engine);
    }
}
