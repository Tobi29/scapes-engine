/*
 * Copyright 2012-2019 Tobi29
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

@file:JvmName("FileUtilJVMKt")

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
            FileUtil::class.java.classLoader
        ), { e ->
            // TODO: How handle logging?
        }, { it.available() })?.implementation()
            ?: throw UnsupportedJVMException(
                "No filesystem implementation available"
            )
}

actual fun path(path: String) = FileUtil.i.path(path)

fun path(path: File): FilePath = FileUtil.i.path(path)

actual fun channel(
    path: FilePath,
    options: Array<out OpenOption>,
    attributes: Array<out FileAttribute>
) = FileUtil.i.channel(path, options, attributes)

actual fun createFile(
    path: FilePath,
    vararg attributes: FileAttribute
): FilePath {
    return FileUtil.i.createFile(path, *attributes)
}

actual fun createDirectory(
    path: FilePath,
    vararg attributes: FileAttribute
) = FileUtil.i.createDirectory(path, *attributes)

actual fun createDirectories(
    path: FilePath,
    vararg attributes: FileAttribute
) = FileUtil.i.createDirectories(path, *attributes)

actual fun delete(path: FilePath) {
    FileUtil.i.delete(path)
}

actual fun deleteIfExists(path: FilePath) =
    FileUtil.i.deleteIfExists(path)

actual fun metadata(
    path: FilePath,
    vararg options: LinkOption
) = FileUtil.i.metadata(path, *options)

actual fun attributes(
    path: FilePath,
    vararg options: LinkOption
) = FileUtil.i.attributes(path, *options)

actual fun createTempFile(
    prefix: String,
    suffix: String,
    vararg attributes: FileAttribute
) = FileUtil.i.createTempFile(prefix, suffix, *attributes)

actual fun createTempDir(
    prefix: String,
    vararg attributes: FileAttribute
) = FileUtil.i.createTempDir(prefix, *attributes)

actual fun copy(
    source: FilePath,
    target: FilePath
) = FileUtil.i.copy(source, target)

actual fun move(
    source: FilePath,
    target: FilePath
) = FileUtil.i.move(source, target)

actual fun directoryStream(path: FilePath) =
    FileUtil.i.directoryStream(path)

actual fun setLastModifiedTime(
    path: FilePath,
    value: InstantNanos
) {
    FileUtil.i.setLastModifiedTime(path, value)
}

actual fun getLastModifiedTime(path: FilePath) =
    FileUtil.i.getLastModifiedTime(path)
