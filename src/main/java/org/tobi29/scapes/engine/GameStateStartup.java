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
package org.tobi29.scapes.engine;

import org.tobi29.scapes.engine.graphics.Texture;
import org.tobi29.scapes.engine.gui.GuiComponentImage;
import org.tobi29.scapes.engine.gui.GuiState;
import org.tobi29.scapes.engine.gui.GuiStyle;
import org.tobi29.scapes.engine.graphics.Scene;
import org.tobi29.scapes.engine.utils.math.FastMath;

public class GameStateStartup extends GameState {
    private final GameState nextState;
    private final String image;
    private final double scale;
    private GuiComponentImage icon;
    private double time;
    private int warmUp;

    public GameStateStartup(GameState nextState, String image, double scale,
            Scene scene, ScapesEngine engine) {
        super(engine, scene);
        this.image = image;
        this.scale = scale;
        this.nextState = nextState;
        this.scene = scene;
    }

    @Override
    public void init() {
        engine.guiStack().addUnfocused("20-Image",
                new GuiImage(engine.graphics().textures().get(image),
                        engine.guiStyle()));
    }

    @Override
    public boolean isMouseGrabbed() {
        return true;
    }

    @Override
    public void step(double delta) {
        if (icon != null) {
            if (warmUp > 20) {
                time += delta / 5.0;
                float a = (float) FastMath.sinTable(time * FastMath.PI);
                a = FastMath.min(a * 1.4f, 1.0f);
                icon.setColor(1.0f, 1.0f, 1.0f, a);
                if (time > 1.0) {
                    engine.setState(nextState);
                }
            } else {
                icon.setColor(1.0f, 1.0f, 1.0f, 0.0f);
                warmUp++;
            }
        }
    }

    private class GuiImage extends GuiState {
        public GuiImage(Texture texture, GuiStyle style) {
            super(GameStateStartup.this, style);
            int width = texture.width();
            int height = texture.height();
            double ratio = (double) width / height;
            int w = (int) (540 * ratio * scale);
            int h = (int) (540 * scale);
            spacer();
            icon = addHori((960 - w) / 2, (540 - h) / 2, w, h,
                    p -> new GuiComponentImage(p, texture));
            spacer();
        }
    }
}
