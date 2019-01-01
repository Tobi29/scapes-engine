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
package org.tobi29.application.swt.framework

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import org.eclipse.swt.SWT
import org.eclipse.swt.program.Program
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.tobi29.application.Application
import org.tobi29.application.StatusCode
import org.tobi29.application.swt.platform.*
import org.tobi29.application.swt.widgets.Dialogs
import org.tobi29.args.CommandLine
import org.tobi29.io.*
import org.tobi29.io.filesystem.FilePath
import org.tobi29.io.filesystem.createTempFile
import org.tobi29.io.filesystem.write
import org.tobi29.utils.sleepAtLeast
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

abstract class GuiApplication(
    val taskExecutor: CoroutineContext = Dispatchers.Default
) : Application() {
    val display: Display by lazy {
        Display.setAppName(name)
        Display.setAppVersion(version.toString())
        Display.getDefault()
    }
    val uiContext by lazy {
        DisplayDispatcher(display)
    }

    fun message(
        style: Int,
        title: String,
        message: String
    ): Int {
        val shell = activeShell ?: return 0
        return Dialogs.openMessage(shell, style, title, message)
    }

    val activeShell: Shell?
        get() {
            val shell = display.activeShell
            if (shell == null) {
                val shells = display.shells
                if (shells.isEmpty()) {
                    return null
                }
                return shells[0]
            }
            return shell
        }

    fun access(runnable: () -> Unit) {
        display.syncExec(runnable)
    }

    fun accessAsync(runnable: () -> Unit) {
        display.asyncExec(runnable)
    }

    override suspend fun execute(commandLine: CommandLine): StatusCode {
        try {
            initApplication(commandLine)
            while (!done()) {
                if (!display.readAndDispatch()) {
                    display.sleep()
                }
            }
            display.dispose()
            disposeApplication()
        } catch (e: Throwable) {
            crash(e)
        }
        return 0
    }

    fun done(): Boolean {
        return display.isDisposed || display.shells.isEmpty()
    }

    fun initApplication(commandLine: CommandLine) {
        init(commandLine)
    }

    fun disposeApplication() {
        dispose()
    }

    fun crash(e: Throwable): Nothing {
        val path = writeCrash(e) ?: exitProcess(1)
        if (!Program.launch(path.toString())) {
            display.asyncExec {
                Program.launch(path.toString())
                exitProcess(1)
            }
            sleepAtLeast(1000)
        }
        exitProcess(1)
    }

    private fun writeCrash(e: Throwable): FilePath? {
        try {
            val path = createTempFile("CrashReport", ".txt")
            write(path) {
                it.writeCrashReport(
                    e, "SWT Application",
                    crashReportSectionStacktrace(e),
                    crashReportSectionActiveThreads(),
                    crashReportSectionSystemProperties()
                )
            }
            return path
        } catch (e1: IOException) {
        }
        return null
    }

    protected abstract fun init(commandLine: CommandLine)

    protected abstract fun dispose()

    companion object {
        val platform: Platform = when (SWT.getPlatform()) {
            "gtk" -> PlatformLinux()
            "cocoa" -> PlatformMacOSX()
            "win32" -> PlatformWindows()
            else -> PlatformUnknown()
        }
    }
}

class DisplayDispatcher(private val display: Display) : CoroutineDispatcher() {
    override fun dispatch(
        context: CoroutineContext,
        block: Runnable
    ) {
        display.asyncExec(block)
    }
}
