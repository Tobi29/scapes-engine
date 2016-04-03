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

import java8.util.Optional;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.gui.*;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.profiler.Profiler;

public class GuiWidgetProfiler extends GuiComponentWidget {
    private final GuiComponentScrollPaneViewport scrollPane;
    private Optional<Profiler.Node> node = Optional.empty();

    public GuiWidgetProfiler(GuiLayoutData parent) {
        super(parent, "Profiler");
        GuiComponentGroupSlab slab =
                addVert(2, 2, -1, 20, GuiComponentGroupSlab::new);
        GuiComponentTextButton toggle = slab.addHori(2, 2, -1, -1,
                p -> new GuiComponentTextButton(p, 12, "Enable"));
        toggle.onClickLeft(event -> {
            if (Profiler.enabled()) {
                toggle.setText("Enable");
                Profiler.disable();
            } else {
                toggle.setText("Disable");
                Profiler.enable();
            }
            nodes();
        });
        GuiComponentTextButton refresh = slab.addHori(2, 2, -1, -1,
                p -> new GuiComponentTextButton(p, 12, "Refresh"));
        refresh.onClickLeft(event -> {
            nodes();
        });
        GuiComponentTextButton reset = slab.addHori(2, 2, -1, -1,
                p -> new GuiComponentTextButton(p, 12, "Reset"));
        reset.onClickLeft(event -> {
            Profiler.reset();
            threads();
        });
        scrollPane =
                addVert(10, 10, -1, -1, p -> new GuiComponentScrollPane(p, 20))
                        .viewport();
        nodes();
    }

    public synchronized void nodes() {
        if (!node.isPresent()) {
            threads();
            return;
        }
        scrollPane.removeAll();
        Profiler.Node node = this.node.get();
        scrollPane
                .addVert(0, 0, -1, 20, p -> new Element(p, node, node.parent));
        for (Profiler.Node child : node.children.values()) {
            scrollPane.addVert(10, 0, 0, 0, -1, 20,
                    p -> new Element(p, child, Optional.of(child)));
        }
    }

    public synchronized void threads() {
        scrollPane.removeAll();
        node = Optional.empty();
        Thread[] threads;
        int count = Thread.activeCount();
        do {
            threads = new Thread[count];
            count = Thread.enumerate(threads);
        } while (threads.length < count);
        for (int i = 0; i < count; i++) {
            Thread thread = threads[i];
            Optional<Profiler.Node> node = Profiler.node(thread);
            if (node.isPresent()) {
                scrollPane.addVert(10, 0, 0, 0, -1, 20,
                        p -> new Element(p, node.get(), node));
            }
        }
    }

    public synchronized void nodes(Optional<Profiler.Node> node) {
        this.node = node;
        nodes();
    }

    private final class Element extends GuiComponentSlab {
        private final Profiler.Node node;
        private final GuiComponentTextButton key;
        private final GuiComponentText value;

        private Element(GuiLayoutData parent, Profiler.Node node,
                Optional<Profiler.Node> go) {
            super(parent);
            this.node = node;
            key = addHori(2, 2, -1, -1,
                    p -> new GuiComponentTextButton(p, 12, node.name.get()));
            value = addHori(4, 4, -1, -1, p -> new GuiComponentText(p, ""));
            key.onClickLeft(event -> {
                nodes(go);
            });
        }

        @Override
        public void updateComponent(ScapesEngine engine, double delta,
                Vector2 size) {
            value.setText(String.valueOf(node.time()));
        }
    }
}
