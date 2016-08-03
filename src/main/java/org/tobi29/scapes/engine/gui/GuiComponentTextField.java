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

import java.util.Arrays;

public class GuiComponentTextField extends GuiComponentButtonHeavy {
    protected final GuiComponentEditableText text;
    protected final boolean major;

    public GuiComponentTextField(GuiLayoutData parent, int textSize,
            String text) {
        this(parent, textSize, text, false);
    }

    public GuiComponentTextField(GuiLayoutData parent, int textSize,
            String text, boolean hiddenText) {
        this(parent, textSize, text, Integer.MAX_VALUE, hiddenText);
    }

    public GuiComponentTextField(GuiLayoutData parent, int textSize,
            String text, int maxLength) {
        this(parent, textSize, text, maxLength, false);
    }

    public GuiComponentTextField(GuiLayoutData parent, int textSize,
            String text, int maxLength, boolean hiddenText) {
        this(parent, textSize, text, maxLength, hiddenText, false);
    }

    public GuiComponentTextField(GuiLayoutData parent, int textSize,
            String text, int maxLength, boolean hiddenText, boolean major) {
        this(parent, 4, textSize, text, maxLength, hiddenText, major);
    }

    public GuiComponentTextField(GuiLayoutData parent, int textX, int textSize,
            String text, int maxLength, boolean hiddenText, boolean major) {
        super(parent);
        this.text = addSubHori(textX, 0, -1, textSize,
                p -> new GuiComponentEditableText(p, text, maxLength));
        this.major = major;
        if (hiddenText) {
            this.text.setTextFilter(str -> {
                char[] array = new char[str.length()];
                Arrays.fill(array, '*');
                return new String(array);
            });
        } else {
            this.text.setTextFilter(str -> str);
        }
    }

    public String text() {
        return text.text();
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    @Override
    protected void updateComponent(ScapesEngine engine, double delta) {
        GuiComponent current = gui.lastClicked();
        if (current == this || current == text || major) {
            text.setActive(true);
        } else {
            text.setActive(false);
        }
    }
}
