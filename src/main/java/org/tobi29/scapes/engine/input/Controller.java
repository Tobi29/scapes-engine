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

import java.util.stream.Stream;

public interface Controller {
    boolean isActive();

    boolean isDown(ControllerKey key);

    boolean isPressed(ControllerKey key);

    Stream<PressEvent> pressEvents();

    void poll();

    void addPressEvent(ControllerKey key, PressState state);

    enum PressState {
        PRESS,
        REPEAT,
        RELEASE
    }

    class PressEvent {
        protected final ControllerKey key;
        protected final PressState state;

        public PressEvent(ControllerKey key, PressState state) {
            this.key = key;
            this.state = state;
        }

        public ControllerKey key() {
            return key;
        }

        public PressState state() {
            return state;
        }
    }
}
