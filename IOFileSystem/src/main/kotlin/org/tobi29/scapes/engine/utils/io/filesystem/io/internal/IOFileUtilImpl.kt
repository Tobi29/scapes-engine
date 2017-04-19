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

import org.apache.tika.Tika
import org.threeten.bp.Instant
import org.tobi29.scapes.engine.utils.io.*
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath
import org.tobi29.scapes.engine.utils.io.filesystem.FileUtilImpl
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.URI
import java.nio.channels.FileChannel
import java.util.zip.ZipFile

internal object IOFileUtilImpl : FileUtilImpl {
    override fun path(path: String): FilePath {
        return path(File(path))
    }

    override fun read(path: FilePath): ReadSource {
        return read(toFile(path))
    }

    override fun <R> read(path: FilePath,
                          read: (ReadableByteStream) -> R): R {
        return read(toFile(path), read)
    }

    override fun <R> write(path: FilePath,
                           write: (WritableByteStream) -> R): R {
        return write(toFile(path), write)
    }

    override fun createFile(path: FilePath): FilePath {
        toFile(path).let { file ->
            if (!file.createNewFile()) {
                throw FileAlreadyExistsException(file)
            }
        }
        return path
    }

    override fun createDirectories(path: FilePath): FilePath {
        toFile(path).mkdirs()
        return path
    }

    override fun delete(path: FilePath) {
        toFile(path).delete()
    }

    override fun deleteIfExists(path: FilePath): Boolean {
        val file = toFile(path)
        if (file.exists()) {
            file.delete()
            return true
        }
        return false
    }

    override fun deleteDir(path: FilePath) {
        deleteDir(toFile(path))
    }

    override fun exists(path: FilePath): Boolean {
        return toFile(path).exists()
    }

    override fun isRegularFile(path: FilePath): Boolean {
        return toFile(path).isFile
    }

    override fun isDirectory(path: FilePath): Boolean {
        return toFile(path).isDirectory
    }

    override fun isHidden(path: FilePath): Boolean {
        return toFile(path).isHidden
    }

    override fun isNotHidden(path: FilePath): Boolean {
        return !isHidden(path)
    }

    override fun createTempFile(prefix: String,
                                suffix: String): FilePath {
        return path(kotlin.io.createTempFile(prefix, suffix))
    }

    override fun createTempDir(prefix: String): FilePath {
        return path(kotlin.io.createTempDir(prefix))
    }

    override fun copy(source: FilePath,
                      target: FilePath): FilePath {
        read(toFile(source), { input ->
            write(toFile(target),
                    { output ->
                        process(input, { output.put(it) })
                    })
        })
        return target
    }

    override fun move(source: FilePath,
                      target: FilePath): FilePath {
        toFile(source).renameTo(toFile(target))
        return target
    }

    override fun <R> list(path: FilePath,
                          consumer: (Sequence<FilePath>) -> R): R {
        return consumer(list(path).asSequence())
    }

    override fun list(path: FilePath): List<FilePath> {
        return toFile(path).listFiles()?.map { path(it) } ?: emptyList()
    }

    override fun <R> listRecursive(path: FilePath,
                                   consumer: (Sequence<FilePath>) -> R): R {
        return consumer(listRecursive(path).asSequence())
    }

    override fun listRecursive(path: FilePath): List<FilePath> {
        val files = ArrayList<FilePath>()
        listRecursive(toFile(path), files)
        return files
    }

    override fun setLastModifiedTime(path: FilePath,
                                     value: Instant) {
        toFile(path).setLastModified(value.toEpochMilli())
    }

    override fun getLastModifiedTime(path: FilePath): Instant {
        return Instant.ofEpochMilli(toFile(path).lastModified())
    }

    override fun zipFile(path: FilePath): ZipFile {
        return ZipFile(toFile(path))
    }

    override fun <R> tempChannel(path: FilePath,
                                 consumer: (FileChannel) -> R): R {
        val file = toFile(path)
        return try {
            channelRW(file).use { consumer(it) }
        } finally {
            file.delete()
        }
    }

    private data class FilePathImpl(val file: File) : FilePath {
        override fun compareTo(other: FilePath): Int {
            return file.compareTo(toFile(other))
        }

        override fun toString(): String {
            return file.toString()
        }

        override fun toUri(): URI {
            return file.toURI()
        }

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
            return file.startsWith(toFile(other))
        }

        override fun relativize(other: FilePath): FilePath? {
            return file.relativeToOrNull(toFile(other))?.let { path(it) }
        }

        override val fileName: FilePath
            get() = path(File(file.name))

        override fun toAbsolutePath(): FilePath {
            return path(file.absoluteFile)
        }
    }

    private fun toFile(path: FilePath): File {
        if (path is FilePathImpl) {
            return path.file
        }
        return File(path.toUri())
    }

    private fun <R> read(file: File,
                         read: (ReadableByteStream) -> R): R {
        channelR(file).use { channel ->
            return read(BufferedReadChannelStream(channel))
        }
    }

    private fun <R> write(file: File,
                          write: (WritableByteStream) -> R): R {
        channelDRW(file).use { channel ->
            val stream = BufferedWriteChannelStream(channel)
            val r = write(stream)
            stream.flush()
            return r
        }
    }

    private fun path(file: File): FilePath {
        return FilePathImpl(file)
    }

    private fun read(file: File): ReadSource {
        return object : ReadSource {
            override fun exists(): Boolean {
                return file.exists()
            }

            override fun readIO(): InputStream {
                return FileInputStream(file)
            }

            override fun channel(): ReadableByteChannel {
                return channelR(file)
            }

            override fun <R> read(reader: (ReadableByteStream) -> R): R {
                return read(file, reader)
            }

            override fun mimeType(): String {
                readIO().use { streamIn ->
                    return Tika().detect(streamIn, file.toString())
                }
            }
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

    private fun channelR(file: File): FileChannel {
        return RandomAccessFile(file, "r").channel
    }

    private fun channelRW(file: File): FileChannel {
        return RandomAccessFile(file, "rw").channel
    }

    private fun channelDRW(file: File): FileChannel {
        file.delete()
        return RandomAccessFile(file, "rw").channel
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
