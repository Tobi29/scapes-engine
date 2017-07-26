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

package org.tobi29.scapes.engine.utils.io.filesystem.io.internal

import org.threeten.bp.Instant
import org.tobi29.scapes.engine.utils.io.*
import org.tobi29.scapes.engine.utils.io.filesystem.*
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.net.URI

internal object IOFileUtilImpl : FileUtilImpl {
    override fun path(path: String) = path(File(path))

    override fun path(file: File): FilePath = FilePathImpl(file)

    override fun channel(path: FilePath,
                         options: Array<out OpenOption>,
                         attributes: Array<out FileAttribute>): FileChannel {
        var openRead = false
        var openWrite = false
        var openCreate = false
        var openCreateNew = false
        var openTruncateExisting = false
        var linkNofollow = false
        for (option in options) {
            when (option) {
                OPEN_READ -> openRead = true
                OPEN_WRITE -> openWrite = true
                OPEN_CREATE -> openCreate = true
                OPEN_CREATE_NEW -> {
                    openCreate = true
                    openCreateNew = true
                }
                OPEN_TRUNCATE_EXISTING -> openTruncateExisting = true
                LINK_NOFOLLOW -> linkNofollow = true
            }
        }
        if (linkNofollow) {
            throw UnsupportedOperationException(
                    "LINK_NOFOLLOW is not supported on java.io")
        }
        val file = path.toFile()
        if (!openCreate || openCreateNew) {
            val exists = file.exists()
            if (!openCreate && !exists) {
                throw NoSuchFileException(path)
            } else if (openCreateNew && exists) {
                throw FileAlreadyExistsException(path)
            }
        }
        val mode = if (openWrite) {
            "rw"
        } else if (openRead) {
            "r"
        } else {
            ""
        }
        try {
            val channel = RandomAccessFile(path.toFile(), mode).channel
            if (openTruncateExisting) {
                channel.truncate(0L)
            }
            return channel
        } catch (e: FileNotFoundException) {
            throw NoSuchFileException(path)
        }
    }

