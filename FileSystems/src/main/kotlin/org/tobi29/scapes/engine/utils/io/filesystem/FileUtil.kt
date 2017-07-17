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

import org.tobi29.scapes.engine.utils.io.*

header fun path(path: String): FilePath

// TODO: @Throws(IOException::class)
fun channel(path: FilePath,
            options: Array<out OpenOption> = emptyArray(),
            attributes: Array<out FileAttribute<*>> = emptyArray()): FileChannel =
        channelImpl(path, options, attributes)

// TODO: @Throws(IOException::class)
header internal fun channelImpl(path: FilePath,
                                options: Array<out OpenOption>,
                                attributes: Array<out FileAttribute<*>>): FileChannel

// TODO: @Throws(IOException::class)
header fun createFile(path: FilePath,
                      vararg attributes: FileAttribute<*>): FilePath

// TODO: @Throws(IOException::class)
header fun createDirectory(path: FilePath,
                           vararg attributes: FileAttribute<*>): FilePath

// TODO: @Throws(IOException::class)
header fun createDirectories(path: FilePath,
                             vararg attributes: FileAttribute<*>): FilePath

// TODO: @Throws(IOException::class)
header fun delete(path: FilePath)

// TODO: @Throws(IOException::class)
header fun deleteIfExists(path: FilePath): Boolean

// TODO: @Throws(IOException::class)
header fun deleteDir(path: FilePath)

header fun exists(path: FilePath,
                  vararg options: LinkOption): Boolean

header fun isRegularFile(path: FilePath,
                         vararg options: LinkOption): Boolean

header fun isDirectory(path: FilePath,
                       vararg options: LinkOption): Boolean

header fun isHidden(path: FilePath): Boolean

inline fun isNotHidden(path: FilePath): Boolean = !isHidden(path)

// TODO: @Throws(IOException::class)
header fun createTempFile(prefix: String,
                          suffix: String,
                          vararg attributes: FileAttribute<*>): FilePath

// TODO: @Throws(IOException::class)
header fun createTempDir(prefix: String,
                         vararg attributes: FileAttribute<*>): FilePath

// TODO: @Throws(IOException::class)
header fun copy(source: FilePath,
                target: FilePath): FilePath

// TODO: @Throws(IOException::class)
header fun move(source: FilePath,
                target: FilePath): FilePath

// TODO: @Throws(IOException::class)
header fun list(path: FilePath): List<FilePath>

// TODO: @Throws(IOException::class)
header fun <R> list(path: FilePath,
                    consumer: Sequence<FilePath>.() -> R): R

// TODO: @Throws(IOException::class)
header fun list(path: FilePath,
                vararg filters: (FilePath) -> Boolean): List<FilePath>

// TODO: @Throws(IOException::class)
header fun listRecursive(path: FilePath): List<FilePath>

// TODO: @Throws(IOException::class)
header fun <R> listRecursive(path: FilePath,
                             consumer: Sequence<FilePath>.() -> R): R

// TODO: @Throws(IOException::class)
header fun listRecursive(path: FilePath,
                         vararg filters: (FilePath) -> Boolean): List<FilePath>

// TODO: @Throws(IOException::class)
fun <R> read(path: FilePath,
             read: (ReadableByteStream) -> R): R {
    channel(path, options = arrayOf(OPEN_READ)).use {
        return read(BufferedReadChannelStream(it))
    }
}

// TODO: @Throws(IOException::class)
fun <R> write(path: FilePath,
              write: (WritableByteStream) -> R): R {
    channel(path, options = arrayOf(OPEN_WRITE, OPEN_CREATE,
            OPEN_TRUNCATE_EXISTING)).use {
        val stream = BufferedWriteChannelStream(it)
        val result = write(stream)
        stream.flush()
        return result
    }
}

interface FileOption
interface OpenOption : FileOption
interface CopyOption : FileOption
interface LinkOption : OpenOption, CopyOption

val OPEN_READ = object : OpenOption {}
val OPEN_WRITE = object : OpenOption {}
val OPEN_CREATE = object : OpenOption {}
val OPEN_CREATE_NEW = object : OpenOption {}
val OPEN_TRUNCATE_EXISTING = object : OpenOption {}
val LINK_NOFOLLOW = object : LinkOption {}

interface FileAttribute<T>
