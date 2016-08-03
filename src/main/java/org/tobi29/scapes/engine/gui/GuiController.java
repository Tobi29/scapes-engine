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

import java8.util.Optional;
import java8.util.function.Predicate;
import java8.util.stream.Stream;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.input.ControllerBasic;
import org.tobi29.scapes.engine.input.ControllerKey;
import org.tobi29.scapes.engine.utils.ListenerManager;
import org.tobi29.scapes.engine.utils.ListenerOwner;
import org.tobi29.scapes.engine.utils.Pair;

public abstract class GuiController {
    protected final ScapesEngine engine;
    protected final ListenerManager<Predicate<ControllerKey>> pressListeners =
            new ListenerManager<>();

    protected GuiController(ScapesEngine engine) {
        this.engine = engine;
    }

    public abstract void update(double delta);

    public abstract void focusTextField(TextFieldData data, boolean multiline);

    public abstract boolean processTextField(TextFieldData data,
            boolean multiline);

    public abstract Stream<GuiCursor> cursors();

    public abstract Stream<Pair<GuiCursor, ControllerBasic.PressEvent>> clicks();

    public abstract boolean captureCursor();

    public void onPress(ListenerOwner owner,
            Predicate<ControllerKey> listener) {
        pressListeners.add(owner, listener);
    }

    protected boolean firePress(ControllerKey key) {
        return pressListeners.fireReturn(listener -> listener.test(key));
    }

    public static class TextFieldData {
        @SuppressWarnings("StringBufferField")
        public StringBuilder text = new StringBuilder(100);
        public int cursor, selectionStart = -1, selectionEnd;

        public void selectAll() {
            cursor = text.length();
            selectionStart = 0;
            selectionEnd = cursor;
        }

        public Optional<String> copy() {
            if (selectionStart >= 0) {
                return Optional
                        .of(text.substring(selectionStart, selectionEnd));
            }
            return Optional.empty();
        }

        public Optional<String> cut() {
            if (selectionStart >= 0) {
                String cut = text.substring(selectionStart, selectionEnd);
                text.delete(selectionStart, selectionEnd);
                cursor = selectionStart;
                selectionStart = -1;
                return Optional.of(cut);
            }
            return Optional.empty();
        }

        public void paste(String paste) {
            deleteSelection();
            text.insert(cursor, paste);
            cursor += paste.length();
        }

        public void deleteSelection() {
            if (selectionStart >= 0) {
                text.delete(selectionStart, selectionEnd);
                cursor = selectionStart;
                selectionStart = -1;
            }
        }

        public void left(boolean shift) {
            if (cursor > 0) {
                cursor--;
                if (shift) {
                    if (selectionStart == -1) {
                        selectionStart = cursor;
                        selectionEnd = cursor + 1;
                    } else if (cursor >= selectionStart) {
                        selectionEnd = cursor;
                    } else {
                        selectionStart = cursor;
                    }
                }
            }
            if (!shift && selectionStart >= 0) {
                selectionStart = -1;
            }
        }

        public void right(boolean shift) {
            if (cursor < text.length()) {
                cursor++;
                if (shift) {
                    if (selectionStart == -1) {
                        selectionStart = cursor - 1;
                        selectionEnd = cursor;
                    } else if (cursor <= selectionEnd) {
                        selectionStart = cursor;
                    } else {
                        selectionEnd = cursor;
                    }
                }
            }
            if (!shift && selectionStart >= 0) {
                selectionStart = -1;
            }
        }

        public void insert(char character) {
            deleteSelection();
            text.insert(cursor++, character);
        }
    }
}
