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

public class GuiBasicStyle implements GuiStyle {
    private final ScapesEngine engine;
    private final FontRenderer font;

    public GuiBasicStyle(ScapesEngine engine, FontRenderer font) {
        this.engine = engine;
        this.font = font;
    }

    @Override
    public ScapesEngine engine() {
        return engine;
    }

    @Override
    public FontRenderer font() {
        return font;
    }

    @Override
    public void pane(GuiRenderer renderer, Vector2 size) {
        renderer.texture(engine.graphics().textureEmpty(), 0);
        GuiUtils.shadow(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, 0.2f);
        GuiUtils.rectangle(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, 0.3f);
    }

    @Override
    public void button(GuiRenderer renderer, Vector2 size, boolean hover) {
        renderer.texture(engine.graphics().textureEmpty(), 0);
        float a;
        if (hover) {
            a = 1.0f;
        } else {
            a = 0.5f;
        }
        GuiUtils.rectangle(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, a);
    }

    @Override
    public void border(GuiRenderer renderer, Vector2 size) {
        renderer.texture(engine.graphics().textureEmpty(), 0);
        GuiUtils.shadow(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, 0.2f);
    }

    @Override
    public void slider(GuiRenderer renderer, Vector2 size, boolean horizontal,
            double value, double sliderSize, boolean hover) {
        renderer.texture(engine.graphics().textureEmpty(), 0);
        float v, a, ab;
        if (hover) {
            a = 1.0f;
            if (horizontal) {
                v = 0.3f;
                a = 1.0f;
                ab = 1.0f;
            } else {
                v = 0.0f;
                a = 1.0f;
                ab = 0.3f;
            }
        } else {
            v = 0.0f;
            a = 0.5f;
            ab = 0.3f;
        }
        GuiUtils.rectangle(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, ab);
        if (horizontal) {
            value = value * (size.doubleX() - sliderSize);
            GuiUtils.rectangle(renderer, (float) value, 0.0f,
                    (float) (value + sliderSize), size.floatY(), v, v, v, a);
        } else {
            value = value * (size.doubleY() - sliderSize);
            GuiUtils.rectangle(renderer, 0.0f, (float) value, size.floatX(),
                    (float) (value + sliderSize), v, v, v, a);
        }
    }

    @Override
    public void separator(GuiRenderer renderer, Vector2 size) {
        renderer.texture(engine.graphics().textureEmpty(), 0);
        GuiUtils.rectangle(renderer, 0.0f, 0.0f, size.floatX(),
                size.floatY() * 0.5f, 0.0f, 0.0f, 0.0f, 0.3f);
        GuiUtils.rectangle(renderer, 0.0f, size.floatY() * 0.5f, size.floatX(),
                size.floatY(), 0.2f, 0.2f, 0.2f, 0.3f);
    }

    @Override
    public void widget(GuiRenderer renderer, Vector2 size) {
        pane(renderer, size);
    }

    @Override
    public void widgetTitle(GuiRenderer renderer, Vector2 size) {
        pane(renderer, size);
    }
}
