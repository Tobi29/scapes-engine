package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3f;

public class GuiRenderer extends GuiRenderBatch {
    private final MatrixStack matrixStack = new MatrixStack(64);
    private Vector2 pixelSize;

    public MatrixStack matrixStack() {
        return matrixStack;
    }

    public void setPixelSize(Vector2 pixelSize) {
        this.pixelSize = pixelSize;
    }

    public Vector2 pixelSize() {
        return pixelSize;
    }

    @Override
    public Vector3 vector(float x, float y) {
        Matrix matrix = matrixStack.current();
        return matrix.modelView().multiply(new Vector3f(x, y, 0.0f));
    }
}
