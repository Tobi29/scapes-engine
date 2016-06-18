package org.tobi29.scapes.engine;

import java8.util.Optional;
import java8.util.stream.Stream;
import org.tobi29.scapes.engine.input.ControllerDefault;
import org.tobi29.scapes.engine.input.ControllerKey;
import org.tobi29.scapes.engine.input.ControllerTouch;
import org.tobi29.scapes.engine.utils.Streams;

public class ContainerEmulateTouch extends ContainerEmulate
        implements ControllerTouch {
    private final ControllerDefault controller;
    private Optional<Tracker> tracker = Optional.empty();

    public ContainerEmulateTouch(Container container) {
        this(container, container.controller().get());
    }

    public ContainerEmulateTouch(Container container,
            ControllerDefault controller) {
        super(container);
        this.controller = controller;
    }

    @Override
    public FormFactor formFactor() {
        return FormFactor.PHONE;
    }

    @Override
    public void setMouseGrabbed(boolean value) {
    }

    @Override
    public Optional<ControllerDefault> controller() {
        return Optional.empty();
    }

    @Override
    public Optional<ControllerTouch> touch() {
        return Optional.of(this);
    }

    @Override
    public Stream<Tracker> fingers() {
        return Streams.of(tracker);
    }

    @Override
    public boolean isActive() {
        return controller.isActive();
    }

    @Override
    public void poll() {
        controller.poll();
        if (tracker.isPresent()) {
            if (controller.isDown(ControllerKey.BUTTON_0)) {
                Tracker tracker = this.tracker.get();
                tracker.pos().set(controller.x(), controller.y());
            } else {
                tracker = Optional.empty();
            }
        } else if (controller.isPressed(ControllerKey.BUTTON_0)) {
            Tracker tracker = new Tracker();
            tracker.pos().set(controller.x(), controller.y());
            this.tracker = Optional.of(tracker);
        }
    }
}
