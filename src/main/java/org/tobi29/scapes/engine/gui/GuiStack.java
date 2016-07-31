package org.tobi29.scapes.engine.gui;

import java8.util.Optional;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.graphics.GL;
import org.tobi29.scapes.engine.graphics.Shader;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class GuiStack {
    private final Map<String, Gui> guis = new ConcurrentSkipListMap<>();
    private final Map<Gui, String> keys = new HashMap<>();
    private Optional<Gui> focus = Optional.empty();

    public synchronized void add(String id, Gui add) {
        Gui previous = guis.put(id, add);
        if (previous != null) {
            removed(previous);
        }
        keys.put(add, id);
        focus = Optional.of(add);
    }

    public synchronized void addUnfocused(String id, Gui add) {
        Gui previous = guis.put(id, add);
        if (previous != null) {
            removed(previous);
        }
        keys.put(add, id);
    }

    public Optional<Gui> get(String id) {
        return Optional.ofNullable(guis.get(id));
    }

    public boolean has(String id) {
        return guis.containsKey(id);
    }

    public synchronized Optional<Gui> remove(String id) {
        Gui previous = guis.remove(id);
        if (previous == null) {
            return Optional.empty();
        }
        removed(previous);
        return Optional.of(previous);
    }

    public synchronized boolean remove(Gui previous) {
        if (!guis.values().remove(previous)) {
            return false;
        }
        removed(previous);
        return true;
    }

    public synchronized boolean swap(Gui remove, Gui add) {
        String id = keys.get(remove);
        if (id == null) {
            return false;
        }
        guis.put(id, add);
        keys.put(add, id);
        if (removed(remove)) {
            focus = Optional.of(add);
        }
        return true;
    }

    private boolean removed(Gui gui) {
        guis.values().remove(gui);
        keys.remove(gui);
        if (focus.isPresent() && focus.get() == gui) {
            focus = Optional.empty();
            return true;
        }
        return false;
    }

    public void step(ScapesEngine engine, double delta) {
        Streams.forEach(guis.values(), gui -> {
            if (gui.valid()) {
                gui.update(engine, delta);
            } else {
                remove(gui);
            }
        });
    }

    public Optional<GuiComponent> fireEvent(GuiEvent type,
            GuiComponentEvent event, ScapesEngine engine) {
        return fireEvent(event, GuiComponent.sink(type), engine);
    }

    public Optional<GuiComponent> fireEvent(GuiComponentEvent event,
            GuiComponent.EventSink listener, ScapesEngine engine) {
        List<Gui> guis = new ArrayList<>(this.guis.size());
        guis.addAll(this.guis.values());
        for (int i = guis.size() - 1; i >= 0; i--) {
            Optional<GuiComponent> sink =
                    guis.get(i).fireNewEvent(event, listener, engine);
            if (sink.isPresent()) {
                return sink;
            }
        }
        return Optional.empty();
    }

    public Set<GuiComponent> fireRecursiveEvent(GuiEvent type,
            GuiComponentEvent event, ScapesEngine engine) {
        return fireRecursiveEvent(event, GuiComponent.sink(type), engine);
    }

    public Set<GuiComponent> fireRecursiveEvent(GuiComponentEvent event,
            GuiComponent.EventSink listener, ScapesEngine engine) {
        List<Gui> guis = new ArrayList<>(this.guis.size());
        guis.addAll(this.guis.values());
        for (int i = guis.size() - 1; i >= 0; i--) {
            Set<GuiComponent> sink =
                    guis.get(i).fireNewRecursiveEvent(event, listener, engine);
            if (!sink.isEmpty()) {
                return sink;
            }
        }
        return Collections.emptySet();
    }

    public boolean fireAction(GuiAction action, ScapesEngine engine) {
        Optional<Gui> focus = this.focus;
        return focus.isPresent() && focus.get().fireAction(action, engine);
    }

    public void render(GL gl, Shader shader, double delta) {
        Vector2 pixelSize = new Vector2d(540.0 / gl.contentWidth(),
                540.0 / gl.contentHeight());
        Streams.forEach(guis.values(), gui -> gui
                .render(gl, shader, gui.baseSize(gl), pixelSize, delta));
        Streams.forEach(guis.values(),
                gui -> gui.renderOverlays(gl, shader, pixelSize));
    }
}
