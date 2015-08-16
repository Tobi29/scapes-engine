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

public class GuiComponentText extends GuiComponent {
    private final int textSize;
    private String text;
    private FontRenderer.Text vaoText;
    private FontRenderer font;

    public GuiComponentText(GuiComponent parent, int x, int y, int textSize,
            String text) {
        this(parent, x, y, Integer.MAX_VALUE, textSize, text);
    }

    public GuiComponentText(GuiComponent parent, int x, int y, int maxLength,
            int textSize, String text) {
        super(parent, x, y, maxLength, textSize);
        this.text = text;
        this.textSize = textSize;
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        if (!this.text.equals(text)) {
            this.text = text;
            updateText();
        }
    }

    @Override
    public void renderComponent(GL gl, Shader shader, FontRenderer font,
            double delta) {
        if (this.font != font) {
            this.font = font;
            updateText();
        }
        vaoText.render(gl, shader);
    }

    private void updateText() {
        if (font != null) {
            vaoText = font.render(text, 0.0f, 0.0f, textSize, width, 1.0f, 1.0f,
                    1.0f, 1);
        }
    }
}
