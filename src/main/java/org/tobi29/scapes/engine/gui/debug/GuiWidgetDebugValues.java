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

package org.tobi29.scapes.engine.gui.debug;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.gui.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class GuiWidgetDebugValues extends GuiWidget {
    private final Map<String, Element> elements = new ConcurrentHashMap<>();
    private final GuiComponentScrollPaneViewport scrollPane;

    public GuiWidgetDebugValues() {
        super(32, 32, 360, 240, "Debug Values");
        scrollPane = new GuiComponentScrollPaneList(this, 10, 10, 340, 220, 20)
                .viewport();
    }

    public synchronized Element get(String key) {
        Element element = elements.get(key);
        if (element == null) {
            element = new Element(scrollPane, key);
            elements.put(key, element);
        }
        return element;
    }

    public Set<Map.Entry<String, Element>> elements() {
        return elements.entrySet();
    }

    public static class Element extends GuiComponentPane {
        private final GuiComponentText value;
        private final AtomicReference<String> text = new AtomicReference<>();

        private Element(GuiComponent parent, String key) {
            super(parent, 0, 0, 378, 20);
            new GuiComponentTextButton(this, 10, 2, 180, 15, 12, key);
            value = new GuiComponentText(this, 200, 5, 12, "");
        }

        public void setValue(String value) {
            text.lazySet(value);
        }

        public void setValue(boolean value) {
            setValue(String.valueOf(value));
        }

        public void setValue(byte value) {
            setValue(String.valueOf(value));
        }

        public void setValue(short value) {
            setValue(String.valueOf(value));
        }

        public void setValue(int value) {
            setValue(String.valueOf(value));
        }

        public void setValue(long value) {
            setValue(String.valueOf(value));
        }

        public void setValue(float value) {
            setValue(String.valueOf(value));
        }

        public void setValue(double value) {
            setValue(String.valueOf(value));
        }

        public void setValue(Object value) {
            setValue(String.valueOf(value));
        }

        @Override
        public void update(double mouseX, double mouseY, boolean mouseInside,
                ScapesEngine engine) {
            super.update(mouseX, mouseY, mouseInside, engine);
            String newText = text.getAndSet(null);
            if (newText != null) {
                value.setText(newText);
            }
        }

        @Override
        public String toString() {
            String text = this.text.get();
            if (text != null) {
                return text;
            }
            return value.text();
        }
    }
}
