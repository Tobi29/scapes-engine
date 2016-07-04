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
            component.gui().sendNewEvent(GuiEvent.DRAG_LEFT,
                    new GuiComponentEvent(guiCursorX, guiCursorY, relativeX,
                            relativeY), component, engine);
        }
        if (draggingRight.isPresent()) {
            GuiComponent component = draggingRight.get();
            double relativeX = cursor.guiX() - dragRightX;
            double relativeY = cursor.guiY() - dragRightY;
            dragRightX = cursor.guiX();
            dragRightY = cursor.guiY();
            component.gui().sendNewEvent(GuiEvent.DRAG_RIGHT,
                    new GuiComponentEvent(guiCursorX, guiCursorY, relativeX,
                            relativeY), component, engine);
        }
        double scrollX = controller.scrollX() * scrollSensitivity;
        double scrollY = controller.scrollY() * scrollSensitivity;
        if (scrollX != 0.0 || scrollY != 0.0) {
            engine.guiStack().fireRecursiveEvent(GuiEvent.SCROLL,
                    new GuiComponentEvent(guiCursorX, guiCursorY, scrollX,
                            scrollY, false), engine);
        }
        GuiComponentEvent componentEvent =
                new GuiComponentEvent(guiCursorX, guiCursorY);
        engine.guiStack()
                .fireEvent(componentEvent, GuiComponent::hover, engine);
        controller.pressEvents().forEach(event -> {
            switch (event.state()) {
                case PRESS:
                case REPEAT:
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
                        .fireEvent(GuiEvent.PRESS_LEFT, event, engine);
                dragLeftX = cursor.guiX();
                dragLeftY = cursor.guiY();
                if (engine.guiStack()
                        .fireEvent(GuiEvent.CLICK_LEFT, event, engine)
                        .isPresent()) {
                    return;
                }
                break;
            case BUTTON_1:
                draggingRight = engine.guiStack()
                        .fireEvent(GuiEvent.PRESS_RIGHT, event, engine);
                dragRightX = cursor.guiX();
                dragRightY = cursor.guiY();
                if (engine.guiStack()
                        .fireEvent(GuiEvent.CLICK_RIGHT, event, engine)
                        .isPresent()) {
                    return;
                }
                break;
            case KEY_ESCAPE:
                if (engine.guiStack().fireAction(GuiAction.BACK, engine)) {
                    return;
                }
                break;
            case KEY_ENTER:
                if (engine.guiStack().fireAction(GuiAction.ACTIVATE, engine)) {
                    return;
                }
                break;
            case KEY_UP:
                if (engine.guiStack().fireAction(GuiAction.UP, engine)) {
                    return;
                }
                break;
            case KEY_DOWN:
                if (engine.guiStack().fireAction(GuiAction.DOWN, engine)) {
                    return;
                }
                break;
            case KEY_LEFT:
                if (engine.guiStack().fireAction(GuiAction.LEFT, engine)) {
                    return;
                }
                break;
            case KEY_RIGHT:
                if (engine.guiStack().fireAction(GuiAction.RIGHT, engine)) {
                    return;
                }
                break;
        }
        firePress(key);
    }

    private void handleRelease(ControllerKey key, GuiComponentEvent event) {
        switch (key) {
            case BUTTON_0:
                if (draggingLeft.isPresent()) {
                    GuiComponent component = draggingLeft.get();
                    component.gui()
                            .sendNewEvent(GuiEvent.DROP_LEFT, event, component,
                                    engine);
                    draggingLeft = Optional.empty();
                }
                break;
            case BUTTON_1:
                if (draggingRight.isPresent()) {
                    GuiComponent component = draggingRight.get();
                    component.gui()
                            .sendNewEvent(GuiEvent.DROP_RIGHT, event, component,
                                    engine);
                    draggingRight = Optional.empty();
                }
                break;
        }
    }
}
