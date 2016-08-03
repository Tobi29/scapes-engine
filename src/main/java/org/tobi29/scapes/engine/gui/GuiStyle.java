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

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.graphics.FontRenderer;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public interface GuiStyle {
    ScapesEngine engine();

    FontRenderer font();

    void pane(GuiRenderer renderer, Vector2 size);

    void button(GuiRenderer renderer, Vector2 size, boolean hover);

    void border(GuiRenderer renderer, Vector2 size);

    void slider(GuiRenderer renderer, Vector2 size, boolean horizontal,
            double value, double sliderSize, boolean hover);

    void separator(GuiRenderer renderer, Vector2 size);

    void widget(GuiRenderer renderer, Vector2 size);

    void widgetTitle(GuiRenderer renderer, Vector2 size);
}
