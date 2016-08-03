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
import java8.util.concurrent.ConcurrentMaps;
import java8.util.function.Consumer;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class Gui extends GuiComponentSlabHeavy {
    protected final GuiStyle style;
    private final List<SelectionEntry> selections = new ArrayList<>();
    private final ConcurrentMap<GuiAction, Set<Consumer<ScapesEngine>>>
            actions = new ConcurrentHashMap<>();
    private GuiComponent lastClicked;
    private int selection = -1, selectionColumn;

    protected Gui(GuiStyle style) {
        super(new GuiLayoutDataRoot());
        this.style = style;

        on(GuiAction.ACTIVATE, engine -> {
            synchronized (selections) {
                if (selection < 0) {
                    return;
                }
                SelectionEntry entry = selections.get(selection);
                sendNewEvent(GuiEvent.CLICK_LEFT, new GuiComponentEvent(),
                        entry.components.get(FastMath.min(selectionColumn,
                                entry.components.size() - 1)), engine);
            }
        });
        on(GuiAction.UP, () -> {
            synchronized (selections) {
                selection = FastMath.max(selection - 1,
                        FastMath.min(0, selections.size() - 1));
            }
        });
        on(GuiAction.DOWN, () -> {
            synchronized (selections) {
                selection = FastMath.min(selection + 1, selections.size() - 1);
            }
        });
        on(GuiAction.LEFT, engine -> {
            synchronized (selections) {
                if (selection < 0) {
                    return;
                }
                SelectionEntry entry = selections.get(selection);
                if (selectionColumn > 0) {
                    selectionColumn = FastMath.min(selectionColumn,
                            entry.components.size() - 1);
                    selectionColumn = FastMath.max(selectionColumn - 1, 0);
                }
                sendNewEvent(GuiEvent.SCROLL,
                        new GuiComponentEvent(Double.NaN, Double.NaN, 1, 0,
                                false), entry.components.get(FastMath
                                .min(selectionColumn,
                                        entry.components.size() - 1)), engine);
            }
        });
        on(GuiAction.RIGHT, engine -> {
            synchronized (selections) {
                if (selection < 0) {
                    return;
                }
                SelectionEntry entry = selections.get(selection);
                if (selectionColumn < entry.components.size() - 1) {
                    selectionColumn = FastMath.min(selectionColumn + 1,
                            entry.components.size() - 1);
                }
                sendNewEvent(GuiEvent.SCROLL,
                        new GuiComponentEvent(Double.NaN, Double.NaN, -1, 0,
                                false), entry.components.get(FastMath
                                .min(selectionColumn,
                                        entry.components.size() - 1)), engine);
            }
        });
    }

    protected void selection(GuiComponent component) {
        selection(component.parent.priority(), component);
    }

    protected void selection(long priority, GuiComponent component) {
        addSelection(priority, Streams.collect(component));
    }

    protected void selection(GuiComponent... components) {
        if (components.length == 0) {
            return;
        }
        selection(components[0].parent.priority(), components);
    }

    protected void selection(long priority, GuiComponent... components) {
        if (components.length == 0) {
            return;
        }
        addSelection(priority, Streams.collect(components));
    }

    protected void selection(List<GuiComponent> components) {
        if (components.isEmpty()) {
            return;
        }
        selection(components.get(0).parent.priority(), components);
    }

    protected void selection(long priority, List<GuiComponent> components) {
        if (components.isEmpty()) {
            return;
        }
        List<GuiComponent> list = new ArrayList<>();
        list.addAll(components);
        addSelection(priority, list);
    }

    private void addSelection(long priority, List<GuiComponent> components) {
        SelectionEntry entry = new SelectionEntry(priority, components);
        synchronized (selections) {
            for (int i = selections.size() - 1; i >= 0; i--) {
                if (selections.get(i).priority >= priority) {
                    selections.add(i + 1, entry);
                    return;
                }
            }
            selections.add(0, entry);
        }
    }

    public void on(GuiAction action, Runnable listener) {
        on(action, engine -> listener.run());
    }

    public void on(GuiAction action, Consumer<ScapesEngine> listener) {
        Set<Consumer<ScapesEngine>> listeners = ConcurrentMaps
                .computeIfAbsent(actions, action, key -> Collections
                        .newSetFromMap(new ConcurrentHashMap<>()));
        listeners.add(listener);
    }

    public Optional<GuiComponent> fireNewEvent(GuiEvent type,
            GuiComponentEvent event, ScapesEngine engine) {
        return fireNewEvent(event, GuiComponent.sink(type), engine);
    }

    public Optional<GuiComponent> fireNewEvent(GuiComponentEvent event,
            EventSink listener, ScapesEngine engine) {
        return fireEvent(new GuiComponentEvent(event, baseSize(engine)),
                listener, engine);
    }

    public Set<GuiComponent> fireNewRecursiveEvent(GuiEvent type,
            GuiComponentEvent event, ScapesEngine engine) {
        return fireNewRecursiveEvent(event, GuiComponent.sink(type), engine);
    }

    public Set<GuiComponent> fireNewRecursiveEvent(GuiComponentEvent event,
            EventSink listener, ScapesEngine engine) {
        return fireRecursiveEvent(
                new GuiComponentEvent(event, baseSize(engine)), listener,
                engine);
    }

    public boolean sendNewEvent(GuiEvent type, GuiComponentEvent event,
            GuiComponent destination, ScapesEngine engine) {
        return sendNewEvent(event, destination,
                GuiComponent.sink(type, destination), engine);
    }

    public boolean sendNewEvent(GuiComponentEvent event,
            GuiComponent destination, EventDestination listener,
            ScapesEngine engine) {
        return sendEvent(new GuiComponentEvent(event, baseSize(engine)),
                destination, listener, engine);
    }

    public boolean fireAction(GuiAction action, ScapesEngine engine) {
        Set<Consumer<ScapesEngine>> listeners = actions.get(action);
        if (listeners == null || listeners.isEmpty()) {
            return false;
        }
        Streams.forEach(listeners, listener -> listener.accept(engine));
        return true;
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
    protected void update(ScapesEngine engine, double delta) {
        super.update(engine, delta);
        if (visible) {
            synchronized (selections) {
                Iterator<SelectionEntry> iterator = selections.iterator();
                while (iterator.hasNext()) {
                    SelectionEntry entry = iterator.next();
                    Iterator<GuiComponent> componentIterator =
                            entry.components.iterator();
                    while (componentIterator.hasNext()) {
                        GuiComponent component = componentIterator.next();
                        if (component.removed) {
                            componentIterator.remove();
                        }
                    }
                    if (entry.components.isEmpty()) {
                        iterator.remove();
                        selection =
                                FastMath.min(selection, selections.size() - 1);
                    }
                }
                if (selection < 0) {
                    return;
                }
                SelectionEntry entry = selections.get(selection);
                GuiComponent component = entry.components.get(FastMath
                        .min(selectionColumn, entry.components.size() - 1));
                sendNewEvent(new GuiComponentEvent(), component,
                        component::hover, engine);
            }
        }
    }

    @Override
    public boolean ignoresEvents() {
        return true;
    }

    private static class SelectionEntry {
        public final long priority;
        public final List<GuiComponent> components;

        public SelectionEntry(long priority, List<GuiComponent> components) {
            this.priority = priority;
            this.components = components;
        }
    }
}
