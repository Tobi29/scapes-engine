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

package org.tobi29.scapes.engine.opengl;

import org.tobi29.scapes.engine.gui.GlyphRenderer;
import org.tobi29.scapes.engine.input.ControllerDefault;
import org.tobi29.scapes.engine.input.ControllerJoystick;
import org.tobi29.scapes.engine.openal.OpenAL;
import org.tobi29.scapes.engine.utils.DesktopException;
import org.tobi29.scapes.engine.utils.Pair;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public interface Container {
    int containerWidth();

    int containerHeight();

    int contentWidth();

    int contentHeight();

    boolean contentResized();

    void setMouseGrabbed(boolean value);

    void updateContainer();

    GL gl();

    OpenAL al();

    ControllerDefault controller();

    Collection<ControllerJoystick> joysticks();

    boolean joysticksChanged();

    boolean loadFont(String asset);

    GlyphRenderer createGlyphRenderer(String fontName, int size);

    void run() throws DesktopException;

    void stop();

    Path[] openFileDialog(Pair<String, String>[] extensions, String title,
            boolean multiple);

    Optional<Path> saveFileDialog(Pair<String, String>[] extensions,
            String title);

    void message(MessageType messageType, String title, String message);

    void openFile(Path path);

    enum MessageType {
        ERROR,
        INFORMATION,
        WARNING,
        QUESTION,
        PLAIN
    }
}
