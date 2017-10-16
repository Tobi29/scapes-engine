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

package org.tobi29.scapes.engine.utils.io.filesystem.nio.internal

import org.tobi29.scapes.engine.utils.*
import org.tobi29.scapes.engine.utils.io.*
import org.tobi29.scapes.engine.utils.io.filesystem.*
import org.tobi29.scapes.engine.utils.io.filesystem.DirectoryStream
import org.tobi29.scapes.engine.utils.io.filesystem.FileAttribute
import org.tobi29.scapes.engine.utils.io.filesystem.LinkOption
import org.tobi29.scapes.engine.utils.io.filesystem.OpenOption
import java.io.File
import java.nio.file.*
import java.nio.file.Path
import java.nio.file.attribute.*
import java.util.*

internal object NIOFileUtilImpl : FileUtilImpl {
    override fun path(path: String): FilePath {
        return path(Paths.get(path))
    }

    override fun path(file: File): FilePath {
        return path(file.toPath())
    }

    override fun channel(path: FilePath,
                         options: Array<out OpenOption>,
                         attributes: Array<out FileAttribute>): FileChannel =
            java.nio.channels.FileChannel.open(toPath(path), options.toNIOSet(),
                    *attributes.toNIO()).toChannel()

    override fun createFile(path: FilePath,
                            vararg attributes: FileAttribute): FilePath {
        try {
            return path(Files.createFile(toPath(path), *attributes.toNIO()))
        } catch (e: java.nio.file.FileAlreadyExistsException) {
            throw FileAlreadyExistsException(path, reason = e.reason)
        }
    }

    override fun createDirectory(path: FilePath,
                                 vararg attributes: FileAttribute): FilePath {
        return path(Files.createDirectory(toPath(path), *attributes.toNIO()))
    }

    override fun createDirectories(path: FilePath,
                                   vararg attributes: FileAttribute): FilePath {
        return path(Files.createDirectories(toPath(path), *attributes.toNIO()))
    }

    override fun delete(path: FilePath) {
        Files.delete(toPath(path))
    }

    override fun deleteIfExists(path: FilePath): Boolean {
        return Files.deleteIfExists(toPath(path))
    }

    override fun metadata(path: FilePath,
                          vararg options: LinkOption): Array<FileMetadata> {
        val list = ArrayList<FileMetadata>()
        val optionsNIO = options.toNIO()
        val posix = try {
            Files.readAttributes(toPath(path),
                    PosixFileAttributes::class.java,
                    *optionsNIO)
        } catch (e: UnsupportedOperationException) {
            null
        }
        val dos = try {
            Files.readAttributes(toPath(path),
                    DosFileAttributes::class.java,
                    *optionsNIO)
        } catch (e: UnsupportedOperationException) {
            null
        }
        val basic = posix ?: dos ?: Files.readAttributes(toPath(path),
                BasicFileAttributes::class.java,
                *optionsNIO)

        list.add(FileBasicMetadata(basic.fileType(), basic.size(),
                basic.fileKey()))
        list.add(FileModificationTime(basic.lastModifiedTime().toMillis()))
        if (posix != null) {
            list.add(posix.permissions().toUnixPermissionMode())
        }
        if (dos != null) {
            list.add(FileVisibility(dos.isHidden))
        } else {
            list.add(FileVisibility(path.fileName?.startsWith(".") ?: false))
        }
        return list.toTypedArray()
    }

    override fun attributes(path: FilePath,
                            vararg options: LinkOption): Array<FileAttribute> =
            metadata(path, *options).asSequence()
                    .filterMap<FileAttribute>().toArray()

    override fun createTempFile(prefix: String,
                                suffix: String,
                                vararg attributes: FileAttribute): FilePath {
        return path(Files.createTempFile(prefix, suffix, *attributes.toNIO()))
    }

    override fun createTempDir(prefix: String,
                               vararg attributes: FileAttribute): FilePath {
        return path(Files.createTempDirectory(prefix, *attributes.toNIO()))
    }

    override fun copy(source: FilePath,
                      target: FilePath): FilePath {
        return path(Files.copy(toPath(source), toPath(target)))
    }

    override fun move(source: FilePath,
                      target: FilePath): FilePath {
        return path(Files.move(toPath(source), toPath(target)))
    }

    override fun directoryStream(path: FilePath): DirectoryStream {
        val stream = Files.newDirectoryStream(toPath(path))
        val iterator = stream.asSequence().map { path(it) }.iterator()
        return object : DirectoryStream, Iterator<FilePath> by iterator {
            override fun close() = stream.close()
        }
    }

    override fun setLastModifiedTime(path: FilePath,
                                     value: InstantNanos) {
        Files.setLastModifiedTime(toPath(path),
                FileTime.fromMillis(value.millis.toLongClamped()))
    }

    override fun getLastModifiedTime(path: FilePath): InstantNanos {
        return Instant.fromMillis(
                Files.getLastModifiedTime(toPath(path)).toMillis())
    }

    private data class FilePathImpl(val path: Path) : FilePath {
        override fun compareTo(other: FilePath): Int {
            return path.compareTo(toPath(other))
        }

        override fun toString(): String {
            return path.toString()
        }

        override fun toUri(): Uri {
            return path.toUri().toUri()
        }

        override fun toFile(): File = path.toFile()

        override fun normalize(): FilePath {
            return path(path.normalize())
        }

        override fun resolve(other: String): FilePath {
            return path(path.resolve(other))
        }

        override fun resolve(other: FilePath): FilePath {
            return path(path.resolve(toPath(other)))
        }

        override fun startsWith(other: String): Boolean {
            return path.startsWith(other)
        }

