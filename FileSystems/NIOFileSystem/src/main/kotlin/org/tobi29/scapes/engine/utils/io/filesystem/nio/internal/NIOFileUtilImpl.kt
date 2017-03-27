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

import org.apache.tika.Tika
import org.threeten.bp.Instant
import org.tobi29.scapes.engine.utils.io.BufferedReadChannelStream
import org.tobi29.scapes.engine.utils.io.BufferedWriteChannelStream
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.io.WritableByteStream
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath
import org.tobi29.scapes.engine.utils.io.filesystem.FileUtilImpl
import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource
import org.tobi29.scapes.engine.utils.use
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.util.*
import java.util.zip.ZipFile

internal object NIOFileUtilImpl : FileUtilImpl {
    override fun path(path: String): FilePath {
        return path(Paths.get(path))
    }

    override fun read(path: FilePath): ReadSource {
        return read(toPath(path))
    }

    override fun <R> read(path: FilePath,
                          read: (ReadableByteStream) -> R): R {
        return read(toPath(path), read)
    }

    override fun <R> write(path: FilePath,
                           write: (WritableByteStream) -> R): R {
        return write(toPath(path), write)
    }

    override fun createFile(path: FilePath): FilePath {
        toPath(path).let { path ->
            try {
                return path(Files.createFile(path))
            } catch (e: java.nio.file.FileAlreadyExistsException) {
                throw kotlin.io.FileAlreadyExistsException(path.toFile(),
                        reason = e.reason)
            }
        }
    }

    override fun createDirectories(path: FilePath): FilePath {
        return path(Files.createDirectories(toPath(path)))
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

    override fun exists(path: FilePath): Boolean {
        return Files.exists(toPath(path))
    }

    override fun isRegularFile(path: FilePath): Boolean {
        return Files.isRegularFile(toPath(path))
    }

    override fun isDirectory(path: FilePath): Boolean {
        return Files.isDirectory(toPath(path))
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
                                suffix: String): FilePath {
        return path(Files.createTempFile(prefix, suffix))
    }

    override fun createTempDir(prefix: String): FilePath {
        return path(Files.createTempDirectory(prefix))
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

    override fun zipFile(path: FilePath): ZipFile {
        return ZipFile(toPath(path).toFile())
    }

    override fun <R> tempChannel(path: FilePath,
                                 consumer: (FileChannel) -> R): R {
        FileChannel.open(toPath(path), StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.DELETE_ON_CLOSE).use { channel ->
            return consumer(channel)
        }
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

        override val fileName: FilePath
            get() = path(path.fileName)

        override fun toAbsolutePath(): FilePath {
            return path(path.toAbsolutePath())
        }
    }

    private fun toPath(path: FilePath): Path {
        if (path is FilePathImpl) {
            return path.path
        }
        return Paths.get(path.toUri())
    }

    private fun <R> read(path: Path,
                         read: (ReadableByteStream) -> R): R {
        return read(path, read, StandardOpenOption.READ)
    }

    private fun <R> read(path: Path,
                         read: (ReadableByteStream) -> R,
                         vararg options: OpenOption): R {
        FileChannel.open(path, *options).use { channel ->
            return read(BufferedReadChannelStream(channel))
        }
    }

    private fun <R> write(path: Path,
                          write: (WritableByteStream) -> R): R {
        return write(path, write, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)
    }

    private fun <R> write(path: Path,
                          write: (WritableByteStream) -> R,
                          vararg options: OpenOption): R {
        FileChannel.open(path, *options).use { channel ->
            val stream = BufferedWriteChannelStream(channel)
            val r = write(stream)
            stream.flush()
            return r
        }
    }

    private fun path(path: Path): FilePath {
        return FilePathImpl(path)
    }

    private fun read(path: Path): ReadSource {
        return object : ReadSource {
            override fun exists(): Boolean {
                return Files.exists(path)
            }

            override fun readIO(): InputStream {
                return Files.newInputStream(path)
            }

            override fun channel(): ReadableByteChannel {
                return Files.newByteChannel(path, StandardOpenOption.READ)
            }

            override fun <R> read(reader: (ReadableByteStream) -> R): R {
                return read(path, reader)
            }

            override fun mimeType(): String {
                readIO().use { streamIn ->
                    return Tika().detect(streamIn, path.toString())
                }
            }
        }
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
}
