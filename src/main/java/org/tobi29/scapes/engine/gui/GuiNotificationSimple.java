package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.graphics.GL;
import org.tobi29.scapes.engine.graphics.Matrix;
import org.tobi29.scapes.engine.graphics.Texture;
import org.tobi29.scapes.engine.graphics.Shader;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3d;

public class GuiNotificationSimple extends GuiComponentVisibleSlabHeavy {
    private final double speed;
    private double progress;

    public GuiNotificationSimple(GuiLayoutData parent, Texture icon,
            String text) {
        this(parent, icon, text, 3.0);
    }

    public GuiNotificationSimple(GuiLayoutData parent, Texture icon,
            String text, double time) {
        super(parent);
        addHori(10, 10, 40, 40, p -> new GuiComponentIcon(p, icon));
        addHori(10, 23, -1, -1, p -> new GuiComponentText(p, text));
        speed = 1.0 / time;
    }

    @Override
    public void renderComponent(GL gl, Shader shader, Vector2 size,
            double delta) {
        progress += speed * delta;
        if (progress > 1.1) {
            progress = 1.1;
            remove();
        }
    }

    @Override
    protected void transform(Matrix matrix, Vector2 size) {
        float sin = (float) FastMath.sin(progress * FastMath.PI) - 1.0f;
        float sqr = sin * sin;
        sqr *= sqr;
        Vector3 start = matrix.modelView().multiply(Vector3d.ZERO);
        matrix.translate(0.0f, sqr * sin * (start.floatY() + size.floatY()),
                0.0f);
    }
}
