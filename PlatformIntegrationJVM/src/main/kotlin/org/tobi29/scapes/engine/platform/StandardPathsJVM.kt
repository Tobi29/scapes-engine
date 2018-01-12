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

package org.tobi29.scapes.engine.platform

import org.tobi29.scapes.engine.utils.io.filesystem.FilePath
import org.tobi29.scapes.engine.utils.io.filesystem.path
import java.io.File

private val impl: StandardPathsImpl by lazy {
    when (PLATFORM) {
        Platform.LINUX -> XDGPathsImpl()
        Platform.MACOS -> MacOSPathsImpl()
        Platform.UNIX -> TODO()
        Platform.WINDOWS -> WindowsPathsImpl()
        Platform.ANDROID -> AndroidPathsImpl()
        else -> throw UnsupportedOperationException("Unsupported platform")
    }
}

actual val homeDir: FilePath get() = impl.homeDir
actual val configHome: FilePath get() = impl.configHome
actual val configDirs: List<FilePath> get() = impl.configDirs
actual val dataHome: FilePath get() = impl.dataHome
actual val dataDirs: List<FilePath> get() = impl.dataDirs
actual val cacheHome: FilePath get() = impl.cacheHome

actual fun appIDForConfig(id: String,
                        name: String): FilePath = impl.appIDForConfig(id, name)

actual fun appIDForData(id: String,
                      name: String): FilePath = impl.appIDForData(id, name)

actual fun appIDForCache(id: String,
                       name: String): FilePath = impl.appIDForCache(id, name)

private interface StandardPathsImpl {
    val homeDir: FilePath
    val configHome: FilePath
    val configDirs: List<FilePath>
    val dataHome: FilePath
    val dataDirs: List<FilePath>
    val cacheHome: FilePath

    fun appIDForConfig(id: String,
                       name: String): FilePath

    fun appIDForData(id: String,
                     name: String): FilePath

    fun appIDForCache(id: String,
                      name: String): FilePath
}

private class XDGPathsImpl : StandardPathsImpl {
    override val homeDir = XDGBaseDirs().HOME_DIR
    override val configHome = XDGBaseDirs().CONFIG_HOME
    override val configDirs = XDGBaseDirs().CONFIG_DIRS
    override val dataHome = XDGBaseDirs().DATA_HOME
    override val dataDirs = XDGBaseDirs().DATA_DIRS
    override val cacheHome = XDGBaseDirs().CACHE_HOME

    override fun appIDForConfig(id: String,
                                name: String) =
            path(name.toLowerCase().replace(' ', '-'))

    override fun appIDForData(id: String,
                              name: String) =
            path(name.toLowerCase().replace(' ', '-'))

    override fun appIDForCache(id: String,
                               name: String) =
            path(name.toLowerCase().replace(' ', '-'))
}

private class MacOSPathsImpl : StandardPathsImpl {
    override val homeDir = path(System.getProperty("user.home"))
    override val configHome = homeDir.resolve("Library/Preferences")
    override val configDirs = listOf(configHome)
    override val dataHome = homeDir.resolve("Library")
    override val dataDirs = listOf(dataHome)
    override val cacheHome = homeDir.resolve("Library/Caches")

    override fun appIDForConfig(id: String,
                                name: String) = path(id)

    override fun appIDForData(id: String,
                              name: String) = path(name)

    override fun appIDForCache(id: String,
                               name: String) = path(id)
}

private class WindowsPathsImpl : StandardPathsImpl {
    override val homeDir = path(System.getProperty("user.home"))
    override val configHome = path(EnvironmentVariable["APPDATA"]
            ?: throw IllegalStateException(
            "Platform does not expose \$APPDATA environment variable"))
    override val configDirs = listOf(configHome)
    override val dataHome = configHome
    override val dataDirs = configDirs
    override val cacheHome = configHome

    override fun appIDForConfig(id: String,
                                name: String) = path(name)

    override fun appIDForData(id: String,
                              name: String) = path(name)

    override fun appIDForCache(id: String,
                               name: String) = path(name).resolve("Cache")
}

private class AndroidPathsImpl : StandardPathsImpl {
    private val context = Context.getApplicationContext()

    override val homeDir = path(Environment.getExternalStorageDirectory())
    override val configHome = path(context.getFilesDir())
    override val configDirs = listOf(configHome)
    override val dataHome = path(context.getFilesDir())
    override val dataDirs = listOf(dataHome)
    override val cacheHome = path(context.getCacheDir())

    override fun appIDForConfig(id: String,
                                name: String) = path("")

    override fun appIDForData(id: String,
                              name: String) = path("")

    override fun appIDForCache(id: String,
                               name: String) = path("")

    private class Context(private val handle: Any) {
        fun getFilesDir(): File = getFilesDir.invoke(handle) as File
        fun getCacheDir(): File = getCacheDir.invoke(handle) as File

        companion object {
            private val clazz = Class.forName("android.app.Context")
            private val getApplicationContext = clazz.getMethod(
                    "getApplicationContext")
            private val getFilesDir = clazz.getMethod("getFilesDir")
            private val getCacheDir = clazz.getMethod("getCacheDir")

            fun getApplicationContext(): Context =
                    Context(getApplicationContext.invoke(null))
        }
    }

    private class Environment(private val handle: Any) {
        companion object {
            private val clazz = Class.forName("android.os.Environment")
            private val getExternalStorageDirectory = clazz.getMethod(
                    "getExternalStorageDirectory")

            fun getExternalStorageDirectory(): File =
                    getExternalStorageDirectory.invoke(null) as File
        }
    }
}
