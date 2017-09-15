package org.tobi29.scapes.engine.platform

import org.tobi29.scapes.engine.utils.io.filesystem.FilePath
import org.tobi29.scapes.engine.utils.io.filesystem.path
import org.tobi29.scapes.engine.utils.readOnly

val HOME by EnvironmentVariable
val XDG_CONFIG_HOME by EnvironmentVariable
val XDG_CONFIG_DIRS by EnvironmentVariable
val XDG_DATA_HOME by EnvironmentVariable
val XDG_DATA_DIRS by EnvironmentVariable
val XDG_CACHE_HOME by EnvironmentVariable
val XDG_RUNTIME_DIR by EnvironmentVariable

class XDGBaseDirs private constructor() {
    val HOME_DIR: FilePath =
            path(HOME ?: throw IllegalStateException(
                    "Platform does not expose \$HOME environment variable"))

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