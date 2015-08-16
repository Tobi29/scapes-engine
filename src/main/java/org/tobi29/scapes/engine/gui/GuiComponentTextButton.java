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

import org.tobi29.scapes.engine.opengl.FontRenderer;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.shader.Shader;

public class GuiComponentTextButton extends GuiComponentButton {
    protected final int textSize, textX, textY;
    protected FontRenderer font;
    protected FontRenderer.Text vaoText;
    private String text;
    private TextFilter textFilter;

    public GuiComponentTextButton(GuiComponent parent, int x, int y, int width,
            int height, int textSize, String text) {
        this(parent, x, y, width, height, textSize, 4, text);
    }

    public GuiComponentTextButton(GuiComponent parent, int x, int y, int width,
            int height, int textSize, String text, TextFilter textFilter) {
        this(parent, x, y, width, height, textSize, 4, text, textFilter);
    }

    public GuiComponentTextButton(GuiComponent parent, int x, int y, int width,
            int height, int textSize, int textShift, String text) {
        this(parent, x, y, width, height, textSize, textShift, text,
                text1 -> text1);
    }

    public GuiComponentTextButton(GuiComponent parent, int x, int y, int width,
            int height, int textSize, int textShift, String text,
            TextFilter textFilter) {
        super(parent, x, y, width, height);
        this.text = text;
        this.textSize = textSize;
        this.textFilter = textFilter;
        textX = textShift;
        textY = (height - textSize) / 2;
    }

    @Override
    public void renderComponent(GL gl, Shader shader, FontRenderer font,
            double delta) {
        super.renderComponent(gl, shader, font, delta);
        if (this.font != font) {
            this.font = font;
            updateText(textFilter.filter(text));
        }
        vaoText.render(gl, shader);
    }

    protected void updateText(String text) {
        if (font != null) {
            vaoText = font.render(text, textX, textY, textSize, width - textX,
                    1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        updateText(textFilter.filter(text));
    }

    public void setTextFilter(TextFilter textFilter) {
        this.textFilter = textFilter;
        updateText(textFilter.filter(text));
    }

    public interface TextFilter {
        String filter(String text);
    }
}
