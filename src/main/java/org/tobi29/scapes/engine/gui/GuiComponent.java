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
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3d;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
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
    protected final Set<GuiComponent> components =
            new ConcurrentSkipListSet<>();
    private final long uid = UID_COUNTER.getAndIncrement();
    private final AtomicBoolean hasActiveChild = new AtomicBoolean(true);
    protected boolean visible = true, hover, hovering, removing;

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
                    GuiComponentHoverEvent.State.HOVER);
        } else {
            hoverEvent = new GuiComponentHoverEvent(event,
                    GuiComponentHoverEvent.State.ENTER);
            hovering = true;
        }
        hover = true;
        for (BiConsumer<GuiComponentHoverEvent, ScapesEngine> listener : hovers) {
            listener.accept(hoverEvent, engine);
            success = true;
        }
        parent.parent().ifPresent(GuiComponent::activeUpdate);
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

    protected void render(GL gl, Shader shader, Vector2 size, Vector2 pixelSize,
            double delta) {
        if (visible) {
            MatrixStack matrixStack = gl.matrixStack();
            Matrix matrix = matrixStack.push();
            transform(matrix, size);
            GuiLayoutManager layout = layoutManager(size);
            for (Triple<GuiComponent, Vector2, Vector2> component : layout
                    .layout()) {
                Vector3 pos = applyTransform(-component.b.doubleX(),
                        -component.b.doubleY(), size);
                if (-pos.doubleX() >= -component.c.doubleX() &&
                        -pos.doubleY() >= -component.c.doubleY() &&
                        -pos.doubleX() <= size.doubleX() &&
                        -pos.doubleY() <= size.doubleY()) {
                    Matrix childMatrix = matrixStack.push();
                    childMatrix.translate(component.b.floatX(),
                            component.b.floatY(), 0.0f);
                    component.a
                            .render(gl, shader, component.c, pixelSize, delta);
                    matrixStack.pop();
                }
            }
            matrixStack.pop();
        }
    }

    protected void renderOverlays(GL gl, Shader shader) {
        if (visible) {
            Streams.forEach(components,
                    component -> component.renderOverlays(gl, shader));
        }
    }

    protected boolean renderLightweight(GuiRenderer renderer, Vector2 size) {
        return render(renderer, size);
    }

    protected boolean render(GuiRenderer renderer, Vector2 size) {
        boolean hasHeavy = false;
        if (visible) {
            MatrixStack matrixStack = renderer.matrixStack();
            Matrix matrix = matrixStack.push();
            renderer.offset(0x10000);
            transform(matrix, size);
            updateMesh(renderer, size);
            GuiLayoutManager layout = layoutManager(size);
            for (Triple<GuiComponent, Vector2, Vector2> component : layout
                    .layout()) {
                Matrix childMatrix = matrixStack.push();
                childMatrix
                        .translate(component.b.floatX(), component.b.floatY(),
                                0.0f);
                hasHeavy |=
                        component.a.renderLightweight(renderer, component.c);
                matrixStack.pop();
            }
            matrixStack.pop();
            renderer.offset(-0x10000);
        }
        return hasHeavy;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    protected void update(ScapesEngine engine, double delta) {
        if (visible) {
            if (hovering && !hover) {
                hovering = false;
                for (BiConsumer<GuiComponentHoverEvent, ScapesEngine> listener : hovers) {
                    size(engine).ifPresent(size -> {
                        GuiComponentHoverEvent event =
                                new GuiComponentHoverEvent(Double.NaN,
                                        Double.NaN,
                                        GuiComponentHoverEvent.State.LEAVE,
                                        size);
                        listener.accept(event, engine);
                    });
                }
            }
            if (hover) {
                parent.parent().ifPresent(GuiComponent::activeUpdate);
                hover = false;
            }
            if (hasActiveChild.getAndSet(false)) {
                Streams.forEach(components, component -> {
                    if (component.removing) {
                        remove(component);
                    } else {
                        component.update(engine, delta);
                    }
                });
            }
        }
    }

    protected void updateMesh(GuiRenderer renderer, Vector2 size) {
    }

    public void dirty() {
        parent.parent().ifPresent(GuiComponent::dirty);
    }

    protected void activeUpdate() {
        hasActiveChild.set(true);
        parent.parent().ifPresent(GuiComponent::activeUpdate);
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
                        Optional<GuiComponent> sink = component.a
                                .fireEvent(applyTransform(event, component),
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

    @SuppressWarnings("Convert2streamapi")
    protected Set<GuiComponent> fireRecursiveEvent(GuiComponentEvent event,
            EventSink listener, ScapesEngine engine) {
        if (visible) {
            boolean inside = checkInside(event.x(), event.y(), event.size());
            if (inside) {
                Set<GuiComponent> sinks = new HashSet<>();
                GuiLayoutManager layout = layoutManager(event.size());
                for (Triple<GuiComponent, Vector2, Vector2> component : layout
                        .layout()) {
                    if (!component.a.parent.blocksEvents()) {
                        sinks.addAll(component.a.fireRecursiveEvent(
                                applyTransform(event, component), listener,
                                engine));
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
            GuiLayoutManager layout = layoutManager(event.size());
            for (Triple<GuiComponent, Vector2, Vector2> component : layout
                    .layout()) {
                if (!component.a.parent.blocksEvents()) {
                    boolean success = component.a
                            .sendEvent(applyTransform(event, component),
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

    protected Optional<Vector2> calculateSize(Vector2 size,
            GuiComponent destination) {
        if (visible) {
            GuiLayoutManager layout = layoutManager(size);
            for (Triple<GuiComponent, Vector2, Vector2> component : layout
                    .layout()) {
                Optional<Vector2> success =
                        component.a.calculateSize(component.c, destination);
                if (success.isPresent()) {
                    return success;
                }
            }
            if (destination == this) {
                return Optional.of(size);
            }
        }
        return Optional.empty();
    }

    public Optional<Vector2> size(ScapesEngine engine) {
        return gui.calculateSize(gui.baseSize(engine), this);
    }

    protected GuiComponentEvent applyTransform(GuiComponentEvent event,
            Triple<GuiComponent, Vector2, Vector2> component) {
        Vector3 pos = applyTransform(event.x() - component.b.doubleX(),
                event.y() - component.b.doubleY(), component.c);
        return new GuiComponentEvent(event, pos.doubleX(), pos.doubleY(),
                component.c);
    }

    protected Vector3 applyTransform(double x, double y, Vector2 size) {
        return applyTransform(new Vector3d(x, y, 0.0), size);
    }

    protected Vector3 applyTransform(Vector3 pos, Vector2 size) {
        Matrix matrix = new Matrix();
        matrix.identity();
        transform(matrix, size);
        return matrix.modelView().multiply(pos.multiply(-1.0)).multiply(-1.0);
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

    public void remove(GuiComponent component) {
        components.remove(component);
        dirty();
    }

    public void removeAll() {
        Streams.forEach(components, this::remove);
    }

    protected GuiLayoutManager layoutManager(Vector2 size) {
        if (components.isEmpty()) {
            return GuiLayoutManagerEmpty.INSTANCE;
        }
        return newLayoutManager(size);
    }

    protected GuiLayoutManager newLayoutManager(Vector2 size) {
        return new GuiLayoutManagerAbsolute(Vector2d.ZERO, size, components);
    }

    protected void append(GuiComponent component) {
        components.add(component);
        activeUpdate();
        dirty();
    }

    protected Vector2 baseSize(ScapesEngine engine) {
        return baseSize(engine.container());
    }

    protected Vector2 baseSize(Container container) {
        return new Vector2d((double) container.containerWidth() /
                container.containerHeight() * 540.0, 540.0);
    }

    protected Vector2 baseSize(GL gl) {
        return new Vector2d(
                (double) gl.containerWidth() / gl.containerHeight() * 540.0,
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
