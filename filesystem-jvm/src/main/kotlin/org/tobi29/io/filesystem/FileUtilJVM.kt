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

package org.tobi29.io.filesystem

import org.tobi29.io.filesystem.spi.FileSystemProvider
import org.tobi29.stdex.UnsupportedJVMException
import org.tobi29.utils.InstantNanos
import org.tobi29.utils.spiLoad
import org.tobi29.utils.spiLoadFirst
import java.io.File

private object FileUtil {
    val i = spiLoadFirst(
            spiLoad<FileSystemProvider>(
                    FileUtil::class.java.classLoader), { e ->
            // TODO: How handle logging?
    }, { it.available() })?.implementation()
            ?: throw UnsupportedJVMException(
            "No filesystem implementation available")
}

actual fun path(path: String): FilePath {
    return FileUtil.i.path(path)
}

fun path(path: File): FilePath {
    return FileUtil.i.path(path)
}

actual internal fun channelImpl(path: FilePath,
                                options: Array<out OpenOption>,
                                attributes: Array<out FileAttribute>) =
        FileUtil.i.channel(path, options, attributes)

// TODO: @Throws(IOException::class)
actual fun createFile(path: FilePath,
                      vararg attributes: FileAttribute): FilePath {
    return FileUtil.i.createFile(path, *attributes)
}

// TODO: @Throws(IOException::class)
actual fun createDirectory(path: FilePath,
                           vararg attributes: FileAttribute): FilePath {
    return FileUtil.i.createDirectory(path, *attributes)
}

// TODO: @Throws(IOException::class)
actual fun createDirectories(path: FilePath,
                             vararg attributes: FileAttribute): FilePath {
    return FileUtil.i.createDirectories(path, *attributes)
}

// TODO: @Throws(IOException::class)
actual fun delete(path: FilePath) {
    FileUtil.i.delete(path)
}

// TODO: @Throws(IOException::class)
actual fun deleteIfExists(path: FilePath): Boolean {
    return FileUtil.i.deleteIfExists(path)
}

actual fun metadata(path: FilePath,
                    vararg options: LinkOption): Array<FileMetadata> {
    return FileUtil.i.metadata(path, *options)
}

actual fun attributes(path: FilePath,
                      vararg options: LinkOption): Array<FileAttribute> {
    return FileUtil.i.attributes(path, *options)
}

// TODO: @Throws(IOException::class)
actual fun createTempFile(prefix: String,
                          suffix: String,
                          vararg attributes: FileAttribute): FilePath {
    return FileUtil.i.createTempFile(prefix, suffix, *attributes)
}

// TODO: @Throws(IOException::class)
actual fun createTempDir(prefix: String,
                         vararg attributes: FileAttribute): FilePath {
    return FileUtil.i.createTempDir(prefix, *attributes)
}

// TODO: @Throws(IOException::class)
actual fun copy(source: FilePath,
                target: FilePath): FilePath {
    return FileUtil.i.copy(source, target)
}

// TODO: @Throws(IOException::class)
actual fun move(source: FilePath,
                target: FilePath): FilePath {
    return FileUtil.i.move(source, target)
}

// TODO: @Throws(IOException::class)
actual fun directoryStream(path: FilePath): DirectoryStream {
    return FileUtil.i.directoryStream(path)
}

// TODO: @Throws(IOException::class)
actual fun setLastModifiedTime(path: FilePath,
                               value: InstantNanos) {
    FileUtil.i.setLastModifiedTime(path, value)
}

// TODO: @Throws(IOException::class)
actual fun getLastModifiedTime(path: FilePath): InstantNanos {
    return FileUtil.i.getLastModifiedTime(path)
}
