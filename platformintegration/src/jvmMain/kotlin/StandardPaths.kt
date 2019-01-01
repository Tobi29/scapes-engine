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

@file:JvmName("StandardPathsJVMKt")

package org.tobi29.platform

import org.tobi29.io.filesystem.FilePath
import org.tobi29.io.filesystem.path
import org.tobi29.stdex.Volatile
import org.tobi29.utils.Identified
import org.tobi29.utils.Named
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

@Volatile
private var androidApplicationContext: AndroidPathsImpl.Context? = null

actual val homeDir: FilePath get() = impl.homeDir

actual fun <M> M.configHome(): FilePath where M : Identified, M : Named =
    impl.run { configHome() }

actual fun <M> M.configDirectories(): List<FilePath> where M : Identified, M : Named =
    impl.run { configDirectories() }

actual fun <M> M.dataHome(): FilePath where M : Identified, M : Named =
    impl.run { dataHome() }

actual fun <M> M.dataDirectories(): List<FilePath> where M : Identified, M : Named =
    impl.run { dataDirectories() }

actual fun <M> M.cacheHome(): FilePath where M : Identified, M : Named =
    impl.run { cacheHome() }

fun installAndroidContext(handle: Any) {
    if (androidApplicationContext != null) return

    val context = AndroidPathsImpl.Context(handle)
    val applicationContext = context.getApplicationContext()
    androidApplicationContext = applicationContext
}

fun uninstallAndroidContext() {
    androidApplicationContext = null
}

private interface StandardPathsImpl {
    val homeDir: FilePath
        get() = path(System.getProperty("user.home"))

    fun <M> M.configHome(): FilePath where M : Identified, M : Named =
        dataHome()

    fun <M> M.configDirectories(): List<FilePath> where M : Identified, M : Named =
        listOf(configHome())

    fun <M> M.dataHome(): FilePath where M : Identified, M : Named

    fun <M> M.dataDirectories(): List<FilePath> where M : Identified, M : Named =
        listOf(dataHome())

    fun <M> M.cacheHome(): FilePath where M : Identified, M : Named =
        dataHome().resolve("Cache")
}

private class XDGPathsImpl : StandardPathsImpl {
    override val homeDir: FilePath
        get() = XDGBaseDirs().HOME_DIR

    override fun <M> M.configHome(): FilePath where M : Identified, M : Named =
        forApplication(XDGBaseDirs().CONFIG_HOME)

    override fun <M> M.configDirectories(): List<FilePath> where M : Identified, M : Named =
        forApplication(XDGBaseDirs().CONFIG_DIRS)

    override fun <M> M.dataHome(): FilePath where M : Identified, M : Named =
        forApplication(XDGBaseDirs().DATA_HOME)

    override fun <M> M.dataDirectories(): List<FilePath> where M : Identified, M : Named =
        forApplication(XDGBaseDirs().DATA_DIRS)

    override fun <M> M.cacheHome(): FilePath where M : Identified, M : Named =
        forApplication(XDGBaseDirs().CACHE_HOME)

    private fun Named.directoryFor() =
        name.toLowerCase().replace(' ', '-')

    private fun Named.forApplication(path: FilePath) =
        path.resolve(directoryFor())

    // FIXME: Wrong inspection
    @Suppress("ReplaceSingleLineLet")
    private fun Named.forApplication(paths: Iterable<FilePath>) =
        directoryFor().let { d -> paths.map { it.resolve(d) } }
}

private class MacOSPathsImpl : StandardPathsImpl {
    // TODO: Can we do better here?

    override fun <M> M.configHome(): FilePath where M : Identified, M : Named =
        homeDir.resolve("Library/Preferences").resolve(id)

    override fun <M> M.dataHome(): FilePath where M : Identified, M : Named =
        homeDir.resolve("Library").resolve(name)

    override fun <M> M.cacheHome(): FilePath where M : Identified, M : Named =
        homeDir.resolve("Library/Caches").resolve(id)
}

private class WindowsPathsImpl : StandardPathsImpl {
    override fun <M> M.dataHome(): FilePath where M : Identified, M : Named =
        path(
            EnvironmentVariable["APPDATA"]
                    ?: throw IllegalStateException(
                        "Platform does not expose \$APPDATA environment variable"
                    )
        ).resolve(name)
}

private class AndroidPathsImpl : StandardPathsImpl {
    private val context: Context
        get() = androidApplicationContext
                ?: error("Context was not install, consider calling 'installAndroidContext' or using the Android APIs directly")

    override val homeDir: FilePath
        get() = path(Environment.getExternalStorageDirectory())

    override fun <M> M.dataHome(): FilePath where M : Identified, M : Named =
        path(context.getFilesDir())

    override fun <M> M.cacheHome(): FilePath where M : Identified, M : Named =
        path(context.getCacheDir())

    class Context(private val handle: Any) {
        fun getFilesDir(): File =
            getFilesDir.invoke(handle) as File

        fun getCacheDir(): File =
            getCacheDir.invoke(handle) as File

        fun getApplicationContext(): Context =
            Context(getApplicationContext.invoke(handle))

        companion object {
            private val clazz = Class.forName("android.content.Context")
            private val getApplicationContext =
                clazz.getMethod("getApplicationContext")
            private val getFilesDir =
                clazz.getMethod("getFilesDir")
            private val getCacheDir =
                clazz.getMethod("getCacheDir")
        }
    }

    class Environment(private val handle: Any) {
        companion object {
            private val clazz = Class.forName("android.os.Environment")
            private val getExternalStorageDirectory =
                clazz.getMethod("getExternalStorageDirectory")

            fun getExternalStorageDirectory(): File =
                getExternalStorageDirectory.invoke(null) as File
        }
    }
}
