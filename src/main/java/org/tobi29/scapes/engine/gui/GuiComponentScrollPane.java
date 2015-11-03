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

    public GuiComponentScrollPane(GuiLayoutData parent, int width, int height,
            int scrollStep) {
        super(parent, width, height);
        GuiComponentSliderVert slider = add(width - 10, 0,
                p -> new GuiComponentSliderVert(p, 10, height, 0));
        viewport = newViewport(slider, scrollStep);
    }

    protected GuiComponentScrollPaneViewport newViewport(
            GuiComponentSliderVert slider, int scrollStep) {
        return add(0, 0,
                p -> new GuiComponentScrollPaneViewport(p, slider, width,
                        height, scrollStep));
    }

    public GuiComponentScrollPaneViewport viewport() {
        return viewport;
    }
}
