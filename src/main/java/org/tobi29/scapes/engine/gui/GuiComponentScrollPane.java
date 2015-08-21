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

public class GuiComponentScrollPane extends GuiComponentVisiblePane {
    protected final GuiComponentScrollPaneViewport viewport;

    public GuiComponentScrollPane(GuiComponent parent, int x, int y, int width,
            int height, int scrollStep) {
        super(parent, x, y, width, height);
        GuiComponentSliderVert slider =
                new GuiComponentSliderVert(this, width - 10, 0, 10, height, 0);
        viewport = newViewport(slider, scrollStep);
    }

    protected GuiComponentScrollPaneViewport newViewport(
            GuiComponentSliderVert slider, int scrollStep) {
        return new GuiComponentScrollPaneViewport(this, slider, 0, 0, width,
                height, scrollStep);
    }

    public GuiComponentScrollPaneViewport viewport() {
        return viewport;
    }
}
