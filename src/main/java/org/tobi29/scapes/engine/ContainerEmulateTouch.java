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
