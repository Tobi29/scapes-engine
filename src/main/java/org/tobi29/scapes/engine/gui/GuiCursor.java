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

import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public class GuiCursor {
    private final MutableVector2 pos = new MutableVector2d(), guiPos =
            new MutableVector2d();

    public void set(Vector2 pos, Vector2 guiPos) {
        this.pos.set(pos);
        this.guiPos.set(guiPos);
    }

    public Vector2 pos() {
        return pos.now();
    }

    public double x() {
        return pos.doubleX();
    }

    public double y() {
        return pos.doubleY();
    }

    public Vector2 guiPos() {
        return guiPos.now();
    }

    public double guiX() {
        return guiPos.doubleX();
    }

    public double guiY() {
        return guiPos.doubleY();
    }
}
