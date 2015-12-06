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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class GuiWidgetDebugValues extends GuiComponentWidget {
    private final Map<String, Element> elements = new ConcurrentHashMap<>();
    private final GuiComponentScrollPaneViewport scrollPane;

    public GuiWidgetDebugValues(GuiLayoutData parent) {
        super(parent, 360, 256, "Debug Values");
        scrollPane =
                add(10, 10, p -> new GuiComponentScrollPane(p, 340, 220, 20))
                        .viewport();
    }

    public synchronized Element get(String key) {
        Element element = elements.get(key);
        if (element == null) {
            element = scrollPane.addVert(0, 0, p -> new Element(p, key));
            elements.put(key, element);
        }
        return element;
    }

    public synchronized void clear() {
        Iterator<Map.Entry<String, Element>> iterator =
                elements.entrySet().iterator();
        while (iterator.hasNext()) {
            scrollPane.remove(iterator.next().getValue());
            iterator.remove();
        }
    }

    public Set<Map.Entry<String, Element>> elements() {
        return elements.entrySet();
    }

    public static class Element extends GuiComponentPane {
        private final GuiComponentText value;
        private final AtomicReference<String> text = new AtomicReference<>();

        private Element(GuiLayoutData parent, String key) {
            super(parent, 378, 20);
            addHori(3, 3, p -> new GuiComponentTextButton(p, 180, 15, 12, key));
            value = addHori(3, 3, p -> new GuiComponentText(p, 12, ""));
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
        public void updateComponent(ScapesEngine engine) {
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
