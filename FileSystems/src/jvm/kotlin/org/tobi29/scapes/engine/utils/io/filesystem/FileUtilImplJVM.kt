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
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.io.WritableByteStream
import java.io.File

interface FileUtilImpl {
    fun path(path: String): FilePath

    fun path(file: File): FilePath

    fun <R> read(path: FilePath,
                 read: (ReadableByteStream) -> R): R

    fun <R> write(path: FilePath,
                  write: (WritableByteStream) -> R): R

    fun createFile(path: FilePath,
                   vararg attributes: FileAttribute<*>): FilePath

    fun createDirectory(path: FilePath,
                        vararg attributes: FileAttribute<*>): FilePath

    fun createDirectories(path: FilePath,
                          vararg attributes: FileAttribute<*>): FilePath

    fun delete(path: FilePath)

    fun deleteIfExists(path: FilePath): Boolean

    fun deleteDir(path: FilePath)

    fun exists(path: FilePath,
               vararg options: LinkOption): Boolean

    fun isRegularFile(path: FilePath,
                      vararg options: LinkOption): Boolean

    fun isDirectory(path: FilePath,
                    vararg options: LinkOption): Boolean

    fun isHidden(path: FilePath): Boolean

    fun isNotHidden(path: FilePath): Boolean

    fun createTempFile(prefix: String,
                       suffix: String,
                       vararg attributes: FileAttribute<*>): FilePath

    fun createTempDir(prefix: String,
                      vararg attributes: FileAttribute<*>): FilePath

    fun copy(source: FilePath,
             target: FilePath): FilePath

    fun move(source: FilePath,
             target: FilePath): FilePath

    fun <R> list(path: FilePath,
                 consumer: (Sequence<FilePath>) -> R): R

    fun list(path: FilePath): List<FilePath>

    fun <R> listRecursive(path: FilePath,
                          consumer: (Sequence<FilePath>) -> R): R

    fun listRecursive(path: FilePath): List<FilePath>

    fun setLastModifiedTime(path: FilePath,
                            value: Instant)

    fun getLastModifiedTime(path: FilePath): Instant

    fun <R> tempChannel(path: FilePath,
                        consumer: (FileChannel) -> R): R
}
