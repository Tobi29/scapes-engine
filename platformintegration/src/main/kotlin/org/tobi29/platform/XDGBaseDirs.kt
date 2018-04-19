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

package org.tobi29.platform

import org.tobi29.io.filesystem.FilePath
import org.tobi29.io.filesystem.path
import org.tobi29.stdex.readOnly

val HOME by EnvironmentVariable
val XDG_CONFIG_HOME by EnvironmentVariable
val XDG_CONFIG_DIRS by EnvironmentVariable
val XDG_DATA_HOME by EnvironmentVariable
val XDG_DATA_DIRS by EnvironmentVariable
val XDG_CACHE_HOME by EnvironmentVariable
val XDG_RUNTIME_DIR by EnvironmentVariable

class XDGBaseDirs private constructor() {
    val HOME_DIR: FilePath =
        path(
            HOME ?: throw IllegalStateException(
                "Platform does not expose \$HOME environment variable"
            )
        )

    val CONFIG_HOME: FilePath =
        XDG_CONFIG_HOME?.let(::path) ?: HOME_DIR.resolve(".config")

    val CONFIG_DIRS: List<FilePath> =
        (listOf(CONFIG_HOME) + (XDG_CONFIG_DIRS?.takeUnless { it.isEmpty() }
            ?.split(":")?.map { path(it) }
                ?: listOf(path("/etc/xdg")))
                ).readOnly()

    val DATA_HOME: FilePath =
        XDG_DATA_HOME?.let(::path) ?: HOME_DIR.resolve(".local/share")

    val DATA_DIRS: List<FilePath> =
        (listOf(DATA_HOME) + (XDG_DATA_DIRS?.takeUnless { it.isEmpty() }
            ?.split(":")?.map { path(it) }
                ?: listOf(path("/usr/local/share"), path("/usr/share")))
                ).readOnly()

    val CACHE_HOME: FilePath =
        XDG_CACHE_HOME?.let(::path) ?: HOME_DIR.resolve(".cache")

    val RUNTIME_DIR: FilePath? =
        XDG_RUNTIME_DIR?.let(::path)

    companion object {
        private val instance by lazy { XDGBaseDirs() }

        operator fun invoke(): XDGBaseDirs = instance
    }
}