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
import java.util.concurrent.ConcurrentSkipListMap;

public class GuiStack {
    protected final VAO cursor;
    private final Map<String, Gui> guis = new ConcurrentSkipListMap<>();

    public GuiStack(ScapesEngine engine) {
        cursor = VAOUtility.createVCTI(engine,
                new float[]{-16.0f, -16.0f, 0.0f, 16.0f, -16.0f, 0.0f, -16.0f,
                        16.0f, 0.0f, 16.0f, 16.0f, 0.0f},
                new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f},
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f},
                new int[]{0, 1, 2, 1, 2, 3}, RenderType.TRIANGLES);
    }

    public void add(String id, Gui add) {
        guis.put(id, add);
    }

    public void remove(Gui remove) {
        guis.values().remove(remove);
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

    public void render(GL gl, Shader shader, ScapesEngine engine,
            double delta) {
        Streams.forEach(guis.values(),
                gui -> gui.render(gl, shader, gui.baseSize(gl), delta));
        Streams.forEach(guis.values(), gui -> gui.renderOverlays(gl, shader));
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
                        this.cursor.render(gl, shader);
                        matrixStack.pop();
                    });
        }
    }
}
