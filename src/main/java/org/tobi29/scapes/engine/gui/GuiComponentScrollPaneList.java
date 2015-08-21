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

public class GuiComponentScrollPaneList extends GuiComponentScrollPane {
    public GuiComponentScrollPaneList(GuiComponent parent, int x, int y,
            int width, int height, int scrollStep) {
        super(parent, x, y, width, height, scrollStep);
    }

    @Override
    protected GuiComponentScrollPaneViewport newViewport(
            GuiComponentSliderVert slider, int scrollStep) {
        return new GuiComponentScrollPaneViewport(this, slider, 0, 0, width,
                height, scrollStep) {
            @Override
            protected void append(GuiComponent component) {
                components.add(component);
                layout();
            }

            @Override
            protected void drop(GuiComponent component) {
                components.remove(component);
                component.removed();
                layout();
            }
        };
    }

    protected void layout() {
        int i = 0;
        for (GuiComponent component : viewport.components) {
            component.setY(i);
            i += viewport.scrollStep;
        }
        viewport.setMaxY(i - viewport.scrollStep);
    }
}
