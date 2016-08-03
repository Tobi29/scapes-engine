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

import java8.util.function.Function;

import java.util.concurrent.atomic.AtomicLong;

public class GuiNotifications extends Gui {
    private final GuiComponentGroup pane;
    private final AtomicLong id = new AtomicLong(Long.MIN_VALUE);

    public GuiNotifications(GuiStyle style) {
        super(style);
        spacer();
        pane = addHori(0, 0, 310, -1, GuiComponentGroup::new);
    }

    public <T extends GuiComponent> T add(
            Function<GuiLayoutDataVertical, T> child) {
        return pane
                .addVert(10, 10, 10, 10, -1, 60, id.getAndIncrement(), child);
    }

    @Override
    public boolean valid() {
        return true;
    }
}
