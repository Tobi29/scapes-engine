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

import java8.util.Optional;
import org.tobi29.scapes.engine.ScapesEngine;

import java.util.Set;

public abstract class Gui extends GuiComponentSlabHeavy {
    protected final GuiStyle style;
    private GuiComponent lastClicked;

    protected Gui(GuiStyle style) {
        super(new GuiLayoutDataRoot());
        this.style = style;
    }

    public void add(Gui add) {
        append(add);
    }

    public Optional<GuiComponent> fireNewEvent(GuiComponentEvent event,
            EventSink listener, ScapesEngine engine) {
        return fireEvent(new GuiComponentEvent(event, baseSize(engine)),
                listener, engine);
    }

    public Set<GuiComponent> fireNewRecursiveEvent(GuiComponentEvent event,
            EventSink listener, ScapesEngine engine) {
        return fireRecursiveEvent(
                new GuiComponentEvent(event, baseSize(engine)), listener,
                engine);
    }

    public boolean sendNewEvent(GuiComponentEvent event,
            GuiComponent destination, EventDestination listener,
            ScapesEngine engine) {
        return sendEvent(new GuiComponentEvent(event, baseSize(engine)),
                destination, listener, engine);
    }

    public abstract boolean valid();

    public GuiStyle style() {
        return style;
    }

    public GuiComponent lastClicked() {
        return lastClicked;
    }

    protected void setLastClicked(GuiComponent component) {
        lastClicked = component;
    }

    @Override
    public boolean ignoresEvents() {
        return true;
    }
}
