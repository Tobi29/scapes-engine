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

public class GuiComponentTextButton extends GuiComponentButton {
    protected final GuiComponentText text;

    public GuiComponentTextButton(GuiComponent parent, int x, int y, int width,
            int height, int textSize, String text) {
        this(parent, x, y, width, height, 4, textSize, text);
    }

    public GuiComponentTextButton(GuiComponent parent, int x, int y, int width,
            int height, int textX, int textSize, String text) {
        super(parent, x, y, width, height);
        int textY = (height - textSize) / 2;
        this.text = new GuiComponentText(this, textX, textY, width - textX,
                textSize, text);
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

    public interface TextFilter {
        String filter(String text);
    }
}
