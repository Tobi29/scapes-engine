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

import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.Mesh;
import org.tobi29.scapes.engine.opengl.OpenGL;
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.utils.math.FastMath;

public class GuiNotification extends Gui {
    private final VAO vao;
    private final double speed;
    private final float x, y;
    private double progress;

    public GuiNotification(int x, int y, int width, int height, GuiStyle style,
            GuiAlignment alignment, double time) {
        super(width, height, style, alignment);
        this.x = x;
        this.y = y;
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, width, height, 0.2f);
        mesh.addVertex(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 0.0f);
        mesh.addVertex(0.0f, height, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 0.0f);
        mesh.addVertex(width, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 0.0f);
        mesh.addVertex(width, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 0.0f);
        mesh.addVertex(0.0f, height, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 0.0f);
        mesh.addVertex(width, height, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 0.0f);
        vao = mesh.finish();
        speed = 1.0 / time;
    }

    @Override
    public void renderComponent(GL gl, Shader shader, double delta) {
        progress += speed * delta;
        gl.textures().unbind(gl);
        gl.setAttribute4f(OpenGL.COLOR_ATTRIBUTE, 1.0f, 1.0f, 1.0f, 1.0f);
        vao.render(gl, shader);
        progress = FastMath.min(progress, 1.1);
    }

    @Override
    protected void transform(Matrix matrix) {
        float sin = (float) FastMath.sin(progress * FastMath.PI) - 1.0f;
        float sqr = sin * sin;
        sqr *= sqr;
        matrix.translate(x, y + sqr * sin * height, 0.0f);
    }

    @Override
    public boolean valid() {
        return progress < 1.05;
    }
}
