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
package org.tobi29.scapes.engine.opengl.scenes;

import org.tobi29.scapes.engine.gui.Gui;
import org.tobi29.scapes.engine.gui.GuiAlignment;
import org.tobi29.scapes.engine.gui.GuiComponentIcon;
import org.tobi29.scapes.engine.gui.GuiStyle;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.texture.Texture;

public class SceneImage extends Scene {
    private final Texture image;
    private final double scale;
    private GuiComponentIcon icon;

    public SceneImage(Texture image, double scale) {
        this.image = image;
        this.scale = scale;
    }

    @Override
    public void init(GL gl) {
        addGui(new GuiImage(image, state.engine().globalGUI().style()));
    }

    @Override
    public void renderScene(GL gl) {
    }

    @Override
    public void dispose(GL gl) {
    }

    public GuiComponentIcon image() {
        return icon;
    }

    private class GuiImage extends Gui {
        public GuiImage(Texture texture, GuiStyle style) {
            super(style, GuiAlignment.CENTER);
            int width = texture.width();
            int height = texture.height();
            double ratio = (double) width / height;
            int w = (int) (512 * ratio * scale);
            int h = (int) (512 * scale);
            icon = add((800 - w) / 2, (512 - h) / 2,
                    p -> new GuiComponentIcon(p, w, h, texture));
        }
    }
}
