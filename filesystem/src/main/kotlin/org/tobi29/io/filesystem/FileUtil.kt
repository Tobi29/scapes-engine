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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.io.filesystem

import org.tobi29.io.*
import org.tobi29.stdex.Throws
import org.tobi29.utils.InstantNanos
import org.tobi29.utils.findMap

expect fun path(path: String): FilePath

@Throws(IOException::class)
expect fun channel(
    path: FilePath,
    options: Array<out OpenOption> = emptyArray(),
    attributes: Array<out FileAttribute> = emptyArray()
): FileChannel

@Throws(IOException::class)
fun <R> read(
    path: FilePath,
    read: (ReadableByteStream) -> R
): R = channel(path, options = arrayOf(OPEN_READ)).use {
    read(BufferedReadChannelStream(it))
}

@Throws(IOException::class)
fun <R> write(
    path: FilePath,
    write: (WritableByteStream) -> R
): R = channel(
    path, options = arrayOf(
        OPEN_WRITE, OPEN_CREATE,
        OPEN_TRUNCATE_EXISTING
    )
).use {
    val stream = BufferedWriteChannelStream(it)
    val result = write(stream)
    stream.flush()
    result
}

fun exists(
    path: FilePath,
    vararg options: LinkOption
): Boolean = try {
    metadata(path, *options)
    true
} catch (e: IOException) {
    false
}

fun notExists(
    path: FilePath,
    vararg options: LinkOption
): Boolean = try {
    metadata(path, *options)
    false
} catch (e: NoSuchFileException) {
    true
} catch (e: IOException) {
    false
}

fun isRegularFile(
    path: FilePath,
    vararg options: LinkOption
): Boolean = try {
    metadata(path, *options).findMap<FileBasicMetadata>()
        ?.type == FileType.TYPE_REGULAR_FILE
} catch (e: IOException) {
    false
}

fun isDirectory(
    path: FilePath,
    vararg options: LinkOption
): Boolean = try {
    metadata(path, *options).findMap<FileBasicMetadata>()
        ?.type == FileType.TYPE_DIRECTORY
} catch (e: IOException) {
    false
}

fun isHidden(
    path: FilePath,
    vararg options: LinkOption
): Boolean = try {
    metadata(path, *options).findMap<FileVisibility>()?.hidden ?: false
} catch (e: IOException) {
    false
}

inline fun isNotHidden(
    path: FilePath,
    vararg options: LinkOption
): Boolean = isHidden(path, *options)

fun fileUID(
    path: FilePath,
    vararg options: LinkOption
): Any? = try {
    metadata(path, *options).findMap<FileBasicMetadata>()?.uid
} catch (e: IOException) {
    null
}

@Throws(IOException::class)
fun deleteDir(path: FilePath) {
    walk(path, order = FileTreeOrder.POST_ORDER).use {
        it.forEach { delete(it) }
    }
}

@Throws(IOException::class)
fun list(path: FilePath): List<FilePath> =
    directoryStream(path).use { it.asSequence().toList() }

@Throws(IOException::class)
fun <R> list(
    path: FilePath,
    consumer: Sequence<FilePath>.() -> R
): R = directoryStream(path).use { consumer(it.asSequence()) }

@Throws(IOException::class)
fun listRecursive(path: FilePath): List<FilePath> =
    walk(path).use { it.asSequence().toList() }

@Throws(IOException::class)
fun <R> listRecursive(
    path: FilePath,
    consumer: Sequence<FilePath>.() -> R
): R = walk(path).use { consumer(it.asSequence()) }

@Throws(IOException::class)
expect fun createFile(
    path: FilePath,
    vararg attributes: FileAttribute
): FilePath

@Throws(IOException::class)
expect fun createDirectory(
    path: FilePath,
    vararg attributes: FileAttribute
): FilePath

@Throws(IOException::class)
expect fun createDirectories(
    path: FilePath,
    vararg attributes: FileAttribute
): FilePath

@Throws(IOException::class)
expect fun delete(path: FilePath)

@Throws(IOException::class)
expect fun deleteIfExists(path: FilePath): Boolean

@Throws(IOException::class)
expect fun metadata(
    path: FilePath,
    vararg options: LinkOption
): Array<FileMetadata>

@Throws(IOException::class)
expect fun attributes(
    path: FilePath,
    vararg options: LinkOption
): Array<FileAttribute>

@Throws(IOException::class)
expect fun createTempFile(
    prefix: String,
    suffix: String,
    vararg attributes: FileAttribute
): FilePath

@Throws(IOException::class)
expect fun createTempDir(
    prefix: String,
    vararg attributes: FileAttribute
): FilePath

@Throws(IOException::class)
expect fun copy(
    source: FilePath,
    target: FilePath
): FilePath

@Throws(IOException::class)
expect fun move(
    source: FilePath,
    target: FilePath
): FilePath

@Throws(IOException::class)
expect fun directoryStream(path: FilePath): DirectoryStream

@Throws(IOException::class)
expect fun setLastModifiedTime(
    path: FilePath,
    value: InstantNanos
)

@Throws(IOException::class)
expect fun getLastModifiedTime(path: FilePath): InstantNanos

interface DirectoryStream : Iterator<FilePath>,
    AutoCloseable

interface FileOption
interface OpenOption : FileOption
interface CopyOption : FileOption
interface LinkOption : OpenOption,
    CopyOption

val OPEN_READ = object : OpenOption {}
val OPEN_WRITE = object : OpenOption {}
val OPEN_CREATE = object : OpenOption {}
val OPEN_CREATE_NEW = object : OpenOption {}
val OPEN_TRUNCATE_EXISTING = object : OpenOption {}
val LINK_NOFOLLOW = object : LinkOption {}
