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

import java8.util.function.Function;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;


public class GuiComponentPane extends GuiComponent {
    public GuiComponentPane(GuiLayoutData parent, int width, int height) {
        super(parent, width, height);
    }

    public <T extends GuiComponent> T add(double x, double y,
            Function<GuiLayoutDataAbsolute, T> child) {
        return add(new Vector2d(x, y), child);
    }

    public <T extends GuiComponent> T add(Vector2 pos,
            Function<GuiLayoutDataAbsolute, T> child) {
        GuiLayoutDataAbsolute layoutData = new GuiLayoutDataAbsolute(this, pos);
        T component = child.apply(layoutData);
        append(component);
        return component;
    }

    public <T extends GuiComponent> T addHori(double marginX, double marginY,
            Function<GuiLayoutDataHorizontal, T> child) {
        return addHori(marginX, marginY, marginX, marginY, child);
    }

    public <T extends GuiComponent> T addHori(double marginStartX,
            double marginStartY, double marginEndX, double marginEndY,
            Function<GuiLayoutDataHorizontal, T> child) {
        return addHori(new Vector2d(marginStartX, marginStartY),
                new Vector2d(marginEndX, marginEndY), child);
    }

    public <T extends GuiComponent> T addHori(Vector2 marginStart,
            Vector2 marginEnd, Function<GuiLayoutDataHorizontal, T> child) {
        GuiLayoutDataHorizontal layoutData =
                new GuiLayoutDataHorizontal(this, marginStart, marginEnd);
        T component = child.apply(layoutData);
        append(component);
        return component;
    }

    public <T extends GuiComponent> T addVert(double marginX, double marginY,
            Function<GuiLayoutDataVertical, T> child) {
        return addVert(marginX, marginY, marginX, marginY, child);
    }

    public <T extends GuiComponent> T addVert(double marginStartX,
            double marginStartY, double marginEndX, double marginEndY,
            Function<GuiLayoutDataVertical, T> child) {
        return addVert(new Vector2d(marginStartX, marginStartY),
                new Vector2d(marginEndX, marginEndY), child);
    }

    public <T extends GuiComponent> T addVert(Vector2 marginStart,
            Vector2 marginEnd, Function<GuiLayoutDataVertical, T> child) {
        GuiLayoutDataVertical layoutData =
                new GuiLayoutDataVertical(this, marginStart, marginEnd);
        T component = child.apply(layoutData);
        append(component);
        return component;
    }
}
