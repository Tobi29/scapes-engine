/*
 * Copyright 2012-2016 Tobi29
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
package org.tobi29.scapes.engine.swt.util.framework

import mu.KLogging
import org.eclipse.swt.SWT
import org.eclipse.swt.program.Program
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.tobi29.scapes.engine.swt.util.platform.*
import org.tobi29.scapes.engine.swt.util.widgets.Dialogs
import org.tobi29.scapes.engine.utils.Crashable
import org.tobi29.scapes.engine.utils.Version
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath
import org.tobi29.scapes.engine.utils.io.filesystem.createTempFile
import org.tobi29.scapes.engine.utils.io.filesystem.writeCrashReport
import org.tobi29.scapes.engine.utils.math.clamp
import org.tobi29.scapes.engine.utils.sleepAtLeast
import org.tobi29.scapes.engine.utils.task.TaskExecutor
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

abstract class Application : Runnable, Crashable {

    val display: Display
    val taskExecutor: TaskExecutor
    private var timerSchedule: Long = Long.MIN_VALUE

    protected constructor(name: String, id: String, version: Version) {
        Display.setAppName(name)
        Display.setAppVersion(version.toString())
        display = Display.getDefault()
        taskExecutor = TaskExecutor(this, id)
    }

    protected constructor(name: String, id: String, version: Version,
                          taskExecutor: TaskExecutor) {
        Display.setAppName(name)
        Display.setAppVersion(version.toString())
        display = Display.getDefault()
        this.taskExecutor = TaskExecutor(taskExecutor, id)
    }

    fun message(style: Int,
                title: String,
                message: String): Int {
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

    override fun run() {
        try {
            init()
            tick(timerSchedule)
            while (!done()) {
                if (!display.readAndDispatch()) {
                    tick(timerSchedule)
                    display.sleep()
                }
            }
            dispose()
            taskExecutor.shutdown()
        } catch (e: Throwable) {
            crash(e)
        }

        display.dispose()
    }

    fun done(): Boolean {
        return display.shells.isEmpty()
    }

    fun initApplication() {
        init()
    }

    fun disposeApplication() {
        dispose()
        taskExecutor.shutdown()
    }

    override fun crash(e: Throwable) {
        logger.error(e) { "Application crashed:" }
        val path = writeCrash(e)
        if (path == null) {
            System.exit(1)
            return
        }
        if (!Program.launch(path.toString())) {
            display.asyncExec {
                Program.launch(path.toString())
                System.exit(1)
            }
            sleepAtLeast(1000)
            System.exit(1)
        }
    }

    private fun tick(schedule: Long) {
        val sleep = clamp(taskExecutor.tick(), 1,
                Int.MAX_VALUE.toLong()).toInt()
        if (timerSchedule == schedule) {
            timerSchedule++
            val nextSchedule = timerSchedule
            display.timerExec(sleep) {
                tick(nextSchedule)
            }
        }
    }

    private fun writeCrash(e: Throwable): FilePath? {
        try {
            val path = createTempFile("CrashReport", ".txt")
            val debugValues = ConcurrentHashMap<String, String>()
            writeCrashReport(e, path, "ScapesEngine", debugValues)
            return path
        } catch (e1: IOException) {
        }
        return null
    }

    protected abstract fun init()

    protected abstract fun dispose()

    companion object : KLogging() {
        val platform: Platform

        init {
            when (SWT.getPlatform()) {
                "gtk" -> platform = PlatformLinux()
                "cocoa" -> platform = PlatformMacOSX()
                "win32" -> platform = PlatformWindows()
                else -> {
                    logger.warn { "Unknown SWT platform: ${SWT.getPlatform()}" }
                    platform = PlatformUnknown()
                }
            }
        }
    }
}
