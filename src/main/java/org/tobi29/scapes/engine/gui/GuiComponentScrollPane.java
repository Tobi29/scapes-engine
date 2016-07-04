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

public class GuiComponentScrollPane extends GuiComponentVisibleSlabHeavy {
    protected final GuiComponentScrollPaneViewport viewport;

    public GuiComponentScrollPane(GuiLayoutData parent, int scrollStep) {
        super(parent);
        viewport = addHori(0, 0, -1, -1,
                p -> new GuiComponentScrollPaneViewport(p, scrollStep));
        GuiComponentSliderVert slider =
                addHori(0, 0, 10, -1, p -> new GuiComponentSliderVert(p, 0));
        slider.on(GuiEvent.CHANGE, event -> viewport.setScrollY(slider.value() *
                Math.max(0, viewport.maxY() - event.size().doubleY())));
        slider.on(GuiEvent.SCROLL, event -> viewport.setScrollY(slider.value() *
                Math.max(0, viewport.maxY() - event.size().doubleY())));
        viewport.setSliderY(slider);
    }

    public GuiComponentScrollPaneViewport viewport() {
        return viewport;
    }
}
