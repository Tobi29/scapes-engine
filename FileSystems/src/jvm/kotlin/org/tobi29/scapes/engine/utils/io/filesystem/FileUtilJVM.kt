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

import org.threeten.bp.Instant
import org.tobi29.scapes.engine.utils.UnsupportedJVMException
import org.tobi29.scapes.engine.utils.io.ReadSource
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.io.WritableByteStream
import org.tobi29.scapes.engine.utils.io.filesystem.spi.FileSystemProvider
import java.util.*
import java.util.zip.ZipFile

private val IMPL = loadService()

private fun loadService(): FileUtilImpl {
    for (filesystem in ServiceLoader.load(FileSystemProvider::class.java)) {
        try {
            if (filesystem.available()) {
                return filesystem.implementation()
            }
        } catch (e: ServiceConfigurationError) {
        }

    }
    throw UnsupportedJVMException(
            "No filesystem implementation available")
}

/* impl */ fun path(path: String): FilePath {
    return IMPL.path(path)
}

/* impl */ fun read(path: FilePath): ReadSource {
    return IMPL.read(path)
}

// TODO: @Throws(IOException::class)
/* impl */ fun <R> read(path: FilePath,
                        read: (ReadableByteStream) -> R): R {
    return IMPL.read(path, read)
}

// TODO: @Throws(IOException::class)
/* impl */ fun <R> write(path: FilePath,
                         write: (WritableByteStream) -> R): R {
    return IMPL.write(path, write)
}

// TODO: @Throws(IOException::class)
/* impl */ fun createFile(path: FilePath): FilePath {
    return IMPL.createFile(path)
}

// TODO: @Throws(IOException::class)
/* impl */ fun createDirectories(path: FilePath): FilePath {
    return IMPL.createDirectories(path)
}

// TODO: @Throws(IOException::class)
/* impl */ fun delete(path: FilePath) {
    IMPL.delete(path)
}

// TODO: @Throws(IOException::class)
/* impl */ fun deleteIfExists(path: FilePath): Boolean {
    return IMPL.deleteIfExists(path)
}

// TODO: @Throws(IOException::class)
/* impl */ fun deleteDir(path: FilePath) {
    IMPL.deleteDir(path)
}

/* impl */ fun exists(path: FilePath): Boolean {
    return IMPL.exists(path)
}

/* impl */ fun isRegularFile(path: FilePath): Boolean {
    return IMPL.isRegularFile(path)
}

/* impl */ fun isDirectory(path: FilePath): Boolean {
    return IMPL.isDirectory(path)
}

/* impl */ fun isHidden(path: FilePath): Boolean {
    return IMPL.isHidden(path)
}

/* impl */ fun isNotHidden(path: FilePath): Boolean {
    return !isHidden(path)
}

// TODO: @Throws(IOException::class)
/* impl */ fun createTempFile(prefix: String,
                              suffix: String): FilePath {
    return IMPL.createTempFile(prefix, suffix)
}

// TODO: @Throws(IOException::class)
/* impl */ fun createTempDir(prefix: String): FilePath {
    return IMPL.createTempDir(prefix)
}

// TODO: @Throws(IOException::class)
/* impl */ fun copy(source: FilePath,
                    target: FilePath): FilePath {
    return IMPL.copy(source, target)
}

// TODO: @Throws(IOException::class)
/* impl */ fun move(source: FilePath,
                    target: FilePath): FilePath {
    return IMPL.move(source, target)
}

// TODO: @Throws(IOException::class)
/* impl */ fun list(path: FilePath): List<FilePath> {
    return list(path) { toList() }
}

// TODO: @Throws(IOException::class)
/* impl */ fun <R> list(path: FilePath,
                        consumer: Sequence<FilePath>.() -> R): R {
    return IMPL.list(path) { consumer(it.asSequence()) }
}

// TODO: @Throws(IOException::class)
/* impl */ fun list(path: FilePath,
                    vararg filters: (FilePath) -> Boolean): List<FilePath> {
    return list(path) {
        filter {
            filters.forEach { filter ->
                if (!filter(it)) {
                    return@filter false
                }
            }
            true
        }.toList()
    }
}

// TODO: @Throws(IOException::class)
/* impl */ fun listRecursive(path: FilePath): List<FilePath> {
    return listRecursive(path) { toList() }
}

// TODO: @Throws(IOException::class)
/* impl */ fun <R> listRecursive(path: FilePath,
                                 consumer: Sequence<FilePath>.() -> R): R {
    return IMPL.listRecursive(path) { consumer(it.asSequence()) }
}

// TODO: @Throws(IOException::class)
/* impl */ fun listRecursive(path: FilePath,
                             vararg filters: (FilePath) -> Boolean): List<FilePath> {
    return listRecursive(path) {
        filter {
            filters.forEach { filter ->
                if (!filter(it)) {
                    return@filter false
                }
            }
            true
        }.toList()
    }
}

// TODO: @Throws(IOException::class)
fun setLastModifiedTime(path: FilePath,
                        value: Instant) {
    IMPL.setLastModifiedTime(path, value)
}

// TODO: @Throws(IOException::class)
fun getLastModifiedTime(path: FilePath): Instant {
    return IMPL.getLastModifiedTime(path)
}

// TODO: @Throws(IOException::class)
/* impl */ fun <R> tempChannel(path: FilePath,
                               consumer: (FileChannel) -> R): R {
    return IMPL.tempChannel(path, consumer)
}

// TODO: @Throws(IOException::class)
fun zipFile(path: FilePath): ZipFile {
    return IMPL.zipFile(path)
}
