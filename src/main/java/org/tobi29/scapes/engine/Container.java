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
package org.tobi29.scapes.engine;

import java8.util.Optional;
import org.tobi29.scapes.engine.gui.GlyphRenderer;
import org.tobi29.scapes.engine.gui.GuiController;
import org.tobi29.scapes.engine.input.ControllerDefault;
import org.tobi29.scapes.engine.input.ControllerJoystick;
import org.tobi29.scapes.engine.input.ControllerTouch;
import org.tobi29.scapes.engine.input.FileType;
import org.tobi29.scapes.engine.graphics.GL;
import org.tobi29.scapes.engine.sound.SoundSystem;
import org.tobi29.scapes.engine.utils.DesktopException;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.io.IOBiConsumer;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

public interface Container {
    FormFactor formFactor();

    int containerWidth();

    int containerHeight();

    int contentWidth();

    int contentHeight();

    boolean contentResized();

    void setMouseGrabbed(boolean value);

    void updateContainer();

    void update(double delta);

    GL gl();

    SoundSystem sound();

    Optional<ControllerDefault> controller();

    Collection<ControllerJoystick> joysticks();

    boolean joysticksChanged();

    Optional<ControllerTouch> touch();

    Optional<String> loadFont(String asset);

    GlyphRenderer createGlyphRenderer(String fontName, int size);

    ByteBuffer allocate(int capacity);

    void run() throws DesktopException;

    void stop();

    void clipboardCopy(String value);

    String clipboardPaste();

    void openFileDialog(FileType type, String title, boolean multiple,
            IOBiConsumer<String, ReadableByteStream> result) throws IOException;

    Optional<FilePath> saveFileDialog(Pair<String, String>[] extensions,
            String title);

    void message(MessageType messageType, String title, String message);

    void dialog(String title, GuiController.TextFieldData text,
            boolean multiline);

    void openFile(FilePath path);

    enum MessageType {
        ERROR,
        INFORMATION,
        WARNING,
        QUESTION,
        PLAIN
    }

    enum FormFactor {
        DESKTOP,
        PHONE
    }
}
