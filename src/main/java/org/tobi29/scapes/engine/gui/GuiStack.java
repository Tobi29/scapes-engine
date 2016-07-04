package org.tobi29.scapes.engine.gui;

import java8.util.Optional;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.opengl.vao.RenderType;
import org.tobi29.scapes.engine.opengl.vao.VAO;
import org.tobi29.scapes.engine.opengl.vao.VAOUtility;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class GuiStack {
    protected final VAO cursor;
    private final Map<String, Gui> guis = new ConcurrentSkipListMap<>();
    private final Map<Gui, String> keys = new HashMap<>();
    private Optional<Gui> focus = Optional.empty();

    public GuiStack(ScapesEngine engine) {
        cursor = VAOUtility.createVCTI(engine,
                new float[]{-16.0f, -16.0f, 0.0f, 16.0f, -16.0f, 0.0f, -16.0f,
                        16.0f, 0.0f, 16.0f, 16.0f, 0.0f},
                new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f},
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f},
                new int[]{0, 1, 2, 1, 2, 3}, RenderType.TRIANGLES);
    }

    public synchronized void add(String id, Gui add) {
        guis.put(id, add);
        keys.put(add, id);
        focus = Optional.of(add);
    }

    public synchronized void addUnfocused(String id, Gui add) {
        guis.put(id, add);
        keys.put(add, id);
    }

    public Optional<Gui> get(String id) {
        return Optional.ofNullable(guis.get(id));
    }

    public boolean has(String id) {
        return guis.containsKey(id);
    }

    public synchronized Optional<Gui> remove(String id) {
        Gui gui = guis.remove(id);
        if (gui == null) {
            return Optional.empty();
        }
        keys.remove(gui);
        if (focus.isPresent() && focus.get() == gui) {
            focus = Optional.empty();
        }
        return Optional.of(gui);
    }

    public synchronized boolean remove(Gui remove) {
        if (!guis.values().remove(remove)) {
            return false;
        }
        keys.remove(remove);
        if (focus.isPresent() && focus.get() == remove) {
            focus = Optional.empty();
        }
        return true;
    }

    public synchronized boolean swap(Gui remove, Gui add) {
        String id = keys.get(remove);
        if (id == null) {
            return false;
        }
        guis.values().remove(remove);
        keys.remove(remove);
        guis.put(id, add);
        keys.put(add, id);
        if (focus.isPresent() && focus.get() == remove) {
            focus = Optional.of(add);
        }
        return true;
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

    public void render(GL gl, Shader shader, ScapesEngine engine,
            double delta) {
        Vector2 pixelSize = new Vector2d(540.0 / gl.contentWidth(),
                540.0 / gl.contentHeight());
        Streams.forEach(guis.values(), gui -> gui
                .render(gl, shader, gui.baseSize(gl), pixelSize, delta));
        Streams.forEach(guis.values(), gui -> gui.renderOverlays(gl, shader));
        MatrixStack matrixStack = gl.matrixStack();
        GuiController guiController = engine.guiController();
        if (!engine.state().isMouseGrabbed()) {
            gl.setProjectionOrthogonal(0.0f, 0.0f, gl.containerWidth(),
                    gl.containerHeight());
            gl.textures().bind("Engine:image/Cursor", gl);
            guiController.cursors().filter(GuiCursor::software)
                    .forEach(cursor -> {
                        Matrix matrix = matrixStack.push();
                        matrix.translate((float) cursor.x(), (float) cursor.y(),
                                0.0f);
                        this.cursor.render(gl, shader);
                        matrixStack.pop();
                    });
        }
    }
}
