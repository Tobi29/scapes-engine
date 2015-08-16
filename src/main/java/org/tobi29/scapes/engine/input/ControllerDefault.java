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
import java.util.stream.Stream;

public abstract class ControllerDefault implements Controller {
    private final byte[] states;
    private final Queue<PressEvent> pressEventQueue =
            new ConcurrentLinkedQueue<>();
    private final Queue<KeyTypeEvent> typeEventQueue =
            new ConcurrentLinkedQueue<>();
    private Collection<PressEvent> pressEvents = Collections.emptyList();
    private Collection<KeyTypeEvent> typeEvents = Collections.emptyList();
    private double x, y, deltaX, deltaY, scrollX, scrollY;
    private double deltaXSet, deltaYSet, scrollXSet, scrollYSet;
    private boolean active;

    protected ControllerDefault() {
        states = new byte[ControllerKey.values().length];
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
        List<KeyTypeEvent> newTypeEvents = new ArrayList<>();
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
        while (!typeEventQueue.isEmpty()) {
            newTypeEvents.add(typeEventQueue.poll());
        }
        pressEvents = newPressEvents;
        typeEvents = newTypeEvents;
        deltaX = deltaXSet;
        deltaXSet = 0.0;
        deltaY = deltaYSet;
        deltaYSet = 0.0;
        scrollX = scrollXSet;
        scrollXSet = 0.0;
        scrollY = scrollYSet;
        scrollYSet = 0.0;
    }

    @Override
    public void addPressEvent(ControllerKey key, PressState state) {
        pressEventQueue.add(new PressEvent(key, state));
    }

    public Stream<KeyTypeEvent> typeEvents() {
        return typeEvents.stream();
    }

    public abstract boolean isModifierDown();

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double deltaX() {
        return deltaX;
    }

    public double deltaY() {
        return deltaY;
    }

    public double scrollX() {
        return scrollX;
    }

    public double scrollY() {
        return scrollY;
    }

    public void addTypeEvent(char character) {
        typeEventQueue.add(new KeyTypeEvent(character));
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void addDelta(double x, double y) {
        deltaXSet += x;
        deltaYSet += y;
    }

    public void addScroll(double x, double y) {
        scrollXSet += x;
        scrollYSet += y;
        if (x < 0.0) {
            addPressEvent(ControllerKey.SCROLL_LEFT, PressState.PRESS);
            addPressEvent(ControllerKey.SCROLL_LEFT, PressState.RELEASE);
        } else if (x > 0.0) {
            addPressEvent(ControllerKey.SCROLL_RIGHT, PressState.PRESS);
            addPressEvent(ControllerKey.SCROLL_RIGHT, PressState.RELEASE);
        }
        if (y < 0.0) {
            addPressEvent(ControllerKey.SCROLL_DOWN, PressState.PRESS);
            addPressEvent(ControllerKey.SCROLL_DOWN, PressState.RELEASE);
        } else if (y > 0.0) {
            addPressEvent(ControllerKey.SCROLL_UP, PressState.PRESS);
            addPressEvent(ControllerKey.SCROLL_UP, PressState.RELEASE);
        }
    }

    public synchronized void clearStates() {
        Arrays.fill(states, (byte) 0);
    }

    public abstract void clipboardCopy(String value);

    public abstract String clipboardPaste();

    public static class KeyTypeEvent {
        private final char character;

        public KeyTypeEvent(char character) {
            this.character = character;
        }

        public char character() {
            return character;
        }
    }
}
