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
import org.tobi29.scapes.engine.utils.Triple;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Gui extends GuiComponentSlab {
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
        if (visible) {
            if (event.screen()) {
                event = new GuiComponentEvent(event.x(), event.relativeX(),
                        event);
            } else {
                event = new GuiComponentEvent(event.x(), event);
            }
            GuiLayoutManager layout = layoutManager(baseSize(engine));
            for (Triple<GuiComponent, Vector2, Vector2> component : layout
                    .layout()) {
                if (!component.a.parent.blocksEvents()) {
                    Optional<GuiComponent> sink = component.a.fireEvent(
                            new GuiComponentEvent(event, component.b.doubleX(),
                                    component.b.doubleY(), component.c),
                            listener, engine);
                    if (sink.isPresent()) {
                        return sink;
                    }
                }
            }
        }
        return Optional.empty();
    }

    public Set<GuiComponent> fireNewRecursiveEvent(GuiComponentEvent event,
            EventSink listener, ScapesEngine engine) {
        if (visible) {
            GuiComponentEvent event1;
            if (event.screen()) {
                event1 = new GuiComponentEvent(event.x(), event.relativeX(),
                        event);
            } else {
                event1 = new GuiComponentEvent(event.x(), event);
            }
            Set<GuiComponent> sinks = new HashSet<>();
            layoutStream(baseSize(engine))
                    .filter(component -> !component.a.parent.blocksEvents())
                    .forEach(component -> sinks.addAll(component.a
                            .fireRecursiveEvent(new GuiComponentEvent(event1,
                                            component.b.doubleX(),
                                            component.b.doubleY(), component.c),
                                    listener, engine)));
            return sinks;
        }
        return Collections.emptySet();
    }

    public boolean sendNewEvent(GuiComponentEvent event,
            GuiComponent destination, EventDestination listener,
            ScapesEngine engine) {
        if (visible) {
            if (event.screen()) {
                event = new GuiComponentEvent(event.x(), event.relativeX(),
                        event);
            } else {
                event = new GuiComponentEvent(event.x(), event);
            }
            GuiLayoutManager layout = layoutManager(baseSize(engine));
            for (Triple<GuiComponent, Vector2, Vector2> component : layout
                    .layout()) {
                if (!component.a.parent.blocksEvents()) {
                    boolean success = component.a.sendEvent(
                            new GuiComponentEvent(event, component.b.doubleX(),
                                    component.b.doubleY(), component.c),
                            destination, listener, engine);
                    if (success) {
                        return true;
                    }
                }
            }
        }
        return false;
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
}
