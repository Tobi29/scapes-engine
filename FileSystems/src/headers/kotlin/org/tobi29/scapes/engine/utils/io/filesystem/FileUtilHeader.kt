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

header fun path(path: String): FilePath

// TODO: @Throws(IOException::class)
header internal fun channelImpl(path: FilePath,
                                options: Array<out OpenOption>,
                                attributes: Array<out FileAttribute>): FileChannel

// TODO: @Throws(IOException::class)
header fun createFile(path: FilePath,
                      vararg attributes: FileAttribute): FilePath

// TODO: @Throws(IOException::class)
header fun createDirectory(path: FilePath,
                           vararg attributes: FileAttribute): FilePath

// TODO: @Throws(IOException::class)
header fun createDirectories(path: FilePath,
                             vararg attributes: FileAttribute): FilePath

// TODO: @Throws(IOException::class)
header fun delete(path: FilePath)

// TODO: @Throws(IOException::class)
header fun deleteIfExists(path: FilePath): Boolean

// TODO: @Throws(IOException::class)
header fun metadata(path: FilePath,
                    vararg options: LinkOption): Array<FileMetadata>

// TODO: @Throws(IOException::class)
header fun attributes(path: FilePath,
                      vararg options: LinkOption): Array<FileAttribute>

// TODO: @Throws(IOException::class)
header fun createTempFile(prefix: String,
                          suffix: String,
                          vararg attributes: FileAttribute): FilePath

// TODO: @Throws(IOException::class)
header fun createTempDir(prefix: String,
                         vararg attributes: FileAttribute): FilePath

// TODO: @Throws(IOException::class)
header fun copy(source: FilePath,
                target: FilePath): FilePath

// TODO: @Throws(IOException::class)
header fun move(source: FilePath,
                target: FilePath): FilePath

// TODO: @Throws(IOException::class)
header fun directoryStream(path: FilePath): DirectoryStream
