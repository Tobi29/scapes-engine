package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.ThreadLocalUtil;
import org.tobi29.scapes.engine.utils.Triple;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class GuiComponentHeavy extends GuiComponent {
    private static final ThreadLocal<GuiRenderer> RENDERER =
            ThreadLocalUtil.of(GuiRenderer::new);
    private final AtomicBoolean dirty = new AtomicBoolean(true);
    protected List<Pair<VAO, Texture>> meshes = Collections.emptyList();
    private Vector2 lastSize = Vector2d.ZERO;
    private boolean hasHeavyChild;

    protected GuiComponentHeavy(GuiLayoutData parent) {
        super(parent);
    }

    @Override
    protected void render(GL gl, Shader shader, Vector2 size, double delta) {
        if (visible) {
            MatrixStack matrixStack = gl.matrixStack();
            Matrix matrix = matrixStack.push();
            transform(matrix, size);
            if (dirty.getAndSet(false) || !lastSize.equals(size)) {
                GuiRenderer renderer = RENDERER.get();
                hasHeavyChild = render(renderer, size);
                meshes = renderer.finish();
                lastSize = size;
            }
            Streams.forEach(meshes, mesh -> {
                mesh.b.bind(gl);
                mesh.a.render(gl, shader);
            });
            renderComponent(gl, shader, size, delta);
            if (hasHeavyChild) {
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
                        component.a.render(gl, shader, component.c, delta);
                        matrixStack.pop();
                    }
                }
            }
            matrixStack.pop();
        }
    }

    @Override
    protected void renderOverlays(GL gl, Shader shader) {
        super.renderOverlays(gl, shader);
        if (visible) {
            renderOverlay(gl, shader);
        }
    }

    @Override
    protected boolean renderLightweight(GuiRenderer renderer, Vector2 size) {
        return true;
    }

    @Override
    protected boolean render(GuiRenderer renderer, Vector2 size) {
        boolean hasHeavy = false;
        MatrixStack matrixStack = renderer.matrixStack();
        updateMesh(renderer, size);
        GuiLayoutManager layout = layoutManager(size);
        for (Triple<GuiComponent, Vector2, Vector2> component : layout
                .layout()) {
            Matrix childMatrix = matrixStack.push();
            childMatrix.translate(component.b.floatX(), component.b.floatY(),
                    0.0f);
            hasHeavy |= component.a.renderLightweight(renderer, component.c);
            matrixStack.pop();
        }
        return hasHeavy;
    }

    @Override
    protected void update(ScapesEngine engine, double delta) {
        super.update(engine, delta);
        if (visible) {
            updateComponent(engine, delta);
        }
        parent.parent().ifPresent(GuiComponent::activeUpdate);
    }

    @Override
    public void dirty() {
        dirty.set(true);
    }

    protected void updateComponent(ScapesEngine engine, double delta) {
    }

    protected void renderComponent(GL gl, Shader shader, Vector2 size,
            double delta) {
    }

    protected void renderOverlay(GL gl, Shader shader) {
    }
}
