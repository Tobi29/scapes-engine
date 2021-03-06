/*
 * Copyright 2012-2019 Tobi29
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

import org.tobi29.io.PathT
import org.tobi29.io.Uri

expect interface FilePath : PathT<FileChannel>, Comparable<FilePath> {
    override fun toUri(): Uri

    fun normalize(): FilePath

    fun resolve(other: String): FilePath

    fun resolve(other: FilePath): FilePath

    fun startsWith(other: String): Boolean

    fun startsWith(other: FilePath): Boolean

    fun relativize(other: FilePath): FilePath?

    val fileName: FilePath?

    override val parent: FilePath?

    fun toAbsolutePath(): FilePath
}
