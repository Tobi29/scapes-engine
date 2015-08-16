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

import org.tobi29.scapes.engine.opengl.*;
import org.tobi29.scapes.engine.opengl.shader.Shader;

public class GuiComponentSeparator extends GuiComponent {
    private final VAO vao;

    public GuiComponentSeparator(GuiComponent parent, int x, int y, int width,
            int height) {
        super(parent, x, y, width, height);
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, width, height, 0.1f);
        float halfHeight = height * 0.5f;
        mesh.color(0, 0, 0, 0.3f);
        mesh.vertex(0.0f, 0.0f, 0.0f);
        mesh.vertex(width, halfHeight, 0.0f);
        mesh.vertex(width, 0.0f, 0.0f);
        mesh.vertex(0.0f, halfHeight, 0.0f);
        mesh.vertex(width, halfHeight, 0.0f);
        mesh.vertex(0.0f, 0.0f, 0.0f);
        mesh.color(0.2f, 0.2f, 0.2f, 0.3f);
        mesh.vertex(0.0f, halfHeight, 0.0f);
        mesh.vertex(width, height, 0.0f);
        mesh.vertex(width, halfHeight, 0.0f);
        mesh.vertex(0.0f, height, 0.0f);
        mesh.vertex(width, height, 0.0f);
        mesh.vertex(0.0f, halfHeight, 0.0f);
        vao = mesh.finish();
    }

    @Override
    public void renderComponent(GL gl, Shader shader, FontRenderer font,
            double delta) {
        gl.textures().unbind(gl);
        gl.setAttribute4f(OpenGL.COLOR_ATTRIBUTE, 1.0f, 1.0f, 1.0f, 1.0f);
        vao.render(gl, shader);
    }
}
