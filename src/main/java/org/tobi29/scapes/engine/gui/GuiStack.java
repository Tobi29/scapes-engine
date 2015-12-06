package org.tobi29.scapes.engine.gui;

import java8.util.Optional;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.RenderType;
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.VAOUtility;
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.utils.Streams;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;

public class GuiStack {
    protected static final VAO CURSOR;

    static {
        CURSOR = VAOUtility.createVCTI(
                new float[]{-16.0f, -16.0f, 0.0f, 16.0f, -16.0f, 0.0f, -16.0f,
                        16.0f, 0.0f, 16.0f, 16.0f, 0.0f},
                new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f},
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f},
                new int[]{0, 1, 2, 1, 2, 3}, RenderType.TRIANGLES);
    }

    protected final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final Map<String, Gui> guis = new ConcurrentSkipListMap<>();

    public void add(String id, Gui add) {
        Gui old = guis.put(id, add);
        if (old != null) {
            queue.add(old::removed);
        }
    }

    public void remove(Gui remove) {
        guis.values().remove(remove);
        queue.add(remove::removed);
    }

    public void step(ScapesEngine engine) {
        Streams.of(guis.values()).forEach(gui -> {
            if (gui.valid()) {
                gui.update(engine);
            } else {
                remove(gui);
            }
        });
        while (!queue.isEmpty()) {
            queue.poll().run();
        }
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

    public void render(GL gl, Shader shader, double delta,
            ScapesEngine engine) {
        Streams.of(guis.values())
                .forEach(gui -> gui.renderGUI(gl, shader, delta));
        Streams.of(guis.values())
                .forEach(gui -> gui.renderOverlays(gl, shader));
        MatrixStack matrixStack = gl.matrixStack();
        GuiController guiController = engine.guiController();
        if (!engine.state().isMouseGrabbed()) {
            gl.setProjectionOrthogonal(0.0f, 0.0f, gl.contentWidth(),
                    gl.containerHeight());
            gl.textures().bind("Engine:image/Cursor", gl);
            guiController.cursors().filter(GuiCursor::software)
                    .forEach(cursor -> {
                        Matrix matrix = matrixStack.push();
                        matrix.translate((float) cursor.x(), (float) cursor.y(),
                                0.0f);
                        matrix.scale(10, 10, 10);
                        CURSOR.render(gl, shader);
                        matrixStack.pop();
                    });
        }
    }
}
