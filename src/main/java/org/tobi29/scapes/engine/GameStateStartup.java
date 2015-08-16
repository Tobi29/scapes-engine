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

import org.tobi29.scapes.engine.gui.GuiComponentIcon;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.scenes.SceneImage;
import org.tobi29.scapes.engine.utils.math.FastMath;

public class GameStateStartup extends GameState {
    private final GameState nextState;
    private final SceneImage scene;
    private double time;
    private int warmUp;

    public GameStateStartup(GameState nextState, SceneImage scene,
            ScapesEngine engine) {
        super(engine, scene);
        this.nextState = nextState;
        this.scene = scene;
    }

    @Override
    public void dispose(GL gl) {
    }

    @Override
    public void init(GL gl) {
    }

    @Override
    public boolean isMouseGrabbed() {
        return true;
    }

    @Override
    public boolean isThreaded() {
        return false;
    }

    @Override
    public void stepComponent(double delta) {
        GuiComponentIcon image = scene.image();
        if (image != null) {
            if (warmUp > 20) {
                time += delta / 5.0;
                float a = (float) FastMath.sinTable(time * FastMath.PI);
                a = FastMath.min(a * 1.4f, 1.0f);
                image.setColor(1.0f, 1.0f, 1.0f, a);
                if (time > 1.0) {
                    engine.setState(nextState);
                }
            } else {
                image.setColor(1.0f, 1.0f, 1.0f, 0.0f);
                warmUp++;
            }
        }
    }
}
