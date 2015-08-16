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

package org.tobi29.scapes.engine.input;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ControllerJoystick implements Controller {
    private static final Pattern REPLACE = Pattern.compile(" |/|-");
    private final String name, id;
    private final byte[] states;
    private final double[] axes;
    private final Queue<PressEvent> pressEventQueue =
            new ConcurrentLinkedQueue<>();
    private Collection<PressEvent> pressEvents = Collections.emptyList();
    private boolean active;

    public ControllerJoystick(String name, int axisCount) {
        this.name = name;
        id = REPLACE.matcher(name).replaceAll("");
        states = new byte[ControllerKey.values().length];
        axes = new double[axisCount];
    }

    public String name() {
        return name;
    }

    public String id() {
        return id;
    }

    public int axes() {
        return axes.length;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isDown(ControllerKey key) {
        return states[key.id()] >= 1;
    }

    @Override
    public boolean isPressed(ControllerKey key) {
        return states[key.id()] >= 2;
    }

    @Override
    public Stream<PressEvent> pressEvents() {
        return pressEvents.stream();
    }

    @Override
    public synchronized void poll() {
        for (int i = 0; i < states.length; i++) {
            switch (states[i]) {
                case 2:
                    states[i] = 1;
                    break;
                case 3:
                    states[i] = 0;
                    break;
            }
        }
        List<PressEvent> newPressEvents = new ArrayList<>();
        active = !pressEventQueue.isEmpty();
        while (!pressEventQueue.isEmpty()) {
            PressEvent event = pressEventQueue.poll();
            int keyID = event.key.id();
            switch (event.state) {
                case PRESS:
                    states[keyID] = 2;
                    break;
                case RELEASE:
                    if (states[keyID] == 2) {
                        states[keyID] = 3;
                    } else {
                        states[keyID] = 0;
                    }
                    break;
            }
            newPressEvents.add(event);
        }
        pressEvents = newPressEvents;
    }

    @Override
    public void addPressEvent(ControllerKey key, PressState state) {
        pressEventQueue.add(new PressEvent(key, state));
    }

    public double axis(int axis) {
        if (axis < 0 || axis >= axes.length) {
            return 0.0;
        }
        return axes[axis];
    }

    public synchronized void setAxis(int axis, double value) {
        if (axes[axis] < 0.5 && value >= 0.5) {
            addPressEvent(ControllerKey.axis(axis), PressState.PRESS);
        } else if (axes[axis] >= 0.5 && value < 0.5) {
            addPressEvent(ControllerKey.axis(axis), PressState.RELEASE);
        }
        axes[axis] = value;
    }
}
