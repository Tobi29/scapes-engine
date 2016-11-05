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

package org.tobi29.scapes.engine.backends.lwjgl3.glfw

import mu.KLogging
import org.lwjgl.glfw.GLFW
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.Platform
import org.lwjgl.util.nfd.NFDPathSet
import org.lwjgl.util.nfd.NativeFileDialog
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath
import org.tobi29.scapes.engine.utils.io.filesystem.path
import org.tobi29.scapes.engine.utils.io.filesystem.read
import org.tobi29.scapes.engine.utils.io.use
import org.tobi29.scapes.engine.utils.mapNotNull
import org.tobi29.scapes.engine.utils.stream
import org.tobi29.scapes.engine.utils.toTypedArray
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

object PlatformDialogs : KLogging() {
    private val WILDCARD = Pattern.compile("\\*\\.(.*)")

    private fun filter(extensions: Array<Pair<String, String>>): String {
        val filters = stream(*extensions).map { it.first }.map { filter ->
            val matcher = WILDCARD.matcher(filter)
            val builder = StringBuilder(filter.length)
            while (matcher.find()) {
                builder.append(matcher.group(1))
            }
            builder.toString()
        }.toTypedArray()
        return filters.joinToString(",")
    }

    private fun single(filter: String): List<String> {
        val buffer = MemoryUtil.memAllocPointer(1)
        try {
            val result = NativeFileDialog.NFD_OpenDialog(filter, null, buffer)
            when (result) {
                NativeFileDialog.NFD_OKAY -> {
                    val path = listOf(buffer.getStringUTF8(0))
                    NativeFileDialog.nNFDi_Free(buffer.get(0))
                    return path
                }
                NativeFileDialog.NFD_CANCEL -> {
                }
                NativeFileDialog.NFD_ERROR -> logger.warn { "NFD Error: ${NativeFileDialog.NFD_GetError()}" }
                else -> throw IllegalStateException(
                        "Unknown dialog result: $result")
            }
        } finally {
            MemoryUtil.memFree(buffer)
        }
        return emptyList()
    }

    private fun multi(filter: String): List<String> {
        NFDPathSet.calloc().use { pathSet ->
            val result = NativeFileDialog.NFD_OpenDialogMultiple(filter, null,
                    pathSet)
            when (result) {
                NativeFileDialog.NFD_OKAY -> {
                    val count = NativeFileDialog.NFD_PathSet_GetCount(pathSet)
                    try {
                        // If someone manages this, I *think* we can consider
                        // them to have their own problems besides this...
                        // Also, this would probably run out of memory way
                        // earlier.
                        if (count > Int.MAX_VALUE) {
                            throw IllegalStateException(
                                    "User selected too many files: " + count)
                        }
                        val paths = ArrayList<String>(count.toInt())
                        for (i in 0..count - 1) {
                            paths.add(NativeFileDialog.NFD_PathSet_GetPath(
                                    pathSet, i))
                        }
                        return paths
                    } finally {
                        NativeFileDialog.NFD_PathSet_Free(pathSet)
                    }
                }
                NativeFileDialog.NFD_CANCEL -> {
                }
                NativeFileDialog.NFD_ERROR -> logger.warn { "NFD Error: ${NativeFileDialog.NFD_GetError()}" }
                else -> throw IllegalStateException(
                        "Unknown dialog result: $result")
            }
        }
        return emptyList()
    }

    private fun save(filter: String): String? {
        val savePath = MemoryUtil.memAllocPointer(1)
        try {
            val result = NativeFileDialog.NFD_SaveDialog(filter, null, savePath)
            when (result) {
                NativeFileDialog.NFD_OKAY -> {
                    val path = savePath.getStringUTF8(0)
                    NativeFileDialog.nNFDi_Free(savePath.get(0))
                    return path
                }
                NativeFileDialog.NFD_CANCEL -> {
                }
                NativeFileDialog.NFD_ERROR -> logger.warn { "NFD Error: ${NativeFileDialog.NFD_GetError()}" }
                else -> throw IllegalStateException(
                        "Unknown dialog result: $result")
            }
        } finally {
            MemoryUtil.memFree(savePath)
        }
        return null
    }

    fun openFileDialog(window: Long,
                       extensions: Array<Pair<String, String>>,
                       multiple: Boolean,
                       result: (String, ReadableByteStream) -> Unit) {
        iconify(window) {
            val filter = filter(extensions)
            val paths: List<String>
            if (multiple) {
                paths = multi(filter)
            } else {
                paths = single(filter)
            }
            for (filePath in paths) {
                val path = path(filePath).toAbsolutePath()
                read(path) { stream ->
                    result.invoke(path.fileName.toString(), stream)
                }
            }
        }
    }

    fun saveFileDialog(window: Long,
                       extensions: Array<Pair<String, String>>): FilePath? {
        iconify(window) {
            val filter = filter(extensions)
            return save(filter)?.mapNotNull(::path)
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
