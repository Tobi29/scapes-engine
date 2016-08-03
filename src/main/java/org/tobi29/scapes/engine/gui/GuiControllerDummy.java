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

import java8.util.stream.Stream;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.input.ControllerBasic;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.Streams;

public class GuiControllerDummy extends GuiController {
    public GuiControllerDummy(ScapesEngine engine) {
        super(engine);
    }

    @Override
    public void update(double delta) {
    }

    @Override
    public void focusTextField(TextFieldData data, boolean multiline) {
    }

    @Override
    public boolean processTextField(TextFieldData data, boolean multiline) {
        return false;
    }

    @Override
    public Stream<GuiCursor> cursors() {
        return Streams.of();
    }

    @Override
    public Stream<Pair<GuiCursor, ControllerBasic.PressEvent>> clicks() {
        return Streams.of();
    }

    @Override
    public boolean captureCursor() {
        return false;
    }
}
