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

public class ContainerEmulate implements Container {
    protected final Container container;

    public ContainerEmulate(Container container) {
        this.container = container;
    }

    @Override
    public FormFactor formFactor() {
        return container.formFactor();
    }

    @Override
    public int containerWidth() {
        return container.containerWidth();
    }

    @Override
    public int containerHeight() {
        return container.containerHeight();
    }

    @Override
    public int contentWidth() {
        return container.contentWidth();
    }

    @Override
    public int contentHeight() {
        return container.contentHeight();
    }

    @Override
    public boolean contentResized() {
        return container.contentResized();
    }

    @Override
    public void setMouseGrabbed(boolean value) {
        container.setMouseGrabbed(value);
    }

    @Override
    public void updateContainer() {
        container.updateContainer();
    }

    @Override
    public void update(double delta) {
        container.update(delta);
    }

    @Override
    public GL gl() {
        return container.gl();
    }

    @Override
    public SoundSystem sound() {
        return container.sound();
    }

    @Override
    public Optional<ControllerDefault> controller() {
        return container.controller();
    }

    @Override
    public Collection<ControllerJoystick> joysticks() {
        return container.joysticks();
    }

    @Override
    public boolean joysticksChanged() {
        return container.joysticksChanged();
    }

    @Override
    public Optional<ControllerTouch> touch() {
        return container.touch();
    }

    @Override
    public Optional<String> loadFont(String asset) {
        return container.loadFont(asset);
    }

    @Override
    public GlyphRenderer createGlyphRenderer(String fontName, int size) {
        return container.createGlyphRenderer(fontName, size);
    }

    @Override
    public ByteBuffer allocate(int capacity) {
        return container.allocate(capacity);
    }

    @Override
    public void run() throws DesktopException {
        container.run();
    }

    @Override
    public void stop() {
        container.stop();
    }

    @Override
    public void clipboardCopy(String value) {
        container.clipboardCopy(value);
    }

    @Override
    public String clipboardPaste() {
        return container.clipboardPaste();
    }

    @Override
    public void openFileDialog(FileType type, String title, boolean multiple,
            IOBiConsumer<String, ReadableByteStream> result)
            throws IOException {
        container.openFileDialog(type, title, multiple, result);
    }

    @Override
    public Optional<FilePath> saveFileDialog(Pair<String, String>[] extensions,
            String title) {
        return container.saveFileDialog(extensions, title);
    }

    @Override
    public void message(MessageType messageType, String title, String message) {
        container.message(messageType, title, message);
    }

    @Override
    public void dialog(String title, GuiController.TextFieldData text,
            boolean multiline) {
        container.dialog(title, text, multiline);
    }

    @Override
    public void openFile(FilePath path) {
        container.openFile(path);
    }
}
