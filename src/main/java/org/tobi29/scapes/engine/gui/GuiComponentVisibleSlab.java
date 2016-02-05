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
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public class GuiComponentVisibleSlab extends GuiComponentSlab {
    private Pair<VAO, Texture> vao;

    public GuiComponentVisibleSlab(GuiLayoutData parent) {
        super(parent);
    }

    @Override
    protected void renderComponent(GL gl, Shader shader, double width, double height) {
        vao.b.bind(gl);
        vao.a.render(gl, shader);
    }

    @Override
    protected void updateMesh(Vector2 size) {
        vao = gui.style().pane(size);
    }
}
