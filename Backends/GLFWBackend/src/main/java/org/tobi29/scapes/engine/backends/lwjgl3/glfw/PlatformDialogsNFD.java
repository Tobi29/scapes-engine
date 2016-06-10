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
package org.tobi29.scapes.engine.backends.lwjgl3.glfw;

import java8.util.Optional;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Platform;
import org.lwjgl.util.nfd.NFDPathSet;
import org.lwjgl.util.nfd.NativeFileDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.Container;
import org.tobi29.scapes.engine.gui.GuiController;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.io.IOBiConsumer;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath;
import org.tobi29.scapes.engine.utils.io.filesystem.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlatformDialogsNFD implements PlatformDialogs {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PlatformDialogsNFD.class);
    private static final Pattern WILDCARD = Pattern.compile("\\*\\.(.*)");

    private static String filter(Pair<String, String>[] extensions) {
        String[] filters = Streams.of(extensions).map(Pair::a).map(filter -> {
            Matcher matcher = WILDCARD.matcher(filter);
            StringBuilder builder = new StringBuilder(filter.length());
            while (matcher.find()) {
                builder.append(matcher.group(1));
            }
            return builder.toString();
        }).toArray(String[]::new);
        return String.join(",", (CharSequence[]) filters);
    }

    private static List<String> single(String filter) {
        PointerBuffer buffer = MemoryUtil.memAllocPointer(1);
        try {
            int result = NativeFileDialog.NFD_OpenDialog(filter, null, buffer);
            switch (result) {
                case NativeFileDialog.NFD_OKAY:
                    List<String> path =
                            Collections.singletonList(buffer.getStringUTF8(0));
                    NativeFileDialog.nNFD_Free(buffer.get(0));
                    return path;
                case NativeFileDialog.NFD_CANCEL:
                    break;
                case NativeFileDialog.NFD_ERROR:
                    LOGGER.warn("NFD Error: {}",
                            NativeFileDialog.NFD_GetError());
                    break;
                default:
                    throw new IllegalStateException(
                            "Unknown dialog result: " + result);
            }
        } finally {
            MemoryUtil.memFree(buffer);
        }
        return Collections.emptyList();
    }

    private static List<String> multi(String filter) {
        try (NFDPathSet pathSet = NFDPathSet.calloc()) {
            int result = NativeFileDialog
                    .NFD_OpenDialogMultiple(filter, null, pathSet);
            switch (result) {
                case NativeFileDialog.NFD_OKAY:
                    long count = NativeFileDialog.NFD_PathSet_GetCount(pathSet);
                    try {
                        // If someone manages this, I *think* we can consider
                        // them to have their own problems besides this...
                        // Also, this would probably run out of memory way
                        // earlier.
                        if (count > Integer.MAX_VALUE) {
                            throw new IllegalStateException(
                                    "User selected too many files: " + count);
                        }
                        List<String> paths = new ArrayList<>((int) count);
                        for (long i = 0; i < count; i++) {
                            paths.add(NativeFileDialog
                                    .NFD_PathSet_GetPath(pathSet, i));
                        }
                        return paths;
                    } finally {
                        NativeFileDialog.NFD_PathSet_Free(pathSet);
                    }
                case NativeFileDialog.NFD_CANCEL:
                    break;
                case NativeFileDialog.NFD_ERROR:
                    LOGGER.warn("NFD Error: {}",
                            NativeFileDialog.NFD_GetError());
                    break;
                default:
                    throw new IllegalStateException(
                            "Unknown dialog result: " + result);
            }
        }
        return Collections.emptyList();
    }

    private static Optional<String> save(String filter) {
        PointerBuffer savePath = MemoryUtil.memAllocPointer(1);
        try {
            int result =
                    NativeFileDialog.NFD_SaveDialog(filter, null, savePath);
            switch (result) {
                case NativeFileDialog.NFD_OKAY:
                    String path = savePath.getStringUTF8(0);
                    NativeFileDialog.nNFD_Free(savePath.get(0));
                    return Optional.of(path);
                case NativeFileDialog.NFD_CANCEL:
                    break;
                case NativeFileDialog.NFD_ERROR:
                    LOGGER.warn("NFD Error: {}",
                            NativeFileDialog.NFD_GetError());
                    break;
                default:
                    throw new IllegalStateException(
                            "Unknown dialog result: " + result);
            }
        } finally {
            MemoryUtil.memFree(savePath);
        }
        return Optional.empty();
    }

    @Override
    public void openFileDialog(long window, Pair<String, String>[] extensions,
            String title, boolean multiple,
            IOBiConsumer<String, ReadableByteStream> result)
            throws IOException {
        if (window != 0) {
            GLFW.glfwIconifyWindow(window);
        }
        try {
            String filter = filter(extensions);
            List<String> paths;
            if (multiple) {
                paths = multi(filter);
            } else {
                paths = single(filter);
            }
            for (String filePath : paths) {
                FilePath path = FileUtil.path(filePath).toAbsolutePath();
                FileUtil.read(path, stream -> result
                        .accept(String.valueOf(path.getFileName()), stream));
            }
        } finally {
            if (window != 0) {
                GLFW.glfwRestoreWindow(window);
            }
        }
    }

    @Override
    public Optional<FilePath> saveFileDialog(long window,
            Pair<String, String>[] extensions, String title) {
        if (window != 0) {
            GLFW.glfwIconifyWindow(window);
        }
        try {
            String filter = filter(extensions);
            return save(filter).map(FileUtil::path);
        } finally {
            if (window != 0) {
                GLFW.glfwRestoreWindow(window);
            }
        }
    }

    @Override
    public void message(long window, Container.MessageType messageType,
            String title, String message) {
        // Cannot implement
    }

    @Override
    public void dialog(long window, String title,
            GuiController.TextFieldData text, boolean multiline) {
        // Cannot implement
    }

    @SuppressWarnings("UseOfProcessBuilder")
    @Override
    public void openFile(long window, FilePath path) {
        String[] command;
        String pathStr = String.valueOf(path.toAbsolutePath());
        switch (Platform.get()) {
            case LINUX:
                command = new String[]{"xdg-open", pathStr};
                break;
            case MACOSX:
                command = new String[]{"open", pathStr};
                break;
            case WINDOWS:
                command = new String[]{"cmd.exe", "/c", "start", pathStr};
                break;
            default:
                return;
        }
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            processBuilder.start();
        } catch (IOException e) {
            LOGGER.warn("Failed to open file: {}", e.toString());
        }
    }
}
