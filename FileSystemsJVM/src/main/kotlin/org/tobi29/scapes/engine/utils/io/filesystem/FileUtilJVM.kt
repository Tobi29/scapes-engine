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

package org.tobi29.scapes.engine.utils.io.filesystem

import org.tobi29.scapes.engine.utils.InstantNanos
import org.tobi29.scapes.engine.utils.UnsupportedJVMException
import org.tobi29.scapes.engine.utils.io.filesystem.spi.FileSystemProvider
import java.io.File
import java.util.*

private val IMPL = loadService()

private fun loadService(): FileUtilImpl {
    for (filesystem in ServiceLoader.load(FileSystemProvider::class.java)) {
        try {
            if (filesystem.available()) {
                return filesystem.implementation()
            }
        } catch (e: ServiceConfigurationError) {
        }

    }
    throw UnsupportedJVMException(
            "No filesystem implementation available")
}

impl fun path(path: String): FilePath {
    return IMPL.path(path)
}

fun path(path: File): FilePath {
    return IMPL.path(path)
}

impl internal fun channelImpl(path: FilePath,
                              options: Array<out OpenOption>,
                              attributes: Array<out FileAttribute>) =
        IMPL.channel(path, options, attributes)

// TODO: @Throws(IOException::class)
impl fun createFile(path: FilePath,
                    vararg attributes: FileAttribute): FilePath {
    return IMPL.createFile(path, *attributes)
}

// TODO: @Throws(IOException::class)
impl fun createDirectory(path: FilePath,
                         vararg attributes: FileAttribute): FilePath {
    return IMPL.createDirectory(path, *attributes)
}

// TODO: @Throws(IOException::class)
impl fun createDirectories(path: FilePath,
                           vararg attributes: FileAttribute): FilePath {
    return IMPL.createDirectories(path, *attributes)
}

// TODO: @Throws(IOException::class)
impl fun delete(path: FilePath) {
    IMPL.delete(path)
}

// TODO: @Throws(IOException::class)
impl fun deleteIfExists(path: FilePath): Boolean {
    return IMPL.deleteIfExists(path)
}

impl fun metadata(path: FilePath,
                  vararg options: LinkOption): Array<FileMetadata> {
    return IMPL.metadata(path, *options)
}

impl fun attributes(path: FilePath,
                    vararg options: LinkOption): Array<FileAttribute> {
    return IMPL.attributes(path, *options)
}

// TODO: @Throws(IOException::class)
impl fun createTempFile(prefix: String,
                        suffix: String,
                        vararg attributes: FileAttribute): FilePath {
    return IMPL.createTempFile(prefix, suffix, *attributes)
}

// TODO: @Throws(IOException::class)
impl fun createTempDir(prefix: String,
                       vararg attributes: FileAttribute): FilePath {
    return IMPL.createTempDir(prefix, *attributes)
}

// TODO: @Throws(IOException::class)
impl fun copy(source: FilePath,
              target: FilePath): FilePath {
    return IMPL.copy(source, target)
}

// TODO: @Throws(IOException::class)
impl fun move(source: FilePath,
              target: FilePath): FilePath {
    return IMPL.move(source, target)
}

// TODO: @Throws(IOException::class)
impl fun directoryStream(path: FilePath): DirectoryStream {
    return IMPL.directoryStream(path)
}

// TODO: @Throws(IOException::class)
fun setLastModifiedTime(path: FilePath,
                        value: InstantNanos) {
    IMPL.setLastModifiedTime(path, value)
}

// TODO: @Throws(IOException::class)
fun getLastModifiedTime(path: FilePath): InstantNanos {
    return IMPL.getLastModifiedTime(path)
}
