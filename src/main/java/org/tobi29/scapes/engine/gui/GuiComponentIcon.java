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
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public class GuiComponentIcon extends GuiComponent {
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
        dirty();
    }

    public void unsetIcon() {
        texture = Optional.empty();
        dirty();
    }

    public void setColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        dirty();
    }

    @Override
    protected void updateMesh(GuiRenderer renderer, Vector2 size) {
        gui.style().border(renderer, size);
        Optional<Texture> texture = this.texture;
        if (texture.isPresent()) {
            renderer.texture(texture.get());
            GuiUtils.rectangle(renderer, 0.0f, 0.0f, size.floatX(),
                    size.floatY(), r, g, b, a);
        }
    }
}
