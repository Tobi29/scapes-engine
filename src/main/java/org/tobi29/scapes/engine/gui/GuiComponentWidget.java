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

import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

public class GuiComponentWidget extends GuiComponentVisiblePane {
    private final GuiComponentWidgetTitle titleBar;

    public GuiComponentWidget(GuiLayoutData parent, int width, int height,
            String name) {
        super(parent, width, height);
        titleBar = add(0, -16,
                p -> new GuiComponentWidgetTitle(p, width, 16, 12, name));
        if (!(parent instanceof GuiLayoutDataAbsolute)) {
            return;
        }
        MutableVector2 pos = ((GuiLayoutDataAbsolute) parent).posMutable();
        titleBar.onDragLeft(event -> {
            pos.plusX(event.relativeX()).plusY(event.relativeY());
        });
    }

    @Override
    protected GuiLayoutManager layoutManager() {
        return new GuiLayoutManager(new Vector2d(0.0, 16.0));
    }
}
