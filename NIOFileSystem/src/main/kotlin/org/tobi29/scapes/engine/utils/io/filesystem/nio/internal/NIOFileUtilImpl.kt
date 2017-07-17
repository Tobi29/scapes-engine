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

import org.threeten.bp.Instant
import org.tobi29.scapes.engine.utils.io.*
import org.tobi29.scapes.engine.utils.io.filesystem.*
import org.tobi29.scapes.engine.utils.io.filesystem.LinkOption
import org.tobi29.scapes.engine.utils.io.filesystem.OpenOption
import java.io.File
import java.net.URI
import java.nio.file.*
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
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
                         attributes: Array<out FileAttribute<*>>): FileChannel =
            FileChannel.open(toPath(path), options.toNIOSet(),
                    *attributes.toNIO())

    override fun createFile(path: FilePath,
                            vararg attributes: FileAttribute<*>): FilePath {
        try {
            return path(Files.createFile(toPath(path), *attributes.toNIO()))
        } catch (e: java.nio.file.FileAlreadyExistsException) {
            throw FileAlreadyExistsException(path, reason = e.reason)
        }
    }

    override fun createDirectory(path: FilePath,
                                 vararg attributes: FileAttribute<*>): FilePath {
        return path(Files.createDirectory(toPath(path), *attributes.toNIO()))
    }

    override fun createDirectories(path: FilePath,
                                   vararg attributes: FileAttribute<*>): FilePath {
        return path(Files.createDirectories(toPath(path), *attributes.toNIO()))
    }

    override fun delete(path: FilePath) {
        Files.delete(toPath(path))
    }

    override fun deleteIfExists(path: FilePath): Boolean {
        return Files.deleteIfExists(toPath(path))
    }

    override fun deleteDir(path: FilePath) {
        deleteDir(toPath(path))
    }

    override fun exists(path: FilePath,
                        vararg options: LinkOption): Boolean {
        return Files.exists(toPath(path), *options.toNIO())
    }

    override fun isRegularFile(path: FilePath,
                               vararg options: LinkOption): Boolean {
        return Files.isRegularFile(toPath(path), *options.toNIO())
    }

    override fun isDirectory(path: FilePath,
                             vararg options: LinkOption): Boolean {
        return Files.isDirectory(toPath(path), *options.toNIO())
    }

    override fun isHidden(path: FilePath): Boolean {
        try {
            return Files.isHidden(toPath(path))
        } catch (e: IOException) {
            return false
        }

    }

    override fun isNotHidden(path: FilePath): Boolean {
        return !isHidden(path)
    }

    override fun createTempFile(prefix: String,
                                suffix: String,
                                vararg attributes: FileAttribute<*>): FilePath {
        return path(Files.createTempFile(prefix, suffix, *attributes.toNIO()))
    }

    override fun createTempDir(prefix: String,
                               vararg attributes: FileAttribute<*>): FilePath {
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

    override fun <R> list(path: FilePath,
                          consumer: (Sequence<FilePath>) -> R): R {
        Files.newDirectoryStream(toPath(path)).use { stream ->
            return consumer(stream.asSequence().map { path(it) })
        }
    }

    override fun list(path: FilePath): List<FilePath> {
        val files = ArrayList<FilePath>()
        list(path) { it.forEach { files.add(it) } }
        return files
    }

    override fun <R> listRecursive(path: FilePath,
                                   consumer: (Sequence<FilePath>) -> R): R {
        return consumer(listRecursive(path).asSequence())
    }

    override fun listRecursive(path: FilePath): List<FilePath> {
        val files = ArrayList<FilePath>()
        Files.walkFileTree(toPath(path),
                EnumSet.of(FileVisitOption.FOLLOW_LINKS), Int.MAX_VALUE,
                object : SimpleFileVisitor<Path>() {
                    override fun preVisitDirectory(dir: Path,
                                                   attrs: BasicFileAttributes): FileVisitResult {
                        files.add(path(dir))
                        return FileVisitResult.CONTINUE
                    }

                    override fun visitFile(file: Path,
                                           attrs: BasicFileAttributes): FileVisitResult {
                        files.add(path(file))
                        return FileVisitResult.CONTINUE
                    }
                })
        return files
    }

    override fun setLastModifiedTime(path: FilePath,
                                     value: Instant) {
        Files.setLastModifiedTime(toPath(path),
                FileTime.fromMillis(value.toEpochMilli()))
    }

    override fun getLastModifiedTime(path: FilePath): Instant {
        return Instant.ofEpochMilli(
                Files.getLastModifiedTime(toPath(path)).toMillis())
    }

    private data class FilePathImpl(val path: Path) : FilePath {
        override fun compareTo(other: FilePath): Int {
            return path.compareTo(toPath(other))
        }

        override fun toString(): String {
            return path.toString()
        }

        override fun toUri(): URI {
            return path.toUri()
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

    private fun Array<out FileAttribute<*>>.toNIO() =
            Array(size) { this[it].toNIO() }

    private fun FileAttribute<*>.toNIO(): java.nio.file.attribute.FileAttribute<*> =
            when (this) {
                else -> throw IllegalArgumentException(
                        "Unsupported attribute: $this")
            }
}
