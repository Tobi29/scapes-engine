package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3f;

public class GuiRenderer extends GuiRenderBatch {
    private final MatrixStack matrixStack =
            new MatrixStack(64, BufferCreator::bytes);

    public MatrixStack matrixStack() {
        return matrixStack;
    }

    @Override
    public Vector3 vector(float x, float y) {
        Matrix matrix = matrixStack.current();
        return matrix.modelView().multiply(new Vector3f(x, y, 0.0f));
    }
}
