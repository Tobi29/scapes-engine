package org.tobi29.scapes.engine;

import java8.util.Optional;
import org.tobi29.scapes.engine.gui.GlyphRenderer;
import org.tobi29.scapes.engine.gui.GuiController;
import org.tobi29.scapes.engine.input.ControllerDefault;
import org.tobi29.scapes.engine.input.ControllerJoystick;
import org.tobi29.scapes.engine.input.ControllerTouch;
import org.tobi29.scapes.engine.input.FileType;
import org.tobi29.scapes.engine.opengl.GL;
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
