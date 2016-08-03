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
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public class GuiLayoutData {
    private final Optional<GuiComponent> parent;
    private final long priority;
    private final boolean blocksEvents;
    private final MutableVector2 size = new MutableVector2d();

    public GuiLayoutData(Optional<GuiComponent> parent, Vector2 size,
            long priority, boolean blocksEvents) {
        this.parent = parent;
        this.size.set(size);
        this.priority = priority;
        this.blocksEvents = blocksEvents;
    }

    public double width() {
        return size.doubleX();
    }

    public double height() {
        return size.doubleY();
    }

    public void setWidth(double value) {
        size.setX(value);
    }

    public void setHeight(double value) {
        size.setY(value);
    }

    public void setSize(Vector2 size) {
        this.size.set(size);
    }

    public Optional<GuiComponent> parent() {
        return parent;
    }

    public long priority() {
        return priority;
    }

    public boolean blocksEvents() {
        return blocksEvents;
    }
}
