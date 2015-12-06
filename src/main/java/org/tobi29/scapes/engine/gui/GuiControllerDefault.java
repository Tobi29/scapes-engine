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

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.input.ControllerBasic;
import org.tobi29.scapes.engine.input.ControllerDefault;
import org.tobi29.scapes.engine.input.ControllerKey;
import org.tobi29.scapes.engine.opengl.Container;
import org.tobi29.scapes.engine.utils.MutableSingle;

import java.util.regex.Pattern;

public abstract class GuiControllerDefault implements GuiController {
    private static final Pattern REPLACE = Pattern.compile("\n");
    protected final ScapesEngine engine;
    protected final ControllerDefault controller;

    protected GuiControllerDefault(ScapesEngine engine,
            ControllerDefault controller) {
        this.engine = engine;
        this.controller = controller;
    }

    @Override
    public void focusTextField(TextFieldData data, boolean multiline) {
    }

    @Override
    public boolean processTextField(TextFieldData data, boolean multiline) {
        MutableSingle<Boolean> changed = new MutableSingle<>(false);
        boolean shift = controller.isDown(ControllerKey.KEY_LEFT_SHIFT) ||
                controller.isDown(ControllerKey.KEY_RIGHT_SHIFT);
        if (controller.isModifierDown()) {
            Container container = engine.container();
            controller.pressEvents().filter(event -> event.state() !=
                    ControllerBasic.PressState.RELEASE).forEach(event -> {
                switch (event.key()) {
                    case KEY_A:
                        data.selectAll();
                        break;
                    case KEY_C:
                        data.copy().ifPresent(container::clipboardCopy);
                        break;
                    case KEY_X:
                        data.cut().ifPresent(container::clipboardCopy);
                        break;
                    case KEY_V:
                        String paste = container.clipboardPaste();
                        if (paste != null) {
                            if (!multiline) {
                                paste = REPLACE.matcher(paste).replaceAll("");
                            }
                            data.paste(paste);
                        }
                        break;
                }
                changed.a = true;
            });
        } else {
            controller.typeEvents().forEach(event -> {
                char character = event.character();
                if (!Character.isISOControl(character)) {
                    data.insert(character);
                    changed.a = true;
                }
            });
            controller.pressEvents().filter(event -> event.state() !=
                    ControllerBasic.PressState.RELEASE).forEach(event -> {
                switch (event.key()) {
                    case KEY_LEFT:
                        data.left(shift);
                        break;
                    case KEY_RIGHT:
                        data.right(shift);
                        break;
                    case KEY_ENTER:
                        if (multiline) {
                            data.insert('\n');
                        }
                        break;
                    case KEY_BACKSPACE:
                        if (data.selectionStart >= 0) {
                            data.deleteSelection();
                        } else {
                            if (data.cursor > 0) {
                                data.text.deleteCharAt(data.cursor - 1);
                                data.cursor--;
                            }
                        }
                        break;
                    case KEY_DELETE:
                        if (data.selectionStart >= 0) {
                            data.deleteSelection();
                        } else {
                            if (data.cursor < data.text.length()) {
                                data.text.deleteCharAt(data.cursor);
                            }
                        }
                        break;
                }
                changed.a = true;
            });
        }
        if (changed.a) {
            if (data.selectionStart == data.selectionEnd) {
                data.selectionStart = -1;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean captureCursor() {
        return false;
    }
}
