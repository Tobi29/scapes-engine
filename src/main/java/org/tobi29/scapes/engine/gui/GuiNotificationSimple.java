/*
 * Copyright 2012-2016 Tobi29
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
            Vector2 pixelSize, double delta) {
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
