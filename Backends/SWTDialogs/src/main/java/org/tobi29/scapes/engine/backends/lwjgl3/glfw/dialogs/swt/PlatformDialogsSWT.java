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
package org.tobi29.scapes.engine.backends.lwjgl3.glfw.dialogs.swt;

import java8.util.Optional;
import java8.util.function.Consumer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.lwjgl.glfw.GLFW;
import org.tobi29.scapes.engine.Container;
import org.tobi29.scapes.engine.backends.lwjgl3.glfw.PlatformDialogs;
import org.tobi29.scapes.engine.gui.GuiController;
import org.tobi29.scapes.engine.swt.util.widgets.InputDialog;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.io.IOBiConsumer;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath;
import org.tobi29.scapes.engine.utils.io.filesystem.FileUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PlatformDialogsSWT implements PlatformDialogs {
    private final String name;

    public PlatformDialogsSWT(String name) {
        this.name = name;
    }

    @Override
    public void openFileDialog(long window, Pair<String, String>[] extensions,
            String title, boolean multiple,
            IOBiConsumer<String, ReadableByteStream> result)
            throws IOException {
        String[] filterExtensions = new String[extensions.length];
        String[] filterNames = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            Pair<String, String> extension = extensions[i];
            filterExtensions[i] = extension.a;
            filterNames[i] = extension.b;
        }
        AtomicBoolean successful = new AtomicBoolean();
        AtomicReference<String> filterPath = new AtomicReference<>();
        AtomicReference<String[]> fileNames = new AtomicReference<>();
        shell(window, shell -> {
            int style = SWT.OPEN | SWT.APPLICATION_MODAL;
            if (multiple) {
                style |= SWT.MULTI;
            }
            FileDialog fileDialog = new FileDialog(shell, style);
            fileDialog.setText(title);
            fileDialog.setFilterExtensions(filterExtensions);
            fileDialog.setFilterNames(filterNames);
            String str = fileDialog.open();
            if (str != null) {
                successful.set(true);
                filterPath.set(fileDialog.getFilterPath());
                fileNames.set(fileDialog.getFileNames());
            }
        });
        if (!successful.get()) {
            return;
        }
        for (String fileName : fileNames.get()) {
            FilePath path = FileUtil.path(filterPath.get()).resolve(fileName)
                    .toAbsolutePath();
            FileUtil.read(path, stream -> result
                    .accept(String.valueOf(path.getFileName()), stream));
        }
    }

    @Override
    public Optional<FilePath> saveFileDialog(long window,
            Pair<String, String>[] extensions, String title) {
        String[] filterExtensions = new String[extensions.length];
        String[] filterNames = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            Pair<String, String> extension = extensions[i];
            filterExtensions[i] = extension.a;
            filterNames[i] = extension.b;
        }
        AtomicBoolean successful = new AtomicBoolean();
        AtomicReference<String> filterPath = new AtomicReference<>();
        AtomicReference<String> fileName = new AtomicReference<>();
        shell(window, shell -> {
            FileDialog fileDialog =
                    new FileDialog(shell, SWT.SAVE | SWT.APPLICATION_MODAL);
            fileDialog.setText(title);
            fileDialog.setFilterExtensions(filterExtensions);
            fileDialog.setFilterNames(filterNames);
            String str = fileDialog.open();
            if (str != null) {
                successful.set(true);
                filterPath.set(fileDialog.getFilterPath());
                fileName.set(fileDialog.getFileName());
            }
        });
        if (!successful.get()) {
            return Optional.empty();
        }
        return Optional
                .of(FileUtil.path(filterPath.get()).resolve(fileName.get())
                        .toAbsolutePath());
    }

    @Override
    public void message(long window, Container.MessageType messageType,
            String title, String message) {
        shell(window, shell -> {
            int style = SWT.APPLICATION_MODAL;
            switch (messageType) {
                case ERROR:
                    style |= SWT.ICON_ERROR;
                    break;
                case INFORMATION:
                    style |= SWT.ICON_INFORMATION;
                    break;
                case WARNING:
                    style |= SWT.ICON_WARNING;
                    break;
                case QUESTION:
                    style |= SWT.ICON_QUESTION;
                    break;
            }
            MessageBox messageBox = new MessageBox(shell, style);
            messageBox.setText(title);
            messageBox.setMessage(message);
            messageBox.open();
        });
    }

    @Override
    public void dialog(long window, String title,
            GuiController.TextFieldData text, boolean multiline) {
        shell(window, shell -> {
            InputDialog dialog = new InputDialog(shell, title);
            int style;
            if (multiline) {
                style = SWT.BORDER | SWT.MULTI;
            } else {
                style = SWT.BORDER | SWT.SINGLE;
            }
            Text textField = dialog.add("Text", d -> new Text(d, style));
            String str = text.text.toString();
            textField.setText(str);
            textField.setSelection(str.length());
            dialog.open(() -> {
                if (text.text.length() > 0) {
                    text.text.delete(0, Integer.MAX_VALUE);
                }
                text.text.append(textField.getText());
                text.cursor = text.text.length();
            });
        });
    }

    @Override
    public void openFile(long window, FilePath path) {
        Program.launch(path.toString());
    }

    private void shell(long window, Consumer<Shell> consumer) {
        if (window != 0) {
            GLFW.glfwIconifyWindow(window);
        }
        Shell shell = createShell();
        try {
            consumer.accept(shell);
        } finally {
            disposeShell(shell);
            if (window != 0) {
                GLFW.glfwRestoreWindow(window);
            }
        }
    }

    private Shell createShell() {
        Shell shell = new Shell();
        shell.setText(name);
        // TODO: Test on MacOSX and Windows
        shell.setSize(0, 0);
        return shell;
    }

    private void disposeShell(Shell shell) {
        Display display = shell.getDisplay();
        shell.dispose();
        while (display.readAndDispatch()) {
        }
    }
}
