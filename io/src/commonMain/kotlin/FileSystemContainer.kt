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

package org.tobi29.io

import org.tobi29.stdex.ConcurrentHashMap

class FileSystemContainer : Path {
    private val fileSystems = ConcurrentHashMap<String, Path>()

    fun registerFileSystem(id: String, path: Path) {
        fileSystems.put(id, path)
    }

    fun removeFileSystem(id: String) {
        fileSystems.remove(id)
    }

    override fun get(path: String): Path {
        val location = splitPath(path)
        return fileSystem(location.first)[location.second]
    }

    override val parent: Path?
        get() {
            // This is a container of parent-less roots, having a parent speaks
            // plenty sense
            return null
        }

    override fun channel() =
        throw IOException("File system container cannot have data")

    private fun fileSystem(id: String): Path {
        val fileSystem = fileSystems[id] ?: throw IllegalArgumentException(
            "Unknown file system: $id"
        )
        return fileSystem
    }

    private fun splitPath(path: String): Pair<String, String> {
        val split = path.split(':', limit = 2)
        if (split.size != 2) {
            throw IllegalArgumentException("Invalid path: $path")
        }
        return Pair(split[0], split[1])
    }
}
