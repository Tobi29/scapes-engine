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

@file:JvmName("ProcessJVMKt")
@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.platform

import org.tobi29.io.filesystem.FilePath
import java.util.concurrent.ConcurrentLinkedQueue
import java.lang.Process as JProcess

actual class Process(
    val java: JProcess
) {
    internal val onStop by lazy {
        val queue = ConcurrentLinkedQueue<OnStopCallback>()
        val thread = Thread {
            val exitCode = java.waitFor()
            while (true) {
                (queue.poll() ?: break)(exitCode)
            }
        }
        thread.isDaemon = true
        thread.name = "Process-Wait"
        thread.start()
        queue
    }

    actual val exitCode: Int
        get() = try {
            java.exitValue()
        } catch (e: IllegalThreadStateException) {
            throw IllegalStateException(e)
        }

    actual val isAlive: Boolean get() = java.isAlive
}

actual fun Process.onStop(callback: (Int) -> Unit) {
    val wrapper = OnStopCallback(callback)
    var passed = false
    if (isAlive) {
        passed = true
        onStop.add(wrapper)
        if (!isAlive && !onStop.remove(wrapper)) passed = false
    }
    if (!passed) {
        callback(exitCode)
    }
}

actual fun Process.awaitStop() {
    // TODO: Should we catch interrupt exceptions?
    java.waitFor()
}

@PublishedApi
internal actual fun launchProcessImpl(
    executable: String,
    arguments: Array<out String>,
    redirectStdout: RedirectOut,
    redirectStderr: RedirectOut,
    redirectStdin: RedirectIn
): Process {
    val builder = ProcessBuilder()
    builder.command(executable, *arguments)
    builder.redirectOutput(redirectStdout.configureRedirect())
    builder.redirectError(redirectStderr.configureRedirect())
    builder.redirectInput(redirectStdin.configureRedirect())
    return Process(builder.start())
}

private fun RedirectOut.configureRedirect(
): ProcessBuilder.Redirect = when (this) {
    RedirectOut.Inherit -> ProcessBuilder.Redirect.INHERIT
    is RedirectOut.File -> path.let { path: FilePath ->
        if (append) ProcessBuilder.Redirect.appendTo(path.toFile())
        else ProcessBuilder.Redirect.to(path.toFile())
    }
}

private fun RedirectIn.configureRedirect(
): ProcessBuilder.Redirect = when (this) {
    RedirectIn.Inherit -> ProcessBuilder.Redirect.INHERIT
    is RedirectIn.File -> path.let { path: FilePath ->
        ProcessBuilder.Redirect.from(path.toFile())
    }
}

actual inline fun checkChildProcesses() {}

internal class OnStopCallback(
    private val callback: (Int) -> Unit
) {
    operator fun invoke(exitCode: Int) = callback(exitCode)
}
