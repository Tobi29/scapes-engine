/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.scapes.engine.backends.glfw

import org.lwjgl.PointerBuffer
import org.lwjgl.glfw.GLFW
import org.lwjgl.util.tinyfd.TinyFileDialogs
import org.tobi29.io.IOException
import org.tobi29.io.ReadableByteStream
import org.tobi29.io.filesystem.FilePath
import org.tobi29.io.filesystem.path
import org.tobi29.io.filesystem.read
import org.tobi29.logging.KLogging
import org.tobi29.platform.PLATFORM
import org.tobi29.platform.Platform
import org.tobi29.platform.launchProcess
import org.tobi29.scapes.engine.Container
import org.tobi29.scapes.engine.backends.lwjgl3.stackFrame
import org.tobi29.scapes.engine.gui.GuiController
import org.tobi29.stdex.concurrent.withLock

actual fun openFileDialog(
    window: ContainerGLFW,
    extensions: Array<Pair<String, String>>,
    multiple: Boolean,
    result: (String, ReadableByteStream) -> Unit
) {
    iconify(window.window) {
        filter(extensions) { filters ->
            TinyFileDialogs.tinyfd_openFileDialog(
                "Open File...", "",
                filters, null, multiple
            )?.split(
                '|'
            )?.forEach { filePath ->
                val path = path(filePath).toAbsolutePath()
                read(path) { stream ->
                    result(path.fileName.toString(), stream)
                }
            }
        }
    }
}

actual fun saveFileDialog(
    window: ContainerGLFW,
    extensions: Array<Pair<String, String>>
): FilePath? {
    iconify(window.window) {
        filter(extensions) { filters ->
            TinyFileDialogs.tinyfd_saveFileDialog(
                "Save File...", "",
                filters, null
            )?.let { filePath ->
                return path(filePath).toAbsolutePath()
            }
        }
    }
    return null
}

actual fun message(
    window: ContainerGLFW,
    messageType: Container.MessageType,
    title: String,
    message: String
) {
    iconify(window.window) {
        val type = when (messageType) {
            Container.MessageType.PLAIN -> "plain"
            Container.MessageType.INFORMATION -> "plain"
            Container.MessageType.WARNING -> "warning"
            Container.MessageType.ERROR -> "error"
            Container.MessageType.QUESTION -> "question"
            else -> throw IllegalArgumentException(
                "Unknown message type: $messageType"
            )
        }
        TinyFileDialogs.tinyfd_messageBox(title, message, "ok", type, true)
    }
}

actual fun dialog(
    window: ContainerGLFW,
    title: String,
    text: GuiController.TextFieldData,
    multiline: Boolean
) {
    iconify(window.window) {
        TinyFileDialogs.tinyfd_inputBox(
            title, "",
            text.text
        )?.let { editText ->
            text.lock.withLock {
                if (text.text.isNotEmpty()) {
                    text.text.delete(0, Int.MAX_VALUE)
                }
                text.text.append(editText)
                text.dirty.set(true)
            }
        }
    }
}

actual fun openFile(path: FilePath) {
    val executable = when (PLATFORM) {
        Platform.LINUX -> "xdg-open"
        Platform.MACOS -> "open"
        Platform.WINDOWS -> "cmd.exe"
        else -> return
    }
    val pathStr = path.toAbsolutePath().toString()
    val arguments = when (PLATFORM) {
        Platform.LINUX -> arrayOf(pathStr)
        Platform.MACOS -> arrayOf(pathStr)
        Platform.WINDOWS -> arrayOf("/c", "start", pathStr)
        else -> return
    }
    try {
        launchProcess(executable, *arguments)
    } catch (e: IOException) {
        PlatformDialogs.logger.warn { "Failed to open file: $e" }
    }
}

private inline fun <R> filter(
    extensions: Array<Pair<String, String>>,
    block: (PointerBuffer) -> R
): R = stackFrame { stack ->
    val buffer = stack.mallocPointer(extensions.size)
    extensions.forEach { (filter) ->
        buffer.put(stack.UTF8(filter))
    }
    buffer.flip()
    block(buffer)
}

private inline fun <R> iconify(
    window: Long,
    block: () -> R
): R {
    if (window != 0L) {
        GLFW.glfwIconifyWindow(window)
    }
    return try {
        block()
    } finally {
        if (window != 0L) {
            GLFW.glfwRestoreWindow(window)
        }
    }
}

private object PlatformDialogs : KLogging()
