package org.tobi29.scapes.engine.gui;

import java8.util.Optional;
import java8.util.stream.Stream;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.input.ControllerBasic;
import org.tobi29.scapes.engine.input.ControllerDefault;
import org.tobi29.scapes.engine.input.ControllerKey;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

public class GuiControllerMouse extends GuiControllerDefault {
    private final double scrollSensitivity;
    private final GuiCursor cursor = new GuiCursor(false);
    private Optional<GuiComponent> draggingLeft = Optional.empty(),
            draggingRight = Optional.empty();
    private double dragLeftX, dragLeftY, dragRightX, dragRightY;

    public GuiControllerMouse(ScapesEngine engine,
            ControllerDefault controller) {
        this(engine, controller, 1.0);
    }

    public GuiControllerMouse(ScapesEngine engine, ControllerDefault controller,
            double scrollSensitivity) {
        super(engine, controller);
        this.scrollSensitivity = scrollSensitivity;
    }

    @Override
    public void update(double delta) {
        double cursorX = controller.x();
        double cursorY = controller.y();
        double ratio = 540.0 / engine.container().containerHeight();
        double guiCursorX = cursorX * ratio;
        double guiCursorY = cursorY * ratio;
        cursor.set(new Vector2d(cursorX, cursorY),
                new Vector2d(guiCursorX, guiCursorY));
        if (draggingLeft.isPresent()) {
            GuiComponent component = draggingLeft.get();
            double relativeX = cursor.guiX() - dragLeftX;
            double relativeY = cursor.guiY() - dragLeftY;
            dragLeftX = cursor.guiX();
            dragLeftY = cursor.guiY();
            component.gui().sendNewEvent(
                    new GuiComponentEvent(guiCursorX, guiCursorY, relativeX,
                            relativeY), component, component::dragLeft, engine);
        }
        if (draggingRight.isPresent()) {
            GuiComponent component = draggingRight.get();
            double relativeX = cursor.guiX() - dragRightX;
            double relativeY = cursor.guiY() - dragRightY;
            dragRightX = cursor.guiX();
            dragRightY = cursor.guiY();
            component.gui().sendNewEvent(
                    new GuiComponentEvent(guiCursorX, guiCursorY, relativeX,
                            relativeY), component, component::dragRight,
                    engine);
        }
        double scrollX = controller.scrollX() * scrollSensitivity;
        double scrollY = controller.scrollY() * scrollSensitivity;
        if (scrollX != 0.0 || scrollY != 0.0) {
            engine.guiStack().fireRecursiveEvent(
                    new GuiComponentEvent(guiCursorX, guiCursorY, scrollX,
                            scrollY, false), GuiComponent::scroll, engine);
        }
        GuiComponentEvent componentEvent =
                new GuiComponentEvent(guiCursorX, guiCursorY);
        engine.guiStack()
                .fireEvent(componentEvent, GuiComponent::hover, engine);
        controller.pressEvents().forEach(event -> {
            switch (event.state()) {
                case PRESS:
                    handlePress(event.key(), componentEvent);
                    break;
                case RELEASE:
                    handleRelease(event.key(), componentEvent);
                    break;
            }
        });
    }

    @Override
    public Stream<GuiCursor> cursors() {
        return Streams.of(cursor);
    }

    @Override
    public Stream<Pair<GuiCursor, ControllerBasic.PressEvent>> clicks() {
        return controller.pressEvents().map(event -> new Pair<>(cursor, event));
    }

    private void handlePress(ControllerKey key, GuiComponentEvent event) {
        switch (key) {
            case BUTTON_0:
                draggingLeft = engine.guiStack()
                        .fireEvent(event, GuiComponent::pressLeft, engine);
                dragLeftX = cursor.guiX();
                dragLeftY = cursor.guiY();
                engine.guiStack()
                        .fireEvent(event, GuiComponent::clickLeft, engine);
                break;
            case BUTTON_1:
                draggingLeft = engine.guiStack()
                        .fireEvent(event, GuiComponent::pressRight, engine);
                dragRightX = cursor.guiX();
                dragRightY = cursor.guiY();
                engine.guiStack()
                        .fireEvent(event, GuiComponent::clickRight, engine);
                break;
        }
    }

    private void handleRelease(ControllerKey key, GuiComponentEvent event) {
        switch (key) {
            case BUTTON_0:
                if (draggingLeft.isPresent()) {
                    GuiComponent component = draggingLeft.get();
                    component.gui()
                            .sendNewEvent(event, component, component::dropLeft,
                                    engine);
                    draggingLeft = Optional.empty();
                }
                break;
            case BUTTON_1:
                if (draggingRight.isPresent()) {
                    GuiComponent component = draggingRight.get();
                    component.gui().sendNewEvent(event, component,
                            component::dropRight, engine);
                    draggingRight = Optional.empty();
                }
                break;
        }
    }
}
