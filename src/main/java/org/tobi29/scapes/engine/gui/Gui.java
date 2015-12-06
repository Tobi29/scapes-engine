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
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.matrix.MatrixStack;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Gui extends GuiComponentPane {
    protected final GuiStyle style;
    private final GuiAlignment alignment;
    private GuiComponent lastClicked;

    protected Gui(GuiStyle style, GuiAlignment alignment) {
        this(960, 540, style, alignment);
    }

    protected Gui(int width, int height, GuiStyle style,
            GuiAlignment alignment) {
        super(new GuiLayoutDataRoot(), width, height);
        this.style = style;
        this.alignment = alignment;
    }

    public void add(Gui add) {
        changeComponents.add(() -> append(add));
    }

    public Optional<GuiComponent> fireNewEvent(GuiComponentEvent event,
            EventSink listener, ScapesEngine engine) {
        if (visible) {
            boolean inside = checkInside(event.x(), event.y());
            if (inside) {
                if (event.screen()) {
                    event = new GuiComponentEvent(alignedX(event.x(), engine),
                            alignedRelativeX(event.relativeX(), engine), event);
                } else {
                    event = new GuiComponentEvent(alignedX(event.x(), engine),
                            event);
                }
                GuiLayoutManager layout = layoutManager();
                for (GuiComponent component : components) {
                    Vector2 pos = layout.layout(component);
                    if (!component.parent.blocksEvents()) {
                        Optional<GuiComponent> sink = component.fireEvent(
                                new GuiComponentEvent(event, pos.doubleX(),
                                        pos.doubleY()), listener, engine);
                        if (sink.isPresent()) {
                            return sink;
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    public Set<GuiComponent> fireNewRecursiveEvent(GuiComponentEvent event,
            EventSink listener, ScapesEngine engine) {
        if (visible) {
            boolean inside = checkInside(event.x(), event.y());
            if (inside) {
                if (event.screen()) {
                    event = new GuiComponentEvent(alignedX(event.x(), engine),
                            alignedRelativeX(event.relativeX(), engine), event);
                } else {
                    event = new GuiComponentEvent(alignedX(event.x(), engine),
                            event);
                }
                Set<GuiComponent> sinks = new HashSet<>();
                GuiLayoutManager layout = layoutManager();
                for (GuiComponent component : components) {
                    Vector2 pos = layout.layout(component);
                    if (!component.parent.blocksEvents()) {
                        sinks.addAll(component.fireRecursiveEvent(
                                new GuiComponentEvent(event, pos.doubleX(),
                                        pos.doubleY()), listener, engine));
                    }
                }
                return sinks;
            }
        }
        return Collections.emptySet();
    }

    public boolean sendNewEvent(GuiComponentEvent event,
            GuiComponent destination, EventDestination listener,
            ScapesEngine engine) {
        if (visible) {
            if (event.screen()) {
                event = new GuiComponentEvent(alignedX(event.x(), engine),
                        alignedRelativeX(event.relativeX(), engine), event);
            } else {
                event = new GuiComponentEvent(alignedX(event.x(), engine),
                        event);
            }
            GuiLayoutManager layout = layoutManager();
            for (GuiComponent component : components) {
                Vector2 pos = layout.layout(component);
                if (!component.parent.blocksEvents()) {
                    boolean success = component.sendEvent(
                            new GuiComponentEvent(event, pos.doubleX(),
                                    pos.doubleY()), destination, listener,
                            engine);
                    if (success) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void renderGUI(GL gl, Shader shader, double delta) {
        if (visible) {
            if (alignment == GuiAlignment.STRETCH) {
                render(gl, shader, delta);
            } else {
                MatrixStack matrixStack = gl.matrixStack();
                Matrix matrix = matrixStack.push();
                float ratio =
                        (float) gl.sceneHeight() / gl.sceneWidth() / 540 * 960;
                matrix.scale(ratio, 1.0f, 1.0f);
                switch (alignment) {
                    case CENTER:
                        matrix.translate(-480.0f + 480.0f / ratio, 0.0f, 0.0f);
                        break;
                    case RIGHT:
                        matrix.translate(-960.0f + 960.0f / ratio, 0.0f, 0.0f);
                        break;
                }
                render(gl, shader, delta);
                matrixStack.pop();
            }
        }
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

    protected double alignedX(double x, ScapesEngine engine) {
        switch (alignment) {
            case LEFT:
                x *= engine.container().containerWidth() * 540.0 /
                        engine.container().containerHeight() /
                        960.0;
                return x;
            case CENTER:
                double width = engine.container().containerWidth() * 540.0 /
                        engine.container().containerHeight();
                x *= width / 960.0;
                x += (960.0 - width) * 0.5;
                return x;
            case RIGHT:
                width = engine.container().containerWidth() * 540.0 /
                        engine.container().containerHeight();
                x *= width / 960.0;
                x += 960.0 - width;
                return x;
        }
        return x;
    }

    protected double alignedRelativeX(double x, ScapesEngine engine) {
        switch (alignment) {
            case LEFT:
            case CENTER:
            case RIGHT:
                x *= engine.container().containerWidth() * 540.0 /
                        engine.container().containerHeight() /
                        960.0;
                return x;
        }
        return x;
    }
}
