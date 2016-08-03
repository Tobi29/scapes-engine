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

import org.tobi29.scapes.engine.graphics.FontRenderer;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public class GuiComponentFlowText extends GuiComponent {
    protected final int textWidth;
    protected final float r, g, b, a;
    protected String text;
    protected TextFilter textFilter = str -> str;

    public GuiComponentFlowText(GuiLayoutData parent, String text) {
        this(parent, Integer.MAX_VALUE, text);
    }

    public GuiComponentFlowText(GuiLayoutData parent, String text, float r,
            float g, float b, float a) {
        this(parent, Integer.MAX_VALUE, text, r, g, b, a);
    }

    public GuiComponentFlowText(GuiLayoutData parent, int width, String text) {
        this(parent, width, text, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public GuiComponentFlowText(GuiLayoutData parent, int width, String text,
            float r, float g, float b, float a) {
        super(parent);
        this.text = text;
        textWidth = width;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        updateSize();
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        if (!this.text.equals(text)) {
            this.text = text;
            updateSize();
            dirty();
        }
    }

    public void setTextFilter(TextFilter textFilter) {
        this.textFilter = textFilter;
        updateSize();
        dirty();
    }

    private void updateSize() {
        FontRenderer font = gui.style().font();
        FontRenderer.TextInfo textInfo =
                font.render(FontRenderer.to(), textFilter.filter(text),
                        (float) parent.height(), textWidth);
        parent.setSize(textInfo.size());
    }

    @Override
    protected void updateMesh(GuiRenderer renderer, Vector2 size) {
        FontRenderer font = gui.style().font();
        font.render(FontRenderer.to(renderer, r, g, b, a),
                textFilter.filter(text), size.floatY(), textWidth);
    }

    public interface TextFilter {
        String filter(String text);
    }
}
