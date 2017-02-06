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
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.io.WritableByteStream
import org.tobi29.scapes.engine.utils.io.filesystem.spi.FileSystemProvider
import java.io.IOException
import java.nio.channels.FileChannel
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

fun path(path: String): FilePath {
    return IMPL.path(path)
}

fun read(path: FilePath): ReadSource {
    return IMPL.read(path)
}

@Throws(IOException::class)
fun <R> read(path: FilePath,
             read: (ReadableByteStream) -> R): R {
    return IMPL.read(path, read)
}

@Throws(IOException::class)
fun <R> write(path: FilePath,
              write: (WritableByteStream) -> R): R {
    return IMPL.write(path, write)
}

@Throws(IOException::class)
fun createDirectories(path: FilePath): FilePath {
    return IMPL.createDirectories(path)
}

@Throws(IOException::class)
fun delete(path: FilePath) {
    IMPL.delete(path)
}

@Throws(IOException::class)
fun deleteIfExists(path: FilePath): Boolean {
    return IMPL.deleteIfExists(path)
}

@Throws(IOException::class)
fun deleteDir(path: FilePath) {
    IMPL.deleteDir(path)
}

fun exists(path: FilePath): Boolean {
    return IMPL.exists(path)
}

fun isRegularFile(path: FilePath): Boolean {
    return IMPL.isRegularFile(path)
}

fun isDirectory(path: FilePath): Boolean {
    return IMPL.isDirectory(path)
}

fun isHidden(path: FilePath): Boolean {
    return IMPL.isHidden(path)
}

fun isNotHidden(path: FilePath): Boolean {
    return !isHidden(path)
}

@Throws(IOException::class)
fun createTempFile(prefix: String,
                   suffix: String): FilePath {
    return IMPL.createTempFile(prefix, suffix)
}

@Throws(IOException::class)
fun createTempDir(prefix: String): FilePath {
    return IMPL.createTempDir(prefix)
}

@Throws(IOException::class)
fun copy(source: FilePath,
         target: FilePath): FilePath {
    return IMPL.copy(source, target)
}

@Throws(IOException::class)
fun move(source: FilePath,
         target: FilePath): FilePath {
    return IMPL.move(source, target)
}

@Throws(IOException::class)
fun list(path: FilePath): List<FilePath> {
    return list(path) { toList() }
}

@Throws(IOException::class)
fun <R> list(path: FilePath,
             consumer: Sequence<FilePath>.() -> R): R {
    return IMPL.list(path) { consumer(it.asSequence()) }
}

@Throws(IOException::class)
fun list(path: FilePath,
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

@Throws(IOException::class)
fun listRecursive(path: FilePath): List<FilePath> {
    return listRecursive(path) { toList() }
}

@Throws(IOException::class)
fun <R> listRecursive(path: FilePath,
                      consumer: Sequence<FilePath>.() -> R): R {
    return IMPL.listRecursive(path) { consumer(it.asSequence()) }
}

@Throws(IOException::class)
fun listRecursive(path: FilePath,
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

@Throws(IOException::class)
fun setLastModifiedTime(path: FilePath,
                        value: Instant) {
    IMPL.setLastModifiedTime(path, value)
}

@Throws(IOException::class)
fun getLastModifiedTime(path: FilePath): Instant {
    return IMPL.getLastModifiedTime(path)
}

@Throws(IOException::class)
fun <R> tempChannel(path: FilePath,
                    consumer: (FileChannel) -> R): R {
    return IMPL.tempChannel(path, consumer)
}

@Throws(IOException::class)
fun zipFile(path: FilePath): ZipFile {
    return IMPL.zipFile(path)
}
