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

import java8.util.Optional;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public class GuiLayoutDataVertical extends GuiLayoutData {
    private final Vector2 marginStart, marginEnd;

    public GuiLayoutDataVertical(GuiComponent parent, Vector2 marginStart,
            Vector2 marginEnd, Vector2 size, long priority) {
        this(parent, marginStart, marginEnd, size, priority, false);
    }

    public GuiLayoutDataVertical(GuiComponent parent, Vector2 marginStart,
            Vector2 marginEnd, Vector2 size, long priority,
            boolean blocksEvents) {
        super(Optional.of(parent), size, priority, blocksEvents);
        this.marginStart = marginStart;
        this.marginEnd = marginEnd;
    }

    public Vector2 marginStart() {
        return marginStart;
    }

    public Vector2 marginEnd() {
        return marginEnd;
    }
}
