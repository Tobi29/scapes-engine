/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.backends.lwjgl3.glfw

import mu.KLogging
import org.lwjgl.PointerBuffer
import org.lwjgl.glfw.GLFW
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.Platform
import org.lwjgl.util.tinyfd.TinyFileDialogs
import org.tobi29.scapes.engine.Container
import org.tobi29.scapes.engine.gui.GuiController
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath
import org.tobi29.scapes.engine.utils.io.filesystem.get
import org.tobi29.scapes.engine.utils.io.filesystem.read
import org.tobi29.scapes.engine.utils.io.use
import java.io.IOException

object PlatformDialogs : KLogging() {
    private inline fun <R> filter(extensions: Array<Pair<String, String>>,
                                  block: (PointerBuffer) -> R): R {
        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocPointer(extensions.size)
            extensions.forEach { extension ->
                val filter = extension.first
                buffer.put(stack.UTF8(filter))
            }
            buffer.flip()
            return block(buffer)
        }
    }

    fun openFileDialog(window: Long,
                       extensions: Array<Pair<String, String>>,
                       multiple: Boolean,
                       result: (String, ReadableByteStream) -> Unit) {
        iconify(window) {
            filter(extensions) { filters ->
                TinyFileDialogs.tinyfd_openFileDialog("Open File...", "",
                        filters, null, multiple)?.split(
                        '|')?.forEach { filePath ->
                    val path = get(filePath).toAbsolutePath()
                    read(path) { stream ->
                        result(path.fileName.toString(), stream)
                    }
                }
            }
        }
    }

    fun saveFileDialog(window: Long,
                       extensions: Array<Pair<String, String>>): FilePath? {
        iconify(window) {
            filter(extensions) { filters ->
                TinyFileDialogs.tinyfd_saveFileDialog("Save File...", "",
                        filters, null)?.let { filePath ->
                    return get(filePath).toAbsolutePath()
                }
            }
        }
        return null
    }

    fun message(window: Long,
                messageType: Container.MessageType,
                title: String,
                message: String) {
        iconify(window) {
            val type = when (messageType) {
                Container.MessageType.PLAIN -> "plain"
                Container.MessageType.INFORMATION -> "plain"
                Container.MessageType.WARNING -> "warning"
                Container.MessageType.ERROR -> "error"
                Container.MessageType.QUESTION -> "question"
                else -> throw IllegalArgumentException(
                        "Unknown message type: $messageType")
            }
            TinyFileDialogs.tinyfd_messageBox(title, message, "ok", type, true)
        }
    }

    fun dialog(window: Long,
               title: String,
               text: GuiController.TextFieldData,
               multiline: Boolean) {
        iconify(window) {
            TinyFileDialogs.tinyfd_inputBox(title, "",
                    text.text)?.let { editText ->
                if (text.text.isNotEmpty()) {
                    text.text.delete(0, Int.MAX_VALUE)
                }
                text.text.append(editText)
            }
        }
    }

    fun openFile(path: FilePath) {
        val command: Array<String>
        val pathStr = path.toAbsolutePath().toString()
        when (Platform.get()) {
            Platform.LINUX -> command = arrayOf("xdg-open", pathStr)
            Platform.MACOSX -> command = arrayOf("open", pathStr)
            Platform.WINDOWS -> command = arrayOf("cmd.exe", "/c", "start",
                    pathStr)
            else -> return
        }
        val processBuilder = ProcessBuilder(*command)
        try {
            processBuilder.start()
        } catch (e: IOException) {
            logger.warn { "Failed to open file: $e" }
        }
    }

    private inline fun <R> iconify(window: Long,
                                   block: () -> R): R {
        if (window != 0L) {
            GLFW.glfwIconifyWindow(window)
        }
        try {
            return block()
        } finally {
            if (window != 0L) {
                GLFW.glfwRestoreWindow(window)
            }
        }
    }
}
