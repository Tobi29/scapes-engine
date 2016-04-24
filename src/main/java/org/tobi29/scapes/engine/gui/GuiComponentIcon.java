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

import java8.util.Optional;
import org.tobi29.scapes.engine.opengl.*;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public class GuiComponentIcon extends GuiComponent {
    private VAO vao;
    private Pair<VAO, Texture> vaoBorder;
    private float r = 1.0f, g = 1.0f, b = 1.0f, a = 1.0f;
    private Optional<Texture> texture;

    public GuiComponentIcon(GuiLayoutData parent) {
        this(parent, Optional.empty());
    }

    public GuiComponentIcon(GuiLayoutData parent, Texture texture) {
        this(parent, Optional.of(texture));
    }

    public GuiComponentIcon(GuiLayoutData parent, Optional<Texture> texture) {
        super(parent);
        this.texture = texture;
    }

    public void setIcon(Texture texture) {
        this.texture = Optional.of(texture);
    }

    public void unsetIcon() {
        texture = Optional.empty();
    }

    public void setColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public void renderComponent(GL gl, Shader shader, double width,
            double height) {
        Optional<Texture> texture = this.texture;
        if (texture.isPresent()) {
            texture.get().bind(gl);
            gl.setAttribute4f(OpenGL.COLOR_ATTRIBUTE, r, g, b, a);
            vao.render(gl, shader);
            vaoBorder.b.bind(gl);
            vaoBorder.a.render(gl, shader);
        }
    }

    @Override
    protected void updateMesh(Vector2 size) {
        vao = VAOUtility.createVTI(gui.style().engine(),
                new float[]{0.0f, size.floatY(), 0.0f, size.floatX(),
                        size.floatY(), 0.0f, 0.0f, 0.0f, 0.0f, size.floatX(),
                        0.0f, 0.0f},
                new float[]{0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f},
                new int[]{0, 1, 2, 3, 2, 1}, RenderType.TRIANGLES);
        vaoBorder = gui.style().border(size);
    }
}
