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

import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

public class GuiComponentTextButton extends GuiComponentButton {
    protected final GuiComponentText text;

    public GuiComponentTextButton(GuiLayoutData parent, int textSize,
            String text) {
        this(parent, 4, textSize, text);
    }

    public GuiComponentTextButton(GuiLayoutData parent, int textX, int textSize,
            String text) {
        super(parent);
        this.text = addSubHori(textX, 0, -1, textSize,
                p -> new GuiComponentText(p, text));
    }

    public String text() {
        return text.text();
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public void setTextFilter(TextFilter textFilter) {
        text.setTextFilter(textFilter::filter);
    }

    @Override
    protected GuiLayoutManager newLayoutManager(Vector2 size) {
        return new GuiLayoutManagerHorizontal(Vector2d.ZERO, size, components);
    }

    public interface TextFilter {
        String filter(String text);
    }
}
