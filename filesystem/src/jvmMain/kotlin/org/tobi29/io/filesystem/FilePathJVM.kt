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

package org.tobi29.io.filesystem

import org.tobi29.io.*
import java.io.File

actual interface FilePath : PathT<FileChannel>, Comparable<FilePath> {
    actual override fun toUri(): Uri

    override val name: String? get() = fileName?.toString()

    fun toFile(): File

    actual fun normalize(): FilePath

    actual fun resolve(other: String): FilePath

    actual fun resolve(other: FilePath): FilePath

    actual fun startsWith(other: String): Boolean

    actual fun startsWith(other: FilePath): Boolean

    actual fun relativize(other: FilePath): FilePath?

    actual val fileName: FilePath?

    actual override val parent: FilePath?

    actual fun toAbsolutePath(): FilePath

    override fun get(path: String) = resolve(path)

    override fun channel() = channel(this, options = arrayOf(OPEN_READ))

    override suspend fun <R> readAsync(reader: suspend (ReadableByteStream) -> R): R {
        channel().use {
            return reader(BufferedReadChannelStream(it))
        }
    }
}