        override fun startsWith(other: FilePath): Boolean {
            return path.startsWith(toPath(other))
        }

        override fun relativize(other: FilePath): FilePath? {
            try {
                return path(path.relativize(toPath(other)))
            } catch (e: IllegalArgumentException) {
                return null
            }
        }

        override val fileName get() = path.fileName?.let { path(it) }

        override val parent get() = path.parent?.let { path(it) }

        override fun toAbsolutePath(): FilePath {
            return path(path.toAbsolutePath())
        }

        override fun exists(): Boolean {
            return Files.exists(path)
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
            return read { detectMime(it, path.toString()) }
        }
    }

    private fun toPath(path: FilePath): Path {
        if (path is FilePathImpl) {
            return path.path
        }
        return path.toFile().toPath()
    }

    private fun path(path: Path): FilePath {
        return FilePathImpl(path)
    }

    private fun deleteDir(path: Path) {
        Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path,
                                   attrs: BasicFileAttributes): FileVisitResult {
                Files.delete(file)
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path,
                                            exc: IOException?): FileVisitResult {
                Files.delete(dir)
                return FileVisitResult.CONTINUE
            }
        })
    }

    private fun Array<out LinkOption>.toNIO() =
            Array(size) { this[it].toNIO() }

    private fun LinkOption.toNIO(): java.nio.file.LinkOption =
            when (this) {
                LINK_NOFOLLOW -> java.nio.file.LinkOption.NOFOLLOW_LINKS
                else -> throw IllegalArgumentException(
                        "Unsupported option: $this")
            }

    private fun Array<out OpenOption>.toNIO() =
            Array(size) { this[it].toNIO() }

    private fun Array<out OpenOption>.toNIOSet() =
            HashSet<java.nio.file.OpenOption>(size).also { set ->
                forEach { set.add(it.toNIO()) }
            }

    private fun OpenOption.toNIO(): java.nio.file.OpenOption =
            when (this) {
                OPEN_READ -> StandardOpenOption.READ
                OPEN_WRITE -> StandardOpenOption.WRITE
                OPEN_CREATE -> StandardOpenOption.CREATE
                OPEN_CREATE_NEW -> StandardOpenOption.CREATE_NEW
                OPEN_TRUNCATE_EXISTING -> StandardOpenOption.TRUNCATE_EXISTING
                is LinkOption -> toNIO()
                else -> throw IllegalArgumentException(
                        "Unsupported option: $this")
            }

    private fun Array<out FileAttribute>.toNIO() =
            Array(size) { this[it].toNIO() }

    private fun FileAttribute.toNIO(): java.nio.file.attribute.FileAttribute<*> =
            when (this) {
                is UnixPermissionMode -> toNIO()
                else -> throw IllegalArgumentException(
                        "Unsupported attribute: $this")
            }

    private fun UnixPermissionMode.toNIO(
    ): java.nio.file.attribute.FileAttribute<Set<java.nio.file.attribute.PosixFilePermission>> {
        val value = HashSet<java.nio.file.attribute.PosixFilePermission>().apply {
            if (owner.isExecute) add(
                    java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE)
            if (owner.isWrite) add(
                    java.nio.file.attribute.PosixFilePermission.OWNER_WRITE)
            if (owner.isRead) add(
                    java.nio.file.attribute.PosixFilePermission.OWNER_READ)
            if (group.isExecute) add(
                    java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE)
            if (group.isWrite) add(
                    java.nio.file.attribute.PosixFilePermission.GROUP_WRITE)
            if (group.isRead) add(
                    java.nio.file.attribute.PosixFilePermission.GROUP_READ)
            if (others.isExecute) add(
                    java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE)
            if (others.isWrite) add(
                    java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE)
            if (others.isRead) add(
                    java.nio.file.attribute.PosixFilePermission.OTHERS_READ)
        }
        return object : java.nio.file.attribute.FileAttribute<Set<PosixFilePermission>> {
            override fun name(): String {
                return "posix:permissions"
            }

            override fun value(): Set<PosixFilePermission> {
                return value.readOnly()
            }
        }
    }

    private fun Set<java.nio.file.attribute.PosixFilePermission>.toUnixPermissionMode(): UnixPermissionMode {
        var owner: Int = 0
        var group: Int = 0
        var others: Int = 0
        for (element in this) {
            when (element) {
                PosixFilePermission.OWNER_EXECUTE -> owner = owner.setAt(0)
                PosixFilePermission.OWNER_WRITE -> owner = owner.setAt(1)
                PosixFilePermission.OWNER_READ -> owner = owner.setAt(2)
                PosixFilePermission.GROUP_EXECUTE -> group = group.setAt(0)
                PosixFilePermission.GROUP_WRITE -> group = group.setAt(1)
                PosixFilePermission.GROUP_READ -> group = group.setAt(2)
                PosixFilePermission.OTHERS_EXECUTE -> others = others.setAt(0)
                PosixFilePermission.OTHERS_WRITE -> others = others.setAt(1)
                PosixFilePermission.OTHERS_READ -> others = others.setAt(2)
            }
        }
        return UnixPermissionMode(owner.toUnixPermissionModeLevel(),
                group.toUnixPermissionModeLevel(),
                others.toUnixPermissionModeLevel())
    }

    private fun BasicFileAttributes.fileType() =
            if (isRegularFile) FileType.TYPE_REGULAR_FILE
            else if (isDirectory) FileType.TYPE_DIRECTORY
            else if (isSymbolicLink) FileType.TYPE_SYMLINK
            else FileType.TYPE_UNKNOWN
}
