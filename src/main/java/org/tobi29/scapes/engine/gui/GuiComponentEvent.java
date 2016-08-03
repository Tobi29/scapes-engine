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

import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

public class GuiComponentEvent {
    private final double x, y, relativeX, relativeY;
    private final boolean screen;
    private final Vector2 size;

    public GuiComponentEvent() {
        this(Double.NaN, Double.NaN);
    }

    public GuiComponentEvent(double x, double y) {
        this(x, y, Double.NaN, Double.NaN);
    }

    public GuiComponentEvent(double x, double y, double relativeX,
            double relativeY) {
        this(x, y, relativeX, relativeY, true);
    }

    public GuiComponentEvent(double x, double y, double relativeX,
            double relativeY, boolean screen) {
        this(x, y, relativeX, relativeY, screen,
                new Vector2d(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    public GuiComponentEvent(double x, double y, Vector2 size) {
        this(x, y, Double.NaN, Double.NaN, size);
    }

    public GuiComponentEvent(double x, double y, double relativeX,
            double relativeY, Vector2 size) {
        this(x, y, relativeX, relativeY, true, size);
    }

    public GuiComponentEvent(double x, double y, double relativeX,
            double relativeY, boolean screen, Vector2 size) {
        this.x = x;
        this.y = y;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.screen = screen;
        this.size = size;
    }

    public GuiComponentEvent(GuiComponentEvent parent, Vector2 size) {
        this(parent.x, parent.y, parent.relativeX, parent.relativeY,
                parent.screen, size);
    }

    public GuiComponentEvent(GuiComponentEvent parent, double x, double y,
            Vector2 size) {
        this(x, y, parent.relativeX, parent.relativeY, parent.screen, size);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double relativeX() {
        return relativeX;
    }

    public double relativeY() {
        return relativeY;
    }

    public boolean screen() {
        return screen;
    }

    public Vector2 size() {
        return size;
    }
}