    override fun createFile(path: FilePath,
                            vararg attributes: FileAttribute): FilePath {
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(
                    "Attributes are not supported on java.io")
        }
        path.toFile().let { file ->
            if (!file.createNewFile()) {
                throw FileAlreadyExistsException(file)
            }
        }
        return path
    }

    override fun createDirectory(path: FilePath,
                                 vararg attributes: FileAttribute): FilePath {
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(
                    "Attributes are not supported on java.io")
        }
        if (!path.toFile().mkdir()) {
            throw FileSystemException(path,
                    reason = "Failed to create directory")
        }
        return path
    }

    override fun createDirectories(path: FilePath,
                                   vararg attributes: FileAttribute): FilePath {
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(
                    "Attributes are not supported on java.io")
        }
        val file = path.toFile()
        if (!file.mkdirs() && !file.isDirectory) {
            throw FileSystemException(path,
                    reason = "Failed to create directories")
        }
        return path
    }

    override fun delete(path: FilePath) {
        path.toFile().delete()
    }

    override fun deleteIfExists(path: FilePath): Boolean {
        val file = path.toFile()
        if (file.exists()) {
            file.delete()
            return true
        }
        return false
    }

    override fun deleteDir(path: FilePath) {
        deleteDir(path.toFile())
    }

    override fun exists(path: FilePath,
                        vararg options: LinkOption): Boolean {
        val nofollow = options.contains(LINK_NOFOLLOW)
        if (nofollow) {
            throw UnsupportedOperationException(
                    "LINK_NOFOLLOW is not supported on java.io")
        }
        return path.toFile().exists()
    }

    override fun isRegularFile(path: FilePath,
                               vararg options: LinkOption): Boolean {
        val nofollow = options.contains(LINK_NOFOLLOW)
        if (nofollow) {
            throw UnsupportedOperationException(
                    "LINK_NOFOLLOW is not supported on java.io")
        }
        return path.toFile().isFile
    }

    override fun isDirectory(path: FilePath,
                             vararg options: LinkOption): Boolean {
        val nofollow = options.contains(LINK_NOFOLLOW)
        if (nofollow) {
            throw UnsupportedOperationException(
                    "LINK_NOFOLLOW is not supported on java.io")
        }
        return path.toFile().isDirectory
    }

    override fun isHidden(path: FilePath): Boolean {
        return path.toFile().isHidden
    }

    override fun isNotHidden(path: FilePath): Boolean {
        return !isHidden(path)
    }

    override fun createTempFile(prefix: String,
                                suffix: String,
                                vararg attributes: FileAttribute): FilePath {
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(
                    "Attributes are not supported on java.io")
        }
        return path(kotlin.io.createTempFile(prefix, suffix))
    }

    override fun createTempDir(prefix: String,
                               vararg attributes: FileAttribute): FilePath {
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(
                    "Attributes are not supported on java.io")
        }
        return path(kotlin.io.createTempDir(prefix))
    }

    override fun copy(source: FilePath,
                      target: FilePath): FilePath {
        channel(source, options = arrayOf(OPEN_READ)).use { channelIn ->
            channel(target, options = arrayOf(OPEN_WRITE, OPEN_CREATE,
                    OPEN_TRUNCATE_EXISTING)).use { channelOut ->
                val buffer = ByteBuffer(1024)
                while (true) {
                    val read = channelIn.read(buffer)
                    if (read == -1) {
                        break
                    }
                    buffer.flip()
                    channelOut.write(buffer)
                    buffer.compact()
                }
            }
        }
        return target
    }

    override fun move(source: FilePath,
                      target: FilePath): FilePath {
        source.toFile().renameTo(target.toFile())
        return target
    }

    override fun <R> list(path: FilePath,
                          consumer: (Sequence<FilePath>) -> R): R {
        return consumer(list(path).asSequence())
    }

    override fun list(path: FilePath): List<FilePath> {
        return path.toFile().listFiles()?.map { path(it) } ?: emptyList()
    }

    override fun <R> listRecursive(path: FilePath,
                                   consumer: (Sequence<FilePath>) -> R): R {
        return consumer(listRecursive(path).asSequence())
    }

    override fun listRecursive(path: FilePath): List<FilePath> {
        val files = ArrayList<FilePath>()
        listRecursive(path.toFile(), files)
        return files
    }

    override fun setLastModifiedTime(path: FilePath,
                                     value: Instant) {
        path.toFile().setLastModified(value.toEpochMilli())
    }

    override fun getLastModifiedTime(path: FilePath): Instant {
        return Instant.ofEpochMilli(path.toFile().lastModified())
    }

    private data class FilePathImpl(val file: File) : FilePath {
        override fun compareTo(other: FilePath): Int {
            return file.compareTo(other.toFile())
        }

        override fun toString(): String {
            return file.toString()
        }

        override fun toUri(): URI {
            return file.toURI()
        }

        override fun toFile(): File = file

        override fun normalize(): FilePath {
            return path(file.normalize())
        }

        override fun resolve(other: String): FilePath {
            return path(File(file, other))
        }

        override fun resolve(other: FilePath): FilePath {
            return resolve(other.toString())
        }

        override fun startsWith(other: String): Boolean {
            return file.startsWith(other)
        }

        override fun startsWith(other: FilePath): Boolean {
            return file.startsWith(other.toFile())
        }

        override fun relativize(other: FilePath): FilePath? {
            return file.relativeToOrNull(other.toFile())?.let { path(it) }
        }

        override val fileName get() = path(File(file.name))

        override val parent get() = file.parentFile?.let { path(it) }

        override fun toAbsolutePath(): FilePath {
            return path(file.absoluteFile)
        }

        override fun exists(): Boolean {
            return file.exists()
        }

        override fun channel(): ReadableByteChannel {
            return channel(this, options = arrayOf(OPEN_READ))
        }

        override fun <R> read(reader: (ReadableByteStream) -> R): R {
            channel().use {
                return reader(BufferedReadChannelStream(it))
            }
        }

        override fun mimeType(): String {
            return read { detectMime(it, file.toString()) }
        }
    }

    private fun deleteDir(file: File) {
        // TODO: Add loop check
        val files = file.listFiles()
        if (files != null) {
            for (child in files) {
                if (child.isFile) {
                    child.delete()
                } else if (child.isDirectory) {
                    deleteDir(child)
                }
            }
        }
        file.delete()
    }

    private fun listRecursive(file: File,
                              list: MutableList<FilePath>) {
        val files = file.listFiles()
        if (files != null) {
            for (child in files) {
                list.add(path(child))
                if (child.isDirectory) {
                    listRecursive(child, list)
                }
            }
        }
    }
}
