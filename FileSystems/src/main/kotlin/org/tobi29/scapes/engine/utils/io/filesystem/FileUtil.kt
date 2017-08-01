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

import org.tobi29.scapes.engine.utils.findMap
import org.tobi29.scapes.engine.utils.io.*

// TODO: @Throws(IOException::class)
fun channel(path: FilePath,
            options: Array<out OpenOption> = emptyArray(),
            attributes: Array<out FileAttribute> = emptyArray()): FileChannel =
        channelImpl(path, options, attributes)

inline fun isNotHidden(path: FilePath): Boolean = !isHidden(path)

// TODO: @Throws(IOException::class)
fun <R> read(path: FilePath,
             read: (ReadableByteStream) -> R): R =
        channel(path, options = arrayOf(OPEN_READ)).use {
            read(BufferedReadChannelStream(it))
        }

// TODO: @Throws(IOException::class)
fun <R> write(path: FilePath,
              write: (WritableByteStream) -> R): R =
        channel(path, options = arrayOf(OPEN_WRITE, OPEN_CREATE,
                OPEN_TRUNCATE_EXISTING)).use {
            val stream = BufferedWriteChannelStream(it)
            val result = write(stream)
            stream.flush()
            result
        }

fun exists(path: FilePath,
           vararg options: LinkOption): Boolean =
        try {
            metadata(path, *options)
            true
        } catch (e: IOException) {
            false
        }

fun notExists(path: FilePath,
              vararg options: LinkOption): Boolean =
        try {
            metadata(path, *options)
            false
        } catch (e: NoSuchFileException) {
            true
        } catch (e: IOException) {
            false
        }

fun isRegularFile(path: FilePath,
                  vararg options: LinkOption): Boolean =
        try {
            metadata(path, *options).findMap<FileBasicMetadata>()
                    ?.type == FileType.TYPE_REGULAR_FILE
        } catch (e: IOException) {
            false
        }

fun isDirectory(path: FilePath,
                vararg options: LinkOption): Boolean =
        try {
            metadata(path, *options).findMap<FileBasicMetadata>()
                    ?.type == FileType.TYPE_DIRECTORY
        } catch (e: IOException) {
            false
        }

fun isHidden(path: FilePath,
             vararg options: LinkOption): Boolean =
        try {
            metadata(path, *options).findMap<FileVisibility>()?.hidden ?: false
        } catch (e: IOException) {
            false
        }

fun fileUID(path: FilePath,
            vararg options: LinkOption): Any? =
        try {
            metadata(path, *options).findMap<FileBasicMetadata>()?.uid
        } catch (e: IOException) {
            null
        }

// TODO: @Throws(IOException::class)
fun deleteDir(path: FilePath) {
    walk(path, order = FileTreeOrder.POST_ORDER).use {
        it.forEach { delete(it) }
    }
}

// TODO: @Throws(IOException::class)
fun list(path: FilePath): List<FilePath> =
        directoryStream(path).use { it.asSequence().toList() }

// TODO: @Throws(IOException::class)
fun <R> list(path: FilePath,
             consumer: Sequence<FilePath>.() -> R): R =
        directoryStream(path).use { consumer(it.asSequence()) }

// TODO: @Throws(IOException::class)
fun listRecursive(path: FilePath): List<FilePath> =
        walk(path).use { it.asSequence().toList() }

// TODO: @Throws(IOException::class)
fun <R> listRecursive(path: FilePath,
                      consumer: Sequence<FilePath>.() -> R): R =
        walk(path).use { consumer(it.asSequence()) }

interface DirectoryStream : Iterator<FilePath>, AutoCloseable

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
