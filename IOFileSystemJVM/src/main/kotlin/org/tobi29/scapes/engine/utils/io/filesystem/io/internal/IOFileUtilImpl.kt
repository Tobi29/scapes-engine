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

import org.tobi29.scapes.engine.utils.*
import org.tobi29.scapes.engine.utils.io.*
import org.tobi29.scapes.engine.utils.io.filesystem.*
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.util.*

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
        return try {
            val channel = RandomAccessFile(path.toFile(), mode).channel
            if (openTruncateExisting) {
                channel.truncate(0L)
            }
            channel.toChannel()
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
        if (!deleteIfExists(path)) {
            throw FileSystemException(path, reason = "Failed to delete")
        }
    }

    override fun deleteIfExists(path: FilePath): Boolean {
        return path.toFile().delete()
    }

    override fun metadata(path: FilePath,
                          vararg options: LinkOption): Array<FileMetadata> {
        val list = ArrayList<FileMetadata>()
        val nofollow = options.contains(LINK_NOFOLLOW)
        if (nofollow) {
            throw UnsupportedOperationException(
                    "LINK_NOFOLLOW is not supported on java.io")
        }
        val file = path.toFile()
        if (!file.exists()) {
            throw NoSuchFileException(file)
        }
        list.add(FileBasicMetadata(file.fileType(), file.length(),
                file.canonicalPath))
        list.add(FileVisibility(file.isHidden))
        return list.toTypedArray()
    }

    override fun attributes(path: FilePath,
                            vararg options: LinkOption): Array<FileAttribute> =
            metadata(path, *options).asSequence()
                    .filterIsInstance<FileAttribute>().toArray()

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
                val buffer = ByteArray(1024).view
                while (true) {
                    val read = channelIn.read(buffer)
                    if (read < 0) break
                    var write = buffer.slice(0, read)
                    while (write.size > 0) {
                        val wrote = channelOut.write(write)
                        if (wrote < 0) throw EndOfStreamException()
                        write = write.slice(wrote)
                    }
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

    override fun directoryStream(path: FilePath): DirectoryStream {
        val iterator = (path.toFile().listFiles() ?: emptyArray<File>())
                .asSequence().map { path(it) }.iterator()
        return object : DirectoryStream, Iterator<FilePath> by iterator {
            override fun close() {}
        }
    }

    override fun setLastModifiedTime(path: FilePath,
                                     value: InstantNanos) {
        path.toFile().setLastModified(value.millis.toLongClamped())
    }

    override fun getLastModifiedTime(path: FilePath): InstantNanos {
        return Instant.fromMillis(path.toFile().lastModified())
    }

    private data class FilePathImpl(val file: File) : FilePath {
        override fun compareTo(other: FilePath): Int {
            return file.compareTo(other.toFile())
        }

        override fun toString(): String {
            return file.toString()
        }

        override fun toUri(): Uri {
            return file.toURI().toUri()
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

        override fun channel(): ReadableByteChannel {
            return channel(this, options = arrayOf(OPEN_READ))
        }

        override suspend fun <R> readAsync(reader: suspend (ReadableByteStream) -> R): R {
            channel().use {
                return reader(BufferedReadChannelStream(it))
            }
        }

        override suspend fun mimeType(): String {
            return readAsync { detectMime(it, file.toString()) }
        }
    }

    private fun File.fileType() =
            if (isFile) FileType.TYPE_REGULAR_FILE
            else if (isDirectory) FileType.TYPE_DIRECTORY
            else FileType.TYPE_UNKNOWN
}
