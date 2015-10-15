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
package org.tobi29.scapes.engine.backends.lwjgl3.qt;

import net.java.games.input.*;
import org.tobi29.scapes.engine.input.ControllerJoystick;
import org.tobi29.scapes.engine.input.ControllerKey;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JInputControllers {
    private static final double DEADZONES = 0.05, DEADZONES_SCALE = 0.95;
    private final Event event = new Event();
    private final List<JoystickMapper> joysticks;
    private boolean joysticksChanged;

    public JInputControllers(List<JoystickMapper> joysticks) {
        this.joysticks = joysticks;
        ControllerEnvironment environment =
                ControllerEnvironment.getDefaultEnvironment();
        environment.addControllerListener(new ControllerListener() {
            @Override
            public void controllerRemoved(ControllerEvent controllerEvent) {
                removeController(controllerEvent.getController());
            }

            @Override
            public void controllerAdded(ControllerEvent controllerEvent) {
                addController(controllerEvent.getController());
            }
        });
        Arrays.stream(environment.getControllers())
                .forEach(this::addController);
    }

    private static double deadzones(double value) {
        if (value > DEADZONES) {
            return (value - DEADZONES) / DEADZONES_SCALE;
        } else if (value < -DEADZONES) {
            return (value + DEADZONES) / DEADZONES_SCALE;
        }
        return 0.0f;
    }

    private void addController(Controller controller) {
        if (controller.getType() == Controller.Type.GAMEPAD) {
            joysticks.add(new JoystickMapper(controller));
        }
        joysticksChanged = true;
    }

    private void removeController(Controller controller) {
        for (JoystickMapper joystick : joysticks) {
            if (controller == joystick.controller) {
                joysticks.remove(joystick);
                break;
            }
        }
        joysticksChanged = true;
    }

    public boolean poll() {
        joysticks.forEach(joystick -> {
            joystick.controller.poll();
            EventQueue eventQueue = joystick.controller.getEventQueue();
            while (eventQueue.getNextEvent(event)) {
                poll(joystick);
            }
        });
        boolean joysticksChanged = this.joysticksChanged;
        this.joysticksChanged = false;
        return joysticksChanged;
    }

    private void poll(JoystickMapper joystick) {
        Component.Identifier identifier = event.getComponent().getIdentifier();
        if (identifier instanceof Component.Identifier.Button) {
            ControllerKey button = joystick.buttons.get(identifier);
            if (button != null) {
                joystick.virtualController.addPressEvent(button,
                        event.getValue() > 0.5f ?
                                org.tobi29.scapes.engine.input.Controller.PressState.PRESS :
                                org.tobi29.scapes.engine.input.Controller.PressState.RELEASE);
            }
        } else if (identifier instanceof Component.Identifier.Axis) {
            Integer axis = joystick.axes.get(identifier);
            if (axis != null) {
                joystick.virtualController
                        .setAxis(axis, deadzones(event.getValue()));
            }
        }
    }

    public static class JoystickMapper {
        private final Controller controller;
        private final ControllerJoystick virtualController;
        private final Map<Component.Identifier.Button, ControllerKey> buttons =
                new ConcurrentHashMap<>();
        private final Map<Component.Identifier.Axis, Integer> axes =
                new ConcurrentHashMap<>();

        public JoystickMapper(Controller controller) {
            this.controller = controller;
            int buttonID = 0, axisID = 0;
            for (Component component : controller.getComponents()) {
                Component.Identifier identifier = component.getIdentifier();
                if (identifier instanceof Component.Identifier.Button) {
                    buttons.put((Component.Identifier.Button) identifier,
                            ControllerKey.button(buttonID++));
                } else if (identifier instanceof Component.Identifier.Axis) {
                    axes.put((Component.Identifier.Axis) identifier, axisID++);
                }
            }
            virtualController =
                    new ControllerJoystick(controller.getName(), axes.size());
        }

        public ControllerJoystick joystick() {
            return virtualController;
        }
    }
}
