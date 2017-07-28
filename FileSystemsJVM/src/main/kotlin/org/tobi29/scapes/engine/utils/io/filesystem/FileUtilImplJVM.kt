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

import org.threeten.bp.Instant
import java.io.File

interface FileUtilImpl {
    fun path(path: String): FilePath

    fun path(file: File): FilePath

    fun channel(path: FilePath,
                options: Array<out OpenOption> = emptyArray(),
                attributes: Array<out FileAttribute> = emptyArray()): FileChannel

    fun createFile(path: FilePath,
                   vararg attributes: FileAttribute): FilePath

    fun createDirectory(path: FilePath,
                        vararg attributes: FileAttribute): FilePath

    fun createDirectories(path: FilePath,
                          vararg attributes: FileAttribute): FilePath

    fun delete(path: FilePath)

    fun deleteIfExists(path: FilePath): Boolean

    fun metadata(path: FilePath,
                 vararg options: LinkOption): Array<FileMetadata>

    fun attributes(path: FilePath,
                   vararg options: LinkOption): Array<FileAttribute>

    fun exists(path: FilePath,
               vararg options: LinkOption): Boolean

    fun isRegularFile(path: FilePath,
                      vararg options: LinkOption): Boolean

    fun isDirectory(path: FilePath,
                    vararg options: LinkOption): Boolean

    fun isHidden(path: FilePath): Boolean

    fun fileUID(path: FilePath): Any?

    fun createTempFile(prefix: String,
                       suffix: String,
                       vararg attributes: FileAttribute): FilePath

    fun createTempDir(prefix: String,
                      vararg attributes: FileAttribute): FilePath

    fun copy(source: FilePath,
             target: FilePath): FilePath

    fun move(source: FilePath,
             target: FilePath): FilePath

    fun directoryStream(path: FilePath): DirectoryStream

    fun setLastModifiedTime(path: FilePath,
                            value: Instant)

    fun getLastModifiedTime(path: FilePath): Instant
}
