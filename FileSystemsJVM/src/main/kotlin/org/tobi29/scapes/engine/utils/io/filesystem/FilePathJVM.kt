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

import org.tobi29.scapes.engine.utils.io.Path
import org.tobi29.scapes.engine.utils.io.Uri
import java.io.File

impl interface FilePath : Path, Comparable<FilePath> {
    fun toUri(): Uri

    fun toFile(): File

    impl fun normalize(): FilePath

    impl fun resolve(other: String): FilePath

    impl fun resolve(other: FilePath): FilePath

    impl fun startsWith(other: String): Boolean

    impl fun startsWith(other: FilePath): Boolean

    impl fun relativize(other: FilePath): FilePath?

    impl val fileName: FilePath?

    impl override val parent: FilePath?

    impl fun toAbsolutePath(): FilePath

    override fun get(path: String): Path {
        return resolve(path)
    }
}
